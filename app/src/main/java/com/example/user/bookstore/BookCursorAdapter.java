package com.example.user.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.bookstore.data.BookContract;

public class BookCursorAdapter extends CursorAdapter {


    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.titleBook);
        TextView type = (TextView) view.findViewById(R.id.typeBook);
        TextView quantity = (TextView) view.findViewById(R.id.quantityBook);
        TextView price = (TextView) view.findViewById(R.id.priceBook);
        ImageView sales = (ImageView) view.findViewById(R.id.sales);

        int titleColumn = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
        int typeColumn = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_TYPE);
        int quantityColumn = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
        int priceColumn = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);

        String bookTitle = cursor.getString(titleColumn);
        Integer bookType = cursor.getInt(typeColumn);
        String bookQuantity = cursor.getString(quantityColumn);
        String bookPrice = cursor.getString(priceColumn);
        String bookDet = "";
        if (bookType == 0){
             bookDet = context.getString(R.string.unknown);
        } else if (bookType == 1) {
             bookDet = context.getString(R.string.fiction);
        } else {
            bookDet = context.getString(R.string.non_fiction);
        }
        title.setText(bookTitle);
        type.setText(bookDet);
        quantity.setText(bookQuantity);
        price.setText(bookPrice);


        final int bookId = cursor.getInt(cursor.getColumnIndex(BookContract.BookEntry._ID));
        final int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
        String cQuantity = cursor.getString(quantityColumnIndex);
        final int currentQuantity = Integer.valueOf(cQuantity);


        sales.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentQuantity > 0) {
                    int newQuantity = currentQuantity - 1;
                    Uri quantityUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, bookId);

                    ContentValues values = new ContentValues();
                    values.put(BookContract.BookEntry.COLUMN_QUANTITY, newQuantity);
                    context.getContentResolver().update(quantityUri, values, null, null);
                } else {
                    Toast.makeText(context, "This book is out of stock.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



}
