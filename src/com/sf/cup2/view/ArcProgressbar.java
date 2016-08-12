package com.sf.cup2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class ArcProgressbar extends View {

    private int bgStrokeWidth = 60;
    private int barStrokeWidth = 60;
    private int bgColor = Color.GRAY;
    private int barColor = Color.RED;
    private int smallBgColor = Color.WHITE;
    private int progress = 100;
    //private int angleOfMoveCircle = 140;
    private int startAngle = 140; // 移动小园的起始角度。
    private int endAngle = 260;
    private Paint mPaintBar = null;
    private Paint mPaintSmallBg = null;
    private Paint mPaintBg = null;
    private Paint mPaintCircle = null;
    private RectF rectBg = null;
    /**
     * 直徑。
     */
    private int diameter = 450;
 
    private boolean showSmallBg = false;// 是否显示小背景。

	
    public ArcProgressbar(Context context) {
        super(context);
    }
 
    public ArcProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init(canvas);
    }
 
    private void init(Canvas canvas) {
        // 画弧形的矩阵区域。
        rectBg = new RectF(50, 50, diameter, diameter);
 
        // 计算弧形的圆心和半径。
        int cx1 = (diameter + 50) / 2;
        int cy1 = (diameter + 50) / 2;
        int arcRadius = (diameter - 50) / 2;
        // ProgressBar结尾和开始画2个圆，实现ProgressBar的圆角。
        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setColor(bgColor);
         
        canvas.drawCircle(
                (float) (cx1 + arcRadius * Math.cos(startAngle * 3.14 / 180)),
                (float) (cy1 + arcRadius * Math.sin(startAngle * 3.14 / 180)),
                bgStrokeWidth / 2, mPaintCircle);// 起点的圆
 

        canvas.drawCircle(
                (float) (cx1 + arcRadius
                        * Math.cos((180 - startAngle) * 3.14 / 180)),
                (float) (cy1 + arcRadius
                        * Math.sin((180 - startAngle) * 3.14 / 180)),
                bgStrokeWidth / 2, mPaintCircle);// 终点的圆
 
        // 弧形背景。
        mPaintBg = new Paint();
        mPaintBg.setAntiAlias(true);
        mPaintBg.setStyle(Style.STROKE);
        mPaintBg.setStrokeWidth(bgStrokeWidth);
        mPaintBg.setColor(bgColor);
        canvas.drawArc(rectBg, startAngle, endAngle, false, mPaintBg);
 

 
        // 弧形ProgressBar。
        mPaintBar = new Paint();
        mPaintBar.setAntiAlias(true);
        mPaintBar.setStyle(Style.STROKE);
        mPaintBar.setStrokeWidth(barStrokeWidth);
        mPaintBar.setColor(barColor);
        canvas.drawArc(rectBg, startAngle, progress, false, mPaintBar);
 
        // 随ProgressBar移动的圆。
            mPaintCircle.setColor(barColor);
            canvas.drawCircle(
                    (float) (cx1 + arcRadius
                            * Math.cos(startAngle * 3.14 / 180)),
                    (float) (cy1 + arcRadius
                            * Math.sin(startAngle * 3.14 / 180)),
                    bgStrokeWidth / 2, mPaintCircle);
            
            canvas.drawCircle(
                    (float) (cx1 + arcRadius
                            * Math.cos((180 - startAngle - progress) * 3.14 / 180)),
                    (float) (cy1 + arcRadius
                            * Math.sin((180 - startAngle - progress) * 3.14 / 180)),
                    bgStrokeWidth / 2, mPaintCircle);// 终点的圆
            canvas.drawCircle(
                    (float) (cx1 + arcRadius
                            * Math.cos((180 - startAngle) * 3.14 / 180)),
                    (float) (cy1 + arcRadius
                            * Math.sin((180 - startAngle) * 3.14 / 180)),
                    bgStrokeWidth / 2, mPaintCircle);// 终点的圆
        invalidate();
    }
 
    /**
     * 
     * @param progress
     */
    public void addProgress(int _progress) {
        progress += _progress;
        System.out.println(progress);
        if (progress > endAngle) {
            progress = 0;
        }
        invalidate();
    }
 
    /**
     * 设置弧形背景的画笔宽度。
     */
    public void setBgStrokeWidth(int bgStrokeWidth) {
        this.bgStrokeWidth = bgStrokeWidth;
    }
 
    /**
     * 设置弧形ProgressBar的画笔宽度。
     */
    public void setBarStrokeWidth(int barStrokeWidth) {
        this.barStrokeWidth = barStrokeWidth;
    }
 
    /**
     * 设置弧形背景的颜色。
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }
 
    /**
     * 设置弧形ProgressBar的颜色。
     */
    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }
 
    /**
     * 设置弧形小背景的颜色。
     */
    public void setSmallBgColor(int smallBgColor) {
        this.smallBgColor = smallBgColor;
    }
 
    /**
     * 设置弧形的直径。
     */
    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }
 
    /**
     * 是否显示小背景。
     */
    public void setShowSmallBg(boolean showSmallBg) {
        this.showSmallBg = showSmallBg;
    }
 

 

}
