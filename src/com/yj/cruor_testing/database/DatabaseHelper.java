package com.yj.cruor_testing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/*
 * ���ݿ���� sqlite ��Ҫ�Ǳ���R K Angle MAֵ
 */
public class DatabaseHelper extends SQLiteOpenHelper{
	public static final String CREATE_PRRAMS = "create table Params("
			+ "id integer primary key autoincrement,"
			+ "r_value real,"
			+ "k_value real,"
			+ "angel real,"
			+ "ma_value real,"
			+ "date text)";
	//����һ��������
	private Context mContext;
	public DatabaseHelper(Context context,String name,CursorFactory factory,int version){
		super(context,name,factory,version);
		mContext = context;
	}
	@Override
	public void onCreate(SQLiteDatabase db){
		/*
		 * 1���ڵ�һ�δ����ݿ��ʱ��               ��ִ��
		 * 2�����������֮���ٴ�����  -->�����ݿ�   ��ִ��
		 * 3��û��������ݣ�         ����ִ��
		 * 4�����ݿ�������ʱ��    ����ִ��
		 */
		db.execSQL(CREATE_PRRAMS);//ִ�д���params ��
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		/*
		 * 1����һ�δ������ݿ��ʱ��  ��ִ��
		 * 2��������ݺ��ٴ����У��൱�ڵ�һ�δ�����  ��ִ��
		 * 3�����ݿ��Ѿ����ڣ����Ұ汾���ߵ�ʱ��    ����
		 */
		//������ݿ� ���´���
		db.execSQL("drop table if exists Params");
		onCreate(db);
		//������ݿ�������� ֻ���һ��switch�ж�  oldVsersion
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
//		super.onDowngrade(db, oldVersion, newVersion);
		/**
		* ִ�����ݿ�Ľ�������
		* 1��ֻ���°汾�Ⱦɰ汾�͵�ʱ��Ż�ִ��
		* 2�������ִ�н������������׳��쳣
		*/
		db.execSQL("drop table if exists Params");
		onCreate(db);
	}
}
