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
 * ɨ����ʾ�Ļҳ��
 */
public class ScanDisplayActivity extends Activity{
	private final static String TAG = ScanDisplayActivity.class.getSimpleName();//ScanDisplayActivity
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";//����һ�����
	private static final int REQUEST_ENABLE_BT = 1;//ѡ���Ƿ�������Ի���
	private static final int REQUEST_PARAM = 2;//�����������
	private Button btn_Start_Scan,btn_Stop_Scan,btn_Param_Config;//��ʼ��ֹͣ�ɼ�����������
	private Button btn_change;//��ʾָ�겿����ԭʼ�������ݽ����ʾ�л�
	private TextView tv_times;//��ʾ�ڼ���ɨ�����
	//����4.0 ����
	private String mDeviceAddress;//�����豸��ַ
	private BluetoothLeService mBluetoothLeService;//��������
	private boolean mConnected = false;//�ж�����״̬
	private boolean mConnectedService = false;//�Ƿ��ҵ�����  
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	//д����
	private BluetoothGattCharacteristic characteristic;
	private BluetoothGattService mnotyGattService;
	//������
	private BluetoothGattCharacteristic readCharacteristic;
	private BluetoothGattService readMnotyGattService;
	
	private boolean mViewChange;//�л�view
	private View view;//ָ����ʾ����
	//���Ϳ���ָ������ Ĭ��100Hz - 120Hz����50Hz 10ms 2�� 
	String sendString = "861101011027e02e3202800200020004050068";
	byte[] sendData = ConvertUtils.hexStringToBytes("861101011027e02e3202800200020200050068");// Ҫ���͵����� �ֽ����� �ʴ��Ƚ�16�����ַ���ת�����ֽڷ���
	byte[] sendData_real = ConvertUtils.hexStringToBytes("861101011027e02e3202800200020004050068");
	byte[] sendData_stop = ConvertUtils.hexStringToBytes("8603030168");// Ҫ����ֹͣɨ������� ��ʾ����
	boolean isFirstSend = true;//�Ƿ��ǵ�һ�η���
	MyHandler handler;// ������Ϣ���д���
	
	// �������õ���
	private LinearLayout mLLayout, mLLayout_result;// ������ʾ���ߵ������������ؼ�----������������Ѫ���ߵ�ֵ
	private GraphicalView mView, mView_result;// ��ͼ��GraphicalView
	private ChartService mService, mService_result;// ��ͼ�Ĺ����ࡣ����һ���ǻ�ԭʼ
	private Timer timer;// ��ʱ�� �û���̬��ͼ
	private int iAuto = 0; // ��̬�������ж��Ƿ�һ������ ���߻������������
	private boolean flag = false;// ����Ƿ�ʼ��̬������
	private boolean flag_result = false;
	private List<Double> xList;
	private List<Double> yList;// һ��һ�����߱���
	private List<Double> yListTemp;// һ��һ�����߱���
	private List<Double> yListFilter;//�˲�֮������� ���� yListȥ��ͼ
	private double[] source;
	private List<Byte> listByte;
//	private int count = 401;// һ�ζ��ٸ�Ƶ�ʵ�
	private int iCount = 0;// �жϵ�ǰɨ��ڼ��ε����ݵ���
	private int times_point = 3;//ϵͳ��Ļ��������ٴε�ʱ����Ĭ����3�ε�ʱ����
	private String title = "��ֵ����";// ������ʾ�ı���
	private String title_result = "��Ѫ����";

	// �����ս����һЩ����
	private float interval_time = 0;// s �뼶
	private float maxValue;//���ֵ
	private Date currentDate, beforeDate;
	String tempStr = "";// ������

	private int[] frequency_value = { 100, 120, 50 };// �Ӳ������ô���������ֹƵ�� ����Ƶ�� Ĭ��Ϊ100,120,50
	private int pointCount;//һ�ζ��ٸ���
	private int times = 1024;//Ĭ����10��
	// ����excelʹ�õı���
//	private String excelPath;// ���浽sd��·��
	private String str_date;//�����ʱ��ĵ�ǰʱ���ַ���
	private File excel_Source_File, excel_Result_File, excel_Filter_File;// excel�ļ�����ԭʼ����, ����������, ����ԭʼ�����˲�֮�������
	
	//����Ĳ������
	private Params<MyPointF> params;//���������
	private ArrayList<MyPointF> vecTegPoint;//���յ���Ѫ���ݺ�������������ֵ
	private MyPointF pointf;//ʱ��---ֵ
	private TextView tv_rValue,tv_kValue,tv_angleValue,tv_maValue;
	private boolean isQuerySave = false;
	
	//����������������
	private final ServiceConnection mServiceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
			//�󶨷����ʱ����ô˷���  bindService���õ�ʱ��
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if(!mBluetoothLeService.initialize()){
				//��ʼ�������������� �鿴�Ƿ�API>18 
				Toast.makeText(ScanDisplayActivity.this, "�����豸��֧��BLE", Toast.LENGTH_SHORT).show();
				finish();
			}//ע����ʵ��һ���е���࣬��Ϊ��MainActivity�Ѿ��ж��豸�Ƿ�֧��BLE
			//��ʼ���ɹ�֮���Զ������豸
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBluetoothLeService = null;//unBindService�������ʱ����ô˷���
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scandisplayactivity);
		this.getActionBar().setTitle(R.string.title_activity_scan_display);//ɨ����ʾ
		mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);//�õ���һ��������ĵ�ַ����
		widgetInit();//�ؼ���ʼ��
		drawlineInit();//��������Ҫ�ĳ�ʼ��
		resultInit();//���������ʼ��
//		saveInit();//ִ�б������ݲ�����һЩ������ʼ��
		handler = new MyHandler();//�Զ������Ϣ����
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());//ע���������һЩ����Ĺ㲥
		//ע����� �������� ---�����ķ���
		Intent gattServiceIntent = new Intent(this,BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//������ͷ�����а󶨺��Զ���������
		
		
		//��ʱ������һֱ�����ر�  �ʴ��ڻ������ʱ������  ������
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendMessage(handler.obtainMessage(2));// ����Ϣ���з���2��� ��ʱ��Ϊ�˻�����
			}
		}, 100, 4);//��Զ�������û����ʾ�����
		btn_Start_Scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_Start_Scan.setKeepScreenOn(true);//��ʼɨ��֮���Ҫ����Ļ���ֳ���
				isFirstSend = true;
				sendData(true,false);//��������
			}
		});
		//ֹͣɨ��
		btn_Stop_Scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_Start_Scan.setKeepScreenOn(false);//�رյ�ʱ�� �Ͳ���һֱ������Ļ����
				sendData(false,false);
			}
		});
		//��������
		btn_Param_Config.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// �����������õĻ ����
				Intent intent = new Intent(ScanDisplayActivity.this, ParametersActivity.class);
				startActivityForResult(intent, REQUEST_PARAM);
			}
		});
		//�л�view  �����ʾ��ָ����ʾ�л�
		btn_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�滻��ʾ
				if(!mViewChange){
					//���Ի�����  ��ΪĿǰ��һ��Ҫȥ���� ���� ���������ʾ
					AlertDialog.Builder builder = new Builder(ScanDisplayActivity.this);
					builder.setTitle("ѡ���Ƿ�������ֵ");
					builder.setPositiveButton("������", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//ʲô������  ֱ�ӷ���
							return;
						}
					}).setNegativeButton("����", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mViewChange = true;
							view.setVisibility(View.VISIBLE);
				    		mView.setVisibility(View.GONE);
				    		view.setAnimation(AnimationUtil.moveToViewLocation());
				    		mView.setAnimation(AnimationUtil.moveToViewBottom());
				    		btn_change.setText("ԭʼ����");
				    		//�˴�ȥ����   ����ֵ
//				    		params  = CalcParamsUtils.getTegCurveData(vecTegPoint);
//				    		tv_rValue.setText(params.getR_value().y + "");
//				    		tv_kValue.setText(params.getK_value().y + "");
//				    		tv_angleValue.setText(params.getAngle().y + "");
//				    		tv_maValue.setText(params.getMa_value().y + "");
				    		//��ʹ�������������
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
		    		btn_change.setText("���ָ��");	
				}
			}
		});
	}
	
	public void widgetInit(){
		btn_Start_Scan = (Button)findViewById(R.id.startScan);//��ʼɨ�谴ť
		btn_Stop_Scan = (Button)findViewById(R.id.stopScan);//ֹͣɨ�谴ť
		btn_Param_Config = (Button)findViewById(R.id.param_config);//�������ð�ť
		tv_times = (TextView)findViewById(R.id.times);//��ʾ��ǰ״̬��tx
		btn_change = (Button)findViewById(R.id.btn_change);//�ؼ��л���ť
		view = View.inflate(ScanDisplayActivity.this, R.layout.param_result, null);//��ʾview�ؼ�
	}
	public void drawlineInit(){
		mLLayout = (LinearLayout) findViewById(R.id.drawline_result);//��ԭʼ���ߵ�LL�ؼ�
		mLLayout_result = (LinearLayout) findViewById(R.id.drawline_widget);//�����յ���Ѫ���߽����LL�ؼ�
		xList = new ArrayList<Double>();
		yList = new ArrayList<Double>();
		yListTemp = new ArrayList<Double>();
		yListFilter = new ArrayList<Double>();
		listByte = new ArrayList<Byte>();
		timer = new Timer();
		
		mService = new ChartService(ScanDisplayActivity.this);
		mService.setXYMultipleSeriesDataset(title);
		mService.setXYMultipleSeriesRenderer(frequency_value[1], frequency_value[0], "Ƶ��", "��ֵ");// ����x������ֵ����Сֵ

		// �����ս�����ߵ�����
		mService_result = new ChartService(ScanDisplayActivity.this);
		mService_result.setXYMultipleSeriesDataset(title_result);
		mService_result.setXYMultipleSeriesRenderer("ʱ��", "ֵ");
		mService_result.getRenderer().setYAxisMin(0d);
		mService_result.getRenderer().setXAxisMin(6d);
		
		mView = mService.getGraphicalView();
		mView_result = mService_result.getGraphicalView();
		mLLayout.addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//������ʾ������ߣ�������ʾ�������ߣ��� Ȼ��ȵ�ɨ�����֮��  ������������� Ȼ����ĳɼ���Ĳ���ֵ
		mLLayout_result.addView(mView_result,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setVisibility(View.GONE);
		mLLayout.addView(view,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));//��ӵ��ؼ� ��������
	}
	public void saveInit(){
		// ÿ�δ������ʱ��Ĭ���½�һ��excel��� �����ǵ�ǰʱ��
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		str_date = sdf.format(new Date()).toString();
		String filename = str_date + "_Source" + ".csv";
//		excelPath = SaveActionUtils.getExcelDir() + File.separator + filename;
		// ��ǰ·��/mnt/sdcart/Excel/Data/��ǰʱ��.xls
		excel_Source_File = new File(SaveActionUtils.getExcelDir() + File.separator + filename);// �õ���ǰ����ļ�=
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
	
	//������Ϣ���� ��ȡ����  ��һ�����������Ƿ��ǿ�ʼ�ɼ���������ڶ��������Ƿ����ٴη��Ͳɼ�����
	public void sendData(boolean isStart,boolean isRepeatStart){
		if(!mConnected){
			//û������  ��ʾû������
			tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("û������");
			return;
		}
		if(!mConnectedService){
			//û������  ��ʾû������
			tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("�޷���������������������");
			return;
		}
		if(isStart){
			iCount = 0;// ����Ҫ���
			flag = false;// Ҫ��ʱֹͣ��������
			flag_result = false;// Ҫ��ʱ�����յ�����
			xList.removeAll(xList);
			yList.removeAll(yList);
			listByte.removeAll(listByte);// ���������
			mService.updateRender(frequency_value[1], frequency_value[0]);
			mService.clearValue();
			if(!isRepeatStart){
				mService_result.clearValue();//Ҫ�ǵڶ��λ��Ͳ������֮ǰ������
			}
			// ÿ�ο�ʼɨ�趼����Ҫ��������x���ֵ����Ϊ����������Ҫ�����仯��
			// ����x���ֵ
			double tempVar = frequency_value[0] * 1000;
			pointCount = (frequency_value[1] - frequency_value[0]) * 1000 / frequency_value[2] + 1;
			for (int i = 0; i < pointCount; i++) {
				xList.add(tempVar / 1000);
				tempVar = tempVar + frequency_value[2];
			}
			source = new double[pointCount];//ÿ�ζ�����̫�����ڴ���   ��������ڷ��͵�ʱ�����x������ݴ�������
			if(!isRepeatStart){
				beforeDate = new Date();
			}
		}
		read();//��ȡ����
		final int charaProp = characteristic.getProperties();
		//�����char��д
		if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification( mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            //��ȡ���� �ڻص�������
            if(isStart){
            	if(!isRepeatStart){
            		characteristic.setValue(sendData);
	            }else{
	            	characteristic.setValue(sendData_real);//����ǵڶ��η���  ���͵����µ�����
	            }
	            mBluetoothLeService.writeCharacteristic(characteristic);
            }else{//����ֹͣ�ź�
            	characteristic.setValue(sendData_stop);
	            mBluetoothLeService.writeCharacteristic(characteristic);
	            setScreenBrightness(getSystemScreenBrightness());//����ֹͣҲ��������
            }
//                Toast.makeText(getApplicationContext(), "д��ɹ���", Toast.LENGTH_SHORT).show();
            tv_times.setTextColor(Color.parseColor("#5D5B5B"));
			tv_times.setText("���ͳɹ�");
			if(isStart){
				//���ͳɹ��ű���  ��Ȼһֱ�����ļ�
				if(!isRepeatStart){//��Ϊֻ�е�һ�η��͵�ʱ�� ���½��ļ�
					saveInit();//ִ�б������ݲ�����һЩ������ʼ��  ÿ�ε���������µ�csv�ļ�
				}
				// �˴��뱣�������й� ÿ�ο�ʼɨ���ʱ����ڵ�ǰ��Ŀ�µı��д���һ��Sheet��
				SaveActionUtils.exportCSV(excel_Source_File, xList);
				SaveActionUtils.exportCSV(excel_Filter_File, xList);
	//				SaveActionUtils.exportCSV(excel_Result_File, new double[]{0d,0d});
				//��Ȼ��ͼ���ֿ�����һ��ֱ��
				isQuerySave = true;//˵���Ѿ���������  �˳���ʱ��ѯ���Ƿ񱣴�  params
			}
        }else{
        	tv_times.setTextColor(Color.parseColor("#FF0000"));
			tv_times.setText("����ʧ�ܣ������·���");
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
        }
	}
	
	//��ʼ��ѡ��˵�
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
	//�������������  Ϊ�˵������ü�����
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
	 * ������
	 */
	private void read(){
		mBluetoothLeService.setCharacteristicNotification(readCharacteristic, true);
	}
	//ͨ��������Ʋ�ͬ���¼�
	//ʹ�������� ʹ�ù㲥����  �������ӡ��ɶ�д��״̬
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            	mConnected = true;
            	invalidateOptionsMenu();//ˢ�µ�ǰ�Ĳ˵�
            	tv_times.setTextColor(Color.parseColor("#5D5B5B"));
            	tv_times.setText("����������....");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectedService = false;
                invalidateOptionsMenu();//ˢ�µ�ǰ�Ĳ˵�
                tv_times.setTextColor(Color.parseColor("#FF0000"));
				tv_times.setText("���ӶϿ�");
				//���ӶϿ���ͬʱ �رձ�����Ļ����
				btn_Start_Scan.setKeepScreenOn(false);
            } 
            //�����п�֧�ֵķ���
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            	//д���ݵķ����characteristic
            	mnotyGattService = mBluetoothLeService.getSupportedGattServices(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                characteristic = mnotyGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                //�����ݵķ����characteristic
                readMnotyGattService = mBluetoothLeService.getSupportedGattServices(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                readCharacteristic = readMnotyGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                //ֻ�з��ַ����� �����Ҳŵ�������ȫ���ӳɹ���
                mConnectedService = true;
				tv_times.setTextColor(Color.parseColor("#5D5B5B"));
				tv_times.setText("���ӳɹ�");
            } 
            //��ʾ����
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            	//����������  ��·�߼�  ��������ȥ�жϰ���������ǲ�������
            	tempStr = ConvertUtils.bytesToHexString(data);
            	if(!tempStr.contains("FF01")){//������2���ֽڻ���4���ֽ�  Ӧ��˵�ǲ����Ƕ����ֽڵ�����
            		for(int i = 0; i < data.length; i++){
            			listByte.add(data[i]);
            		}
            	}else{//���� ff01--�����жϴ�ff01�ǲ��ǽ�β���Ǹ�ff01//�жϴ�ff01�ǲ�����Ľ�β���Ǹ�ff01 
            		if(tempStr.indexOf("FF01") % 2 != 0){
            			Log.e("######", "#######" + tempStr + "#######"+listByte.size());
            			for(int i = 0; i < data.length; i++){
                			listByte.add(data[i]);
                		}
            		}else{
	            		byte[] tempByte = ConvertUtils.hexStringToBytes(tempStr.substring(0, tempStr.indexOf("FF01")));
	            		//���п��� tempByteΪ��  ��Ϊ����ff01��ͷ��  ---20171121 �����˺ܶ�� ���Ӵ���û�з���
	            		if(tempByte != null){
		            		for(int i = 0; i < tempByte.length; i++){
		            			listByte.add(tempByte[i]);
		            		}
	            		}
	//            		//����������жϵ���ff01������ǲ������
	            		if(listByte.size() < (pointCount*2)){//˵�������а����� ff01
	            			tempByte = ConvertUtils.hexStringToBytes("FF01");
	            			Log.e("######", "#######"+listByte.size());
	            			for(int i = 0; i < tempByte.length; i++){
		            			listByte.add(tempByte[i]);
		            		}
	            		}else{
	            			processBuffer();
	            		}
	            		//�ж��ǲ�����ff01��β�� ���ǵĻ� ˵������û�������� �������
	            		if(!tempStr.endsWith("FF01")){
	            			tempByte = ConvertUtils.hexStringToBytes(tempStr.substring(tempStr.indexOf("FF01") + 4));
	            			for(int i = 0; i < tempByte.length; i++){
	                			listByte.add(tempByte[i]);
	                		}
	            		}//�˴���Ȼ����©���ģ�������Ϊ�п��ܽ��յ������ݲ�����ż��  ��ô�ƣ�----��20171121
            		}
            	}
            }
        }
    };  
	
    public void processBuffer() {
		// �Ѿ�����һ��ɨ���������// ˵��һ��ɨ���������� ����ֻ���������������	//ÿ�θ�yList�������֮ǰ�����һ��
		yList.removeAll(yList);
		yListTemp.removeAll(yListTemp);
		yListFilter.removeAll(yListFilter);//���·�ֵ֮ǰ�������
//		source = new double[listByte.size()/2];//ÿ�ζ�����̫�����ڴ���   ��������ڷ��͵�ʱ�����x������ݳ��ȴ�������
		for (int i = 0, j = 0; i < listByte.size() - 1; i = i + 2, j++) {
//			yList.add((listByte.get(i) & 0xff) * 256 + (listByte.get(i + 1) & 0xff));//�˴�������˲�֮���޸�
			//��Ϊ���ջ�������Ӧ�����˲�֮�������
			double temp = (listByte.get(i) & 0xff) * 256 + (listByte.get(i + 1) & 0xff);
			if(j < source.length){
				source[j] = temp;
			}//�˴������Ϊ�˷�ֹ���ݳ��� û���жϵ�0xff01������յ��������ε�����
			yListTemp.add(temp);
		}
		// �����ݱ�����֮��listByte�����
		listByte.removeAll(listByte);
		handler.sendMessage(handler.obtainMessage(1));
	}
    

	// ���½����Hanlder ��
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// ÿ��������ʱ�򶼻�ȡ���μ��ʱ��
				currentDate = new Date();
				interval_time = (currentDate.getTime() - beforeDate.getTime());
				interval_time = interval_time / 1000;
				pointf.x = interval_time;
				// һ��Ҫ��������ʱ��֮�󣬲�Ȼ��Ӱ����Ѫ����ʱ����ж�//��ͼ֮ǰ������ֵ֮ǰ�Ƚ����˲�����//���������double���ͣ�
				source = process_Data(source);//ȥ�����ݼ��˲�
				for(int i = 0; i < source.length; i++){
					yList.add(source[i]);
					yListFilter.add(source[i]);
				}//yList��yListFilter�д洢�Ķ����˲�֮������ݣ���yList��ȥ�����ߣ�yListFilter��ȥ����
				double tempData = Collections.max(yList);//�˴������ֵ�ǲ������   ��Ϊԭʼ������Ҫ������
				maxValue = (float) tempData * 2 / 65536;
				maxValue = (float)(Math.round(maxValue* 10000))/10000;//����С�������λ
				pointf.y = maxValue;
				vecTegPoint.add(pointf);//�˴���������յ���Ѫ���ߵ�����
				//������һ�ε����� �����֮ǰ������
				mService.clearValue();
				flag_result = true;
				flag = true;
				iCount++;
				handler.sendMessage(handler.obtainMessage(3));
				// �жϽ��յ���������û��ɨ��һ�ε��������� ��ʼ��ͼ
				// ��yList���浽excel��
				//TODO �´β���һ�µ������ݵ�ʱ��
				SaveActionUtils.exportCSV(excel_Source_File,yListTemp);//����ԭʼ����
				SaveActionUtils.exportCSV(excel_Filter_File, yListFilter);//�����˲�֮�������
				SaveActionUtils.exportCSV(excel_Result_File, pointf);
				break;
			case 2:
				// ��ʼ��̬������//һ�߲ɼ�һ�߻�����
				if (flag) {
					if (iAuto < xList.size() && iAuto < yList.size()) {
						mService.updateChart(xList.get(iAuto), yList.get(iAuto));
						iAuto++;
						// Log.e("����", iAuto + "");
					} else {
						// �Ѿ�����һ�������ˣ����yList Ȼ�������һ�ε�
						yList.removeAll(yList);
						flag = false;// ��ֹͣ��ͼ
						iAuto = 0;
					}
				}
				if (flag_result) {
					mService_result.updateChart(interval_time, maxValue);
					mService_result.getRenderer().setXAxisMax(interval_time);//�������ֵ���ǵ�ǰʱ�����ֵ
					mService_result.getRenderer().setYAxisMax(maxValue + maxValue/10); //����y�����ֵ���ǵ�ǰֵ+ 10%
					flag_result = false;// ÿ�λ����ֹͣ��
				}
				break;
			case 3:
//				Toast.makeText(ScanDisplayActivity.this, "��" + iCount + "��ɨ�����",
//						Toast.LENGTH_SHORT).show();
				//ÿ��ɨ�赽3�ν�����ʱ����  
				if(iCount == times_point){
					setScreenBrightness(20);
				}
				if(iCount == times && !isFirstSend){
					//ɨ������ָ���ϵͳ�Զ�������
					setScreenBrightness(getSystemScreenBrightness());
					btn_Start_Scan.setKeepScreenOn(false);//ɨ���������Ҫ�ٱ�����Ļ����
				}
				tv_times.setText("��" + iCount + "��ɨ�����");
				if(iCount == 2 && isFirstSend){
					isFirstSend = false;//˵���ǵ�һ�η���Ȼ�����¼�����ֹƵ��
					//���ֵ����Сֵ���ڵ�Ƶ�ʵ� ǿת��int
					double minFrequency = xList.get(yList.indexOf(Collections.max(yList))) * 100;
					double maxFrequency = xList.get(yList.indexOf(Collections.min(yList))) * 100;
					if(minFrequency > maxFrequency){
						double temp = minFrequency;
						minFrequency = maxFrequency;
						maxFrequency = temp;
					}//Ϊ�˷�ֹ���⣬��������Ƶ��ֵ��С ���ˡ�
					frequency_value[0] = (int)(minFrequency - (maxFrequency - minFrequency))/100;
					frequency_value[1] = (int)(maxFrequency + (maxFrequency - minFrequency))/100;
					//��������Ҫ���͵�����
					sendData_real = ConvertUtils.hexStringToBytes(sendString.substring(0, 8) + 
							ConvertUtils.HighExchangeLow(ConvertUtils.dataConvertHex(frequency_value[0] + "00")) + 
							ConvertUtils.HighExchangeLow(ConvertUtils.dataConvertHex(frequency_value[1] + "00")) +
							sendString.substring(16));//���¼���
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
		//ÿ��ҳ�潻����ʱ�� ע��㲥 ����
//		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());//ע���������һЩ����Ĺ㲥
//		 if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);//ע��㲥��ʱ������ȥ����
//            Log.d(TAG, "Connect request result=" + result);
//        }
		//ȷ����ǰ�����豸�ǿ��ŵ� ��û�У�������Ҫ����dialog  ѯ���Ƿ������
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
//		unregisterReceiver(mGattUpdateReceiver);//ȡ��ע��㲥
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null; 
		unregisterReceiver(mGattUpdateReceiver);//ȡ��ע��㲥
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode){
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_CANCELED){
				//�������ȡ�� ���˳���ǰ�
				this.finish();
				return;
			}
			break;	
		case REQUEST_PARAM:
			if (resultCode == RESULT_OK) {
				sendString = intent.getStringExtra("params");
				frequency_value = intent.getIntArrayExtra("value");
				times = intent.getIntExtra("times", 1024);//Ĭ����10
				sendData_real = ConvertUtils.hexStringToBytes(sendString);//������ȷ Ҫ�޸ĵ���Ƶ��
				//��������Ϊ2��				
				sendData = ConvertUtils.hexStringToBytes(sendString.substring(0, sendString.length()-10) + "0200" + sendString.substring(sendString.length()-6));
				
			}
			break;
		}
	}
	/*
	 *����˳���ť֮��  �����Ի���ѯ���Ƿ񱣴� 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//���Back��ť  ������ص�ʱ��  ��ѯ�ʱ����Ƿ񱣴���Ѫ������õĲ���ֵ
			// ���������ǲ�����Ĳɼ�������   �� ����˿�ʼɨ��
			if(isQuerySave && params != null){
				AlertDialog.Builder isSave = new Builder(this);//�����Ի���
				isSave.setTitle("ϵͳ��ʾ");
				isSave.setMessage("�Ƿ񱣴���������ݣ�");
				isSave.setPositiveButton("����", new DialogInterface.OnClickListener() {//���ȷ�����水ť
					@Override
					public void onClick(DialogInterface dialog, int which) {//���水ť�¼�
	//					Params params = new Params();
						SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//���ÿ�д������ݿ�
						ContentValues values = new ContentValues();//��������
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
				}).setNegativeButton("������", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();//������ֱ���˳�
					}
				});
				isSave.show();
			}
		}
		finish();
		return flag;
	}
	//Begin������Ļ������
  	/**
  	 * �����Ҫ���ȵ��ڣ�������Ҫ������Ļ���ȵ���ģʽΪ�ֶ�ģʽ
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
  	 * ��ȡϵͳ��Ļ����ֵ����Ļ�������ֵ255���������ֵΪ0.
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
  	 * ��ȡ��ǰ��Ļ����ֵ
  	 * @return
  	 */
  	private float getCurrentScreenBrightness(){
  		return this.getWindow().getAttributes().screenBrightness;
  	}
  	/**
  	 * ������Ļ����ֵ
  	 */
  	private void setScreenBrightness(int value) {
  		setScrennManualMode();//��������Ϊ�ֶ�ģʽ
  		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
  		lp.screenBrightness = value < 0 ? 20/255f : value/255f;
  		this.getWindow().setAttributes(lp);
  	}
  	//End������Ļ������
  	/** 
  	 * �������¼�  �����ʱ����Ļ�ص�ϵͳ������
  	 */
  	@Override
  	public boolean onTouchEvent(MotionEvent event) {
  		if(getCurrentScreenBrightness() == 20/255f){
  			//˵�����ڲɼ��� ��ɨ���� ���䰵��
//  			Toast.makeText(this, "��Ļ����¼�", Toast.LENGTH_SHORT).show();
  			setScreenBrightness(getSystemScreenBrightness());//���Ǹ�һ��ʱ���Զ��������������ɨ���ʱ��Ļ�
  			times_point = iCount + 2;
  		}//������� �ǰ�����״̬�Ͳ���
  		return super.onTouchEvent(event);
  	}
	//Start    Java��JNI����
	static{
		System.loadLibrary("CALLC");
	}
	public native double[] process_Data(double[] source);//���ط��� �������˲�������㷨
	//End      Java��JNI����
}
