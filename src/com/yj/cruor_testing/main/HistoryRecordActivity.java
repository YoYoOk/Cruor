package com.yj.cruor_testing.main;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.achartengine.GraphicalView;

import com.yj.cruor_testing.util.ChartService;
import com.yj.cruor_testing.util.SaveActionUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/*
 * ��ʾ��ʷ����б� ÿ�δ���ʾ���µ���һ���������
 */
public class HistoryRecordActivity extends Activity implements OnClickListener{
	private List<String> listFileName;//�洢�ļ��б���
	private ArrayAdapter<String> listViewAdapter;//�б�������
	private File[] files;//�ļ��б�
	private List<Double> xList_time;//x�����ʾʱ��
	private List<Double> yList_value;//y�����ʾ��ֵ
	private LinearLayout mLLayout_result;//��ʾ����
	private GraphicalView mView_result;//���View
	private ChartService mChartService_result;
	private String title_result = "��Ѫ����";
	//�����Ի����б����ʽ��ʾ��ʷ��¼
	private AlertDialog.Builder builder;
	private AlertDialog alertDialog;
	private LinearLayout test_result_view;//��ʾ�����view
	//Dialog
	private Dialog dialog;
	private TextView tv_wait,tv_delete;
	private int longClickPosition;//���������position
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historyrecordactivity);
		this.getActionBar().setTitle("��ʷ�ɼ���¼");
		listFileName = new ArrayList<String>();
		saveInit();//�����һЩ��������
		//��ȡָ���ļ��е��б�
		File itemDir = new File(SaveActionUtils.getExcelDir());
		files = itemDir.listFiles();//��ȡ��ǰĿ¼�����е��ļ�
		if(files.length == 0){
			Toast.makeText(HistoryRecordActivity.this, "û����ʷ�ɼ�����", Toast.LENGTH_SHORT).show();
			this.finish();
			return;//�������һ��   ��Ȼfinish��������
		}
		//���Լ���ӵ���Ҳ������ʾ����  ֻҪ��csv�ļ�����
		for (File file : files) {
			if(file.getName().contains("Result") || !file.getName().contains("Source")){
				if(!file.getName().contains("_new") && !file.getPath().contains(".txt")){
					listFileName.add(file.getName());
				}
			}
		}
		listViewAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1
				,android.R.id.text1,listFileName);
		readCSV(SaveActionUtils.getExcelDir() + 
				File.separator + listFileName.get(listFileName.size() - 1));
		//��̬��Ӳ���������ֵ�view��
		test_result_view = (LinearLayout)findViewById(R.id.test_result_view);
		test_result_view.addView(View.inflate(HistoryRecordActivity.this, R.layout.param_result, null), 
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//����ɾ����
		dialog = new Dialog(HistoryRecordActivity.this, R.style.MyDialogStyle);
		dialog.setContentView(R.layout.dialog_deleteorset);
		tv_wait = (TextView)dialog.findViewById(R.id.tv_dialog_wait);
		tv_delete = (TextView)dialog.findViewById(R.id.tv_dialog_delete);
		tv_wait.setOnClickListener(this);
		tv_delete.setOnClickListener(this);
	}
	
	public void saveInit(){
		xList_time = new ArrayList<Double>();
		yList_value = new ArrayList<Double>();
		mLLayout_result = (LinearLayout)findViewById(R.id.ll_filelist_draw);
		mChartService_result = new ChartService(HistoryRecordActivity.this);
		mChartService_result.setXYMultipleSeriesDataset(title_result);
		mChartService_result.setXYMultipleSeriesRenderer("ʱ��", "ֵ");
		mView_result = mChartService_result.getGraphicalView();
		mLLayout_result.addView(mView_result, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	public void readCSV(String path){
		xList_time.removeAll(xList_time);
		yList_value.removeAll(yList_value);
		List<String> tempList = SaveActionUtils.importCsv(new File(path));
		for(String str : tempList){
			xList_time.add(Double.parseDouble(str.split(",")[0]));
			yList_value.add(Double.parseDouble(str.split(",")[1]));
		}
		mChartService_result.getRenderer().setXAxisMax(Collections.max(xList_time));
		mChartService_result.getRenderer().setYAxisMax(Collections.max(yList_value) + Collections.max(yList_value)/10);
		mChartService_result.getRenderer().setYAxisMin(Collections.min(yList_value) - Collections.min(yList_value)/10);
		mChartService_result.updateChart(xList_time, yList_value);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.view_history, menu);
		setIconEnable(menu, true);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.title_btn1:
			//��������΢�����Ͻǵ�Ч��
//			startActivityForResult(new Intent(HistoryRecordActivity.this, DialogActivity.class), REQUEST_CODE);
			//������  ����list�б�
			LayoutInflater inflater = (LayoutInflater)HistoryRecordActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_listitem_filename, null);
			ListView dialog_listview = (ListView)layout.findViewById(R.id.file_list_item_dialog);
			dialog_listview.setAdapter(listViewAdapter);
			dialog_listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					//�����������ִ�е����Ҫ���еĲ���.
//					Toast.makeText(HistoryRecordActivity.this, "��������" + listViewAdapter.getItem(position), Toast.LENGTH_SHORT).show();
					//ÿ�ζ�Ҫ���һ������
					if(alertDialog != null){
						alertDialog.dismiss();//�˳�
					}
					mChartService_result.clearValue();
					String fileName = listViewAdapter.getItem(position);
					readCSV(SaveActionUtils.getExcelDir() + 
							File.separator + fileName);
				}
			});
			dialog_listview.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					//��������¼�
					Toast.makeText(HistoryRecordActivity.this, "�����Ľ��", Toast.LENGTH_SHORT).show();
					
					int[] location = new int[2];
					// ��ȡ��ǰview����Ļ�еľ���λ��
					// ,location[0]��ʾview��x����ֵ,location[1]��ʾview������ֵ
					view.getLocationOnScreen(location);
					longClickPosition = position;
					DisplayMetrics displayMetrics = new DisplayMetrics();
					Display display = HistoryRecordActivity.this.getWindowManager().getDefaultDisplay();
					display.getMetrics(displayMetrics);
					WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
					params.gravity = Gravity.BOTTOM;
//					params.y =display.getHeight() -  location[1];//getHeight�ѷ���
					Point size = new Point();
					display.getSize(size);
					params.y = size.y - location[1];
					dialog.getWindow().setAttributes(params);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
					return true;//����true������֮�� �Ի�����Ȼ����
				}
			});
			builder = new AlertDialog.Builder(HistoryRecordActivity.this);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.show();
			break;
		case R.id.title_btn3:
			//��ѯ���ݿ���ʾ���м�¼���б��� Ŀǰ����ʾ������˵ Log��ӡ
			SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//���ÿ�д������ݿ�
			Cursor cursor = db.query("Params", null, null, null, null, null, null);
			//��ѯ �������е�����  ��Ŀǰָ��ָ����е����ݵ�һ��
			if(cursor.moveToFirst()){
				do {
					float r_Value = cursor.getFloat(cursor.getColumnIndex("r_value"));
					float k_Value = cursor.getFloat(cursor.getColumnIndex("k_value"));
					float angel = cursor.getFloat(cursor.getColumnIndex("angel"));
					float ma_Value = cursor.getFloat(cursor.getColumnIndex("ma_value"));
					String date = cursor.getString(cursor.getColumnIndex("date"));
					Log.e("��������", r_Value + "-" + k_Value + "-" + angel + "-" + ma_Value + "-" + date);
				} while (cursor.moveToNext());
			}
			cursor.close();
			break;
		}
		return true;
	}
	
	 //enableΪtrueʱ���˵����ͼ����Ч��enableΪfalseʱ��Ч��4.0ϵͳĬ����Ч  
    private void setIconEnable(Menu menu, boolean enable)  
    {  
        try   
        {  
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");  
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);  
            m.setAccessible(true);  
            //MenuBuilderʵ��Menu�ӿڣ������˵�ʱ����������menu��ʵ����MenuBuilder����(java�Ķ�̬����)  
            m.invoke(menu, enable);  
              
        } catch (Exception e)   
        {  
            e.printStackTrace();  
        }  
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tv_dialog_wait:
			
			break;
		case R.id.tv_dialog_delete:
			dialog.dismiss();//Ȼ�󽫴˶Ի���ر�
			//Ȼ����ļ���ɾ�����ļ� ----��ɾ������ǰ��ʱ���  Resource�ļ�---ɾ�����ݿ����������   ����ʱ��
			String fileName = listFileName.get(longClickPosition);
			String filePath  = SaveActionUtils.getExcelDir() + "/" + fileName;
			boolean deleteFile = deleteFile(filePath);//ɾ��Result�ļ�
			filePath = SaveActionUtils.getExcelDir() + "/" + fileName.substring(0, fileName.indexOf("_") + 1) + "Source.csv";
			deleteFile = deleteFile(filePath);
			//���ݵ�ǰʱ�� ɾ�� ���ݿ������    //��ѯ���ݿ���ʾ���м�¼���б��� Ŀǰ����ʾ������˵ Log��ӡ
			SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//���ÿ�д������ݿ�
			int result = db.delete("Params", "date=?", new String[]{fileName.substring(0, fileName.indexOf("_"))});
			result = db.delete("Params", "date=?", new String[]{"2017-06-19 16:42:54"});
			Log.e("ɾ�����", result + "");
			listFileName.remove(longClickPosition);//���б���ɾ�� 
			listViewAdapter.notifyDataSetChanged();//Ȼ��ˢ���б�
			//Ȼ����ļ��и��ݵ�ǰѡ�е��ļ���   ɾ����Ӧ���ļ�
			break;
		}
	}
	
	/**
     * ɾ�������ļ�
     * @param   filePath    ��ɾ���ļ����ļ���
     * @return �ļ�ɾ���ɹ�����true�����򷵻�false
     */
    public boolean deleteFile(String filePath) {
    	File file = new File(filePath);
        if (file.isFile() && file.exists()) {
        return file.delete();
        }
        return false;
    }

}
