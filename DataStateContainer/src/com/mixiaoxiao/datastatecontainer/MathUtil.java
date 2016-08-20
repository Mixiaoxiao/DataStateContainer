package com.mixiaoxiao.datastatecontainer;

public class MathUtil {
	
	public static int limitInt(int data, int min , int max){
		if(data < min){
			return min;
		}else if(data > max){
			return max;
		}
		return data;
	}

}
