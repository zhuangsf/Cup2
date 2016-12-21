package com.sf.cup2;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.aigestudio.wheelpicker.WheelPicker;
import com.aigestudio.wheelpicker.WheelPicker.OnWheelChangeListener;
import com.sf.cup2.utils.Utils;

public class FragmentAddData extends FragmentPack {
	private LinearLayout goBack;
	private LinearLayout save;
	

	private WheelPicker wheelPickerHour;
	private WheelPicker wheelPickerMinute;
	private LinearLayout date;
    private TextView date_string;
	private String sYear;
	private String sMonth;
	private String sDay;

	private LinearLayout time;
	private TextView time_string;
	private int nHour;
	private int nMinute;
	
	private EditText water_value;
	private String sWaterValue;
	
    private View inflate;
    private Dialog dialog;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_home_add_data, null);

		goBack = (LinearLayout) view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {



				new Thread() {
					public void run() {
						try {
							Instrumentation inst = new Instrumentation();
							inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
						} catch (Exception e) {

						}
					}
				}.start();
				
				

			}
		});

		save = (LinearLayout) view.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				
				sWaterValue = water_value.getText().toString();
				
				if(sWaterValue == null || sWaterValue.length() == 0){
				    Toast.makeText(getActivity(),"请输入喝水量",0).show();    //弹出一个自动消失的提示框
				    return;
				}
				
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(water_value.getWindowToken(), 0);
			    
				insertData();	
				new Thread() {
					public void run() {
						try {
							Instrumentation inst = new Instrumentation();
							inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
						} catch (Exception e) {

						}
					}
				}.start();
//				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//				ft.add(R.id.fragmentfield, new FragmentData());
//				ft.remove(FragmentAddData.this);
//				ft.addToBackStack(null);
//				ft.commit();	   
			}
		});

		
		water_value = (EditText)view.findViewById(R.id.water_value); 
		
		date_string = (TextView)view.findViewById(R.id.date_string);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		final String[] clickDate = format.format(new java.util.Date()).split("-");

		sYear = clickDate[0];
		sMonth = clickDate[1];
		sDay = clickDate[2];

		date_string.setText(sYear + "年" + sMonth + "月" + sDay	+ "日");
		
		date = (LinearLayout) view.findViewById(R.id.date);
		date.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
						sYear = year + "";
						sMonth = month + 1 + "";
						if(month < 9 )
						{
							sMonth = "0"+sMonth;
						}
						sDay = dayOfMonth + "";
						if(dayOfMonth < 10 )
						{
							sDay = "0"+sDay;
						}
						date_string.setText(sYear + "年" + sMonth + "月" + sDay	+ "日");

					}
				}, Integer.parseInt(sYear), // 传入年份
						Integer.parseInt(sMonth) - 1, // 传入月份
						Integer.parseInt(sDay) // 传入天数
				);
				dialog.show();


			}
		});
		
		Time t = new Time("GMT+8"); 
		t.setToNow(); // 取得系统时间。
		nHour = (t.hour +8) % 24; // 0-23
		nMinute = t.minute;
		time_string  = (TextView) view.findViewById(R.id.time_string);
		time_string.setText(String.format("%02d:%02d",nHour,nMinute));
		
		
		time = (LinearLayout) view.findViewById(R.id.time);
		time.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showTimeSelectWidget();
			}
		});

		return view;
	}

	
	public void showTimeSelectWidget( ){
        dialog = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(getActivity()).inflate(R.layout.time_select_widget, null);
        //初始化控件
		
		wheelPickerHour = (WheelPicker) inflate.findViewById(R.id.wheelPickerHour);
		wheelPickerHour.setData(Arrays.asList(getResources().getStringArray(
				R.array.WheelArrayHour)));
		wheelPickerHour.setSelectedItemPosition(nHour);
		wheelPickerHour.setOnWheelChangeListener(new OnWheelChangeListener()
			{
	
				@Override
				public void onWheelScrolled(int offset) {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onWheelSelected(int position) {
					// TODO Auto-generated method stub
					nHour = position;
					time_string.setText(String.format("%02d:%02d",nHour,nMinute));
				}
	
				@Override
				public void onWheelScrollStateChanged(int state) {
					// TODO Auto-generated method stub
					
				}
				
			}
		);		
		
		
		// 设置默认值

		wheelPickerMinute = (WheelPicker) inflate
				.findViewById(R.id.wheelPickerMinute);
		wheelPickerMinute.setData(Arrays.asList(getResources().getStringArray(
				R.array.WheelArrayMinute)));
		// 设置默认值
		wheelPickerMinute.setSelectedItemPosition(nMinute);
		
		wheelPickerMinute.setOnWheelChangeListener(new OnWheelChangeListener()
				{
		
					@Override
					public void onWheelScrolled(int offset) {
						// TODO Auto-generated method stub
						
					}
		
					@Override
					public void onWheelSelected(int position) {
						// TODO Auto-generated method stub
						nMinute = position;
						time_string.setText(String.format("%02d:%02d",nHour,nMinute));
					}
		
					@Override
					public void onWheelScrollStateChanged(int state) {
						// TODO Auto-generated method stub
						
					}
					
				}
		);
        
        
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }	
	
	
	public static FragmentAddData newInstance(Bundle b) {
		FragmentAddData fd = new FragmentAddData();
		fd.setArguments(b);
		return fd;
	}


	private void insertData()
	{
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
   	    long id;
    	String date = sYear+"-"+sMonth+"-"+sDay;
					
    	String time = String.format("%02d:%02d", nHour,nMinute);
	
    	id = db.insertWaterData(
			date,
			time,
			sWaterValue);

		db.close();
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
		return FragmentAddData.class.getName();
	}
}
