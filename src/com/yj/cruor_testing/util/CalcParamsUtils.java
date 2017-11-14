package com.yj.cruor_testing.util;

import java.util.ArrayList;
import java.util.Vector;

import com.yj.cruor_testing.database.MyPointF;
import com.yj.cruor_testing.database.Params;

import android.graphics.PointF;

public class CalcParamsUtils {
	/**
	 * ��Ѫ���߲�������
	 * @param ��Ѫ����ֵ
	 */
	public static Params<MyPointF> getTegCurveData(ArrayList<MyPointF> vecTegPoint){
		Params<MyPointF> params = new Params<MyPointF>();
		if(vecTegPoint.isEmpty())	return params;
		int numOfPoint = vecTegPoint.size();//�ж��ٸ����ݵ�
		Vector<PointF> arcVecTegPoint = new Vector<PointF>(numOfPoint);//����ʹ��Vector  ��Ϊ���������ڴ�ռ�  
		arcVecTegPoint.setSize(numOfPoint);//�����úó���
//		float[][] arcVecTegPoint = new float[numOfPoint][2];
		boolean baseFlag = true;
		boolean RValueFlag = false;
		
		double baseValue = vecTegPoint.get(0).y;
		
		arcVecTegPoint.set(0, new PointF(vecTegPoint.get(0).x, vecTegPoint.get(0).y));//��ֵ��һ��
		double MaxValue = 0;
		if(numOfPoint <= 5){
			double sum = 0;
			for(int i = 0; i < numOfPoint; i++){
				sum += vecTegPoint.get(i).y;
			}// ��ǰ5����ĺ�
			baseValue = sum/numOfPoint;//ǰ5�����ƽ��ֵ
			for(int i = 0; i < numOfPoint; i++){
				vecTegPoint.get(i).y = (float)baseValue;
			}
			return params;//���ǰ<5�����ľ�ֵ�����·�ֵ���⼸����
		}
		if(numOfPoint > 2){
			for(int i = 5; i < numOfPoint - 2; i++){
				if(baseFlag){
					if(vecTegPoint.get(i+1).y > vecTegPoint.get(i).y + 0.008){
						vecTegPoint.get(i+1).y = vecTegPoint.get(i).y;//��һ��ֵ>��ǰֵ + 0.008  ����ǰ����ֵ����һ����   
					}
					//  ��ǰֵ - ��һ��ֵ  > 0.002   && ��һ��ֵ - ����һ��ֵ  > 0.002
					//��  ��׼ֵ - ����һ��ֵ > 0.008  && ��׼ֵ - ��һ��ֵ > 0.008
					//��  ��׼ֵ - ����һ��ֵ > 0.008  && ��׼ֵ - ��һ��ֵ > 0.008 
					if((((vecTegPoint.get(i).y - vecTegPoint.get(i+1).y > 0.002)&&
							(vecTegPoint.get(i+1).y - vecTegPoint.get(i+2).y > 0.002))&&
								((baseValue - vecTegPoint.get(i+2).y > 0.008)&&
									(baseValue - vecTegPoint.get(i+1).y > 0.008)))||
										((baseValue - vecTegPoint.get(i+2).y > 0.008)&&
												(baseValue - vecTegPoint.get(i+1).y > 0.008))){
						baseFlag= false;
						RValueFlag = true;
					}else{
						//��ǰֵ - ��һ��ֵ <= 0.002 && ��һ��ֵ - ����һ��ֵ  <= 0.002
						if((vecTegPoint.get(i).y - vecTegPoint.get(i+1).y <= 0.002)&&
								(vecTegPoint.get(i+1).y - vecTegPoint.get(i+2).y <= 0.002)){
							for(int j = 0; j <= i+2; j++){
								vecTegPoint.get(j).y = (float)baseValue;
								arcVecTegPoint.set(j, new PointF(vecTegPoint.get(j).x,(float)baseValue));
								//�˴���һ���ж�    ���j���������� ���޸�ֵ ��û�� �����ֵ
							}
//							RValuePoint = vecTegPoint.get(i+1);//���Rֵ
							params.setR_value(vecTegPoint.get(i+1));
						}
					}
					
				}
				if(RValueFlag){
					if(vecTegPoint.get(i+1).y > vecTegPoint.get(i).y + 0.008){
						vecTegPoint.get(i+1).y = vecTegPoint.get(i).y;
					}
					if(arcVecTegPoint.get(i).y < baseValue + 0.05){
//						KValuePoint = vecTegPoint.get(i);
						params.setK_value(vecTegPoint.get(i));
					}
				}
				arcVecTegPoint.set(i+1, new PointF(vecTegPoint.get(i+1).x,(float)(2*baseValue) - vecTegPoint.get(i+1).y));
				if(arcVecTegPoint.get(i).y > MaxValue){
					MaxValue = arcVecTegPoint.get(i).y;
//					MAValuePoint = vecTegPoint.get(i);
					params.setMa_value(vecTegPoint.get(i));
				}
			}//forѭ������
			
			vecTegPoint.get(numOfPoint - 1).y = vecTegPoint.get(numOfPoint - 2).y;
			arcVecTegPoint.set(numOfPoint - 1, new PointF(vecTegPoint.get(numOfPoint - 1).x,(float)(2*baseValue) - vecTegPoint.get(numOfPoint - 1).y));
		}
		
		return params;
	}
}
