package com.yj.cruor_testing.database;

/*
 * 参数类 添加泛型  目的是为了  和c++数据映射  Ljava/lang/Ojbect
 */
public class Params<T> {
	private T r_value;
	private T k_value;
	private T angle;
	private T ma_value;
	
	public Params() {
		super();
	}

	public Params(T r_value, T k_value, T angle, T ma_value) {
		super();
		this.r_value = r_value;
		this.k_value = k_value;
		this.angle = angle;
		this.ma_value = ma_value;
	}

	public T getR_value() {
		return r_value;
	}

	public void setR_value(T r_value) {
		this.r_value = r_value;
	}

	public T getK_value() {
		return k_value;
	}

	public void setK_value(T k_value) {
		this.k_value = k_value;
	}

	public T getAngle() {
		return angle;
	}

	public void setAngle(T angle) {
		this.angle = angle;
	}

	public T getMa_value() {
		return ma_value;
	}

	public void setMa_value(T ma_value) {
		this.ma_value = ma_value;
	}

	@Override
	public String toString() {
		return "Params [r_value=" + r_value + ", k_value=" + k_value + ", angle=" + angle + ", ma_value=" + ma_value
				+ "]";
	}
	
	
}
