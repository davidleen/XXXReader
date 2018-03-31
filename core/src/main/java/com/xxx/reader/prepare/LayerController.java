package com.xxx.reader.prepare;


import com.xxx.reader.book.IBook;
import com.xxx.reader.book.IChapter;
import com.xxx.reader.core.DrawParam;

/**
 * Created by davidleen29 on 2017/10/30.
 */

public interface LayerController<C extends IChapter,DP extends DrawParam>  {
    int getCurrentPageIndex();

    int getCurrentPageCount();

    void jumpTo(int chapterIndex);

    void jumpTo(int chapterIndex, int pageIndex);

    void updateBook(IBook<C> iBook);

    boolean canPageChangedNext();

    boolean canPageChangedPrevious();

    void turnNext();

    void turnPrevious();

    void onDestroy();

    int getNextChapterIndex();

    int getPrevChapterIndex();

    int getCurrentChapterIndex();

    void jumpInChapter(float percent);

    void setProgressUpdateListener(PrepareLayer.ReadPosUpdateListener readPosListener);

    void clear();

    void updateCache();

    void updateDrawParam(DP drawParam);



    interface ReadPosUpdateListener {
        void onUpdate(int chapterIndex, int pageIndex, int pageCount);
    }
}
