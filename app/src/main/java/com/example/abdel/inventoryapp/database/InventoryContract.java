package com.example.abdel.inventoryapp.database;

import android.provider.BaseColumns;

public class InventoryContract {
        private InventoryContract() {
        }

        public static final class InventoryEntry implements BaseColumns {


            public static final String TABLE_NAME = "inventory";
            public static final String _ID = BaseColumns._ID;
            public static final String COLUMN_PRODUCT_NAME = "name";
            public static final String COLUMN_PRODUCT_PRICE = "price";
            public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
            public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
            public static final String COLUMN_PRODUCT_SUPPLIER_PHONE = "phone";
        }


    }

