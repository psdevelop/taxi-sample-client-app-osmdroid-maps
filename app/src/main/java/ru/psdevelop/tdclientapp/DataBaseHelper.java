package ru.psdevelop.tdclientapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "employeedb";
	private static final int DATABASE_VERSION = 1;

	public static final String EMPLOYEE_TABLE = "employee";

	public static final String ID_COLUMN = "id";
	public static final String NAME_COLUMN = "name";
	public static final String EMPLOYEE_DOB = "dob";
	public static final String EMPLOYEE_SALARY = "salary";

	public static final String CREATE_EMPLOYEE_TABLE = "CREATE TABLE "
			+ EMPLOYEE_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY, "
			+ NAME_COLUMN + " TEXT, " + EMPLOYEE_SALARY + " DOUBLE, "
			+ EMPLOYEE_DOB + " DATE" + ")";

	private static DataBaseHelper instance;

	public static synchronized DataBaseHelper getHelper(Context context) {
		if (instance == null)
			instance = new DataBaseHelper(context);
		return instance;
	}

	private DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_EMPLOYEE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
