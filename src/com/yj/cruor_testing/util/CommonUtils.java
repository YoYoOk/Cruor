package com.yj.cruor_testing.util;

import java.util.Random;

public class CommonUtils {
	
	/*public static double nextDouble(final double min, final double max) throws Exception {
        if (max < min) {
            throw new Exception("min < max");
        }
        if (min == max) {
            return min;
        }
        return min + ((max - min) * new Random().nextDouble());
    }*/
	
	public static double nextDouble(final double min, final double max){
        return min + ((max - min) * new Random().nextDouble());
    }
}
