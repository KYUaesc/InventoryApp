package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * single entry in Inventory Database
 * (Define schema & convention for where to find database constants)
 */

public class InventoryContract {

    private static final String TAG = "Inventory";

    // name for entire content provider, i.e. package name for the app
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    // creating base of URI for contacting content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path (appended to base content URI for possible URI's)
    public static final String PATH_INVENTORY = "inventory";

    // empty constructor
    private InventoryContract(){
    }

    public static class InventoryEntry implements BaseColumns{

        /** The content URI to access the all inventory data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        /** Name of database table for inventory */
        public static final String TABLE_NAME = "inventory";

        /**
         * Unique ID number for single inventory (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String ID = BaseColumns._ID;

        /**
         * Name of the inventory.
         *
         * Type: TEXT
         */
        public static final String COLUMN_INVENTORY_NAME = "name";


        /**
         * price of the inventory
         *
         * Type: INTEGER
         */
        public static final String COLUMN_INVENTORY_PRICE = "price";


        /**
         * quantity of the inventory
         *
         * Type: INTEGER
         */
        public static final String COLUMN_INVENTORY_QTY = "quantity";
    }
}
