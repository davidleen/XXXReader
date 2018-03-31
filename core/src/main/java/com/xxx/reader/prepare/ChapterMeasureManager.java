package com.xxx.reader.prepare;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.SparseArray;

import com.xxx.frame.Log;
import com.xxx.reader.book.ChapterMeasureResult;
import com.xxx.reader.book.IBook;
import com.xxx.reader.book.IChapter;
import com.xxx.reader.core.CacheUpdateListener;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.PageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节测量任务处理类
 * Created by davidleen29 on 2017/8/25.
 */

public class ChapterMeasureManager<C extends IChapter, P extends PageInfo, DP extends DrawParam> {


    /**
     * 章节分页数据集合
     */
    SparseArray<ChapterMeasureResult<P>> chapterMeasureResultSparseArray;
    List<PreparingTask> measureTasks = new ArrayList<>();
    PrepareJob<C, P, DP> prepareJob;
    private CacheUpdateListener cacheUpdateListener;
    private DP drawParam;

    private IBook<C> book;

    private int pageType;

    public ChapterMeasureManager(CacheUpdateListener drawCache, PrepareJob<C, P, DP> prepareJob, int pageType) {

        this.cacheUpdateListener = drawCache;
        this.prepareJob = prepareJob;
        this.pageType = pageType;
        chapterMeasureResultSparseArray = new SparseArray<>();
    }

    /**
     * 新书籍设置
     *
     * @param book
     */
    public void setbook(IBook<C> book) {

        this.book = book;

        clear();

    }


    public ChapterMeasureResult<P> getMeasuredResult(int chapterIndex) {
        synchronized (chapterMeasureResultSparseArray) {
            return chapterMeasureResultSparseArray.get(chapterIndex);
        }
    }

    /**
     * 根据参数获取指定页的分页数据
     *
     * @param chapterIndex
     * @param pageIndex
     * @param offset       当前页的偏移量
     * @return null 分页信息不存在
     */
    public P getPageInfo(int chapterIndex, int pageIndex, int offset) {


        ChapterMeasureResult<P> chapterMeasureResult = getMeasuredResult(chapterIndex);
        if (chapterMeasureResult == null || chapterMeasureResult.pageValues == null) {
            addTask(chapterIndex);
            return null;
        }


        if (pageIndex + offset >= chapterMeasureResult.pageCount) {
            return getPageInfo(chapterIndex + 1, 0, pageIndex + offset - chapterMeasureResult.pageCount);


        } else if (pageIndex + offset < 0) {
            chapterMeasureResult = getMeasuredResult(chapterIndex - 1);
            if (chapterMeasureResult == null) {
                addTask(chapterIndex - 1);
                return null;
            }
            return getPageInfo(chapterIndex - 1, chapterMeasureResult.pageCount - 1, pageIndex + offset + 1);
        }


        return chapterMeasureResult.pageValues.get(pageIndex + offset);
    }

    public P getPageInfo(int chapterIndex, int pageIndex) {
        return getPageInfo(chapterIndex, pageIndex, 0);
    }

    public P getNextPageInfo(int currentChapterIndex, int currentPageIndex) {
        return getPageInfo(currentChapterIndex, currentPageIndex, 1);
    }

    public boolean canPageChangedPrevious(int currentChapterIndex, int currentPageIndex) {

        return getPreviousPageInfo(currentChapterIndex, currentPageIndex) != null;

    }

    public boolean canPageChangedNext(int currentChapterIndex, int currentPageIndex) {
        return getNextPageInfo(currentChapterIndex, currentPageIndex) != null;
    }

    public P getPreviousPageInfo(int currentChapterIndex, int currentPageIndex) {
        return getPageInfo(currentChapterIndex, currentPageIndex, -1);
    }

    /**
     * /**
     * 进行章节测量线程 进行页数划分
     */
    private class PreparingTask extends AsyncTask implements Cancelable {

        public IBook<C> iBook;
        int chapterIndex;
        private PrepareJob<C, P, DP> prepareJob;


        public PreparingTask(IBook<C> iBook, int chapterIndex, PrepareJob<C, P, DP> prepareJob) {
            this.iBook = iBook;
            this.chapterIndex = chapterIndex;


            this.prepareJob = prepareJob;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            ChapterMeasureResult<P> chapterMeasureResult = null;
            C iChapter = iBook.getChapter(chapterIndex);
            if (iChapter != null) {
                chapterMeasureResult = prepareJob.measureChapter(iChapter, drawParam, this, pageType);


            }


            return chapterMeasureResult;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Object o) {
            super.onCancelled(o);
            synchronized (measureTasks) {
                Log.e("measureTasks task:" + chapterIndex + "cancel!!!!!!!!!!!" + o + "drawParam:" + drawParam);
                measureTasks.remove(this);

            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            synchronized (measureTasks) {
                Log.e("measureTasks task:" + chapterIndex + "complete!!!!!!!!!!!" + o + "drawParam:" + drawParam);
                measureTasks.remove(this);
            }
            if (o instanceof ChapterMeasureResult) {

                synchronized (chapterMeasureResultSparseArray) {
                    chapterMeasureResultSparseArray.put(chapterIndex, (ChapterMeasureResult<P>) o);
                }


                //成功后通知缓存改变
                cacheUpdateListener.updateCache();
            }


        }
    }


    public void addTask(int chapterIndex) {

        if (book == null) return;
        if (chapterIndex < 0) return;
        if (chapterIndex >= book.getChapterCount()) return;
        C chapter = book.getChapter(chapterIndex);
        if (chapter == null) return;
        if (drawParam == null) return;

        //已经有测量的结果
        if (getMeasuredResult(chapterIndex) != null) {
            return;
        }


        synchronized (measureTasks) {
            //正在测量中
            PreparingTask findTask = null;
            for (PreparingTask task : measureTasks) {

                if (task.getStatus() == AsyncTask.Status.RUNNING && task.chapterIndex == chapterIndex) {

                    findTask = task;
                    break;
                }
            }
            if (findTask != null) {
                findTask.cancel(true);
                measureTasks.remove(findTask);
            }


            PreparingTask task = new PreparingTask(book, chapterIndex, prepareJob);
            measureTasks.add(task);
            task.execute();
        }


    }


    public void updateDrawParam(DP drawParam) {


        if (this.drawParam != null && this.drawParam.equals(drawParam))
            return;
        this.drawParam = drawParam;
        //基本上 绘制参数的改变，都会导致分页失效。

        clear();

    }


    public void clear() {


        //书籍切换 清空所有缓存数据。
        synchronized (measureTasks) {
            for (PreparingTask task : measureTasks) {
                task.cancel(true);


            }
            measureTasks.clear();
        }

        synchronized (chapterMeasureResultSparseArray) {
            chapterMeasureResultSparseArray.clear();
        }
    }


}
