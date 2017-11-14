package com.yj.cruor_testing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/*
 * 数据库操作 sqlite 主要是保存R K Angle MA值
 */
public class DatabaseHelper extends SQLiteOpenHelper{
	public static final String CREATE_PRRAMS = "create table Params("
			+ "id integer primary key autoincrement,"
			+ "r_value real,"
			+ "k_value real,"
			+ "angel real,"
			+ "ma_value real,"
			+ "date text)";
	//创建一个表的语句
	private Context mContext;
	public DatabaseHelper(Context context,String name,CursorFactory factory,int version){
		super(context,name,factory,version);
		mContext = context;
	}
	@Override
	public void onCreate(SQLiteDatabase db){
		/*
		 * 1、在第一次打开数据库的时候               会执行
		 * 2、在清除数据之后再次运行  -->打开数据库   会执行
		 * 3、没有清除数据，         不会执行
		 * 4、数据库升级的时候    不会执行
		 */
		db.execSQL(CREATE_PRRAMS);//执行创建params 表
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		/*
		 * 1、第一次创建数据库的时候  不执行
		 * 2、清除数据后再次运行（相当于第一次创建）  不执行
		 * 3、数据库已经存在，而且版本升高的时候，    调用
		 */
		//清除数据库 重新创建
		db.execSQL("drop table if exists Params");
		onCreate(db);
		//解决数据库更新问题 只需加一个switch判断  oldVsersion
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
//		super.onDowngrade(db, oldVersion, newVersion);
		/**
		* 执行数据库的降级操作
		* 1、只有新版本比旧版本低的时候才会执行
		* 2、如果不执行降级操作，会抛出异常
		*/
		db.execSQL("drop table if exists Params");
		onCreate(db);
	}
}
