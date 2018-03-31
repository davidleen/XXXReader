package test.changu.com.customreader.epub;


/**
 * ���ڴ洢Epub�鱾�½���Ϣ
 * @author Chenli
 *
 */
public class EpubChapter {
	/**
	 * chapterTitle 章节名
	 */
	private String text;
	/**
	 * chapterTitle 章节id
	 */
//	private String id;
	/**
	 * chapterTitle 章节地址
	 */
	private String src;
	
	/** 在epub中的顺序 */
	private int playOrder;
	
	private int pri = 0;
	
	private boolean mHasChild = false;
	
	private boolean expanded = false;
	
	public void setHasChild(boolean hasChild) {
		this.mHasChild = hasChild;
	}
	
	public boolean hasChild() {
		return mHasChild;
	}
	
	public void setExpanded(boolean isExpanded) {
		this.expanded = isExpanded;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getSrc() {
		return src;
	}

	// 部分epub书籍章节节点 为  xxx.htm#xxxx
	public void setSrc(String src) {
		int index1 = src.lastIndexOf('.');
		if (index1 != -1) {
			int index2 = src.indexOf("#", index1);	
			if (index2 != -1) {
				this.src = src.substring(0, index2);
				return;
			}
		}
		this.src = src;
	}

	public int getPri() {
		return pri;
	}

	public void setPri(int pri) {
		this.pri = pri;
	}
	
	public int getPlayOrder(){
		return playOrder;
	}
	
	public void setPlayOrder(int playOrder){
		this.playOrder = playOrder;
	}


	EpubChapter() {
	}
}
