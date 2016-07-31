package com.sf.cup2.guide;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.sf.cup2.MainActivity;
import com.sf.cup2.R;
import com.sf.cup2.utils.Utils;

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

public class GuideView extends Activity{
	     private ViewPager viewPager;  
	     private ArrayList<View> pageViews;  
	     private ImageView imageView;  
	     private ImageView[] imageViews; 
	     private ViewGroup main;
	     private ViewGroup group;
	     private Button buttonGo;
	     View maskView;//
	     
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        LayoutInflater inflater = getLayoutInflater();  
	        pageViews = new ArrayList<View>();  
	        pageViews.add(inflater.inflate(R.layout.item01, null));  
	        pageViews.add(inflater.inflate(R.layout.item02, null));  
	        pageViews.add(inflater.inflate(R.layout.item03, null));  
	        pageViews.add(inflater.inflate(R.layout.item04, null));  
//	        pageViews.add(inflater.inflate(R.layout.item05, null));
//	        pageViews.add(inflater.inflate(R.layout.item06, null));
	        imageViews = new ImageView[pageViews.size()];  
	        main = (ViewGroup)inflater.inflate(R.layout.guideview, null);  
	        group = (ViewGroup)main.findViewById(R.id.viewGroup);  
	        viewPager = (ViewPager)main.findViewById(R.id.guidePages);  
	        buttonGo = (Button)main.findViewById(R.id.buttonGo);
	        for (int i = 0; i < pageViews.size(); i++) {  
	            imageView = new ImageView(GuideView.this);  
	            imageView.setLayoutParams(new LayoutParams(20,20));  
	            imageView.setPadding(20, 0, 20, 0);  
	            imageViews[i] = imageView;  
	            if (i == 0) {  
	                //Ĭ��ѡ�е�һ��ͼƬ
	                imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);  
	            } else {  
	                imageViews[i].setBackgroundResource(R.drawable.page_indicator);  
	            }  
	            group.addView(imageViews[i]);  
	        }  
	        setContentView(main);
	        viewPager.setAdapter(new GuidePageAdapter());  
	        viewPager.setOnPageChangeListener(new GuidePageChangeListener()); 
	        
	        
	        GifImageButton a;
	      /* ((TextView) main.findViewById(R.id.skip)).setOnClickListener(new View.OnClickListener() {
	           	@Override
	           	public void onClick(View arg0) {
	           		// TODO Auto-generated method stub
	           		Intent intent = new Intent();  
						intent.setClass(GuideView.this,MainActivity.class);  
						startActivity(intent);  
						finish(); 
	           	}
	           });*/
	        
	        
	        
	      //create a mask to disable mode change
			maskView=new View(this);
			maskView.setLayoutParams(new ViewGroup.LayoutParams(
			            ViewGroup.LayoutParams.FILL_PARENT,
			            ViewGroup.LayoutParams.FILL_PARENT));
			maskView.setBackground(getResources().getDrawable(R.drawable.guide_logo));
			maskView.setClickable(true);// set true  to disable other view click
			maskView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					maskView.setVisibility(View.GONE);
					main.removeView(maskView);
				}
			});
			
			main.addView(maskView);
//			 dismissMaskView();
			 new dismissMaskViewTask().execute();
			
	    }
	    
	    private Timer timer = new Timer(true);
		public void dismissMaskView(){
			//任务
			TimerTask task = new TimerTask() {
			  @Override
			public void run() {
				  // must do in ui thread    
				  main.removeView(maskView);
				  }
			};
			 
			//启动定时器
			timer.schedule(task, 2000);
		}
		
		 private class dismissMaskViewTask extends AsyncTask<Void, Void, String[]> {

				@Override
				protected String[] doInBackground(Void... params) {
					// Simulates a background job.
					try {
						Thread.sleep(new Random().nextInt(1000)+1500);
					} catch (InterruptedException e) {
					}
					return null;
				}

				@Override
				protected void onPostExecute(String[] result) {
					// Do some stuff here
					main.removeView(maskView);
					super.onPostExecute(result);
				}
			}
	    
	    
	    class GuidePageAdapter extends PagerAdapter {  
	        @Override  
	        public int getCount() {  
	            return pageViews.size();  
	        }  
	        @Override  
	        public boolean isViewFromObject(View arg0, Object arg1) {  
	            return arg0 == arg1;  
	        }  
	        @Override  
	        public int getItemPosition(Object object) {  
	            // TODO Auto-generated method stub  
	            return super.getItemPosition(object);  
	        }  
	        @Override  
	        public void destroyItem(View arg0, int arg1, Object arg2) {  
	            // TODO Auto-generated method stub  
	            ((ViewPager) arg0).removeView(pageViews.get(arg1));  
	        }  
	        @Override  
	        public Object instantiateItem(View arg0, int arg1) {  
	            // TODO Auto-generated method stub  
	            ((ViewPager) arg0).addView(pageViews.get(arg1));  
	            if(arg1==pageViews.size()-1)
	            {
	            pageViews.get(arg1).setOnClickListener(new View.OnClickListener() {
	            	@Override
					public void onClick(View arg0) {
	            		SharedPreferences p=Utils.getSharedPpreference(GuideView.this);
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(GuideView.this);
						e.putInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS,p.getInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, 0)+1 );
						e.commit();
						Intent intent = new Intent();
						intent.setClass(GuideView.this, MainActivity.class);
						// ����B
						startActivity(intent);
						finish();
					}
	            });
	            buttonGo.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						SharedPreferences p=Utils.getSharedPpreference(GuideView.this);
				        SharedPreferences.Editor e=Utils.getSharedPpreferenceEdit(GuideView.this);
				        e.putInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS,p.getInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, 0)+1 );
						e.commit();
						
	            		Intent intent = new Intent();  
						intent.setClass(GuideView.this,MainActivity.class);  
						//����B  
						startActivity(intent);  
						finish(); 
						
					}
				});
	            }
	            return pageViews.get(arg1);  
	        }  
	        @Override  
	        public void restoreState(Parcelable arg0, ClassLoader arg1) {  
	            // TODO Auto-generated method stub  
	        }  
	        @Override  
	        public Parcelable saveState() {  
	            // TODO Auto-generated method stub  
	            return null;  
	        }  

	        @Override  
	        public void startUpdate(View arg0) {  
	            // TODO Auto-generated method stub  
	        }  
	        @Override  
	        public void finishUpdate(View arg0) {  
	            // TODO Auto-generated method stub  
	        }  
	    } 
	    class GuidePageChangeListener implements OnPageChangeListener {  
	        @Override  
	        public void onPageScrollStateChanged(int arg0) {  
	            // TODO Auto-generated method stub  
	        }  
	        @Override  
	        public void onPageScrolled(int arg0, float arg1, int arg2) {  
	            // TODO Auto-generated method stub  
	        }  
	        @Override  
	        public void onPageSelected(int arg0) {  
	            for (int i = 0; i < imageViews.length; i++) {  
	                imageViews[arg0].setBackgroundResource(R.drawable.page_indicator_focused);
	                if (arg0 != i) {  
	                    imageViews[i].setBackgroundResource(R.drawable.page_indicator);  
	                }  
	            }
	            Utils.Log("imageViews.length = "+imageViews.length+" ,arg0="+arg0);
	            if(arg0==(imageViews.length-1)){
	            	buttonGo.setVisibility(View.VISIBLE);
	            }else{
	            	buttonGo.setVisibility(View.INVISIBLE);
	            }
	        }  
	    }  
	}