package com.wg.moveview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DegreesFixedView extends View {
    private Paint mPaint;
    private RectF mRotateRectF;
    private PointF mCenterPointF; //屏幕中心控制点
    private Matrix mMatrix;
    private SparseArray<PointF> mPrevPointFs;
    private SparseArray<PointF> mCurrPointFs;

    private float mDegrees = 0f;

    private Bitmap mIconBitmap;

    public DegreesFixedView(Context context) {
        this(context,null);
    }

    public DegreesFixedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DegreesFixedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(dpToPx(2f));

        mRotateRectF = new RectF();
        mCenterPointF = new PointF();
        mMatrix = new Matrix();

        mPrevPointFs = new SparseArray<>();
        mCurrPointFs = new SparseArray<>();

        mIconBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.support);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCenterPointF.set(getWidth() / 2, getHeight() / 2);
        mRotateRectF.set(mCenterPointF.x - dpToPx(100f), mCenterPointF.y - dpToPx(100f),
                mCenterPointF.x + dpToPx(100f), mCenterPointF.y + dpToPx(100f));

        // 获取矩形的中心点坐标
        float rectCenterX = mRotateRectF.right;
        float rectCenterY = mRotateRectF.top;

        // 保存当前的 Canvas 状态
        int saveCount = canvas.save();

        // 应用 Matrix 变换
        canvas.concat(mMatrix);

        // 绘制旋转矩形
        canvas.drawRect(mRotateRectF, mPaint);

        // 恢复之前保存的 Canvas 状态，以便图标的位置不受影响
        canvas.restoreToCount(saveCount);

        // 将图标的位置设置为矩形的中心点，并应用矩阵变换
        float[] iconPosition = {rectCenterX, rectCenterY};
        mMatrix.mapPoints(iconPosition);

        // 绘制图标
        canvas.drawBitmap(mIconBitmap, iconPosition[0] - mIconBitmap.getWidth() / 2,
                iconPosition[1] - mIconBitmap.getHeight() / 2, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                int pointerIndex = event.getActionIndex();
                int id = event.getPointerId(pointerIndex);

                PointF last = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
                mPrevPointFs.put(id, last);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2){
                    PointF mCurrPoint1 = new PointF();
                    PointF mCurrPoint2 = new PointF();

                    PointF mPrevPoint1 = new PointF();
                    PointF mPrevPoint2 = new PointF();

                    int[] keys = new int[2];

                    for (int i = 0; i < 2; i++) {
                        id = event.getPointerId(i);
                        keys[i] = id;
                        PointF currentPointer = new PointF(event.getX(i),event.getY(i));
                        mCurrPointFs.put(id,currentPointer);
                    }

                    mCurrPoint1.set(mCurrPointFs.get(keys[0]).x,mCurrPointFs.get(keys[0]).y);
                    mCurrPoint2.set(mCurrPointFs.get(keys[1]).x,mCurrPointFs.get(keys[1]).y);

                    float mCurrDistanceX = mCurrPoint2.x - mCurrPoint1.x;
                    float mCurrDistanceY = mCurrPoint2.y - mCurrPoint1.y;

                    float mCurrDistance = (float) Math.sqrt(Math.pow(mCurrDistanceX,2) + Math.pow(mCurrDistanceY,2));

                    mPrevPoint1.set(mPrevPointFs.get(keys[0]).x, mPrevPointFs.get(keys[0]).y);
                    if (mPrevPointFs.get(keys[1]) != null) {
                        mPrevPoint2.set(mPrevPointFs.get(keys[1]).x, mPrevPointFs.get(keys[1]).y);
                    }

                    float mPrevDistanceX = mPrevPoint2.x - mPrevPoint1.x;
                    float mPrevDistanceY = mPrevPoint2.y - mPrevPoint1.y;

                    float mPrevDistance = (float) Math.sqrt(Math.pow(mPrevDistanceX,2) + Math.pow(mPrevDistanceY,2));

//                    boolean isScale = false;
                    if (mCurrDistance != mPrevDistance && Math.abs(mCurrDistance - mPrevDistance) >= 10f){
                        float scaleValue = mCurrDistance/mPrevDistance;
                        mMatrix.postScale(scaleValue,scaleValue, mCenterPointF.x, mCenterPointF.y);
//                        isScale = true;
                    }

                    //计算偏差的角度
                    double diffRadians = Math.atan2(mPrevDistanceY, mPrevDistanceX) - Math.atan2(mCurrDistanceY, mCurrDistanceX);
                    float degrees = (float) (diffRadians * 180 / Math.PI);

                    if (Math.abs(mDegrees - degrees) >= 0.1f && Math.abs(degrees) <= 120f){
                        mMatrix.postRotate(-degrees * 0.3f, mCenterPointF.x, mCenterPointF.y);

                        mDegrees = degrees;
                    }

                    for (int i = 0; i < mCurrPointFs.size(); i++) {
                        int key = mCurrPointFs.keyAt(i);
                        mPrevPointFs.put(key,mCurrPointFs.get(key));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mPrevPointFs.clear();
                mCurrPointFs.clear();
                break;
            case MotionEvent.ACTION_POINTER_UP:
//                pointerIndex = event.getActionIndex();
//                id = event.getPointerId(pointerIndex);
//                mPrevPointFs.remove(id);
//                mCurrPointFs.remove(id);
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
