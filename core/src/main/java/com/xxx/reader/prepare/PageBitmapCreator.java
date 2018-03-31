package com.xxx.reader.prepare;


import com.xxx.reader.core.PageBitmap;

/**
 * Created by HP on 2017/8/30.
 */

public interface  PageBitmapCreator<P extends PageBitmap>     {

    P create();

}
