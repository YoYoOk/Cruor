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
 * 显示历史检测列表 每次打开显示最新的那一条结果数据
 */
public class HistoryRecordActivity extends Activity implements OnClickListener{
	private List<String> listFileName;//存储文件列表名
	private ArrayAdapter<String> listViewAdapter;//列表适配器
	private File[] files;//文件列表
	private List<Double> xList_time;//x横轴表示时间
	private List<Double> yList_value;//y纵轴表示数值
	private LinearLayout mLLayout_result;//显示坐标
	private GraphicalView mView_result;//结果View
	private ChartService mChartService_result;
	private String title_result = "凝血曲线";
	//弹出对话框列表的形式显示历史记录
	private AlertDialog.Builder builder;
	private AlertDialog alertDialog;
	private LinearLayout test_result_view;//显示结果的view
	//Dialog
	private Dialog dialog;
	private TextView tv_wait,tv_delete;
	private int longClickPosition;//长按点击的position
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historyrecordactivity);
		this.getActionBar().setTitle("历史采集记录");
		listFileName = new ArrayList<String>();
		saveInit();//保存的一些参数定义
		//读取指定文件夹的列表
		File itemDir = new File(SaveActionUtils.getExcelDir());
		files = itemDir.listFiles();//获取当前目录下所有的文件
		if(files.length == 0){
			Toast.makeText(HistoryRecordActivity.this, "没有历史采集数据", Toast.LENGTH_SHORT).show();
			this.finish();
			return;//必须加这一句   不然finish不起作用
		}
		//将自己添加到的也可以显示出来  只要是csv文件即可
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
		//动态添加参数结果布局到view中
		test_result_view = (LinearLayout)findViewById(R.id.test_result_view);
		test_result_view.addView(View.inflate(HistoryRecordActivity.this, R.layout.param_result, null), 
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//弹出删除框
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
		mChartService_result.setXYMultipleSeriesRenderer("时间", "值");
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
			//弹出类似微信有上角的效果
//			startActivityForResult(new Intent(HistoryRecordActivity.this, DialogActivity.class), REQUEST_CODE);
			//弹出层  弹出list列表
			LayoutInflater inflater = (LayoutInflater)HistoryRecordActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_listitem_filename, null);
			ListView dialog_listview = (ListView)layout.findViewById(R.id.file_list_item_dialog);
			dialog_listview.setAdapter(listViewAdapter);
			dialog_listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					//在这里面就是执行点击后要进行的操作.
//					Toast.makeText(HistoryRecordActivity.this, "你点击的是" + listViewAdapter.getItem(position), Toast.LENGTH_SHORT).show();
					//每次都要清空一次数据
					if(alertDialog != null){
						alertDialog.dismiss();//退出
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
					//长按点击事件
					Toast.makeText(HistoryRecordActivity.this, "长按的结果", Toast.LENGTH_SHORT).show();
					
					int[] location = new int[2];
					// 获取当前view在屏幕中的绝对位置
					// ,location[0]表示view的x坐标值,location[1]表示view的坐标值
					view.getLocationOnScreen(location);
					longClickPosition = position;
					DisplayMetrics displayMetrics = new DisplayMetrics();
					Display display = HistoryRecordActivity.this.getWindowManager().getDefaultDisplay();
					display.getMetrics(displayMetrics);
					WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
					params.gravity = Gravity.BOTTOM;
//					params.y =display.getHeight() -  location[1];//getHeight已废弃
					Point size = new Point();
					display.getSize(size);
					params.y = size.y - location[1];
					dialog.getWindow().setAttributes(params);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
					return true;//返回true代表长按之后 对话框依然还在
				}
			});
			builder = new AlertDialog.Builder(HistoryRecordActivity.this);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.show();
			break;
		case R.id.title_btn3:
			//查询数据库显示所有记录在列表中 目前先显示出来再说 Log打印
			SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//调用可写入的数据库
			Cursor cursor = db.query("Params", null, null, null, null, null, null);
			//查询 表中所有的数据  将目前指针指向表中的数据第一行
			if(cursor.moveToFirst()){
				do {
					float r_Value = cursor.getFloat(cursor.getColumnIndex("r_value"));
					float k_Value = cursor.getFloat(cursor.getColumnIndex("k_value"));
					float angel = cursor.getFloat(cursor.getColumnIndex("angel"));
					float ma_Value = cursor.getFloat(cursor.getColumnIndex("ma_value"));
					String date = cursor.getString(cursor.getColumnIndex("date"));
					Log.e("哈哈哈哈", r_Value + "-" + k_Value + "-" + angel + "-" + ma_Value + "-" + date);
				} while (cursor.moveToNext());
			}
			cursor.close();
			break;
		}
		return true;
	}
	
	 //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效  
    private void setIconEnable(Menu menu, boolean enable)  
    {  
        try   
        {  
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");  
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);  
            m.setAccessible(true);  
            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)  
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
			dialog.dismiss();//然后将此对话框关闭
			//然后从文件中删除该文件 ----再删除包含前面时间的  Resource文件---删除数据库里面的数据   根据时间
			String fileName = listFileName.get(longClickPosition);
			String filePath  = SaveActionUtils.getExcelDir() + "/" + fileName;
			boolean deleteFile = deleteFile(filePath);//删除Result文件
			filePath = SaveActionUtils.getExcelDir() + "/" + fileName.substring(0, fileName.indexOf("_") + 1) + "Source.csv";
			deleteFile = deleteFile(filePath);
			//根据当前时间 删除 数据库里面的    //查询数据库显示所有记录在列表中 目前先显示出来再说 Log打印
			SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();//调用可写入的数据库
			int result = db.delete("Params", "date=?", new String[]{fileName.substring(0, fileName.indexOf("_"))});
			result = db.delete("Params", "date=?", new String[]{"2017-06-19 16:42:54"});
			Log.e("删除结果", result + "");
			listFileName.remove(longClickPosition);//从列表中删除 
			listViewAdapter.notifyDataSetChanged();//然后刷新列表
			//然后从文件中根据当前选中的文件名   删除对应的文件
			break;
		}
	}
	
	/**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
    	File file = new File(filePath);
        if (file.isFile() && file.exists()) {
        return file.delete();
        }
        return false;
    }

}
