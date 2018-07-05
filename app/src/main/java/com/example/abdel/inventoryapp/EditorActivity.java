package com.example.abdel.inventoryapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.abdel.inventoryapp.database.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 0;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    /**
     * EditText field to enter the Product's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the Product's name
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the Product's name
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the Product's breed
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the Product's weight
     */
    private Uri mCurrentProductUri;
    private EditText mPhoneEditText;
    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mCurrentProductUri = getIntent().getData();
        ImageView callImageView = findViewById(R.id.image_view_order);
        final ImageView upImageView = findViewById(R.id.image_view_up);
        final ImageView downImageView = findViewById(R.id.image_view_down);

        if (mCurrentProductUri == null) {
            setTitle(R.string.editor_activity_title_new_product);
            callImageView.setVisibility(View.GONE);
            upImageView.setVisibility(View.GONE);
            downImageView.setVisibility(View.GONE);
            invalidateOptionsMenu();

        } else {
            setTitle(R.string.editor_activity_title_edit_product);
            callImageView.setVisibility(View.VISIBLE);
            upImageView.setVisibility(View.VISIBLE);
            downImageView.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierEditText = findViewById(R.id.edit_product_supplier);
        mPhoneEditText = findViewById(R.id.edit_product_phone);

        upImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                adjustProductQuantity(mCurrentProductUri, quantity, upImageView);
            }
        });
        downImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                adjustProductQuantity(mCurrentProductUri, quantity, downImageView);
            }
        });
        callImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validation()) {
                    saveProduct();
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameProductColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int nameSupplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int phoneSupplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            String nameProduct = cursor.getString(nameProductColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String nameSupplier = cursor.getString(nameSupplierColumnIndex);
            String phoneSupplier = cursor.getString(phoneSupplierColumnIndex);

            mNameEditText.setText(nameProduct);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(nameSupplier);
            mPhoneEditText.setText(phoneSupplier);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
        mQuantityEditText.setText(String.valueOf(0));
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
    }


    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean validation() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String nameSupplierString = mSupplierEditText.getText().toString().trim();
        String phoneSupplierString = mPhoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Name product invalid!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "You must enter price!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "You must enter quantity!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(nameSupplierString)) {
            Toast.makeText(this, "Name supplier invalid!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidPhone(phoneSupplierString)) {
            Toast.makeText(this, "Phone invalid!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean isValidPhone(String phone) {
        String regexPhone = "\\(?\\+[0-9]{1,3}\\)? ?-?[0-9]{1,3} ?-?[0-9]{3,5} ?-?[0-9]{4}( ?-?[0-9]{3})?";
        Pattern pattern = Pattern.compile(regexPhone);
        Matcher matcher = pattern.matcher(phone);

        return matcher.find();
    }

    private void makePhoneCall() {
        String phoneNumber = mPhoneEditText.getText().toString().trim();
        if (isValidPhone(phoneNumber)) {
            if (ContextCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
            }

        } else {
            Toast.makeText(this, "Phone Number is invalid!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String nameSupplierString = mSupplierEditText.getText().toString().trim();
        String phoneSupplierString = mPhoneEditText.getText().toString().trim();

        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(nameSupplierString) && TextUtils.isEmpty(phoneSupplierString)) {
            return;
        }

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);

        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, nameSupplierString);

        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, phoneSupplierString);

        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void adjustProductQuantity(Uri productUri, int currentQuantityInStock, View v) {
        if (v.getId() == R.id.image_view_up) {
            int newQuantityValue = (currentQuantityInStock >= 0) ? currentQuantityInStock + 1 : 0;

            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
            int numRowsUpdated = getContentResolver().update(productUri, contentValues, null, null);
            if (numRowsUpdated > 0) {
                Toast.makeText(getApplicationContext(), R.string.increase_quantity, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();

            }
        } else if (v.getId() == R.id.image_view_down) {
            int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

            if (currentQuantityInStock == 0) {
                Toast.makeText(getApplicationContext(), R.string.toast_out_of_stock, Toast.LENGTH_SHORT).show();
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
            int numRowsUpdated = getContentResolver().update(productUri, contentValues, null, null);
            if (numRowsUpdated > 0) {
                Toast.makeText(getApplicationContext(), R.string.decrease_quantity, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
