package com.xxx.reader.prepare;

import android.os.Handler;
import android.os.Message;

import com.xxx.reader.book.IBook;
import com.xxx.reader.book.IChapter;
import com.xxx.reader.core.DrawParam;


/**
 * Created by davidleen29 on 2017/10/30.
 */

public abstract class BasePrepareLayer<C extends IChapter, DP extends DrawParam> implements LayerController<C, DP> {

    /**
     * 更新进度
     */
    public static final int MSG_UPDATE_PROGRESS = 12345;
    /**
     * 更新进度
     */
    public static final int MSG_UPDATE_CACHE = 12347;
    /**
     * 更新进度
     */
    public static final int MSG_JUMP_PERCENT = 12346;
    /**
     * 当前章节Index
     */
    protected volatile int currentChapterIndex;

    /**
     * 线程与主线程信息处理handler
     *
     * @return
     */
    private Handler createEventHandler() {

        return new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {


                    case MSG_UPDATE_CACHE:

                        updateCache();
                        break;

                    case MSG_UPDATE_PROGRESS:

                        if (readPosListener != null) {
                            readPosListener.onUpdate(currentChapterIndex, currentPageIndex, currentPageCount);
                        }

                        break;
                    case MSG_JUMP_PERCENT:

                        float percent = (float) msg.obj;
                        jumpInChapter(percent);

                        break;
                }


            }
        };


    }

    protected void updateProgress() {
        if (handler != null) {
            handler.removeMessages(MSG_UPDATE_PROGRESS);
            handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 50);
        }
    }


    protected void sendUpdateCacheEvent() {

        if (handler != null) {
            handler.removeMessages(MSG_UPDATE_CACHE);
            handler.sendEmptyMessageDelayed(MSG_UPDATE_CACHE, 50);
        }

    }

    @Override
    public void jumpTo(int chapterIndex) {

        jumpTo(chapterIndex, 0);


    }

    public BasePrepareLayer() {

        handler = createEventHandler();


    }

    protected IBook<C> iBook;

    /**
     * 事件处理handler
     */
    protected Handler handler;

    protected ReadPosUpdateListener readPosListener;

    /**
     * 当前分页index
     */
    protected volatile int currentPageIndex;
    /**
     * 当前章节分页总数
     */
    protected volatile int currentPageCount;


    @Override
    public void setProgressUpdateListener(ReadPosUpdateListener readPosListener) {
        this.readPosListener = readPosListener;
    }

    @Override
    public void updateBook(IBook<C> iBook) {

        this.iBook = iBook;
    }



    @Override
    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    @Override
    public int getCurrentPageCount() {
        return currentPageCount;
    }


    @Override
    public int getNextChapterIndex() {
        return currentChapterIndex + 1;
    }

    @Override
    public int getPrevChapterIndex() {
        return currentChapterIndex - 1;
    }

    @Override
    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

}
