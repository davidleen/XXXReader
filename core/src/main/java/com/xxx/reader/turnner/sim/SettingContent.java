package com.xxx.reader.turnner.sim;

/**
 * Created by HP on 2018/3/22.
 */

public class SettingContent {
    private static  SettingContent settingContent = new SettingContent();
    public static int MODE_NIGHT=1;
    public static int MODE_DAY=0;

    public static SettingContent getInstance() {
        return settingContent;
    }

    public int getDayNeightMode() {
        return MODE_DAY;
    }
}
