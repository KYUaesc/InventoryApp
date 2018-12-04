package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 *
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source.
 * create list items for each row of data in the {@link Cursor}.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    // constructor
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    // makes a new blank list item view. No data is set (or bound) to the views yet.
    // returns a newly created list item
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // insert a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    // binds data at current cursor to given list item layout
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find text view in list item to populate
        TextView nameTextView = view.findViewById(R.id.item_name);
        TextView priceTextView = view.findViewById(R.id.item_price);
        TextView qtyTextView = view.findViewById(R.id.item_quantity);

        // extract properties
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_PRICE));
        int qty = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_QTY));

        // convert integers to strings
        String priceString = '$' + Integer.toString(price);
        String qtyString = Integer.toString(qty);

        // Populate fields with extracted properties
        nameTextView.setText(nameString);
        priceTextView.setText(priceString);
        qtyTextView.setText(qtyString);
    }

}
