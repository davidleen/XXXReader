package com.xxx.reader.core;

/**  翻页切换回调接口
 * Created by davidleen29 on 2017/8/25.
 */

public interface PageSwitchListener {


    int TURN_PREVIOUS = 0x01;
    int TURN_NEXT = 0x02;

    /**
     * 页面切换动画开始判断
     *
     * @param direction
     * @return 是否可以切换
     */
      boolean canPageChanged(int direction);

    /**
     * 页面开始切换前回调接口
     *
     * @param direction
     */
      void beforePageChanged(int direction);

    /**
     * 页面切换结束后回调接口
     *
     * @param direction
     */
      void afterPageChanged(int direction);

    /**
     * 翻页失败时候调用(原因：下载失败，未购买，等)
     * @param turnMoveDirection
     */
    void onPageTurnFail(int turnMoveDirection);
}
