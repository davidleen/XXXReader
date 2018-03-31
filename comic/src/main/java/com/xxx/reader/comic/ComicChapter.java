package com.xxx.reader.comic;



import com.xxx.reader.book.IChapter;

import java.util.List;

/**
 * Created by davidleen29 on 2017/8/25.
 */

public class ComicChapter  implements IChapter {


    /**
     * 本地文件路径 漫画以文件夹形式。
     */
    public String filePath;
    public String url;
    public String id;
    public String name;

    public int index;


    public List<ComicChapterItem> chapters;


    public ComicChapter(String id, String name, int index) {
        this.id = id;
        this.name = name;
        this.index = index;

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean hasPay() {

       return false;


    }




}
