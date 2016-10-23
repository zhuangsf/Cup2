package com.sf.cup2;

import java.lang.reflect.Field;

import com.sf.cup2.login.LoginActivity;
import com.sf.cup2.utils.Utils;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentHomeHardwareUpdate extends FragmentPack {
 
	Button goBackButton;
	Button btn_hardware_update;
	AlertDialog ad;	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v=inflater.inflate(R.layout.tab_home_hardware_update, null);
    	goBackButton= (Button)v.findViewById(R.id.goBack);
    	goBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm= getActivity().getFragmentManager();
				fm.popBackStackImmediate();
			}
		});
    	
    	
    	btn_hardware_update=(Button)v.findViewById(R.id.btn_hardware_update);
    	btn_hardware_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.red_title_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				
				TextView title = (TextView)layout.findViewById(R.id.title);
				title.setText("固件升级");
				TextView summary = (TextView)layout.findViewById(R.id.summary);
				summary.setText(R.string.tips);
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
				
				
//				new AlertDialog.Builder(getActivity())
//						.setMessage(R.string.newest_version)
//				    	.setTitle(R.string.tips)
//						.setPositiveButton(R.string.ok, null)
//						.create()
//						.show();
			}
		});
    	
    	
        return v; 
    }
    
    public static FragmentHomeHardwareUpdate newInstance(Bundle b){
    	FragmentHomeHardwareUpdate fd=new FragmentHomeHardwareUpdate();
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
		return FragmentHomeHardwareUpdate.class.getName();
	}
}