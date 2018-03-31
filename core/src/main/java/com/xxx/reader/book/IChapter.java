package com.xxx.reader.book;

/**
 * Created by HP on 2018/3/20.
 */

public interface IChapter {

    String getId();

    String getName();

    String getUrl();

    String getFilePath();

    int getIndex();

    boolean hasPay();
}
