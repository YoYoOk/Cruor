package com.yj.cruor_testing.util;

import java.util.ArrayList;
import java.util.Vector;

import com.yj.cruor_testing.database.MyPointF;
import com.yj.cruor_testing.database.Params;

import android.graphics.PointF;

public class CalcParamsUtils {
	/**
	 * 凝血曲线参数计算
	 * @param 凝血曲线值
	 */
	public static Params<MyPointF> getTegCurveData(ArrayList<MyPointF> vecTegPoint){
		Params<MyPointF> params = new Params<MyPointF>();
		if(vecTegPoint.isEmpty())	return params;
		int numOfPoint = vecTegPoint.size();//有多少个数据点
		Vector<PointF> arcVecTegPoint = new Vector<PointF>(numOfPoint);//这里使用Vector  是为了先申请内存空间  
		arcVecTegPoint.setSize(numOfPoint);//先设置好长度
//		float[][] arcVecTegPoint = new float[numOfPoint][2];
		boolean baseFlag = true;
		boolean RValueFlag = false;
		
		double baseValue = vecTegPoint.get(0).y;
		
		arcVecTegPoint.set(0, new PointF(vecTegPoint.get(0).x, vecTegPoint.get(0).y));//赋值第一个
		double MaxValue = 0;
		if(numOfPoint <= 5){
			double sum = 0;
			for(int i = 0; i < numOfPoint; i++){
				sum += vecTegPoint.get(i).y;
			}// 求前5个点的和
			baseValue = sum/numOfPoint;//前5个点的平均值
			for(int i = 0; i < numOfPoint; i++){
				vecTegPoint.get(i).y = (float)baseValue;
			}
			return params;//求得前<5个数的均值，重新幅值给这几个数
		}
		if(numOfPoint > 2){
			for(int i = 5; i < numOfPoint - 2; i++){
				if(baseFlag){
					if(vecTegPoint.get(i+1).y > vecTegPoint.get(i).y + 0.008){
						vecTegPoint.get(i+1).y = vecTegPoint.get(i).y;//下一个值>当前值 + 0.008  将当前数赋值给下一个数   
					}
					//  当前值 - 下一个值  > 0.002   && 下一个值 - 下下一个值  > 0.002
					//且  基准值 - 下下一个值 > 0.008  && 基准值 - 下一个值 > 0.008
					//或  基准值 - 下下一个值 > 0.008  && 基准值 - 下一个值 > 0.008 
					if((((vecTegPoint.get(i).y - vecTegPoint.get(i+1).y > 0.002)&&
							(vecTegPoint.get(i+1).y - vecTegPoint.get(i+2).y > 0.002))&&
								((baseValue - vecTegPoint.get(i+2).y > 0.008)&&
									(baseValue - vecTegPoint.get(i+1).y > 0.008)))||
										((baseValue - vecTegPoint.get(i+2).y > 0.008)&&
												(baseValue - vecTegPoint.get(i+1).y > 0.008))){
						baseFlag= false;
						RValueFlag = true;
					}else{
						//当前值 - 下一个值 <= 0.002 && 下一个值 - 下下一个值  <= 0.002
						if((vecTegPoint.get(i).y - vecTegPoint.get(i+1).y <= 0.002)&&
								(vecTegPoint.get(i+1).y - vecTegPoint.get(i+2).y <= 0.002)){
							for(int j = 0; j <= i+2; j++){
								vecTegPoint.get(j).y = (float)baseValue;
								arcVecTegPoint.set(j, new PointF(vecTegPoint.get(j).x,(float)baseValue));
								//此处加一个判断    如果j是在索引中 就修改值 若没有 就添加值
							}
//							RValuePoint = vecTegPoint.get(i+1);//求出R值
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
			}//for循环结束
			
			vecTegPoint.get(numOfPoint - 1).y = vecTegPoint.get(numOfPoint - 2).y;
			arcVecTegPoint.set(numOfPoint - 1, new PointF(vecTegPoint.get(numOfPoint - 1).x,(float)(2*baseValue) - vecTegPoint.get(numOfPoint - 1).y));
		}
		
		return params;
	}
}
