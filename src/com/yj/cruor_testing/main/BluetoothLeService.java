package com.yj.cruor_testing.main;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.yj.cruor_testing.bluetooth.SampleGattAttributes;
import com.yj.cruor_testing.util.ConvertUtils;


/**
 * @author liaoyao
 * 通过BLE  API服务端与BLE设备交互
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    
    public static List<Byte> tempList = new ArrayList<Byte>();
    
    private BluetoothManager mBluetoothManager;//蓝牙管理器
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private String mBluetoothDeviceAddress;//蓝牙设备地址
    private BluetoothGatt mBluetoothGatt;//利用此实例完成对GATT客户端的操作
    private int mConnectionState = STATE_DISCONNECTED;//蓝牙连接状态  无用？

    private static final int STATE_DISCONNECTED = 0;//蓝牙连接状态  未连接  0
    private static final int STATE_CONNECTING = 1;//蓝牙连接状态  正在连接   1
    private static final int STATE_CONNECTED = 2;//蓝牙连接状态  已连接  2

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";//gatt 连接
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";//gatt  未连接
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
    		"com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";//gatt  服务 未连接
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";//数据可用
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";//extra  data

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.，所有函数的回调函数
    //通过BLE  API的不同类型的回调方法 -如连接状态改变、  或者发现服务
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override//当连接上设备或者是去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {//当连接状态发生改变
            String intentAction;
          //收到设备notify值 （设备上报值）
            if (newState == BluetoothProfile.STATE_CONNECTED) {//当蓝牙设备已经连接
                intentAction = ACTION_GATT_CONNECTED;//com.example.bluetooth.le.ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED;//  =2
                broadcastUpdate(intentAction);//更新广播  表示当前设备已连接上  发送广播 告诉广播接收器
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//当设备无法连接
                intentAction = ACTION_GATT_DISCONNECTED;//com.example.bluetooth.le.ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED;// = 0
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
        //发现新服务端  //当设备是否找到服务时，，会回调该 函数
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {//0
            	//找到服务了    在这里可以对服务进行解析，寻找到你需要的服务
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);//com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
                System.out.println("onServicesDiscovered received: " + status);
            }
        }
        //读写特性  当读取设备的时候 会回调该函数
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	//读取到值，在这里读数据
            if (status == BluetoothGatt.GATT_SUCCESS) {//0
            	broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                //读取到的数据存在characteristic当中，可以通过characteristic.getValue();函数取出。然后再进行解析操作。  
                //int charaProp = characteristic.getProperties();
                //if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)表示可发出通知。  判断该Characteristic属性 
            }
        }
        //设备发出通知的时候 会调用该接口 的此方法
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.e(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.对于所有的文件，写入十六进制格式的文件
        	//这里读取到数据
            final byte[] data = characteristic.getValue();
            
            if (data != null && data.length > 0) {
            	            	
            	intent.putExtra(EXTRA_DATA, data);
            	
              /*  //在这里来判断是否是2 个字节还是4个字节  然后若是4个字节  则2个字节字节的发送广播
                if(data.length == 4){
                	intent.putExtra(EXTRA_DATA, new byte[]{data[0],data[1]});
                	sendBroadcast(intent);//先发送  然后再赋值下一次的
                	intent.putExtra(EXTRA_DATA, new byte[]{data[2],data[3]});
                }else{
                	intent.putExtra(EXTRA_DATA, data);
                }*/
            }
        }
        //发送广播，广播接收器在哪？？
        sendBroadcast(intent);
    }
    
    //判断是否是结束符
    public boolean isEnd(byte[] data){
    	return ConvertUtils.bytesToHexString(data).equals("ff01");
    }
    
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * 初始化的时候判断是否支持蓝牙   并获得蓝牙适配器
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;//不支持4.0的蓝牙
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;//表明设备不支持蓝牙
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.  先前连接的设备尝试重新连接
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //说明是第一次连接  设备
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");//设备没有找到  无法连接
            return false;
        }
        
        if (mBluetoothGatt != null) {
        	mBluetoothGatt.close();
        }
     //连接前先关闭之前 的  貌似也无法解决？
        
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.//此处才是真正的去与当前蓝牙模块连接  第二个参数 是否自动连接 选择false
        //连接成功了之后 会回调BluetoothGattCallback 接口，，包括读取设备，往设备里写数据及设备发出通知等都会回调该接口，其中比较重要的是BluetoothGatt
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        System.out.println("device.getBondState=="+device.getBondState());
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        refreshDeviceCache(mBluetoothGatt);
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    //读取数据的函数
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
  //写入指定的characteristic
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     * 获取已连接设备支持的所有GATT服务集合
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    public BluetoothGattService getSupportedGattServices(UUID uuid) {
    	BluetoothGattService mBluetoothGattService;
    	if (mBluetoothGatt == null) return null;
    	mBluetoothGattService=mBluetoothGatt.getService(uuid);
        return mBluetoothGattService;
    } 
    
    /**
     * 清理本地的BluetoothGatt 的缓存，以保证在蓝牙连接设备的时候，设备的服务、特征是最新的
     * @param gatt
     * @return
     */
    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        if(null != gatt){
            try {
                BluetoothGatt localBluetoothGatt = gatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod( "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(
                            localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }
        return false;
    }
    
}
