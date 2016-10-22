package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.Arrays;




import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.aigestudio.wheelpicker.WheelPicker;
import com.sf.cup2.utils.Utils;

public class FragmentTimeEdit extends FragmentPack {
	private ImageView goBack;
	private WheelPicker wheelPickerHour;
	private WheelPicker wheelPickerMinute;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.tab_time_edit, null);
  
    	

    	
		goBack = (ImageView)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentTime());
            	ft.remove(FragmentTimeEdit.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
    	
		
		wheelPickerHour = (WheelPicker)view.findViewById(R.id.wheelPickerHour);
		wheelPickerHour.setData(Arrays.asList(getResources().getStringArray(R.array.WheelArrayHour)));
		
		//设置默认值
		wheelPickerHour.setSelectedItemPosition(22);
		wheelPickerMinute = (WheelPicker)view.findViewById(R.id.wheelPickerMinute);
		wheelPickerMinute.setData(Arrays.asList(getResources().getStringArray(R.array.WheelArrayMinute)));
		//设置默认值
		wheelPickerMinute.setSelectedItemPosition(59);
        return view;
    }
 
    
    public static FragmentTimeEdit newInstance(Bundle b){
    	FragmentTimeEdit fd=new FragmentTimeEdit();
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
		return FragmentTimeEdit.class.getName();
	}
}
