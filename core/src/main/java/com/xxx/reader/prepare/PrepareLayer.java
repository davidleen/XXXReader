package com.xxx.reader.prepare;

import android.os.Message;
import android.view.MotionEvent;

import com.xxx.frame.Log;
import com.xxx.reader.book.ChapterMeasureResult;
import com.xxx.reader.book.IBook;
import com.xxx.reader.book.IChapter;
import com.xxx.reader.core.BuildConfig;
import com.xxx.reader.core.CacheUpdateListener;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.core.IPageTurner;
import com.xxx.reader.core.PageBitmap;
import com.xxx.reader.core.PageInfo;
import com.xxx.reader.text.layout.BitmapHolder;
import com.xxx.reader.text.layout.BitmapProvider;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 绘制准备层
 * <p>
 * 分页调度
 * <p>
 * 准备缓存
 * <p>
 * Created by davidleen29 on 2017/8/25.
 */

public class PrepareLayer<C extends IChapter, P extends PageInfo, DP extends DrawParam, PB extends PageBitmap> extends BasePrepareLayer<C, DP> implements BitmapProvider, CacheUpdateListener {

    public static final boolean DEBUG = true && BuildConfig.DEBUG;

    /**
     * 最大的缓存数量
     */
    public static int MAX_CACHE_SIZE = 7;
    /**
     * 当前页index
     */
    public static int MID_CACHE_INDEX = MAX_CACHE_SIZE / 2;
    /**
     * 缓存图片内容。
     */
    PageBitmap cacheBitmaps[] = new PageBitmap[MAX_CACHE_SIZE];


    /**
     * 缓存工作实现接口   测量，绘制。
     */
    PrepareJob<C, P, DP> prepareJob;


//    PageCacheThread pageCacheThread;


    PrepareThread prepareThread = null;

    /**
     * 章节测量分页功能处理类。
     */
    ChapterMeasureManager<C, P, DP> chapterMeasureManager;


    /**
     * 真实绘制回调接口
     */
    private IDrawable iDrawable;
    /**
     * 相关绘制参数
     */
    private DP drawParam;


    /**
     * @param prepareJob
     * @param iDrawable  界面重绘回调接口
     * @param creator
     */
    public PrepareLayer(PrepareJob<C, P, DP> prepareJob, IDrawable iDrawable, PageBitmapCreator<PB> creator) {

        this.iDrawable = iDrawable;
        this.prepareJob = prepareJob;
        chapterMeasureManager = new ChapterMeasureManager<>(this, prepareJob, IPageTurner.HORIZENTAL);
        for (int i = 0; i < MAX_CACHE_SIZE; i++) {
            cacheBitmaps[i] = creator.create();
            cacheBitmaps[i].updateIDrawable(iDrawable);
        }


        prepareThread = new PrepareThread(this, MAX_CACHE_SIZE);
        prepareThread.start();

//        pageCacheThread=PageCacheThread.getSingleton();


    }

    @Override
    public void jumpInChapter(float percent) {


        if (currentPageCount <= 0) {

            Message message = Message.obtain();
            message.what = MSG_JUMP_PERCENT;
            message.obj = percent;
            handler.removeMessages(MSG_JUMP_PERCENT);
            handler.sendMessageDelayed(message, 50);
            return;
        }
        currentPageIndex = Math.round(currentPageCount * percent);
        currentPageIndex = Math.max(0, Math.min(currentPageIndex, currentPageCount - 1));


        updateCache();


    }

    /**
     * 直接跳转到指定章节
     *
     * @param chapterIndex
     * @param pageIndex
     */
    @Override
    public void jumpTo(int chapterIndex, int pageIndex) {


        currentPageIndex = pageIndex;
        currentChapterIndex = chapterIndex;

        updateCache();


    }


    @Override
    public BitmapHolder getNextBitmap() {
        return cacheBitmaps[MID_CACHE_INDEX + 1];
    }

    @Override
    public BitmapHolder getPreviousBitmap() {
        return cacheBitmaps[MID_CACHE_INDEX - 1];
    }

    @Override
    public BitmapHolder getBitmap(int index) {
        return cacheBitmaps[index];
    }

    @Override
    public PageBitmap getCurrentBitmap() {
        return cacheBitmaps[MID_CACHE_INDEX];
    }


    @Override
    public void updateDrawParam(DP drawParam) {
        if (this.drawParam != null && this.drawParam.equals(drawParam))
            return;
        this.drawParam = drawParam;

        for(PageBitmap bitmap:cacheBitmaps)
        {
            bitmap.updateDrawParam(drawParam);
        }
        //全部重新绘制。
        setAllCacheDirty();
        chapterMeasureManager.updateDrawParam(drawParam);


//        for (int i = 0; i < MAX_CACHE_SIZE; i++) {
//
//            fillCacheThreads[i].setDrawPara(drawParam);
//        }
        updateCache();

    }


    private void setAllCacheDirty() {
        for (int i = 0; i < MAX_CACHE_SIZE; i++) {
            cacheBitmaps[i].setDirty();
        }
    }


    /**
     * 切换书籍
     *
     * @param iBook
     */
    @Override
    public void updateBook(IBook<C> iBook) {
        super.updateBook(iBook);
        setAllCacheDirty();

        chapterMeasureManager.setbook(iBook);
        updateCache();


    }

    public void clear() {

        setAllCacheDirty();
        chapterMeasureManager.clear();

    }


    /**
     * 缓存页面的额绘制，
     *
     * @param offset 偏移值 0 为当前页， 中间页
     * @param skip   绘制过程中是否略过
     */
    public void preparePage(int offset, AtomicBoolean skip) {


//        if (skip.get()) return;


        int index = MID_CACHE_INDEX + offset;

        if (offset == 0) {
            //矫正pageindex位置
            ChapterMeasureResult<P> result = chapterMeasureManager.getMeasuredResult(currentChapterIndex);
            if (result != null && result.pageCount > 0 && currentPageIndex >= result.pageCount) {
                currentPageIndex = result.pageCount - 1;
            }

        }

        P currentPageInfo = chapterMeasureManager.getPageInfo(currentChapterIndex, currentPageIndex, offset);
        if (offset == 0 && currentPageInfo != null) {
            currentPageCount = currentPageInfo.pageCount;

            updateProgress();


        }

        if (DEBUG)
            Log.e("startDrawThread:=========================" + currentPageInfo + ",currentChapterIndex:" + currentChapterIndex + ",currentPageIndex:" + currentPageIndex + ",comicbook:" + iBook);
        PageBitmap cacheBitmap = cacheBitmaps[index];
        cacheBitmap.attachPageInfo(currentPageInfo);


//        if (cacheBitmap.getPageInfo() == currentPageInfo && cacheBitmap.isValid()) return;

//
//
//        startDrawThread(cacheBitmap, currentPageInfo, drawParam, skip);
//        cacheBitmap.setState(CACHE_STATE_DRAWING);


    }


    @Override
    public boolean canPageChangedNext() {

        //如果当前页在测量中, return false;


        return chapterMeasureManager.canPageChangedNext(currentChapterIndex, currentPageIndex);


    }


    @Override
    public boolean canPageChangedPrevious() {

        //如果当前页在测量中, return false;
        return chapterMeasureManager.canPageChangedPrevious(currentChapterIndex, currentPageIndex);


    }


    @Override
    public void turnNext() {


        P pageInfo = chapterMeasureManager.getNextPageInfo(currentChapterIndex, currentPageIndex);
        if (pageInfo != null) {


            currentPageIndex = pageInfo.pageIndex;
            currentChapterIndex = pageInfo.chapterIndex;
            currentPageCount = pageInfo.pageCount;


            long time = Calendar.getInstance().getTimeInMillis();

            PageBitmap tempPageBitmap = cacheBitmaps[0];
            for (int i = 0; i < MAX_CACHE_SIZE - 1; i++) {
                cacheBitmaps[i] = cacheBitmaps[i + 1];
            }
            cacheBitmaps[MAX_CACHE_SIZE - 1] = tempPageBitmap;


            tempPageBitmap.setDirty();
            if (DEBUG)
                Log.e("time use in switch:" + (Calendar.getInstance().getTimeInMillis() - time));
            iDrawable.updateView();
            updateCache();
        }


    }


    @Override
    public void turnPrevious() {


        P pageInfo = chapterMeasureManager.getPreviousPageInfo(currentChapterIndex, currentPageIndex);
        if (pageInfo != null) {
            currentPageIndex = pageInfo.pageIndex;
            currentChapterIndex = pageInfo.chapterIndex;
            currentPageCount = pageInfo.pageCount;

            PageBitmap temp = cacheBitmaps[MAX_CACHE_SIZE - 1];
            for (int i = MAX_CACHE_SIZE - 1; i > 0; i--) {
                cacheBitmaps[i] = cacheBitmaps[i - 1];
            }
            cacheBitmaps[0] = temp;
            temp.setDirty();

            updateCache();
        }


    }


    /**
     * 更新缓存
     */
    @Override
    public void updateCache() {

        if (prepareThread != null) {
            prepareThread.setSkip(true);
            prepareThread.interrupt();
        }


//        pageCacheThread.update(chapterMeasureManager,currentChapterIndex);


    }


    @Override
    public void onDestroy() {

        for (PageBitmap bitmap : cacheBitmaps) {
            bitmap.onDestroy();
        }

        if (prepareThread != null) {
            prepareThread.setDestroy();
        }


    }

    public boolean onTouchEvent(MotionEvent event) {

        return false;
    }


}
