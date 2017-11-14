package com.yj.cruor_testing.database;


/**
 * @author liaoyao
 * 自写一个point类   目的的是为了能够在使用JNI的时候   能够使用  备用
 */
public class MyPointF{
	public float x;
    public float y;
    
    public MyPointF() {
    	super();
    }

    public MyPointF(float x, float y) {
    	super();
        this.x = x;
        this.y = y; 
    }

	@Override
	public String toString() {
		return "MyPointF [x=" + x + ", y=" + y + "]";
	}
    
}
