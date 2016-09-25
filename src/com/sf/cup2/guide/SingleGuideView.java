package com.sf.cup2.guide;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.sf.cup2.MainActivity;
import com.sf.cup2.R;
import com.sf.cup2.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import pl.droidsonroids.gif.GifImageButton;

public class SingleGuideView extends Activity{

	     View maskView;//
	     private View main;
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        LayoutInflater inflater = getLayoutInflater();  
	        main = inflater.inflate(R.layout.singleguideview, null);  
	        setContentView(main);
	        dismissMaskView();
	    }
	    

	    private Timer timer = new Timer(true);
	    private void dismissMaskView(){
			//任务
			TimerTask task = new TimerTask() {
			  @Override
			public void run() {
				  // must do in ui thread    
          		    SharedPreferences p=Utils.getSharedPpreference(SingleGuideView.this);
					SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(SingleGuideView.this);
					e.putInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS,p.getInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, 0)+1 );
					e.commit();
					Intent intent = new Intent();
					intent.setClass(SingleGuideView.this, MainActivity.class);
					startActivity(intent);
					finish();
				  }
			};
			 
			//启动定时器
			timer.schedule(task, 5000);
		}
	    
	    
	    @Override
		protected void onResume() {
			super.onResume();
			
			//add for umeng
			MobclickAgent.onResume(this);
		}
	
		@Override
		protected void onPause() {
			super.onPause();
			
			//add for umeng
	        MobclickAgent.onPause(this);
		}
	}