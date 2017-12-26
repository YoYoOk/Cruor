package com.yj.cruor_testing.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.yj.cruor_testing.database.MyPointF;

import android.os.Environment;
import android.util.Log;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/*
 * ���ݵ��뵼��excel  ����csv����  csv������excel����
 */
public class SaveActionUtils {
	// ��ȡExcel�ļ���
	public static String getExcelDir() {
		// SD��ָ���ļ���//�õ�����/mnt/sdcard/ ��SD���ĸ�Ŀ¼
		String sdcardPath = Environment.getExternalStorageDirectory().toString();
		// File.separator ��ϵͳ�йص�Ĭ�Ϸָ���
		File dir = new File(sdcardPath + File.separator + "Excel" + File.separator + "Scan");

		if (dir.exists()) {
			return dir.toString();
			
		} else {
			// �����˳���·����ָ����Ŀ¼���������б��赫�����ڵĸ�Ŀ¼��
			dir.mkdirs();
			Log.d("BAG", "����·��������,");
			return dir.toString();
		}
	}

	// ����һ��Sheet��
	public static void createExcel(File file) {
		WritableWorkbook wwb;// excel����������
		WritableSheet ws = null;
		try {
			if (!file.exists()) {
				wwb = Workbook.createWorkbook(file);
				// ���һ���±� ���µ��������ǵ�ǰSheet�ĸ���
				String sheetName = wwb.getNumberOfSheets() + 1 + "";
				ws = wwb.createSheet("sheet" + sheetName, wwb.getNumberOfSheets());
				// ���ڴ���д���ļ���
				wwb.write();
				wwb.close();
			} else {
				// ������ڵĻ����½�һ��Sheet��
				// �����ɶ���Excel����������
				Workbook oldWwb = Workbook.getWorkbook(file);
				// ������д���Excel����������
				wwb = Workbook.createWorkbook(file, oldWwb);
				// ���һ���±� ���µ��������ǵ�ǰSheet�ĸ���
				String sheetName = wwb.getNumberOfSheets() + 1 + "";
				ws = wwb.createSheet("sheet" + sheetName, wwb.getNumberOfSheets());
				// ���ڴ���д���ļ���
				wwb.write();
				wwb.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//ʹ��excel��������̫��ʱ  Ч�ʵ������Ǳ�������ݵ�ʱ��һֱ�򿪹ر��� ���º�ʱ���� 
	public static void writeToExcel(File file,List list) {
		WritableWorkbook wwb;// excel����������
		try {
			// �����ɶ���Excel����������
			Workbook oldWwb = Workbook.getWorkbook(file);
			// ������д���Excel����������
			wwb = Workbook.createWorkbook(file, oldWwb);
			// ��ȡ���µ�һ�ű�
			WritableSheet ws = wwb.getSheet(wwb.getNumberOfSheets() - 1);
			// ��ǰ���� ��ǰ���� ����Զ�����������һ������
			int colums = ws.getColumns();
//			Log.e("��ǰָ�������", colums + "");
//			Log.e("y����������ִ�е�����û��", list.get(5) + "");
			for (int i = 0; i < list.size(); i++) {
				Label labelN = new Label(colums, i, list.get(i) + "");
				ws.addCell(labelN);
			}
			// ���ڴ���д���ļ���,ֻ��ˢһ��.
			wwb.write();	
			wwb.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void exportCSV(File file,List list){
		//�ֽ�list������ת����String�ĸ�ʽ
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < list.size() - 1; i++){
			sb.append(list.get(i) + ",");
		}
		sb.append(list.get(list.size() - 1));
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			out = new FileOutputStream(file,true);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			bw.append(sb.toString()).append("\r");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
		}
	}
	public static void exportCSV(File file, MyPointF pointf){
		//�ֽ�list������ת����String�ĸ�ʽ
		String str = pointf.x + "," + pointf.y;
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			out = new FileOutputStream(file,true);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			bw.append(str).append("\r");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
		}
	}
	
	  /* ����
     * 
     * @param file csv�ļ�(·��+�ļ�)
     * @return
     */
    public static List<String> importCsv(File file){
        List<String> dataList=new ArrayList<String>();
        
        BufferedReader br=null;
        try { 
            br = new BufferedReader(new FileReader(file));
            String line = ""; 
            while ((line = br.readLine()) != null) { 
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return dataList;
    }
    
    /**
     * ɾ�������ļ�
     * @param   filePath    ��ɾ���ļ����ļ���
     * @return �ļ�ɾ���ɹ�����true�����򷵻�false
     */
    public static boolean deleteFile(String filePath) {
    	File file = new File(filePath);
        if (file.isFile() && file.exists()) {
        return file.delete();
        }
        return false;
    }
}
