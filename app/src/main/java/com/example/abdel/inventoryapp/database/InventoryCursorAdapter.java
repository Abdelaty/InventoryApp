package com.example.abdel.inventoryapp.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abdel.inventoryapp.R;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry;
import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME;
import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE;
import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY;
import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameView = view.findViewById(R.id.product_name);
        TextView productQuantityView = view.findViewById(R.id.quantity);
        TextView productSupplierView = view.findViewById(R.id.supplier_name);
        TextView productPriceView = view.findViewById(R.id.price);
        ImageView buyImage = view.findViewById(R.id.buy_button);

        int nameColumn = cursor.getColumnIndex(COLUMN_PRODUCT_NAME);
        int quantityColumn = cursor.getColumnIndex(COLUMN_PRODUCT_QUANTITY);
        int supplierColumn = cursor.getColumnIndex(COLUMN_PRODUCT_SUPPLIER);
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        int priceColumn = cursor.getColumnIndex(COLUMN_PRODUCT_PRICE);

        String name = cursor.getString(nameColumn);
        double price = cursor.getDouble(priceColumn);
        final int quantity = cursor.getInt(quantityColumn);
        String supplier = cursor.getString(supplierColumn);

        productNameView.setText(name);
        productQuantityView.setText(String.valueOf(quantity));
        productPriceView.setText(String.valueOf(price) + "$");
        productSupplierView.setText(supplier);

        buyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productIdColumnIndex);
                adjustProductQuantity(context, productUri, quantity);
            }
        });
    }

    private void adjustProductQuantity(Context context, Uri productUri, int currentQuantityInStock) {

        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

        if (currentQuantityInStock == 0) {
            makeText(context.getApplicationContext(), R.string.toast_out_of_stock, LENGTH_SHORT).show();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            makeText(context.getApplicationContext(), R.string.buy_msg_confirm, LENGTH_SHORT).show();
        } else {
            makeText(context.getApplicationContext(), R.string.no_product_in_stock, LENGTH_SHORT).show();

        }
    }
}
