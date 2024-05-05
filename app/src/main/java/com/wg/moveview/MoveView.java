package com.wg.moveview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

public class MoveView extends View implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{

    private float x = 100, y = 100; // 视图的当前位置
    private Paint paint; // 用于绘制视图的画笔
    private Matrix matrix; //用于存储矩阵变换对象
    private RectF sourceRectF; //用于设置画布的原始矩阵大小
    private GestureDetector gestureDetector; // 手势检测器

    private Bitmap bitmap = null;

    public MoveView(Context context) {
        this(context,null);
    }

    public MoveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MoveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化画笔
        paint = new Paint();
        paint.setColor(Color.GRAY);

        matrix = new Matrix();
        //matrix.postRotate(0);
        //matrix.postScale(2f,2f);
        //matrix.postTranslate(150,150);
        sourceRectF = new RectF(Float.MIN_VALUE,Float.MIN_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        matrix.mapRect(sourceRectF);

        float width = sourceRectF.width();
        float height = sourceRectF.height();

        // 初始化手势检测器
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.GRAY);
        canvas.concat(matrix);
        canvas.drawRect(sourceRectF,paint);
        //canvas.saveLayer(sourceRectF,paint);
        //canvas.clipRect(sourceRectF);
        //canvas.save();
        paint.setColor(Color.RED);
        canvas.drawCircle(100,100,60,paint);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * 用户按下时触发 return true 表示消费了该事件
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        CustemTextView custemTextView = new CustemTextView(getContext(),"我喜欢你 ");
        custemTextView.setX(motionEvent.getX() - custemTextView.getBasePoint().x);
        custemTextView.setY(motionEvent.getY() - custemTextView.getBasePoint().y);
        ((ViewGroup)getParent()).addView(custemTextView);
        postInvalidate();
        return true;
    }

    private Bitmap convertViewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 当按下时一段时间内（通常是 100 毫秒）没有触发其他动作时触发。
     * 可用于在按下时显示一些反馈，比如高亮按钮。
     * @param motionEvent
     */
    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    /**
     * 当手指抬起时触发。
     * 可用于处理单击事件，比如响应单击动作。
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    /**
     *当手指在屏幕上滑动时触发。
     * 可用于处理滑动事件，比如实现滑动效果。
     * @param motionEvent
     * @param motionEvent1
     * @param v
     * @param v1
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        float dx = motionEvent1.getX() - motionEvent.getX();
        float dy = motionEvent1.getY() - motionEvent.getY();
        matrix.postTranslate(dx, dy);
        //matrix.mapRect(sourceRectF);

        float width = sourceRectF.width();
        float height = sourceRectF.height();
        postInvalidate();
        return true;
    }

    /**
     *当手指长时间按住时触发。
     * 可用于处理长按事件，比如长按删除某项。
     * @param motionEvent
     */
    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    /**
     *当手指在屏幕上快速滑动后松开时触发。
     * 可用于处理快速滑动事件，比如实现列表的快速滑动。
     * @param motionEvent
     * @param motionEvent1
     * @param v
     * @param v1
     * @return
     */
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        return false;
    }

    /**
     *在确认用户不是双击时触发。
     * 用于处理单击事件，与 onSingleTapUp() 类似，但是会等待一段时间确认是否为双击事件。
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    /**
     *当用户双击时触发。
     * 可用于处理双击事件，比如放大或缩小图片。
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
         matrix.postScale(2f,2f);
         postInvalidate();
        return true;
    }

    /**
     *双击事件的其他事件（例如按下、移动和抬起）。
     * 通常不需要重写此方法。
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }
}
