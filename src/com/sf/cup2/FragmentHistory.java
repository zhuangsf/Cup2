package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
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
	private View mView;
	private LineChart mChart;
	private String mClickDateString;
	private DBAdapter mdbAdapter;
	private ListView mHistoryList;
	private SimpleAdapter historyListAdapter;
	
	private Button buttonDay;
	private Button buttonWeek;
	private Button buttonMonth;
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
		mChart = (LineChart) view.findViewById(R.id.chart);
		mHistoryList = (ListView) view.findViewById(R.id.history_listview);
		historyListAdapter =  new SimpleAdapter(getActivity(), getData(), R.layout.simpleitem, new String[]{"image", "time", "value"}, new int[]{R.id.img, R.id.time, R.id.value});
		mHistoryList.setAdapter(historyListAdapter);
		showDayData();

		  buttonDay = (Button) view.findViewById(R.id.history_day);  
		  buttonDay.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	showDayData();
	            }  
	        });  
		  buttonWeek = (Button) view.findViewById(R.id.history_week);  
		  buttonWeek.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	showWeekData(); 
	            }  
	        });  
		  buttonMonth = (Button) view.findViewById(R.id.history_month);  
		  buttonMonth.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	buttonMonth.setText("OnClick. " + " ....");      
	            }  
	        });  
		
	}

	
	public void setClickDate(String clickDateString)
	{
		mClickDateString = clickDateString;
	}
	private void showDayData()
	{
		setChartStyle(mChart);
		reflashChartData(mClickDateString);
	}
	private void showWeekData()
	{
		
	}
	
	
	private List<HashMap<String, Object>> getData() {
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
	        map.put("image", R.drawable.login_phone_pic);
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
	
	private void setChartStyle(LineChart mlinechart) {

		// 将x轴放到底部 默认在顶部
		XAxis mXAxis = mlinechart.getXAxis();
		mXAxis.setPosition(XAxisPosition.TOP);

		mXAxis.setDrawAxisLine(false);
		
		LimitLine ll = new LimitLine(200f, "500ml");
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
		mlinechart.animateX(1000);

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
		// set.setCircleColorHole(Color.YELLOW);
		// 设置折线上显示数据的格式。如果不设置，将默认显示float数据格式。

		set.setValueFormatter(new ValueFormatter() {

			@Override
			public String getFormattedValue(float value, Entry entry,
					int dataSetIndex, ViewPortHandler viewPortHandler) {
				int n = (int) value;
				String str = n + "℃";
				// return str;
				return "";
			}
		});

		ArrayList<ILineDataSet> mLineDataSets = new ArrayList<ILineDataSet>();
		mLineDataSets.add(set);

		LineData mLineData = new LineData(x, mLineDataSets);
		return mLineData;

	}
	
	
	
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.history_day:
//			if(currentClick != R.id.history_day)
//			{
//				currentClick = R.id.history_day;
//				showDayData();
//			}
//			break;
//		case R.id.history_week:
//			if(currentClick != R.id.history_week)
//			{
//			//	showDayData();
//			}
//			break;
//		case R.id.history_month:
//			if(currentClick != R.id.history_month)
//			{
//			//	showDayData();
//			}
//			break;
//
//
//		default:
//			break;
//		}
//	}


	
	
}
