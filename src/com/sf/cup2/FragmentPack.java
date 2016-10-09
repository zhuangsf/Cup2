package com.sf.cup2;

import android.app.Fragment;

import com.umeng.analytics.MobclickAgent;

public abstract class FragmentPack extends Fragment {

	 @Override
     public void onPause() {
         super.onPause();
         MobclickAgent.onPageEnd(getPageName());
     }

     @Override
     public void onResume() {
         super.onResume();
         MobclickAgent.onPageStart(getPageName());
     }
     
     protected abstract String getPageName();

}
