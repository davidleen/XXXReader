package test.changu.com.customreader.zip;


import test.changu.com.customreader.utils.Utils;

public class ZipJNIInterface {
	
	static {
		Utils.loadLib("unzip");
	}

	/**
	 * @param args
	 */
	/*
	 * 将libunzip.so 放于libs\armeabi 目录下
	 * Activity初始化时调用，xxxxx为程序的包名
	static
	{
		
		System.load("/data/data/xxxx/lib/libunzip.so");
		
	}
	将ZipJNIInterface.java放于 com.nd.zip包名下
	
	UnZip
	 
	*/
	/*
	 * UnZip 居功返回true 反之false    
	 * strZipFile Zip文件所在的完整路径
	 * strSubFileName 要解压的文件  
	 * strSavePath 保存解压后文件的完全路径
	 * strZipencoding 新增的Zip文件里的文件名编码
	 */
	public static native boolean UnZip(String strZipFile, String strSubFileName, String strSavePath, String strZipencoding);
	
	
	/**
	 * 获取zip文件列表（包含所有的文件夹和文件）
	 * @param strZipFile 	zip path
	 * @return
	 */
	public static native Object getZipEntries(String strZipFileName);

}
