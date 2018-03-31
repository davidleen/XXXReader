package com.xxx.reader.comic;



import com.xxx.reader.core.PageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 二次元分页数据结构。
 * <p>
 * Created by davidleen29 on 2017/8/26.
 */

public class ComicPageInfo extends PageInfo {





  public   List<BitmapFrame> bitmapFrames;


    public BitmapFrame add(String downloadUrl, String filePath, int offset, int width, int height) {

        BitmapFrame bitmapFrame = new BitmapFrame();
        bitmapFrame.url = downloadUrl;
        bitmapFrame.filePath = filePath;
        bitmapFrame.offset = offset;
        bitmapFrame.width = width;
        bitmapFrame.length = height;
        if (bitmapFrames == null) {
            bitmapFrames = new ArrayList<>();

        }
        bitmapFrames.add(bitmapFrame);
        return bitmapFrame;


    }

    /**
     * 图片绘制块
     */
    public static class BitmapFrame {

        /**
         * 起始位置。
         */
        public int offset;
        /**
         * 绘制宽度
         */
        public int width;
        /**
         * 绘制区域长度
         */
        public int length;
        /**
         * 图片url
         */
        public String url;
        /**
         * 图片的路径
         */
        public String filePath;


        public int left;
        public int right;
        public int top;
        public int bottom;

        public void setRect(int left, int top, int right, int bottom) {

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
