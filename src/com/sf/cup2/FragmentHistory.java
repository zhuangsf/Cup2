package com.sf.cup2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
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
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

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
import com.sf.cup2.view.swipelistview.SwipeMenu;
import com.sf.cup2.view.swipelistview.SwipeMenuCreator;
import com.sf.cup2.view.swipelistview.SwipeMenuItem;
import com.sf.cup2.view.swipelistview.SwipeMenuListView;

public class FragmentHistory extends FragmentPack {
	
	private final static String TAG = "FragmentHistory";
	private View mView;
	private LineChart mChart;
	private String mClickDateString;
	private DBAdapter mdbAdapter;
	private SwipeMenuListView mHistoryList;
	private SimpleAdapter historyListAdapter;
	private LinearLayout share;
	private ImageView buttonDay;
	private ImageView buttonWeek;
	private ImageView buttonMonth;
	private LinearLayout month_view;
	private LinearLayout goBack;
	private TextView month_text;
	private TextView year_text;
	List<HashMap<String, Object>> maps_day = new ArrayList<HashMap<String,Object>>();
	List<HashMap<String, Object>> maps_weekAndmonth = new ArrayList<HashMap<String,Object>>();
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
        
    }	
   	@Override
   	public void onDetach() {
   	    super.onDetach();

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
		
		
		goBack = (LinearLayout)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentData());
            	ft.remove(FragmentHistory.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
		
		month_view = (LinearLayout)view.findViewById(R.id.month_view);
		mChart = (LineChart) view.findViewById(R.id.chart);
		mHistoryList = (SwipeMenuListView) view.findViewById(R.id.history_listview);
		historyListAdapter =  new SimpleAdapter(getActivity(), getDayData(), R.layout.simple_day_item, new String[]{"image", "time", "value"}, new int[]{R.id.img, R.id.time, R.id.value});
		mHistoryList.setAdapter(historyListAdapter);
		setChartStyle(mChart);
		showDayData();

		
	     // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                		getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xFF,
                        0x00, 0x00)));
                // set item width
                deleteItem.setWidth(100);

                // set a icon
            //    deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                deleteItem.setTitle("删除");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mHistoryList.setMenuCreator(creator);

        // step 2. listener item click event
        mHistoryList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
            	if(currentClick == R.id.history_day)
            	{
            		Log.e("jockeyTrack", "position = "+position+" index = "+index); 
            		
            		if(maps_day.size() == 0)
            		{
            			return false;
            		}
            		
            		HashMap<String, Object> map = maps_day.get(position);
            		Long columnID = (Long)map.get("columnID");
            		Log.e("jockeyTrack", "columnID = "+columnID);
            		//mClickDateString
            		mdbAdapter.open();
           			mdbAdapter.deleteDataByID(columnID);
            		mdbAdapter.close();
            		
            		showDayData();
            	}
            	else
            	{
            		if(maps_weekAndmonth.size() == 0)
            		{
            			return false;
            		}
            		HashMap<String, Object> map = maps_weekAndmonth.get(position);
            		String date = (String)map.get("date");
            		Log.e("jockeyTrack", "date = "+date);
            		//mClickDateString
            		mdbAdapter.open();
           			mdbAdapter.deleteDataByDate(date);
            		mdbAdapter.close();
            		
            		if(currentClick == R.id.history_week)
            		{
            			showWeekData();
            		}
            		else
            		{
            			showMonthData();
            		}
            	}
            		


            		
        
           	
            	

                return false;
            }
        });

        // set SwipeListener
        mHistoryList.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        mHistoryList.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });		
		
		
		  buttonDay = (ImageView) view.findViewById(R.id.history_day);  
		  buttonDay.setOnClickListener(new View.OnClickListener() {  
	            public void onClick(View v) {  
	                // Perform action on click  
	                //增加自己的代码......  
	            	showDayData();
	            	currentClick = R.id.history_day;
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
	            	currentClick = R.id.history_week;
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
	            	currentClick = R.id.history_month;
	            	buttonDay.setBackgroundResource(R.drawable.record_icon_day_initial);
	            	buttonWeek.setBackgroundResource(R.drawable.record_icon_week_initial);
	            	buttonMonth.setBackgroundResource(R.drawable.record_icon_month);
	            }  
	        });  
		  
		  month_text = (TextView) view.findViewById(R.id.month_text);  	
		  year_text = (TextView) view.findViewById(R.id.year_text);  
		 
		  
			share = (LinearLayout) view.findViewById(R.id.share);
			
			share.setOnClickListener(new View.OnClickListener() {  
			      
			    @Override  
			    public void onClick(View v) {  
			        // TODO Auto-generated method stub  
			    	showShare();
			    }  
			});  
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
//        wechat.setTitle("我是分享标题");
//        wechat.setText("我是分享文本内容");
//        wechat.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
//
//        wechat.setUrl("http://mob.com");
//        wechat.setShareType(Platform.SHARE_WEBPAGE);
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

	
	private String getScreenCaptureSavePath() {
		String filePath = Utils.getInternelStoragePath(getActivity());
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		Log.e("jockeyTrack", "getScreenCaptureSavePath = "+filePath+"/capture.jpg"); 
		return filePath+"/capture.jpg";
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
		  String[] dataString = mClickDateString.split("-");
		  month_text.setText(dataString[1]);
		  year_text.setText("月"+dataString[0]);
		
		historyListAdapter =  new SimpleAdapter(getActivity(), getWeekData(false), R.layout.simple_week_item, new String[]{"image", "time", "value","percent"}, new int[]{R.id.img, R.id.time, R.id.value,R.id.percent});
		mHistoryList.setAdapter(historyListAdapter);
		
		reflashChartWeekData(mClickDateString,false);
	}
	
	private void showMonthData()
	{
		month_view.setVisibility(View.VISIBLE);
		  String[] dataString = mClickDateString.split("-");
		  month_text.setText(dataString[1]);
		  year_text.setText("月"+dataString[0]);
		  
		historyListAdapter =  new SimpleAdapter(getActivity(), getWeekData(true), R.layout.simple_week_item, new String[]{"image", "time", "value","percent"}, new int[]{R.id.img, R.id.time, R.id.value,R.id.percent});
		mHistoryList.setAdapter(historyListAdapter);
		
		reflashChartWeekData(mClickDateString,true);
	}
	
	
	
	private List<HashMap<String, Object>> getDayData() {
		if(mClickDateString == null)
		{
			return null;
		}
		//List<HashMap<String, Object>> maps = new ArrayList<HashMap<String,Object>>();
		
		maps_day.clear();
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		mdbAdapter.open();
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
	        map.put("columnID", cursor.getLong(DBAdapter.DATA_COLUMN_ID));
	        maps_day.add(map);
		} while (cursor.moveToNext());
		}
		
		if(cursor != null)
		{
			cursor.close();
		}
		mdbAdapter.close();
		return maps_day;
	}
	
	private List<HashMap<String, Object>> getWeekData(boolean bMonth) {
		if(mClickDateString == null)
		{
			return null;
		}
		//List<HashMap<String, Object>> maps = new ArrayList<HashMap<String,Object>>();
		maps_weekAndmonth.clear();
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
				mdbAdapter.open();
				int onedaywater = mdbAdapter.getOneDayWater(sf.format(date));
				mdbAdapter.close();
				if(onedaywater != 0)
				{
					map = new HashMap<String, Object>();
			        map.put("image", R.drawable.record_icon_time);
			        String[] weekdataString = sf.format(date).split("-");
			        map.put("time", weekdataString[2]+"日");
			        map.put("value", "已喝"+onedaywater+"ml");
			        
			        map.put("date",sf.format(date));
					SharedPreferences p = Utils.getSharedPpreference(getActivity());
					String planValue = p.getString(Utils.SHARE_PREFERENCE_CUP_PLAN, "null");
					if(Integer.parseInt(planValue) == 0)
					{
						map.put("percent", "完成 0%");
					}
					else
					{
					int percent = onedaywater * 100 / Integer.parseInt(planValue);
					if(percent > 100)
					{
						percent = 100;
					}
						map.put("percent", "完成 "+percent+"%");
					}
					maps_weekAndmonth.add(map);
				}
			}while(++i < days);
		

		return maps_weekAndmonth;
	}	
	
	
	private void reflashChartData(String currentDateString) {
		if(mChart != null)
		{
			mChart.clear();
		}
		//DBAdapter db = new DBAdapter(getActivity());
		//db.open();
		mdbAdapter.open();
		Cursor cursor = mdbAdapter.getDataByDate(currentDateString);
		if (cursor != null && mChart != null) {
			
			LimitLine ll = new LimitLine(500f, "500ml");
			ll.setLineColor(Color.RED);
			ll.setLineWidth(1f);
			ll.enableDashedLine(10f, 5f, 0f);
			ll.setTextColor(Color.GRAY);
			ll.setTextSize(12f);
			YAxis leftAxis = mChart.getAxisLeft();
			//显示横方向线
			leftAxis.setDrawGridLines(true);
		//	leftAxis.addLimitLine(ll);    貌似没必要设置这条指示线
			
			mChart.setData(getLineData(cursor));
			mChart.notifyDataSetChanged();
			cursor.close();
		}
		mdbAdapter.close();
		//db.close();
	}
	
	private void reflashChartWeekData(String currentDateString,boolean bMonth) {
		if(mChart != null)
		{
			mChart.clear();
		}
		if(mChart != null)
		{
			YAxis leftAxis = mChart.getAxisLeft();
			leftAxis.removeAllLimitLines();
			
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
				mdbAdapter.open();
				int onedaywater = mdbAdapter.getOneDayWater(sf.format(date));
				mdbAdapter.close();
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
		boolean bEmptyData = false;
		if (!cursor.moveToFirst()) {
			bEmptyData = true;
			
			
			for (int i = 0; i < 24; i++) {
				String times = getString(R.string.times);
				times = String.format(times, (i + 6)%24);
				x.add(times);
				
				if(i == 0 || i == 23)
				{
					Entry entry = new Entry(0.0f, i);
					y.add(entry);
				}
			}			
			
			
		}
		else
		{
			int dataCount = cursor.getCount();
			int[] waters = new int[dataCount];
			int i = 0;
			if (cursor.moveToFirst()) {
				do {			
					String drinkTime = cursor.getString(DBAdapter.DATA_COLUMN_TIME);
					x.add(drinkTime);
					waters[i] += Integer.parseInt(cursor.getString(DBAdapter.DATA_COLUMN_WATER));
					Entry entry = new Entry((float)(waters[i]), i);
					y.add(entry);
					i++;
					
					if(dataCount == 1)
					{
						x.add(drinkTime);
						Entry entry1 = new Entry((float)(waters[0]), i);
						y.add(entry1);
					}
				//	waters[(Integer.parseInt(drinkTIme[0]) + 18) % 24] += Integer.parseInt(cursor.getString(DBAdapter.DATA_COLUMN_WATER));
				} while (cursor.moveToNext());
			}
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

	@Override
	protected String getPageName() {
		return FragmentHistory.class.getName();
	}
	
	
	


	
	
}
