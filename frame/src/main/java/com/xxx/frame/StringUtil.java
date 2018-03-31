package com.xxx.frame;


import java.util.List;

/**
 * 字符串 功能类。2009-9-22
 */
public class StringUtil {



	public static String parseLong(long num) {
		long n = num;
		if (n < 1024) {
			return n + "B";
		}
		long t = n;
		t = n / 1024;
		if (t < 1024) {
			return format(n / 1024f) + "KB";
		}
		n = t;
		t = n / 1024;
		if (t < 1024) {
			return format(n / 1024f) + "MB";
		}
		return format(t / 1024f) + "GB";
	}

	private static String format(double d) {
		String s = String.valueOf(d);
		int i = s.indexOf('.');
		if (i == -1)
			return s;
		if (i == 0)
			s = "0" + s;
		if (s.endsWith(".0") || i >= 3)
			return s.substring(0, i);
		if (s.length() < i + 3)
			return s;
		return s.substring(0, i + 3);
	}


	public static final boolean isEmpty(String s) {
		return s == null || s.trim().equals("");
	}

	public static int getInt(String s) {
		if (s == null)
			return 0;
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getNotEmpty(String... strs) {
		if (strs == null)
			return null;
		for (int i = 0; i < strs.length; i++) {
			if (!StringUtil.isEmpty(strs[i]))
				return strs[i];
		}
		return null;
	}
	
	public static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}

	/**
	 * 判断字符是否空格
	 *
	 *
	 * @param c
	 * @return
     */
	public static boolean isBlank(char c) {
		//半角   全角
		return '\u0008'==c||12288==c||' '==c;

	}



	/**
	 * 删除对应相关特定前后关键词
	 *
	 * @return
	 */
	public static String delKey(String mgs, String keymsg) {
		if (mgs.startsWith(keymsg)) {
			mgs = mgs.substring(1);
		}

		if (mgs.endsWith(keymsg)) {
			mgs = mgs.substring(0, mgs.length() - 1);
		}

		return mgs;
	}

	public static String asString(List<String> list) {




			StringBuffer mBuffer = new StringBuffer();
			for (String u : list) {
				mBuffer.append(u);
			}

			return mBuffer.toString();

	}
}
