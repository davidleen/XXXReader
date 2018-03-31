package com.xxx.reader.prepare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.core.IPageTurner;
import com.xxx.reader.text.layout.BitmapHolder;
import com.xxx.reader.text.layout.BitmapProvider;


/**
 * Created by davidleen29 on 2017/8/25.
 */

public class DrawLayer {


    IZoomHandler zoomHandler;
    private BitmapProvider drawCache;
    private IDrawable iDrawable;

    private DrawParam drawParam;


    public DrawLayer(Context context, BitmapProvider drawCache, IDrawable iDrawable) {


        this.drawCache = drawCache;
//        zoomHandler = new ZoomHandler(context, iDrawable);
//        zoomHandler.setSimpleOnGestureListener(new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//
//
//                if (isInMenuArea((int) e.getX(), (int) e.getY())) {
//                    clickListener.onMenuAreaClick();
//                }
//
//                return super.onSingleTapConfirmed(e);
//            }
//
//
//        });

        this.iDrawable = iDrawable;

    }

    private boolean isInMenuArea(int x, int y) {
        return x < drawParam.width * 2 / 3 && x > drawParam.width / 3
                &&
                y < drawParam.height * 2 / 3 && y > drawParam.height / 3;


    }

    public void setPageTurner(IPageTurner pageTurner) {
        this.pageTurner = pageTurner;

        if (drawParam != null) {
            pageTurner.updateDrawParam(drawParam);

        }
    }


    IPageTurner pageTurner;


    public boolean onTouchEvent(MotionEvent event) {


        if (zoomHandler != null && zoomHandler.onTouchEvent(event)) return true;

        if (pageTurner != null)
            return pageTurner.onTouchEvent(event);
        return false;

    }

    public void onDraw(Canvas canvas) {


        canvas.save();

        if (zoomHandler != null)
            zoomHandler.zoom(canvas);

        if (pageTurner != null && pageTurner.isInAnimation()) {
            pageTurner.onDraw(canvas);
        } else {

            BitmapHolder currentBitmap = drawCache.getCurrentBitmap();
            Bitmap bitmap = currentBitmap.lockRead();
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
            }

            currentBitmap.unLockRead();


        }


        canvas.restore();

    }


    public void updateDrawParam(DrawParam drawParam) {
        this.drawParam = drawParam;


        if (pageTurner != null)
            pageTurner.updateDrawParam(drawParam);
        if (zoomHandler != null)
            zoomHandler.setSize(drawParam.width, drawParam.height);
    }


}
