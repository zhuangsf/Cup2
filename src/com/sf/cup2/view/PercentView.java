package com.sf.cup2.view;

import com.sf.cup2.R;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;




/**
 * 用户击败百分比
 * 设计思路：首先定义最外层小圆点的半径，外层细圆弧的宽度，内存宽圆弧的宽度，两个圆弧中间间隔的宽度，
 * 然后利用旋转画布来绘制文本，接着绘制两个圆弧的底色，接着根据传过来的数据绘制渐变色圆弧和原点
 * 利用ValueAnimator来设置动画效果
 *
 * Created by Forrest on 16/8/12.
 */
public class PercentView extends View{
    private Paint paint;//画笔
    private Paint shaderPaint;//彩色画笔
    private Paint bitmapPaint;//图片画笔
    private Paint textPaint;//文字画笔
    /**控件宽度*/
    private int width;
    /**控件高度*/
    private int height;
    /**半径*/
    private int radius;
    /**外圆弧的宽度*/
    private float outerArcWidth;
    /**内部大圆弧的宽度*/
    private float insideArcWidth;
    /**两圆弧中间间隔距离*/
    private float spaceWidth;
    /**两圆弧中间间隔距离*/
    private float percentTextSize;
    /**最外层滑动小球的半径*/
    private float scrollCircleRadius;
    /**粉红底色*/
    private int pinkColor;
    /**黄色*/
    private int yellowColor;
    /**粉色红*/
    private int pinkRedColor;
    /**浅红*/
    private int redColor;
    /**深红*/
    private int deepRedColor;
    /**灰色*/
    private int grayColor;
    /**绿色*/
    private int greenColor;
    /**间隔的角度*/
    private double spaceAngle=22.5;
    /**两条圆弧的起始角度*/
    private double floatAngel=60;
    /**自定义的Bitmap*/
    private Bitmap mBitmap;
    /**自定义的画布，目的是为了能画出重叠的效果*/
    private Canvas mCanvas;
    /**时刻变化的Angel*/
    private double mAngel;
    /**内弧半径*/
    private float insideArcRadius;
    private double aimPercent=0;
    private float outerArcRadius;
    private float[] pos;                // 当前点的实际位置
    private float[] tan;                // 当前点的tangent值,用于计算图片所需旋转的角度
    private Bitmap mBitmapBackDeepRed;  // 箭头图片
    private Matrix mMatrix;             // 矩阵,用于对图片进行一些操作
    private RectF outerArea;            //外圈的矩形
    private String tag;
    private String aim;
    private int textSizeTag;//名列前茅字体大小
    private int textSizeAim;//击败百分比字体大小
    private String batteryLevel = "8";
    // 动效过程监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;
    //过程动画
    private ValueAnimator mValueAnimator;
    // 用于控制动画状态转换
    private Handler mAnimatorHandler;
    // 默认的动效周期 2s
    private int defaultDuration = 500;


    public PercentView(Context context) {
        super(context);
        initView(context);

    }

    public PercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public PercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    private void initView(Context context){
        shaderPaint=new Paint();
        textPaint=new Paint();

        paint=new Paint();
        paint.setStyle(Paint.Style.STROKE); //设置空心
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        bitmapPaint=new Paint();
        bitmapPaint.setStyle(Paint.Style.FILL);
        bitmapPaint.setAntiAlias(true);

        outerArcWidth = context.getResources().getDimensionPixelOffset(R.dimen.dp2);
        insideArcWidth = context.getResources().getDimensionPixelOffset(R.dimen.dp18);
        spaceWidth = context.getResources().getDimensionPixelOffset(R.dimen.dp12);
        scrollCircleRadius = context.getResources().getDimensionPixelOffset(R.dimen.dp4);
        percentTextSize = context.getResources().getDimensionPixelOffset(R.dimen.dp8);
        textSizeAim = context.getResources().getDimensionPixelOffset(R.dimen.sp40);
        textSizeTag = context.getResources().getDimensionPixelOffset(R.dimen.sp18);
        pinkColor = context.getResources().getColor(R.color.percent_pink);
        yellowColor = context.getResources().getColor(R.color.percent_yellow);
        pinkRedColor = context.getResources().getColor(R.color.percent_pink_red);
        redColor = context.getResources().getColor(R.color.percent_red);
        deepRedColor = context.getResources().getColor(R.color.percent_deep_red);
        grayColor = context.getResources().getColor(R.color.percent_gray);
        greenColor = context.getResources().getColor(R.color.percent_green);

        pos = new float[2];
        tan = new float[2];
        mBitmapBackDeepRed= BitmapFactory.decodeResource(context.getResources(), R.mipmap.blur_back_deep_red);
        mMatrix=new Matrix();
        

    }

    private int count=0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
  //      Log.i("PercentVIew", "开始绘制" + count);
        long startTime=System.currentTimeMillis();
        count++;
        width = getWidth(); //获取宽度
        height = getHeight();//获取高度
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas =new Canvas(mBitmap);

        radius= (int) (9 * height/(16*(1+Math.sin(Math.toRadians(spaceAngle)))));//获取最外园的半径
        insideArcRadius= radius;//内弧半径
//        Log.i("PercentVIew","最外园半径"+radius+" 高度为"+height+" 宽度为"+width);
//        Log.i(TAG,"最外园半径"+Math.sin(Math.toRadians(spaceAngle)));
//        paintPercentText(mCanvas);
        paintPercentBack(mCanvas);
        paintPercent(mAngel, aimPercent, mCanvas);
//        calculateItemPositions(aimPercent,increaseValue,mCanvas,mBitmapBackDeepRed);
        //将Bitmap画到Canvas
        paintText(mCanvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        long endTime=System.currentTimeMillis();
    //    Log.i("PercentVIew", "绘制结束" + (endTime-startTime));
    }

    /**
     * 旋转画布画刻度
     * @param canvas 画布
     */
    private void paintPercentText(Canvas canvas){
        paint.setTextSize(percentTextSize);
        paint.setColor(pinkColor);
        paint.setStrokeWidth(1);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i=0;i<=10;i++){
            //保存画布
            canvas.save();
            //旋转角度，第一个参数是旋转的角度、第二个参数和第三个参数是旋转中心点x和y
            canvas.rotate((float) (spaceAngle * i + -135 + spaceAngle), width / 2, radius);
            //画文字
            canvas.drawText(i * 10 + "", width / 2,  outerArcWidth + insideArcWidth + spaceWidth * 2, paint);
            canvas.restore();
        }
    }
    /**画两条线的底色*/
    private void paintPercentBack(Canvas canvas){
        paint.setColor(grayColor);
        paint.setStrokeWidth(outerArcWidth);//outerArcWidth
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);//设置为圆角
        paint.setAntiAlias(true);
        //绘制最外层圆条底色
//        outerArcRadius=radius-outerArcWidth;
//        outerArea= new RectF(width/2 - outerArcRadius, radius - outerArcRadius, width/2  + outerArcRadius, radius + outerArcRadius);
//        canvas.drawArc(outerArea,
//                (float) (180 - floatAngel),
//                (float) (180 + 2 * floatAngel), false, paint);
        //绘制里层大宽度弧形
        paint.setColor(pinkColor);
        paint.setStrokeWidth(insideArcWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(width / 2 - insideArcRadius, radius - insideArcRadius+insideArcWidth, width / 2 + insideArcRadius, radius + insideArcRadius+insideArcWidth),
                (float) (180 - floatAngel),
                (float) (180 + 2 * floatAngel), false, paint);

    }

    /***
     * 4个色值由浅到深分别是 ffd200 ff5656 fa4040 f60157
     * 等级划分：0-20% 再接再厉   21-60% 技高一筹   61-90% 名列前茅   90以上 理财达人
     * 绘制外层和内层的颜色线条
     * 主要用到Xfermode的SRC_ATOP显示上层绘制
     * setStrokeCap   Paint.Cap.ROUND设置为圆角矩形
     */
    private void paintPercent(double percent,double aimPercent,Canvas canvas){
        double roateAngel=percent*0.01*225;
        shaderPaint.setColor(yellowColor);
        shaderPaint.setStrokeCap(Paint.Cap.ROUND);
        shaderPaint.setAntiAlias(true);
        shaderPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));//shaderPaint.setColor(yellowColor);
        if (aimPercent>=0&&aimPercent<=20){
        }else if (aimPercent>20&&aimPercent<=60){
            int colorSweep[] = { yellowColor,pinkRedColor };
            float position[]={0.5f,0.7f};
            SweepGradient sweepGradient=new SweepGradient(width / 2, radius, colorSweep, position);
            shaderPaint.setShader(sweepGradient);
        }else if (aimPercent>60&&aimPercent<=90){
            int colorSweep[] = { yellowColor,pinkRedColor,redColor };
            float position[]={0.5f,0.7f,0.8f};
            SweepGradient sweepGradient=new SweepGradient(width / 2, radius, colorSweep, position);
            shaderPaint.setShader(sweepGradient);
        }else if (aimPercent>90){
            int colorSweep[] = {deepRedColor, yellowColor,yellowColor,pinkRedColor,redColor, deepRedColor};
            float position[]={0.2f,0.4f,0.5f,0.7f,0.9f,1.0f};
            SweepGradient sweepGradient=new SweepGradient(width / 2, radius, colorSweep, position);
            shaderPaint.setShader(sweepGradient);
        }
        if (aimPercent<=10){//目的是为了
            drawInsideArc((float) (180 - floatAngel), (float) roateAngel, canvas);
        }else if (aimPercent>10&&aimPercent<=20){
            drawInsideArc((float) (180 - floatAngel), (float) roateAngel, canvas);
        }else if (aimPercent>20&&aimPercent<=60){
            drawInsideArc((float) (180 - floatAngel), (float) (roateAngel-(spaceAngle-floatAngel)), canvas);
        }else if (aimPercent>60&&aimPercent<=90){
            drawInsideArc((float) (180 - floatAngel), (float) (roateAngel-(spaceAngle-floatAngel)), canvas);
        }else {
            drawInsideArc((float) (180 - floatAngel), (float) (roateAngel-2*(spaceAngle-floatAngel)), canvas);
        }


    }

    /***
     * 画内部圆环渐变
     * @param formDegree 起始角度
     * @param toDegree 旋转角度
     * @param canvas 画布
     */
    private void drawInsideArc(float formDegree ,float toDegree,Canvas canvas){
        shaderPaint.setStrokeWidth(insideArcWidth);
        shaderPaint.setStyle(Paint.Style.STROKE);
        //内弧半径
        canvas.drawArc(new RectF(width / 2 - insideArcRadius, radius - insideArcRadius+insideArcWidth, width / 2 + insideArcRadius, radius + insideArcRadius+insideArcWidth),
                formDegree,
                toDegree, false, shaderPaint);

    }

    /***
     * 4个色值由浅到深分别是 ffd200 ff5656 fa4040 f60157
     * 等级划分：0-20% 再接再厉   21-60% 技高一筹   61-90% 名列前茅   90以上 理财达人
     */
    private void paintText(Canvas canvas){
        if (!TextUtils.isEmpty(tag)&&!TextUtils.isEmpty(aim)){
            textPaint.setColor(deepRedColor);
            textPaint.setTextSize(textSizeTag);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setStrokeWidth(2);
            canvas.drawText("完成"+aimPercent+"%", width / 2, radius - textSizeTag, textPaint);
            textPaint.setColor(deepRedColor);
            textPaint.setTextSize(textSizeAim);
            textPaint.setStrokeWidth(1);
//            float leftLength=textPaint.measureText("你击败了");
//            float rightLength=textPaint.measureText("的用户");
//            float centerLength=textPaint.measureText(aim+"%");
//            float rightOffest=textSizeAim/2;
            canvas.drawText(tag,width/2,radius + textSizeAim/2, textPaint);
            
            
            textPaint.setColor(grayColor);
            textPaint.setTextSize(textSizeTag);
            canvas.drawText("目标  "+aim+"ml",width/2,radius + textSizeAim, textPaint);

            textPaint.setColor(greenColor);
            textPaint.setTextSize(textSizeTag);
            
            
            int batteryDrawableID = R.drawable.icon_batterylevel_8;
            if(batteryLevel.equals("8"))
            {
            	batteryDrawableID = R.drawable.icon_batterylevel_8;
            	canvas.drawText("100%",width/2,2*radius , textPaint);
            }else if(batteryLevel.equals("4"))
            {
            	batteryDrawableID = R.drawable.icon_batterylevel_4;
            	canvas.drawText("75%",width/2,2*radius , textPaint);
            }else if(batteryLevel.equals("2"))
            {
            	batteryDrawableID = R.drawable.icon_batterylevel_2;
            	canvas.drawText("50%",width/2,2*radius , textPaint);
            }else if(batteryLevel.equals("1"))
            {
            	batteryDrawableID = R.drawable.icon_batterylevel_1;
            	canvas.drawText("25%",width/2,2*radius , textPaint);
            }else if(batteryLevel.equals("0"))
            {
            	batteryDrawableID = R.drawable.icon_battery_low;
            	canvas.drawText("0%",width/2,2*radius , textPaint);
            }
            		
            
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), batteryDrawableID);  
            canvas.drawBitmap(bitmap, (width - bitmap.getWidth())/2,2*radius + 5, textPaint);  

        }


    }

    /**
     * 设置角度变化，刷新界面
     * @param aimPercent 目标百分比
     */
    public void setAngel(double aimPercent){
        //两边监测
        if (aimPercent<0){
            aimPercent=0;
        }else if (aimPercent>100){
            aimPercent=100;
        }
        this.aimPercent=aimPercent;
        initListener();

        initHandler();

        initAnimator();
        mValueAnimator.start();

    }

    
    public void setBatteryLevel(String level)
    {
    	batteryLevel = level;
    }
    /**
     * 设置文字
     * @param tag 名列前茅文案
     * @param aim 击败的百分比
     */
    public void setRankText(String tag,String aim){
        this.tag=tag;
        this.aim=aim;
        
        if(Integer.parseInt(aim) == 0)
        {
        	setAngel(0);
        }
        else
        	{
        	int percent = Integer.parseInt(tag) * 100 / Integer.parseInt(aim);
       
        setAngel(percent);
         	}
        mAnimatorHandler.sendEmptyMessage(1);

    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngel = (float) animation.getAnimatedValue()*aimPercent;
//                Log.i("TAG", "mAnimatorValue="+mAnimatorValue);
                invalidate();
            }
        };

        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // getHandle发消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }

    private void initHandler() {
        mAnimatorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        mValueAnimator.removeAllUpdateListeners();
                        mValueAnimator.removeAllListeners();
                        break;
                    case 1:
                    	
                        invalidate();
                        break;
                }

            }
        };
    }

    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(defaultDuration);

        mValueAnimator.addUpdateListener(mUpdateListener);

        mValueAnimator.addListener(mAnimatorListener);
    }


}
