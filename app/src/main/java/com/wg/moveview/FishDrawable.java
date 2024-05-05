package com.wg.moveview;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.provider.DocumentsContract;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FishDrawable extends Drawable {

    private Path mPath;
    private Paint mPaint;

    //除鱼身外的所有透明度
    private final static int OTHER_ALPHA = 110;
    //鱼身透明度
    private final static int BODY_ALPHA = 160;
    //转弯更自然的重心（身体的中心点）
    private PointF middlePoint;

    private PointF headPoint;

    public void setFishMainAngle(float fishMainAngle) {
        this.fishMainAngle = fishMainAngle;
    }

    //鱼的主角度
    private float fishMainAngle = 90;
    //鱼头的半径
    public final static float Head_raduis = 60;
    //鱼身长度
    private final static float BODY_LENGTH = 3.2f * Head_raduis;
    //寻找鱼鲤开始点的线长
    private final static float FIND_FINS_LENGTH = 0.9f * Head_raduis;
    //鱼鲤的长度
    private final static float FINS_LENGTH = 1.3f * Head_raduis;
    //尾部大圆的半径
    private final float BIG_CIRCLE_RADIUS = Head_raduis * 0.7f;
    //尾部中圆的半径
    private final float MIDDLE_CIRCLE_RADIUS = BIG_CIRCLE_RADIUS * 0.6f;
    //尾部小圆的半径
    private final float SMALL_CIRCLE_RADIUS = MIDDLE_CIRCLE_RADIUS * 0.4f;
    //寻找尾部中圆圆心的线长
    private final float FIND_MIDDLE_CIRCLE_LENGTH = BIG_CIRCLE_RADIUS + MIDDLE_CIRCLE_RADIUS;
    //寻找尾部小圆圆心的线长
    private final float FIND_SMALL_CIRCLE_LENGTH = MIDDLE_CIRCLE_RADIUS * (0.4f + 2.7f);
    //寻找大三角形底边中心点的线长
    private final float FIND_TRIANGLE_LENGTH = MIDDLE_CIRCLE_RADIUS * 2.7f;

    private float currentValue = 0;

    public FishDrawable(){
        init();
    }

    private void init() {
        mPath = new Path(); //路径
        mPaint = new Paint(); // 画笔
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setARGB(OTHER_ALPHA,244,92,71);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true); //防抖
        //设置中心点坐标
        middlePoint = new PointF(4.19f * Head_raduis,4.19f * Head_raduis);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,360);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                 currentValue = (float) animator.getAnimatedValue();
                 invalidateSelf();
            }
        });
        valueAnimator.start();
    }

    /**
     *
     * @param startPoint 起始点
     * @param length 两点的距离
     * @param angle 角度
     * @return
     */
    public static PointF calculatPoint(PointF startPoint,float length,float angle){
        //cos
        float deltaX = (float) (Math.cos(Math.toRadians(angle)) * length);
        float deltaY = (float) (Math.sin(Math.toRadians(angle - 180)) * length);
        return new PointF(startPoint.x + deltaX,startPoint.y + deltaY);
    }

    /**
     * 绘制方法 等同于自定义view中的onDraw
     * @param canvas
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        float fishAngle = (float) (fishMainAngle + Math.sin(Math.toRadians(currentValue)) * 10);

        //绘制鱼头
        headPoint = calculatPoint(middlePoint,BODY_LENGTH/2,fishAngle);
        canvas.drawCircle(headPoint.x,headPoint.y,Head_raduis,mPaint);
        //绘制右鱼鳍
        PointF rightFinshPoint = calculatPoint(headPoint,FIND_FINS_LENGTH,fishAngle - 110);
        makeFins(canvas,rightFinshPoint,fishAngle,true);
        //绘制左鱼鳍
        PointF leftFinshPoint = calculatPoint(headPoint,FIND_FINS_LENGTH,fishAngle + 110);
        makeFins(canvas,leftFinshPoint,fishAngle,false);

        //身体底部的中心点
        PointF bodyBottomCenterPoint = calculatPoint(headPoint,BODY_LENGTH,fishAngle - 180);

        //画节肢1
        PointF middleCircleCenterPoint = makeSegment(canvas,bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,MIDDLE_CIRCLE_RADIUS,
                FIND_MIDDLE_CIRCLE_LENGTH,fishAngle,true);
        //画节肢2
//        PointF middleCircleCenterPoint = calculatPoint(bodyBottomCenterPoint,
//                FIND_MIDDLE_CIRCLE_LENGTH,fishAngle - 180);
        makeSegment(canvas,middleCircleCenterPoint,MIDDLE_CIRCLE_RADIUS,SMALL_CIRCLE_RADIUS,
                FIND_SMALL_CIRCLE_LENGTH,fishAngle,false);
        //画尾巴
        makeTriangle(canvas,middleCircleCenterPoint, FIND_TRIANGLE_LENGTH,
                BIG_CIRCLE_RADIUS,fishAngle);
        makeTriangle(canvas,middleCircleCenterPoint, FIND_TRIANGLE_LENGTH - 10,
                BIG_CIRCLE_RADIUS - 20,fishAngle);

        //绘制身体
        makeBody(canvas, headPoint, bodyBottomCenterPoint, fishAngle);

    }

    /**
     * 绘制鱼鳍
     * @param canvas
     * @param startPoint
     * @param fishAngle
     * @param isRightFins
     */
    private void makeFins(Canvas canvas, PointF startPoint, float fishAngle,boolean isRightFins) {
        float controlAngle = 115;
        PointF endPoint = calculatPoint(startPoint,FINS_LENGTH,fishAngle - 180);
        PointF controlPoint = calculatPoint(startPoint, (float) (1.8f * FINS_LENGTH * Math.abs(Math.sin(Math.toRadians(currentValue)))),
                isRightFins ? fishAngle - controlAngle : fishAngle + controlAngle);
        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.quadTo(controlPoint.x,controlPoint.y, endPoint.x, endPoint.y);
        canvas.drawPath(mPath,mPaint);
    }

    private void makeBody(Canvas canvas,PointF headPoint,
                          PointF bodyBottomCenterPoint,float fishAngle){
        //身体的四个点
        PointF topLeftPoint = calculatPoint(headPoint, Head_raduis, fishAngle + 90);
        PointF topRightPoint = calculatPoint(headPoint,Head_raduis,fishAngle - 90);
        PointF bottomLeftPoint = calculatPoint(bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,
                fishAngle + 90);
        PointF bottomRightPoint = calculatPoint(bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,
                fishAngle - 90);

        //二阶贝塞尔曲线的控制点,决定鱼的胖瘦
        PointF controlLeft = calculatPoint(headPoint,BODY_LENGTH * 0.56f,
                fishAngle + 130);
        PointF controlRight = calculatPoint(headPoint,BODY_LENGTH * 0.56f,
                fishAngle - 130);

        //画身体
        mPath.reset();
        mPath.moveTo(topLeftPoint.x, topLeftPoint.y);
        mPath.quadTo(controlLeft.x, controlLeft.y, bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        mPath.quadTo(controlRight.x, controlRight.y, topRightPoint.x, topRightPoint.y);
        mPaint.setAlpha(BODY_ALPHA);
        canvas.drawPath(mPath,mPaint);
    }

    /**
     * 绘制节肢
     * @param canvas
     * @param bottomCenterPoint
     * @param bigRadius
     * @param smallRadius
     * @param findSmallCircleLength
     * @param fishAngle
     * @param hasBigCircle
     */
    private PointF makeSegment(Canvas canvas, PointF bottomCenterPoint, float bigRadius,
                             float smallRadius,float findSmallCircleLength, float fishAngle,
                             boolean hasBigCircle){
       float segmentAngle;
       if (hasBigCircle){
           segmentAngle = (float) (fishMainAngle + Math.cos(Math.toRadians(currentValue * 2)) * 20);
       }else {
           segmentAngle = (float) (fishMainAngle + Math.sin(Math.toRadians(currentValue * 3)) * 20);
       }

        //根据上底中心点计算下底中心点
        PointF upperCenterPoint = calculatPoint(bottomCenterPoint,findSmallCircleLength,
                segmentAngle - 180);
        //计算梯形的四个点
        PointF bottomLeftPoint = calculatPoint(bottomCenterPoint,bigRadius,segmentAngle + 90);
        PointF bottomRightPoint = calculatPoint(bottomCenterPoint,bigRadius,segmentAngle - 90);
        PointF upperLeftPoint = calculatPoint(upperCenterPoint,smallRadius,segmentAngle + 90);
        PointF upperRightPoint = calculatPoint(upperCenterPoint,smallRadius,segmentAngle - 90);
        if (hasBigCircle){
            //画大圆
            canvas.drawCircle(bottomCenterPoint.x,bottomCenterPoint.y,bigRadius,mPaint);
        }
        //画小圆
        canvas.drawCircle(upperCenterPoint.x,upperCenterPoint.y,smallRadius,mPaint);
        //画梯形
        mPath.reset();
        mPath.moveTo(bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.lineTo(upperLeftPoint.x, upperLeftPoint.y);
        mPath.lineTo(upperRightPoint.x, upperRightPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        canvas.drawPath(mPath,mPaint);

        return upperCenterPoint;
    }

    /**
     * 绘制尾部三角形
     * @param canvas
     * @param startPoint
     * @param findCenterLength
     * @param findEdgeLength
     * @param fishAngle
     */
    private void makeTriangle(Canvas canvas,PointF startPoint,
                              float findCenterLength,float findEdgeLength,float fishAngle){
        float triangleAngle = (float) (fishMainAngle + Math.sin(Math.toRadians(currentValue * 3)) * 30);
        //三角形底边的中心点
        PointF centerPoint = calculatPoint(startPoint,findCenterLength,triangleAngle - 180);
        //三角形底边的两点
        PointF leftPoint = calculatPoint(centerPoint,findEdgeLength,triangleAngle + 90);
        PointF rightPoint = calculatPoint(centerPoint,findEdgeLength,triangleAngle - 90);

        //绘制三角形
        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(leftPoint.x,leftPoint.y);
        mPath.lineTo(rightPoint.x,rightPoint.y);
        canvas.drawPath(mPath,mPaint);
    }

    /**
     * 设置透明度
     * @param i
     */
    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    /**
     * 设置颜色过滤器
     * @param colorFilter
     */
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
         mPaint.setColorFilter(colorFilter);
    }

    /**
     * 获取透明度
     * @return
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (8.38f * Head_raduis);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (8.38f * Head_raduis);
    }

    public PointF getMiddlePoint() {
        return middlePoint;
    }

    public PointF getHeadPoint() {
        return headPoint;
    }
}
