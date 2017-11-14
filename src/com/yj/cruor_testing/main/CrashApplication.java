package com.yj.cruor_testing.main;

import android.app.Application;

/**
 * application用来管理应用程序的全局状态。定义加强版的Application中注册未捕获异常处理器
 * @author liaoyao
 *
 */
public class CrashApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
	}
	
}
