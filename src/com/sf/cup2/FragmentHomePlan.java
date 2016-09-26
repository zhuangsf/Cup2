package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FragmentHomePlan extends Fragment {

	private ScrollView ruler;
	private LinearLayout rulerlayout;

	private ImageView goBack;
	private ImageView save;
	
	private TextView line_hint;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	
	protected void initViews(View v) {


		ruler = (ScrollView) v.findViewById(R.id.vruler);
		ruler.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();

				switch (action) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							line_hint.setText(String.valueOf((int) Math.ceil(ruler.getScrollY())) +"ml");
						}
					}, 1000);
					break;
				}
				return false;
			}


		});

		line_hint = (TextView) v.findViewById(R.id.line_hint);
		rulerlayout = (LinearLayout) v.findViewById(R.id.vruler_layout);

		new Handler().postDelayed((new Runnable() {
			@Override
			public void run() {
				constructRuler();
			}
		}), 300);

		goBack = (ImageView)v.findViewById(R.id.goBack);
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
		save = (ImageView)v.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			//todo 保存的操作
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
		return v;
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
}