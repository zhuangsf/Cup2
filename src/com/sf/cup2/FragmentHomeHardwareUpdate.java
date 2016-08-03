package com.sf.cup2;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentHomeHardwareUpdate extends Fragment {
 
	Button goBackButton;
	Button btn_hardware_update;
	
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
				new AlertDialog.Builder(getActivity())
						.setMessage("您现在使用的为最新版本系统")
				    	.setTitle("温馨提示")
						.setPositiveButton("确定", null)
						.create()
						.show();
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
}