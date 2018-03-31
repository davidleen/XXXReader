package com.xxx.reader.text.layout;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by HP on 2018/3/20.
 */

public interface Reader {

      void layout();
     void draw(Canvas canvas);

    boolean onTouchEvent(MotionEvent event);

}
