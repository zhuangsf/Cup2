package com.sf.cup2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_DATA = "data";
	public static final String KEY_TIME = "time";
	public static final String KEY_WATER = "water";
	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "water";
	private static final String DATABASE_TABLE = "water_data";
	
	public static final int DATA_COLUMN_ID = 0;
	public static final int DATA_COLUMN_DATA = 1;
	public static final int DATA_COLUMN_TIME = 2;
	public static final int DATA_COLUMN_WATER = 3;
	
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "create table water_data (_id integer primary key autoincrement, "
			+ "data text not null, time text not null, "
			+ "water text not null);";
	private final Context mContext;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context context) {
		mContext = context;
		DBHelper = new DatabaseHelper(mContext);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}

	// ---打开数据库---

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}


	public void close() {
		DBHelper.close();
	}

	// ---向数据库中插入一个数据----

	public long insertWaterData(String data, String time, String water) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATA, data);
		initialValues.put(KEY_TIME, time);
		initialValues.put(KEY_WATER, water);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	// ---删除一个指定数据----

	public boolean deleteDataByID(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// ---检索所有数据----

	public Cursor getAllData() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_DATA,
				KEY_TIME, KEY_WATER }, null, null, null, null, null);
	}

	
	public void dumpData()
	{
		Cursor c = getAllData();
		Log.w(TAG, "dumpData START-----------------");
		if (c.moveToFirst())
		{
		do {
			Log.w(TAG, "DATA_COLUMN_ID = "+c.getString(DATA_COLUMN_ID)
					+"DATA_COLUMN_DATA = "+c.getString(DATA_COLUMN_DATA)
					+"DATA_COLUMN_TIME = "+c.getString(DATA_COLUMN_TIME)
					+"DATA_COLUMN_WATER = "+c.getString(DATA_COLUMN_WATER)
					);
		} while (c.moveToNext());
		}
		Log.w(TAG, "dumpData END-----------------");
	}
	
	// ---检索一个指定数据----

	public Cursor getDataByID(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_DATA, KEY_TIME, KEY_WATER }, KEY_ROWID
				+ "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// ---更新一个数据---

	public boolean updateWater(long rowId, String data, String time,
			String water) {
		ContentValues args = new ContentValues();
		args.put(KEY_DATA, data);
		args.put(KEY_TIME, time);
		args.put(KEY_WATER, water);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}