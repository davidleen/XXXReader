package test.changu.com.customreader;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 
 * 
 * <br>Created 2016年7月28日 下午3:15:39
 * @version  
 * @author   davidleen29		
 *
 * @see
 */
public class TextDrawViewer implements OnTouchListener,  GestureDetector.OnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
        
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
       
        
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
         
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        
        
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
         
        return false;
    }
    
    
    
    

}
