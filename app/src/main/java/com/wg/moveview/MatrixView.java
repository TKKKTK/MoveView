package com.wg.moveview;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.wg.moveview.detector.RotateGestureDetector;

public class MatrixView extends View implements
        ScaleGestureDetector.OnScaleGestureListener,
        RotateGestureDetector.OnRotateGestureListener {
    private Paint mSrcPaint,mDesPaint,mCenterPaint;
    private RectF mSrcRectF,mDesRectF;
    private Matrix matrix;
    private float degress;
    private float scaleValue;

    private ScaleGestureDetector mScaleGestureDetector;
    private RotateGestureDetector mRotateGestureDetector;

    private float mRotationThreshold = 5f; // 旋转阈值，超过该阈值认为是旋转手势
    private float mScaleThreshold = 0.01f; // 缩放阈值，超过该阈值认为是缩放手势

    public MatrixView(Context context) {
        this(context,null);
    }

    public MatrixView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MatrixView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScaleGestureDetector = new ScaleGestureDetector(context,this);
        mRotateGestureDetector = new RotateGestureDetector(context,this);
        scaleValue = 1.0f;
        initView();


    }

    /**
     * 设置旋转时的属性动画
     * @param anggle
     * @param centerX
     * @param centerY
     */
    private void setAnggle(float anggle, float centerX, float centerY){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this,"degress",0,anggle);
        objectAnimator.setDuration(70);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(0);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                matrix.postRotate(degress,centerX,centerY);
                invalidate();
            }
        });
    }

    private void setScale(float scale){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this,"scaleValue",scaleValue,scale);
        objectAnimator.setDuration(70);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(0);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                matrix.postRotate(degress,centerX,centerY);
                invalidate();
            }
        });
    }

    public void setDegress(float degress) {
        this.degress = degress;
    }

    public void setScaleValue(float scaleValue) {
        this.scaleValue = scaleValue;
    }

    private void  initView(){
        mSrcPaint = new Paint();
        mSrcPaint.setAntiAlias(true);
        mSrcPaint.setDither(true);
        mSrcPaint.setStyle(Paint.Style.STROKE);
        mSrcPaint.setStrokeWidth(dpToPx(2f));
        mSrcPaint.setColor(Color.RED);

        mDesPaint = new Paint();
        mDesPaint.setAntiAlias(true);
        mDesPaint.setDither(true);
        mDesPaint.setStyle(Paint.Style.STROKE);
        mDesPaint.setStrokeWidth(dpToPx(2f));
        mDesPaint.setColor(Color.GREEN);

        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setDither(true);
        mCenterPaint.setColor(Color.RED);
        mCenterPaint.setStyle(Paint.Style.FILL);
        mCenterPaint.setStrokeWidth(dpToPx(5f));

        mSrcRectF = new RectF();
        mDesRectF = new RectF();

        matrix = new Matrix();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = getWidth()/2;
        float y = getHeight()/2;

        canvas.drawPoint(x,y,mCenterPaint);
        mSrcRectF.set(x - dpToPx(100), y - dpToPx(100),x + dpToPx(100), y + dpToPx(100));
        matrix.postRotate(degress,x,y);
        matrix.postScale(scaleValue,scaleValue,x,y);
        canvas.concat(matrix);
        canvas.drawRect(mSrcRectF,mDesPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
         mRotateGestureDetector.onTouchEvent(event);
         mScaleGestureDetector.onTouchEvent(event);

        // 获取旋转手势的角度变化
        float rotationDegreesDelta = Math.abs(mRotateGestureDetector.getRotationDegreesDelta());

        // 获取缩放手势的变化
        float scaleFactor = Math.abs(mScaleGestureDetector.getScaleFactor() - 1.0f);

        // 判断旋转手势和缩放手势的变化是否超过阈值
        boolean isRotating = rotationDegreesDelta > mRotationThreshold;
        boolean isScaling = scaleFactor > mScaleThreshold;

        // 如果旋转手势和缩放手势都超过阈值，则判断哪个手势变化大，并处理对应手势
        if (isRotating && isScaling) {
            if (rotationDegreesDelta > scaleFactor) {
                // 处理旋转手势
                float centerX = mRotateGestureDetector.getCurrCenterPoint().x;
                float centerY = mRotateGestureDetector.getCurrCenterPoint().y;
                Log.d("MatrixView", "onRotate:  detector.getTimeDelta ==> " + mRotateGestureDetector.getTimeDelta());
                setAnggle(-mRotateGestureDetector.getRotationDegreesDelta(),centerX,centerY);
                return true; // 消费事件，不传递给下一级处理
            } else {
                // 处理缩放手势
                setScale(mScaleGestureDetector.getScaleFactor());
                return true; // 消费事件，不传递给下一级处理
            }
        }
        return true;
    }

    private float dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        //setScale(scaleGestureDetector.getScaleFactor());
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        float centerX = detector.getCurrCenterPoint().x;
        float centerY = detector.getCurrCenterPoint().y;
        Log.d("MatrixView", "onRotate:  detector.getTimeDelta ==> " + detector.getTimeDelta());
        setAnggle(-detector.getRotationDegreesDelta(),centerX,centerY);
        return false;
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
//        float centerX = detector.getCurrCenterPoint().x;
//        float centerY = detector.getCurrCenterPoint().y;
//        Log.d("MatrixView", "onRotate:  detector.getTimeDelta ==> " + detector.getTimeDelta());
//        setAnggle(-detector.getRotationDegreesDelta(),centerX,centerY);
        return true;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector) {

    }
}
