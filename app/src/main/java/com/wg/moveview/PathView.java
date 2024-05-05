package com.wg.moveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PathView extends View {
    private Paint mPaint;
    private Path mPath;
    private float frequency = 2.0f; // 频率
    private float phase = 0; // 相位
    private float amplitude = 1.0f; // 振幅
    private Region mPathRegion;
    private Matrix matrix;
    private RectF bounds;
    private RectF destRectF;
    private Paint rectPaint;

    private List<RectF> destRectFs;
    private List<Matrix> matrixList;

    public PathView(Context context) {
        this(context,null);
    }

    public PathView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint(){
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mPaint.setDither(true);
         mPaint.setColor(Color.GREEN);
         mPaint.setStrokeWidth(dpToPx(10f));
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeCap(Paint.Cap.ROUND);

         rectPaint = new Paint();
         rectPaint.setAntiAlias(true);
         rectPaint.setDither(true);
         rectPaint.setColor(Color.RED);
         rectPaint.setStrokeWidth(dpToPx(2f));
         rectPaint.setStyle(Paint.Style.STROKE);

         mPath = new Path();
         matrix = new Matrix();

        destRectFs = new ArrayList<>();
        matrixList = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = (int) (getWidth() - dpToPx(20f));
        int height = getHeight();

        float centerY = height / 2;
        float unit = width / (2 * (float) Math.PI); // 用于缩放x轴，使一个周期完整显示

        float firstX = 0,firstY = 0;
        int index = 0;
        // 开始绘制正弦波
        for (int x = (int) dpToPx(20f); x < width; x++) {
            index ++;
            float y = (float) (Math.sin(frequency * (x / unit) + phase) * amplitude * 100); // 计算正弦值并调整振幅
            if (x == (int) dpToPx(20f)){
                mPath.moveTo(x,centerY - y);
                firstX = x;
                firstY = centerY - y;
            }else {
                mPath.lineTo(x,centerY - y);
                if (index % 6 == 0){
                     float tan = (firstY - centerY - y)/(firstX - x);
                     float angle = (float) Math.toDegrees(Math.atan(tan));
                     bounds = new RectF();
                     matrix = new Matrix();
                     bounds.set(firstX - dpToPx(10),firstY - dpToPx(10),firstX + dpToPx(10),firstY + dpToPx(10));
                     matrix.mapRect(bounds);
                     matrix.setRotate(angle,firstX,firstY);
                     canvas.save();
                     canvas.concat(matrix);
                     canvas.drawRect(bounds,rectPaint);
                     canvas.restore();
                     destRectFs.add(bounds);
                     //matrixList.add(matrix);

                     firstX = x;
                     firstY = centerY - y;
                }
            }




        }
        canvas.drawPath(mPath,mPaint);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(dpToPx(8f));
        canvas.drawPath(mPath,mPaint);

//        bounds = new RectF();
//        mPath.computeBounds(bounds,true);
//        mPaint.setColor(Color.BLUE);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(dpToPx(2f));
//
//        destRectF = new RectF();
//        matrix.mapRect(bounds);
//
//        matrix.postRotate(30f);
//
//        matrix.mapRect(destRectF,bounds);
//
//
//        int left = Math.round(bounds.left);
//        int top = Math.round(bounds.top);
//        int right = Math.round(bounds.right);
//        int bottom = Math.round(bounds.bottom);
//
//        mPathRegion = new Region();
//        mPathRegion.setPath(mPath,new Region(left,top,right,bottom));
//        canvas.concat(matrix);
//        canvas.drawRect(destRectF,mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x =  event.getX();
        float y =  event.getY();
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                boolean isNavi = destRectF.contains(x,y);
//                if (isNavi){
//                    Log.d("PathView", "onTouchEvent: ==> isNavi :" + isNavi);
//                }
//                break;
//        }
        return super.onTouchEvent(event);
    }

    public float dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density;
    }
}
