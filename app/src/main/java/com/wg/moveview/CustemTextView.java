package com.wg.moveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class CustemTextView extends View {
    private Paint textPaint,bgPaint;
    private String textInfo = "我喜欢你,你喜欢我吗？";
    private int padding = dpToPx(8f);
    private Point basePoint;
    private RectF backgroundRect;
    private RectF poleRect;
    int textWidth = 0;
    int textHeight = 0;

    public CustemTextView(Context context,String text) {
        super(context);
        textInfo = text;
        init();
    }

    private void init(){
         textPaint = new Paint();
         textPaint.setColor(Color.WHITE);
         textPaint.setStyle(Paint.Style.FILL);
         textPaint.setStrokeWidth(3f);
         textPaint.setDither(true);
         textPaint.setAntiAlias(true);
         textPaint.setTextAlign(Paint.Align.CENTER);
         textPaint.setTextSize(dpToPx(18f));

         bgPaint = new Paint();
         bgPaint.setColor(Color.GRAY);
         bgPaint.setStyle(Paint.Style.FILL);
         bgPaint.setAntiAlias(true);

         basePoint = new Point();

        // 测量文字宽度并设置文本大小
        if (textInfo != null && !textInfo.isEmpty()) {
            textWidth = (int) textPaint.measureText(textInfo)  + padding * 2;
            textHeight = (int) (textPaint.descent() - textPaint.ascent()) + padding * 2;
        }

        // 绘制背景
        backgroundRect = new RectF(0, 0, textWidth, textHeight);
        int poleWidth = dpToPx(3f);
        //绘制杠子
        poleRect = new RectF(textWidth/2 -poleWidth/2,textHeight,textWidth/2 + poleWidth/2,textHeight + dpToPx(80f));
        basePoint.set((int) (backgroundRect.width()/2),(int) poleRect.bottom);

        Log.d("CustemTextView", "onDraw: ");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景
        canvas.drawRoundRect(backgroundRect, textHeight/2, textHeight/2, bgPaint);
        //绘制文字
        if (textInfo != null && !textInfo.isEmpty()) {
            float textX = textWidth / 2;
            float textY = textHeight / 2 - (textPaint.ascent() + textPaint.descent()) / 2; // 使文字居中
            canvas.drawText(textInfo, textX, textY, textPaint);
        }
        canvas.drawRect(poleRect,bgPaint);

    }

    public String getTextInfo() {
        return textInfo;
    }

    public void setTextInfo(String textInfo) {
        this.textInfo = textInfo;
        requestLayout(); // 重新请求布局
        invalidate(); // 重新绘制
    }

    public int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public Point getBasePoint() {
        return basePoint;
    }
}
