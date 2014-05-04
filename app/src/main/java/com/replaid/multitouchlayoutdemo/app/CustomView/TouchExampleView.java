package com.replaid.multitouchlayoutdemo.app.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.replaid.multitouchlayoutdemo.app.R;

/**
 * Created by desmond on 4/5/14.
 */
public class TouchExampleView extends View {
    private static final String TAG = "TouchExampleView";
    private static final int INVALID_POINTER_ID = -1;
    private Drawable mIcon;
    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    //The 'active pointer' is the one currently moving our object
    private int mActivePointerId = INVALID_POINTER_ID;

    //To provide scaling
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public TouchExampleView(Context context) {
        super(context);
        init(context);
    }

    public TouchExampleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchExampleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mIcon = context.getResources().getDrawable(R.drawable.ferrari);
        //.setBounds(rect) tells the Drawable where it is drawn and how large it should be
        //getIntrinsic will find the preferred size for the Drawable
        mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());

        //Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        mIcon.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //Let the ScaleGestureDetector inspect all events
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                //Remember where we started
                mLastTouchX = x;
                mLastTouchY = y;

                //Save the ID of this pointer
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                //Find the index of teh active pointer and fetch its position
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);


                //Only move if the ScaleGestureDetector isn't processing a gesture
                if (!mScaleDetector.isInProgress()) {
                    //Calculate the distance moved
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    //Move the object
                    mPosX += dx;
                    mPosY += dy;

                    //Invalidate to request a redraw
                    invalidate();
                }

                //Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

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
                    //This was our active pointer going up. Choose a new
                    //active pointer and adjust accordingly
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

    /**
     * ScaleGestureDetector supports: pinch zooming
     * GestureDetector is for several common single-pointer gestures
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            //Don't let the object get too small or too large
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }
}
