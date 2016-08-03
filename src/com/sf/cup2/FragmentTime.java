package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import com.sf.cup2.utils.Utils;

import android.app.AlarmManager;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class FragmentTime extends Fragment {
	private final static String TAG = FragmentTime.class.getPackage().getName() + "."
			+ FragmentTime.class.getSimpleName();
	
	TextView alarm1;
	TextView alarm2;
	TextView alarm3;
	TextView alarm4;
	TextView alarm5;
	TextView alarm6;
	TextView alarm7;
	TextView alarm8;
	TextView alarm9;
	List<TextView> alarmList=new ArrayList<TextView>();
	Calendar c;
	Switch switch1;
	Switch switch2;
	Switch switch3;
	Switch switch4;
	Switch switch5;
	Switch switch6;
	Switch switch7;
	Switch switch8;
	Switch switch9;
	List<Switch> swtichList=new ArrayList<Switch>();
	View maskView;
	FrameLayout alarm_layout;
	ImageView time_logo;
	boolean alarmEnable=true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences p;
		p = Utils.getSharedPpreference(getActivity());
		alarmEnable=p.getBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_time, null);
		c = Calendar.getInstance();

		alarm1 = (TextView) v.findViewById(R.id.alarm1);
		switch1 = (Switch) v.findViewById(R.id.switch1);
		alarm2 = (TextView) v.findViewById(R.id.alarm2);
		switch2 = (Switch) v.findViewById(R.id.switch2);
		alarm3 = (TextView) v.findViewById(R.id.alarm3);
		switch3 = (Switch) v.findViewById(R.id.switch3);
		alarm4 = (TextView) v.findViewById(R.id.alarm4);
		switch4 = (Switch) v.findViewById(R.id.switch4);
		alarm5 = (TextView) v.findViewById(R.id.alarm5);
		switch5 = (Switch) v.findViewById(R.id.switch5);
		alarm6 = (TextView) v.findViewById(R.id.alarm6);
		switch6 = (Switch) v.findViewById(R.id.switch6);
		alarm7 = (TextView) v.findViewById(R.id.alarm7);
		switch7 = (Switch) v.findViewById(R.id.switch7);
		alarm8 = (TextView) v.findViewById(R.id.alarm8);
		switch8 = (Switch) v.findViewById(R.id.switch8);
		alarm9 = (TextView) v.findViewById(R.id.alarm9);
		switch9 = (Switch) v.findViewById(R.id.switch9);
		swtichList.add(switch1);
		swtichList.add(switch2);
		swtichList.add(switch3);
		swtichList.add(switch4);
		swtichList.add(switch5);
		swtichList.add(switch6);
		swtichList.add(switch7);
		swtichList.add(switch8);
		swtichList.add(switch9);
		alarmList.add(alarm1);
		alarmList.add(alarm2);
		alarmList.add(alarm3);
		alarmList.add(alarm4);
		alarmList.add(alarm5);
		alarmList.add(alarm6);
		alarmList.add(alarm7);
		alarmList.add(alarm8);
		alarmList.add(alarm9);
		
		//get the setting from preferrence
		initAlarm();
		
		for(int i = 0;i<9;i++){
			setSwitchListener(i);
			setAlarmTextClickListener(i);
		}
		
		
		
		
		time_logo=(ImageView)v.findViewById(R.id.time_logo);
		time_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alarmEnable=!alarmEnable;
				if(alarmEnable){
					Toast.makeText(getActivity(), "启用闹钟功能", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "禁用闹钟功能", Toast.LENGTH_SHORT).show();
				}
				
				//1,update ui
				updateAlarm();
				
				//2,update all alarm                only click can change the diff status
				updateAlarmStatus();
				
				//3, save the status                  only click can change the diff status
				SharedPreferences.Editor e=Utils.getSharedPpreferenceEdit(getActivity());
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_ENABLE, alarmEnable);
				e.commit();
				
			}
		});
		alarm_layout=(FrameLayout)v.findViewById(R.id.alarm_layout);
		//create a mask to disable mode change
		maskView=new View(getActivity());
		maskView.setLayoutParams(new ViewGroup.LayoutParams(
		            ViewGroup.LayoutParams.FILL_PARENT,
		            ViewGroup.LayoutParams.FILL_PARENT));
		maskView.setBackgroundColor(0x88000000);
		maskView.setClickable(true);// set true  to disable other view click
		
		updateAlarm();
		return v;
	}
	
	//get the setting from preferrence
	private void initAlarm(){
		 //SharedPreferences 初始化界面
			SharedPreferences p;
			p = getActivity().getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
			
			for(int i=0;i<9;i++){
				boolean checked=p.getBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON+i, false);
				String text=p.getString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME+i, "00:00");
				swtichList.get(i).setChecked(checked);
				alarmList.get(i).setText(text);
			}
	}

	
	private void updateAlarm(){
		if(!alarmEnable)
		{
			//2,update mask
			alarm_layout.addView(maskView);
			//3,change clock ui
			time_logo.setImageResource(R.drawable.time_logo_disable);
		}else
		{
			alarm_layout.removeView(maskView);
			time_logo.setImageResource(R.drawable.time_logo_enable);
		}
	}
	
	private void updateAlarmStatus(){
		if(!alarmEnable)
		{
			for(int i = 0;i<9;i++){
				if(swtichList.get(i).isChecked()){
					AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
					am.cancel(getPendingIntent(i));
				}
			}
		}else{
			for(int i = 0;i<9;i++){
				if(swtichList.get(i).isChecked()){
					String timeString=alarmList.get(i).getText().toString();
					String[] timeArray=timeString.split(":");
					c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
					c.set(Calendar.MINUTE,  Integer.parseInt(timeArray[1]));
					Utils.Log("xxxxxxxxx hour:"+Integer.parseInt(timeArray[0])+" min:"+Integer.parseInt(timeArray[1]));
					if (c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
						c.add(Calendar.DAY_OF_MONTH, 1);
					}
					long tmpMills = c.getTimeInMillis() - System.currentTimeMillis();
					AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
					am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY, getPendingIntent(i));
				}
			}
		}
	}
	
	private Intent getIntent(int requestCode){
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Utils.IS_FROM_ALARM, true);
		intent.putExtra(Utils.FROM_ALARM_INDEX, requestCode);
		return intent;
	}
	
	private PendingIntent getPendingIntent(int requestCode){
		PendingIntent senderPI = PendingIntent.getActivity(getActivity(), requestCode, getIntent(requestCode),PendingIntent.FLAG_UPDATE_CURRENT);
		
		return senderPI;
	}
	
	private void setSwitchListener(final int index){
		swtichList.get(index).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					String timeString=alarmList.get(index).getText().toString();
					String[] timeArray=timeString.split(":");
					c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
					c.set(Calendar.MINUTE,  Integer.parseInt(timeArray[1]));
					Utils.Log("xxxxxxxxx hour:"+Integer.parseInt(timeArray[0])+" min:"+Integer.parseInt(timeArray[1]));
					if (c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
						c.add(Calendar.DAY_OF_MONTH, 1);
					}
					long tmpMills = c.getTimeInMillis() - System.currentTimeMillis();
					Toast.makeText(getActivity(), "闹钟"+(index+1)+" 设置:" + Utils.formatTime(tmpMills) + "后", Toast.LENGTH_LONG).show();
					AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
					am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY,getPendingIntent(index));
				} else {
					AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
					am.cancel(getPendingIntent(index));
				}
				
				 //SharedPreferences保存数据
				SharedPreferences p;
				SharedPreferences.Editor e;
				p = getActivity().getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
				e = p.edit();
				
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON+index, isChecked);
				e.commit();
			}
		});
	}
	
	private boolean isTimePickerOk=false;
	private void setAlarmTextClickListener(final int index){
		alarmList.get(index).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//选中的时候设置他的初始值
				String timeString=alarmList.get(index).getText().toString();
				String[] timeArray=timeString.split(":");
				c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
				c.set(Calendar.MINUTE,  Integer.parseInt(timeArray[1]));
				Utils.Log("xxxxxxxxx hour:"+Integer.parseInt(timeArray[0])+" min:"+Integer.parseInt(timeArray[1]));
				if (c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
					c.add(Calendar.DAY_OF_MONTH, 1);
				}
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minute = c.get(Calendar.MINUTE);

				TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						if(!isTimePickerOk){
							return ;
						}
						c = timePicker(index,hourOfDay, minute);
						// TODO  there is a bug that  cant cancel or return    fix it 
						if (!swtichList.get(index).isChecked()) {
							swtichList.get(index).setChecked(true);
						} else {
							long tmpMills = c.getTimeInMillis() - System.currentTimeMillis();
							Toast.makeText(getActivity(), "闹钟"+(index+1)+" 设置:" + Utils.formatTime(tmpMills) + "后",Toast.LENGTH_LONG).show();
							AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
							am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY, getPendingIntent(index));
						}
					}
				}, hour, minute, true);
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
					Utils.Log("android version newer than L");
					isTimePickerOk=true;
				}else{
					Utils.Log("android version older than KK");
					tpd.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isTimePickerOk=true;
						}
					});
					tpd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isTimePickerOk=false;
						}
					});
				}
				tpd.show();
			}
		});
	}
	
	
	private Calendar timePicker(int i,int hourOfDay, int minute) {
		String timeString = minute < 10 ? hourOfDay + ":0" + minute : hourOfDay + ":" + minute;
		alarmList.get(i).setText(timeString);
		
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Utils.Log("xxxxxxxxx edit alarm :" + hourOfDay + ":" + minute + ":" + c.getTimeInMillis() + ":"
				+ Calendar.getInstance().getTimeInMillis());
		// 避免设置时间比当前时间小时 马上响应的情况发生
		if (c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			// c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
			c.add(Calendar.DAY_OF_MONTH, 1);
			Utils.Log("xxxxxxxxx edit alarm 2:" + (c.get(Calendar.MONTH) + 1) + ":" + c.get(Calendar.DAY_OF_MONTH));
		}
		
		 //SharedPreferences保存数据
		SharedPreferences p;
		SharedPreferences.Editor e;
		p = getActivity().getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
		e = p.edit();
		e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME+i, timeString);
		e.commit();
		
		//send to server
		saveTimeAction(timeString);
		return c;
	}

	/**
	 * 
	 * @param time  length=5   "12:34"
	 */
	private void saveTimeAction(String time){
		try {
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		final JSONObject result = new JSONObject();
		final String accountid = p.getString(Utils.SHARE_PREFERENCE_CUP_ACCOUNTID, "");
		final String phone = p.getString(Utils.SHARE_PREFERENCE_CUP_PHONE, "");
		if(TextUtils.isEmpty(accountid)||TextUtils.isEmpty(phone)){
			// it must be a bug   missing the accountid
			return ;
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
			Utils.Log(TAG,"xxxxxxxxxxxxxxxxxx httpPut error:" + e);
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
