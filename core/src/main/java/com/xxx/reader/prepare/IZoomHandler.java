package com.xxx.reader.prepare;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by davidleen29 on 2017/11/24.
 */

public interface IZoomHandler {
    void zoom(Canvas canvas);

    void clear();

    boolean onTouchEvent(@NonNull MotionEvent ev);

    void setSize(int width, int height);

    void addOnScaleGestureListener(ScaleGestureDetector.SimpleOnScaleGestureListener listener);

    void removeOnScaleGestureListener(ScaleGestureDetector.SimpleOnScaleGestureListener listener);

    void removeOnScaleGestureListeners();

    void setSimpleOnGestureListener(GestureDetector.SimpleOnGestureListener listener);

    void removeOnSimpleOnGestureListener(GestureDetector.SimpleOnGestureListener listener);

    void removeOnSimpleOnGestureListeners();

    void setOnZoomListener(OnZoomListener listener);

    void removeOnZoomListener( OnZoomListener listener);

    public interface OnZoomListener {

        void onViewZoomUpdate(ValueAnimator animation, float translateX, float translateY,
                              float scaleX, float scaleY);

        void onViewStart();

        void onViewCancel();
    }
}
