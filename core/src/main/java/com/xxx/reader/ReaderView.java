package com.xxx.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xxx.frame.Log;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.prepare.DrawLayer;

/**
 * Created by davidleen29 on 2018/3/21.
 */

public class ReaderView  extends View implements IDrawable{
    public ReaderView(Context context) {
        super(context);
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setDrawLayer(DrawLayer drawLayer) {
        this.drawLayer = drawLayer;
    }

    DrawLayer drawLayer;



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(drawLayer!=null)
        {
            if(drawLayer.onTouchEvent(event)) return true;
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawLayer!=null)
        {
            drawLayer.onDraw(canvas);
        }



    }




    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        DrawParam drawParam=new DrawParam();
        drawParam.width=w;
        drawParam.height=h;

        drawParam.padding=new int[]{30,80,30,80};
        if(drawLayer!=null)
        {
            drawLayer.updateDrawParam(drawParam);
        }


    }

    @Override
    public void updateView() {
        postInvalidate();
    }


    @Override
    public boolean onTrackballEvent(MotionEvent event) {


        Log.e(event);
        return false;
    }
}
