package com.wg.moveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawPathView extends View {
    private Paint mPaint;
    private SparseArray<Path> mPaths; // 用于存储每个手指的路径
    private static final int INVALID_POINTER_ID = -1;
    private SparseArray<Float> mLastXs; // 用于存储每个手指上一个触摸点的X坐标
    private SparseArray<Float> mLastYs; // 用于存储每个手指上一个触摸点的Y坐标
    private int mActivePointerId = INVALID_POINTER_ID; // 当前活动的手指ID

    public DrawPathView(Context context) {
        this(context, null);
    }

    public DrawPathView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(dpToPx(2f));
        mPaint.setColor(Color.RED);

        mPaths = new SparseArray<>();
        mLastXs = new SparseArray<>();
        mLastYs = new SparseArray<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mPaths.size(); i++) {
            int pointerId = mPaths.keyAt(i);
            Path path = mPaths.get(pointerId);
            canvas.drawPath(path, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                Log.d("DrawPathView", "onTouchEvent: pointerId ==>" + pointerId +"; pointerIndex ==> " +pointerIndex);
                Path path = new Path();
                path.moveTo(event.getX(pointerIndex), event.getY(pointerIndex));
                mPaths.put(pointerId, path);
                mLastXs.put(pointerId, event.getX(pointerIndex));
                mLastYs.put(pointerId, event.getY(pointerIndex));
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    float x = event.getX(i);
                    float y = event.getY(i);
                    Path p = mPaths.get(id);
                    if (p != null) {
                        float lastX = mLastXs.get(id);
                        float lastY = mLastYs.get(id);
                        p.quadTo(lastX, lastY, (lastX + x) / 2, (lastY + y) / 2);
                        mLastXs.put(id, x);
                        mLastYs.put(id, y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = event.getActionIndex();
                pointerId = event.getPointerId(pointerIndex);
                mPaths.remove(pointerId);
                mLastXs.remove(pointerId);
                mLastYs.remove(pointerId);
                break;
        }
        invalidate();
        return true;
    }

    private float dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density;
    }
}
