package com.xxx.reader.text.layout;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.xxx.reader.prepare.DrawLayer;


/**
 * Created by davidleen29 on 2018/3/20.
 */

public class PageImpl  implements Reader{



    private DrawLayer drawLayer;

    @Override
    public void layout() {







    }

    @Override
    public void draw(Canvas canvas) {











    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(drawLayer.onTouchEvent(event))
            return true;
//        if(prepareLayer.onTouchEvent(event))
//            return true;
       return false;


    }
}
