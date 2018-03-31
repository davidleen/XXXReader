package com.xxx.reader.core;


import android.view.MotionEvent;

import com.xxx.reader.text.layout.BitmapHolder;

/**
 * 分页内容绘制
 * <p>
 * <p>
 * <p>
 * Created by davidleen29 on 2017/8/30.
 */

public abstract class PageBitmap<P extends PageInfo, D extends DrawParam> extends AbstractBitmapHolder implements BitmapHolder, IDrawable {


    /**
     * 缓存图片的状态   0  初始 1 已经绘制  2 DIRTY
     */
    public volatile int state;


    BitmapPainThread thread;
    private IDrawable drawable;

    public P getPageInfo() {
        return pageInfo;
    }

    private P pageInfo;
    private D drawParam;


    public PageBitmap(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
        thread = new BitmapPainThread();
        thread.start();

    }


    public void attachPageInfo(P pageInfo) {
        if (this.pageInfo == pageInfo) return;
        this.pageInfo = pageInfo;
        thread.interrupt();


    }

    public void updateDrawParam(D drawParam) {
        if (this.drawParam == drawParam) return;
        this.drawParam = drawParam;
        thread.interrupt();

    }

    public void updateIDrawable(IDrawable drawable) {
        if (this.drawable == drawable) return;
        this.drawable = drawable;


    }

    public void setDirty() {


    }


    public void setState(int drawState) {
        this.state = drawState;
    }


    /**
     * 分页绘制方法
     *
     * @param pageInfo
     */
    public abstract void drawPage(P pageInfo, D drawParam);


    public abstract boolean onTouchEvent(MotionEvent event);


    class BitmapPainThread extends DestroyableThread {


        @Override
        public void runOnThread() {

            final P aPageInfo=pageInfo;
            final D aDrawPara=drawParam;

            if(aPageInfo==null) return ;
            if(aDrawPara==null) return ;
            drawPage(pageInfo, drawParam);
            drawable.updateView();


        }


    }


    public void onDestroy() {
        if (thread != null) {
            thread.setDestroy();
        }


    }


    @Override
    public void updateView() {

        if (thread != null) {

            thread.interrupt();

        }


    }
}
