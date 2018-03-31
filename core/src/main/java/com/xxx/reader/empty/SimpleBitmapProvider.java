package com.xxx.reader.empty;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;


import com.xxx.reader.core.AbstractBitmapHolder;
import com.xxx.reader.text.layout.BitmapHolder;
import com.xxx.reader.text.layout.BitmapProvider;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by HP on 2018/3/21.
 */

public class SimpleBitmapProvider implements BitmapProvider {


    private final int width;
    private final int height;


    BitmapHolder[] bitmapHolders = new BitmapHolder[3];

    public SimpleBitmapProvider(Context context, int width, int height) {


        this.width = width;
        this.height = height;
        for (int i = 0; i < 3; i++) {
            bitmapHolders[i] = new AbstractBitmapHolder(width, height);


            Bitmap bitmap = bitmapHolders[i].lockWrite();

            if (bitmap != null) {

                Canvas canvas = new Canvas(bitmap);
                InputStream open = null;
                try {
                    open = context.getAssets().open("test" + i + ".jpg");
                    canvas.drawBitmap(BitmapFactory.decodeStream(open), null, new Rect(0, 0, width, height), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            bitmapHolders[i].unLockWrite();


        }


    }

    @Override
    public BitmapHolder getBitmap(int index) {
        return bitmapHolders[index];
    }

    @Override
    public BitmapHolder getCurrentBitmap() {
        return bitmapHolders[1];
    }

    @Override
    public BitmapHolder getNextBitmap() {
        return bitmapHolders[2];
    }

    @Override
    public BitmapHolder getPreviousBitmap() {
        return bitmapHolders[0];
    }
}
