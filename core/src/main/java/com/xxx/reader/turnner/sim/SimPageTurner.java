package com.xxx.reader.turnner.sim;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;

import com.xxx.frame.Log;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.core.IDrawable;
import com.xxx.reader.core.PageSwitchListener;
import com.xxx.reader.text.layout.BitmapHolder;
import com.xxx.reader.text.layout.BitmapProvider;
import com.xxx.reader.turnner.AbsPageTurner;

import static com.xxx.reader.turnner.sim.PageTurnHelper.GLIDE_MULTIPLE;
import static com.xxx.reader.turnner.sim.PageTurnHelper.PAGGING_SLOP;
import static com.xxx.reader.turnner.sim.PageTurnHelper.TOUCH_SLOP;


public class SimPageTurner extends AbsPageTurner {
    public static final int TURN_NONE = 0x00;
    public static final int TURN_PREVIOUS = 0x01;
    public static final int TURN_NEXT = 0x02;
    public static final int TURN_NO_PREVIOUS = 0x41;
    public static final int TURN_NO_NEXT = 0x42;

    private static int DELAY_MILLIS = 800;
    private static int DELAY_MILLIS_PREVIOUS = 800;
    private final static int DELAY_MILLIS_NONE = 800;

    private static int DELAY_MILLIS_PREVIOUS_SLIDE = 500;
    private static int DELAY_MILLIS_SLIDE = 600;
    private Shape mShape = new Shape(320, 480);          //外形

    private PointF mCorner = new PointF();                 //拖拽点对应的页脚
    private PointF mMiddle = new PointF();

    private PointF mTouchDown = new PointF(0.0f, 0.0f);
    private PointF mTouchMove = new PointF(0.01f, 0.01f);     //拖拽点,不让x,y为0,否则在点计算时会有问题
    private PointF mTouchUp = new PointF(0.0f, 0.0f);

    private PageTurnHelper.Bezier mBezierHorizontal = new PageTurnHelper.Bezier();                 //贝塞尔曲线
    private PageTurnHelper.Bezier mBezierVertical = new PageTurnHelper.Bezier();                 //另一条贝塞尔曲线

    private float mDegrees;
    private float mTouch2Corner;
    private float mDiagonal;                                                            //对角线

    private boolean mIsRtLb;                                                            //是否属于右上左下

    private Paint mPaint;

    private int motionEvent;
    private boolean isMotionMoveSetting;

    private int direction = TURN_NONE;
    private boolean isDirectionSetting;
    private boolean isPagging;

    private boolean isHorizonTurnning;

    private PointF mCornerTop = new PointF(0.0f, 0.0f);       //拖拽点对应的页脚
    private PointF mCornerBottom = new PointF(0.0f, 0.0f);
    private PointF mFoldTop = new PointF(0.0f, 0.0f);
    private PointF mFoldBottom = new PointF(0.0f, 0.0f);
    private PointF mTouchTop = new PointF(0.0f, 0.0f);
    private PointF mTouchBottom = new PointF(0.0f, 0.0f);

    private View additiveView;
    private boolean needSpeedUp = false;


    public SimPageTurner(Context context, PageSwitchListener pageSwitchListener, IDrawable drawable, BitmapProvider bitmapProvider) {
        super(context, pageSwitchListener, drawable, bitmapProvider);


//          mBitmap = Bitmap.createBitmap(mShape.width, mShape.height, Bitmap.Config.ARGB_4444);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0.0f);

    }

    @Override
    public void updateDrawParam(DrawParam drawParam) {
        super.updateDrawParam(drawParam);

        int width = drawParam.width;
        int height = drawParam.height;
        if(drawParam.padding!=null)
        {
            width-=(drawParam.padding[0]+drawParam.padding[2]);
            height-=(drawParam.padding[1]+drawParam.padding[3]);
        }
        setShape(width, height);
    }


    private PointF lastTouch = new PointF();
    private PointF firstTouch = new PointF();
    private boolean isTouched;
    private int turnMoveDirection;

    @Override
    public boolean onTouchEvent(MotionEvent event) {



        if(pageSwitchListener==null) return false;



        int action = event.getAction() & MotionEvent.ACTION_MASK;
        float eX = event.getX(), eY = event.getY();
        switch (action) {
            case MotionEvent.ACTION_UP:
                isTouched=false;
                setMotionEvent(MotionEvent.ACTION_UP);
                setTouchUp(eX, eY, true);

               int direction=   getDragTag();
                startAnimation(direction);
                break;

            case MotionEvent.ACTION_CANCEL:
                isTouched=false;
                break;

            case MotionEvent.ACTION_MOVE:
                setMotionEvent(MotionEvent.ACTION_MOVE);
                lastTouch.x = eX;
                lastTouch.y = eY;

                setTouch(eX, eY, true);

                break;

            case MotionEvent.ACTION_DOWN:

                isTouched=true;
                firstTouch.x=eX;
                firstTouch.y=eY;
                lastTouch.x = eX;
                lastTouch.y = eY;
                setTouchDown(eX, eY, true);
                turnMoveDirection = getTurnMoveDirection(eX, eY, action);

                break;

            default:
                break;
        }

        drawable.updateView();
        return true;

    }

    public void setSpeedUpState(boolean isEnable) {
        needSpeedUp = isEnable;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void setShape(int width, int height) {


        mShape.height = height;
        mShape.width = width;
        mShape.isLandscape = width > height;

        if (isXXhdpi()) {
            DELAY_MILLIS = 850;
            DELAY_MILLIS_PREVIOUS = 850;
        }

        mDiagonal = (float) mShape.getDiagonal();
    }


    public int getPaggingDelayMillis(int turn) {
        int millis = 0;

        if (turn == TURN_NO_PREVIOUS || turn == TURN_NO_NEXT) {
            millis = DELAY_MILLIS_NONE;
        } else if (turn == TURN_PREVIOUS) {
            millis = DELAY_MILLIS_PREVIOUS;
        } else {
            millis = DELAY_MILLIS;
        }

        return millis;
    }

    public int getPaggingDelayMillisSlide(int turn) {
        int millis = 0;

        if (turn == TURN_NO_PREVIOUS || turn == TURN_NO_NEXT) {
            millis = DELAY_MILLIS_NONE;
        } else if (turn == TURN_PREVIOUS) {
            millis = DELAY_MILLIS_PREVIOUS_SLIDE;
        } else {
            millis = DELAY_MILLIS_SLIDE;
        }

        return millis;
    }

    public void setTouch(float x, float y, boolean isOnTouch) {
        isHorizonTurnning = PageTurnHelper.coordinateTouchMove(mShape, mTouchMove, mTouchDown, x, y, isOnTouch);
        setPagging(!isOnTouch);

        if (motionEvent == MotionEvent.ACTION_DOWN ||
                !isMotionMoveSetting && motionEvent == MotionEvent.ACTION_MOVE) {//touch move时只能改变mCorner一次.
            PageTurnHelper.calculateCorner(mCorner, mTouchDown, mTouchMove, mShape);
            mIsRtLb = PageTurnHelper.isRightTopOrLeftBottom(mShape, mCorner);
            isMotionMoveSetting = motionEvent == MotionEvent.ACTION_MOVE;
        }
    }

    public void setTouchSlide(float x, float y) {
        PageTurnHelper.coordinateTouchMoveSlide(mTouchMove, x, y);

        if (motionEvent == MotionEvent.ACTION_DOWN ||
                !isMotionMoveSetting && motionEvent == MotionEvent.ACTION_MOVE) {//touch move时只能改变mCorner一次.
            PageTurnHelper.calculateCornerSlide(mCorner, mTouchMove);
            mIsRtLb = PageTurnHelper.isRightTopOrLeftBottom(mShape, mCorner);
            isMotionMoveSetting = motionEvent == MotionEvent.ACTION_MOVE;
        }
    }

    public void setTouchRolling(float x, float y) {
        PageTurnHelper.coordinateTouchMoveRolling(mTouchMove, x, y);

        if (motionEvent == MotionEvent.ACTION_DOWN ||
                !isMotionMoveSetting && motionEvent == MotionEvent.ACTION_MOVE) {//touch move时只能改变mCorner一次.
            PageTurnHelper.calculateCornerSlide(mCorner, mTouchMove);
            mIsRtLb = PageTurnHelper.isRightTopOrLeftBottom(mShape, mCorner);
            isMotionMoveSetting = motionEvent == MotionEvent.ACTION_MOVE;
        }
    }

    public PointF getCorner() {
        return mCorner;
    }

    public float getTouchMoveY() {
        return mTouchMove.y;
    }

    public void setTouchDown(float x, float y, boolean isOnTouch) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        setLastTurnDirection(TURN_NONE);
        motionEvent = MotionEvent.ACTION_DOWN;

        isHorizonTurnning = PageTurnHelper.coordinateTouchMove(mShape, mTouchDown, null, x, y, isOnTouch);
        setPagging(!isOnTouch);
    }

    public void setTouchDownSlide(float x, float y) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        setLastTurnDirection(TURN_NONE);
        motionEvent = MotionEvent.ACTION_DOWN;
        PageTurnHelper.coordinateTouchMoveSlide(mTouchDown, x, y);
    }

    public void setTouchDownRolling(float x, float y) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        setLastTurnDirection(TURN_NONE);
        motionEvent = MotionEvent.ACTION_DOWN;
        PageTurnHelper.coordinateTouchMoveRolling(mTouchDown, x, y);
    }

    public PointF getTouchDown() {
        return mTouchDown;
    }

    public PointF getTouchUp() {
        return mTouchUp;
    }

    public void setTouchUp(float x, float y, boolean isOnTouch) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        motionEvent = MotionEvent.ACTION_UP;

        isHorizonTurnning = PageTurnHelper.coordinateTouchMove(mShape, mTouchUp, mTouchDown, x, y, isOnTouch);
        setPagging(!isOnTouch);
    }

    public void setTouchUpSlide(float x, float y) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        motionEvent = MotionEvent.ACTION_UP;
        PageTurnHelper.coordinateTouchMoveSlide(mTouchUp, x, y);
    }

    public void setTouchUpRolling(float x, float y) {
        isDirectionSetting = false;
        isMotionMoveSetting = false;
        motionEvent = MotionEvent.ACTION_UP;
        PageTurnHelper.coordinateTouchMoveRolling(mTouchUp, x, y);
    }

    public void setMotionEvent(int motionEvent) {
        this.motionEvent = motionEvent;
    }

    /**
     * 翻页趋势
     *
     * @param x
     * @param y
     * @return
     */
    public int getTurnMoveDirection(float x, float y, int event) {
        int turn = TURN_NONE;

        float difference = mTouchDown.x - x;
        if (event == MotionEvent.ACTION_DOWN ||
                !isDirectionSetting && event == MotionEvent.ACTION_MOVE && difference != 0) {
            if (difference < -PAGGING_SLOP) {//PREVIOUS
                turn = TURN_PREVIOUS;
            } else if (difference > PAGGING_SLOP) {//NEXT
                turn = TURN_NEXT;
            } else if (x < mShape.width / 3) {

                turn = TURN_PREVIOUS;

            } else {
                turn = TURN_NEXT;
            }

            setLastTurnDirection(turn);
            isDirectionSetting = event == MotionEvent.ACTION_MOVE;
        } else {
            turn = getLastTurnDirection();
        }

        return turn;
    }

    public int getTurnMoveDirection(float x, float y) {

        return getTurnMoveDirection(x, y, motionEvent);
    }

    /**
     * 上一touch时翻页趋势
     *

     * @return
     */
    public int getLastTurnDirection() {

        return direction;
    }

    public void setLastTurnDirection(int direction) {
        this.direction = direction;
    }

    /**
     * 是否可以翻过去
     *
     * @return
     */
    public int getDragTag() {
        int turn = TURN_NONE;

        int lastTurn = getLastTurnDirection();
        float difference = mTouchDown.x - mTouchUp.x;
        if (lastTurn == TURN_PREVIOUS && difference < -mShape.width / GLIDE_MULTIPLE) {//当滑动的长度是屏幕的宽的1/12,代表可以翻页
            turn = TURN_PREVIOUS;
        } else if (lastTurn == TURN_PREVIOUS && difference > PAGGING_SLOP) {//反向
            turn = TURN_NO_PREVIOUS;
        } else if (lastTurn == TURN_NEXT && difference > mShape.width / GLIDE_MULTIPLE) {
            turn = TURN_NEXT;
        } else if (lastTurn == TURN_NEXT && difference < -PAGGING_SLOP) {//反向
            turn = TURN_NO_NEXT;
        } else if (lastTurn == TURN_PREVIOUS && mTouchUp.x < mShape.width / 3) { //mShape.width>>1
            turn = TURN_PREVIOUS;
        } else if (lastTurn == TURN_PREVIOUS && mTouchUp.x > mShape.width / 3) { //mShape.width>>1
            turn = TURN_NO_PREVIOUS;
        } else if (lastTurn == TURN_NEXT && mTouchUp.x > mShape.width / 3) { //mShape.width>>1
            turn = TURN_NEXT;
        } else if (lastTurn == TURN_NEXT && mTouchUp.x < mShape.width / 3) { //mShape.width>>1

            turn = TURN_NO_NEXT;

        }

        return turn;
    }

    /**
     * 是否可以翻过去
     *
     * @deprecated
     */
    public boolean canDragOver() {

        return Math.hypot((mTouchMove.x - mCorner.x), (mTouchMove.y - mCorner.y)) > mShape.width / 10;
    }

    public float getRollingMinDistance() {
        return mShape.width / GLIDE_MULTIPLE + 1.0f;
    }

    public void setPagging(boolean isPagging) {
        this.isPagging = isPagging;
    }

    public boolean isHorizonTurnning() {
        return isHorizonTurnning;
    }

    public PointF onSimulation(int turn) {
        PointF point = new PointF();

        if (turn == TURN_PREVIOUS) {
            point.x = 0.1f;
            point.y = mShape.height >> 1;
        } else {
            point.x = mShape.width - 0.1f;
            point.y = mShape.height;
        }

        return point;
    }

    public PointF onSimulationSlide(int turn) {
        PointF point = new PointF();

        if (turn == TURN_PREVIOUS) {
            point.x = 0.1f;
            point.y = mShape.height >> 1;
        } else {
            point.x = mShape.width - 0.1f;
            point.y = mShape.height;
        }

        return point;
    }

    public float getMoveSlop() {
        return motionEvent == MotionEvent.ACTION_DOWN ? PAGGING_SLOP : TOUCH_SLOP;
    }

    public void onTurn(Canvas mCanvas, BitmapProvider provider, int trun) throws Throwable {
        if (provider == null || provider.getCurrentBitmap()== null) return;

        mCanvas.save();
        calculate(trun);
        //获取当前页和底页

        //获取当前页和底页
        BitmapHolder topPage = null;
        BitmapHolder bottomPage = null;
        if (trun == TURN_PREVIOUS || trun == TURN_NO_PREVIOUS) {
            topPage = provider.getPreviousBitmap();
            bottomPage = provider.getCurrentBitmap();

        } else if (trun == TURN_NEXT || trun == TURN_NO_NEXT) {
            topPage = provider.getCurrentBitmap();
            bottomPage = provider.getNextBitmap();
        }


        Path lastPath = null;
        if (topPage != null) {

            Bitmap bitmap = topPage.lockRead();

            lastPath = drawCurrentPageArea(mCanvas, bitmap);
            drawCurrentBackArea(mCanvas, bitmap, lastPath);
            topPage.unLockRead();
        }

        if (bottomPage != null) {

            Bitmap bitmap = bottomPage.lockRead();

            drawUndersidePageAreaAndShadow(mCanvas, bitmap, lastPath);
            bottomPage.unLockRead();
        }

        if (!needSpeedUp || mBezierVertical.control.x > 0) {
            if (isHorizonTurnning) {
                drawCurrentHorizontalPageShadow(mCanvas, lastPath);
            } else {
                drawCurrentPageShadow(mCanvas, lastPath);
            }
        }

        mCanvas.restore();
    }


//    public void onTurnRolling(Bitmap bit , Canvas mCanvas, PageBitmap mPageBitmapCurrent, PageBitmap mPageBitmapUnderside,int trun) throws Throwable{
//        //mCanvas.save();
//        
//        if(mPageBitmapUnderside!=null){
//            drawUndersidePageAreaRolling(mCanvas, mPageBitmapUnderside);
//        }
//        
//        if(mPageBitmapCurrent!=null){
//            drawCurrentPageAreaRolling( bit ,mCanvas, mPageBitmapCurrent);
//        }
//        
//        //mCanvas.restore();
//    }

    public void onTurnSlide(Canvas mCanvas, BitmapProvider provider, int trun) throws Throwable {
//      mCanvas.save();
        calculateSlide(trun);

        //获取当前页和底页
        BitmapHolder topPage = null;
        BitmapHolder bottomPage = null;
        if (trun == TURN_PREVIOUS || trun == TURN_NO_PREVIOUS) {
            topPage = provider.getPreviousBitmap();
            bottomPage = provider.getCurrentBitmap();

        } else if (trun == TURN_NEXT || trun == TURN_NO_NEXT) {
            topPage = provider.getCurrentBitmap();
            bottomPage = provider.getNextBitmap();
        }
        Path tempPath = PageTurnHelper.drawPolygonSlide(mTouchBottom, mCornerBottom, mCornerTop, mTouchTop);
        if (bottomPage != null) {


            Bitmap bitmap = bottomPage.lockRead();
            try {


                drawUndersidePageAreaAndShadowSlide(mCanvas, bitmap, tempPath);
            } finally {
                bottomPage.unLockRead();
            }


        }

        if (topPage != null) {
            Bitmap bitmap = topPage.lockRead();
            try {
                drawCurrentPageAreaSlide(mCanvas, bitmap, tempPath);
            } finally {
                topPage.unLockRead();
            }
        }
//      mCanvas.restore();
    }

    /**
     * 是否从左边翻向右边
     *
     * @deprecated
     */
    public boolean isDragPrevious() {

        return mCorner.x == 0;
    }

    /**
     * 计算翻页成功的目标坐标
     *
     * @return
     */
    public Point getDistancePoint(int turn) {
        return PageTurnHelper.getDistancePoint(mTouchUp, mCorner, mShape, turn);
    }

    /**
     * 计算翻页成功的目标坐标
     *
     * @return
     */
    public Point getDistancePointSlide(int turn) {
        return PageTurnHelper.getDistancePointSlide(mTouchUp, mShape, turn);
    }

    /**
     * 计算没有翻页成功的目标坐标
     *
     * @return
     */
    public Point getNoDistancePoint(int turn) {

        return PageTurnHelper.getNoDistancePoint(mTouchUp, mCorner, mShape, turn);
    }

    private void calculate(int trun) {
        if (trun == TURN_PREVIOUS || trun == TURN_NO_PREVIOUS) {
//          calculate();
//          PageTurnHelper.calculateTouchMove(mTouchMove, mBezierHorizontal.vertex, mBezierVertical.vertex);

            PointF p = PageTurnHelper.excursion(mShape, mTouchDown, mTouchMove);
            mTouchMove.x = p.x;
            mTouchMove.y = p.y;
        }

        PageTurnHelper.checkIllegalPoint(mTouchMove, mCorner);

        mMiddle.x = (mTouchMove.x + mCorner.x) * 0.5f;
        mMiddle.y = (mTouchMove.y + mCorner.y) * 0.5f;
        mBezierHorizontal.control.x = mMiddle.x - (mCorner.y - mMiddle.y) * (mCorner.y - mMiddle.y) / (mCorner.x - mMiddle.x);
        mBezierHorizontal.control.y = mCorner.y;
        mBezierVertical.control.x = mCorner.x;
        mBezierVertical.control.y = mMiddle.y - (mCorner.x - mMiddle.x) * (mCorner.x - mMiddle.x) / (mCorner.y - mMiddle.y);

        mBezierHorizontal.start.x = mBezierHorizontal.control.x - (mCorner.x - mBezierHorizontal.control.x) * 0.5f;
        mBezierHorizontal.start.y = mCorner.y;

        // 当mBezierStart1.x < 0或者mBezierStart1.x > 480时
        // 如果继续翻页，会出现BUG故在此限制
        if (mTouchMove.x > 0 && mTouchMove.x < mShape.width && (mBezierHorizontal.start.x < 0 || mBezierHorizontal.start.x > mShape.width)) {
            if (mBezierHorizontal.start.x < 0) {
                mBezierHorizontal.start.x = mShape.width - mBezierHorizontal.start.x;
            }

            float f1 = Math.abs(mCorner.x - mTouchMove.x);
            float f2 = mShape.width * f1 / mBezierHorizontal.start.x;
            mTouchMove.x = Math.abs(mCorner.x - f2);

            float f3 = Math.abs(mCorner.x - mTouchMove.x) * Math.abs(mCorner.y - mTouchMove.y) / f1;
            mTouchMove.y = Math.abs(mCorner.y - f3);

            mMiddle.x = (mTouchMove.x + mCorner.x) * 0.5f;
            mMiddle.y = (mTouchMove.y + mCorner.y) * 0.5f;

            mBezierHorizontal.control.x = mMiddle.x - (mCorner.y - mMiddle.y) * (mCorner.y - mMiddle.y) / (mCorner.x - mMiddle.x);
            mBezierHorizontal.control.y = mCorner.y;

            mBezierVertical.control.x = mCorner.x;
            mBezierVertical.control.y = mMiddle.y - (mCorner.x - mMiddle.x) * (mCorner.x - mMiddle.x) / (mCorner.y - mMiddle.y);
            mBezierHorizontal.start.x = mBezierHorizontal.control.x - (mCorner.x - mBezierHorizontal.control.x) * 0.5f;
        } else if (trun == TURN_PREVIOUS || trun == TURN_NO_PREVIOUS) {

        }

        mTouch2Corner = (float) Math.hypot((mTouchMove.x - mCorner.x), (mTouchMove.y - mCorner.y));
        mDegrees = PageTurnHelper.getDegrees(mBezierHorizontal.control.x - mCorner.x, mBezierVertical.control.y - mCorner.y);

        if (isHorizonTurnning) {
            mIsRtLb = trun == TURN_PREVIOUS || trun == TURN_NO_NEXT;

            mCornerTop.x = mShape.width;
            mCornerTop.y = 0.0f;
            mCornerBottom.x = mCornerTop.x;
            mCornerBottom.y = mShape.height;

            mFoldTop.x = mTouchMove.x + (mShape.width - mTouchMove.x) * 0.4f;
            mFoldTop.y = 0.0f;
            mFoldBottom.x = mFoldTop.x;
            mFoldBottom.y = mShape.height;

            mTouchTop.x = mTouchMove.x;
            mTouchTop.y = 0;
            mTouchBottom.x = mTouchTop.x;
            mTouchBottom.y = mShape.height;
        } else {
            mBezierVertical.start.x = mCorner.x;
            mBezierVertical.start.y = mBezierVertical.control.y - (mCorner.y - mBezierVertical.control.y) * 0.5f;

            PageTurnHelper.calculateCross(mBezierHorizontal.end, mTouchMove, mBezierHorizontal.control, mBezierHorizontal.start, mBezierVertical.start);
            PageTurnHelper.calculateCross(mBezierVertical.end, mTouchMove, mBezierVertical.control, mBezierHorizontal.start, mBezierVertical.start);

            mBezierHorizontal.vertex.x = (mBezierHorizontal.start.x + 2 * mBezierHorizontal.control.x + mBezierHorizontal.end.x) * 0.25f;
            mBezierHorizontal.vertex.y = (2 * mBezierHorizontal.control.y + mBezierHorizontal.start.y + mBezierHorizontal.end.y) * 0.25f;
            mBezierVertical.vertex.x = (mBezierVertical.start.x + 2 * mBezierVertical.control.x + mBezierVertical.end.x) * 0.25f;
            mBezierVertical.vertex.y = (2 * mBezierVertical.control.y + mBezierVertical.start.y + mBezierVertical.end.y) * 0.25f;
        }
    }

    private void calculateSlide(int trun) {
        if (trun == TURN_PREVIOUS || trun == TURN_NO_PREVIOUS) {
            PointF p = PageTurnHelper.excursionSlide(mShape, mTouchDown,
                    mTouchMove);
            mTouchMove.x = p.x;
            mTouchMove.y = p.y;
        }

        mCornerTop.x = mShape.width;
        mCornerTop.y = 0.0f;
        mCornerBottom.x = mCornerTop.x;
        mCornerBottom.y = mShape.height;

        mFoldTop.x = (mTouchMove.x > mShape.width ? mShape.width : mTouchMove.x) - PageTurnHelper.getBookBoxRect().left;
        mFoldTop.y = 0.0f;
        mFoldBottom.x = mFoldTop.x;
        mFoldBottom.y = mShape.height;

        if (trun == TURN_NEXT) {
            mTouchTop.x = mTouchMove.x > mShape.width ? mShape.width : (mTouchMove.x < mTouchDown.x ? mShape.width - (mTouchDown.x - mTouchMove.x) : mShape.width);
        } else if (trun == TURN_NO_NEXT) {
            mTouchTop.x = mShape.width;
        } else {
            mTouchTop.x = mTouchMove.x > mShape.width ? mShape.width : mTouchMove.x;
        }

        mTouchTop.y = 0;
        mTouchBottom.x = mTouchTop.x;
        mTouchBottom.y = mShape.height;
    }

    /**
     * 当前页的显示区域
     * (扣掉下一页显示区域和当前页的背影区域)
     */
    private Path drawCurrentPageArea(Canvas canvas, Bitmap topPage) throws Throwable {
        Path tempPath = new Path();
        if (canvas != null) {
            //下一页显示区域和当前页的背影区域
            canvas.save();

            tempPath = isHorizonTurnning ? PageTurnHelper.drawPolygon(mTouchBottom, mCornerBottom, mCornerTop, mTouchTop) :
                    PageTurnHelper.drawCurrentPagePolygon(mBezierHorizontal, mBezierVertical, mTouchMove, mCorner);
            if (needSpeedUp) {
                if (mBezierVertical.control.x < 0) {
                    return tempPath;
                }
                if (isXXhdpi()) {
                    mPaint.setAntiAlias(mTouchMove.x > 0);
                    mPaint.setSubpixelText(mTouchMove.x > 0);
                }
            }
            canvas.clipPath(tempPath, Region.Op.XOR);

            canvas.translate(PageTurnHelper.getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left), 0);
            canvas.drawBitmap(topPage, 0, 0, mPaint);
            canvas.restore();
        }

        return tempPath;
    }
    private boolean isXXhdpi() {
        return mShape.width > 1900 || mShape.height > 1900;
    }
//    
//    /**
//     * 当前页的底页区域
//     * (扣掉下一页显示区域和当前页的背影区域)
//     * @param tempPath 
//     */
//    private Path drawUndersidePageAreaRolling(Canvas canvas, PageBitmap bottomPage) throws Throwable{
//        
//        if(canvas!=null){
//            //下一页显示区域
//            canvas.save();
//            if (needSpeedUp && isXXhdpi()) {
//                mPaint.setAntiAlias(mTouchMove.x < 0);
//                mPaint.setSubpixelText(mTouchMove.x < 0);
//            }
//            bottomPage.drawHPage(canvas,  PageTurnHelper
//                    .getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left) , 0, mPaint);
//            canvas.restore();
//        }
//        
//        return null;
//    }

//    /**
//     * 当前页的显示区域
//     * (扣掉下一页显示区域和当前页的背影区域)
//     * @param tempPath 
//     */
//    private Path drawCurrentPageAreaRolling(Bitmap bit ,Canvas canvas, PageBitmap topPage) throws Throwable{
//
//        if(canvas!=null){
//            //下一页显示区域和当前页的背影区域
//            canvas.save();
//
//            if (needSpeedUp) {
//                if (isXXhdpi()) {
//                    mPaint.setAntiAlias(mTouchMove.x > 0);
//                    mPaint.setSubpixelText(mTouchMove.x > 0);
//                }
//            }
//            topPage.drawPageRolling(bit ,canvas, PageTurnHelper
//                    .getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left) , mTouchMove.y , mPaint);
//            canvas.restore();
//        }
//        
//        return null;
//    }

    /**
     * 当前页的显示区域
     * (扣掉下一页显示区域和当前页的背影区域)
     *
     * @param tempPath
     */
    private Path drawCurrentPageAreaSlide(Canvas canvas, Bitmap topPage, Path tempPath) throws Throwable {

        if (canvas != null) {
            //下一页显示区域和当前页的背影区域
            canvas.save();

            if (needSpeedUp) {
                if (isXXhdpi()) {
                    mPaint.setAntiAlias(mTouchMove.x > 0);
                    mPaint.setSubpixelText(mTouchMove.x > 0);
                }
            }
            canvas.clipPath(tempPath, Region.Op.XOR);
            canvas.translate(mTouchBottom.x - mShape.width + PageTurnHelper.getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left), 0);
            canvas.drawBitmap(topPage, 0, 0, mPaint);
            canvas.restore();
        }

        return tempPath;
    }

    /**
     * 下一页显示区域
     */
    private void drawUndersidePageAreaAndShadow(Canvas canvas, Bitmap bottomPage, Path lastPath) throws Throwable {
        if (canvas != null && lastPath != null) {
            //下一页显示区域
            canvas.save();
            if (needSpeedUp && isXXhdpi()) {
                mPaint.setAntiAlias(mTouchMove.x < 0);
                mPaint.setSubpixelText(mTouchMove.x < 0);
            }
            if (isHorizonTurnning) {
                canvas.clipPath(lastPath);
                canvas.clipPath(PageTurnHelper.drawPolygon(mFoldBottom, mCornerBottom, mCornerTop, mFoldTop), Region.Op.INTERSECT);
                canvas.save();
                Rect bookBoxRect = PageTurnHelper.getBookBoxRect();
                canvas.translate(PageTurnHelper.getBookBoxRectLeft(bookBoxRect.left), 0);
                canvas.drawBitmap(bottomPage, 0, 0, mPaint);

                canvas.restore();
                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    GradientDrawable mBackShadowDrawable = PageTurnHelper.getBackShadowDrawableLR();
                    mBackShadowDrawable.setBounds(PageTurnHelper.getUndersideShadowRect(mTouchBottom, mFoldBottom, mShape));
                    mBackShadowDrawable.draw(canvas);
                }
            } else {
                Path tempPath = PageTurnHelper.drawPolygon(mBezierHorizontal.start, mBezierHorizontal.vertex,
                        mBezierVertical.vertex, mBezierVertical.start, mCorner);

                canvas.clipPath(lastPath);
                canvas.clipPath(tempPath, Region.Op.INTERSECT);
                canvas.save();
                Rect bookBoxRect = PageTurnHelper.getBookBoxRect();
                canvas.translate(PageTurnHelper.getBookBoxRectLeft(bookBoxRect.left), 0);
                canvas.drawBitmap(bottomPage, 0, 0, mPaint);
                canvas.restore();
                canvas.rotate(mDegrees, mBezierHorizontal.start.x, mBezierHorizontal.start.y);
                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    GradientDrawable mBackShadowDrawable = mIsRtLb ?
                            PageTurnHelper.getBackShadowDrawableLR() : PageTurnHelper.getBackShadowDrawableRL();
                    mBackShadowDrawable.setBounds(PageTurnHelper.getUndersideShadowRect(mIsRtLb, mBezierHorizontal, mDiagonal, mTouch2Corner));
                    mBackShadowDrawable.draw(canvas);
                }
            }
            canvas.restore();
        }
    }

    /**
     * 下一页显示区域
     */
    private void drawUndersidePageAreaAndShadowSlide(Canvas canvas, Bitmap bottomPage, Path lastPath) throws Throwable {
        if (canvas != null && lastPath != null) {
            //下一页显示区域
            canvas.save();
            if (needSpeedUp && isXXhdpi()) {
                mPaint.setAntiAlias(mTouchMove.x < 0);
                mPaint.setSubpixelText(mTouchMove.x < 0);
            }

            canvas.clipPath(lastPath);
            canvas.clipPath(PageTurnHelper.drawPolygon(mFoldBottom,
                    mCornerBottom, mCornerTop, mFoldTop), Region.Op.INTERSECT);
            canvas.translate(PageTurnHelper.getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left), 0);

            canvas.drawBitmap(bottomPage, 0, 0, mPaint);

            GradientDrawable mBackShadowDrawable = PageTurnHelper.getBackShadowDrawableLR();
            mBackShadowDrawable.setBounds(PageTurnHelper.getUndersideShadowRectSlide(mTouchBottom, mShape));
            mBackShadowDrawable.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 绘制翻起页
     */
    public void drawCurrentHorizontalPageShadow(Canvas canvas, Path lastPath) {
        if (canvas != null && lastPath != null) {
            //绘制翻起水平阴影
            canvas.save();
            canvas.clipPath(lastPath, Region.Op.XOR);
            if (!PageTurnHelper.isDayModeTitleLineColor()) {
                GradientDrawable mCurrentPageShadow = PageTurnHelper.getFrontShadowDrawableVRL();
                mCurrentPageShadow.setBounds(PageTurnHelper.getCurrentHorizontalShadowRect(mTouchBottom,
                        mFoldBottom, mTouch2Corner * 0.17f, mShape));

                mCurrentPageShadow.draw(canvas);
            }
            canvas.restore();
        }
    }

    /**
     * 绘制翻起页
     */
    public void drawCurrentPageShadow(Canvas canvas, Path lastPath) throws Throwable {
        if (canvas != null && lastPath != null) {
            float touch2Corner = mTouch2Corner * 0.17f, degrees = 0.0f;

            PointF shadowVertexPoint = PageTurnHelper.getShadowVertexPoint(mBezierHorizontal, mTouchMove, mIsRtLb, touch2Corner);

            GradientDrawable mCurrentPageShadow = null;

            //绘制翻起水平阴影
            degrees = PageTurnHelper.getDegrees(mTouchMove.x - mBezierHorizontal.control.x, mBezierHorizontal.control.y - mTouchMove.y);
            if (mCorner.y == mShape.height && degrees > -89 || mCorner.y == 0 && (degrees < -95 || degrees > 0)) {
                canvas.save();
                canvas.clipPath(lastPath, Region.Op.XOR);
                canvas.clipPath(PageTurnHelper.drawPolygon(shadowVertexPoint, mTouchMove,
                        mBezierHorizontal.control, mBezierHorizontal.start), Region.Op.INTERSECT);
                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    mCurrentPageShadow = mIsRtLb ? PageTurnHelper.getFrontShadowDrawableVLR() : PageTurnHelper.getFrontShadowDrawableVRL();
                    mCurrentPageShadow.setBounds(PageTurnHelper.getCurrentHorizontalShadowRect(mIsRtLb,
                            mBezierHorizontal, mDiagonal, touch2Corner));

                    canvas.rotate(PageTurnHelper.getDegrees(mTouchMove.x - mBezierHorizontal.control.x, mBezierHorizontal.control.y - mTouchMove.y),
                            mBezierHorizontal.control.x, mBezierHorizontal.control.y);
                    mCurrentPageShadow.draw(canvas);
                }
                canvas.restore();
            }

            //绘制翻起垂直阴影
            degrees = PageTurnHelper.getDegrees(mBezierVertical.control.y - mTouchMove.y, mBezierVertical.control.x - mTouchMove.x);
            if (mCorner.y == mShape.height && degrees < 85 || mCorner.y == 0 && degrees > -85) {
                canvas.save();
                canvas.clipPath(lastPath, Region.Op.XOR);
                canvas.clipPath(PageTurnHelper.drawPolygon(shadowVertexPoint, mTouchMove,
                        mBezierVertical.control, mBezierVertical.start), Region.Op.INTERSECT);
                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    mCurrentPageShadow = mIsRtLb ? PageTurnHelper.getFrontShadowDrawableHTB() : PageTurnHelper.getFrontShadowDrawableHBT();
                    mCurrentPageShadow.setBounds(PageTurnHelper.getCurrentVerticalShadowRect(mIsRtLb,
                            mBezierVertical, mDiagonal, touch2Corner, mShape));

                    canvas.rotate(degrees, mBezierVertical.control.x, mBezierVertical.control.y);
                    mCurrentPageShadow.draw(canvas);
                }
                canvas.restore();
            }
        }
    }

    /**
     * 绘制翻起页背面
     */
    private void drawCurrentBackArea(Canvas canvas, Bitmap topPage, Path lastPath) throws Throwable {
        if (canvas != null && lastPath != null) {
            ColorFilter tempColorFilter = mPaint.getColorFilter();
            mPaint.setColorFilter(PageTurnHelper.getColorMatrixColorFilter());

            canvas.save();

            if (isHorizonTurnning) {
                canvas.clipPath(lastPath);
                canvas.clipPath(PageTurnHelper.drawPolygon(mTouchBottom, mFoldBottom, mFoldTop, mTouchTop), Region.Op.INTERSECT);

                Matrix matrix = PageTurnHelper.getCurrentBackAreaMatrix(mShape, mTouchTop);
                canvas.save();
                canvas.setMatrix(matrix);

                canvas.translate(PageTurnHelper.getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().right), 0);
                canvas.drawBitmap(topPage, 0, 0, mPaint);
                canvas.restore();



                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    GradientDrawable mFolderShadowDrawable = PageTurnHelper.getFolderShadowDrawableLR();
                    mFolderShadowDrawable.setBounds(PageTurnHelper.getCurrentBackShadowRect(mTouchBottom, mFoldBottom, mShape));
                    mFolderShadowDrawable.draw(canvas);
                }
            } else {
                canvas.clipPath(lastPath);
                canvas.clipPath(PageTurnHelper.drawPolygon(mBezierVertical.vertex, mBezierHorizontal.vertex,
                        mBezierHorizontal.end, mTouchMove, mBezierVertical.end), Region.Op.INTERSECT);

                Matrix matrix = PageTurnHelper.getCurrentBackAreaMatrix(mCorner, mBezierHorizontal, mBezierVertical);
                matrix.preConcat(PageTurnHelper.getRotateMatrix(mShape.isLandscape, canvas.getWidth()));
                canvas.save();

                canvas.setMatrix(matrix);
                canvas.translate(PageTurnHelper.getBookBoxRectLeft(PageTurnHelper.getBookBoxRect().left), 0);
                canvas.drawBitmap(topPage, 0, 0, mPaint);

                canvas.restore();


                canvas.rotate(mDegrees, mBezierHorizontal.start.x, mBezierHorizontal.start.y);
                if (!PageTurnHelper.isDayModeTitleLineColor()) {
                    GradientDrawable mFolderShadowDrawable = mIsRtLb ?
                            PageTurnHelper.getFolderShadowDrawableLR() : PageTurnHelper.getFolderShadowDrawableRL();
                    mFolderShadowDrawable.setBounds(PageTurnHelper.getCurrentBackShadowRect(mIsRtLb, mBezierHorizontal, mBezierVertical, mDiagonal));
                    mFolderShadowDrawable.draw(canvas);
                }
            }
            canvas.restore();

            mPaint.setColorFilter(tempColorFilter);
        }
    }

    public void release() {

    }
    


    public void resetPoints() {
        mTouchDown.set(0.0f, 0.0f);
        mTouchMove.set(0.01f, 0.01f);       //拖拽点,不让x,y为0,否则在点计算时会有问题
        mTouchUp.set(0.0f, 0.0f);
        motionEvent = MotionEvent.ACTION_CANCEL;
    }




    @Override
    public void onDraw(Canvas canvas) {

        canvas.save();
        if(drawParam.padding!=null)
        {

            canvas.translate(drawParam.padding[0],drawParam.padding[1]);
            canvas.clipRect(0,0,mShape.width,mShape.height);
        }

        if (isInAnimation()) {
            try {

                onTurn(canvas, bitmapProvider ,turnMoveDirection);

            } catch (Throwable throwable) {
                Log.e(throwable);
            }
        }else
        {
            BitmapHolder currentBitmap = bitmapProvider.getCurrentBitmap();
            Bitmap bitmap = currentBitmap.lockRead();
            canvas.drawBitmap(bitmap,0,0,null);
            currentBitmap.unLockRead();
        }




        canvas.restore();
        computeScroll();
       // onDrawTemp( );

    }

    private void onDrawTemp( ) {
        Bitmap tempBitmap=Bitmap.createBitmap(2000,2000, Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(tempBitmap);
        canvas.save();
        if(drawParam.padding!=null)
        {

            canvas.translate(drawParam.padding[0],drawParam.padding[1]);
            canvas.clipRect(0,0,mShape.width,mShape.height);
        }

        if (isInAnimation()) {
            try {
                onTurn(canvas, bitmapProvider ,turnMoveDirection);

            } catch (Throwable throwable) {
                Log.e(throwable);
            }
        }else
        {
            BitmapHolder currentBitmap = bitmapProvider.getCurrentBitmap();
            Bitmap bitmap = currentBitmap.lockRead();
            canvas.drawBitmap(bitmap,0,0,null);
            currentBitmap.unLockRead();
        }




        canvas.restore();



    }

    private void computeScroll() {


            if (scroller.computeScrollOffset()) {
                int x = scroller.getCurrX(), y = scroller.getCurrY();
                setTouch(x, y, false);
                drawable.updateView();
            }


    }


    private void startAnimation(int turn) {
        Log.i(turn);





                PointF touchUp = getTouchUp();
                setMotionEvent(MotionEvent.ACTION_MASK);
                setSpeedUpState(true);
                int millis = getPaggingDelayMillis(turn);
                if (turn == TURN_NO_PREVIOUS || turn == TURN_NO_NEXT) {
                    Point distancePoint = getNoDistancePoint(turn);
                    scroller.startScroll((int) touchUp.x, (int) touchUp.y, distancePoint.x, distancePoint.y, millis);
                } else if (turn == TURN_PREVIOUS || turn == TURN_NEXT) {
                    Point distancePoint = getDistancePoint(turn);
                    scroller.startScroll((int) touchUp.x, (int) touchUp.y, distancePoint.x, distancePoint.y, millis);
                }

                drawable.updateView();



    }



}
