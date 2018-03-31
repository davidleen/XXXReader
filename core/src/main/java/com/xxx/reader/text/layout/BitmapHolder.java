package com.xxx.reader.text.layout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by davidleen29 on 2018/3/21.
 */

public interface BitmapHolder {


    Bitmap lockRead();


    void unLockRead( );


    Bitmap lockWrite();


    void unLockWrite( );


      boolean onTouchEvent(MotionEvent event);


    int getWidth();
    int getHeight();



    Canvas lockCanvas();
    void unLockCanvas();
}
