package com.sf.cup2;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Fragment;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sf.cup2.R;
import com.sf.cup2.utils.Utils;

public class FragmentHistory extends Fragment {
	
	private final static String TAG = "FragmentHistory";
	private View mView;
	private LineChart mChart;
	private String mClickDateString;
	private DBAdapter mdbAdapter;
	private ListView mHistoryList;
	private SimpleAdapter historyListAdapter;
	
	private ImageView buttonDay;
	private ImageView buttonWeek;
	private ImageView buttonMonth;
	private LinearLayout month_view;
	
	//当前点击的按钮
	private int currentClick = R.id.history_day;
	public static FragmentHistory newInstance(Bundle b) {
		FragmentHistory fd = new FragmentHistory();
		fd.setArguments(b);
		return fd;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        mdbAdapter =  new DBAdapter(getActivity());
        mdbAdapter.open();
        
    }	
   	@Override
   	public void onDetach() {
   	    super.onDetach();

   	    mdbAdapter.close();
   	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.tab_history, container,false);
				
		initLayout(mView);
		

		return mView;
	}

	/**
	 * 初始化控件
	 */
	public void initLayout(View view){
		
		
		month_view = (LinearLayout)view.findViewById(R.id.month_view);
		mChart = (LineChart) view.findViewById(R.id.chart);
		mHistoryList = (ListView) view.findViewById(R.id.history_listview);
		historyListAdapter =  new SimpleAdapter(getActivity(), getDayData(), R.layout.simple_day_item, new String[]{"image", "time", "value"}, new int[]{R.id.img, R.id.time, R.id.value});
		mHistoryList.setAdapter(historyListAdapter);
		setChartStyle(mChart);
		showDayData();

		  buttonDay = (ImageView) view.findViewById(R.id.history_day);  
		  buttonDay.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	showDayData();
	            	buttonDay.setBackgroundResource(R.drawable.record_icon_day);
	            	buttonWeek.setBackgroundResource(R.drawable.record_icon_week_initial);
	            	buttonMonth.setBackgroundResource(R.drawable.record_icon_month_initial);
	            }  
	        });  
		  buttonWeek = (ImageView) view.findViewById(R.id.history_week);  
		  buttonWeek.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	showWeekData(); 
	            	buttonDay.setBackgroundResource(R.drawable.record_icon_day_initial);
	            	buttonWeek.setBackgroundResource(R.drawable.record_icon_week);
	            	buttonMonth.setBackgroundResource(R.drawable.record_icon_month_initial);
	            }  
	        });  
		  buttonMonth = (ImageView) view.findViewById(R.id.history_month);  
		  buttonMonth.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	            	showMonthData();     
	            	buttonDay.setBackgroundResource(R.drawable.record_icon_day_initial);
	            	buttonWeek.setBackgroundResource(R.drawable.record_icon_week_initial);
	            	buttonMonth.setBackgroundResource(R.drawable.record_icon_month);
	            }  
	        });  
		
	}

	
	public void setClickDate(String clickDateString)
	{
		mClickDateString = clickDateString;
	}
	private void showDayData()
	{
		month_view.setVisibility(View.GONE);
		historyListAdapter =  new SimpleAdapter(getActivity(), getDayData(), R.layout.simple_day_item, new String[]{"image", "time", "value"}, new int[]{R.id.img, R.id.time, R.id.value});
		mHistoryList.setAdapter(historyListAdapter);		
		reflashChartData(mClickDateString);
	}
	private void showWeekData()
	{
		month_view.setVisibility(View.VISIBLE);
		historyListAdapter =  new SimpleAdapter(getActivity(), getWeekData(false), R.layout.simple_week_item, new String[]{"image", "time", "value","percent"}, new int[]{R.id.img, R.id.time, R.id.value,R.id.percent});
		mHistoryList.setAdapter(historyListAdapter);
		
		reflashChartWeekData(mClickDateString,false);
	}
	
	private void showMonthData()
	{
		month_view.setVisibility(View.VISIBLE);
		historyListAdapter =  new SimpleAdapter(getActivity(), getWeekData(true), R.layout.simple_week_item, new String[]{"image", "time", "value","percent"}, new int[]{R.id.img, R.id.time, R.id.value,R.id.percent});
		mHistoryList.setAdapter(historyListAdapter);
		
		reflashChartWeekData(mClickDateString,true);
	}
	
	
	
	private List<HashMap<String, Object>> getDayData() {
		if(mClickDateString == null)
		{
			return null;
		}
		List<HashMap<String, Object>> maps = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Cursor cursor = mdbAdapter.getDataByDate(mClickDateString);
		if (cursor.moveToFirst())
		{
		do {
		//	Log.w(TAG, "DATA_COLUMN_ID = "+cursor.getString(DBAdapter.DATA_COLUMN_ID)
		//			+"DATA_COLUMN_DATA = "+cursor.getString(DBAdapter.DATA_COLUMN_DATA)
		//			+"DATA_COLUMN_TIME = "+cursor.getString(DBAdapter.DATA_COLUMN_TIME)
		//			+"DATA_COLUMN_WATER = "+cursor.getString(DBAdapter.DATA_COLUMN_WATER)
		//			);
			map = new HashMap<String, Object>();
	        map.put("image", R.drawable.record_icon_time);
	        map.put("time", cursor.getString(DBAdapter.DATA_COLUMN_TIME));
	        map.put("value", "喝了"+cursor.getString(DBAdapter.DATA_COLUMN_WATER)+"ml");
			maps.add(map);
		} while (cursor.moveToNext());
		}
		
		if(cursor != null)
		{
			cursor.close();
		}
		return maps;
	}
	
	private List<HashMap<String, Object>> getWeekData(boolean bMonth) {
		if(mClickDateString == null)
		{
			return null;
		}
		List<HashMap<String, Object>> maps = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd"); 
		String[] dataString = mClickDateString.split("-");
		Calendar ca = Calendar.getInstance();//得到一个Calendar的实例 
		ca.set(Integer.parseInt(dataString[0]), Integer.parseInt(dataString[1])-1, Integer.parseInt(dataString[2]));//月份是从0开始的，所以11表示12月 ,日期+1,是下面要减1
		int days = 7;
		
		if(bMonth)
		{
			days = 30;
		}
			int i = 0;
			ca.add(Calendar.DATE, -days);
			do
			{
				ca.add(Calendar.DATE, +1); //月份减1 
				Date date = ca.getTime(); //结果 
				Log.w(TAG, "sf.format(date) = "+sf.format(date));
				
				int onedaywater = DBAdapter.getOneDayWater(sf.format(date));

				if(onedaywater != 0)
				{
					map = new HashMap<String, Object>();
			        map.put("image", R.drawable.record_icon_time);
			        String[] weekdataString = sf.format(date).split("-");
			        map.put("time", weekdataString[2]+"日");
			        map.put("value", "已喝"+onedaywater+"ml");
			        map.put("percent", "完成 35%");
					maps.add(map);
				}
			}while(++i < days);
		

		return maps;
	}	
	
	
	private void reflashChartData(String currentDateString) {
		if(mChart != null)
		{
			mChart.clear();
		}
		//DBAdapter db = new DBAdapter(getActivity());
		//db.open();
		Cursor cursor = mdbAdapter.getDataByDate(currentDateString);
		if (cursor != null && mChart != null) {
			mChart.setData(getLineData(cursor));
			mChart.notifyDataSetChanged();
			cursor.close();
		}
		
		//db.close();
	}
	
	private void reflashChartWeekData(String currentDateString,boolean bMonth) {
		if(mChart != null)
		{
			mChart.clear();
		}
		if(mChart != null)
		{
			mChart.setData(getLineWeekData(currentDateString,bMonth));
			mChart.notifyDataSetChanged();
		}
		
	}	
	
	
	private void setChartStyle(LineChart mlinechart) {

		// 将x轴放到底部 默认在顶部
		XAxis mXAxis = mlinechart.getXAxis();
		mXAxis.setPosition(XAxisPosition.BOTTOM);

		//x轴底部线
		mXAxis.setDrawAxisLine(true);
		
		//竖方向线不显示
		mXAxis.setDrawGridLines(false);
		
		LimitLine ll = new LimitLine(500f, "500ml");
		ll.setLineColor(Color.RED);
		ll.setLineWidth(1f);
		ll.enableDashedLine(10f, 5f, 0f);
		ll.setTextColor(Color.GRAY);
		ll.setTextSize(12f);
		// .. and more styling options
		YAxis leftAxis = mlinechart.getAxisLeft();
		YAxis rightAxis = mlinechart.getAxisRight();
		
		//显示横方向线
		leftAxis.setDrawGridLines(true);
		rightAxis.setDrawGridLines(true);
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
		// mlinechart.setBackgroundColor(Color.YELLOW);
		// 设置x y轴的数据
		// mlinechart.setData(mLineData);

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
		
		//是否显示表格下方表格的名称
		mLegend.setEnabled(false);
		
		mlinechart.animateX(1000);

	}


	
	private LineData getLineWeekData(String currentDateString,boolean bMonth) {
		ArrayList<String> x = new ArrayList<String>();
		ArrayList<Entry> y = new ArrayList<Entry>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd"); 
		String[] dataString = currentDateString.split("-");
		int days = 7;
		
		if(bMonth)
		{
			days = 30;
		}
		
		Calendar ca = Calendar.getInstance();//得到一个Calendar的实例 
		ca.set(Integer.parseInt(dataString[0]), Integer.parseInt(dataString[1])-1, Integer.parseInt(dataString[2]));//月份是从0开始的，所以11表示12月 ,日期+1,是下面要减1
			
			int i = 0;
			ca.add(Calendar.DATE, -days);
			do
			{
				ca.add(Calendar.DATE, +1); //月份减1 
				Date date = ca.getTime(); //结果 
				Log.w(TAG, "sf.format(date) = "+sf.format(date));
				x.add(sf.format(date));
				int onedaywater = DBAdapter.getOneDayWater(sf.format(date));
				if(onedaywater == 0)
				{
					onedaywater = 1;
				}
				if(onedaywater != 0)
				{
					Entry entry = new Entry(onedaywater*1.0f, i);
					y.add(entry);
				}
			}while(++i < days);
		


		// y轴的数据集
			LineDataSet set = new LineDataSet(y, null);
			// 用y轴的集合来设置参数
			// 线宽
			set.enableDashedLine(10f, 5f, 0f);
			set.setLineWidth(1.0f);
			// 显示圆形大小
			set.setCircleSize(2.0f);
			// 折线的颜色
			set.setColor(Color.RED);
			// 圆球颜色
			set.setCircleColor(Color.RED);
			set.setDrawCircleHole(false);
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
		//	set.setFillAlpha(50);
		//	set.setFillColor(Color.RED);
			set.setFillDrawable(getActivity().getResources().getDrawable(R.drawable.background_linechart));

		set.setValueFormatter(new ValueFormatter() {

			@Override
			public String getFormattedValue(float value, Entry entry,
					int dataSetIndex, ViewPortHandler viewPortHandler) {
				//int n = (int) value;
				//String str = n + "℃";
				// return str;
				return "";
			}
		});

		ArrayList<ILineDataSet> mLineDataSets = new ArrayList<ILineDataSet>();
		mLineDataSets.add(set);

		LineData mLineData = new LineData(x, mLineDataSets);
		return mLineData;

	}	
	

	private LineData getLineData(Cursor cursor) {
		ArrayList<String> x = new ArrayList<String>();
		ArrayList<Entry> y = new ArrayList<Entry>();

		for (int i = 0; i < 24; i++) {
			String times = getString(R.string.times);
			times = String.format(times, (i + 6)%24);
			x.add(times);
		}

		if (cursor.moveToFirst()) {
			do {
				String[] drinkTIme = cursor.getString(DBAdapter.DATA_COLUMN_TIME).split(":");
				Entry entry = new Entry(
						Float.parseFloat(cursor.getString(DBAdapter.DATA_COLUMN_WATER)), (Integer.parseInt(drinkTIme[0]) - 6) % 24);
				y.add(entry);
			} while (cursor.moveToNext());
		}

		// y轴的数据集
		LineDataSet set = new LineDataSet(y, null);
		// 用y轴的集合来设置参数
		// 线宽
		set.enableDashedLine(10f, 5f, 0f);
		set.setLineWidth(1.0f);
		// 显示圆形大小
		set.setCircleSize(2.0f);
		// 折线的颜色
		set.setColor(Color.RED);
		// 圆球颜色
		set.setCircleColor(Color.RED);
		set.setDrawCircleHole(false);
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
	//	set.setFillAlpha(50);
	//	set.setFillColor(Color.RED);
		set.setFillDrawable(getActivity().getResources().getDrawable(R.drawable.background_linechart));

		set.setValueFormatter(new ValueFormatter() {

			@Override
			public String getFormattedValue(float value, Entry entry,
					int dataSetIndex, ViewPortHandler viewPortHandler) {
				//int n = (int) value;
				//String str = n + "℃";
				// return str;
				return "";
			}
		});

		ArrayList<ILineDataSet> mLineDataSets = new ArrayList<ILineDataSet>();
		mLineDataSets.add(set);

		LineData mLineData = new LineData(x, mLineDataSets);
		return mLineData;

	}
	
	
	


	
	
}
