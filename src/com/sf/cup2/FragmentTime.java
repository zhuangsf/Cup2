package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.sf.cup2.utils.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class FragmentTime extends Fragment {
	private final static String TAG = FragmentTime.class.getPackage().getName()
			+ "." + FragmentTime.class.getSimpleName();

	private final static int MAX_ALARM_NUMBER = 20;
	private final static int MAX_ALARM_VISABLE_NUMBER = 3; // 初始3个可见

	Calendar c;

	View maskView;
	FrameLayout alarm_layout;
	ImageView time_logo;
	boolean alarmEnable = true;

	private ListView mAlarmsList;
	private AlarmsListAdapter alarmsListAdapter;
	ImageView add_alarm_button;
	private SharedPreferences mSharedPreferences;
	List<Map<String, Object>> mListData = new ArrayList<Map<String, Object>>();

	private void updateAddButtonStatus() {
		int size = 0;

		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i,
					i > MAX_ALARM_VISABLE_NUMBER ? false : true);
			if (bVisable) {
				size++;
			}
		}

		Log.e("jockey", "updateAddButtonStatus size =" + size);
		if (size == 20) {
			add_alarm_button.setEnabled(false);
			add_alarm_button.setClickable(false);
			add_alarm_button.setBackground(getActivity().getResources()
					.getDrawable(R.drawable.water_mode_add_disable));
		} else {
			add_alarm_button.setEnabled(true);
			add_alarm_button.setClickable(true);
			add_alarm_button.setBackground(getActivity().getResources()
					.getDrawable(R.drawable.mode_add_selector));
		}
	}

	// 找到第一个不显示的序号
	private void addAlarm() {
		int firstMatchNumber = 0;
		for (int i = 0; i < MAX_ALARM_NUMBER; i++) {
			boolean bVisable = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i,
					i > MAX_ALARM_VISABLE_NUMBER ? false : true);
			if (bVisable) {
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
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i,
					i > MAX_ALARM_VISABLE_NUMBER ? false : true);

			if (bVisable) {
				matchAlarmIDIndex++;
			}

			if (matchAlarmIDIndex == alarmID) {
				Log.e("jockey", "getRealIndex matchAlarmIDIndex = " + matchAlarmIDIndex + " alarmID = " + alarmID);
				dumpData();
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

			updateAddButtonStatus();
			getData();
			alarmsListAdapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSharedPreferences = Utils.getSharedPpreference(getActivity());
		alarmEnable = mSharedPreferences.getBoolean(
				Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_time, null);
		c = Calendar.getInstance();

		mAlarmsList = (ListView) v.findViewById(R.id.alarm_listview);
		alarmsListAdapter = new AlarmsListAdapter(getData());
		mAlarmsList.setAdapter(alarmsListAdapter);

		add_alarm_button = (ImageView) v.findViewById(R.id.add_alarm_button);
		updateAddButtonStatus();

		add_alarm_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAlarm();
			}
		});

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
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i,
					i > MAX_ALARM_VISABLE_NUMBER ? false : true);

			Utils.Log(" index = " + i + " checked = " + checked
					+ " bVisable = " + bVisable);
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
					Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE + i,
					i > MAX_ALARM_VISABLE_NUMBER ? false : true);
			if (bVisable) {
				map = new HashMap<String, Object>();
				map.put("time", (String) time);
				map.put("status", (boolean) checked);
				mListData.add(map);
			}
		}

		return mListData;
	}

	// get the setting from preferrence
	/*
	 * private void initAlarm() { for (int i = 0; i < 9; i++) { boolean checked
	 * = mSharedPreferences.getBoolean( Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON +
	 * i, false); String text = mSharedPreferences.getString(
	 * Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + i, "00:00");
	 * swtichList.get(i).setChecked(checked); alarmList.get(i).setText(text); }
	 * }
	 */
	private void updateAlarm() {
		if (!alarmEnable) {
			// 2,update mask
			alarm_layout.addView(maskView);
			// 3,change clock ui
			time_logo.setImageResource(R.drawable.time_logo_disable);
		} else {
			alarm_layout.removeView(maskView);
			time_logo.setImageResource(R.drawable.time_logo_enable);
		}
	}

	/*
	 * private void updateAlarmStatus() { if (!alarmEnable) { for (int i = 0; i
	 * < 9; i++) { if (swtichList.get(i).isChecked()) { AlarmManager am =
	 * (AlarmManager) getActivity() .getSystemService(Context.ALARM_SERVICE);
	 * am.cancel(getPendingIntent(i)); } } } else { for (int i = 0; i < 9; i++)
	 * { if (swtichList.get(i).isChecked()) { String timeString =
	 * alarmList.get(i).getText().toString(); String[] timeArray =
	 * timeString.split(":"); c.set(Calendar.HOUR_OF_DAY,
	 * Integer.parseInt(timeArray[0])); c.set(Calendar.MINUTE,
	 * Integer.parseInt(timeArray[1])); Utils.Log("xxxxxxxxx hour:" +
	 * Integer.parseInt(timeArray[0]) + " min:" +
	 * Integer.parseInt(timeArray[1])); if (c.getTimeInMillis() <
	 * Calendar.getInstance() .getTimeInMillis()) { c.add(Calendar.DAY_OF_MONTH,
	 * 1); } long tmpMills = c.getTimeInMillis() - System.currentTimeMillis();
	 * AlarmManager am = (AlarmManager) getActivity()
	 * .getSystemService(Context.ALARM_SERVICE);
	 * am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
	 * AlarmManager.INTERVAL_DAY, getPendingIntent(i)); } } } }
	 */

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

	private boolean isTimePickerOk = false;

	/*
	 * private void setAlarmTextClickListener(final int index) {
	 * alarmList.get(index).setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // 选中的时候设置他的初始值 String timeString
	 * = alarmList.get(index).getText().toString(); String[] timeArray =
	 * timeString.split(":"); c.set(Calendar.HOUR_OF_DAY,
	 * Integer.parseInt(timeArray[0])); c.set(Calendar.MINUTE,
	 * Integer.parseInt(timeArray[1])); Utils.Log("xxxxxxxxx hour:" +
	 * Integer.parseInt(timeArray[0]) + " min:" +
	 * Integer.parseInt(timeArray[1])); if (c.getTimeInMillis() <
	 * Calendar.getInstance() .getTimeInMillis()) { c.add(Calendar.DAY_OF_MONTH,
	 * 1); } int hour = c.get(Calendar.HOUR_OF_DAY); int minute =
	 * c.get(Calendar.MINUTE);
	 * 
	 * TimePickerDialog tpd = new TimePickerDialog(getActivity(), new
	 * OnTimeSetListener() {
	 * 
	 * @Override public void onTimeSet(TimePicker view, int hourOfDay, int
	 * minute) { if (!isTimePickerOk) { return; } c = timePicker(index,
	 * hourOfDay, minute); // TODO there is a bug that cant cancel or // return
	 * fix it if (!swtichList.get(index).isChecked()) {
	 * swtichList.get(index).setChecked(true); } else { long tmpMills =
	 * c.getTimeInMillis() - System.currentTimeMillis(); //
	 * Toast.makeText(getActivity(), // "闹钟"+(index+1)+" 设置:" + //
	 * Utils.formatTime(tmpMills) + // "后",Toast.LENGTH_LONG).show(); //
	 * todo后续修改 AlarmManager am = (AlarmManager) getActivity()
	 * .getSystemService( Context.ALARM_SERVICE);
	 * am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
	 * AlarmManager.INTERVAL_DAY, getPendingIntent(index)); } } }, hour, minute,
	 * true); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	 * Utils.Log("android version newer than L"); isTimePickerOk = true; } else
	 * { Utils.Log("android version older than KK");
	 * tpd.setButton(DialogInterface.BUTTON_POSITIVE,
	 * getResources().getString(R.string.ok), new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * isTimePickerOk = true; } });
	 * tpd.setButton(DialogInterface.BUTTON_NEGATIVE,
	 * getResources().getString(R.string.cancel), new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * isTimePickerOk = false; } }); } tpd.show(); } }); }
	 * 
	 * private Calendar timePicker(int i, int hourOfDay, int minute) { String
	 * timeString = minute < 10 ? hourOfDay + ":0" + minute : hourOfDay + ":" +
	 * minute; alarmList.get(i).setText(timeString);
	 * 
	 * c.setTimeInMillis(System.currentTimeMillis());
	 * c.set(Calendar.HOUR_OF_DAY, hourOfDay); c.set(Calendar.MINUTE, minute);
	 * c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
	 * Utils.Log("xxxxxxxxx edit alarm :" + hourOfDay + ":" + minute + ":" +
	 * c.getTimeInMillis() + ":" + Calendar.getInstance().getTimeInMillis()); //
	 * 避免设置时间比当前时间小时 马上响应的情况发生 if (c.getTimeInMillis() <
	 * Calendar.getInstance().getTimeInMillis()) { //
	 * c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
	 * c.add(Calendar.DAY_OF_MONTH, 1); Utils.Log("xxxxxxxxx edit alarm 2:" +
	 * (c.get(Calendar.MONTH) + 1) + ":" + c.get(Calendar.DAY_OF_MONTH)); }
	 * 
	 * // SharedPreferences保存数据
	 * 
	 * SharedPreferences.Editor e = mSharedPreferences.edit();
	 * e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + i, timeString);
	 * e.commit();
	 * 
	 * // send to server saveTimeAction(timeString); return c; }
	 */
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

	private class AlarmsListAdapter extends BaseAdapter {

		private LayoutInflater mInflator;
		private List<Map<String, Object>> alarmData;

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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				convertView = mInflator.inflate(R.layout.listview_item, null);
			}
			final int alarmPosition = position;
			ImageView delete_alarm = (ImageView) convertView
					.findViewById(R.id.delete_alarm);
			TextView alarm_index = (TextView) convertView
					.findViewById(R.id.alarm_index);
			Switch switchView = (Switch) convertView
					.findViewById(R.id.alarm_switch);
			boolean switchOn = mSharedPreferences.getBoolean(
					Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
							+ getRealIndex(alarmPosition), false);
			Log.e("jockey", "getView switchOn = " + switchOn
					+ " alarmPosition = " + alarmPosition);
			switchView.setChecked(switchOn);
			switchView
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {

								/*
								 * String timeString =
								 * alarmList.get(index).getText() .toString();
								 * String[] timeArray = timeString.split(":");
								 * c.set(Calendar.HOUR_OF_DAY,
								 * Integer.parseInt(timeArray[0]));
								 * c.set(Calendar.MINUTE,
								 * Integer.parseInt(timeArray[1]));
								 * Utils.Log("xxxxxxxxx hour:" +
								 * Integer.parseInt(timeArray[0]) + " min:" +
								 * Integer.parseInt(timeArray[1])); if
								 * (c.getTimeInMillis() < Calendar.getInstance()
								 * .getTimeInMillis()) {
								 * c.add(Calendar.DAY_OF_MONTH, 1); } long
								 * tmpMills = c.getTimeInMillis() -
								 * System.currentTimeMillis(); //
								 * Toast.makeText(getActivity(), //
								 * "闹钟"+(index+1)+" 设置:" + //
								 * Utils.formatTime(tmpMills) + "后", //
								 * Toast.LENGTH_LONG).show(); // todo 后续再修改字符串
								 * 
								 * AlarmManager am = (AlarmManager)
								 * getActivity()
								 * .getSystemService(Context.ALARM_SERVICE);
								 * am.setRepeating(AlarmManager.RTC_WAKEUP,
								 * c.getTimeInMillis(),
								 * AlarmManager.INTERVAL_DAY,
								 * getPendingIntent(index));
								 * 
								 * } else { AlarmManager am = (AlarmManager)
								 * getActivity()
								 * .getSystemService(Context.ALARM_SERVICE);
								 * am.cancel(getPendingIntent(index)); }
								 */
								// SharedPreferences保存数据

								SharedPreferences.Editor e = mSharedPreferences.edit();
								Log.e("jockey", "onCheckedChanged isChecked = " + isChecked + " getRealIndex(alarmPosition) = " + getRealIndex(alarmPosition));
								e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON + getRealIndex(alarmPosition),isChecked);
								e.commit();
								dumpData();
							}
						}
					});

			alarm_index.setText(Integer.toString(position));
			delete_alarm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// ################ for delete must be carefull

					new AlertDialog.Builder(getActivity())
							.setMessage(R.string.delete_alarm)
							.setTitle(R.string.tips)
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Log.e("jockey",
													"onClick alarmPosition ="
															+ alarmPosition);
											deleteAlarm(alarmPosition);

										}
									}).setNegativeButton(R.string.cancel, null)
							.create().show();

				}
			});

			return convertView;
		}

	}

}
