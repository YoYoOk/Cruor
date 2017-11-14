package com.yj.cruor_testing.util;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


import android.content.Context;
import android.graphics.Color;

/*
 * achartengine画图工具类
 */
public class ChartService {
	private GraphicalView mGraphicalView;
	private XYMultipleSeriesDataset dataset;// 数据集容器
	private XYMultipleSeriesRenderer renderer;// 渲染集
	private XYSeries mSeries;//单条曲线数据集
	private XYSeriesRenderer mRender;//单条曲线渲染
	private Context context;
//	private int count = 0;
	
	public ChartService(Context context) {
		this.context = context;
	}

	// 获取图表
	public GraphicalView getGraphicalView() {
		this.mGraphicalView = ChartFactory.getLineChartView(context, dataset, renderer);
		return mGraphicalView;
	}

	// 获取数据集 及xy坐标的集合
	public void setXYMultipleSeriesDataset(String curveTitle) {
		this.dataset = new XYMultipleSeriesDataset();
		mSeries = new XYSeries(curveTitle);
		dataset.addSeries(mSeries);
	}

	public void setXYMultipleSeriesRenderer(double maxX, double minX, String xTitle, String yTitle) {
		renderer = new XYMultipleSeriesRenderer();
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		// 只设置x轴的范围
		renderer.setXAxisMax(maxX);
		renderer.setXAxisMin(minX);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setLabelsTextSize(20);// 设置数轴刻度字体大小
		renderer.setLabelsColor(Color.RED);
		renderer.setPointSize(0.4f);// 曲线描点尺寸
//		renderer.setZoomButtonsVisible(true);
		renderer.setBackgroundColor(Color.parseColor("#00000000"));// 背景色
		renderer.setApplyBackgroundColor(true);
		renderer.setMarginsColor(Color.parseColor("#2688AF"));// 边距背景色，默认背景色为黑色，这里修改为白色
		mRender = new XYSeriesRenderer();
		renderer.addSeriesRenderer(mRender);
	}

	public void setXYMultipleSeriesRenderer(String xTitle, String yTitle) {
		renderer = new XYMultipleSeriesRenderer();
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		// 只设置x轴的范围
		// renderer.setXAxisMax(maxX);
		// renderer.setXAxisMin(minX);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setLabelsTextSize(20);// 设置数轴刻度字体大小
		renderer.setLabelsColor(Color.RED);
		renderer.setPointSize(0.4f);// 曲线描点尺寸
//		renderer.setZoomButtonsVisible(true);
		renderer.setBackgroundColor(Color.parseColor("#00000000"));// 背景色
		renderer.setApplyBackgroundColor(true);
		renderer.setMarginsColor(Color.parseColor("#2688AF"));// 边距背景色，默认背景色为黑色，这里修改为白色
		mRender = new XYSeriesRenderer();
		renderer.addSeriesRenderer(mRender);
	}
	// 根据新加的数据，更新曲线，只能运行在主线程
	public void updateChart(double x, double y) {
		// 如果count=0 的情况下是要先把之前的数据清除
//		this.dataset.getSeriesAt(count).add(x, y);
		mSeries.add(x, y);
		this.mGraphicalView.repaint();

	}
	//新加全部的数据
	public void updateChart(List<Double> xList,List<Double> yList){
		for(int i = 0; i < xList.size(); i++){
			mSeries.add(xList.get(i), yList.get(i));
		}
		this.mGraphicalView.repaint();
	}
	//还是设置一个横坐标最值
	public void updateRender(double maxX, double minX){
		if(this.renderer.getXAxisMax() != maxX){
			this.renderer.setXAxisMax(maxX);
		}
		if(this.renderer.getXAxisMin() != minX){
			this.renderer.setXAxisMin(minX);
		}
		this.mGraphicalView.repaint();
	}
	
	
	// 每添加一条曲线都应该添加 一个XYSeries 和 XYSeriesRenderer
//	public void updateChart() {
//		this.dataset.addSeries(new XYSeries(count + 1 + ""));
//		this.renderer.addSeriesRenderer(new XYSeriesRenderer());
//	}

	// 只画一条曲线的时候 每次都要更新title 然后清除数据
//	public void clearValue(String title) {
//		this.dataset.getSeriesAt(count).clear();
//		this.dataset.getSeriesAt(count).setTitle(title);
//		this.mGraphicalView.repaint();
//	}
	public void clearValue(){
		this.mSeries.clearSeriesValues();//清空数据
		this.mGraphicalView.repaint();
	}
	
//	public int getCount() {
//		return count;
//	}
//
//	public void setCount(int count) {
//		this.count = count;
//	}

	public XYMultipleSeriesDataset getDataset() {
		return dataset;
	}

	public void setDataset(XYMultipleSeriesDataset dataset) {
		this.dataset = dataset;
	}

	public XYMultipleSeriesRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(XYMultipleSeriesRenderer renderer) {
		this.renderer = renderer;
	}

	public XYSeries getmSeries() {
		return mSeries;
	}

	public void setmSeries(XYSeries mSeries) {
		this.mSeries = mSeries;
	}

	public XYSeriesRenderer getmRender() {
		return mRender;
	}

	public void setmRender(XYSeriesRenderer mRender) {
		this.mRender = mRender;
	}
}
