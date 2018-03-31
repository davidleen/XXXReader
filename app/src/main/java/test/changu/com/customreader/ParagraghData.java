package test.changu.com.customreader;

import android.util.SparseIntArray;

import com.changdu.changdulib.util.Log;


public class ParagraghData {
	/*段落内容*/
	private String content;
	
	/*段落内每个字符在文件中的偏移*/
	private long paragragStart = 0;
	
	/*编码*/
	private int code;
	
	
	/**
	 * 段落类型   章节头，章节内容  尾部评论  作者的话 ==     //默认章节内容
	 */
	public int type;
	
	/*内容长度*/
	private int contentCount = -1;
	
	private SparseIntArray offsetHash = null;
	
	public int mAuthorType = -1;
	private String authHead;
	
	public final int getContentCount() {
		if (contentCount == -1) {
			if (content == null || content.length() == 0) {
				contentCount = 0;
			} else {
				contentCount = content.length();
			}
		}
		
		return contentCount;
	}
	
	public void setContentCount(int count) {
		contentCount = count;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public void setStartParagrag(long paragragStart) {
		this.paragragStart = paragragStart;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getAuthHead() {
		return authHead;
	}
	
	public void setAuthHead(String content) {
		this.authHead = content;
	}
	
	/**
	 * 获取输入偏移对应的字符偏移
	 * @param loc
	 * @param locBegin
	 * @param beginIndex
	 * @param locEnd
	 * @param endIndex
	 * @return
	 */
	public int getCharIndex(long loc, int locBegin, int beginIndex, long locEnd, int endIndex) {
		byte[] temp = null;
		String string = null;
		int byteCount = (int)(loc - locBegin);
		if (byteCount < 0) {
			return locBegin;
		}
		try {
			temp = content.substring(locBegin, endIndex).getBytes(TXTReader.getEncoding(code));
			string = new String(temp, 0, byteCount, TXTReader.getEncoding(code));
		} catch (Exception e) {
			Log.e(e);
			return locBegin;
		}
		
		return string.length() + locBegin;
	}
	
	public long getCharLocation(int index) {
		if (index == 0) {
			return paragragStart;
		}
		
		if (offsetHash == null) {
			offsetHash = new SparseIntArray();
		}
		int int1 = offsetHash.get(index, -1);
		if (int1 != 0 && int1 != -1) {
			return int1 + paragragStart;
		}
		
		int offset = 0 ;
		try {
			if (index == -1) {
				Log.e("index error -1");
				index = 0;
			}
			offset = content.substring(0, index).getBytes(TXTReader.getEncoding(code)).length;
		} catch (Exception e) {
			Log.e(e);
		}
		
		offsetHash.put(index, offset);
		return paragragStart + offset;
	}

	public void setType(int authorType) {
		mAuthorType = authorType;
	}
	
}
