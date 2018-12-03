package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
/**
 * content provider that access database and perform CRUD operations
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // database helper
    private InventoryDbHelper mDbHelper;

    // URI matcher code for the content URI for the whole inventory table
    private static final int INVENTORY = 100;

    // URI matcher code for the content URI for a single inventory in the table
    private static final int INVENTORY_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The content URI of the form "content://com.example.android.inventoryapp/inventory" is map to code INVENTORY
        // This URI is used to provide access to MULTIPLE rows of the inventory table.
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY,INVENTORY);

        // The content URI of the form "content://com.example.android.inventoryapp/inventory/#" is map to code INVENTORY
        // This URI is used to provide access to single row of the inventory table.
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY + "/#",INVENTORY_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);

        switch (match){
            case INVENTORY:
                // query with given projection, selection, selection arguments, and sort order.
                // The cursor could contain multiple rows of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null, null, sortOrder);
                break;

            case INVENTORY_ID:
                //  extract out the ID from the URI.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        if (getContext() != null){
            // Set notification URI on the Cursor;  update the Cursor if data at this uri changes
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        // return the cursor
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     *
     * content://com.example.android.pets/pets → Returns directory MIME type
     * content://com.example.android.pets/pets/# → Returns item MIME type
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch ((match)){
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return insertInventory(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a inventory into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventory(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
        if(name == null || name.length()==0){
            throw new IllegalArgumentException("Inventory requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if(price != null && price <0){
            throw new IllegalArgumentException("Inventory shall have a valid price");
        }

        // check validity of quantity
        Integer qty = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QTY);
        if(qty != null && qty <0){
            throw new IllegalArgumentException("Inventory shall have valid quantity");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // insert new pet with given ContentValue, return id
        long id = database.insert(InventoryEntry.TABLE_NAME,null, values);

        // if fail, log an error and return null,
        if(id ==-1){
            Log.e(LOG_TAG,"Failed to insert row for" + uri);
            return null;
        }

        if (getContext() != null) {
            // Notify all listeners that the data has changed for the pet content URI
            // uri: content://com.example.android.inventoryapp/inventory
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if(rowsDeleted != 0  && getContext() != null){
                // Notify all listeners that the data has changed for the pet content URI
                getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventory in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Check that the name is not null
        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)){
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if(name == null || name.length()==0){
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        // check that the price value is valid.
        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            // If the weight is provided, check that it's greater than or equal to 0
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Inventory shall have a valid price");
            }
        }

        // check that the quantity value is valid.
        if(values.containsKey(InventoryEntry.COLUMN_INVENTORY_QTY)) {
            Integer qty = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (qty != null && qty < 0) {
                throw new IllegalArgumentException("Inventory shall have valid quantity");
            }
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME,values,selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if(rowsUpdated != 0 && getContext() != null){
            // Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri,null);
        }

        // Return the number of rows that were affected
        return rowsUpdated;
    }
}
