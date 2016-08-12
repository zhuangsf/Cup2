package com.sf.cup2.view;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.sf.cup2.R;



public class CricleProgressBar extends View
{
  private int cricleColor;
  private int cricleProgressColor;
  private float cricleWidth;
  private int currentProgress;
  private boolean isPrivate = true;
  private Context mContext;
  private ArrayList<Entry> mEntries;
  private int maxProgress;
  private Paint paint;
  private int percent;
  private float progressNum;
  private int style;
  private int textColor;
  private boolean textIsDisplayable;
  private float textNum;
  private float textSize;

  public CricleProgressBar(Context paramContext)
  {
    this(paramContext, null);
    this.mContext = paramContext;
  }

  public CricleProgressBar(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
    this.mContext = paramContext;
  }
  
  public CricleProgressBar(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    Log.i("shubind", "CricleProgressBar start");
    this.mContext = paramContext;
    this.paint = new Paint();
    TypedArray typeArray  = paramContext.obtainStyledAttributes(paramAttributeSet, R.styleable.CricleProgressBar);
    this.cricleColor = typeArray.getColor(0, Color.parseColor("#eaeaea"));
    this.cricleProgressColor = typeArray.getColor(1, Color.parseColor("#eaffea"));
    this.textColor = typeArray.getColor(10, Color.parseColor("#eaeaff"));
    this.textSize = typeArray.getDimension(11, 25.0F);
    this.cricleWidth = typeArray.getDimension(9, 20.0F);
    this.maxProgress = typeArray.getInteger(12, 100);
    this.currentProgress = typeArray.getInteger(13, this.currentProgress);
    this.style = typeArray.getInt(22, 0);
    this.textIsDisplayable = typeArray.getBoolean(21, true);
    Log.i("shubind", "CricleProgressBar end 1");
    typeArray.recycle();
    Log.i("shubind", "CricleProgressBar end 2");
    

    
    
  }

  private int dip2px(Context paramContext, float paramFloat)
  {
    return (int)(paramFloat * paramContext.getResources().getDisplayMetrics().density + 0.5F);
  }

  @Override
protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    Log.i("shubind", "AAAA");
    int centre = getWidth() / 2;
    int radius  = (int)(centre - this.cricleWidth / 2.0F);
    Log.i("shubin", "paint==" + this.paint);
    this.paint.setColor(this.cricleColor);
    this.paint.setStyle(Paint.Style.STROKE);
    this.paint.setStrokeWidth(this.cricleWidth);
    this.paint.setAntiAlias(true);
    paramCanvas.drawCircle(centre, centre, radius, this.paint);
    
    
    this.paint.setStrokeWidth(this.cricleWidth);
    this.paint.setColor(this.cricleProgressColor);
    RectF localRectF = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
    
    	 
     if (this.mEntries != null){
     	Log.i("shubind", "mEntries.size()=="+mEntries.size());
	int i = this.mEntries.size() - 1;
	float startAngle01 =0f;
	float totalUse=0f;
	for(int j=0;j<mEntries.size();j++){
		totalUse+=mEntries.get(j).percentage;
	}
	float scalerate=percent/(totalUse*100);
	Log.i("shubind", "mEntries total use=="+totalUse +" scalerate="+scalerate);
	
	for(i=mEntries.size()-1;i>=0;i--)
	{
		progressNum=startAngle01;
		 Log.i("shubind", "percent=="+((Entry)this.mEntries.get(i)).percentage+",progressNum:"+progressNum);
	 Log.i("shubind", "percent==" + ((Entry)this.mEntries.get(i)).percentage * 360.0F +" maxProgress "+ this.maxProgress);
	 if ((((Entry)this.mEntries.get(i)).percentage >= 0.0F) && (((Entry)this.mEntries.get(i)).percentage < 1.0F))
	 	if((((Entry)this.mEntries.get(i)).percentage < 0.01F)){ //min 
	           paramCanvas.drawArc(localRectF, progressNum - 90, 3.6F , false, ((Entry)this.mEntries.get(i)).paint);
       		   startAngle01+=3.6F;//0.01 * 360;
	 	}else{
       		 paramCanvas.drawArc(localRectF, progressNum - 90, mEntries.get(i).percentage * 360 *scalerate , false, ((Entry)this.mEntries.get(i)).paint);
       		startAngle01+=mEntries.get(i).percentage * 360 *scalerate;
       		}
       		// paramCanvas.drawArc(localRectF, progressNum - 90, 30, false, ((Entry)this.mEntries.get(i)).paint);
       		// startAngle01+=30;
       		 
    	  }
    	  
	    /** 
     * 
     */  
    paint.setStrokeWidth(0);   
    paint.setColor(textColor);  
    paint.setTextSize(textSize);  
    paint.setTypeface(Typeface.DEFAULT_BOLD); 
    float textWidth = paint.measureText(percent + "%");  
      
      
      Log.i("shubind", "  private detail percent="+percent +" textIsDisplayable "+textIsDisplayable+" style "+style);
    if(textIsDisplayable && percent != 0 && style == 0){  
    	paramCanvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize/2, paint);  
    	Log.i("shubind", "  private detail percent="+percent);
        
    }  
    
    
    }else{
    
    switch (style) {  
    case 0:{  
        paint.setStyle(Paint.Style.STROKE);  
        paramCanvas.drawArc(localRectF, progressNum - 90, 360*currentProgress  / maxProgress, false, paint);  
            Log.i("shubind", "  111 progressNum="+progressNum +",currentProgress="+currentProgress+",maxProgress="+maxProgress);
        break;  
    }  
    case 1:{  
        paint.setStyle(Paint.Style.FILL_AND_STROKE);  
        if(currentProgress !=0)  
        	paramCanvas.drawArc(localRectF, progressNum - 90, 360*currentProgress  / maxProgress, true, paint);  
        	Log.i("shubind", "  222 progressNum="+progressNum +",currentProgress="+currentProgress+",maxProgress="+maxProgress);
        break;  
    }  
    }  
    
    
    
    /** 
     * 
     */  
    paint.setStrokeWidth(0);   
    paint.setColor(textColor);  
    paint.setTextSize(textSize);  
    paint.setTypeface(Typeface.DEFAULT_BOLD); 
    int percent = (int)(((float)currentProgress / (float)maxProgress) * 100); 
    float textWidth = paint.measureText(percent + "%");   
      
    if(textIsDisplayable && percent != 0 && style == 0){  
    	paramCanvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize/2, paint); 
    	Log.i("shubind", "  333 percent="+percent);
        
    }  
   }
  }

  public void setCricleProgressColor(int paramInt)
  {
    this.cricleProgressColor = paramInt;
  }

  public void setCricleWidth(float paramFloat)
  {
    this.cricleWidth = paramFloat;
  }

  public void setEntries(ArrayList<Entry> paramArrayList)
  {
    this.mEntries = paramArrayList;
  }

  public void setPercent(int paramInt)
  {
  	  Log.i("shubind", "  setPercent="+paramInt);
    this.percent = paramInt;
  }

  public Entry setProgress(float paramFloat, int paramInt)
  {
    this.isPrivate = true;
    Paint localPaint = new Paint();
    localPaint.setStrokeWidth(this.cricleWidth);
    localPaint.setAntiAlias(true);
    localPaint.setColor(paramInt);
    localPaint.setStyle(Paint.Style.STROKE);
    return new Entry(paramInt, paramFloat, localPaint);
  }

  public void setProgressWithAnimation(float paramFloat)
  {
    Log.i("shubin", "progress start----" + paramFloat);
    ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(this, "progresses", new float[] { paramFloat });
    localObjectAnimator.setDuration(1000L);
    localObjectAnimator.setInterpolator(new DecelerateInterpolator());
    localObjectAnimator.start();
     Log.i("shubin", "progress end----" + paramFloat);
  }

  public void setProgresses(float paramFloat)
  {
  	Log.i("shubind", "  444 paramFloat="+paramFloat);
    if (paramFloat < 0.0F)
      throw new IllegalArgumentException("progress not less than 0");
    if (paramFloat > this.maxProgress)
      this.currentProgress = this.maxProgress;
    if (paramFloat <= this.maxProgress)
    {
      this.currentProgress = ((int)paramFloat);
      this.isPrivate = false;
      postInvalidate();
    }
  }

  public void setTextColor(int paramInt)
  {
    this.textColor = paramInt;
  }

  public void setTextSize(float paramFloat)
  {
    this.textSize = paramFloat;
  }

  public static class Entry
  {
    public final int color;
    public final Paint paint;
    public final float percentage;

    protected Entry(int paramInt, float paramFloat, Paint paramPaint)
    {
      this.color = paramInt;
      this.percentage = paramFloat;
      this.paint = paramPaint;
    }
  }
}


/* Location:           D:\apktool\dex2jar-2.0\classes-dex2jar.jar
 * Qualified Name:     com.android.settings.deviceinfo.CricleProgressBar
 * JD-Core Version:    0.6.2
 */