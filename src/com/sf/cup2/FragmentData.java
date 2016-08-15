package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sf.cup2.view.ArcProgressbar;
import com.sf.cup2.view.CricleProgressBar;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class FragmentData extends Fragment {
	
    static final int COLOR_PUBLIC = Color.parseColor("#ff00cd66");
    static final int COLOR_WARNING = Color.parseColor("#fff4511e");
    
    protected String[] mMonths = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };
	private LineChart mChart;
	private ArcProgressbar cpb;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.tab_data, null);
    	mChart = (LineChart)view.findViewById(R.id.chart);
        	
        LineData mLineData = LineData(24);

        setChartStyle(mChart, mLineData);        
   
        cpb = (ArcProgressbar)view.findViewById(R.id.arcProgressbar_view);
        ObjectAnimator oObjectAnimator = ObjectAnimator.ofInt(cpb, "progress", 0,220);
        oObjectAnimator.setDuration(2000L);
      //  oObjectAnimator.setInterpolator(new DecelerateInterpolator());
        oObjectAnimator.start();
        
 
        return view;
    }
 
    // 设置显示样式
      private void setChartStyle(LineChart mlinechart, LineData mLineData) {
    	  
          // 将x轴放到底部 默认在顶部
          XAxis mXAxis = mlinechart.getXAxis();
          mXAxis.setPosition(XAxisPosition.TOP);
          
          mXAxis.setDrawAxisLine(false);
          
          LimitLine ll = new LimitLine(35f, "500ml");  
          ll.setLineColor(Color.RED);  
          ll.setLineWidth(1f);  
          ll.enableDashedLine(10f, 5f, 0f);
          ll.setTextColor(Color.GRAY);  
          ll.setTextSize(12f);  
          // .. and more styling options  
          YAxis leftAxis = mlinechart.getAxisLeft();
          YAxis rightAxis = mlinechart.getAxisRight();
          leftAxis.setDrawGridLines(false);
          rightAxis.setDrawGridLines(false);
          rightAxis.setEnabled(false);
          leftAxis.addLimitLine(ll);  	  
    	  
 
          // 是否在折线上添加边框
          mlinechart.setDrawBorders(false);
          // 数据描述
          mlinechart.setDescription("   ");
          // 如果没有数据的时候，会显示这个，类似listview的emtpyview
          mlinechart.setNoDataTextDescription("喝水量异常,请检查与水杯蓝牙连接");
          // 是否绘制背景颜色。
          // 如果mLineChart.setDrawGridBackground(false)，
          // 那么mLineChart.setGridBackgroundColor()将失效;
          mlinechart.setDrawGridBackground(false);
          // 折线图的背景
          mlinechart.setGridBackgroundColor(Color.CYAN);
          // 设置触摸
      mlinechart.setTouchEnabled(true);
       // 设置拖拽
          mlinechart.setDragEnabled(false);
          // 设置缩放
         mlinechart.setScaleEnabled(false);
          mlinechart.setPinchZoom(false);
          // x y 轴的背景
       //   mlinechart.setBackgroundColor(Color.YELLOW);
          // 设置x y轴的数据
          mlinechart.setData(mLineData);

          // 设置比例图标，就是那一组y的value的
          Legend mLegend = mlinechart.getLegend();
          mLegend.setPosition(LegendPosition.BELOW_CHART_CENTER);
          // 样式
          mLegend.setForm(LegendForm.LINE);
          // 字体
          mLegend.setFormSize(20.0f);
          // 下方字体颜色
          mLegend.setTextColor(Color.BLUE);
          // 设置x轴的动画
          mlinechart.animateX(1000);
  
      }
  
      private LineData LineData(int count) {
          ArrayList<String> x = new ArrayList<String>();
          // x轴的数据

          for (int i = 0; i < count; i++) {
              String times = getString(R.string.times);
              times = String.format(times, i);
        	  
              x.add(times);
          }
          // y轴的数据
          ArrayList<Entry> y = new ArrayList<Entry>();
          for (int i = 0; i < count; i++) {
              float f = (float) (Math.random() * 50);
              Entry entry = new Entry(f, i);
              y.add(entry);
          }
          // y轴的数据集
          LineDataSet set = new LineDataSet(y, "   ");
          // 用y轴的集合来设置参数
          // 线宽
          set.setLineWidth(3.0f);
          // 显示圆形大小
          set.setCircleSize(5.0f);
          // 折线的颜色
          set.setColor(Color.BLACK);
          // 圆球颜色
         set.setCircleColor(Color.RED);
         // 设置mLineDataSet.setDrawHighlightIndicators(false)后，
         // Highlight的十字交叉的纵横线将不会显示，
         // 同时，mLineDataSet.setHighLightColor()失效。
 
         set.setDrawHighlightIndicators(true);
         // 点击后，十字交叉线的颜色
         set.setHighLightColor(Color.BLUE);
         // 设置显示数据点字体大小
         set.setValueTextSize(10.0f);
         // mLineDataSet.setDrawCircleHole(true);
 
         // 改变折线样式，用曲线。
         set.setDrawCubic(true);
         // 默认是直线
         // 曲线的平滑度，值越大越平滑。
         set.setCubicIntensity(0.2f);
 
         // 填充曲线下方的区域，红色，半透明。
         set.setDrawFilled(true);
        // 数值越小 透明度越大
        set.setFillAlpha(50);
        set.setFillColor(Color.RED);
 
         // 填充折线上数据点、圆球里面包裹的中心空白处的颜色。
      //   set.setCircleColorHole(Color.YELLOW);
         // 设置折线上显示数据的格式。如果不设置，将默认显示float数据格式。
   
         set.setValueFormatter(new ValueFormatter() {
 
             @Override
             public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                     ViewPortHandler viewPortHandler) {
                 int n = (int) value;
                 String str = n + "℃";
               //  return str;
                 return "";
             }
         });
  
         ArrayList<ILineDataSet> mLineDataSets = new ArrayList<ILineDataSet>();
         mLineDataSets.add(set);
 
         LineData mLineData = new LineData(x, mLineDataSets);
         return mLineData;
 
}

  
   
    
    
    public static FragmentData newInstance(Bundle b){
    	FragmentData fd=new FragmentData();
			fd.setArguments(b);
			return fd;
		}
    
    
    
    
    
    /**
	 * to avoid IllegalStateException: No activity
	 */
	@Override
	public void onDetach() {
	    super.onDetach();
	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }

	}
	
}