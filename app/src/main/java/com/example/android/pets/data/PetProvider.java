package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.Selection;
import android.widget.Switch;

/**
 * Created by HP on 8/11/2017.
 */

public class PetProvider extends ContentProvider {


    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    PetDbHelper petDbHelper;

    static
    {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS +"/#",PETS_ID);
    }
    @Override
    public boolean onCreate() {
        petDbHelper = new PetDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = petDbHelper.getReadableDatabase();
        Cursor cursor ;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                cursor = database.query(PetContract.PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PETS_ID:
                selection = PetContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return PetContract.PetsEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI "+uri+" with match"+match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return insertPet(uri,values);
            default:
                throw new IllegalArgumentException("Error"+uri);
        }
    }

    private Uri insertPet(Uri uri , ContentValues contentValues)
    {
        SQLiteDatabase database = petDbHelper.getWritableDatabase();
        long id = database.insert(PetContract.PetsEntry.TABLE_NAME,null,contentValues);
        if (id == -1)
        {
           return null;
        }
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = petDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return database.delete(PetContract.PetsEntry.TABLE_NAME,selection,selectionArgs);
            case PETS_ID:
                selection = PetContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(uri)};
                return database.delete(PetContract.PetsEntry.TABLE_NAME,selection,selectionArgs);
            default:
                throw new IllegalStateException("Delete is not supported"+uri);
        }
    }






    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match)
        {
            case PETS:
                return updatePet(uri,values,selection,selectionArgs);
            case PETS_ID:
                selection = PetContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,values,selection,selectionArgs);
            default:
                throw new IllegalStateException("Update not supported"+uri);
        }
    }

    private int updatePet (Uri uri,ContentValues contentValues, String selection,String[] selectionArgs)
    {

        if (contentValues.containsKey(PetContract.PetsEntry.COLUMN_PET_NAME))
        {
            String name = contentValues.getAsString(PetContract.PetsEntry.COLUMN_PET_NAME);
            if (name == null)
            {
                throw new IllegalStateException("Pet Requires A Name");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (contentValues.containsKey(PetContract.PetsEntry.COLUMN_PET_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetContract.PetsEntry.COLUMN_PET_GENDER);
            if (gender == null ) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (contentValues.containsKey(PetContract.PetsEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = contentValues.getAsInteger(PetContract.PetsEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        if (contentValues.size()==0)
        {
            return  0;
        }
        SQLiteDatabase sqLiteDatabase = petDbHelper.getWritableDatabase();
        return  sqLiteDatabase.update(PetContract.PetsEntry.TABLE_NAME,contentValues,selection,selectionArgs);
    }
}
