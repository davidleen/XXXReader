package com.xxx.reader.prepare;


import com.xxx.reader.book.ChapterMeasureResult;
import com.xxx.reader.book.IChapter;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.PageInfo;

/**
 * 缓存处理准备工作
 * <p>
 * 1   对章节分页
 * <p>
 * 2   绘制指定分页数据到缓存上
 * <p>
 * Created by davidleen29 on 2017/8/26.
 */

public interface PrepareJob<C extends IChapter, P extends PageInfo, DP extends DrawParam> {


    /**
     * 章节分页处理。
     *
     * @param iChapter
     * @return
     */
    ChapterMeasureResult<P> measureChapter(C iChapter, DP drawParam, Cancelable cancelable, int pageType);




}
