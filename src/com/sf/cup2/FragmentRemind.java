package com.sf.cup2;

import java.lang.reflect.Field;

import com.sf.cup2.utils.Utils;

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
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FragmentRemind extends FragmentPack {
 

	private ImageView goBack;
	private String appPush_Value;
	private TextView hintText;
	private ToggleButton mTogBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		appPush_Value = p.getString(Utils.SHARE_PREFERENCE_CUP_REMIND, "on");
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.tab_remind, null);
  
    	
    	hintText = (TextView)view.findViewById(R.id.hint_text);
    	mTogBtn = (ToggleButton) view.findViewById(R.id.mTogBtn); // 获取到控件
   		mTogBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

		    	if(isChecked)
		    	{    		
		    		hintText.setText("已开启");
		    	}
		    	else
		    	{
		    		hintText.setText("未开启");
		    	}
				
				SharedPreferences.Editor e = Utils
						.getSharedPpreferenceEdit(getActivity());
				e.putString(Utils.SHARE_PREFERENCE_CUP_REMIND,
						isChecked?"on":"off");
				e.commit();				
				
				
				
				
			}
		});// 添加监听事件
    	if("on".equals(appPush_Value))
    	{    		
    		hintText.setText("已开启");
    		mTogBtn.setChecked(true);
    	}
    	else
    	{
    		hintText.setText("未开启");
    		mTogBtn.setChecked(false);
    	}
    	
		goBack = (ImageView)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHome());
            	ft.remove(FragmentRemind.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
    	
        return view;
    }
 
    
    public static FragmentRemind newInstance(Bundle b){
    	FragmentRemind fd=new FragmentRemind();
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
		return FragmentRemind.class.getName();
	}
}