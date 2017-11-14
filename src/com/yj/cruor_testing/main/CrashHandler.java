package com.yj.cruor_testing.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * UncaughtException处理类，当程序发生Uncaught异常的时候，由该类来接管程序，并记录发送错误报告。
 * @author liaoyao
 */
public class CrashHandler implements UncaughtExceptionHandler {
	
	private static final String TAG = "Cruor";
	private Thread.UncaughtExceptionHandler mDefaultHandler;//系统默认的UncaughtException处理类
	private static CrashHandler INSTANCE = new CrashHandler();//单例模式  CrashHandler实例
	private Context mContext;//程序的Context对象
	private Map<String,String> info = new HashMap<String,String>();//用来存储设备信息和异常信息
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//保证只有一个CrashHandler实例
	private CrashHandler(){}
	//获取CrashHandler实例，单例模式
	public static CrashHandler getInstance(){
		return INSTANCE;
	}
	
	/**
	 * 初始化
	 * @param context
	 */
	public void init(Context context){
		this.mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();//获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);//设置该CrashHandler为程序默认处理器，就有一点像装饰者设计模式
	}
	
	/** 
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if(!handlerException(ex) && mDefaultHandler != null){
			//如果用户没有处理，则系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		}else{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error: " , e);
			}
		}
		//退出程序
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}
	
	/**
	 * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成。
	 * @param ex
	 * @return
	 */
	public boolean handlerException(Throwable ex){
		if(ex == null){
			return false;
		}
		new Thread(){
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉，程序出现异常！", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		//手机设备参数信息
//		collectDeviceInfo(mContext);
		//保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}
	
	/**
	 * 搜集设备参数信息
	 * @param context
	 */
	public void collectDeviceInfo(Context context){
		try {
			PackageManager pm = context.getPackageManager();//获得包管理器
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);//得到该应用的信息，即主Activity
			if(pi != null){
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Field[] fields = Build.class.getDeclaredFields();//反射机制
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				info.put(field.getName(), field.get("").toString());
				Log.d(TAG, field.getName() + ":" + field.get(""));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String saveCrashInfo2File(Throwable ex){
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, String> entry : info.entrySet()){
			sb.append(entry.getKey() + "=" + entry.getValue() + "\r\n");
		}
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);  
        ex.printStackTrace(pw);  
        Throwable cause = ex.getCause();  
        // 循环着把所有的异常信息写入writer中  
        while (cause != null) {  
            cause.printStackTrace(pw);  
            cause = cause.getCause();  
        }  
        pw.close();// 记得关闭  
        String result = writer.toString();  
        sb.append(result);  
        // 保存文件  
//        long timetamp = System.currentTimeMillis();  
        String time = format.format(new Date());  
        String fileName = "crash-" + time + "-" + TAG + ".log";  
//        if (Environment.getExternalStorageState().equals(  
//                Environment.MEDIA_MOUNTED)) {  
        try {  
            File dir = new File(Environment.getExternalStorageDirectory().toString() + 
            		File.separator + "Excel" + File.separator + "Crash");  
            Log.i("CrashHandler", dir.toString());  
            if (!dir.exists())  
                dir.mkdir();  
            FileOutputStream fos = new FileOutputStream(new File(dir,  
                     fileName));  
            fos.write(sb.toString().getBytes());  
            fos.close();  
            return fileName;  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
//        }  
        return null;  
	}
	
}
