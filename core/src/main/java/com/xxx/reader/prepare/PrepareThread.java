package com.xxx.reader.prepare;


import com.xxx.frame.Log;
import com.xxx.reader.core.DestroyableThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据准备线程，
 * Created by davidleen29 on 2017/8/25.
 */

public class PrepareThread extends DestroyableThread {


    private PrepareLayer prepareLayer;
    private int maxCacheSize;
    //每个页面的准备顺序   当前  下一页 ， 上一页 ，下下页，  上上页
    int[] indexOfPage;// = new int[]{0, 1, -1, 2, -2}

    public PrepareThread(PrepareLayer prepareLayer, int maxCacheSize) {

        this.prepareLayer = prepareLayer;
        this.maxCacheSize = maxCacheSize;

        indexOfPage = new int[maxCacheSize];
        for (int i = 0; i < maxCacheSize; i++) {

            indexOfPage[i] = ((int) Math.pow(-1, i + 1)) * ((i + 1) / 2);
        }
        String result = "";
        for (int i = 0; i < maxCacheSize; i++) {
            result += indexOfPage[i] + ",";
        }
        Log.e("indexOfPage:" + result);

    }


    public AtomicBoolean skip = new AtomicBoolean();

    private static final int SLEEP_DURATION = Integer.MAX_VALUE;


    @Override
    protected void runOnThread() {


        skip.set(false);


        for (int index : indexOfPage) {
            prepareLayer.preparePage(index, skip);
        }


    }


    public void setSkip(boolean skip) {
        this.skip.set(skip);
    }
}
