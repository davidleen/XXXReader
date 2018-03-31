package com.xxx.reader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by HP on 2018/3/21.
 */

public class Utils {

private static Context baseContext;

    /**
     * 获取android设备真实的屏幕宽高。
     *
     * @return
     */
    public static int[] getScreenDimension(Activity activity) {



         int[]   wh = new int[2];
            if (Build.VERSION.SDK_INT >= 17) {

                Point size = new Point();
                try {

                    activity.getWindowManager().getDefaultDisplay().getRealSize(size);
                    wh[0] = size.x;
                    wh[1] = size.y;
                } catch (NoSuchMethodError e) {
                   e.printStackTrace();
                }

            } else {

                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();

                wh[0] = metrics.widthPixels;
                wh[1] = metrics.heightPixels;
            }


        return wh;
    }




    public static int dipDimensionInteger(float value){
        return (int)(dipDimensionFloat(value)+0.5f);
    }

    public static float dipDimensionFloat(float value) {
        return baseContext == null ? value : TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, baseContext.getResources()
                        .getDisplayMetrics());
    }


    public static void init(Context context)
    {
        baseContext=context;
    }

    public static boolean isLandscape() {

        DisplayMetrics displayMetrics = baseContext.getResources().getDisplayMetrics();
        return  displayMetrics.widthPixels > displayMetrics.heightPixels;
    }



    /**
     * sp 转换px
     *
     * @param sp
     * @return
     */
    public static float sp2px(float sp) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, baseContext.getResources().getDisplayMetrics());


    }


    public static int dp2px(Context context, float dp) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
