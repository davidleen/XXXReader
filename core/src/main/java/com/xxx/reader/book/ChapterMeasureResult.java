package com.xxx.reader.book;

import java.util.List;

/**
 * 章节测量结果
 * Created by davidleen29 on 2017/8/25.
 */

public class ChapterMeasureResult<T> {

    /**
     * 总页数
     */
   public int pageCount;
    /**
     * 分页信息
     */
    public List<T> pageValues;

    /**
     * 总文件大小   章节内图片的总高度
     */
    public  int fileSize;
    /**
     * 章节名称
     */
      public String name;


}
