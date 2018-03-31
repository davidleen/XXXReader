package com.xxx.reader;


import com.xxx.reader.book.IChapter;

/**
 * Created by davidleen29 on 2017/10/17.
 */

public interface Url2FileMapper<T extends IChapter> {


    String  map( T iChapter,String url);
    String map(String chapterName);
}
