package com.sf.cup2;

import java.lang.reflect.Field;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentHomePairInfo extends Fragment {
 
	Button goBackButton;
	TextView pair_info;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v=inflater.inflate(R.layout.tab_home_pair_info, null);
    	pair_info= (TextView)v.findViewById(R.id.pair_info);
    	if(true){//获取蓝牙配对信息
    		pair_info.setText(R.string.cup_number1);
    	}
    	
        return v; 
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static FragmentHomePairInfo newInstance(Bundle b){
    	FragmentHomePairInfo fd=new FragmentHomePairInfo();
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