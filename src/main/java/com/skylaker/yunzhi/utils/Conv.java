package com.skylaker.yunzhi.utils;

import org.apache.log4j.Logger;

/**
 * 数值类型转换的工具类，避免使用强制类型转换带来的未知错误 当转换溢出时，直接抛出异常
 */
public class Conv {
	
	public static Logger logger = Logger.getLogger(Conv.class);
	
	public static long toLong(int v) {
		return (long)v;
	}
	public static long toLong(short v) {
		return (long)v;
	}
	
	public static long toLong(byte v) {
		return (long)v;
	}
	
	public static long toLong(float v) {
		
		if(!(v >= Long.MIN_VALUE && v<= Long.MAX_VALUE)) {
			throw new RuntimeException("Conv Float to Long overflow! Float=" + v);
		}
		return (long)v;
	}
	
	public static long toLong(double v) {
		
		if(!(v >= Long.MIN_VALUE && v<= Long.MAX_VALUE)) {
			throw new RuntimeException("Conv Double to Long overflow! Float=" + v);
		}
		return (long)v;
	}
	
	
	public static int toInt(long v) {
		
		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("Conv Long to Int overflow! Long=" + v);
		}
		return (int)v;
	}
	
	public static int toInt(short v) {
		return (int)v;
	}
	
	public static int toInt(byte v) {
		return (int)v;
	}
	
	public static int toInt(float v) {
		
		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("Conv Float to Int overflow! Float=" + v);
		}
		return (int)v;
	}
	
	public static int toInt(double v) {
		
		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("Conv Double to Int overflow! Double=" + v);
		}
		return (int)v;
	}
	
	public static short toShort(int v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("Conv Int to Short overflow! Int=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(long v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("Conv Long to Short overflow! Long=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(float v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("Conv Float to Short overflow! Float=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(byte v) {
		return (short)v;
	}
	
	public static byte toByte(int v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("Conv Int to Byte overflow! Int=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(long v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("Conv Long to Byte overflow! Long=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(float v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("Conv Float to Byte overflow! Float=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(short v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("Conv Short to Byte overflow! Short=" + v);
		}
		return (byte)v;
	}
	
	public static float toFloat(double v) {
		
		if(!(v >= -Float.MAX_VALUE && v <= Float.MAX_VALUE)) {
			throw new RuntimeException("Conv Double to Float overflow! Double=" + v);
		}
		return (float)v;
	}
}
