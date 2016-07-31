package com.sf.cup2;

import java.lang.reflect.Field;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentData extends Fragment {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_data, null);
    }
 
    public static FragmentData newInstance(Bundle b){
    	FragmentData fd=new FragmentData();
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