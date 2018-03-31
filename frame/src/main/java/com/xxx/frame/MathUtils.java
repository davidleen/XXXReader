package com.xxx.frame;

import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Pattern;

import android.text.TextUtils;



public class MathUtils{
	
	public static int getRandomNumber(int min, int max){
		return new Random().nextInt(max-min+1)+min;
	}
	
    /**
     * 判断字符串是否都为数字
     * 
     */
	public static boolean isNumeric(String str){
		boolean isNumeric=false;

		if(!TextUtils.isEmpty(str)){
		    Pattern pattern = Pattern.compile("[0-9]+"); 
		    isNumeric=pattern.matcher(str).matches();    
		}
		
		return isNumeric;
	}
	
	public static short valueOfOctalShort(String strShort){
		return valueOfOctalShort(strShort, (short)0);
	}
	
	public static short valueOfOctalShort(String strShort, short defValue){
		return valueOfNumber(strShort, defValue, 8).shortValue();
	}
	
	public static int valueOfOctalInteger(String strInt){
		return valueOfOctalInteger(strInt, 00);
	}
	
	public static int valueOfOctalInteger(String strInt, int defValue){
		return valueOfNumber(strInt, defValue, 8).intValue();
	}
	
	public static long valueOfOctalLong(String strLong){
		return valueOfOctalLong(strLong, 00l);
	}
	
	public static long valueOfOctalLong(String strLong, long defValue){
		return valueOfNumber(strLong, defValue, 8).longValue();
	}
	
	public static short valueOfHexShort(String strShort){
		return valueOfHexShort(strShort, (short)0);
	}
	
	public static short valueOfHexShort(String strShort, short defValue){
		return valueOfNumber(strShort, defValue, 16).shortValue();
	}
	
	public static int valueOfHexInteger(String strInt){
		return valueOfHexInteger(strInt, 0x0);
	}
	
	public static int valueOfHexInteger(String strInt, int defValue){
		return valueOfNumber(strInt, defValue, 16).intValue();
	}
	
	public static long valueOfHexLong(String strLong){
		return valueOfHexLong(strLong, 0x0l);
	}
	
	public static long valueOfHexLong(String strLong, long defValue){
		return valueOfNumber(strLong, defValue, 16).longValue();
	}
	
	public static int valueOfInteger(String strInt){
		return valueOfInteger(strInt, 0);
	}
	
	public static int valueOfInteger(String strInt, int defValue) {
		int value = defValue;

		if (!TextUtils.isEmpty(strInt)) {
			try {
				value = Integer.parseInt(strInt);
			} catch (Exception e) {
				Log.d(e);
			}
		}

		return value;
	}
	
	public static double valueOfDouble(String strDou){
		return valueOfDouble(strDou, 0);
	}
	
	public static double valueOfDouble(String strDou, double defValue){
		double value = defValue;
		
		if(!TextUtils.isEmpty(strDou)){
			try {
				value = Double.parseDouble(strDou);
			} catch (Exception e) {
				Log.d(e);
			}
		}
		
		return value;
	}
	
	public static float valueOfFloat(String strFloat){
		return valueOfFloat(strFloat, 0.0f);
	}
	
	public static float valueOfFloat(String strFloat, float defValue){
		float value = defValue;
		
		if(!TextUtils.isEmpty(strFloat)){
			try {
				value = Float.parseFloat(strFloat);
			} catch (Exception e) {
				Log.d(e);
			}
		}
		
		return value;
	}
	
	public static long valueOfLong(String strLong){
		return valueOfLong(strLong, 0l);
	}
	
	public static long valueOfLong(String strLong, long defValue) {
		long value = defValue;
		if (!TextUtils.isEmpty(strLong)) {
			try {
				value = Long.parseLong(strLong);
			} catch (Exception e) {
				Log.d(e);
			}
		}

		return value;
	}
	
	public static short valueOfShort(String strShort){
		return valueOfShort(strShort, (short)0);
	}
	
	public static short valueOfShort(String strShort, short defValue){
		return valueOfNumber(strShort, defValue, 10).shortValue();
	}
	
	private static Number valueOfNumber(String num, Number def, int radix){
		Number value=def;

		if(def!=null){
			try{
				if(radix > Character.MIN_RADIX && radix < Character.MAX_RADIX && num != null){
					if(radix==16 && num.startsWith("0x") || num.startsWith("0X")){
						num=num.substring(2);
					}else if(radix==8 && num.startsWith("0")){
						num=num.substring(1);
					}
					
					value=new BigInteger(num, radix);
				}
			}catch(Exception e){
				Log.e(e);
			}
		}else{
			throw new NumberFormatException();
		}
		
		return value;
	}


	/**
	 * 获取精确的图片压缩比例值，
	 * bitmapfractory decode 中用到
	 * @param scaleSize
	 * @return
	 */
	public static int getAccurateSampleSize(int scaleSize)
	{

		int sampeSize=1;
		while (true)
		{
			if(sampeSize>=scaleSize)
			{
				return sampeSize;
			}else {
				sampeSize*=2;
			}

		}
	}
}
