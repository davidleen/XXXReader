package com.xxx.reader.core;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * 翻页动画接口
 * 接受鼠标处理事件，
 * 判断
 * Created by davidleen29 on 2017/8/25.
 */

public interface IPageTurner {
    public static final int TURN_NONE					= 0x00;
    public static final int TURN_PREVIOUS				= 0x01;
    public static final int TURN_NEXT					= 0x02;
    public static final int TURN_NO_PREVIOUS			= 0x41;
    public static final int TURN_NO_NEXT				= 0x42;

    /**
     * 翻页滚动类型 横向滑动
     */
    int PAGE_TURN_SLIDE = 1;
    /**
     * 翻页滚动类型 横向滚动
     */
    int PAGE_TURN_SCROLL = 2;
    int HORIZENTAL = 333;


    boolean isInAnimation();

    void onDraw(Canvas canvas );

    void updateDrawParam(DrawParam drawParam);


    boolean onTouchEvent(MotionEvent event);

    void setOnScrollListener(ScrollListener listener);

    interface ScrollListener {
        void onScrollBegin();

    }
}
