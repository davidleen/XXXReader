package test.changu.com.customreader;

import java.io.IOException;


/**
 * 增加引用次数的reader  
 * 
 *  打开关闭文件时候  更新引用计数  只有引用计数达到0 时候才真正的关闭或打开文件。
 * <br>Created 2016年7月1日 上午9:47:42
 * @version  
 * @author   davidleen29		
 *
 * @see
 */
public class CountableLineReader implements LineReader{
    
    public int refrenceCount;
    
    private LineReader linReader;
    
    private String pasePath;

    @Override
    public String m_readline() throws IOException {
         
        return null;
    }

    
   
    
    
    @Override
    public void findLastLine() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOffset(long o, boolean isOpen) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String openfile() throws IOException {
        
        if(refrenceCount==0)
        {
            pasePath=linReader.openfile();
            refrenceCount=1;
        }else
        {
            refrenceCount++;
        }
        
         
        return pasePath;
    }

    @Override
    public void closefile() {
        if(refrenceCount==1)
        {
             linReader.closefile();
            refrenceCount=0;
        }else
        {
            refrenceCount--;
        }
        
        
    }

    @Override
    public long getLocation() throws IOException {
         
        return 0;
    }

    @Override
    public long getSize() throws IOException {
        
        return 0;
    }

    @Override
    public long getFileEndPos() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void m_seekNextLine() throws IOException {
        
        
    }

    @Override
    public ParagraghData m_readlineData() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    

}
