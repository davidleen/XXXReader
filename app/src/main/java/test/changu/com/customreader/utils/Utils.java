package test.changu.com.customreader.utils ;

public class Utils
{

	private static String sLibPath1 = "/system/lib/";	// for 预装
	/**
	 * 载入本地的so库
	 * @param libName 库文件名
	 * @return 是否加载成功
	 */
	public static boolean loadLib(String libName){
		try {
			System.loadLibrary(libName);
		} catch (UnsatisfiedLinkError e) {
			StringBuffer temp = new StringBuffer(sLibPath1);
			temp.append("lib");
			temp.append(libName);
			temp.append(".so");

			try {
				System.loadLibrary(temp.toString());
			} catch (UnsatisfiedLinkError e1) {
				return false;
			}
		}
		return true;
	}
}