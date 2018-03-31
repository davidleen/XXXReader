package com.xxx.reader.core;

import com.xxx.frame.Log;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by HP on 2018/3/30.
 */

public abstract class DestroyableThread extends Thread {

    public AtomicBoolean destroy = new AtomicBoolean();

    private static final int SLEEP_DURATION = Integer.MAX_VALUE;
    /**
     * 标记 当前绘制界面是否销毁 是 直接退出死循环
     */
    public void setDestroy() {

        destroy.set(true);
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public final void run() {


        while (true) {

            // 绘制停止 退出循环
            if (destroy.get()) {
                break;
            }


            long time = Calendar.getInstance().getTimeInMillis();

            runOnThread();


            Log.e("time use in prepare:" + (Calendar.getInstance().getTimeInMillis() - time));


            //线程进入睡眠等待状态。调用update 中断睡眠，可以继续循环。
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("interrupt for preparing");
            }


        }
    }

    protected abstract void runOnThread();
}
