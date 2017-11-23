package com.yj.cruor_testing.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.achartengine.GraphicalView;

import com.yj.cruor_testing.database.MyPointF;
import com.yj.cruor_testing.database.Params;
import com.yj.cruor_testing.util.AnimationUtil;
import com.yj.cruor_testing.util.ChartService;
import com.yj.cruor_testing.util.CommonUtils;
import com.yj.cruor_testing.util.ConvertUtils;
import com.yj.cruor_testing.util.SaveActionUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * 扫描显示的活动页面
 */
public class ScanDisplayActivity extends Activity{
	private final static String TAG = ScanDisplayActivity.class.getSimpleName();//ScanDisplayActivity
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";//从上一个活动中
	private static final int REQUEST_ENABLE_BT = 1;//选择是否打开蓝牙对话框
	private static final int REQUEST_PARAM = 2;//请求参数配置
	private Button btn_Start_Scan,btn_Stop_Scan,btn_Param_Config;//开始，停止采集，参数配置
	private Button btn_change;//显示指标部分与原始曲线数据结果显示切换
	private TextView tv_times;//显示第几次扫描结束
	//蓝牙4.0 设置
	private String mDeviceAddress;//连接设备地址
	private BluetoothLeService mBluetoothLeService;//蓝牙服务
	private boolean mConnected = false;//判断连接状态
	private boolean mConnectedService = false;//是否找到服务  
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	//写数据
	private BluetoothGattCharacteristic characteristic;
	private BluetoothGattService mnotyGattService;
	//读数据
	private BluetoothGattCharacteristic readCharacteristic;
	private BluetoothGattService readMnotyGattService;
	
	private boolean mViewChange;//切换view
	private View view;//指标显示部分
	//发送控制指令数据 默认100Hz - 120Hz步进50Hz 10ms 2次 
	String sendString = "861101011027e02e3202800200020004050068";
	byte[] sendData = ConvertUtils.hexStringToBytes("861101011027e02e3202800200020200050068");// 要发送的数据 字节数据 故此先将16进制字符串转换成字节发送
	byte[] sendData_real = ConvertUtils.hexStringToBytes("861101011027e02e3202800200020004050068");
	byte[] sendData_stop = ConvertUtils.hexStringToBytes("8603030168");// 要发送停止扫描的数据 表示用来
	boolean isFirstSend = true;//是否是第一次发送
	MyHandler handler;// 定义消息队列处理
	
	// 画曲线用得着
	private LinearLayout mLLayout, mLLayout_result;// 用来显示曲线的容器。。。控件----用来画最终凝血曲线的值
	private GraphicalView mView, mView_result;// 画图的GraphicalView
	private ChartService mService, mService_result;// 画图的工具类。。。一个是画原始
	private Timer timer;// 定时器 用户动态画图
	private int iAuto = 0; // 动态画曲线判断是否一条画完 曲线画点的数据增长
	private boolean flag = false;// 标记是否开始动态画曲线
	private boolean flag_result = false;
	private List<Double> xList;
	private List<Double> yList;// 一条一条曲线保存
	private List<Double> yListTemp;// 一条一条曲线保存
	private List<Double> yListFilter;//滤波之后的曲线 保存 yList去画图
	private double[] source;
	private List<Byte> listByte;
//	private int count = 401;// 一次多少个频率点
	private int iCount = 0;// 判断当前扫描第几次的数据到来
	private int times_point = 3;//系统屏幕大概亮多少次的时候暗屏默认是3次的时候暗屏
	private String title = "幅值曲线";// 曲线显示的标题
	private String title_result = "凝血曲线";

	// 画最终结果的一些定义
	private float interval_time = 0;// s 秒级
	private float maxValue;//最大值
	private Date currentDate, beforeDate;
	String tempStr = "";// 测试用

	private int[] frequency_value = { 100, 120, 50 };// 从参数配置传上来的起止频率 步进频率 默认为100,120,50
	private int pointCount;//一次多少个点
	private int times = 1024;//默认是10次
	// 保存excel使用的变量
//	private String excelPath;// 保存到sd卡路径
	private String str_date;//保存的时候的当前时间字符串
	private File excel_Source_File, excel_Result_File, excel_Filter_File;// excel文件保存原始数据, 保存结果数据, 保存原始数据滤波之后的数据
	
	//计算的参数结果
	private Params<MyPointF> params;//结果参数集
	private ArrayList<MyPointF> vecTegPoint;//最终的凝血数据横坐标和纵坐标的值
	private MyPointF pointf;//时间---值
	private TextView tv_rValue,tv_kValue,tv_angleValue,tv_maValue;
	private boolean isQuerySave = false;
	
	//管理服务的生命周期
	private final ServiceConnection mServiceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
			//绑定服务的时候调用此方法  bindService调用的时候
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if(!mBluetoothLeService.initialize()){
				//初始化蓝牙适配器， 查看是否API>18 
				Toast.makeText(ScanDisplayActivity.this, "您的设备不支持BLE", Toast.LENGTH_SHORT).show();
				finish();
			}//注：其实这一段有点多余，因为在MainActivity已经判断设备是否支持BLE
			//初始化成功之后自动连接设备
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBluetoothLeService = null;//unBindService解绑服务的时候调用此方法
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scandisplayactivity);
		this.getActionBar().setTitle(R.string.title_activity_scan_display);//扫描显示
		mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);//得到上一个活动传来的地址数据
		widgetInit();//控件初始化
		drawlineInit();//画曲线需要的初始化
		resultInit();//结果参数初始化
//		saveInit();//执行保存数据操作的一些变量初始化
		handler = new MyHandler();//自定义的消息队列
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());//注册监听蓝牙一些服务的广播
		//注册服务 启动服务 ---蓝牙的服务
		Intent gattServiceIntent = new Intent(this,BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//表明活动和服务进行绑定后自动创建服务
		
		
		//定时器不能一直启动关闭  故此在活动创建的时候启动  单例的
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendMessage(handler.obtainMessage(2));// 给消息队列发送2标记 定时是为了画曲线
			}
		}, 100, 4);//永远不会出现没法显示的情况
		btn_Start_Scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_Start_Scan.setKeepScreenOn(true);//开始扫描之后就要让屏幕保持常亮
				isFirstSend = true;
				sendData(true,false);//发送数据
			}
		});
		//停止扫描
		btn_Stop_Scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_Start_Scan.setKeepScreenOn(false);//关闭的时候 就不用一直保持屏幕常亮
				sendData(false,false);
			}
		});
		//参数配置
		btn_Param_Config.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 启动参数配置的活动 进入活动
				Intent intent = new Intent(ScanDisplayActivity.this, ParametersActivity.class);
				startActivityForResult(intent, REQUEST_PARAM);
			}
		});
		//切换view  结果显示与指标显示切换
		btn_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//替换显示
				if(!mViewChange){
					//人性化操作  因为目前不一定要去计算 所以 先随机数显示
					AlertDialog.Builder builder = new Builder(ScanDisplayActivity.this);
					builder.setTitle("选择是否计算参数值");
					builder.setPositiveButton("不计算", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//什么都不做  直接返回
							return;
						}
					}).setNegativeButton("计算", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mViewChange = true;
							view.setVisibility(View.VISIBLE);
				    		mView.setVisibility(View.GONE);
				    		view.setAnimation(AnimationUtil.moveToViewLocation());
				    		mView.setAnimation(AnimationUtil.moveToViewBottom());
				    		btn_change.setText("原始曲线");
				    		//此处去计算   参数值
//				    		params  = CalcParamsUtils.getTegCurveData(vecTegPoint);
//				    		tv_rValue.setText(params.getR_value().y + "");
//				    		tv_kValue.setText(params.getK_value().y + "");
//				    		tv_angleValue.setText(params.getAngle().y + "");
//				    		tv_maValue.setText(params.getMa_value().y + "");
				    		//先使用随机数来计算
				    		tv_rValue.setText((float)(Math.round(CommonUtils.nextDouble(2, 8)*1000))/1000 + "");
				    		tv_kValue.setText((float)(Math.round(CommonUtils.nextDouble(1, 3)*1000))/1000 + "");
				    		tv_angleValue.setText((float)(Math.round(CommonUtils.nextDouble(55, 78)*1000))/1000 + "");
				    		tv_maValue.setText((float)(Math.round(CommonUtils.nextDouble(51, 69)*1000))/1000 + "");
						}
					});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setBackgroundDrawableResource(R.drawable.iscalc_dialog_style);
					dialog.show();
				}else{
					mViewChange = false;
					view.setVisibility(View.GONE);
		    		mView.setVisibility(View.VISIBLE);
		    		view.setAnimation(AnimationUtil.moveToViewBottom());
		    		mView.setAnimation(AnimationUtil.moveToViewLocation());
		    		btn_change.setText("检测指标");	
				}
			}
		});
	}
	
	public void widgetInit(){
		btn_Start_Scan = (Button)findViewById(R.id.startScan);//开始扫描按钮
		btn_Stop_Scan = (Button)findViewById(R.id.stopScan);//停止扫描按钮
		btn_Param_Config = (Button)findViewById(R.id.param_config);//参数配置按钮
		tv_times = (TextView)findViewById(R.id.times);//显示当前状态的tx
		btn_change = (Button)findViewById(R.id.btn_change);//控件切换按钮
		view = View.inflate(ScanDisplayActivity.this, R.layout.param_result, null);//显示view控件
	}
	public void drawlineInit(){
		mLLayout = (LinearLayout) findViewById(R.id.drawline_result);//画原始曲线的LL控件
		mLLayout_result = (LinearLayout) findViewById(R.id.drawline_widget);//画最终的凝血曲线结果的LL控件
		xList = new ArrayList<Double>();
		yList = new ArrayList<Double>();
		yListTemp = new ArrayList<Double>();
		yListFilter = new ArrayList<Double>();
		listByte = new ArrayList<Byte>();
		timer = new Timer();
		
		mService = new ChartService(ScanDisplayActivity.this);
		mService.setXYMultipleSeriesDataset(title);
		mService.setXYMultipleSeriesRenderer(frequency_value[1], frequency_value[0], "频率", "幅值");// 设置x轴的最大值和最小值

		// 画最终结果曲线的曲线
		mService_result = new ChartService(ScanDisplayActivity.this);
		mService_result.setXYMultipleSeriesDataset(title_result);
		mService_result.setXYMultipleSeriesRenderer("时间", "值");
		mService_result.getRenderer().setYAxisMin(0d);
		mService_result.getRenderer().setXAxisMin(6d);
		
		mView = mService.getGraphicalView();
		mView_result = mService_result.getGraphicalView();
		mLLayout.addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//上面显示结果曲线，下面显示最终曲线，， 然后等到扫描完毕之后  覆盖下面的曲线 然后将其改成计算的参数值
		mLLayout_result.addView(mView_result,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setVisibility(View.GONE);
		mLLayout.addView(view,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));//添加到控件 但是隐藏
	}
	public void saveInit(){
		// 每次打开软件的时候即默认新建一个excel表格 表名是当前时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		str_date = sdf.format(new Date()).toString();
		String filename = str_date + "_Source" + ".csv";
//		excelPath = SaveActionUtils.getExcelDir() + File.separator + filename;
		// 当前路径/mnt/sdcart/Excel/Data/当前时间.xls
		excel_Source_File = new File(SaveActionUtils.getExcelDir() + File.separator + filename);// 得到当前这个文件=
		filename = str_date + "_Result" + ".csv";
		excel_Result_File = new File(SaveActionUtils.getExcelDir() + File.separator + filename);
		filename = str_date + "_Filter" + ".csv";
		excel_Filter_File = new File(SaveActionUtils.getExcelDir() + File.separator + filename);
	}
	
	public void resultInit(){
		pointf = new MyPointF();
		vecTegPoint = new ArrayList<MyPointF>();
		tv_rValue = (TextView)view.findViewById(R.id.tv_rValue);
		tv_kValue = (TextView)view.findViewById(R.id.tv_kValue);
		tv_angleValue = (TextView)view.findViewById(R.id.tv_angleValue);
		tv_maValue = (TextView)view.findViewById(R.id.tv_maValue);
	}
	
	//发送消息控制 抽取代码  第一个参数发送是否是开始采集命令，，，第二个参数是否是再次发送采集数据
	public void sendData(boolean isStart,boolean isRepeatStart){
		if(!mConnected){
			//没有连接  提示没有连接
			tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("没有连接");
			return;
		}
		if(!mConnectedService){
			//没有连接  提示没有连接
			tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("无法搜索到服务，请重新连接");
			return;
		}
		if(isStart){
			iCount = 0;// 次数要清空
			flag = false;// 要暂时停止画曲线了
			flag_result = false;// 要暂时画最终的曲线
			xList.removeAll(xList);
			yList.removeAll(yList);
			listByte.removeAll(listByte);// 先清掉数据
			mService.updateRender(frequency_value[1], frequency_value[0]);
			mService.clearValue();
			if(!isRepeatStart){
				mService_result.clearValue();//要是第二次画就不用清除之前的数据
			}
			// 每次开始扫描都必须要重新设置x轴的值，因为在配置中是要发生变化的
			// 设置x轴的值
			double tempVar = frequency_value[0] * 1000;
			pointCount = (frequency_value[1] - frequency_value[0]) * 1000 / frequency_value[2] + 1;
			for (int i = 0; i < pointCount; i++) {
				xList.add(tempVar / 1000);
				tempVar = tempVar + frequency_value[2];
			}
			source = new double[pointCount];//每次都创建太消耗内存了   解决方案在发送的时候根据x轴的数据创建数组
			if(!isRepeatStart){
				beforeDate = new Date();
			}
		}
		read();//读取数据
		final int charaProp = characteristic.getProperties();
		//如果该char可写
		if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification( mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            //读取数据 在回调函数中
            if(isStart){
            	if(!isRepeatStart){
            		characteristic.setValue(sendData);
	            }else{
	            	characteristic.setValue(sendData_real);//如果是第二次发送  则发送的是新的数据
	            }
	            mBluetoothLeService.writeCharacteristic(characteristic);
            }else{//发送停止信号
            	characteristic.setValue(sendData_stop);
	            mBluetoothLeService.writeCharacteristic(characteristic);
	            setScreenBrightness(getSystemScreenBrightness());//发送停止也让其亮屏
            }
//                Toast.makeText(getApplicationContext(), "写入成功！", Toast.LENGTH_SHORT).show();
            tv_times.setTextColor(Color.parseColor("#5D5B5B"));
			tv_times.setText("发送成功");
			if(isStart){
				//发送成功才保存  不然一直创建文件
				if(!isRepeatStart){//因为只有第一次发送的时候 才新建文件
					saveInit();//执行保存数据操作的一些变量初始化  每次点击都创建新的csv文件
				}
				// 此处与保存数据有关 每次开始扫描的时候就在当前项目下的表中创建一个Sheet表
				SaveActionUtils.exportCSV(excel_Source_File, xList);
				SaveActionUtils.exportCSV(excel_Filter_File, xList);
	//				SaveActionUtils.exportCSV(excel_Result_File, new double[]{0d,0d});
				//不然画图部分看着是一条直线
				isQuerySave = true;//说明已经连接上了  退出的时候询问是否保存  params
			}
        }else{
        	tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("发送失败，请重新发送");
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
        }
	}
	
	//初始化选项菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_connect, menu);
		if(mConnected){
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		}else{
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}
	//点击标题栏连接  为菜单项设置监听器
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			break;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			break;
		}
		return true;
	}
	/**
	 * 读函数
	 */
	private void read(){
		mBluetoothLeService.setCharacteristicNotification(readCharacteristic, true);
	}
	//通过服务控制不同的事件
	//使用匿名类 使用广播监听  蓝牙连接、可读写的状态
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            	mConnected = true;
            	invalidateOptionsMenu();//刷新当前的菜单
            	tv_times.setTextColor(Color.parseColor("#5D5B5B"));
            	tv_times.setText("正搜索服务....");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectedService = false;
                invalidateOptionsMenu();//刷新当前的菜单
                tv_times.setTextColor(Color.parseColor("#FF0000"));
				tv_times.setText("连接断开");
				//连接断开的同时 关闭保持屏幕常亮
				btn_Start_Scan.setKeepScreenOn(false);
            } 
            //发现有可支持的服务
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            	//写数据的服务和characteristic
            	mnotyGattService = mBluetoothLeService.getSupportedGattServices(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                characteristic = mnotyGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                //读数据的服务和characteristic
                readMnotyGattService = mBluetoothLeService.getSupportedGattServices(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                readCharacteristic = readMnotyGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                //只有发现服务了 ，，我才当你是完全连接成功了
                mConnectedService = true;
				tv_times.setTextColor(Color.parseColor("#5D5B5B"));
				tv_times.setText("连接成功");
            } 
            //显示数据
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            	//有数据来了  短路逻辑  若包含就去判断包含的这个是不是最后的
            	tempStr = ConvertUtils.bytesToHexString(data);
            	if(!tempStr.contains("FF01")){//不管是2个字节还是4个字节  应该说是不管是多少字节的数据
            		for(int i = 0; i < data.length; i++){
            			listByte.add(data[i]);
            		}
            	}else{//包含 ff01--还得判断此ff01是不是结尾的那个ff01//判断此ff01是不是真的结尾的那个ff01 
            		if(tempStr.indexOf("FF01") % 2 != 0){
            			Log.e("######", "#######" + tempStr + "#######"+listByte.size());
            			for(int i = 0; i < data.length; i++){
                			listByte.add(data[i]);
                		}
            		}else{
	            		byte[] tempByte = ConvertUtils.hexStringToBytes(tempStr.substring(0, tempStr.indexOf("FF01")));
	            		//极有可能 tempByte为空  因为是以ff01起头的  ---20171121 报错了很多次 脑子呆笨没有发现
	            		if(tempByte != null){
		            		for(int i = 0; i < tempByte.length; i++){
		            			listByte.add(tempByte[i]);
		            		}
	            		}
	//            		//？？？如何判断到底ff01是最后还是不是最后
	            		if(listByte.size() < (pointCount*2)){//说明数据中包含了 ff01
	            			tempByte = ConvertUtils.hexStringToBytes("FF01");
	            			Log.e("######", "#######"+listByte.size());
	            			for(int i = 0; i < tempByte.length; i++){
		            			listByte.add(tempByte[i]);
		            		}
	            		}else{
	            			processBuffer();
	            		}
	            		//判断是不是以ff01结尾的 若是的话 说明后面没有数据了 再添加了
	            		if(!tempStr.endsWith("FF01")){
	            			tempByte = ConvertUtils.hexStringToBytes(tempStr.substring(tempStr.indexOf("FF01") + 4));
	            			for(int i = 0; i < tempByte.length; i++){
	                			listByte.add(tempByte[i]);
	                		}
	            		}//此处仍然是有漏洞的，，，因为有可能接收到的数据并不是偶数  怎么破？----于20171121
            		}
            	}
            }
        }
    };  
	
    public void processBuffer() {
		// 已经来了一次扫描的数据了// 说明一次扫描正常结束 现在只考虑正常的情况下	//每次给yList添加数据之前都清空一次
		yList.removeAll(yList);
		yListTemp.removeAll(yListTemp);
		yListFilter.removeAll(yListFilter);//重新幅值之前必须清空
//		source = new double[listByte.size()/2];//每次都创建太消耗内存了   解决方案在发送的时候根据x轴的数据长度创建数组
		for (int i = 0, j = 0; i < listByte.size() - 1; i = i + 2, j++) {
//			yList.add((listByte.get(i) & 0xff) * 256 + (listByte.get(i + 1) & 0xff));//此处在添加滤波之后修改
			//因为最终画的曲线应该是滤波之后的曲线
			double temp = (listByte.get(i) & 0xff) * 256 + (listByte.get(i + 1) & 0xff);
			if(j < source.length){
				source[j] = temp;
			}//此处添加是为了防止数据出错 没有判断到0xff01结果接收到的是两次的数据
			yListTemp.add(temp);
		}
		// 将数据保存了之后listByte得清空
		listByte.removeAll(listByte);
		handler.sendMessage(handler.obtainMessage(1));
	}
    

	// 更新界面的Hanlder 类
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// 每次来数据时候都获取两次间隔时间
				currentDate = new Date();
				interval_time = (currentDate.getTime() - beforeDate.getTime());
				interval_time = interval_time / 1000;
				pointf.x = interval_time;
				// 一定要在求两次时间之后，不然会影响凝血曲线时间的判断//画图之前计算最值之前先进行滤波处理//输入必须是double类型？
				source = process_Data(source);//去对数据简单滤波
				for(int i = 0; i < source.length; i++){
					yList.add(source[i]);
					yListFilter.add(source[i]);
				}//yList和yListFilter中存储的都是滤波之后的数据，，yList是去画曲线，yListFilter是去保存
				double tempData = Collections.max(yList);//此处求最大值是不合理的   因为原始数据需要做处理
				maxValue = (float) tempData * 2 / 65536;
				maxValue = (float)(Math.round(maxValue* 10000))/10000;//保留小数点后四位
				pointf.y = maxValue;
				vecTegPoint.add(pointf);//此处求的是最终的凝血曲线的数据
				//又来了一次的数据 先清空之前的数据
				mService.clearValue();
				flag_result = true;
				flag = true;
				iCount++;
				handler.sendMessage(handler.obtainMessage(3));
				// 判断接收到的数据有没有扫描一次的数据以上 则开始画图
				// 将yList保存到excel中
				//TODO 下次测试一下导入数据的时间
				SaveActionUtils.exportCSV(excel_Source_File,yListTemp);//保存原始数据
				SaveActionUtils.exportCSV(excel_Filter_File, yListFilter);//保存滤波之后的数据
				SaveActionUtils.exportCSV(excel_Result_File, pointf);
				break;
			case 2:
				// 开始动态画曲线//一边采集一边画曲线
				if (flag) {
					if (iAuto < xList.size() && iAuto < yList.size()) {
						mService.updateChart(xList.get(iAuto), yList.get(iAuto));
						iAuto++;
						// Log.e("错误", iAuto + "");
					} else {
						// 已经画完一条曲线了，清空yList 然后添加下一次的
						yList.removeAll(yList);
						flag = false;// 先停止绘图
						iAuto = 0;
					}
				}
				if (flag_result) {
					mService_result.updateChart(interval_time, maxValue);
					mService_result.getRenderer().setXAxisMax(interval_time);//设置最大值就是当前时间最大值
					mService_result.getRenderer().setYAxisMax(maxValue + maxValue/10); //设置y的最大值就是当前值+ 10%
					flag_result = false;// 每次画完就停止画
				}
				break;
			case 3:
//				Toast.makeText(ScanDisplayActivity.this, "第" + iCount + "次扫描结束",
//						Toast.LENGTH_SHORT).show();
				//每次扫描到3次结束的时候暗屏  
				if(iCount == times_point){
					setScreenBrightness(20);
				}
				if(iCount == times && !isFirstSend){
					//扫描结束恢复到系统自动的亮度
					setScreenBrightness(getSystemScreenBrightness());
					btn_Start_Scan.setKeepScreenOn(false);//扫描结束不需要再保持屏幕常亮
				}
				tv_times.setText("第" + iCount + "次扫描结束");
				if(iCount == 2 && isFirstSend){
					isFirstSend = false;//说明是第一次发送然后重新计算起止频率
					//最大值和最小值所在的频率点 强转成int
					double minFrequency = xList.get(yList.indexOf(Collections.max(yList))) * 100;
					double maxFrequency = xList.get(yList.indexOf(Collections.min(yList))) * 100;
					if(minFrequency > maxFrequency){
						double temp = minFrequency;
						minFrequency = maxFrequency;
						maxFrequency = temp;
					}//为了防止意外，，，，，频率值大小 反了。
					frequency_value[0] = (int)(minFrequency - (maxFrequency - minFrequency))/100;
					frequency_value[1] = (int)(maxFrequency + (maxFrequency - minFrequency))/100;
					//重新设置要发送的数据
					sendData_real = ConvertUtils.hexStringToBytes(sendString.substring(0, 8) + 
							ConvertUtils.HighExchangeLow(ConvertUtils.dataConvertHex(frequency_value[0] + "00")) + 
							ConvertUtils.HighExchangeLow(ConvertUtils.dataConvertHex(frequency_value[1] + "00")) +
							sendString.substring(16));//重新计算
					Log.e("#####", ConvertUtils.bytesToHexString(sendData_real));
					sendData(true, true);
				}
				break;
			}
		}
	}
	
	private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		//每次页面交互的时候 注册广播 监听
//		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());//注册监听蓝牙一些服务的广播
//		 if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);//注册广播的时候连接去连接
//            Log.d(TAG, "Connect request result=" + result);
//        }
		//确保当前蓝牙设备是开着的 若没有，，就需要启动dialog  询问是否打开蓝牙
		if(!MainActivity.mBluetoothAdapter.isEnabled()){
			if(!MainActivity.mBluetoothAdapter.isEnabled()){
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		unregisterReceiver(mGattUpdateReceiver);//取消注册广播
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null; 
		unregisterReceiver(mGattUpdateReceiver);//取消注册广播
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode){
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_CANCELED){
				//若点击了取消 就退出当前活动
				this.finish();
				return;
			}
			break;	
		case REQUEST_PARAM:
			if (resultCode == RESULT_OK) {
				sendString = intent.getStringExtra("params");
				frequency_value = intent.getIntArrayExtra("value");
				times = intent.getIntExtra("times", 1024);//默认是10
				sendData_real = ConvertUtils.hexStringToBytes(sendString);//次数正确 要修改的是频率
				//将其设置为2次				
				sendData = ConvertUtils.hexStringToBytes(sendString.substring(0, sendString.length()-10) + "0200" + sendString.substring(sendString.length()-6));
				
			}
			break;
		}
	}
	/*
	 *点击退出按钮之后  弹出对话框询问是否保存 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//点击Back按钮  点击返回的时候  先询问保存是否保存凝血曲线求得的参数值
			// 还得满足是不是真的采集了数据   即 点击了开始扫描
			if(isQuerySave && params != null){
				AlertDialog.Builder isSave = new Builder(this);//创建对话框
				isSave.setTitle("系统提示");
				isSave.setMessage("是否保存最后结果数据？");
				isSave.setPositiveButton("保存", new DialogInterface.OnClickListener() {//添加确定保存按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {//保存按钮事件
	//					Params params = new Params();
						SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//调用可写入的数据库
						ContentValues values = new ContentValues();//保存数据
						values.put("r_value", params.getR_value().x);
						values.put("k_value", params.getK_value().x);
						values.put("angel", params.getAngle().x);
						values.put("ma_value", params.getMa_value().x);
	//					values.put("r_value", 6.3);
	//					values.put("k_value", 1.9);
	//					values.put("angel", 60);
	//					values.put("ma_value", 60);
						if(str_date == null){
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							str_date = sdf.format(new Date()).toString();
						}
						values.put("date", str_date);
						db.insert("Params", null, values);
						finish();
					}
				}).setNegativeButton("不保存", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();//不保存直接退出
					}
				});
				isSave.show();
			}
		}
		finish();
		return flag;
	}
	//Begin调节屏幕亮暗度
  	/**
  	 * 如果需要亮度调节，首先需要设置屏幕亮度调节模式为手动模式
  	 */
  	public void setScrennManualMode() {
  		ContentResolver contentResolver = this.getContentResolver();
  		try {
  			int mode = Settings.System.getInt(contentResolver,
  					Settings.System.SCREEN_BRIGHTNESS_MODE);
  			if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
  				Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
  						Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
  			}
  		} catch (Settings.SettingNotFoundException e) {
  			e.printStackTrace();
  		}
  	}
  	/**
  	 * 获取系统屏幕亮度值。屏幕最大亮度值255，最低亮度值为0.
  	 * @return
  	 */
  	private int getSystemScreenBrightness() {
  		int systemBrightness = 0;
  		try {
  			systemBrightness = Settings.System.getInt(this.getContentResolver(),
  					Settings.System.SCREEN_BRIGHTNESS);
  		} catch (SettingNotFoundException e) {
  			e.printStackTrace();
  		}
  		return systemBrightness;
  	}
  	/**
  	 * 获取当前屏幕亮度值
  	 * @return
  	 */
  	private float getCurrentScreenBrightness(){
  		return this.getWindow().getAttributes().screenBrightness;
  	}
  	/**
  	 * 设置屏幕亮度值
  	 */
  	private void setScreenBrightness(int value) {
  		setScrennManualMode();//将其设置为手动模式
  		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
  		lp.screenBrightness = value < 0 ? 20/255f : value/255f;
  		this.getWindow().setAttributes(lp);
  	}
  	//End调节屏幕亮暗度
  	/** 
  	 * 触摸屏事件  点击的时候屏幕回到系统的亮度
  	 */
  	@Override
  	public boolean onTouchEvent(MotionEvent event) {
  		if(getCurrentScreenBrightness() == 20/255f){
  			//说明是在采集中 在扫描中 将其暗屏
//  			Toast.makeText(this, "屏幕点击事件", Toast.LENGTH_SHORT).show();
  			setScreenBrightness(getSystemScreenBrightness());//但是隔一段时间自动暗屏，如果是在扫描的时候的话
  			times_point = iCount + 2;
  		}//如果不是 是暗屏的状态就不管
  		return super.onTouchEvent(event);
  	}
	//Start    Java的JNI技术
	static{
		System.loadLibrary("CALLC");
	}
	public native double[] process_Data(double[] source);//本地方法 对数据滤波处理的算法
	//End      Java的JNI技术
}
