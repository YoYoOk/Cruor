package com.yj.cruor_testing.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * ��������ҳ��
 */
public class ParametersActivity extends Activity {
	// ��ԭʼ��ֵ ��ʼƵ�� ��ֹƵ�� ����Ƶ�� ��100 120 50 ����ֵ
	private int start_frq_original;
	private int end_frq_original;
	private int frq_interval_original;
	private String start_frq; // ��ʼƵ�� �����ֽ�
	private String end_frq; // ��ֹƵ�� �����ֽ�
	private String frq_interval;// ����Ƶ�� 1���ֽ�
	private String frq_time; // Ƶ�ʲ���ʱ�� 1���ֽ�
	private String dianping; // ֱ����ƽ 2���ֽ�
	private String enlarge; // �̿طŴ� 2���ֽ�
	private String times; // ɨ����� 2���ֽ�
	private String interval_time;// ���ɨ��ʱ����
	private EditText et_start_frq; // ��ʼƵ�� �����ֽ�
	private EditText et_end_frq; // ��ֹƵ�� �����ֽ�
	private EditText et_frq_interval;// ����Ƶ�� 1���ֽ�
	private EditText et_frq_time; // Ƶ�ʲ���ʱ�� 1���ֽ�
	private EditText et_dianping; // ֱ����ƽ 2���ֽ�
	private EditText et_enlarge; // �̿طŴ� 2���ֽ�
	private EditText et_times; // ɨ����� 2���ֽ�
	private EditText et_interval_time;// ���ɨ��ʱ����
	private String resultData;// ������� ������ר��16�����ַ��� ���������ֽڵ����ݵ��ֽ���ǰ �����ֽ��ں�
	// private StringBuilder buffer;
	private Button btn_confirm;// ȷ������ ���ҹرյ�ǰ� �����ݷ���
	private Button btn_cancel;//

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parametersactivity);
		init();// ��������������ȡ����
		// ���ص�ʱ�����һ�����������
		btn_confirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getData();// �����������ȡ�������� ��ǰ��ʮ������ Ȼ����Ҫ�һ����ֽں͵��ֽ�
				// �û����� Ȼ�� �Ȳ���
				resultData = start_frq + end_frq + frq_interval + frq_time + dianping + enlarge + times + interval_time;
				resultData = "86110101" + resultData + "68";
				// ����һ���������
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

	// �õ����������
	protected void getData() {
		// ���������ֽڵ���Ҫת��
		start_frq_original = Integer.parseInt(et_start_frq.getText().toString().trim());
		end_frq_original = Integer.parseInt(et_end_frq.getText().toString().trim());
		frq_interval_original = Integer.parseInt(et_frq_interval.getText().toString().trim());
		start_frq = dataConvertHex(et_start_frq.getText().toString().trim() + "00"); // ��ʼƵ��
																						// �����ֽ�
		start_frq = HighExchangeLow(start_frq);
		end_frq = dataConvertHex(et_end_frq.getText().toString().trim() + "00"); // ��ֹƵ��
																					// �����ֽ�
		end_frq = HighExchangeLow(end_frq);
		frq_interval = dataConvertHex(et_frq_interval.getText().toString().trim());// ����Ƶ��
																					// 1���ֽ�
		frq_time = dataConvertHex(et_frq_time.getText().toString().trim()); // Ƶ�ʲ���ʱ��
																			// 1���ֽ�
		dianping = dataConvertHex(et_dianping.getText().toString().trim()); // ֱ����ƽ
																			// 2���ֽ�
		dianping = HighExchangeLow(dianping);
		enlarge = dataConvertHex(et_enlarge.getText().toString().trim()); // �̿طŴ�
																			// 2���ֽ�
		enlarge = HighExchangeLow(enlarge);
		times = dataConvertHex(et_times.getText().toString().trim()); // ɨ�����
																		// 2���ֽ�
		times = HighExchangeLow(times);
		interval_time = dataConvertHex(et_interval_time.getText().toString().trim());// ���ɨ��ʱ����
		interval_time = HighExchangeLow(interval_time);
	}

	// ��ʼ���ؼ�
	private void init() {
		btn_confirm = (Button) findViewById(R.id.confirm_set);
		btn_cancel = (Button) findViewById(R.id.cancel_set);
		et_start_frq = (EditText) findViewById(R.id.start_frq); // ��ʼƵ�� �����ֽ�
		et_end_frq = (EditText) findViewById(R.id.end_frq); // ��ֹƵ�� �����ֽ�
		et_frq_interval = (EditText) findViewById(R.id.frq_interval);// ����Ƶ��
																		// 1���ֽ�
		et_frq_time = (EditText) findViewById(R.id.frq_time); // Ƶ�ʲ���ʱ�� 1���ֽ�
		et_dianping = (EditText) findViewById(R.id.dianping); // ֱ����ƽ 2���ֽ�
		et_enlarge = (EditText) findViewById(R.id.enlarge); // �̿طŴ� 2���ֽ�
		et_times = (EditText) findViewById(R.id.times); // ɨ����� 2���ֽ�
		et_interval_time = (EditText) findViewById(R.id.interval_time);// ���ɨ��ʱ����
	}

	/*
	 * ������ת��ʮ������
	 */
	public String dataConvertHex(String data) {
		String str = Long.toHexString(Long.parseLong(data)).toUpperCase();
		str = str.length() % 2 == 0 ? str : "0" + str;
		return str;
	}

	/*
	 * �����ֽ�ת���ɵ��ֽ�
	 */
	public static String HighExchangeLow(String data) {
		int size = data.length();
		String str = "";
		switch (size) {
		case 2:// ��2���ֽ� ����Ŀǰֻ������1���ֽ���2�� 0200
			str = data + "00";
			break;
		case 4:// �����ֽ� �������ֽں͵��ֽ�
			str = data.substring(2) + data.substring(0, 2);
			break;
		default:
			break;
		}
		return str;
	}
}
