package com.xxx.reader.comic;



import com.xxx.frame.Log;
import com.xxx.reader.Url2FileMapper;
import com.xxx.reader.book.ChapterMeasureResult;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.IPageTurner;
import com.xxx.reader.prepare.Cancelable;
import com.xxx.reader.prepare.PrepareJob;

import java.util.ArrayList;
import java.util.List;

/**
 * 漫画准备工作类型
 * <p>
 * 负责分页处理
 * <p>
 * <p>
 * Created by davidleen29 on 2017/8/25.
 */

public class ComicPrepareJob implements PrepareJob<ComicChapter, ComicPageInfo, DrawParam> {


    private Url2FileMapper<ComicChapter> filemapper;



    public ComicPrepareJob(Url2FileMapper<ComicChapter> filemapper) {

        this.filemapper = filemapper;
    }


    /**
     * 这里进行章节分页处理。
     *
     * @param iChapter
     * @param drawParam
     * @return
     */

    @Override
    public ChapterMeasureResult<ComicPageInfo> measureChapter(ComicChapter iChapter, DrawParam drawParam, Cancelable cancelable, int pageType) {


        if (iChapter == null) return null;
        if (drawParam == null) return null;
        if(drawParam.width<=0||drawParam.height<=0) return  null;
        if (iChapter.chapters == null)
            return null;


        switch (pageType) {
            case IPageTurner.HORIZENTAL:

                return measureChapterHorizontal(iChapter, drawParam, cancelable);


            default:

                return measureChapterVertical(iChapter, drawParam, cancelable);

        }

    }


    /**
     * 垂直布局
     * 等比压缩后的高度
     * 小于屏幕高度的 一张就是一页
     * 大于屏幕高度的 按屏幕高度分页。
     *
     * @param iChapter
     * @param drawParam
     * @param cancelable
     * @return
     */
    private ChapterMeasureResult<ComicPageInfo> measureChapterVertical(ComicChapter iChapter, DrawParam drawParam, Cancelable cancelable) {


        int totalPictureHeight = 0;
        int maxWidth = 0;


        List<ComicPageInfo> pageInfos = new ArrayList<>();

        int pageIndex = 0;

        for ( ComicChapterItem chapterItem : iChapter.chapters) {

            if (cancelable.isCancelled()) return null;
            float scaleSize = (float) chapterItem.width / drawParam.width;
            int scaleHeight = (int) (chapterItem.height / scaleSize);



            if (cancelable.isCancelled()) return null;
            //垂直划分
            int size = (scaleHeight - 1) / drawParam.height + 1;

            for (int i = 0; i < size; i++) {


                ComicPageInfo comicPageInfo = new ComicPageInfo();


                //每张图上起点
                int offset = drawParam.height * i;
                //图的长度
                int length = Math.min(scaleHeight - offset, drawParam.height);
                ComicPageInfo.BitmapFrame bitmapFrame = comicPageInfo.add(chapterItem.downloadUrl, filemapper.map(iChapter, chapterItem.downloadUrl), (int) (offset * scaleSize), chapterItem.width, (int) (length * scaleSize));
                bitmapFrame.setRect(0, 0, drawParam.width, length);


                comicPageInfo.pageIndex = pageIndex++;
                comicPageInfo.chapterIndex = iChapter.getIndex();

                pageInfos.add(comicPageInfo);
                if (cancelable.isCancelled()) return null;
            }


        }


        Log.e("totalPictureHeight:" + totalPictureHeight);
        Log.e("maxWidth:" + maxWidth);

        int size = pageInfos.size();

        for (ComicPageInfo comicPageInfo : pageInfos) {
            comicPageInfo.pageCount = size;
        }


        ChapterMeasureResult<ComicPageInfo> chapterMeasureResult = new ChapterMeasureResult<>();
        chapterMeasureResult.pageCount = size;
        chapterMeasureResult.pageValues = pageInfos;
        chapterMeasureResult.fileSize = totalPictureHeight;
        return chapterMeasureResult;

    }



    private ChapterMeasureResult<ComicPageInfo> measureChapterHorizontal(ComicChapter iChapter, DrawParam drawParam, Cancelable cancelable) {


        List<ComicPageInfo> pageInfos = new ArrayList<>();

        int pageIndex = 0;
        int totalPictureHeight = 0;

        float drawRatio = drawParam.width / (float) drawParam.height;
        for (ComicChapterItem chapterItem : iChapter.chapters) {

            if (cancelable.isCancelled()) return null;
            ComicPageInfo comicPageInfo = new ComicPageInfo();


            ComicPageInfo.BitmapFrame bitmapFrame = comicPageInfo.add(chapterItem.downloadUrl, filemapper.map(iChapter, chapterItem.downloadUrl), 0, chapterItem.width, chapterItem.height);


            float pictureRatio = chapterItem.width / (float) chapterItem.height;


            int left = 0;
            int top = 0;
            int bottom = drawParam.height;
            int right = drawParam.width;

            if (pictureRatio < drawRatio) {
                int width = (int) (drawParam.height * pictureRatio);
                left = (drawParam.width - width) / 2;
                right = left + width;

            } else {
                int height = (int) (drawParam.width / pictureRatio);
                top = (drawParam.height - height) / 2;
                bottom = top + height;
            }

            bitmapFrame.setRect(left, top, right, bottom);


            comicPageInfo.pageIndex = pageIndex++;
            comicPageInfo.chapterIndex = iChapter.getIndex();

            totalPictureHeight += chapterItem.height;
            pageInfos.add(comicPageInfo);
            if (cancelable.isCancelled()) return null;


        }
        int size = pageInfos.size();

        for (ComicPageInfo comicPageInfo : pageInfos) {
            comicPageInfo.pageCount = size;
        }

        ChapterMeasureResult<ComicPageInfo> chapterMeasureResult = new ChapterMeasureResult<>();
        chapterMeasureResult.pageCount = size;
        chapterMeasureResult.pageValues = pageInfos;
        chapterMeasureResult.fileSize = totalPictureHeight;
        return chapterMeasureResult;

    }



}
