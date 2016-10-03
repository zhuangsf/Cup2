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
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentAccount extends Fragment {
 
	private TextView phone_number;
	private String account;
	private ImageView goBack;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		account = p.getString(Utils.SHARE_PREFERENCE_CUP_PHONE, "error");
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.tab_account, null);
    	phone_number = (TextView)view.findViewById(R.id.phone_number);
    	
    	if("error".equals(account))
    	{
    		phone_number.setText("账号异常");
    	}
    	else
    	{
    		phone_number.setText(account);
    	}
    	
    	
		goBack = (ImageView)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHome());
            	ft.remove(FragmentAccount.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
    	
        return view;
    }
 
    
    public static FragmentAccount newInstance(Bundle b){
    	FragmentAccount fd=new FragmentAccount();
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