package com.yj.cruor_testing.main;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/*
 * ɨ�貢��ʾBLE�豸
 */
public class DeviceScanActivity extends ListActivity {
	
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;//Ĭ��ֵ��false
	private Handler mHandler;
	
	private static final int REQUEST_ENABLE_BT = 1;
	//10s��ֹͣɨ��
	private static final long SCAN_PRRIOD = 10000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getActionBar().setTitle(R.string.title_activity_device_scan);
		getListView().setBackground(getResources().getDrawable(R.drawable.background));
		mHandler = new Handler();
//		MainActivity.bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
//		MainActivity.mBluetoothAdapter = MainActivity.bluetoothManager.getAdapter();//û��Ҫÿ�ζ����½�һ��
	}
	
	/**
	 * �˷������ڳ�ʼ���˵�������menu�������Ǽ���Ҫ��ʾ��Menuʵ���� ����true����ʾ��menu,false ����ʾ;
	 * (ֻ���ڵ�һ�γ�ʼ���˵�ʱ����) Inflate the menu; this adds items to the action bar
	 * if it is present.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_scan, menu);
		if(!mScanning){
			//ֹͣɨ���ʱ�� û�н�����
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		}else{
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_scan:
			//���������ն�û��ˢ�µĻ�  �ٴε����ʱ��ͻ�����Ҳ���������������Բ�Ӧ��ȥ���device��list
//			mLeDeviceListAdapter.clear();//ÿ�ο�ʼɨ���ʱ����յ�ǰ���е�ɨ�赽���豸
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}
	
	//��ǰ������û�����״̬
	@Override
	protected void onResume() {
		super.onResume();
		//ȷ����ǰ�����豸�ǿ��ŵ� ��û�У�������Ҫ����dialog  ѯ���Ƿ������
		if(!MainActivity.mBluetoothAdapter.isEnabled()){
			if(!MainActivity.mBluetoothAdapter.isEnabled()){
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
		}
		//��ʼ��listviewAdapter
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		//�ѿ����ʱ��Ϳ�ʼɨ��
		scanLeDevice(true);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
		if(device == null){
			return;
		}
//		Toast.makeText(this, "you click" + device.getName(), Toast.LENGTH_SHORT).show();
		final Intent intent = new Intent();
//		intent.putExtra("EXTRAS_DEVICE_NAME", device.getName());
		intent.putExtra("EXTRAS_DEVICE_ADDRESS", device.getAddress());
		if(mScanning){
			MainActivity.mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
			//�Լ� �����֮��ֹͣɨ��  ���ǻ���ˢ�� ��Ȼ��Ȼû��ɨ���� ������ʵ���������������е�
			invalidateOptionsMenu();//ˢ�µ�ǰ�Ĳ˵�
		}
		setResult(RESULT_OK, intent);
		this.finish();
	}
	
	//ɨ�������豸
	private void scanLeDevice(final boolean enable){
		if(enable){
			//��һ��ɨ�����ڣ�10s��֮��ֹͣɨ��
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					MainActivity.mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();//ˢ�µ�ǰ�Ĳ˵�
				}
			}, SCAN_PRRIOD);//10s��ִ��run����� �� �൱��ֹͣɨ��
			mScanning = true;
			MainActivity.mBluetoothAdapter.startLeScan(mLeScanCallback);
		}else{
			mScanning = false;
			MainActivity.mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();//ˢ�µ�ǰ�Ĳ˵�
	}
	//ɨ�������豸�ص� ��ɨ��֮��  �ص��ӿڱ����ڴ���LEɨ���Ľ��
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();//ʵ�ֶ�̬ˢ���б�
				}
			});
		}
	};
	//�����豸��������
	private class LeDeviceListAdapter extends BaseAdapter{
		private List<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflater;
		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflater = DeviceScanActivity.this.getLayoutInflater();
		}
		
		public void addDevice(BluetoothDevice device){
			if(!mLeDevices.contains(device)){
				mLeDevices.add(device);
			}
		}
		
		public BluetoothDevice getDevice(int position){
			return mLeDevices.get(position);
		}
		
		public void clear(){
			mLeDevices.clear();
		}
		
		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int position) {
			return mLeDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder viewHolder;
			if(view == null){
				view = mInflater.inflate(R.layout.devicescanactivity, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView)view.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView)view.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder)view.getTag();
			}
			BluetoothDevice device = mLeDevices.get(position);
			final String deviceName = device.getName();
			if(deviceName != null && deviceName.length() > 0){
				viewHolder.deviceName.setText(deviceName);
			}else{
				viewHolder.deviceName.setText(R.string.unknown_device);
			}
			viewHolder.deviceAddress.setText(device.getAddress());
			return view;
		}
		
	}
	static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
