package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.sf.cup2.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentWater extends Fragment {
	private final static String TAG = FragmentWater.class.getPackage().getName() + "."
			+ FragmentWater.class.getSimpleName();
	ListView temperatureListView; 
	ImageView add_temperature_button;
	Button cancel_temperature_button;
	List<Map<String, Object>> temperatureList = new ArrayList<Map<String, Object>>(); //list view 就是一直玩弄这个
	TemperatureListViewAdapter hlva;
	
	FrameLayout temperature_mode;
	View maskView;
	LinearLayout temperature_setting;
	boolean temperature_mode_enable=false;
	int temperature_setting_value=50; //00; what a stupid customer
	int temperature_current_value=20;
	int temperature_mode_index=-1;
	
	private static final String VIEW_INFO_TEXT="info_text";
	private static final String VIEW_TEMPERATURE_TEXT="temperature_text";
	private static final String VIEW_RADIO_BTN="radio_btn";
	
	
	TextView water_status_text1;
	TextView water_status_text2;
	TextView water_status_text3;
	ImageView water_status_pic1;
	ImageView water_status_pic2;
	ImageView water_status_pic3;
	
	TextView current_cup_temperature;
	
	EditText infoString;
	EditText tempString; 
	AlertDialog alertDialog;
	
	private static final int MSG_SHOW_IM=1;
	private static final int MSG_STOP_SEND=2;
	
    // Stops sending after 3 seconds.
    private static final long SEND_PERIOD = 3000;
    private static ProgressDialog pd;// 等待进度圈
    private int temp_index=-1; //this is a important  int.   if it !=-1  means send a msg to bt   the msg is this value.
    AlertDialog sendFailAlertDialog;
    
	Handler mHandler = new Handler()
	  {
	    @Override
		public void handleMessage(Message paramAnonymousMessage)
	    {
//	    Utils.Log("handle:"+paramAnonymousMessage);
	     switch (paramAnonymousMessage.what)
	     {
				case MSG_SHOW_IM:
					//alertdialog with edittext cant not open im.  
					try {
						Thread.sleep(200);
						infoString.dispatchTouchEvent( MotionEvent.obtain( SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, infoString.getRight(), infoString.getRight() + 5, 0));
						infoString.dispatchTouchEvent( MotionEvent.obtain( SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, infoString.getRight(), infoString.getRight() + 5, 0));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					break;
				case MSG_STOP_SEND:
					if(pd!=null){
						pd.dismiss();
						Utils.Log(" MSG_STOP_SEND dismiss:");
					}
					if(temp_index!=-1){
						Utils.Log("xxxxxxxxxxxxxxxxxx water mHandler stop send some error may happen");
						temp_index=-1;
						if (sendFailAlertDialog == null) {
							sendFailAlertDialog=new AlertDialog.Builder(getActivity())
									.setTitle(R.string.tips)
									.setMessage(R.string.bt_disconnect)
//									.setCancelable(false)
									.setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											 boolean result= ((MainActivity)getActivity()).reConnect();
											 if(!result){
												 Toast.makeText(getActivity(), getResources().getString(R.string.not_connect_device), Toast.LENGTH_SHORT).show();
											 }
										}
									})
									.setNegativeButton(R.string.cancel,null).create();
						}
						try {
								sendFailAlertDialog.show();
						} catch (Exception e) {
							sendFailAlertDialog=null;
						}
					}
					
					
					
					break;
			}
	    }
	  };
	  
	  
	  /**
	   * update the currentTemperature and temperature status
	   * 
	   */
	  private void updateCurrentTemperature(){
		  //1,update current temperature
		  current_cup_temperature.setText(""+temperature_current_value); 
		  //2,update status
		  water_status_text1.setTextColor(getActivity().getResources().getColor(R.drawable.darkgray));
		  water_status_text1.setTextSize(12);
		  water_status_text2.setTextColor(getActivity().getResources().getColor(R.drawable.darkgray));
		  water_status_text2.setTextSize(12);
		  water_status_text3.setTextColor(getActivity().getResources().getColor(R.drawable.darkgray));
		  water_status_text3.setTextSize(12);
		  water_status_pic1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_disable));
		  water_status_pic2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_disable));
		  water_status_pic3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_disable));
		  if(temperature_mode_index==-1||!temperature_mode_enable){
			  water_status_text1.setTextColor(getActivity().getResources().getColor(R.drawable.cup_pink));
			  water_status_text1.setTextSize(15);
			  water_status_pic1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_focus));
		  } else if(temperature_mode_index!=-1){
			  if(temperature_current_value!=temperature_setting_value){
				  water_status_text2.setTextColor(getActivity().getResources().getColor(R.drawable.cup_pink));
				  water_status_text2.setTextSize(15);
				  water_status_pic2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_focus));
			  }else{
				  water_status_text3.setTextColor(getActivity().getResources().getColor(R.drawable.cup_pink));
				  water_status_text3.setTextSize(15);
				  water_status_pic3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.water_status_point_focus));
				  temperatureComplete();
			  }
		  } 
//		  Utils.Log("updateCurrentTemperature temperature_mode_index:"+temperature_mode_index+" temperature_mode_enable:"+temperature_mode_enable);
	  }
	  
	  private MediaPlayer mp; 

	private void temperatureComplete() {
		try {
			if (alertDialog == null) {
				alertDialog = new AlertDialog.Builder(getActivity()).setMessage("tips1").setTitle(R.string.tips)
						.setPositiveButton(R.string.ok, null).create();
			}

			alertDialog.show();

			// 创建MediaPlayer对象
			mp = new MediaPlayer();
			// 将音乐保存在res/raw/xingshu.mp3,R.java中自动生成{public static final int
			// xingshu=0x7f040000;}
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			// mp = MediaPlayer.create(this, notification);
			mp.setDataSource(getActivity(), notification);
			// 在MediaPlayer取得播放资源与stop()之后要准备PlayBack的状态前一定要使用MediaPlayer.prepeare()
			mp.prepare();
			// 开始播放音乐
			mp.start();
			// 音乐播放完毕的事件处理
			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// 循环播放
					try {
						// mp.start();
						if(mp!=null){
							if(mp.isPlaying())
					            mp.stop();
					        mp.reset();
					        mp.release();
					        mp=null;
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
			});
			// 播放音乐时发生错误的事件处理
			mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// 释放资源
					try {
						if (mp != null) {
							if (mp.isPlaying())
								mp.stop();
							mp.reset();
							mp.release();
							mp = null;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences p;
		p = getActivity().getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
		temperature_mode_enable=p.getBoolean(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_ENABLE, false);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_water, null);

		temperatureListView = (ListView) v.findViewById(R.id.temperature_list);
		
		SharedPreferences p=Utils.getSharedPpreference(getActivity());
		int isFirst=p.getInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, 0);
		Utils.Log("isFirst must be bigger than 2 or there must be a bug :"+isFirst);
		//isFirst must be bigger than 2 or there must be a bug 
		Map<String, Object> m=new HashMap<String, Object>();
		if(isFirst==2){
			m.put(VIEW_INFO_TEXT, "早上第一杯水");
			m.put(VIEW_TEMPERATURE_TEXT, "45");
			m.put(VIEW_RADIO_BTN, false);
			temperatureList.add(m);
			m=new HashMap<String, Object>();
			m.put(VIEW_INFO_TEXT, "工作喝水");
			m.put(VIEW_TEMPERATURE_TEXT, "55");
			m.put(VIEW_RADIO_BTN, false);
			temperatureList.add(m);
			m=new HashMap<String, Object>();
			m.put(VIEW_INFO_TEXT, "运动后喝水");
			m.put(VIEW_TEMPERATURE_TEXT, "50");
			m.put(VIEW_RADIO_BTN, false);
			temperatureList.add(m);	
			SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[0], "早上第一杯水");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[0], "45");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[1], "工作喝水");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[1], "55");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[2], "运动后喝水");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[2], "50");
					e.commit();
		}else{
			for(int i=0;i<5;i++){
				String text=p.getString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[i], "");
				String value=p.getString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[i], "");
				temperature_mode_index=p.getInt(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE, -1);
				if(!TextUtils.isEmpty(text)&&!TextUtils.isEmpty(value)){
					m=new HashMap<String, Object>();
					m.put(VIEW_INFO_TEXT, text);
					m.put(VIEW_TEMPERATURE_TEXT, value);
					if(temperature_mode_index==i){
						m.put(VIEW_RADIO_BTN, true);
						temperature_setting_value=Integer.parseInt(value);
					}else{
						m.put(VIEW_RADIO_BTN, false);
					}
					temperatureList.add(m);
				}
			}
		}
		
		
		hlva = new TemperatureListViewAdapter(this.getActivity(), temperatureList,
				R.layout.tab_water_select_item, new String[] { VIEW_INFO_TEXT, VIEW_TEMPERATURE_TEXT, VIEW_RADIO_BTN },
				new int[] { R.id.info_text, R.id.temperature_text, R.id.radio_btn });
		temperatureListView.setAdapter(hlva);
		setHeight(hlva, temperatureListView);

		add_temperature_button = (ImageView) v.findViewById(R.id.add_temperature_button);
		int size = temperatureList.size();
		if (size >= 5) {
			add_temperature_button.setEnabled(false);
			add_temperature_button.setClickable(false);
			add_temperature_button.setBackground(getActivity().getResources().getDrawable(R.drawable.water_mode_add_disable));
		} else {
			add_temperature_button.setEnabled(true);
			add_temperature_button.setClickable(true);
			add_temperature_button.setBackground(getActivity().getResources().getDrawable(R.drawable.mode_add_selector));
		}
		add_temperature_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.tab_water_select_dialog,
						(ViewGroup) v.findViewById(R.id.dialog));
				infoString = (EditText) layout.findViewById(R.id.info_input);
				tempString = (EditText) layout.findViewById(R.id.temp_input);
				
				final AlertDialog ad = new AlertDialog.Builder(getActivity())
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						Map<String, Object> m=new HashMap<String, Object>();
						m.put(VIEW_INFO_TEXT, infoString.getText().toString());
						m.put(VIEW_TEMPERATURE_TEXT, tempString.getText().toString());
						m.put(VIEW_RADIO_BTN, false);
						temperatureList.add(m);
						doUpdate();
					}

				}).setNegativeButton("取消", null).create();
				
				ad.setTitle("温度模式设定");
				ad.setView(layout);
				ad.show();
				ad.getCurrentFocus();
				
				try {  
				    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");  
				    mAlert.setAccessible(true);  
				    Object alertController = mAlert.get(ad);  
				  
				    Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");  
				    mTitleView.setAccessible(true);  
				  
				    TextView title = (TextView) mTitleView.get(alertController);  
//				    title.setTextColor(0xffff0022);   
//				    title.setGravity(Gravity.CENTER);
				} catch (NoSuchFieldException e) {  
				    e.printStackTrace();  
				} catch (IllegalArgumentException e) {  
				    e.printStackTrace();  
				} catch (IllegalAccessException e) {  
				    e.printStackTrace();  
				}  
				
				Button adPosiButton=ad.getButton(DialogInterface.BUTTON_POSITIVE);
				adPosiButton.setEnabled(false);
				
				Message msg=new Message();
				msg.what=MSG_SHOW_IM;
				msg.arg1=1;
				mHandler.sendMessage(msg);
				/*adPosiButton.setBackground(getActivity().getResources().getDrawable(R.drawable.long_button_selector));
				 LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
				 lp1.setMargins(50, 0, 0, 0);
				adPosiButton.setLayoutParams(lp1);
				
				LinearLayout l=	(LinearLayout) adPosiButton.getParent();
				l.setGravity(Gravity.CENTER);
				l.setBackground(null);
				l.setDividerDrawable(null);
				
				Button adNegaButton=ad.getButton(AlertDialog.BUTTON_NEGATIVE);
				adNegaButton.setEnabled(false);
				adNegaButton.setBackground(getActivity().getResources().getDrawable(R.drawable.long_button_selector));
				 LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
				 lp2.setMargins(0, 0, 50, 0);
				adNegaButton.setLayoutParams(lp2);*/
				
				tempString.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
						if(s!=null&&!"".equals(s.toString())){
							try {
							int a=Integer.parseInt(s.toString());
							String info_text=infoString.getText().toString();
							if(a<=70&&a>=20&&!TextUtils.isEmpty(info_text)){
								ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
							}
							} catch (Exception e) {
								// i dont care this error
							}							
						}
					}
				});
				infoString.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
						if(s!=null&&!"".equals(s.toString())){
							try {
							String temp_text=tempString.getText().toString();
							int a=Integer.parseInt(temp_text.toString());
							if(a<=70&&a>=20&&!TextUtils.isEmpty(s)){
								ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
							}
							} catch (Exception e) {
								// i dont care this error
							}							
						}
					}
				});
				
			}
		});

		
		//visiable gone
		cancel_temperature_button=(Button)v.findViewById(R.id.cancel_temperature_button);
		cancel_temperature_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(Map<String, Object> m :temperatureList){
					m.put(VIEW_RADIO_BTN,false);
				}
				doUpdate();
			}
		});
		
		
		
		//click to open/close the temperature mode
		temperature_setting=(LinearLayout)v.findViewById(R.id.temperature_setting);
		temperature_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.Log("xxxxxxxxxxxxxxxxxx temperature_setting:" + v);
				//1,show confirm dialog
				
				
				//2 send message to bluetooth              not need
				//show waiting dialog
				
				//3 update ui 
				temperature_mode_enable=!temperature_mode_enable;
				if(!temperature_mode_enable){
					//if disable the temperature mode   unselect mode
					temperature_setting_value=0;
					if(temperature_mode_index!=-1){
						temperatureList.get(temperature_mode_index).put(VIEW_RADIO_BTN, false);
					}
					temperature_mode_index = -1;
					hlva.notifyDataSetChanged();
				}
				updateUiShow();
				
				//4 save value  temperature_mode_enable
				SharedPreferences.Editor e=Utils.getSharedPpreferenceEdit(getActivity());
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_ENABLE, temperature_mode_enable);
				e.commit();
			}
		});
		
		
		temperature_mode=(FrameLayout)v.findViewById(R.id.temperature_mode);
		//create a mask to disable mode change
		maskView=new View(getActivity());
		maskView.setLayoutParams(new ViewGroup.LayoutParams(
		            ViewGroup.LayoutParams.FILL_PARENT,
		            ViewGroup.LayoutParams.FILL_PARENT));
		maskView.setBackgroundColor(0x88000000);
		maskView.setClickable(true);// set true  to disable other view click
		
		
		
		
		//status  view
		water_status_text1=(TextView)v.findViewById(R.id.water_status_text1);
		water_status_text2=(TextView)v.findViewById(R.id.water_status_text2);
		water_status_text3=(TextView)v.findViewById(R.id.water_status_text3);
		water_status_pic1=(ImageView)v.findViewById(R.id.water_status_pic1);
		water_status_pic2=(ImageView)v.findViewById(R.id.water_status_pic2);
		water_status_pic3=(ImageView)v.findViewById(R.id.water_status_pic3);		
		
		
		current_cup_temperature=(TextView)v.findViewById(R.id.current_cup_temperature); 
		//create a thread to get cup temperature period
		askTemperatureFromBT();
		
		updateUiShow();// create first time
		

		
		mPullRefreshScrollView = (PullToRefreshScrollView) v.findViewById(R.id.pull_refresh_scrollview);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new GetDataTask().execute();
			}
		});

		mScrollView = mPullRefreshScrollView.getRefreshableView();
		
		return v;
	}
	PullToRefreshScrollView mPullRefreshScrollView;
	ScrollView mScrollView;
    
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(new Random().nextInt(1000)+2000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// Do some stuff here

			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshScrollView.onRefreshComplete();

			super.onPostExecute(result);
		}
	}
	
	
	
	private Timer timer = new Timer(true);
	public void askTemperatureFromBT(){
		//任务
		TimerTask task = new TimerTask() {
		  @Override
		public void run() {
			  //ask temp  not need to send msg  when bt return temp msg it will user setCurrentTemperatureFromBT
			  ((MainActivity)getActivity()).sentAskTemperature();
			  }
		};
		 
		//启动定时器
		timer.schedule(task, 60000, 60000);
	}
	public void setCurrentTemperatureFromBT(int t){
		temperature_current_value=t;
		updateCurrentTemperature();
	}
	
	/**
	 * update 
	 * 1,disable mask
	 * 2,setting temperture value color
	 * 3,update the currentTemperature and temperature status
	 */
	private void updateUiShow(){
		
		//1,turn on/off the temperature mode
		setMaskToModeSetting(temperature_mode_enable);
		
		//2,change display
		TextView t=(TextView)temperature_setting.findViewById(R.id.temperature_du);
		Utils.Log("xxxxxxxxxxxxxxxxxx temperature_mode_enable:" + temperature_mode_enable +"t color:"+Integer.toHexString(t.getCurrentTextColor()));
		if(temperature_mode_enable){
			t.setTextColor(getActivity().getResources().getColor(R.drawable.cup_pink));
		}else{
			t.setTextColor(getActivity().getResources().getColor(R.drawable.darkgray));
		}
		setTemperaturePic(temperature_setting,temperature_mode_enable);
		
		//3,update the status
		updateCurrentTemperature();
	}

	int enableTemperaturePicId[]={
			R.drawable.num_focus_0,
			R.drawable.num_focus_1,
			R.drawable.num_focus_2,
			R.drawable.num_focus_3,
			R.drawable.num_focus_4,
			R.drawable.num_focus_5,
			R.drawable.num_focus_6,
			R.drawable.num_focus_7,
			R.drawable.num_focus_8,
			R.drawable.num_focus_9
	};
	int disableTemperaturePicId[]={
			R.drawable.num_disable_0,
			R.drawable.num_disable_1,
			R.drawable.num_disable_2,
			R.drawable.num_disable_3,
			R.drawable.num_disable_4,
			R.drawable.num_disable_5,
			R.drawable.num_disable_6,
			R.drawable.num_disable_7,
			R.drawable.num_disable_8,
			R.drawable.num_disable_9
	};
	private void setTemperaturePic(View v, boolean isEnable) {
		ImageView v1 = (ImageView) v.findViewById(R.id.temperature_value1);
		ImageView v2 = (ImageView) v.findViewById(R.id.temperature_value2);
		Resources resources = getActivity().getResources();
		int temperatureValue1 = getSettingValue() / 10;
		int temperatureValue2 = getSettingValue() % 10;
		Drawable bgDrawable1;
		Drawable bgDrawable2;
		if (isEnable) {
			bgDrawable1 = resources.getDrawable(enableTemperaturePicId[temperatureValue1]);
			bgDrawable2 = resources.getDrawable(enableTemperaturePicId[temperatureValue2]);
		} else {
			bgDrawable1 = resources.getDrawable(disableTemperaturePicId[temperatureValue1]);
			bgDrawable2 = resources.getDrawable(disableTemperaturePicId[temperatureValue2]);
		}
		v1.setImageDrawable(bgDrawable1);
		v2.setImageDrawable(bgDrawable2);
		
		TextView water_mode=(TextView)v.findViewById(R.id.water_mode);
		water_mode.setText(getSettingMode());

	}
	/**
	 * get temperature which select mode define 
	 * @return
	 */
	private int getSettingValue(){
		
		return temperature_setting_value;
	}
	/**
	 * get temperature which select mode define 
	 * @return
	 */
	private String getSettingMode(){
		String settingMode="未设定";
		if(temperature_mode_index!=-1)
		{
			settingMode=(String)temperatureList.get(temperature_mode_index).get(VIEW_INFO_TEXT);
		}
		
		return settingMode;
	}
	
	
	private void setMaskToModeSetting(boolean isEnable){
//		temperature_mode
		
		if(!isEnable)
		{
			temperature_mode.addView(maskView);
		}else
		{
			temperature_mode.removeView(maskView);
		}
	}
	
	
	
	/**
	 * update the temperature select mode 
	 */
	private void doUpdate() {
		if (hlva != null) {
			int size = temperatureList.size();
			Utils.Log("update listview count:" + size);
			hlva.notifyDataSetChanged();
			setHeight(hlva, temperatureListView);

			SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
			for (int i = 0; i < 5; i++) {
				if (i < size) {
					String text = (String) temperatureList.get(i).get(VIEW_INFO_TEXT);
					String value = (String) temperatureList.get(i).get(VIEW_TEMPERATURE_TEXT);
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[i], text);
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[i], value);
				} else {
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO[i], "");
					e.putString(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE[i], "");
				}

			}
			e.commit();

			if (size >= 5) {
				add_temperature_button.setEnabled(false);
				add_temperature_button.setClickable(false);
				add_temperature_button.setBackground(getActivity().getResources().getDrawable(R.drawable.water_mode_add_disable));
			} else {
				add_temperature_button.setEnabled(true);
				add_temperature_button.setClickable(true);
				add_temperature_button.setBackground(getActivity().getResources().getDrawable(R.drawable.mode_add_selector));
			}
		}
	}

	public static FragmentWater newInstance(Bundle b) {
		FragmentWater fd = new FragmentWater();
		fd.setArguments(b);
		return fd;
	}

	public void setHeight(BaseAdapter comAdapter, ListView l) {
		int listViewHeight = 0;
		int adaptCount = comAdapter.getCount();
		int dpi=Utils.getDisplayDensity(getActivity());
		Utils.Log("xxxxxxxxxxxxx dddddddddddddpi="+dpi);
		for (int i = 0; i < adaptCount; i++) {
			View temp = comAdapter.getView(i, null, l);
			temp.measure(0, 0);
			
			/*
			 * ldpi       120dpi
				mdpi     160dpi
				hdpi       240dpi
				xhdpi     320dpi
			 */
			
			if(320==dpi){
				listViewHeight += temp.getMeasuredHeight()+30;// the divide height
			}else if(240==dpi){
				listViewHeight += temp.getMeasuredHeight()+15;// the divide height
			}else if(160==dpi){
				listViewHeight += temp.getMeasuredHeight()+13;// the divide height
			}else if(320<dpi){
				listViewHeight += temp.getMeasuredHeight()+45;// the divide height
			}else{
				listViewHeight += temp.getMeasuredHeight()+15;// the divide height
			}
		}
		LayoutParams layoutParams = l.getLayoutParams();
		layoutParams.width = LayoutParams.FILL_PARENT;
		layoutParams.height = listViewHeight + 4;
		l.setLayoutParams(layoutParams);
	}

	protected class TemperatureListViewAdapter extends SimpleAdapter {
		float downX=0f;
		float upX=0f;
		// 用于记录每个RadioButton的状态，并保证只可选一个
		 HashMap<String, Boolean> states = new HashMap<String, Boolean>();
		  
		public TemperatureListViewAdapter(Context context, List<Map<String, Object>> data, int resource, String[] from,
				int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
//			view.setOnClickListener(new MyListener(position));
			MyListener ml=new MyListener(position);
			
			final int p=position;
			final RadioButton radio=(RadioButton) view.findViewById(R.id.radio_btn);  
			radio.setOnClickListener(ml);
			
			RelativeLayout temperature_mode_info=(RelativeLayout) view.findViewById(R.id.temperature_mode_info);  
			temperature_mode_info.setOnClickListener(ml);
			temperature_mode_info.setOnLongClickListener(ml);
			if(position==temperature_mode_index){
				temperature_mode_info.setBackground(getResources().getDrawable(R.drawable.list_item_shape_select));
			}
			
			ImageView delete_model=(ImageView)view.findViewById(R.id.delete_model);
			delete_model.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//################ for delete must be carefull
					
					new AlertDialog.Builder(getActivity())
					.setMessage("确定删除此模式？")
			    	.setTitle("温馨提示")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							temperatureList.remove(p);
							if(temperature_mode_index==p)
							{
								temperature_mode_index=-1;
								//0 ,show a waiting dialog
								
								//1, send request to cup                not need
							}else
							{
								for (int i=0;i<temperatureList.size();i++) {
									if((boolean)temperatureList.get(i).get(VIEW_RADIO_BTN))
									{
										temperature_mode_index=i;
									}
								}
							}
					       // TemperatureListViewAdapter.this.notifyDataSetChanged();
					        doUpdate();
						}
					})
					.setNegativeButton("取消", null)
					.create()
					.show();
					
				
				}
			});

			
			
			/*
			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction())//根据动作来执行代码     
                    {    
                    case MotionEvent.ACTION_MOVE://滑动     
                    	int moveX = (int) event.getX(); 
                    	int deltaX=(int)(downX-moveX);
                    	if(deltaX<40&&deltaX>0){
                    		Utils.Log("xxxxxxxxxxxxxxxxxx deltaX:" + deltaX);
                    		v.scrollBy(deltaX, 0);
                    	}
                        break;    
                    case MotionEvent.ACTION_DOWN://按下     
                        downX = event.getX();  
                        break;    
                    case MotionEvent.ACTION_UP://松开     
                        upX = event.getX();  
//                        Toast.makeText(context, "up..." + Math.abs(UpX-DownX), Toast.LENGTH_SHORT).show();  
                        Utils.Log("xxxxxxxxxxxxxxxxxx downX-upX:" + (downX-upX));
                        if(downX-upX>20){  
                            v.scrollBy(40, 0);  
                        }  else{
                        	v.scrollBy(0, 0);
                        }
                        break;    
                    default:    
                    }    
                    return true;   
				}
			});
			
			*/
			return view;
		}
		
		@Override
		public void notifyDataSetChanged() {
			//0 ,show a waiting dialog

			//1, send request to cup
			
			//2  update the mode select
			super.notifyDataSetChanged();
			 //3,update temperature
			if(temperature_mode_index==-1){
				temperature_setting_value=00;
			}else{
				temperature_setting_value=Integer.parseInt((String)temperatureList.get(temperature_mode_index).get(VIEW_TEMPERATURE_TEXT));
			}
			//3.5  update the select mode bg color
			if(temperatureListView!=null){
				for(int i=0;i<temperatureListView.getChildCount();i++){
					if(i==temperature_mode_index){
						temperatureListView.getChildAt(i).findViewById(R.id.temperature_mode_info).setBackground(getResources().getDrawable(R.drawable.list_item_shape_select));
					}else{
						temperatureListView.getChildAt(i).findViewById(R.id.temperature_mode_info).setBackground(getResources().getDrawable(R.drawable.list_item_shape));
					}
				}
			}
	        setTemperaturePic(temperature_setting,temperature_mode_enable);
	        
	        //4,update current temperature and status
	        updateCurrentTemperature();
	        
	        //5,record this index
	     
	        SharedPreferences.Editor e=Utils.getSharedPpreferenceEdit(getActivity());
			e.putInt(Utils.SHARE_PREFERENCE_CUP_TEMPERATURE_MODE, temperature_mode_index);
			e.commit();
			
		}
		
		

	}

	private class MyListener implements OnClickListener,OnLongClickListener {
		int mPosition;

		public MyListener(int inPosition) {
			mPosition = inPosition;
		}


		@Override
		public void onClick(View v) {
			if(temperature_mode_index==mPosition){
				temperatureList.get(mPosition).put(VIEW_RADIO_BTN, false);
				temperature_mode_index = -1;
				// update temperaturemode
				hlva.notifyDataSetChanged();
			} else {
				//in order to prevent the radio button multi select when BT is disconnect
				if(v instanceof RadioButton){
					Utils.Log(" yeah , i am Radio Button");
					RadioButton rb=(RadioButton)v;
					rb.setChecked(false);
				}
				//1 get the select temperature and info
				int setTemperature = Integer
						.parseInt((String) temperatureList.get(mPosition).get(VIEW_TEMPERATURE_TEXT));
				String temperatureInfo=(String) temperatureList.get(mPosition).get(VIEW_INFO_TEXT);
				//2 send the select temperature to bt and server   and  show waiting dialog
				setSelectTemperatureFromMode(setTemperature,temperatureInfo);
				//3 set select
				temp_index = mPosition;
				
			}
		}


		@Override
		public boolean onLongClick(View v) {
			Utils.Log(" onLongClick  edit the temperature ");
			//1 edit name and temperature
			int setTemperature = Integer
					.parseInt((String) temperatureList.get(mPosition).get(VIEW_TEMPERATURE_TEXT));
			String temperatureInfo=(String) temperatureList.get(mPosition).get(VIEW_INFO_TEXT);
			Utils.Log(" onLongClick   temperature="+setTemperature+",temperatureInfo"+temperatureInfo);

					LayoutInflater inflater = getActivity().getLayoutInflater();
					final View layout = inflater.inflate(R.layout.tab_water_select_dialog,
							(ViewGroup) v.findViewById(R.id.dialog));
					infoString = (EditText) layout.findViewById(R.id.info_input);
					tempString = (EditText) layout.findViewById(R.id.temp_input);
					infoString.setText(temperatureInfo);
					tempString.setText(setTemperature+"");
					
					final AlertDialog ad = new AlertDialog.Builder(getActivity())
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							Map<String, Object> m=new HashMap<String, Object>();
							m.put(VIEW_INFO_TEXT, infoString.getText().toString());
							m.put(VIEW_TEMPERATURE_TEXT, tempString.getText().toString());
							m.put(VIEW_RADIO_BTN, false);
							//temperatureList.add(m);
							//replace
							temperatureList.set(mPosition, m);
							doUpdate();
							
							//current select  resent to set temp          same as onclick
							if(temperature_mode_index==mPosition){
								int editedTemperature = Integer.parseInt(tempString.getText().toString());
								//2 send the select temperature to bt and server   and  show waiting dialog
								setSelectTemperatureFromMode(editedTemperature,tempString.getText().toString());
								//3 set select
								temp_index = mPosition;
							}
						}

					}).setNegativeButton("取消", null).create();
					
					ad.setTitle("温度模式设定");
					ad.setView(layout);
					ad.show();
					ad.getCurrentFocus();
					
					try {  
					    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");  
					    mAlert.setAccessible(true);  
					    Object alertController = mAlert.get(ad);  
					  
					    Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");  
					    mTitleView.setAccessible(true);  
					  
					    TextView title = (TextView) mTitleView.get(alertController);  
//					    title.setTextColor(0xffff0022);   
//					    title.setGravity(Gravity.CENTER);
					} catch (NoSuchFieldException e) {  
					    e.printStackTrace();  
					} catch (IllegalArgumentException e) {  
					    e.printStackTrace();  
					} catch (IllegalAccessException e) {  
					    e.printStackTrace();  
					}  
					
					Button adPosiButton=ad.getButton(DialogInterface.BUTTON_POSITIVE);
					//adPosiButton.setEnabled(false);
					
					Message msg=new Message();
					msg.what=MSG_SHOW_IM;
					msg.arg1=1;
					mHandler.sendMessage(msg);
					/*adPosiButton.setBackground(getActivity().getResources().getDrawable(R.drawable.long_button_selector));
					 LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
					 lp1.setMargins(50, 0, 0, 0);
					adPosiButton.setLayoutParams(lp1);
					
					LinearLayout l=	(LinearLayout) adPosiButton.getParent();
					l.setGravity(Gravity.CENTER);
					l.setBackground(null);
					l.setDividerDrawable(null);
					
					Button adNegaButton=ad.getButton(AlertDialog.BUTTON_NEGATIVE);
					adNegaButton.setEnabled(false);
					adNegaButton.setBackground(getActivity().getResources().getDrawable(R.drawable.long_button_selector));
					 LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
					 lp2.setMargins(0, 0, 50, 0);
					adNegaButton.setLayoutParams(lp2);*/
					
					tempString.addTextChangedListener(new TextWatcher() {
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void afterTextChanged(Editable s) {
							ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
							if(s!=null&&!"".equals(s.toString())){
								try {
								int a=Integer.parseInt(s.toString());
								String info_text=infoString.getText().toString();
								if(a<=70&&a>=20&&!TextUtils.isEmpty(info_text)){
									ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
								}
								} catch (Exception e) {
									// i dont care this error
								}							
							}
						}
					});
					infoString.addTextChangedListener(new TextWatcher() {
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void afterTextChanged(Editable s) {
							ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
							if(s!=null&&!"".equals(s.toString())){
								try {
								String temp_text=tempString.getText().toString();
								int a=Integer.parseInt(temp_text.toString());
								if(a<=70&&a>=20&&!TextUtils.isEmpty(s)){
									ad.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
								}
								} catch (Exception e) {
									// i dont care this error
								}							
							}
						}
					});
			
			return true;
		}
	}
	
	private void setSelectTemperatureFromMode(int temp,String info){
		//1 send to BT
		((MainActivity) getActivity()).sentSetTemperature(temp);
		
		//2 send to Server
		saveTemperatureAction(temp,info);
		
		//3,show waiting dialog
		if(pd==null){
			// there is a bug   some time the progressdialog wont show!!!!   fix it   onDestroy set pd=null  i dont know why,but it work     12:14 it doesnot work
			//TODO  idon know why and how fuck!
			pd = new ProgressDialog(getActivity());
			pd.setMessage("正在下达指令，请稍候...");
			pd.setCancelable(false);
		}
		if(!pd.isShowing())
		{
			pd.show();
			// Stops sending after a pre-defined period.
			Message msg2 = new Message();
			msg2.what = MSG_STOP_SEND;
			msg2.arg1 = temp_index;
			mHandler.sendMessageDelayed(msg2, SEND_PERIOD);
			
		}
	}
	
	
	public void setSelectTemperatureFromBT(){
		if(pd!=null){
			Utils.Log(" setSelectTemperatureFromBT dismiss:");
			pd.dismiss();
		}
		if(temp_index!=-1){
			mHandler.removeMessages(MSG_STOP_SEND);
			temperature_mode_index = temp_index;
			// 重置，确保最多只有一项被选中
			for (Map<String, Object> m : temperatureList) {
				m.put(VIEW_RADIO_BTN, false);
			}
			temperatureList.get(temp_index).put(VIEW_RADIO_BTN, true);
			
			hlva.notifyDataSetChanged();
			
			temp_index=-1;
		}
	}
	
	private void saveTemperatureAction(int temperature,String info){
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
			result.put("temperature", temperature+"");
			result.put("explanation", info);
			// send to server
			new Thread(new Runnable() {
				@Override
				public void run() {
					// http://121.199.75.79:8280/behaviour
					Utils.httpPost(Utils.URL_PATH + "/behaviour", result, mHandler);
				}
			}).start();
		} catch (Exception e) {
			Utils.Log(TAG,"xxxxxxxxxxxxxxxxxx httpPut error:" + e);
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private List<Map<String, Object>> getData() {
		return temperatureList;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();  
		if(pd!=null){
			pd.dismiss();
		}
		pd=null;
		if(sendFailAlertDialog!=null){
			sendFailAlertDialog.dismiss();
		}
		sendFailAlertDialog=null;
		if(alertDialog!=null){
			alertDialog.dismiss();
		}
		alertDialog=null;
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