package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Detail Activity for user to input data
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailActivity.class.getName();

    /** Identifier for data loader */
    private static final int EXISTING_LOADER  = 0 ;


    /** Boolean flag that keeps track of whether the entry has been edited (true) or not (false) */
    private boolean mHasChanged = false;

    // OnTouchListener that listens for any user touches on a View
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHasChanged = true;
            view.performClick();
            return false;
        }

    };

    /** Content URI for the existing entry (null if it's a new ) */
    private Uri mCurrentUri;

    /** EditText field to enter inventory name */
    private EditText mNameEditText;

    /** EditText field to enter inventory price */
    private EditText mPriceEditText;

    /** EditText field to enter inventory quantity */
    private EditText mQuantityEditText;

    // Button to decrease inventory quantity
    private Button mDecrQtyButton;

    // Button to decrease inventory quantity
    private Button mIncrQtyButton;

    // Button to order from supplier
    private Button mOrderButton;

    // hold current quantity
    private long mQty = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup layout
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        if(mCurrentUri == null){
            // THis is a new inventory; change the app bar to say "Add Inventory"
            setTitle(R.string.detail_activity_title_new_inventory);
            // invalidates the "delete" option menu
            invalidateOptionsMenu();
        }else{
            // Otherwise this is an existing one; change app bar to say "Edit Inventory"
            setTitle(R.string.detail_activity_title_edit_inventory);
        }

        // find relevant views from which it read inputs
        mNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);

        // find buttons
        mDecrQtyButton = findViewById(R.id.button_decrease);
        mIncrQtyButton = findViewById(R.id.button_increase);
        mOrderButton = findViewById(R.id.order_button);

        // set up onTouchListener
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        // increase and update quantity when increase button is clicked
        mIncrQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQty++;
                mQuantityEditText.setText(String.valueOf(mQty));
            }
        });

        // decrease and update auantity when decrease button is clicked
        mDecrQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mQty > 0){
                    mQty--;
                    mQuantityEditText.setText(String.valueOf(mQty));
                }
                else{
                    Toast.makeText(DetailActivity.this,R.string.editor_no_negative,Toast.LENGTH_SHORT).show();
                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //implicit intent to email
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("*/*");
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }

            }
        });

        // Initialize a loader to read the inventor  data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_LOADER,null,this);
    }

    /**
     * Get user input from editor and save new inventory into database
     * Insert it if it's a new
     * OR update it if it already exists
     */
    private void saveInventory(){

        // Read from input fields
        // use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString= mPriceEditText.getText().toString().trim();
        String qtyString = mQuantityEditText.getText().toString().trim();

        // return early if all inputs are empty
        if(mCurrentUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(qtyString)){
            Toast.makeText(this,R.string.editor_no_field,Toast.LENGTH_SHORT).show();
            return;
        }

        // return early and show toast message if any field is not provided
        if(TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(qtyString)){
            Toast.makeText(this,R.string.editor_no_field,Toast.LENGTH_SHORT).show();
            return;
        }

        // parse price into integers
        long price = Long.parseLong(priceString);
        long qty =  Long.parseLong(qtyString);

        // validates price and qty
        if(qty < 0 || price < 0){
            Toast.makeText(this,R.string.editor_no_negative,Toast.LENGTH_SHORT).show();
            return;
        }

        if(qty > 999999999 || price > 999999999){
            Toast.makeText(this,R.string.editor_number_too_large,Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME,nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE,price);
        values.put(InventoryEntry.COLUMN_INVENTORY_QTY,qty);

        // Insert if it is new  OR update it if it already exists
        if(mCurrentUri == null){
            // Insert a new entry into the provider, returning the content URI
            Uri returnUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // show toast message depending on whether the insertion was successful
            if(returnUri == null){
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_save_failed, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.editor_save_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            // update entry,  returning the content URI
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // show toast message depending on whether the insertion was successful
            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error
                Toast.makeText(this, R.string.editor_save_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_save_successful, Toast.LENGTH_SHORT).show();
            }
        }

        // Exit activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new inventory; hide the "Delete" menu item
        if(mCurrentUri == null){
            // find the "delete" option item and set invisibile
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save inventory to database
                saveInventory();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // pop up the confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the entry hasn't changed, continue with navigating up to parent activity
                if (!mHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion in the database.
     */
    private void deleteInventory() {

        if(mCurrentUri != null) {
            // Call the ContentResolver to delete the entry  at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // show toast message depending on whether the deletion was successful
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_successful, Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Bail early if the Uri is null
        if (mCurrentUri == null) {
            return null;
        }
        // Since the editor shows all attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = new String[]{
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QTY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,            // Query the content URI for the current entry
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Extract out the value from the Cursor for the given column index
            String name = data.getString(data.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_NAME));
            String price = data.getString(data.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_PRICE));
            String qty = data.getString(data.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_QTY));

            // updates quantity
            mQty = Long.parseLong(qty);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(qty);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mPriceEditText.setText("");
    }
}
