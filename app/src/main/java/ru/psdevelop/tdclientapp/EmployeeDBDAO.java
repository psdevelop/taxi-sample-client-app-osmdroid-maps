package ru.psdevelop.tdclientapp;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EmployeeDBDAO {

	protected SQLiteDatabase database;
	private DataBaseHelper dbHelper;
	private Context mContext;

	public EmployeeDBDAO(Context context) {
		this.mContext = context;
		dbHelper = DataBaseHelper.getHelper(mContext);
		open();
		
	}

	public void open() throws SQLException {
		if(dbHelper == null)
			dbHelper = DataBaseHelper.getHelper(mContext);
		database = dbHelper.getWritableDatabase();
	}
	
	/*public void close() {
		dbHelper.close();
		database = null;
	}*/
}
