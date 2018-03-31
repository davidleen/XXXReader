package com.xxx.reader.turnner.sim;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;

import com.xxx.frame.Log;
import com.xxx.reader.Utils;
import com.xxx.reader.core.IPageTurner;

import java.lang.ref.SoftReference;


public class PageTurnHelper {
    public static final int CLICK_TOLANCE = Utils.dipDimensionInteger(8);//点击时防抖动距离

    public static enum Rece {left, top, right, bottom, shadow_middle, shadow_lefe}

    ;

    public static final float TOUCH_SLOP = 1.3f;
    public static final int PAGGING_SLOP = CLICK_TOLANCE;
    public static final int GLIDE_MULTIPLE = 24;
    private static final boolean IS_DEBUG = true;
    public static final int COUNT_BITMAP = 100;
    public static final int COUNT_POINT = (int) Math.sqrt(COUNT_BITMAP);
    public final static int COLOR_BACK_AREA = 0x0000000;
    private final static int LENGHT_SHADOW = Utils.dipDimensionInteger(10);

    private final static float[] COLOR_MATRIX_ARRAY = {0.55f, 0, 0, 0, 100.0f,
            0, 0.55f, 0, 0, 100.0f,
            0, 0, 0.55f, 0, 100.0f,
            0, 0, 0, 0.4f, 0};

    private static float[] matrixArray = {0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f};

    private static int windagePadding = -1;
    private static SoftReference<ShadowDrawable[]> shadowDrawable;
    private static ColorMatrixColorFilter colorMatrixColorFilter;
    private static Matrix matrix;

    private static SoftReference<Bitmap> coverLayer;
    private static int coverColor = -1;

    private static Rect bookBoxRect;

    private static String sLastSchemeName;

    public static float getLenghtShadow(float distance) {

        return Math.min(LENGHT_SHADOW, distance);
    }

    public static Rect getBookBoxRect() {
        if (bookBoxRect == null) {
            synchronized (PageTurnHelper.class) {
                if (bookBoxRect == null) {
                    bookBoxRect = new Rect();


                }
            }
        }

        return bookBoxRect;
    }

    public static int getWindagePadding() {
        if (windagePadding < 0) {
            synchronized (PageTurnHelper.class) {
                if (windagePadding < 0) {
                    windagePadding = (int) (getBookBoxRect().left * 0.3f + 0.5f);
                }
            }
        }

        return windagePadding;
    }

    public static int getBookBoxRectLeft(int x) {
        return x - getWindagePadding();
    }



    /**
     * 是否是非法数据
     *
     * @param mTouchMove
     * @param mCorner
     * @return
     */
    public static void checkIllegalPoint(PointF mTouchMove, PointF mCorner) {
        PointF tMiddle = new PointF();
        tMiddle.x = (mTouchMove.x + mCorner.x) * 0.5f;
        tMiddle.y = (mTouchMove.y + mCorner.y) * 0.5f;
        PointF thControl = new PointF();
        PointF tvControl = new PointF();
        thControl.x = tMiddle.x - (mCorner.y - tMiddle.y) * (mCorner.y - tMiddle.y) / (mCorner.x - tMiddle.x);
        thControl.y = mCorner.y;
        tvControl.x = mCorner.x;
        tvControl.y = tMiddle.y - (mCorner.x - tMiddle.x) * (mCorner.x - tMiddle.x) / (mCorner.y - tMiddle.y);

        if (mTouchMove.x - thControl.x == 0.0f || mTouchMove.x - tvControl.x == 0.0f) {
            android.util.Log.e("PageTurnHelper", "checkIllegalPoint E.R.R.O.R: [" + (mTouchMove.x - thControl.x) + ", " + (mTouchMove.x - tvControl.x) + "]");
            mTouchMove.x -= 0.13f;
        }
    }

    /**
     * 求解直线touch,control和直线bezierHorizontalStart,bezierVerticalStart的交点坐标
     */
    public static void calculateCross(PointF dPoint, PointF p1, PointF p2, PointF s1, PointF s2) {
        // 二元函数通式： y=ax+b
        float a1 = (p2.y - p1.y) / (p2.x - p1.x);
        float b1 = ((p1.x * p2.y) - (p2.x * p1.y)) / (p1.x - p2.x);

        if ((p2.x - p1.x) == 0.0f) {
            Log.e("illegal point");
        }

        float a2 = (s2.y - s1.y) / (s2.x - s1.x);
        float b2 = ((s1.x * s2.y) - (s2.x * s1.y)) / (s1.x - s2.x);

        dPoint.x = (b2 - b1) / (a1 - a2);
        dPoint.y = a1 * dPoint.x + b1;
    }

    /**
     * 计算向前翻页的touch点;
     * 该点由touch点y坐标和2条贝塞尔曲线顶点在的直线与当前touch点y坐标所对应的x坐标组成
     *
     * @param trun
     * @param mTouchMove
     * @param p1
     * @param p2
     */
    public static void calculateTouchMove(PointF mTouchMove, PointF p1, PointF p2) {
        mTouchMove.x = mTouchMove.x + mTouchMove.x - ((mTouchMove.y - p1.y) * (p1.x - p2.x) / (p1.y - p2.y) + p1.x);
    }

    /**
     * 计算拖拽点对应的拖拽脚
     *
     * @deprecated
     */
    public static void calculateCorner(PointF corner, float x, float y, Shape shape) {
        corner.x = x <= shape.width * 0.5f ? 0 : shape.width;
        corner.y = y <= shape.height * 0.3333f ? 0 : shape.height;
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public static void calculateCorner(PointF corner, PointF touchDown, PointF touchMove, Shape shape) {
        float x = 0.0f;

        float degree = touchDown.x - touchMove.x;
        if (degree < -PAGGING_SLOP) {
            x = shape.width;//0.0f;
        } else if (degree > PAGGING_SLOP) {
            x = shape.width;
        } else {
            x = shape.width;//touchMove.x<(shape.width>>1) ? 0.0f : shape.width;
        }

        corner.x = x;
        corner.y = touchMove.y < shape.height * 0.3333f ? 0 : shape.height;
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public static void calculateCornerSlide(PointF corner, PointF touchMove) {
        corner.x = touchMove.x;
        corner.y = touchMove.y;
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public static void calculateCornerRolling(PointF corner, PointF touchMove) {
        corner.x = touchMove.x;
        corner.y = touchMove.y;
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public static boolean calculateCorner(PointF cornerTop, PointF cornerBottom, PointF touchDown, PointF touchMove, Shape shape) {
        boolean isLeft = true;

        float x = 0.0f;
        float degree = touchDown.x - touchMove.x;
        if (degree < -PAGGING_SLOP) {
            x = shape.width;//0.0f;
            isLeft = true;
        } else if (degree > PAGGING_SLOP) {
            x = shape.width;
            isLeft = false;
        } else {
            x = shape.width;//touchMove.x<(shape.width>>1) ? 0.0f : shape.width;
            isLeft = touchMove.x < shape.width >> 1;
        }

        cornerTop.x = x;
        cornerTop.y = 0.0f;

        cornerBottom.x = x;
        cornerBottom.y = shape.height;

        return isLeft;
    }

    /**
     * 计算要旋转的角度
     */
    public static float getDegrees(float x, float y) {
        return (float) Math.toDegrees(Math.atan2(x, y));
    }

    /**
     * 是否属于右上左下
     */
    public static boolean isRightTopOrLeftBottom(Shape shape, PointF corner) {

        return (corner.x == 0 && corner.y == shape.height) || (corner.x == shape.width && corner.y == 0) ? true : false;
    }

    public static PointF getShadowVertexPoint(Bezier mBezierHorizontal, PointF mTouch, boolean isRtLb, float touch2Corner) {
        double degree = 0.0d;
        if (isRtLb) {
            degree = Math.PI * 0.25f - Math.atan2(mBezierHorizontal.control.y - mTouch.y, mTouch.x - mBezierHorizontal.control.x);
        } else {
            degree = Math.PI * 0.25f - Math.atan2(mTouch.y - mBezierHorizontal.control.y, mTouch.x - mBezierHorizontal.control.x);
        }

        // 翻起页阴影顶点与touch点的距离
        float d1 = (float) (getLenghtShadow(touch2Corner) * 1.414f * Math.cos(degree));
        float d2 = (float) (getLenghtShadow(touch2Corner) * 1.414f * Math.sin(degree));

        float x = mTouch.x + d1;//阴影顶点X坐标
        float y = isRtLb ? mTouch.y + d2 : mTouch.y - d2;//阴影顶点Y坐标

        return new PointF(x, y);
    }

    /**
     * 计算翻页成功的目标坐标
     */
    public static Point getDistancePoint(PointF touch, PointF corner, Shape shape, int turn) {
        Point distancePoint = new Point();

        if (turn == IPageTurner.TURN_NEXT) {//corner.x > 0
            distancePoint.x = -(int) (shape.width - 0.1f + touch.x);
        } else if (turn == IPageTurner.TURN_PREVIOUS) {
            PointF p = deExcursion(shape, touch);
            distancePoint.x = (int) ((shape.width * 1.6667f) - p.x);

            touch.y = p.y;
        }

        if (corner.y > 0) {
            distancePoint.y = (int) (shape.height - 0.1f - 1 - touch.y);
        } else {
            distancePoint.y = (int) (1 - touch.y + 0.1f); // 防止mTouch.y最终变为0
        }

        return distancePoint;
    }

    /**
     * 计算翻页成功的目标坐标
     */
    public static Point getDistancePointSlide(PointF touch, Shape shape, int turn) {
        Point distancePoint = new Point();

        if (turn == IPageTurner.TURN_NEXT) {//corner.x > 0
            distancePoint.x = -(int) (shape.width);
        } else if (turn == IPageTurner.TURN_PREVIOUS) {
            PointF p = deExcursionSlide(shape, touch);
            distancePoint.x = (int) (shape.width - p.x);

            touch.y = p.y;
        }
        distancePoint.y = (int) (touch.y + 0.1f); // 防止mTouch.y最终变为0

        return distancePoint;
    }

    /**
     * 计算翻页成功的目标坐标
     *
     * @deprecated
     */
    public static Point getDistancePoint(boolean isLeft, PointF touch, Shape shape) {
        Point distancePoint = new Point();

        if (isLeft) {
            if (shape.isLandscape) {
                distancePoint.x = (int) (shape.width - touch.x);
            } else {
                distancePoint.x = (int) ((shape.width << 1) - touch.x);
            }
        } else {
            if (shape.isLandscape) {
                distancePoint.x = -(int) (touch.x);
            } else {
                distancePoint.x = -(int) (shape.width + touch.x);
            }
        }
        if (isLeft) {
            distancePoint.y = (int) (shape.height - 1 - touch.y);
        } else {
            distancePoint.y = (int) (1 - touch.y); // 防止mTouch.y最终变为0
        }

        return distancePoint;
    }

    /**
     * 计算没有翻页成功的目标坐标
     *
     * @param touch
     * @param corner
     * @param shape
     * @return
     */
    public static Point getNoDistancePoint(PointF touch, PointF corner, Shape shape, int turn) {
        Point distancePoint = new Point();

        if (turn == IPageTurner.TURN_NO_NEXT) {//corner.x > 0
            distancePoint.x = (int) (shape.width - 0.1f - touch.x);
        } else if (turn == IPageTurner.TURN_NO_PREVIOUS) {
            distancePoint.x = (int) (-(shape.width << 1) * 0.3333f - touch.x);
        }

        if (corner.y > 0) {
            distancePoint.y = (int) (shape.height - 0.1f - touch.y);
        } else {
            distancePoint.y = (int) (1 - touch.y + 0.1f); // 防止mTouch.y最终变为0
        }

        return distancePoint;
    }

    /**
     * 计算没有翻页成功的目标坐标
     *
     * @param touch
     * @param corner
     * @param shape
     * @return
     * @deprecated
     */
    public static Point getNoDistancePoint(boolean isLeft, PointF touch, Shape shape) {
        Point distancePoint = new Point();

        if (isLeft) {
            distancePoint.x = (int) (1 - touch.x);
        } else {
            distancePoint.x = (int) (shape.width - touch.x);
        }
        if (isLeft) {
            distancePoint.y = (int) (shape.height - touch.y);
        } else {
            distancePoint.y = (int) (1 - touch.y); // 防止mTouch.y最终变为0
        }

        return distancePoint;
    }

    /**
     * 计算当前翻页方向.
     *
     * @deprecated
     */
    public static boolean isDragPrevious(float x, float y, Shape shape) {
        PointF corner = new PointF();
        calculateCorner(corner, x, y, shape);

        return corner.x == 0;
    }

    /**
     * 按照动画模式设置坐标
     *
     * @param mShape
     * @param point
     * @param x
     * @param y
     */
    public static boolean coordinateTouchMove(Shape mShape, PointF point, PointF consultPoint, float x, float y, boolean isOnTouch) {
        boolean isHorizonTurnning = false;

        point.x = isOnTouch ? (x > mShape.width ? mShape.width - 0.1f : (x <= 0.0f ? 0.1f : x)) : x;

        if ( ( consultPoint != null && consultPoint.y > mShape.height * 0.3333f && consultPoint.y < mShape.height * 0.6667f)) {
            point.y = mShape.height - 0.1f;

            isHorizonTurnning = true;
        } else if (y > mShape.height) {
            point.y = mShape.height - 0.1f;
        } else if (y < 0) {
            point.y = 0.1f;
        } else {
            point.y = y;
        }

        return isHorizonTurnning;
    }

    /**
     * 按照动画模式设置坐标
     *
     * @param mShape
     * @param point
     * @param x
     * @param y
     */
    public static void coordinateTouchMoveSlide(PointF point, float x, float y) {
        point.x = x;
        point.y = y;
    }

    /**
     * 按照动画模式设置坐标
     *
     * @param mShape
     * @param point
     * @param x
     * @param y
     */
    public static void coordinateTouchMoveRolling(PointF point, float x, float y) {
        point.x = x;
        point.y = y;
    }

    /**
     * 动画偏移
     *
     * @param mShape
     * @param x
     * @return
     */
    public static PointF excursion(Shape mShape, PointF touchDown, PointF touchMove) {
        return excursion(mShape, touchDown, touchMove, IPageTurner.TURN_PREVIOUS);
    }

    /**
     * 动画偏移
     *
     * @param mShape
     * @param x
     * @return
     */
    public static PointF excursionSlide(Shape mShape, PointF touchDown, PointF touchMove) {
        return excursionSlide(mShape, touchDown, touchMove, IPageTurner.TURN_PREVIOUS);
    }

    public static PointF excursion(Shape mShape, PointF touchDown, PointF touchMove, int turn) {
        PointF exTouch = new PointF();

        if (turn == IPageTurner.TURN_PREVIOUS || turn == IPageTurner.TURN_NO_PREVIOUS) {
            if (touchDown.y > mShape.height * 0.6667f && touchMove.x < mShape.width * 0.4f && touchMove.y < mShape.height * 0.4f) {
                touchMove.x = mShape.width * 0.4f;
            } else if (touchDown.y < mShape.height * 0.3333f && touchMove.x < mShape.width * 0.4f && touchMove.y > mShape.height * 0.3333f) {
                touchMove.x = mShape.width * 0.4f;
            }

            exTouch.x = touchMove.x - (mShape.width - touchMove.x) * 0.5f;
            exTouch.y = touchMove.y > mShape.height * 0.3333f ? touchMove.y + (mShape.height - touchMove.y) * 0.5f : touchMove.y;
        }

        return exTouch;
    }

    public static PointF excursionSlide(Shape mShape, PointF touchDown, PointF touchMove, int turn) {
        PointF exTouch = new PointF();

        if (turn == IPageTurner.TURN_PREVIOUS || turn == IPageTurner.TURN_NO_PREVIOUS) {
            exTouch.x = touchMove.x > mShape.width ? mShape.width : touchMove.x;
            exTouch.y = touchMove.y;
        }

        return exTouch;
    }

    public static PointF deExcursion(Shape mShape, PointF touch) {
        return deExcursion(mShape, touch, IPageTurner.TURN_PREVIOUS);
    }

    public static PointF deExcursionSlide(Shape mShape, PointF touch) {
        return deExcursionSlide(mShape, touch, IPageTurner.TURN_PREVIOUS);
    }

    public static PointF deExcursion(Shape mShape, PointF touch, int turn) {
        PointF deTouch = new PointF();

        if (turn == IPageTurner.TURN_PREVIOUS || turn == IPageTurner.TURN_NO_PREVIOUS) {
            deTouch.x = (touch.x * 2 + mShape.width) * 0.3333f;
            deTouch.y = touch.y > mShape.height / 3 ? touch.y * 2 - mShape.height : touch.y;
        }

        return deTouch;
    }

    public static PointF deExcursionSlide(Shape mShape, PointF touch, int turn) {
        PointF deTouch = new PointF();

        if (turn == IPageTurner.TURN_PREVIOUS || turn == IPageTurner.TURN_NO_PREVIOUS) {
            deTouch.x = touch.x > mShape.width ? mShape.width : touch.x;
            deTouch.y = touch.y;
        }

        return deTouch;
    }

    /**
     * 绘制多边形
     */
    public static Path drawPolygon(PointF... points) {
        Path tempPath = new Path();

        int length = points.length;
        if (points != null && length > 1) {
            tempPath.moveTo(points[0].x, points[0].y);
            for (int i = 1; i < length; i++) {
                tempPath.lineTo(points[i].x, points[i].y);
            }
            tempPath.close();
        }

        return tempPath;
    }

    /**
     * 绘制多边形
     */
    public static Path drawPolygonSlide(PointF... points) {
        Path tempPath = new Path();

        int length = points.length;
        if (points != null && length > 1) {
            tempPath.moveTo(points[0].x, points[0].y);
            for (int i = 1; i < length; i++) {
                tempPath.lineTo(points[i].x, points[i].y);
            }
            tempPath.close();
        }

        return tempPath;
    }

    public static Path drawCurrentPagePolygon(Bezier bezierHorizontal, Bezier bezierVertical, PointF touch, PointF corner) {
        Path tempPath = new Path();

        if (bezierHorizontal != null && bezierVertical != null && touch != null && corner != null) {
            tempPath.moveTo(bezierHorizontal.start.x, bezierHorizontal.start.y);
            tempPath.quadTo(bezierHorizontal.control.x, bezierHorizontal.control.y,
                    bezierHorizontal.end.x, bezierHorizontal.end.y);
            tempPath.lineTo(touch.x, touch.y);
            tempPath.lineTo(bezierVertical.end.x, bezierVertical.end.y);
            tempPath.quadTo(bezierVertical.control.x, bezierVertical.control.y,
                    bezierVertical.start.x, bezierVertical.start.y);
            tempPath.lineTo(corner.x, corner.y);
            tempPath.close();
        }

        return tempPath;
    }

    public static Rect getUndersideShadowRect(boolean isRtLb, Bezier bezierHorizontal, float diagonal, float touch2Corner) {
        int left = 0, right = 0;

        if (isRtLb) {
            left = (int) (bezierHorizontal.start.x);
            right = (int) (bezierHorizontal.start.x + touch2Corner * 0.25f) + 1;
        } else {
            left = (int) (bezierHorizontal.start.x - touch2Corner * 0.25f);
            right = (int) bezierHorizontal.start.x;
        }

        return new Rect(left, (int) bezierHorizontal.start.y, right, (int) (diagonal + bezierHorizontal.start.y));
    }

    public static Rect getUndersideShadowRect(boolean isLeft, PointF move, PointF fold, Shape shape) {
        int left = 0, right = 0;

        if (isLeft) {
            left = (int) (fold.x - (move.x - fold.x) * 0.25f);
            right = (int) fold.x + 1;
        } else {
            left = (int) fold.x;
            right = (int) (fold.x + (fold.x - move.x) * 0.25f);
        }

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getUndersideShadowRect(PointF move, PointF fold, Shape shape) {
        int left = (int) (fold.x - (fold.x - move.x) * 0.15f);
        int right = (int) (fold.x + (fold.x - move.x) * 0.3f);

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getUndersideShadowRectSlide(PointF move, Shape shape) {
        int left = (int) (move.x - bookBoxRect.left);
        int right = left + 20;

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getCurrentHorizontalShadowRect(boolean isRtLb, Bezier bezierHorizontal, float diagonal, float touch2Corner) {
        int left = 0, right = 0;

        if (isRtLb) {
            left = (int) (bezierHorizontal.control.x);
            right = (int) (bezierHorizontal.control.x + getLenghtShadow(touch2Corner));
        } else {
            left = (int) (bezierHorizontal.control.x - getLenghtShadow(touch2Corner));
            right = (int) bezierHorizontal.control.x + 1;
        }

        return new Rect(left, (int) (bezierHorizontal.control.y - diagonal), right, (int) (bezierHorizontal.control.y));
    }

    public static Rect getCurrentHorizontalShadowRect(boolean isLeft, PointF move, PointF fold, float touch2Corner, Shape shape) {
        int left = 0, right = 0;

        if (isLeft) {
            left = (int) move.x;
            right = (int) (move.x + getLenghtShadow(touch2Corner));
        } else {
            left = (int) (move.x - getLenghtShadow(touch2Corner));
            right = (int) move.x;
        }

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getCurrentHorizontalShadowRect(PointF move, PointF fold, float touch2Corner, Shape shape) {
        int left = (int) (move.x - getLenghtShadow(touch2Corner));
        int right = (int) move.x;

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getCurrentVerticalShadowRect(boolean isRtLb, Bezier bezierVertical, float diagonal, float touch2Corner, Shape shape) {
        Rect rect = new Rect();

        int top = 0, bottom = 0;

        if (isRtLb) {
            top = (int) (bezierVertical.control.y - 1);
            bottom = (int) (bezierVertical.control.y + getLenghtShadow(touch2Corner));
        } else {
            top = (int) (bezierVertical.control.y - getLenghtShadow(touch2Corner));
            bottom = (int) (bezierVertical.control.y + 1);
        }

        float temp = (float) Math.hypot(bezierVertical.control.x,
                bezierVertical.control.y < 0 ? bezierVertical.control.y - shape.height : bezierVertical.control.y);
        if (temp > diagonal) {
            rect.set((int) (bezierVertical.control.x - getLenghtShadow(touch2Corner) - temp), top,
                    (int) (bezierVertical.control.x + diagonal - temp), bottom);
        } else {
            rect.set((int) (bezierVertical.control.x - diagonal), top, (int) (bezierVertical.control.x), bottom);
        }

        return rect;
    }

    public static Rect getCurrentBackShadowRect(boolean isRtLb, Bezier mBezierHorizontal, Bezier mBezierVertical, float diagonal) {
        int ix = (int) (mBezierHorizontal.start.x + mBezierHorizontal.control.x) >> 1;
        int iy = (int) (mBezierVertical.start.y + mBezierVertical.control.y) >> 1;
        float f3 = Math.min(Math.abs(ix - mBezierHorizontal.control.x),
                Math.abs(iy - mBezierVertical.control.y));

        int left = 0, right = 0;
        if (isRtLb) {
            left = (int) (mBezierHorizontal.start.x - 2);
            right = (int) (mBezierHorizontal.start.x + f3 + 2);
        } else {
            left = (int) (mBezierHorizontal.start.x - f3 - 2);
            right = (int) (mBezierHorizontal.start.x + 2);
        }

        return new Rect(left, (int) mBezierHorizontal.start.y, right, (int) (mBezierHorizontal.start.y + diagonal));
    }

    public static Rect getCurrentBackShadowRect(boolean isLeft, PointF move, PointF fold, Shape shape) {
        int left = 0, right = 0;
        if (isLeft) {
            left = (int) fold.x - 2;
            right = (int) (fold.x + (move.x - fold.x) * 0.25f) + 2;
        } else {
            left = (int) (fold.x - (fold.x - move.x) * 0.25f) - 2;
            right = (int) fold.x + 2;
        }

        return new Rect(left, 0, right, shape.height);
    }

    public static Rect getCurrentBackShadowRect(PointF move, PointF fold, Shape shape) {
        int left = (int) (fold.x - (fold.x - move.x) * 0.3f) - 2;
        int right = (int) (fold.x + (fold.x - move.x) * 0.15f) + 2;

        return new Rect(left, 0, right, shape.height);
    }

    public static Matrix getCurrentBackAreaMatrix(PointF corner, Bezier bezierHorizontal, Bezier bezierVertical) {
        if (matrix == null) {
            synchronized (Matrix.class) {
                if (matrix == null) {
                    matrix = new Matrix();
                }
            }
        } else {
            matrix.reset();
        }

        float dis = (float) Math.hypot(corner.x - bezierHorizontal.control.x, bezierVertical.control.y - corner.y);
        float f8 = (corner.x - bezierHorizontal.control.x) / dis;
        float f9 = (bezierVertical.control.y - corner.y) / dis;
        matrixArray[0] = 1 - 2 * f9 * f9;
        matrixArray[1] = 2 * f8 * f9;
        matrixArray[3] = matrixArray[1];
        matrixArray[4] = 1 - 2 * f8 * f8;

        matrix.setValues(matrixArray);
        matrix.preTranslate(-bezierHorizontal.control.x, -bezierHorizontal.control.y);
        matrix.postTranslate(bezierHorizontal.control.x, bezierHorizontal.control.y);

        return matrix;
    }

    public static ColorMatrixColorFilter getColorMatrixColorFilter() {
        if (colorMatrixColorFilter == null) {
            synchronized (ColorMatrixColorFilter.class) {
                if (colorMatrixColorFilter == null) {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.set(COLOR_MATRIX_ARRAY);
                    colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
                }
            }
        }

        return colorMatrixColorFilter;
    }

    public static Matrix getCurrentBackAreaMatrix(Shape shape, PointF mTouchTop, boolean isLeft) {
        if (matrix == null) {
            synchronized (Matrix.class) {
                if (matrix == null) {
                    matrix = new Matrix();
                }
            }
        } else {
            matrix.reset();
        }
        if (!isLeft) {
            matrix.preTranslate(-shape.width, 0);
        }
        matrix.postScale(-1, 1);
        matrix.postTranslate(mTouchTop.x, 0);

        return matrix;
    }

    public static Matrix getCurrentBackAreaMatrix(Shape shape, PointF mTouchTop) {
        return getCurrentBackAreaMatrix(shape, mTouchTop, false);
    }


    public static Matrix getRotateMatrix(boolean isLandscape, int width) {
        Matrix matrix = new Matrix();

        Rect bookBoxRect = getBookBoxRect();

        matrix.postTranslate(getBookBoxRectLeft(bookBoxRect.left), bookBoxRect.top);

        return matrix;
    }

    public static boolean isLandscape() {

        return Utils.isLandscape();

    }





    public static int getPixel(Bitmap backgroup) {
        int color = COLOR_BACK_AREA;

        if (backgroup != null && !IS_DEBUG) {
            int mWidth = backgroup.getWidth();
            int mHeight = backgroup.getHeight();

            try {
                int mount = 0;
                for (int i = 0; i < COUNT_BITMAP; i++) {
                    int p = backgroup.getPixel((i / COUNT_POINT) * ((mWidth - 5) / (COUNT_POINT - 1)), (i % COUNT_POINT) * ((mHeight - 5) / (COUNT_POINT - 1)));
                    mount += p;
                }
                color = mount / COUNT_BITMAP;
            } catch (Exception e) {
                Log.e(e);
            }
        }

        return color;
    }

    /* 底页折起边缘阴影 */
    public static GradientDrawable getBackShadowDrawableLR() {
        return getShadowDrawable().mBackShadowDrawableLR;
    }

    public static GradientDrawable getBackShadowDrawableRL() {
        return getShadowDrawable().mBackShadowDrawableRL;
    }

    /* 翻起页折起边缘阴影 */
    public static GradientDrawable getFolderShadowDrawableLR() {
        return getShadowDrawable().mFolderShadowDrawableLR;
    }

    public static GradientDrawable getFolderShadowDrawableRL() {
        return getShadowDrawable().mFolderShadowDrawableRL;
    }

    /* 翻起页夹角边缘阴影 */
    public static GradientDrawable getFrontShadowDrawableHBT() {
        return getShadowDrawable().mFrontShadowDrawableHBT;
    }

    public static GradientDrawable getFrontShadowDrawableHTB() {
        return getShadowDrawable().mFrontShadowDrawableHTB;
    }

    public static GradientDrawable getFrontShadowDrawableVLR() {
        return getShadowDrawable().mFrontShadowDrawableVLR;
    }

    public static GradientDrawable getFrontShadowDrawableVRL() {
        return getShadowDrawable().mFrontShadowDrawableVRL;
    }

    /**
     * 贝塞尔曲线
     */
    public static class Bezier {
        /**
         * 起始点
         */
        public PointF start = new PointF();

        /**
         * 控制点
         */
        public PointF control = new PointF();

        /**
         * 顶点
         */
        public PointF vertex = new PointF();

        /**
         * 结束点
         */
        public PointF end = new PointF();
    }

    private static ShadowDrawable getShadowDrawable() {
        int mode = SettingContent.getInstance().getDayNeightMode();

        if (getDrawableShadows()[mode] == null) {
            synchronized (ShadowDrawable.class) {
                if (getDrawableShadows()[mode] == null) {
                    getDrawableShadows()[mode] = new ShadowDrawable();
                }
            }
        }

        return shadowDrawable.get()[mode];
    }

    private static ShadowDrawable[] getDrawableShadows() {
        if (shadowDrawable == null || shadowDrawable.get() == null) {
            synchronized (ShadowDrawable.class) {
                if (shadowDrawable == null || shadowDrawable.get() == null) {
                    shadowDrawable = new SoftReference<ShadowDrawable[]>(new ShadowDrawable[2]);
                }
            }
        }

        return shadowDrawable.get();
    }

    private static class ShadowDrawable {
        //翻起页折起边缘阴影
        private static final int[][] COLOR = {{0x00454545, 0x40454545}, {0x00151515, 0x40151515}};

        //底页折起边缘阴影
        private static final int[][] BACK_SHADOW_COLORS = {{0x55454545, 0x00454545}, {0x40454545, 0x00151515}};

        //翻起页夹角边缘阴影
        private static final int[][] FRONT_SHADOW_COLORS = {{0x40454545, 0x00454545}, {0x40454545, 0x00151515}};

        private GradientDrawable mBackShadowDrawableLR;
        private GradientDrawable mBackShadowDrawableRL;

        //折角部分阴影
        private GradientDrawable mFolderShadowDrawableLR;
        private GradientDrawable mFolderShadowDrawableRL;

        private GradientDrawable mFrontShadowDrawableHBT;
        private GradientDrawable mFrontShadowDrawableHTB;
        private GradientDrawable mFrontShadowDrawableVLR;
        private GradientDrawable mFrontShadowDrawableVRL;

        private ShadowDrawable() {
            int mode = SettingContent.getInstance().getDayNeightMode();
            if (isDayModeTitleLineColor()) {
                mode = SettingContent.MODE_NIGHT;
            }
            //翻起页折起边缘阴影
            mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, COLOR[mode]);
            mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, COLOR[mode]);
            mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            //底页折起边缘阴影
            mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, BACK_SHADOW_COLORS[mode]);
            mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, BACK_SHADOW_COLORS[mode]);
            mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            //翻起页夹角边缘阴影
            mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, FRONT_SHADOW_COLORS[mode]);
            mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, FRONT_SHADOW_COLORS[mode]);
            mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, FRONT_SHADOW_COLORS[mode]);
            mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, FRONT_SHADOW_COLORS[mode]);
            mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        }
    }

    public static Bitmap amalgamate(Shape shape, Bitmap bitmapTop, Bitmap bitmapBottom) {
        Bitmap bitmap = null;

        if (bitmapTop != null && bitmapBottom != null) {
            bitmap = Bitmap.createBitmap(shape.width, shape.height, Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            Paint mPaint = new Paint();

            canvas.drawBitmap(bitmapTop, 0, 0, mPaint);
            canvas.drawBitmap(bitmapBottom, 0, shape.height - 30, mPaint);

            canvas.save();
            canvas.restore();
        } else if (bitmapTop != null && bitmapBottom == null) {
            bitmap = bitmapTop;
        } else {
            bitmap = Bitmap.createBitmap(shape.width, shape.height, Config.ARGB_4444);
        }

        return bitmap;
    }

    public static void drawBezier(Canvas canvas, Point mTouchMove, Bezier mBezierHorizontal, Bezier mBezierVertical) {
        canvas.save();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(15.0f);

        canvas.drawPoint(mBezierHorizontal.start.x, mBezierHorizontal.start.y, paint);
        canvas.drawPoint(mBezierHorizontal.control.x, mBezierHorizontal.control.y, paint);
        canvas.drawPoint(mBezierHorizontal.end.x, mBezierHorizontal.end.y, paint);
        canvas.drawPoint(mTouchMove.x, mTouchMove.y, paint);
        canvas.drawPoint(mBezierVertical.end.x, mBezierVertical.end.y, paint);
        canvas.drawPoint(mBezierVertical.control.x, mBezierVertical.control.y, paint);
        canvas.drawPoint(mBezierVertical.start.x, mBezierVertical.start.y, paint);

        canvas.restore();
    }

    public static void test(Canvas canvas) {
        Rect bookboxRect = getBookBoxRect();

        Paint p = new Paint();
        p.setStrokeWidth(2.0f);

        p.setColor(0x8800FF00);
        canvas.drawLine(getBookBoxRectLeft(bookboxRect.left), 0, getBookBoxRectLeft(bookboxRect.left), canvas.getHeight(), p);

        p.setColor(0x88FF0000);
        canvas.drawLine(bookboxRect.left, bookboxRect.top, canvas.getWidth() - bookboxRect.right, bookboxRect.top, p);
        canvas.drawLine(canvas.getWidth() - bookboxRect.right, bookboxRect.top,
                canvas.getWidth() - bookboxRect.right, canvas.getHeight() - 30 - bookboxRect.bottom, p);
        canvas.drawLine(canvas.getWidth() - bookboxRect.right, canvas.getHeight() - 30 - bookboxRect.bottom,
                bookboxRect.left, canvas.getHeight() - 30 - bookboxRect.bottom, p);
        canvas.drawLine(bookboxRect.left, canvas.getHeight() - 30 - bookboxRect.bottom, bookboxRect.left, bookboxRect.top, p);

        p.setColor(0x880000FF);
        canvas.drawLine(0, canvas.getHeight() / 3.0f, canvas.getWidth(), canvas.getHeight() / 3.0f, p);
        canvas.drawLine(0, canvas.getHeight() * 2 / 3.0f, canvas.getWidth(), canvas.getHeight() * 2 / 3.0f, p);
        canvas.drawLine(canvas.getWidth() / 3.0f, 0, canvas.getWidth() / 3.0f, canvas.getHeight(), p);
        canvas.drawLine(canvas.getWidth() * 2 / 3.0f, 0, canvas.getWidth() * 2 / 3.0f, canvas.getHeight(), p);

        p.setColor(Color.GRAY);
        canvas.drawLine(0, canvas.getHeight() * 0.4f, canvas.getWidth(), canvas.getHeight() * 0.4f, p);
        canvas.drawLine(canvas.getWidth() * 0.4f, 0, canvas.getWidth() * 0.4f, canvas.getHeight(), p);
        canvas.drawLine(canvas.getWidth() / 2.0f, 0, canvas.getWidth() / 2.0f, canvas.getHeight(), p);
        canvas.drawLine(0, canvas.getHeight() / 2.0f, canvas.getWidth(), canvas.getHeight() / 2.0f, p);
    }



    private PageTurnHelper() {

    }

    public static boolean isDayModeTitleLineColor() {
//        if (SettingContent.getInstance().getDayMode()) {//白天模式夜间一和夜间二左右翻页白色边缘暂时这么处理
//            String title = SettingContent.getInstance().getSettingSchemeName();
//            if (title.equals("夜间一") || title.equals("夜间二") || title.equals("黑色幻夜")) {
//                return true;
//            } else {
//                return false;
//            }
//        }
        return false;
    }
}
