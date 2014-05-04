package com.replaid.multitouchlayoutdemo.app.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.RelativeLayout;

/**
 * Created by desmond on 4/5/14.
 */
public class ZoomMovableRelativeLayout extends RelativeLayout {
    private static final String TAG = "ZoomMovableRelativeLayout";
    private static final int INVALID_POINTER_ID =  -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    //For Scaling
    private float mScaleFactor = 1;
    private float mFocusX;
    private float mFocusY;
    private ScaleGestureDetector mScaleDetector;
    private Matrix mScaleMatrix;
    private Matrix mScaleMatrixInverse;

    //For Moving
    private float mPosX;
    private float mPosY;
    private Matrix mTranslateMatrix;
    private Matrix mTranslateMatrixInverse;

    private float mLastTouchX;
    private float mLastTouchY;


    private float[] mInvalidateWorkingArray = new float[6];
    private float[] mDispatchTouchEventWorkingArray = new float[2];
    private float[] mOnTouchEventWorkingArray = new float[2];

    public ZoomMovableRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public ZoomMovableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomMovableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mScaleMatrix = new Matrix();
        mScaleMatrixInverse = new Matrix();
        mTranslateMatrix = new Matrix();
        mTranslateMatrixInverse = new Matrix();
        mTranslateMatrix.setTranslate(0, 0);
        mScaleMatrix.setScale(1, 1);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //To draw the child view
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);
        super.dispatchDraw(canvas);     //pass to child view
        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Pass the touchScreenEvent to the target view, or this if it's
        //the target view
        mDispatchTouchEventWorkingArray[0] = ev.getX();
        mDispatchTouchEventWorkingArray[1] = ev.getY();

        screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);

        ev.setLocation(mDispatchTouchEventWorkingArray[0],
                       mDispatchTouchEventWorkingArray[1]);

        return super.dispatchTouchEvent(ev);
    }

    private void screenPointsToScaledPoints(float[] array) {
        mTranslateMatrixInverse.mapPoints(array);
        mScaleMatrixInverse.mapPoints(array);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mOnTouchEventWorkingArray[0] = ev.getX();
        mOnTouchEventWorkingArray[1] = ev.getY();

        scalePointsToScreenPoints(mOnTouchEventWorkingArray);

        ev.setLocation(mOnTouchEventWorkingArray[0], mOnTouchEventWorkingArray[1]);
        //Listener to access all touchevent
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;

                //Save the ID of this pointer
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                //Find the index of the active pointer and fetch its position
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;
                mTranslateMatrix.preTranslate(dx, dy);
                mTranslateMatrix.invert(mTranslateMatrixInverse);

                //Update last position
                mLastTouchX = x;
                mLastTouchY = y;

                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                //Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    //This was our active pointer going up.
                    //Choose a new active pointer and adjust accordingly
                    //Choose the finger that is left on the screen
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void scalePointsToScreenPoints(float[] array) {
        mScaleMatrix.mapPoints(array);
        mTranslateMatrix.mapPoints(array);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            if (detector.isInProgress()) {
                //Content to zoom about the focal point of the gesture
                mFocusX = detector.getFocusX();
                mFocusY = detector.getFocusY();
            }
            //Prevent from scaling to too big or small a size
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            mScaleMatrix.setScale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);
            mScaleMatrix.invert(mScaleMatrixInverse);

            invalidate();
            requestLayout();   //Invalidate the layout of this view and its child views

            return true;
        }
    }
}
