package com.xxx.reader.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.xxx.reader.text.layout.BitmapHolder;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by davidleen29 on 2018/3/21.
 */

public class AbstractBitmapHolder implements BitmapHolder {


    Bitmap bitmap = null;
    Canvas canvas;
    ReentrantReadWriteLock lock;

    public AbstractBitmapHolder(int width, int height) {
        lock = new ReentrantReadWriteLock();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);

    }

    @Override
    public Bitmap lockRead() {

        lock.readLock().lock();


        return bitmap;
    }

    @Override
    public void unLockRead() {

        lock.readLock().unlock();

    }


    @Override
    public Bitmap lockWrite() {

        lock.writeLock().lock();


        return bitmap;
    }

    @Override

    public void unLockWrite() {

        lock.writeLock().unlock();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public Canvas lockCanvas() {

        lock.writeLock().lock();


        return canvas;

    }

    @Override
    public void unLockCanvas() {
        lock.writeLock().unlock();
    }
}
