package com.sf.cup2;

import java.lang.reflect.Field;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentHomePairInfo extends FragmentPack {
 
	Button goBackButton;
	TextView pair_info;
	private LinearLayout goBack;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view=inflater.inflate(R.layout.tab_home_pair_info, null);
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
            	ft.remove(FragmentHomePairInfo.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
        return view; 
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

	@Override
	protected String getPageName() {
		return FragmentHomePairInfo.class.getName();
	}
}