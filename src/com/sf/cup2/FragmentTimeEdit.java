package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.Arrays;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.aigestudio.wheelpicker.WheelPicker;
import com.sf.cup2.utils.Utils;

public class FragmentTimeEdit extends FragmentPack {
	private LinearLayout goBack;
	private LinearLayout save;
	private WheelPicker wheelPickerHour;
	private WheelPicker wheelPickerMinute;

	private int alarmIndex = -1;

	private View popupView;
	PopupWindow popupWindow;
	private Handler handler;
	Runnable runnable;
	private EditText editAlarmTitle;
	private boolean bEditMode;
	private int defaultHour = 8;
	private int defaultMinute = 0;
	private String defaultTitle;
	private LinearLayout delete_button;
	private TextView title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		popupView = LayoutInflater.from(getActivity()).inflate(
				R.layout.save_success, null);
		popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(getActivity().getResources()
				.getDrawable(R.drawable.bounced_success));

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			public void onDismiss() {
				// TODO Auto-generated method stub
				defaultTitle = null;
				// FragmentTransaction
				// ft=getActivity().getFragmentManager().beginTransaction();
				// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// ft.add(R.id.fragmentfield, new FragmentTime());
				// ft.remove(FragmentTimeEdit.this);
				// ft.addToBackStack(null);
				// ft.commit();

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

		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// handler自带方法实现定时器
				try {
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();

						// FragmentTransaction
						// ft=getActivity().getFragmentManager().beginTransaction();
						// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						// ft.add(R.id.fragmentfield, new FragmentTime());
						// ft.remove(FragmentTimeEdit.this);
						// ft.addToBackStack(null);
						// ft.commit();

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_time_edit, null);
		title = (TextView) view.findViewById(R.id.title);
		delete_button = (LinearLayout) view.findViewById(R.id.delete_button);
		if (bEditMode) {
			delete_button.setVisibility(View.VISIBLE);

			delete_button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					if (alarmIndex == -1) {
						return;
					}

					SharedPreferences.Editor e = Utils
							.getSharedPpreferenceEdit(getActivity());
					e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE
							+ alarmIndex, false);
					e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
							+ alarmIndex, false);
					e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME
							+ alarmIndex, "00:00");
					e.commit();

					defaultTitle = null;

//					FragmentTransaction ft = getActivity().getFragmentManager()
//							.beginTransaction();
//					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//					ft.add(R.id.fragmentfield, new FragmentTime());
//					ft.remove(FragmentTimeEdit.this);
//					ft.addToBackStack(null);
//					ft.commit();

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
			title.setText("编辑闹钟");
		} else {
			delete_button.setVisibility(View.GONE);
			title.setText("添加闹钟");
		}

		editAlarmTitle = (EditText) view.findViewById(R.id.editAlarmTitle);
		if (defaultTitle != null) {
			editAlarmTitle.setText(defaultTitle);
		}

		goBack = (LinearLayout) view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				defaultTitle = null;

				new Thread() {
					public void run() {
						try {
							Instrumentation inst = new Instrumentation();
							inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
						} catch (Exception e) {

						}
					}
				}.start();
				
				
				
//				FragmentTransaction ft = getActivity().getFragmentManager()
//						.beginTransaction();
//				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//				ft.add(R.id.fragmentfield, new FragmentTime());
//				ft.remove(FragmentTimeEdit.this);
//				ft.addToBackStack(null);
//				ft.commit();
			}
		});

		save = (LinearLayout) view.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 显示成功对话框
				popupWindow.showAtLocation(popupView, Gravity.CENTER_VERTICAL,
						0, 0);

				if (alarmIndex == -1) {
					return;
				}

				SharedPreferences.Editor e = Utils
						.getSharedPpreferenceEdit(getActivity());
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_VISIBILE
						+ alarmIndex, true);
				e.putBoolean(Utils.SHARE_PREFERENCE_CUP_ALARM_IS_ON
						+ alarmIndex, true);

				int hour = wheelPickerHour.getCurrentItemPosition();
				int minute = wheelPickerMinute.getCurrentItemPosition();

				Utils.Log("save editAlarmTitle.getText().toString() :"
						+ editAlarmTitle.getText().toString());

				String time = String.format("%02d:%02d", hour, minute);
				e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TIME + alarmIndex,
						time);

				if (editAlarmTitle.getText().toString().length() == 0) {
					e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TITLE
							+ alarmIndex, "该喝水啦");
				} else {
					e.putString(Utils.SHARE_PREFERENCE_CUP_ALARM_TITLE
							+ alarmIndex, editAlarmTitle.getText().toString());
				}

				e.commit();
				handler.postDelayed(runnable, 2000);
			}
		});

		wheelPickerHour = (WheelPicker) view.findViewById(R.id.wheelPickerHour);
		wheelPickerHour.setData(Arrays.asList(getResources().getStringArray(
				R.array.WheelArrayHour)));
		wheelPickerHour.setSelectedItemPosition(defaultHour);
		// 设置默认值

		wheelPickerMinute = (WheelPicker) view
				.findViewById(R.id.wheelPickerMinute);
		wheelPickerMinute.setData(Arrays.asList(getResources().getStringArray(
				R.array.WheelArrayMinute)));
		// 设置默认值
		wheelPickerMinute.setSelectedItemPosition(defaultMinute);
		return view;
	}

	public static FragmentTimeEdit newInstance(Bundle b) {
		FragmentTimeEdit fd = new FragmentTimeEdit();
		fd.setArguments(b);
		return fd;
	}

	// 这个是总的数据表格的index
	public void setAlarmIndex(int index) {
		alarmIndex = index;
	}

	// 是否是编辑模式,编辑模式增加删除按钮
	public void setBooleanEditMode(boolean bEditMode) {
		this.bEditMode = bEditMode;

		return;
	}

	public void setDefaultTitle(String title) {
		defaultTitle = title;
	}

	// 设置默认时间
	public void setDefaultTime(String HourMinute) {
		if (HourMinute == null || "".equals(HourMinute)) {
			return;
		}
		String[] timeArray = HourMinute.split(":");
		if (timeArray != null) {
			defaultHour = Integer.parseInt(timeArray[0]);
			defaultMinute = Integer.parseInt(timeArray[1]);
		}
	}

	public void setDefaultTime(int Hour, int Minute) {
		defaultHour = Hour;
		defaultMinute = Minute;
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
		return FragmentTimeEdit.class.getName();
	}
}
