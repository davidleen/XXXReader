package test.changu.com.customreader;

import java.io.IOException;

public interface LineReader {
	final static int FORWARD=0;
	final static int BACKWARD=1;
	public String m_readline() throws IOException;
	public void findLastLine() throws IOException;
	public void setOffset(long o,boolean isOpen) throws IOException;
	public long getOffset();
	
	public String openfile() throws IOException;
	public void closefile();
	

	public long getLocation() throws IOException;
	public long getSize() throws IOException;
	public long getFileEndPos() throws IOException;
	public String getFileName(); 
	public void m_seekNextLine() throws IOException;
	
	/**
     * @param endPos查找截止偏移
     * */
    public ParagraghData m_readlineData() throws IOException ;
}
