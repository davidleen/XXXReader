package com.xxx.reader;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.NestedScrollingChild;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.xxx.reader.core.IDrawable;
import com.xxx.reader.text.VeticalAdapter;

/**
 * Created by HP on 2018/3/19.
 */

public class VerticalView<T extends Frame>  extends View implements IDrawable, GestureDetector.OnGestureListener,NestedScrollingChild {


    private DataSetObserver dataSetObserver=new DataSetObserver(){

        @Override
        public void onChanged() {
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };
    private VeticalAdapter<T> verticalAdapter;

    public VerticalView(Context context) {
        super(context);
        setAdapter(new VeticalAdapter<T>());



    }

    private void setAdapter(VeticalAdapter<T> verticalAdaspter)
    {
        if(this.verticalAdapter !=null)
        {
            verticalAdaspter.unregisterDataSetObserver(dataSetObserver);
        }
        this.verticalAdapter = verticalAdaspter;

        this.verticalAdapter.registerDataSetObserver(dataSetObserver);
    }



    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void updateView() {

    }
}
