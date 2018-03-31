package com.xxx.reader.book;

import java.util.List;

/**
 * 书籍接口
 * Created by davidleen29 on 2017/8/25.
 */

public interface IBook<T extends IChapter> {


    String getName()  ;

    String getUrl();

    String getFilePath();


    List<T> getChapters();

    T getChapter(int index);

    int getChapterCount();

    String getBookId();
}
