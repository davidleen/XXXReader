package com.xxx.reader.text.layout;

/**
 * 提供绘制的图片的接口
 * <p>
 * <br>Created 2016年7月29日 上午11:26:11
 *
 * @author davidleen29
 * @see
 */
public interface BitmapProvider {


    /**
     * 提供3个图片  0 上一页  1 当前页  2  后一页
     * <p>
     * <br>Created 2016年7月29日 上午11:26:26
     *
     * @return
     * @author davidleen29
     */

    BitmapHolder getBitmap(int index);

    BitmapHolder getCurrentBitmap();

    BitmapHolder getNextBitmap();

    BitmapHolder getPreviousBitmap();


}
