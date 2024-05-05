package com.wg.moveview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FishRelativeLayout extends RelativeLayout {

    private Paint mPaint;
    private ImageView ivFish;
    private FishDrawable fishDrawable;

    private float touchX = 0;
    private float touchY = 0;

    private float ripple = 0;
    private int alpha = 0;

    public FishRelativeLayout(Context context) {
        this(context,null);
    }

    public FishRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FishRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        //设置让onDraw执行
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);

        ivFish = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ivFish.setLayoutParams(layoutParams);
        fishDrawable = new FishDrawable();
        ivFish.setImageDrawable(fishDrawable);
        addView(ivFish);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(touchX,touchY,ripple * 150,mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();

        ObjectAnimator objectAnimator = ObjectAnimator.
                ofFloat(this,"ripple",0,1f).setDuration(1000);
        objectAnimator.start();

        makeTrail();

        return super.onTouchEvent(event);
    }

    private void makeTrail(){
        PointF fishRelativeMiddle = fishDrawable.getMiddlePoint();
        //起始点
        PointF fishMiddle = new PointF(ivFish.getX() + fishRelativeMiddle.x,
                ivFish.getY() + fishRelativeMiddle.y);
        PointF fishHead = new PointF(ivFish.getX() + fishDrawable.getHeadPoint().x,
                ivFish.getY() + fishDrawable.getHeadPoint().y);
        //结束点
        PointF touch = new PointF(touchX,touchY);
        float angle = includedAngle(fishMiddle,fishHead,touch);
        float delta = includedAngle(fishMiddle,new PointF(fishMiddle.x + 1,fishMiddle.y),fishHead);
        //控制鱼游动的贝塞尔曲线的控制点
        PointF controlPoint = FishDrawable.calculatPoint(fishMiddle,FishDrawable.Head_raduis * 1.6f,angle / 2 + delta);
        //构造一个路径
        Path path = new Path();
        path.moveTo(fishMiddle.x - fishRelativeMiddle.x, fishMiddle.y - fishRelativeMiddle.y);
        path.cubicTo(fishHead.x- fishRelativeMiddle.x, fishHead.y - fishRelativeMiddle.y,
                controlPoint.x - fishRelativeMiddle.x, controlPoint.y - fishRelativeMiddle.y,
                touchX - fishRelativeMiddle.x,touchY - fishRelativeMiddle.y);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivFish,"x",
                "y",path);
        objectAnimator.setDuration(2000);

        PathMeasure pathMeasure = new PathMeasure(path,false);
        float[] tan = new float[2];
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
               float fraction = animator.getAnimatedFraction();
               pathMeasure.getPosTan(pathMeasure.getLength() * fraction,null,tan);
               float angle = (float) Math.toDegrees(Math.atan2(-tan[1],tan[0]));
               fishDrawable.setFishMainAngle(angle);
            }
        });
        objectAnimator.start();
    }

    public static float includedAngle(PointF O,PointF A,PointF B){
        //OA*OB = (Ax - Ox) * (Bx - Ox) + (Ay - Oy)*(By - Oy)
        float AOB = (A.x - O.x) * (B.x - O.x) + (A.y - O.y) * (B.y - O.y);
        //OA的长度
        float OALength = (float) Math.sqrt((A.x - O.x) * (A.x - O.x) + (A.y - O.y) * (A.y - O.y));
        float OBLength = (float) Math.sqrt((B.x - O.x) * (B.x -O.x) + (B.y - O.y) * (B.y - O.y));
        float cosAOB = AOB / (OALength * OBLength);

        //计算AOB 的角度大小
        float angleAOB = (float) Math.toDegrees(Math.acos(cosAOB));
        float direction = (A.y - B.y) / (A.x - B.x) - (O.y - B.y) / (O.x - B.x);
        if (direction == 0){
           if (AOB >= 0){
               return 0;
           }else
               return 180;
        }else {
            if (direction > 0){
               return -angleAOB;
            }else {
                return angleAOB;
            }
        }
    }

    public void setRipple(float ripple) {
        alpha = (int) (150 * (1-ripple));
        this.ripple = ripple;
        invalidate();
    }
}
