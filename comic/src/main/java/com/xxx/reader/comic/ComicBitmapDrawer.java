package com.xxx.reader.comic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.MotionEvent;

import com.xxx.frame.Log;
import com.xxx.frame.MathUtils;
import com.xxx.frame.StringUtil;
import com.xxx.reader.ThreadConst;
import com.xxx.reader.Utils;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.download.DownloadListener;
import com.xxx.reader.prepare.Cancelable;
import com.xxx.reader.text.layout.BitmapHolder;

import java.io.File;
import java.io.IOException;

/**
 * 漫画图片绘制功能类
 * <p>
 * 对canvas 进行操作
 * Created by davidleen29 on 2017/11/2.
 */

public class ComicBitmapDrawer {
    private LoadTask loadTask;
    public static final int STATE_DRAWING = 1;
    public static final int STATE_ERROR = 3;
    public static final int STATE_OK = 2;
    public static final int STATE_CACHE = 4;
    public static final int STATE_EMPTY = 5;
    volatile int state = 0;

    public BitmapFactory.Options options = null;
    public Rect decodeRect = new Rect();
    public Rect drawRect = new Rect();
    public Rect sourceRect = new Rect();


    public Bitmap bitmap;

    /**
     * 加载失败， 重新加载显示区域
     */
    public Rect errorRect = new Rect();

    private ComicPageInfo.BitmapFrame bitmapFrame;

    private LoadCancel cancel;
    private int rotate;
    Bitmap loadBitmap;


    private boolean pressed = false;

    private class LoadCancel implements Cancelable {

        volatile boolean canceld = false;
        public String url;

        @Override
        public boolean isCancelled() {
            return false;
        }
    }


    /**
     * 下载进度值  只有在 state=STATE_DRAWING时 【0,1000】
     */
    private int progress;
    int textSize;
    private int inSampleSize = 1;
    Paint paint;

    private Context context;

    private IDrawable iDrawable;
    private DownloadListener listener;

    float textWidth = 0;

    public static final String RELOAD_TEXT = "重新加载";


    /**
     * 缩略图是原图的大小倍数
     */
    private static final int SCALE_SIZE_OF_ORIGIN = 4;
    int reloadTextLength;

    //    Bitmap tempBitmap;
//    Canvas tempCanvas;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ComicBitmapDrawer(Context context, BitmapHolder holder, IDrawable iDrawable, DownloadListener listener) {


        this.context = context;

        this.iDrawable = iDrawable;
        this.listener = listener;

        int[] wh = Utils.getScreenDimension((Activity) context);
        try {
            bitmap = Bitmap.createBitmap(wh[0], wh[1], Bitmap.Config.RGB_565);
        } catch (Throwable e) {
            System.gc();
            bitmap = Bitmap.createBitmap(wh[0], wh[1], Bitmap.Config.RGB_565);
        }

        options = new BitmapFactory.Options();
        loadBitmap = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.tag_loading_3)).getBitmap();

//        tempBitmap= Bitmap.createBitmap(wh[0], wh[1], Bitmap.Config.RGB_565);
//        tempCanvas=new Canvas(tempBitmap);
        options.inBitmap = bitmap;

        options.inScaled = true;

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        textSize = (int) Utils.sp2px(14);
        paint.setTextSize(textSize);

        float[] temp = new float[1];
        paint.getTextWidths("测", temp);
        textWidth = temp[0];


        reloadTextLength = (int) (RELOAD_TEXT.length() * textWidth);


    }

    public void draw(Canvas canvas, int width, int height) {

        canvas.drawColor(Color.BLACK);
        switch (state) {

            case STATE_EMPTY:


                break;
            case STATE_OK:

                sourceRect.set(0, 0, decodeRect.width() / inSampleSize, decodeRect.height() / inSampleSize);

//                tempCanvas.drawBitmap(bitmap, sourceRect, drawRect, null);

                canvas.drawBitmap(bitmap, sourceRect, drawRect, null);


                break;
            case STATE_CACHE:

                //缩略图------- 目前未启用

//                sourceRect.set(decodeRect.left / (inSampleSize * SCALE_SIZE_OF_ORIGIN), decodeRect.top / (inSampleSize * SCALE_SIZE_OF_ORIGIN), decodeRect.right / (inSampleSize * SCALE_SIZE_OF_ORIGIN), decodeRect.bottom / (inSampleSize * SCALE_SIZE_OF_ORIGIN));
//
//                Bitmap scaleBitmap = ComicThumbnailFactory.getInstance().getScaleCache(bitmapFrame.filePath);
//                if (scaleBitmap != null && !scaleBitmap.isRecycled())
//                    canvas.drawBitmap(scaleBitmap, sourceRect, drawRect, null);

                //缩略图------- 目前未启用
                break;


            case STATE_ERROR: {


                int left = ((width - reloadTextLength) / 2);
                int top = height / 2;


                paint.setColor(pressed ? Color.DKGRAY : Color.LTGRAY);

                canvas.drawText(RELOAD_TEXT, left, top, paint);


                errorRect.set((left - (int) textWidth), top - textSize * 2, left + reloadTextLength + (int) textWidth, top + textSize);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(errorRect, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.LTGRAY);
            }
            break;
            case STATE_DRAWING: {


                if (progress > 0 && progress < 1000) {

                    int left = (int) ((width - 2 * textWidth) / 2);
                    int top = height / 2;
                    canvas.drawText(progress / 10 + "%", left - 10, top, paint);

                } else {
                    int left = (width - loadBitmap.getWidth()) / 2;
                    int top = (height - loadBitmap.getHeight()) / 2;
                    rotate += 10;
                    rotate %= 360;

                    canvas.save();
                    canvas.rotate(rotate, width / 2, height / 2);
                    canvas.drawBitmap(loadBitmap, left, top, null);
                    canvas.restore();
                }
                iDrawable.updateView();
            }

            break;
        }
    }

    public void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }


    private int downX;
    private int downY;
    boolean inArea = false;
    boolean hasScroll = false;

    /**
     * 事件处理
     *
     * @param event
     * @return
     */


    public boolean onTouchEvent(MotionEvent event) {

        if (state != STATE_ERROR) return false;
        pressed = false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                downX = x;
                downY = y;
                inArea = errorRect.contains(x, y);
                hasScroll = false;
                if (inArea) {
                    pressed = true;
                    iDrawable.updateView();
                    return true;
                }


                break;
            case MotionEvent.ACTION_MOVE: {

                if (inArea) {
                    if (!hasScroll && (Math.abs(downX - x) > 10 || Math.abs(downY - y) > 10)) {
                        hasScroll = true;
                    }
                    return true;
                }


                break;

            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                iDrawable.updateView();
                if (inArea && !hasScroll && Math.abs(downX - x) < 5 && Math.abs(downY - y) < 5) {
                    inArea = errorRect.contains(x, y);
                    if (inArea) {

                        reload();
                        return true;
                    }


                }


                break;
        }

        return false;
    }


    /**
     * 重新加载
     */
    private void reload() {


        bindBitmapFrame(bitmapFrame);
    }

    private void update(ComicPageInfo.BitmapFrame abitmapFrame) {


        if (loadTask != null && loadTask.getStatus() == AsyncTask.Status.RUNNING && loadTask.filePath.equals(bitmapFrame.filePath))
            return;
        if (loadTask != null) loadTask.cancel(true);
        this.bitmapFrame = abitmapFrame;


        loadTask = new LoadTask(this,  iDrawable, bitmapFrame.filePath, decodeRect,  options);
        loadTask.executeOnExecutor(ThreadConst.THREAD_POOL_EXECUTOR);


    }

    public void setBitmapFrame(final ComicPageInfo.BitmapFrame aBitmapFrame) {


        if (this.bitmapFrame == aBitmapFrame) {
            return;
        }
        bindBitmapFrame(aBitmapFrame);
    }

    private void bindBitmapFrame(final ComicPageInfo.BitmapFrame aBitmapFrame) {


        this.bitmapFrame = aBitmapFrame;


        if (cancel != null && (bitmapFrame == null || !bitmapFrame.url.equals(cancel.url))) {
            cancel.canceld = true;
            cancel = null;
        }


        if (bitmapFrame == null) {


            state = STATE_EMPTY;

            return;
        }


        decodeRect.set(0, bitmapFrame.offset, bitmapFrame.width, bitmapFrame.offset + bitmapFrame.length);

        drawRect.set(bitmapFrame.left, bitmapFrame.top, bitmapFrame.right, bitmapFrame.bottom);

        //计算图片压缩比值
        int scaleWSize = (decodeRect.width() - 1) / bitmap.getWidth() + 1;
        int scaleHSize = (decodeRect.height() - 1) / bitmap.getHeight() + 1;
        inSampleSize = MathUtils.getAccurateSampleSize(Math.max(scaleWSize, scaleHSize));
        options.inSampleSize = inSampleSize;


        boolean pictureExist = !StringUtil.isEmpty(bitmapFrame.filePath) && new File(bitmapFrame.filePath).exists();

        if (pictureExist) {

//
//            if (ComicThumbnailFactory.getInstance().hasScaleCache(bitmapFrame.filePath)) {
//
//
//                state = STATE_CACHE;
//
//
//            } else
            state = STATE_DRAWING;

            update(bitmapFrame);

        } else {
            //showloading
            //add loading

            cancelTask();
            state = STATE_DRAWING;
            cancel = new LoadCancel();
            progress = -1;
            if (listener != null) {
                listener.startDownload(bitmapFrame.url, bitmapFrame.filePath, cancel, new DownloadListener.NotifyListener() {
                    @Override
                    public void onComplete(String url, String filePath) {
                        if (bitmapFrame != null && url.equals(bitmapFrame.url) && filePath.equals(bitmapFrame.filePath)) {
                            progress = -1;
                            update(bitmapFrame);
                        }
                    }

                    @Override
                    public void onProgress(String url, String filePath, long bytesLoaded, long maxSize, float process) {

                        if (bitmapFrame != null && url.equals(bitmapFrame.url)) {
                            ComicBitmapDrawer.this.progress = progress;
                        }

                    }

                    @Override
                    public void onError(String url, String filePath) {
                        if (bitmapFrame != null && url.equals(bitmapFrame.url)) {
                            state = STATE_ERROR;
                            iDrawable.updateView();

                        }
                    }

                    @Override
                    public void onCancel(String url, String filePath) {

                    }
                });
            }


        }

        iDrawable.updateView();

    }


    public static class LoadTask extends AsyncTask {

        public String filePath;
        public Rect decodeRect;

        private BitmapFactory.Options options;
        private ComicBitmapDrawer comicBitmapDrawer;

        public IDrawable iDrawable;


        public LoadTask(ComicBitmapDrawer comicBitmapDrawer, IDrawable iDrawable, String filePath, Rect decodeRect,BitmapFactory.Options options) {
            this.comicBitmapDrawer = comicBitmapDrawer;


            this.iDrawable = iDrawable;
            this.filePath = filePath;
            this.decodeRect = decodeRect;

            this.options = options;
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        @Override
        protected Object doInBackground(Object[] params) {

            if (isCancelled()) {
                return null;
            }


//            //缩略图缓存处理。这里执行图片抓取缓存。 会导致大量的内存消耗，造成卡顿。
//            synchronized (ComicThumbnailFactory.class) {
//                if (!ComicThumbnailFactory.getInstance().hasScaleCache(filePath)) {
//                    int orignSampleSize = options.inSampleSize;
//                    Bitmap inbitmap = options.inBitmap;
//                    options.inBitmap = null;
//                    options.inSampleSize *= SCALE_SIZE_OF_ORIGIN;
//                    Bitmap scaleBitmap = BitmapFactory.decodeFile(filePath, options);
//
//                    options.inBitmap = inbitmap;
//                    options.inSampleSize = orignSampleSize;
//
//
//                    ComicThumbnailFactory.getInstance().setScaleCache(filePath, scaleBitmap);
//
//
//                }
//            }
            if (isCancelled()) {
                return null;
            }


            Bitmap bitmap = null;
            BitmapRegionDecoder bitmapRegionDecoder = null;
            try {
                bitmapRegionDecoder = BitmapRegionDecoder.newInstance(filePath, true);
                bitmap = bitmapRegionDecoder.decodeRegion(decodeRect, options);
                bitmapRegionDecoder.recycle();


                if (isCancelled()) {
                    return null;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            return bitmap;


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("draw  Cancel  +" + filePath);
        }

        @Override
        protected void onPostExecute(Object o) {

            if (o == null) return;
            if (o instanceof Bitmap) {


                comicBitmapDrawer.state = STATE_OK;
                iDrawable.updateView();


            }


        }
    }


}
