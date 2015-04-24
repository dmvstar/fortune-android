package net.sf.dvstar.fortune.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dstarzhynskyi on 10.04.2015.
 */
public class FortuneDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "FortuneDBHelper";
    static final String DB_NAME = "fortune"; // имя БД
    static final int DB_VERSION = 3; // версия БД
    public static final String FORTUNE_TABLE_PHRASES = "fortune_phrases";
    private static final String TABLE_PHRASES_COLUMN_PHRASE = "phrase";
    private static final String TABLE_PHRASES_COLUMN_ID = "id";
    private SQLiteDatabase db;

    public FortuneDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
        Log.d(TAG, "FortuneDBHelper "+FORTUNE_TABLE_PHRASES+"["+isTableExists(FORTUNE_TABLE_PHRASES)+"]");
    }


    boolean isTableExists(String tableName)
    {
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate database");

        db.execSQL("create table if not exists "+FORTUNE_TABLE_PHRASES+" ("
                + "id integer primary key," //  autoincrement
                + TABLE_PHRASES_COLUMN_PHRASE+" text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 2 && newVersion == 3) {
            db.execSQL("DROP TABLE IF EXISTS " + FORTUNE_TABLE_PHRASES);
            onCreate(db);
        }
    }


    public void addItem(String tableName,int count, String value){
        ContentValues cv = new ContentValues();
        cv.put(TABLE_PHRASES_COLUMN_ID, count);
        cv.put(TABLE_PHRASES_COLUMN_PHRASE, value);
        db.insert(tableName, null, cv);
        Log.d(TAG, "addItem phrase "+cv.toString());
    }

    public int getCountRows(String tableName){
        int count = 0;
        if(isTableExists(tableName)) {
            String selectQuery = "SELECT COUNT(*) FROM " + tableName;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    String[] columns = null;
    String selection = null;
    String[] selectionArgs = null;

    public String getPhraseById(int id){
        String ret = "";
        columns = new String[] { TABLE_PHRASES_COLUMN_PHRASE };
        Cursor cursor = db.query(FORTUNE_TABLE_PHRASES, columns,
                TABLE_PHRASES_COLUMN_ID + "=?", new String[] { String.valueOf(id) }, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            Log.d(TAG,"getPhrase: "+"["+cursor.getCount()+"]["+cursor.getColumnCount()+"] "+cursor.getColumnName(cursor.getColumnCount()-1));
            if(cursor.getCount()>0)
                ret = cursor.getString(0);
            cursor.close();
        }
        return ret;
    }


    public void removeAll(String tableName) {
        db.delete(tableName, null, null);
    }
}
