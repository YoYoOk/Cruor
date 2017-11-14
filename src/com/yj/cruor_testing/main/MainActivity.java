package com.yj.cruor_testing.main;

import java.lang.reflect.Field;

import com.yj.cruor_testing.database.DatabaseHelper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/*
 * 主活动
 */
public class MainActivity extends Activity {
	
	private static final int REQUEST_DEVICE_ADDRESS = 1;
	
	public static BluetoothAdapter mBluetoothAdapter;
	public static BluetoothManager bluetoothManager;
	private Button btn_search;//搜索周围的蓝牙设备
	private Button btn_scan_display;//进入扫描显示页面
	private Button btn_history;//查看历史记录
//	private static String deviceName = "CC41-A";
	private static String deviceAddress = "00:15:83:00:80:FB";// 要连接的目标蓝牙设备的地址 默认的
	public static DatabaseHelper dbHelper;//sqlite数据库的
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		forceShowActionBarOverflowMenu();//有些手机不显示+号 溢出菜单 采用强制使用
		getActionBar().setDisplayHomeAsUpEnabled(true);//给左上角图标的左边加上一个返回的图标，
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
		//太亮了 浪费电
		
		btn_search = (Button)findViewById(R.id.search_device);
		btn_scan_display = (Button)findViewById(R.id.scan_display);
		btn_history = (Button)findViewById(R.id.history);
		dbHelper = new DatabaseHelper(this, "CruorTest.db", null, 1);//升级数据库 将参数值改成PointF
		dbHelper.getWritableDatabase();//在此处调用表
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			//查看手机是否支持4.0以上蓝牙
			Toast.makeText(this, "不支持4.0蓝牙的设备", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		//初始化蓝牙适配器， 查看是否API>18 
		bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		if(bluetoothManager.getAdapter() == null){
			Toast.makeText(this, "您的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}
		mBluetoothAdapter = bluetoothManager.getAdapter();
		btn_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,DeviceScanActivity.class);
				startActivityForResult(intent, REQUEST_DEVICE_ADDRESS);
			}
		});
		//打开扫描显示页面
		btn_scan_display.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(MainActivity.this,ScanDisplayActivity.class);
				intent.putExtra(ScanDisplayActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
				startActivity(intent);
			}
		});
		//查看历史记录
		btn_history.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//打开目录下的所有文件显示在列表中
				Intent intent = new Intent(MainActivity.this,HistoryRecordActivity.class);
				startActivity(intent);
			}
		});
		
	}
	//活动返回来的地址数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode){
		case REQUEST_DEVICE_ADDRESS:
			if(resultCode == RESULT_OK){
				//TODO 测试一下这里为null的时候的情况？已解决  上一个若是直接返回键 是不会运行此处回调的
				deviceAddress = intent.getStringExtra("EXTRAS_DEVICE_ADDRESS");//这里等于空如何解决？
				//如果上一个活动直接返回按钮返回的 。不会执行到此处来
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//每次开屏的时候的时候就判断是否支持蓝牙  若不支持就令搜索按钮和扫描按钮都不能用
//		isSupportBluetooth = BluetoothService.isSupportBluetooth();
	}
	
	/** 
     * 强制显示 overflow menu 
     */  
    private void forceShowActionBarOverflowMenu() {  
        try {  
            ViewConfiguration config = ViewConfiguration.get(this);  
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
            if (menuKeyField != null) {  
                menuKeyField.setAccessible(true);  
                menuKeyField.setBoolean(config, false);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
	
	
}
