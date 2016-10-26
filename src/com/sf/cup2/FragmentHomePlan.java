package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sf.cup2.utils.Utils;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FragmentHomePlan extends FragmentPack {

	private ScrollView ruler;
	private LinearLayout rulerlayout;

	private LinearLayout goBack;
	private LinearLayout save;
	
	private TextView line_hint;
	private TextView title1;
	
	private String planValue;
	private ImageView save_success;
	private View popupView;
	PopupWindow popupWindow;
	private Handler handler;
	Runnable runnable;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		planValue = p.getString(Utils.SHARE_PREFERENCE_CUP_PLAN, "null");
		
		if("null".equals(planValue))
		{
			planValue = Utils.getSuggestPlan(getActivity());    //这个值要根据健康管理来生成
			SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
			e.putString(Utils.SHARE_PREFERENCE_CUP_PLAN, planValue);
			e.commit();
		}
		
	    popupView = LayoutInflater.from(getActivity()).inflate(R.layout.save_success, null);   
	    popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);  
	    popupWindow.setFocusable(true);  
	    popupWindow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bounced_success) );
	    save_success = (ImageView)popupView.findViewById(R.id.save_success);  
	    
	    handler = new Handler();  
	    runnable = new Runnable() {  
	        @Override  
	        public void run() {  
	            // handler自带方法实现定时器  
	            try {  
	            	if(popupWindow != null && popupWindow.isShowing())
	            	{
	            		popupWindow.dismiss();
	            	}
	            } catch (Exception e) {  
	                // TODO Auto-generated catch block  
	                e.printStackTrace();  
	            }  
	        }  
	    };  
	}

	
	protected void initViews(View v) {


		ruler = (ScrollView) v.findViewById(R.id.vruler);
		

		ruler.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				planValue = String.valueOf((int) Math.ceil(ruler.getScrollY()));
				line_hint.setText(String.valueOf((int) Math.ceil(ruler.getScrollY())) +"ml");
				
				Utils.Log("onTouch action = "+action+" planValue = "+planValue);
				
				switch (action) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							planValue = String.valueOf((int) Math.ceil(ruler.getScrollY()));
							line_hint.setText(String.valueOf((int) Math.ceil(ruler.getScrollY())) +"ml");
						}
					}, 1000);
					break;
				}
				return false;
			}


		});
		
		title1 = (TextView) v.findViewById(R.id.title1);
		title1.setText("每天需要的饮水量(根据您的个人信息)推荐值约为 "+Utils.getSuggestPlan(getActivity())+" ml");
		
		line_hint = (TextView) v.findViewById(R.id.line_hint);
		line_hint.setText(planValue+" ml");
		
		rulerlayout = (LinearLayout) v.findViewById(R.id.vruler_layout);

		
		new Handler().postDelayed((new Runnable() {
			@Override
			public void run() {
				constructRuler();
			}
		}), 10);

		goBack = (LinearLayout)v.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHome());
            	ft.remove(FragmentHomePlan.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
		save = (LinearLayout)v.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			//todo 保存的操作
				popupWindow.showAtLocation(popupView, Gravity.CENTER_VERTICAL, 0, 0); 
			//	title1.setText("每天需要的饮水量(根据您的个人信息)推荐值约为 "+planValue+" ml");
				
				
				Utils.Log("save planValue = "+planValue);
				
				SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
				e.putString(Utils.SHARE_PREFERENCE_CUP_PLAN, planValue);
				e.commit();
				handler.postDelayed(runnable, 2000); 
			}
		});
	}	
	
	
	private void constructRuler() {
		int rulerHeight = ruler.getHeight();

		View topview = (View) LayoutInflater.from(getActivity()).inflate(
				R.layout.blankvrulerunit, null);
		topview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rulerHeight / 2));   //设置之后,可以上线滑动到一半
		rulerlayout.addView(topview);
		for (int i = 0; i < 50; i++) {
			View view = (View) LayoutInflater.from(getActivity()).inflate(
					R.layout.vrulerunit, null);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,100));
			TextView tv = (TextView) view.findViewById(R.id.vrulerunit);
			tv.setText(String.valueOf(i * 100));
			rulerlayout.addView(view);
		}
		View bottomview = (View) LayoutInflater.from(getActivity()).inflate(
				R.layout.blankvrulerunit, null);
		bottomview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rulerHeight / 2));
		rulerlayout.addView(bottomview);
	}	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_home_plan, null);

		initViews(v);
		
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				scroll();
			}
		}, 400);
		return v;
	}

	private void scroll() {
		ruler.smoothScrollTo(0,Integer.parseInt(planValue));
	}

	
	
	
	
	
	
	
	
	
	
	
	public static FragmentHomePlan newInstance(Bundle b) {
		FragmentHomePlan fd = new FragmentHomePlan();
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


	@Override
	protected String getPageName() {
		return FragmentHomePlan.class.getName();
	}
}