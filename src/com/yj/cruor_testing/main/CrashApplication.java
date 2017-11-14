package com.yj.cruor_testing.main;

import android.app.Application;

/**
 * application��������Ӧ�ó����ȫ��״̬�������ǿ���Application��ע��δ�����쳣������
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
