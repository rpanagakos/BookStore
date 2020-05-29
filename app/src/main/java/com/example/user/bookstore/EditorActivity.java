package com.example.user.bookstore;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.app.LoaderManager;
import android.content.CursorLoader;

import com.example.user.bookstore.data.BookContract;
import com.example.user.bookstore.data.BookDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.name) EditText bookName;
    @BindView(R.id.price) EditText priceBook;
    @BindView(R.id.quantity) EditText quantityBook;
    @BindView(R.id.supplierName) EditText sName;
    @BindView(R.id.supplierPhone) EditText sPhone;
    @BindView(R.id.spinner_type) Spinner spinnerType;
    @BindView(R.id.phoneCall) ImageView phoneCall;
    @BindView(R.id.decreaseQuantity) ImageView decreaseQuantity;
    @BindView(R.id.increaseQuantity) ImageView increaseQuantity;

    private static final int EXISTING_BOOK_LOADER = 0;
    private Uri mCurrentBookUri;

    private boolean mBookHasChanged = false;

    private int typeBook = BookContract.BookEntry.TYPE_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if(mCurrentBookUri == null){
            setTitle("Add a Book");
        } else {
            setTitle("Edit a Book");
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        setupSpinner();

        bookName.setOnTouchListener(mTouchListener);
        priceBook.setOnTouchListener(mTouchListener);
        quantityBook.setOnTouchListener(mTouchListener);
        sName.setOnTouchListener(mTouchListener);
        sPhone.setOnTouchListener(mTouchListener);
        spinnerType.setOnTouchListener(mTouchListener);
        phoneCall.setOnClickListener(phoneCallAction);
        decreaseQuantity.setOnClickListener(decreaseAction);
        increaseQuantity.setOnClickListener(increaseAction);

    }

    private View.OnClickListener decreaseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quantityValue = quantityBook.getText().toString();

            int preQuantity = Integer.parseInt(quantityValue);
            if ((preQuantity - 1) >= 0) {
                quantityBook.setText(String.valueOf(preQuantity - 1));
            } else {
                Toast.makeText(EditorActivity.this, "It can't be less than zero.", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    };

    private View.OnClickListener increaseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quantityValue = quantityBook.getText().toString();

                int prevQuantity = Integer.parseInt(quantityValue);
                quantityBook.setText(String.valueOf(prevQuantity + 1));

        }
    };

    private View.OnClickListener phoneCallAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String phoneNumber = sPhone.getText().toString().trim();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    };

    private void setupSpinner() {
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerType.setAdapter(typeSpinnerAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.fiction))) {
                        typeBook = BookContract.BookEntry.TYPE_FICTION;
                    } else if (selection.equals(getString(R.string.non_fiction))){
                        typeBook = BookContract.BookEntry.TYPE_NFICTION;
                    } else {
                        typeBook = BookContract.BookEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                typeBook = BookContract.BookEntry.TYPE_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.new_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBookDetails();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveBookDetails() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameBooksString = bookName.getText().toString().trim();
        String priceBookString = priceBook.getText().toString().trim();
        String quantityBookString = quantityBook.getText().toString().trim();
        String sNameString = sName.getText().toString().trim();
        String sPhoneString = sPhone.getText().toString().trim();

        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameBooksString) && TextUtils.isEmpty(priceBookString) && TextUtils.isEmpty(sPhoneString) && TextUtils.isEmpty(sNameString) &&
                TextUtils.isEmpty(quantityBookString) && typeBook == BookContract.BookEntry.TYPE_UNKNOWN) {
            Toast.makeText(EditorActivity.this, "You have to fill the blanks to save a book", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();



        int phone = 0;
        int quantity = 0;
        int price = 0;
        if (TextUtils.isEmpty(nameBooksString)){
            Toast.makeText(EditorActivity.this, "Please enter the title of the book.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!TextUtils.isEmpty(priceBookString)) {
            price = Integer.parseInt(priceBookString);
        }else{
            Toast.makeText(EditorActivity.this, "Please enter the price.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!TextUtils.isEmpty(quantityBookString)) {
            quantity = Integer.parseInt(quantityBookString);
        }else{

            Toast.makeText(EditorActivity.this, "Please enter the quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(sNameString)){
            Toast.makeText(EditorActivity.this, "Please enter Supplier's name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!TextUtils.isEmpty(sPhoneString)) {
            phone = Integer.parseInt(sPhoneString);
        } else{
            Toast.makeText(EditorActivity.this, "Please enter the phone.", Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(BookContract.BookEntry.COLUMN_PRODUCT_NAME, nameBooksString);
        values.put(BookContract.BookEntry.COLUMN_TYPE, typeBook);
        values.put(BookContract.BookEntry.COLUMN_PRICE, price);
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_NAME, sNameString);
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, phone);


        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookContract.BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Book saved.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Something went wrong",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "The book has been saved",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_PRODUCT_NAME,
                BookContract.BookEntry.COLUMN_PRICE,
                BookContract.BookEntry.COLUMN_QUANTITY,
                BookContract.BookEntry.COLUMN_TYPE,
                BookContract.BookEntry.COLUMN_SUPPLIER_NAME,
                BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
            int typeColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_TYPE);
            int sNameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supName = cursor.getString(sNameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int phone = cursor.getInt(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            bookName.setText(name);
            sName.setText(supName);
            quantityBook.setText(Integer.toString(quantity));
            sPhone.setText(Integer.toString(phone));
            priceBook.setText(Integer.toString(price));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (type) {
                case BookContract.BookEntry.TYPE_FICTION:
                    spinnerType.setSelection(1);
                    break;
                case BookContract.BookEntry.TYPE_NFICTION:
                    spinnerType.setSelection(2);
                    break;
                default:
                    spinnerType.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookName.setText("");
        sName.setText("");
        quantityBook.setText("");
        priceBook.setText("");
        sPhone.setText("");
        spinnerType.setSelection(0);

    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing of the book?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to detele this book?");
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Something went wrong.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "The book has been deleted.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


}

