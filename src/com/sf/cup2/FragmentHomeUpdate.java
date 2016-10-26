package com.sf.cup2;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentHomeUpdate extends FragmentPack {
 
	Button goBackButton;
	TextView pair_info;
	private LinearLayout goBack;
	private TextView update;
	AlertDialog ad;	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view=inflater.inflate(R.layout.tab_home_update, null);
    	pair_info= (TextView)view.findViewById(R.id.pair_info);
    	if(true){//获取蓝牙配对信息
    		pair_info.setText(R.string.cup_number1);
    	}
		goBack = (LinearLayout)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHome());
            	ft.remove(FragmentHomeUpdate.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
		
		
		update = (TextView)view.findViewById(R.id.update);
		update.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.red_title_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				
				TextView title = (TextView)layout.findViewById(R.id.title);
				title.setText("温馨提醒");
				TextView summary = (TextView)layout.findViewById(R.id.summary);
				summary.setText("您当前版本为最新版本");
				TextView ok = (TextView) layout.findViewById(R.id.ok);
				ok.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						ad.dismiss();
					}
				});
				
				TextView cancel = (TextView) layout.findViewById(R.id.cancel);
				cancel.setVisibility(View.GONE);

				AlertDialog.Builder alertBuiler = new AlertDialog.Builder(getActivity());
				ad = alertBuiler.create();
				ad.setView(layout);
				ad.show();
				
				

				
				
				
			}
		});
		
        return view; 
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static FragmentHomeUpdate newInstance(Bundle b){
    	FragmentHomeUpdate fd=new FragmentHomeUpdate();
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
		return FragmentHomeUpdate.class.getName();
	}
}