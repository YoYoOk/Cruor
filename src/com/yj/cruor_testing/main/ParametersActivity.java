package com.yj.cruor_testing.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * 参数配置页面
 */
public class ParametersActivity extends Activity {
	// 最原始的值 开始频率 截止频率 步进频率 即100 120 50 的数值
	private int start_frq_original;
	private int end_frq_original;
	private int frq_interval_original;
	private String start_frq; // 开始频率 两个字节
	private String end_frq; // 截止频率 两个字节
	private String frq_interval;// 步进频率 1个字节
	private String frq_time; // 频率步进时间 1个字节
	private String dianping; // 直流电平 2个字节
	private String enlarge; // 程控放大 2个字节
	private String times; // 扫描次数 2个字节
	private String interval_time;// 多次扫描时间间隔
	private EditText et_start_frq; // 开始频率 两个字节
	private EditText et_end_frq; // 截止频率 两个字节
	private EditText et_frq_interval;// 步进频率 1个字节
	private EditText et_frq_time; // 频率步进时间 1个字节
	private EditText et_dianping; // 直流电平 2个字节
	private EditText et_enlarge; // 程控放大 2个字节
	private EditText et_times; // 扫描次数 2个字节
	private EditText et_interval_time;// 多次扫描时间间隔
	private String resultData;// 结果数据 将数据专场16进制字符串 并且两个字节的数据低字节在前 ，高字节在后
	// private StringBuilder buffer;
	private Button btn_confirm;// 确定设置 并且关闭当前活动 将数据返回
	private Button btn_cancel;//

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parametersactivity);
		init();// 将参数配置数据取出来
		// 返回的时候给上一个活动传递数据
		btn_confirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getData();// 将输入的数据取出来出来 当前是十六进制 然后需要兑换高字节和低字节
				// 置换好了 然后 先测试
				resultData = start_frq + end_frq + frq_interval + frq_time + dianping + enlarge + times + interval_time;
				resultData = "86110101" + resultData + "68";
				// 给上一个活动传数据
				Intent intent = new Intent();
				intent.putExtra("params", resultData);
				intent.putExtra("value", new int[] { start_frq_original, end_frq_original, frq_interval_original });
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	// 得到输入的数据
	protected void getData() {
		// 发送两个字节的需要转换
		start_frq_original = Integer.parseInt(et_start_frq.getText().toString().trim());
		end_frq_original = Integer.parseInt(et_end_frq.getText().toString().trim());
		frq_interval_original = Integer.parseInt(et_frq_interval.getText().toString().trim());
		start_frq = dataConvertHex(et_start_frq.getText().toString().trim() + "00"); // 开始频率
																						// 两个字节
		start_frq = HighExchangeLow(start_frq);
		end_frq = dataConvertHex(et_end_frq.getText().toString().trim() + "00"); // 截止频率
																					// 两个字节
		end_frq = HighExchangeLow(end_frq);
		frq_interval = dataConvertHex(et_frq_interval.getText().toString().trim());// 步进频率
																					// 1个字节
		frq_time = dataConvertHex(et_frq_time.getText().toString().trim()); // 频率步进时间
																			// 1个字节
		dianping = dataConvertHex(et_dianping.getText().toString().trim()); // 直流电平
																			// 2个字节
		dianping = HighExchangeLow(dianping);
		enlarge = dataConvertHex(et_enlarge.getText().toString().trim()); // 程控放大
																			// 2个字节
		enlarge = HighExchangeLow(enlarge);
		times = dataConvertHex(et_times.getText().toString().trim()); // 扫描次数
																		// 2个字节
		times = HighExchangeLow(times);
		interval_time = dataConvertHex(et_interval_time.getText().toString().trim());// 多次扫描时间间隔
		interval_time = HighExchangeLow(interval_time);
	}

	// 初始化控件
	private void init() {
		btn_confirm = (Button) findViewById(R.id.confirm_set);
		btn_cancel = (Button) findViewById(R.id.cancel_set);
		et_start_frq = (EditText) findViewById(R.id.start_frq); // 开始频率 两个字节
		et_end_frq = (EditText) findViewById(R.id.end_frq); // 截止频率 两个字节
		et_frq_interval = (EditText) findViewById(R.id.frq_interval);// 步进频率
																		// 1个字节
		et_frq_time = (EditText) findViewById(R.id.frq_time); // 频率步进时间 1个字节
		et_dianping = (EditText) findViewById(R.id.dianping); // 直流电平 2个字节
		et_enlarge = (EditText) findViewById(R.id.enlarge); // 程控放大 2个字节
		et_times = (EditText) findViewById(R.id.times); // 扫描次数 2个字节
		et_interval_time = (EditText) findViewById(R.id.interval_time);// 多次扫描时间间隔
	}

	/*
	 * 将数据转成十六进制
	 */
	public String dataConvertHex(String data) {
		String str = Long.toHexString(Long.parseLong(data)).toUpperCase();
		str = str.length() % 2 == 0 ? str : "0" + str;
		return str;
	}

	/*
	 * 将高字节转换成低字节
	 */
	public static String HighExchangeLow(String data) {
		int size = data.length();
		String str = "";
		switch (size) {
		case 2:// 即2个字节 但是目前只有输入1个字节如2次 0200
			str = data + "00";
			break;
		case 4:// 两个字节 调换高字节和低字节
			str = data.substring(2) + data.substring(0, 2);
			break;
		default:
			break;
		}
		return str;
	}
}
