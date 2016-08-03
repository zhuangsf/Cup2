package com.sf.cup2;

import java.lang.reflect.Field;

import com.sf.cup2.utils.Utils;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentHomeReset extends Fragment {
 
	Button reset_cancel;
	Button reset_ok;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v=inflater.inflate(R.layout.tab_home_retset, null);
    	reset_cancel= (Button)v.findViewById(R.id.reset_cancel);
    	reset_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm= getActivity().getFragmentManager();
				fm.popBackStackImmediate();
			}
		});
    	
    	
    	reset_ok= (Button)v.findViewById(R.id.reset_ok);
    	reset_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(getActivity())
				.setMessage("确定要恢复出厂设置？")
		    	.setTitle("温馨提示")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//显示等待
						clearAll();
					}
				})
				.setNegativeButton("取消", null)
				.create()
				.show();
			}
		});
        return v; 
    }
    
    private void clearAll(){
    	SharedPreferences p;
		SharedPreferences.Editor e;
		p =getActivity().getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
		e = p.edit();
		e.clear();
		e.commit();
		
		
		new AlertDialog.Builder(getActivity())
		.setMessage("恢复出厂后，请重新登陆")
    	.setTitle("温馨提示")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().finish();
			}
		})
		.create()
		.show();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 
    public static FragmentHomeReset newInstance(Bundle b){
    	FragmentHomeReset fd=new FragmentHomeReset();
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