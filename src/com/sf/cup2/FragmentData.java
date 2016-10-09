package com.sf.cup2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat.ShareParams;

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
import com.sf.cup2.utils.Utils;
import com.sf.cup2.view.ArcProgressbar;
import com.sf.cup2.view.CalendarView;
import com.sf.cup2.view.CalendarView.OnItemClickListener;
import com.sf.cup2.view.CricleProgressBar;
import com.sf.cup2.view.PercentView;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentData extends FragmentPack {

	static final int COLOR_PUBLIC = Color.parseColor("#ff00cd66");
	static final int COLOR_WARNING = Color.parseColor("#fff4511e");

	protected String[] mMonths = new String[] { "Jan", "Feb", "Mar", "Apr",
			"May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec" };
	private LineChart mChart;
	private ArcProgressbar cpb;

	private CalendarView calendar;
	private ImageButton calendarLeft;
	private TextView calendarCenter;
	private ImageButton calendarRight;
	private SimpleDateFormat format;
	private TextView dateTime;
    private FragmentHistory fHistory;
	private RelativeLayout layout_calendar;
	private String mClickDate;
	private View mask_view;
	
	private TextView water_today;
	private TextView water_target;
	private TextView complete_percent;
	private PercentView percentView;
	private ImageView share;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fHistory = new FragmentHistory();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.tab_data, null);
		mChart = (LineChart) view.findViewById(R.id.chart);

		// LineData mLineData = getLineData(24);
		// setChartStyle(mChart, mLineData);
		setChartStyle(mChart);
		
		water_today = (TextView) view.findViewById(R.id.water_today);
		water_target = (TextView) view.findViewById(R.id.water_target);
		complete_percent = (TextView) view.findViewById(R.id.complete_percent);
		
		
		percentView = (PercentView) view.findViewById(R.id.arcProgressbar_view);


		layout_calendar = (RelativeLayout) view
				.findViewById(R.id.layout_calendar);

		format = new SimpleDateFormat("yyyy-MM-dd");
		// 获取日历控件对象
		calendar = (CalendarView) view.findViewById(R.id.calendar);
		calendar.setSelectMore(false); // 单选

		calendarLeft = (ImageButton) view.findViewById(R.id.calendarLeft);
		calendarCenter = (TextView) view.findViewById(R.id.calendarCenter);
		calendarRight = (ImageButton) view.findViewById(R.id.calendarRight);
		try {
			// 设置日历日期
			Date date = format.parse(format.format(new java.util.Date()));
			calendar.setCalendarData(date);
			initWaterData(format.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		mask_view = (View)view.findViewById(R.id.mask_view);
		mask_view.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mask_view.setVisibility(View.GONE);
				layout_calendar.setVisibility(view.GONE);
			}
		});
		
		// 获取日历中年月 ya[0]为年，ya[1]为月（格式大家可以自行在日历控件中改）
		String[] ya = calendar.getYearAndmonth().split("-");
		calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
		calendarLeft.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 点击上一月 同样返回年月
				String leftYearAndmonth = calendar.clickLeftMonth();
				String[] ya = leftYearAndmonth.split("-");
				calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
			}
		});

		calendarRight.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 点击下一月
				String rightYearAndmonth = calendar.clickRightMonth();
				String[] ya = rightYearAndmonth.split("-");
				calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
			}
		});

		// 设置控件监听，可以监听到点击的每一天（大家也可以在控件中根据需求设定）
		calendar.setOnItemClickListener(new OnItemClickListener() {

			public void OnItemClick(Date selectedStartDate,
					Date selectedEndDate, Date downDate) {
				// Toast.makeText(getActivity(), format.format(downDate),
				// Toast.LENGTH_SHORT).show();
				if (dateTime != null) {
					mClickDate = format.format(downDate);
					String[] clickDate = mClickDate.split("-");
					dateTime.setText(clickDate[0] + "年" + clickDate[1] + "月"
							+ clickDate[2] + "日");
				}
				layout_calendar.setVisibility(View.GONE);
				mask_view.setVisibility(View.GONE);
				
				reflashChartData(format.format(downDate));
				initWaterData(format.format(downDate));
			}
		});

		dateTime = (TextView) view.findViewById(R.id.datetime);
		dateTime.setCompoundDrawablePadding(3); 
		Drawable image = getActivity().getResources().getDrawable(R.drawable.icon_calendar);  
		image.setBounds(0, 0, image.getMinimumWidth(), image.getMinimumHeight());
		dateTime.setCompoundDrawables(image,null, null, null); 
		dateTime.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				layout_calendar.setVisibility(view.VISIBLE);
				mask_view.setVisibility(View.VISIBLE);
			}
		});
		mClickDate = format.format(new java.util.Date());
		String[] clickDate = mClickDate.split("-");
		dateTime.setText(clickDate[0] + "年" + clickDate[1] + "月" + clickDate[2]
				+ "日");

		reflashChartData(mClickDate);

		mChart.setOnClickListener(new View.OnClickListener() {  
		      
		    @Override  
		    public void onClick(View v) {  
		        // TODO Auto-generated method stub  
		    	fHistory.setClickDate(mClickDate);
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, fHistory);
            	ft.remove(FragmentData.this);
            	ft.addToBackStack(null);
				ft.commit();
		    }  
		});  		
		
		share = (ImageView) view.findViewById(R.id.share);
		
		share.setOnClickListener(new View.OnClickListener() {  
		      
		    @Override  
		    public void onClick(View v) {  
		        // TODO Auto-generated method stub  
		    	showShare();
		    }  
		});  
		
		return view;
	}

	
	private String getScreenCaptureSavePath() {
		String filePath = Utils.getInternelStoragePath(getActivity());
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		Log.e("jockeyTrack", "getScreenCaptureSavePath = "+filePath+"/capture.jpg"); 
		return filePath+"/capture.jpg";
	}

	
	
	private void getScreenHot(View v)  
	{          
		String filePath = getScreenCaptureSavePath();
		
		File file = new File(filePath);  
		if (file.exists()) { // 判断文件是否存在
			file.delete(); // delete()方法
		} 
		
	    try  
	    {  
	        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);  
	        Canvas canvas = new Canvas();  
	        canvas.setBitmap(bitmap);  
	        v.draw(canvas);  
	  
	        try  
	        {  
	            FileOutputStream fos = new FileOutputStream(filePath);  
	            bitmap.compress(CompressFormat.PNG, 100, fos);  
	        }  
	        catch (FileNotFoundException e)  
	        {  
	            throw new InvalidParameterException();  
	        }  
	  
	    }  
	    catch (Exception e)  
	    {  
	      e.printStackTrace();  
	    }  
	}  
	
	
	private void showShare() {
		 ShareSDK.initSDK(getActivity());
		 
		 getScreenHot((View) getActivity().getWindow().getDecorView());
//		 
//		 ShareParams wechat = new ShareParams();
//         wechat.setTitle("我是分享标题");
//         wechat.setText("我是分享文本内容");
//         wechat.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
// 
//         wechat.setUrl("http://mob.com");
//         wechat.setShareType(Platform.SHARE_WEBPAGE);
//		 
		 OnekeyShare oks = new OnekeyShare();
		 //关闭sso授权
		 oks.disableSSOWhenAuthorize(); 

		// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
		 //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		 // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		 oks.setTitle("分享");
		 // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		 oks.setTitleUrl("http://sharesdk.cn");
		 // text是分享文本，所有平台都需要这个字段
		 oks.setText("我是分享文本");
		 //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
		 //	 oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");

		 //新浪微博需要签名打包,并且需要在网站上填上你的签名
		 // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		 oks.setImagePath(getScreenCaptureSavePath());//确保SDcard下面存在此张图片
		 // url仅在微信（包括好友和朋友圈）中使用
	//	 oks.setUrl("http://sharesdk.cn");
		 // comment是我对这条分享的评论，仅在人人网和QQ空间使用
		 oks.setComment("我是测试评论文本");
		 // site是分享此内容的网站名称，仅在QQ空间使用
		 oks.setSite(getString(R.string.app_name));
		 // siteUrl是分享此内容的网站地址，仅在QQ空间使用
	//	 oks.setSiteUrl("http://sharesdk.cn");

		// 启动分享GUI
		 oks.show(getActivity());
		 }	
	
	private void reflashChartData(String currentDateString) {
		if(mChart != null)
		{
			mChart.clear();
		}
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		Cursor cursor = db.getDataByDate(currentDateString);
		if (cursor != null && mChart != null) {
			mChart.setData(getLineData(cursor));
			mChart.notifyDataSetChanged();
		}
		cursor.close();
		db.close();
	}

	// 设置显示样式
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
	//	leftAxis.addLimitLine(ll);   貌似没必要设置这条指示线

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

	private LineData getLineData(Cursor cursor) {
		ArrayList<String> x = new ArrayList<String>();
		ArrayList<Entry> y = new ArrayList<Entry>();
		boolean bEmptyData = false;
		if (!cursor.moveToFirst()) {
			bEmptyData = true;
		}
		
		for (int i = 0; i < 24; i++) {
			String times = getString(R.string.times);
			times = String.format(times, (i + 6)%24);
			x.add(times);
			if(bEmptyData && i == 0)
			{
				Entry entry = new Entry(0.0f, i);
				y.add(entry);
			}
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

	public boolean bShowCalendar()
	{
		if(mask_view != null)
		return mask_view.getVisibility() == View.VISIBLE;
		else
		return false;
	}
	
	public void closeCalendar()
	{
		if(layout_calendar != null)
		{
			layout_calendar.setVisibility(View.GONE);
		}
		if(layout_calendar != null)
		{
			mask_view.setVisibility(View.GONE);
		}
	}
	
	public static FragmentData newInstance(Bundle b) {
		FragmentData fd = new FragmentData();
		fd.setArguments(b);
		return fd;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
	}

	private void initWaterData(String currentDateString)
	{
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		int currentWaterData = db.getOneDayWater(currentDateString);
		db.close();
		
		water_today.setText(currentWaterData+"");
		
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		String planValue = p.getString(Utils.SHARE_PREFERENCE_CUP_PLAN, "null");

		if ("null".equals(planValue)) {
			planValue = Utils.getSuggestPlan(getActivity());
			; // 这个值要根据健康管理来生成
			SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
			e.putString(Utils.SHARE_PREFERENCE_CUP_PLAN, planValue);
			e.commit();

		}
		if("null".equals(planValue))
		{
			planValue = "1666";
		}

		water_target.setText(planValue);
		
		int percent = currentWaterData * 100 / Integer.parseInt(planValue);
		complete_percent.setText(percent+"%");
		
		
		if(percentView != null)
		{
			percentView.setRankText(currentWaterData+"",planValue);
		}
	}
	/**
	 * to avoid IllegalStateException: No activity
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected String getPageName() {
		return FragmentData.class.getName();
	}

}