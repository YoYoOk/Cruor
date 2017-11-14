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
 * 扫描并显示BLE设备
 */
public class DeviceScanActivity extends ListActivity {
	
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;//默认值是false
	private Handler mHandler;
	
	private static final int REQUEST_ENABLE_BT = 1;
	//10s后停止扫描
	private static final long SCAN_PRRIOD = 10000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getActionBar().setTitle(R.string.title_activity_device_scan);
		getListView().setBackground(getResources().getDrawable(R.drawable.background));
		mHandler = new Handler();
//		MainActivity.bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
//		MainActivity.mBluetoothAdapter = MainActivity.bluetoothManager.getAdapter();//没必要每次都重新建一次
	}
	
	/**
	 * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
	 * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
	 * if it is present.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_scan, menu);
		if(!mScanning){
			//停止扫描的时候 没有进度条
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
			//如果仅是清空而没有刷新的话  再次点击的时候就会出现找不到的情况。。所以不应该去清空device的list
//			mLeDeviceListAdapter.clear();//每次开始扫描的时候清空当前所有的扫描到的设备
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}
	
	//当前活动在与用户交互状态
	@Override
	protected void onResume() {
		super.onResume();
		//确保当前蓝牙设备是开着的 若没有，，就需要启动dialog  询问是否打开蓝牙
		if(!MainActivity.mBluetoothAdapter.isEnabled()){
			if(!MainActivity.mBluetoothAdapter.isEnabled()){
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
		}
		//初始化listviewAdapter
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		//已开启活动时候就开始扫描
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
			//自加 点击了之后停止扫描  但是还得刷新 不然虽然没有扫描了 但是其实进度条还是在运行的
			invalidateOptionsMenu();//刷新当前的菜单
		}
		setResult(RESULT_OK, intent);
		this.finish();
	}
	
	//扫描蓝牙设备
	private void scanLeDevice(final boolean enable){
		if(enable){
			//在一个扫描周期（10s）之后停止扫描
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					MainActivity.mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();//刷新当前的菜单
				}
			}, SCAN_PRRIOD);//10s后执行run里面的 即 相当于停止扫描
			mScanning = true;
			MainActivity.mBluetoothAdapter.startLeScan(mLeScanCallback);
		}else{
			mScanning = false;
			MainActivity.mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();//刷新当前的菜单
	}
	//扫描蓝牙设备回调 即扫描之后  回调接口被用于传输LE扫描后的结果
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();//实现动态刷新列表
				}
			});
		}
	};
	//查找设备的适配器
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
