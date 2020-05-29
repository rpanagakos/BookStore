package com.example.user.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    private BookContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.user.bookstore";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    public static final class BookEntry implements BaseColumns{

        /** The content URI to access the book data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public final static String TABLE_NAME = "books";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_TYPE = "type";
        public final static String COLUMN_SUPPLIER_NAME = "sname";
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "sphone";

        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_FICTION = 1;
        public static final int TYPE_NFICTION = 2;

        public static boolean isValidType(int type) {
            if (type == TYPE_UNKNOWN || type == TYPE_FICTION || type == TYPE_NFICTION) {
                return true;
            }
            return false;
        }
    }
}
