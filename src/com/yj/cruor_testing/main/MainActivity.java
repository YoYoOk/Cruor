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
 * ���
 */
public class MainActivity extends Activity {
	
	private static final int REQUEST_DEVICE_ADDRESS = 1;
	
	public static BluetoothAdapter mBluetoothAdapter;
	public static BluetoothManager bluetoothManager;
	private Button btn_search;//������Χ�������豸
	private Button btn_scan_display;//����ɨ����ʾҳ��
	private Button btn_history;//�鿴��ʷ��¼
//	private static String deviceName = "CC41-A";
	private static String deviceAddress = "00:15:83:00:80:FB";// Ҫ���ӵ�Ŀ�������豸�ĵ�ַ Ĭ�ϵ�
	public static DatabaseHelper dbHelper;//sqlite���ݿ��
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		forceShowActionBarOverflowMenu();//��Щ�ֻ�����ʾ+�� ����˵� ����ǿ��ʹ��
		getActionBar().setDisplayHomeAsUpEnabled(true);//�����Ͻ�ͼ�����߼���һ�����ص�ͼ�꣬
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//������Ļ����
		//̫���� �˷ѵ�
		
		btn_search = (Button)findViewById(R.id.search_device);
		btn_scan_display = (Button)findViewById(R.id.scan_display);
		btn_history = (Button)findViewById(R.id.history);
		dbHelper = new DatabaseHelper(this, "CruorTest.db", null, 1);//�������ݿ� ������ֵ�ĳ�PointF
		dbHelper.getWritableDatabase();//�ڴ˴����ñ�
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			//�鿴�ֻ��Ƿ�֧��4.0��������
			Toast.makeText(this, "��֧��4.0�������豸", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		//��ʼ�������������� �鿴�Ƿ�API>18 
		bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		if(bluetoothManager.getAdapter() == null){
			Toast.makeText(this, "�����豸��֧������", Toast.LENGTH_SHORT).show();
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
		//��ɨ����ʾҳ��
		btn_scan_display.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(MainActivity.this,ScanDisplayActivity.class);
				intent.putExtra(ScanDisplayActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
				startActivity(intent);
			}
		});
		//�鿴��ʷ��¼
		btn_history.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//��Ŀ¼�µ������ļ���ʾ���б���
				Intent intent = new Intent(MainActivity.this,HistoryRecordActivity.class);
				startActivity(intent);
			}
		});
		
	}
	//��������ĵ�ַ����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode){
		case REQUEST_DEVICE_ADDRESS:
			if(resultCode == RESULT_OK){
				//TODO ����һ������Ϊnull��ʱ���������ѽ��  ��һ������ֱ�ӷ��ؼ� �ǲ������д˴��ص���
				deviceAddress = intent.getStringExtra("EXTRAS_DEVICE_ADDRESS");//������ڿ���ν����
				//�����һ���ֱ�ӷ��ذ�ť���ص� ������ִ�е��˴���
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
		//ÿ�ο�����ʱ���ʱ����ж��Ƿ�֧������  ����֧�־���������ť��ɨ�谴ť��������
//		isSupportBluetooth = BluetoothService.isSupportBluetooth();
	}
	
	/** 
     * ǿ����ʾ overflow menu 
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
