package com.xxx.reader.turnner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.xxx.frame.Log;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.core.PageSwitchListener;
import com.xxx.reader.text.layout.BitmapHolder;
import com.xxx.reader.text.layout.BitmapProvider;


/**
 * 滚动式
 * Created by davidleen29 on 2017/8/29.
 */

public class ScrollPageTurner extends AbsPageTurner implements GestureDetector.OnGestureListener {

    private final GestureDetector gestureDetector;
    /**
     * 滑动距离超过一定长度后,翻页调整,用来判断是上一张还是下一张的标志  >0,  ==0, <0
     */
    private int mPageType = 0;
    /**
     * 是否触发了fling的标志
     */
    private boolean mIsFling = false;


    /**
     * 是否要进行翻页调整
     */
    private boolean mIsAdjust = false;
    /**
     * 滑动的距离 根据这个距离进行绘制
     */
    private float offsetX;


    private Rect drawRect = new Rect();

    public ScrollPageTurner(Context context, PageSwitchListener pageSwitchListener, IDrawable drawable, BitmapProvider bitmapProvider) {
        super(context, pageSwitchListener, drawable, bitmapProvider);
        gestureDetector = new GestureDetector(context, this);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }


    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mIsFling) return false;


        int direction = (int) (e2.getX() - e1.getX());

        scroller.abortAnimation();

        int currX = scroller.getCurrX();
        if (!canScrollPrevious(direction, currX)) {
            scroller.startScroll(currX, 0, 0, 0);
        } else if (!canScrollNext(direction, currX)) {
            scroller.startScroll(currX, 0, 0, 0);
        } else {
            scroller.startScroll(currX, 0, (int) (-distanceX), 0);
        }

        drawable.updateView();
//        return true;
        return true;
    }

    /**
     * 能否滚到下一张
     *
     * @param direction
     * @param currX
     * @return true 为能  false 不能
     */
    private boolean canScrollNext(int direction, int currX) {
        return !(!canTurnNext() && direction < 0 && (currX < -drawRect.width() / 3));
    }

    /**
     * 能否滚到上一张的判断
     *
     * @param direction
     * @param currX
     * @return true 为能  false 不能
     */
    private boolean canScrollPrevious(int direction, int currX) {
        return !(direction > 0 && !canTurnPrevious() && !(currX < drawRect.width() / 3));
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        Log.e("=================velocityX="+(velocityX)+" ,offsetX ="+offsetX);
        if (mIsFling) return false;
        if (isCancelMove(e1, e2)) {
            return true;
        }
        if (cancelFlingAndSroll(e1, e2, velocityX)) {
            drawable.updateView();
            return true;
        }

        return true;

    }


    @Override
    public void onDraw(Canvas canvas) {

        if (drawParam == null) return;

        drawRect.set(0, 0, drawParam.width, drawParam.height);

        canvas.save();


        canvas.clipRect(0, 0, drawParam.width, drawParam.height);
        canvas.translate(offsetX, 0);

        //裁剪并绘制当前页
        drawCurrent(canvas, bitmapProvider.getCurrentBitmap());

        if (offsetX > 0) {
            drawPrev(canvas, bitmapProvider.getPreviousBitmap());
//            canvas.restore();
        }
        if (offsetX < 0) {
            drawNext(canvas, bitmapProvider.getNextBitmap());
        }


        canvas.restore();


        computeScroll();


    }

    /**
     * 裁剪并绘制当前的漫画的后一张图
     *
     * @param canvas
     * @param bitmapHolder
     */
    private void drawNext(Canvas canvas, BitmapHolder bitmapHolder) {
        canvas.save();
        canvas.translate(drawParam.width, 0);
        float left = 0;
        Log.e("=====================drawNext ============offsetX=" + offsetX);
        float right = 0 - offsetX;
//        left = 0;
//        right = drawParam.width;
        canvas.clipRect(left, 0, right, drawParam.height);


        Bitmap bitmap = bitmapHolder.lockRead();
        canvas.drawBitmap(bitmap, drawRect, drawRect, null);
        bitmapHolder.unLockRead();

        canvas.restore();
    }

    /**
     * 裁剪并绘制当前的漫画的前一张图
     *
     * @param canvas
     * @param bitmapHolder
     */
    private void drawPrev(Canvas canvas, BitmapHolder bitmapHolder) {
        canvas.save();
        canvas.translate(-drawParam.width, 0);

        float left = drawParam.width - offsetX;
        float right = drawParam.width;
        canvas.clipRect(left, 0, right, drawParam.height);


        Bitmap bitmap = bitmapHolder.lockRead();
        canvas.drawBitmap(bitmap, drawRect, drawRect, null);
        bitmapHolder.unLockRead();

        canvas.restore();
    }

    /**
     * 裁剪并绘制当前的漫画
     *
     * @param canvas
     * @param bitmapHolder
     */
    private void drawCurrent(Canvas canvas, BitmapHolder bitmapHolder) {

        canvas.save();
        float left = offsetX < 0 ? 0 - offsetX : 0;
        float right = offsetX < 0 ? drawParam.width : drawParam.width - offsetX;
        canvas.clipRect(left, 0, right, drawParam.height);
//        drawCache.getCurrentBitmap().paint(canvas, drawRect, drawRect , drawable);
        Bitmap bitmap = bitmapHolder.lockRead();
        canvas.drawBitmap(bitmap, drawRect, drawRect, null);
        bitmapHolder.unLockRead();
        canvas.restore();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {


       return false;


    }


    private void computeScroll() {
        if (drawParam == null) return;

        if (!scroller.computeScrollOffset()) {

            if (mIsFling) { //快速滑动处理
                handleFling();
                resetScroller();
                return;
            }

            if (isNeedScrollBack()) {
                doScrollBack(); //在同一张图的回弹  如 滑动距离不够
                return;
            }

            if (Math.abs(offsetX % drawRect.width()) > drawRect.width() / 2) {
                adjustPage(offsetX < 0); //发动距离够大以后的翻页处理
                return;
            }

            if (mIsAdjust) { // 翻页处理后,进行后台的翻页通知 同时讲scroller设置为0
                mIsAdjust = false;
                if (mPageType < 0 && canTurnNext()&&pageSwitchListener!=null) {
                    pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_NEXT);
                }
                if (mPageType > 0 && canTurnPrevious()&&pageSwitchListener!=null) {
                    pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_PREVIOUS);
                }
                resetScroller();
                mPageType = 0;
            }

            return;
        }

        offsetX = scroller.getCurrX();
//        Log.e("=============computeScroll .offsetX="+offsetX);

        drawable.updateView();


    }

    /**
     * 是否需要回滚到当前页
     *
     * @return
     */
    private boolean isNeedScrollBack() {
        return offsetX != 0 && Math.abs(offsetX) < drawRect.width() / 2;
    }

    /**
     * 处理快速滑动
     */
    private void handleFling() {
        mIsFling = false;
        if (offsetX < 0 && canTurnNext()&&pageSwitchListener!=null) {
            pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_NEXT);

        }
        if (offsetX > 0 && canTurnPrevious()&&pageSwitchListener!=null) {
            pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_PREVIOUS);

        }
    }

    /**
     * 重置scroller 使得scroller.getcurX获取到的值是对的
     */
    private void resetScroller() {
        scroller.startScroll(0, 0, 0, 0);
        offsetX = 0;
    }

    /**
     * 滑动距离够大,进行翻页调整
     *
     * @param isLeft 是否向左划  显示下一张
     */
    private void adjustPage(boolean isLeft) {
//        Log.e("adjustPage =========offsetX"+offsetX);
        if (offsetX == 0) {
            return;
        }

        int w = isLeft ? -drawRect.width() : drawRect.width();
        mPageType = w;
        int diaX = (int) (w - offsetX);

        scroller.startScroll(scroller.getCurrX(), 0, diaX, 0);
        mIsAdjust = true;
        drawable.updateView();

    }


    /**
     * 滑动距离不够 回滚处理
     */
    private void doScrollBack() {
        int startX = scroller.getCurrX();
        scroller.startScroll(startX, 0, -startX, 0);
        drawable.updateView();

    }


    private boolean isCancelMove(MotionEvent e1, MotionEvent event) {

        int direction = (int) (event.getX() - e1.getX());
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (direction > 0 && !canTurnPrevious()

                        || direction < 0 && !canTurnNext()
                        ) {
                    doScrollBack();
                    if(pageSwitchListener!=null)
                    pageSwitchListener.onPageTurnFail(direction > 0 ? PageSwitchListener.TURN_PREVIOUS : PageSwitchListener.TURN_NEXT);
                    drawable.updateView();
                    return true;
                }

                break;

        }
        return false;
    }

    /**
     * 处理fling操作
     *
     * @param e1        开始事件
     * @param e2        结束事件
     * @param velocityX 水平每秒滑动的像素
     * @return
     */
    private boolean cancelFlingAndSroll(MotionEvent e1, MotionEvent e2, float velocityX) {
        int direction = (int) (e2.getX() - e1.getX());
        switch (e2.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.e("==============velocityX=" + velocityX + "==============direction=" + direction + "===========e2.getX()=" + e2.getX() + "====scroller.getCurrX()=" + scroller.getCurrX());
                if (Math.abs(direction) > drawRect.width() / 2 || Math.abs(velocityX) > drawRect.width() * 1.5) {
                    mIsFling = true;
                    scroller.forceFinished(true);
                    int startX = scroller.getCurrX();
                    if (Math.abs(startX) == drawRect.width()) {
                        startX = 0;
                    }
                    if (direction < 0) {

                        scroller.startScroll(startX, 0, -drawRect.width() - startX, 0);
                    }

                    if (direction > 0) {

                        scroller.startScroll(startX, 0, (drawRect.width() - startX), 0);
                    }


                    return true;
                }

        }
        return false;

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        boolean handled = false;
        if (pageSwitchListener != null) {
            int x = (int) e.getX();
            int y = (int) e.getY();


            int width = drawParam.width;
            int height = drawParam.height;


            if (x < width / 3) {
                handled = true;
                if(pageSwitchListener!=null) {
                    if (canTurnPrevious()) {
                        pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_PREVIOUS);

                    } else {
                        pageSwitchListener.onPageTurnFail(PageSwitchListener.TURN_PREVIOUS);

                    }
                }

            } else if (x > width * 2 / 3) {
                handled = true;
                if(pageSwitchListener!=null) {
                    if (canTurnNext()) {
                        pageSwitchListener.afterPageChanged(PageSwitchListener.TURN_NEXT);
                    } else {
                        pageSwitchListener.onPageTurnFail(PageSwitchListener.TURN_NEXT);
                    }
                }
            }

        }


        if (handled) return handled;

        // Log.e("onSingleTapConfirmed");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //  Log.e("onDoubleTap");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        //  Log.e("onDoubleTapEvent");
        return false;
    }

    @Override
    public void setOnScrollListener(ScrollListener listener) {

    }
}
