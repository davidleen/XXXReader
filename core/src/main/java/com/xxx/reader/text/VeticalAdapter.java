package com.xxx.reader.text;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.xxx.reader.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018/3/19.
 */

public class VeticalAdapter<T extends Frame> {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    List<T> bitmapFrames;
    private int scrollYStart;
    private int scrollYEnd;

    public VeticalAdapter()
    {
        this(new ArrayList<T>());
    }
    public VeticalAdapter(List<T> bitmapFrames) {
        this.bitmapFrames = bitmapFrames;
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);

    }


    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }
    public void addFrames(List<T> frames, boolean fromTop)
    {



        int height=0;
        for(T  bitmapFrame:frames)
        {

            height+=bitmapFrame.getLacation()[3]-bitmapFrame.getLacation()[1];

        }
        if(fromTop)
        {

            bitmapFrames.addAll(0,frames);


            scrollYStart-=height;
            notifyDataSetChanged();

        }else {
            bitmapFrames.addAll(frames);

            scrollYEnd+=height;
            notifyDataSetChanged();
        }









    }
}
