package com.sf.cup2;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sf.cup2.login.LoginActivity;
import com.sf.cup2.utils.Utils;
import com.sf.cup2.view.swipelistview.BaseSwipListAdapter;
import com.sf.cup2.view.swipelistview.SwipeMenu;
import com.sf.cup2.view.swipelistview.SwipeMenuCreator;
import com.sf.cup2.view.swipelistview.SwipeMenuItem;
import com.sf.cup2.view.swipelistview.SwipeMenuListView;
import com.umeng.analytics.MobclickAgent;

public class FragmentTime extends FragmentPack {
	private final static String TAG = FragmentTime.class.getPackage().getName()
			+ "." + FragmentTime.class.getSimpleName();

	private final static int MAX_ALARM_NUMBER = 15;
	public static final long ONE_DAY = 1000L * 60 * 60 * 24;
	Calendar c;

	View maskView;
	FrameLayout alarm_layout;
	ImageView time_logo;
	ImageView water_alarm;
	boolean alarmEnable = true;

	private SwipeMenuListView mAlarmsList;
	private AlarmsListAdapter alarmsListAdapter;
	ImageView add_alarm_button;
	private SharedPreferences mSharedPreferences;
	List<Map<String, Object>> mListData = new ArrayList<Map<String, Object>>();
	AlarmManager mAlarmManager;
	private ToggleButton mTogBtn;
	
	private FragmentTimeEdit fTimeEdit;
	AlertDialog ad;	
	public void setNextAlarm()    //获得最近一个闹钟
	{
		int nextAlarmPosition = -1;
		
		Time t = new Time("GMT+8"); 
		t.setToNow(); // 取得系统时间。
		int year = t.year;
		int month = t.month;
		int date = t.monthDay;
		int hour = (t.hour +8) % 24; // 0-23
		int minute = t.minute;
		int second = t.second;
		int timeNow = hour * 60 + minute;

        int minMinutes = 24*60;
        //获得下个最接近的闹钟
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);
			boolean bSwitchOn = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + i, false);
			if (bVisable && bSwitchOn) {
				String time = mSharedPreferences.getString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + i, "00:00");
				String[] timeArray = time.split(":");
				int timeSet = Integer.parseInt(timeArray[0]) * 60 + Integer.parseInt(timeArray[1]);
				
				if(timeSet <= timeNow)
				{
					timeSet += 24*60;
				}
				
				if((timeSet - timeNow) < minMinutes)
				{
					minMinutes = timeSet - timeNow;
					nextAlarmPosition = i;
				}
			}
		}

		//没有闹钟了
		if(nextAlarmPosition == -1)
		{
			return;
		}
		

		
		
	
		long firstTime = SystemClock
				.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
		long systemTime = System
				.currentTimeMillis();
		String timeString = mSharedPreferences.getString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + nextAlarmPosition, "00:00");
		String[] timeArray = timeString
				.split(":");

		c.setTimeInMillis(System
				.currentTimeMillis());
		// 这里时区需要设置一下，不然会有8个小时的时间差
		c.setTimeZone(TimeZone
				.getTimeZone("GMT+8"));
		c.set(Calendar.MINUTE,
				Integer.parseInt(timeArray[1]));
		c.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(timeArray[0]));
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	
		long selectTime = c.getTimeInMillis();
		// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
		if (systemTime > selectTime) {
			c.add(Calendar.DAY_OF_MONTH, 1);
			selectTime = c.getTimeInMillis();
		}
		// 计算现在时间到设定时间的时间差
		long time = selectTime - systemTime;
		firstTime += time;
		
		
		for(int i = 0;i < MAX_ALARM_NUMBER;i++)
		{
			mAlarmManager.cancel(getPendingIntent(i));
		}
		mAlarmManager.setRepeating(
						AlarmManager.ELAPSED_REALTIME_WAKEUP,
						firstTime,
						ONE_DAY,
						getPendingIntent(nextAlarmPosition));
		
		
		//6602 01 时 分 00/0a校验  sum  bb
		StringBuffer sb_celibrate = new StringBuffer("");
		StringBuffer sb_send = new StringBuffer("");
		
		
		Utils.Log("sentMsgToBt string hour:"+hour+" minute = "+minute);
		sb_celibrate.append("6602");
		sb_celibrate.append(String.format("%02X", nextAlarmPosition+1));
		sb_celibrate.append(String.format("%02X", hour));
		sb_celibrate.append(String.format("%02X", minute));
		sb_celibrate.append("0A");
		sb_celibrate.append(String.format("%02X", 02+nextAlarmPosition+1+hour+minute+10));
		sb_celibrate.append("BB");
		Utils.Log("sentMsgToBt string cmd:"+sb_celibrate.toString());
		((MainActivity)getActivity()).sentMsgToBt(sb_celibrate.toString());
		
		sb_send.append("6602");
		sb_send.append(String.format("%02X", nextAlarmPosition+1));
		sb_send.append(String.format("%02X", Integer.parseInt(timeArray[0])));
		sb_send.append(String.format("%02X", Integer.parseInt(timeArray[1])));
		sb_send.append("00");
		sb_send.append(String.format("%02X", 02+nextAlarmPosition+1+Integer.parseInt(timeArray[0])+Integer.parseInt(timeArray[1])));
		sb_send.append("BB");
		Utils.Log("sentMsgToBt string SEND cmd:"+sb_send.toString());
		((MainActivity)getActivity()).sentMsgToBt(sb_send.toString());
				
	}
	
	private void updateAddButtonStatus() {
		int size = 0;

		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);
			if (bVisable) {
				size++;
			}
		}


		if (size == MAX_ALARM_NUMBER) {
			add_alarm_button.setEnabled(false);
			add_alarm_button.setClickable(false);
//			add_alarm_button.setBackground(getActivity().getResources()
//					.getDrawable(R.drawable.water_mode_add_disable));
		} else {
			add_alarm_button.setEnabled(true);
			add_alarm_button.setClickable(true);
//			add_alarm_button.setBackground(getActivity().getResources()
//					.getDrawable(R.drawable.mode_add_selector));
		}
	}

	
	private int getFirstMatchNumber()
	{
		int firstMatchNumber = 0;
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);
			if (bVisable) { // 如果为true,则继续查找
				firstMatchNumber++;
			} else {
				break;
			}
		}
		if (firstMatchNumber > 20) {
			return -1;
		}
		
		return firstMatchNumber;
	}
	
	
	// 找到第一个不显示的序号
	private void addAlarm() {
		int firstMatchNumber = 0;
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);
			if (bVisable) { // 如果为true,则继续查找
				firstMatchNumber++;
			} else {
				break;
			}
		}
		if (firstMatchNumber > 20) {
			return;
		}

		SharedPreferences.Editor e = Utils
				.getSharedPpreferenceEdit(getActivity());
		e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE
				+ firstMatchNumber, true);
		e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + firstMatchNumber,
				false);
		e.commit();

		updateAddButtonStatus();
		getData();
		alarmsListAdapter.notifyDataSetChanged();
	}

	private int getRealIndex(int alarmID) {
		int matchAlarmIDIndex = -1;
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);

			if (bVisable) {
				matchAlarmIDIndex++;
			}

			if (matchAlarmIDIndex == alarmID) {
				return i;
			}

		}

		return -1;
	}

	private void deleteAlarm(int alarmID) {
		if (alarmID > MAX_ALARM_NUMBER) {
			return;
		}

		int realIndex = getRealIndex(alarmID); // 数组里面第alarm位

		if (realIndex != -1) {
			SharedPreferences.Editor e = Utils
					.getSharedPpreferenceEdit(getActivity());
			e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + realIndex,
					false);
			e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + realIndex,
					false);
			e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + realIndex,
					"00:00");
			e.commit();
			mAlarmManager.cancel(getPendingIntent(realIndex));
			updateAddButtonStatus();
			getData();
			
			setNextAlarm();
			alarmsListAdapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSharedPreferences = Utils.getSharedPpreference(getActivity());
		alarmEnable = mSharedPreferences.getBoolean(
				Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE, Utils.SHARE_PREFERENCE_CUP_ALARM_DEFAULT);

		mAlarmManager = (AlarmManager) getActivity().getSystemService(
				Context.ALARM_SERVICE);
		
		fTimeEdit = new FragmentTimeEdit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.tab_time, null);
		c = Calendar.getInstance();
				
		mAlarmsList = (SwipeMenuListView) v.findViewById(R.id.alarm_listview);
		alarmsListAdapter = new AlarmsListAdapter(getData());
		mAlarmsList.setAdapter(alarmsListAdapter);
		
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
        mAlarmsList.setMenuCreator(creator);

        // step 2. listener item click event
        mAlarmsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
            	
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.red_title_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				
				TextView title = (TextView)layout.findViewById(R.id.title);
				title.setText(R.string.tips);
				TextView summary = (TextView)layout.findViewById(R.id.summary);
				summary.setText(R.string.delete_alarm);
				TextView ok = (TextView) layout.findViewById(R.id.ok);
				ok.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						deleteAlarm(position);
						ad.dismiss();
								
					}
				});
				
				TextView cancel = (TextView) layout.findViewById(R.id.cancel);
				cancel.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						ad.dismiss();
					}
				});
				AlertDialog.Builder alertBuiler = new AlertDialog.Builder(getActivity());
				ad = alertBuiler.create();
				ad.setView(layout);
				ad.show();
            	
            	
            	
//				new AlertDialog.Builder(getActivity())
//				.setMessage(R.string.delete_alarm)
//				.setTitle(R.string.tips)
//				.setPositiveButton(R.string.ok,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(
//									DialogInterface dialog,
//									int which) {
//								deleteAlarm(position);
//							}
//						}).setNegativeButton(R.string.cancel, null)
//				.create().show();
                return false;
            }
        });

        // set SwipeListener
        mAlarmsList.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

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
        mAlarmsList.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });


		
		water_alarm = (ImageView) v.findViewById(R.id.img_alarm);
		add_alarm_button = (ImageView) v.findViewById(R.id.add_alarm_button);
		updateAddButtonStatus();

		add_alarm_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			//	addAlarm();
				
				

				
				//获取当前时间
				Time t = new Time("GMT+8"); 
				t.setToNow(); // 取得系统时间。
				int hour = (t.hour +8) % 24; // 0-23
				int minute = t.minute;
				Utils.Log("timeNow hour = " + hour+" minute = "+minute);
				fTimeEdit.setDefaultTime(hour,minute);
				fTimeEdit.setBooleanEditMode(false);
				//把当前的index传入fragment,用于操作数据
				fTimeEdit.setAlarmIndex(getFirstMatchNumber());
				
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, fTimeEdit);
            	ft.remove(FragmentTime.this);
            	ft.addToBackStack(null);
				ft.commit();
				
			}
		});

		mTogBtn = (ToggleButton) v.findViewById(R.id.mTogBtn); // 获取到控件
		mTogBtn.setChecked(alarmEnable);
		mTogBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
				alarmEnable = isChecked;
				if (alarmEnable) {
					Toast.makeText(getActivity(),
							getResources().getString(R.string.open_alarm),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(),
							getResources().getString(R.string.close_alarm),
							Toast.LENGTH_SHORT).show();
				}

				// 1,update ui
				updateAlarm();

				// 2,update all alarm only click can change the diff status
				// updateAlarmStatus();

				// 3, save the status only click can change the diff status
				SharedPreferences.Editor e = Utils
						.getSharedPpreferenceEdit(getActivity());
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE,
						alarmEnable);
				e.commit();				
				
				
				//add for umeng alarm switch on off
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("alarmEnable",alarmEnable+"");
				MobclickAgent.onEvent(getActivity(),Utils.UMENG_EVENT_ALARM_ON_OFF,map);
				
				
			}
		});// 添加监听事件
		
		
		time_logo = (ImageView) v.findViewById(R.id.time_logo);
		time_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alarmEnable = !alarmEnable;
				if (alarmEnable) {
					Toast.makeText(getActivity(),
							getResources().getString(R.string.open_alarm),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(),
							getResources().getString(R.string.close_alarm),
							Toast.LENGTH_SHORT).show();
				}

				// 1,update ui
				updateAlarm();

				// 2,update all alarm only click can change the diff status
				// updateAlarmStatus();

				// 3, save the status only click can change the diff status
				SharedPreferences.Editor e = Utils
						.getSharedPpreferenceEdit(getActivity());
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE,
						alarmEnable);
				e.commit();

			}
		});
		alarm_layout = (FrameLayout) v.findViewById(R.id.alarm_layout);
		// create a mask to disable mode change
		maskView = new View(getActivity());
		maskView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		maskView.setBackgroundColor(0x88000000);
		maskView.setClickable(true);// set true to disable other view click

		updateAlarm();
		return v;
	}

	private void dumpData() {

		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean checked = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + i, false);
			String time = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + i, "00:00");
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);

			String titleString = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ALARM_TITLE + i, "该喝水啦");
			Utils.Log(" index = " + i + " checked = " + checked
					+ " bVisable = " + bVisable+ " title = "+titleString);
		}
	}

	private List<Map<String, Object>> getData() {
		Map<String, Object> map;
		mListData.clear();
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean checked = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + i, false);
			String time = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + i, "00:00");
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i, false);
			if (bVisable) {
				map = new HashMap<String, Object>();
				map.put("time", (String) time);
				map.put("status", (boolean) checked);
				mListData.add(map);
			}
		}

		return mListData;
	}

	private void updateAlarm() {
		if (!alarmEnable) {
			// 2,update mask
			alarm_layout.addView(maskView);
			// 3,change clock ui
			if(water_alarm != null)
			{
				water_alarm.setBackgroundResource(R.drawable.icon_clock_remind_off);
			}
			
		} else {
			alarm_layout.removeView(maskView);
			if(water_alarm != null)
			{
				water_alarm.setBackgroundResource(R.drawable.icon_clock_remind_on);
			}
		}
	}

	private Intent getIntent(int requestCode) {
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Utils.IS_FROM_ALARM, true);
		intent.putExtra(Utils.FROM_ALARM_INDEX, requestCode);
		return intent;
	}

	private PendingIntent getPendingIntent(int requestCode) {
		PendingIntent senderPI = PendingIntent.getActivity(getActivity(),
				requestCode, getIntent(requestCode),
				PendingIntent.FLAG_UPDATE_CURRENT);

		return senderPI;
	}

	/**
	 * 
	 * @param time
	 *            length=5 "12:34"
	 */
	private void saveTimeAction(String time) {
		try {

			final JSONObject result = new JSONObject();
			final String accountid = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ACCOUNTID, "");
			final String phone = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_PHONE, "");
			if (TextUtils.isEmpty(accountid) || TextUtils.isEmpty(phone)) {
				// it must be a bug missing the accountid
				return;
			}
			result.put("accountid", accountid);
			result.put("phone", phone);
			result.put("clock", time);
			// send to server
			new Thread(new Runnable() {
				@Override
				public void run() {
					// http://121.199.75.79:8280/behaviour
					Utils.httpPost(Utils.URL_PATH + "/behaviour", result, null);
				}
			}).start();
		} catch (Exception e) {
			Utils.Log(TAG, "xxxxxxxxxxxxxxxxxx httpPut error:" + e);
			e.printStackTrace();
		}
	}

	public static FragmentTime newInstance(Bundle b) {
		FragmentTime fd = new FragmentTime();
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
	public void onResume() {
		super.onResume();
		Utils.Log("onResume setNextAlarm");
		setNextAlarm();
	}
	
	
	
	
	private class AlarmsListAdapter extends BaseSwipListAdapter {

		private LayoutInflater mInflator;
		private List<Map<String, Object>> alarmData;
		private boolean supressEvent = false;

		public AlarmsListAdapter(List<Map<String, Object>> data) {

			mInflator = getActivity().getLayoutInflater();
			alarmData = data;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return alarmData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return alarmData.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		private boolean isTimePickerOk = false;

		

		
		private void setAlarmTextClickListener(final TextView alarm_time,
				final int position, final boolean bChecked) {
			alarm_time.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				
					boolean switchOn = mSharedPreferences.getBoolean(
							Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
									+ getRealIndex(position), false);
					if(!switchOn)
					{
						return;
					}

					String timeString = alarm_time.getText().toString();
					String[] timeArray = timeString.split(":");

					fTimeEdit.setDefaultTime(timeString);
					fTimeEdit.setBooleanEditMode(true);
					fTimeEdit.setAlarmIndex(getRealIndex(position));//总的数据表格的index
					
					String alarmTitleString = mSharedPreferences.getString(
							Utils.SHARE_PREFERENCE_CUP_ALARM_TITLE	+ getRealIndex(position), "该喝水啦");
					fTimeEdit.setDefaultTitle(alarmTitleString);
					
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, fTimeEdit);
            	ft.remove(FragmentTime.this);
            	ft.addToBackStack(null);
				ft.commit();
				}
			});
		}

		private Calendar timePicker(TextView alarm_time, int hourOfDay,
				int minute, final int position) {
			String timeString = minute < 10 ? hourOfDay + ":0" + minute
					: hourOfDay + ":" + minute;
			alarm_time.setText(timeString);

			c.setTimeInMillis(System.currentTimeMillis());
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			Utils.Log("xxxxxxxxx edit alarm :" + hourOfDay + ":" + minute + ":"
					+ c.getTimeInMillis() + ":"
					+ Calendar.getInstance().getTimeInMillis());

			if (c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
				// c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) +
				// 1);
				c.add(Calendar.DAY_OF_MONTH, 1);
			}

			SharedPreferences.Editor e = mSharedPreferences.edit();
			e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME
					+ getRealIndex(position), timeString);
			e.commit();
			setNextAlarm();
			// send to server
			// saveTimeAction(timeString);
			return c;
		}
		
        @Override
        public boolean getSwipEnableByPosition(int position) {
            return true;
        }
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				convertView = mInflator.inflate(R.layout.listview_item, null);
			}
			final int alarmPosition = position;

			ToggleButton switchView = (ToggleButton) convertView
					.findViewById(R.id.alarm_switch);
			boolean switchOn = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
							+ getRealIndex(alarmPosition), false);

			final TextView alarm_time = (TextView) convertView
					.findViewById(R.id.alarm_time);
			String time = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ALARM_TIME
							+ getRealIndex(alarmPosition), "00:00");
			alarm_time.setText(time);
			setAlarmTextClickListener(alarm_time, position, switchOn);
			
			
			TextView alarmTitle = (TextView) convertView.findViewById(R.id.alarm_title);
			String alarmTitleString = mSharedPreferences.getString(
					Utils.SHARE_PREFERENCE_CUP_ALARM_TITLE	+ getRealIndex(alarmPosition), "该喝水啦");
			
			alarmTitle.setText(alarmTitleString);
			


			supressEvent = true;
			switchView.setChecked(switchOn);
			supressEvent = false;
			switchView
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							if (supressEvent)
								return;

							// SharedPreferences保存数据
							SharedPreferences.Editor e = mSharedPreferences
									.edit();

							e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
									+ getRealIndex(alarmPosition), isChecked);
							e.commit();
							
							setNextAlarm();
						}
					});


			return convertView;
		}

	}







	@Override
	protected String getPageName() {
		return FragmentTime.class.getName();
	}

}
