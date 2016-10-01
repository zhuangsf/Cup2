package com.sf.cup2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.sf.cup2.utils.Utils;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FragmentHome extends Fragment {
	private final static String TAG = FragmentHome.class.getPackage().getName() + "."
			+ FragmentHome.class.getSimpleName();
	
	Button buttonGet;
	TextView textViewGet;
	ListView homeListView;
	ListView homeList2View;
	String[] listTitle;
	String[] list2Title;
	private ImageView exit;
	
	private static final String IMAGE_FILE_NAME = "avatarImage.jpg";// 头像文件名称
	private static final String IMAGE_FILE_NAME_CROP = "avatarImage_crop.jpg";// 头像文件名称
	
	private int[] listDrawable;
	private int[] list2Drawable;
	private static final int MODIFY_INDEX=0;
	private static final int PLAN_INDEX=1;
	
	private static final int ACCOUNT_INDEX=0;
	private static final int WARN_INDEX=1;
	private static final int PAIRING_INDEX=2;
	private static final int UPDATE_INDEX=3;
	private static final int ABOUT_INDEX=4;
	Handler mHandler = new Handler()
	  {
	    @Override
		public void handleMessage(Message paramAnonymousMessage)
	    {
	     switch (paramAnonymousMessage.what)
	     {
				case 1:
					Utils.Log("xxxxxxxxxxxxxxxSG_WAT_START-1---:"+paramAnonymousMessage.obj);
					textViewGet.setText(paramAnonymousMessage.obj.toString());
					break;
				case 2:
					break;
			}
	    }
	  };
	  
	  public static FragmentHome newInstance(Bundle b){
		  FragmentHome fd=new FragmentHome();
			fd.setArguments(b);
			return fd;
		}
	  
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res =getResources();
        listTitle=res.getStringArray(R.array.home_list_top_title);
        list2Title=res.getStringArray(R.array.home_list_down_title);
        listDrawable = new int[]{
        		R.drawable.icon_modify,
        		R.drawable.icon_plan,
        };
        list2Drawable = new int[]{
        		R.drawable.icon_account,
        		R.drawable.icon_warn,
        		R.drawable.icon_pairing,
        		R.drawable.icon_nickname,
        		R.drawable.icon_about,
        };
        
        
        
    }
    
    
	@Override
	public void onResume() {
		super.onResume();
	}
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view=inflater.inflate(R.layout.tab_home, null);

    	homeListView=(ListView) view.findViewById(R.id.homeListView); 
    	HomeListViewAdapter1 hlva=new HomeListViewAdapter1(this.getActivity(), getData(), R.layout.tab_home_list_item_top,
    			new String[]{"item_image","title_text","text_view1","image_view1","image_view2"},
    			new int[]{R.id.item_image,R.id.title_text,R.id.text_view1,R.id.image_view1,R.id.image_view2});
    	setHeight(hlva,homeListView);
    	homeListView.setAdapter(hlva);
    	
    	homeList2View=(ListView) view.findViewById(R.id.homeList2View); 
    	HomeListViewAdapter2 hlva2=new HomeListViewAdapter2(this.getActivity(), getData2(), R.layout.tab_home_list_item_down,
		new String[]{"item_image","title_text","image_view"},
		new int[]{R.id.item_image,R.id.title_text,R.id.image_view});

    	setHeight(hlva2,homeList2View);
    	homeList2View.setAdapter(hlva2);
    	

		exit = (ImageView)view.findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			//todo 退出的操作

			}
		});
    	
        return view;
    }
    private class HomeListViewAdapter1 extends SimpleAdapter{
		public HomeListViewAdapter1(Context context, List<Map<String, Object>> data, int resource, String[] from,int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view=super.getView(position, convertView, parent);
			view.setOnClickListener(new MyListener1(position));
			
			TextView textView;
			ImageView imageView1;
			ImageView imageView2;
			textView = (TextView)view.findViewById(R.id.text_view1);
			imageView1 = (ImageView)view.findViewById(R.id.image_view1);
			imageView2 = (ImageView)view.findViewById(R.id.image_view2);
			
//			private static final int HEAD_INDEX=0;
//			private static final int NICK_NAME_INDEX=1;
//			private static final int GENDER_INDEX=2;
//			private static final int HEIGHT_INDEX=3;
//			private static final int PLAN_INDEX=4;
			if(position == MODIFY_INDEX)
			{
				textView.setVisibility(View.GONE);
				imageView1.setVisibility(View.VISIBLE);
				
				String avatarFilePath = Utils.getInternelStoragePath(getActivity()) + "/" + IMAGE_FILE_NAME;
				if (!TextUtils.isEmpty(avatarFilePath)) {
					Drawable d = Drawable.createFromPath(avatarFilePath);
					Utils.Log("avatar avatarFilePath:" + avatarFilePath + " ,d:" + d);
					if (d == null) {
						imageView1.setImageResource(R.drawable.ic_launcher);
					} else {
						imageView1.setImageDrawable(d);
					}
				} else {
					imageView1.setImageResource(R.drawable.ic_launcher);
				}
				
				
			//	imageView1.setImageResource(R.drawable.ic_launcher);
			}
			else if(position == PLAN_INDEX)
			{
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String planValue = p.getString(Utils.SHARE_PREFERENCE_CUP_PLAN, "null");
				
				if("null".equals(planValue))
				{
					planValue = "2000";    //这个值要根据健康管理来生成,还没处理
					SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
					e.putString(Utils.SHARE_PREFERENCE_CUP_PLAN, planValue);
					e.commit();
				}
				textView.setText(planValue);
				
				textView.setVisibility(View.VISIBLE);
				imageView1.setVisibility(View.GONE);
			}
//			else if(position == GENDER_INDEX)
//			{
//				textView.setVisibility(View.GONE);
//				
//				imageView1.setVisibility(View.VISIBLE);
//				imageView2.setVisibility(View.VISIBLE);
//				
//				SharedPreferences p=Utils.getSharedPpreference(getActivity());
//				String sex=p.getString(Utils.SHARE_PREFERENCE_CUP_SEX, "femail");
//				
//				if("femail".equals(sex))
//				{
//					imageView1.setImageResource(R.drawable.button_man1);
//					imageView2.setImageResource(R.drawable.button_woman2);
//				}
//				else
//				{
//					imageView1.setImageResource(R.drawable.button_man2);
//					imageView2.setImageResource(R.drawable.button_woman1);
//				}
//			}
//			else if(position == HEIGHT_INDEX)
//			{
//				textView.setText("180");
//				textView.setVisibility(View.VISIBLE);
//				
//				imageView1.setVisibility(View.GONE);
//				imageView2.setVisibility(View.GONE);
//			}
//			else if(position == PLAN_INDEX)
//			{
//				textView.setText("2500");
//				Drawable image = getResources().getDrawable(R.drawable.icon_next);  
//				image.setBounds(0, 0, image.getMinimumWidth(), image.getMinimumHeight());//非常重要，必须设置，否则图片不会显示  
//				textView.setCompoundDrawables(null,null, image, null);  
//				textView.setVisibility(View.VISIBLE);
//				imageView1.setVisibility(View.GONE);
//				imageView2.setVisibility(View.GONE);
//			}
			return view;
		}
    	
    }
    private class MyListener1 implements OnClickListener{  
        int mPosition;  
        public MyListener1(int inPosition){  
            mPosition= inPosition;  
        }  
        @Override  
        public void onClick(View v) {  
        	 if(MODIFY_INDEX==mPosition)
        	 {
             	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
             	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
             	ft.add(R.id.fragmentfield, new FragmentHomePerson());
             	ft.remove(FragmentHome.this);
             	ft.addToBackStack(null);
 				ft.commit();
        	 }
        	 else if(PLAN_INDEX==mPosition){
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHomePlan());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }
//             else if (PAIR_INFO_INDEX==mPosition){
//            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
//            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            	ft.add(R.id.fragmentfield, new FragmentHomePairInfo());
//            	ft.remove(FragmentHome.this);
//            	ft.addToBackStack(null);
//				ft.commit();
//            }else if(RESET_INDEX==mPosition){
//            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
//            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            	ft.add(R.id.fragmentfield, new FragmentHomeReset());
//            	ft.remove(FragmentHome.this);
//            	ft.addToBackStack(null);
//				ft.commit();
//            }else if (HARDWARE_UPDATE_INDEX==mPosition){
//            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
//            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            	ft.add(R.id.fragmentfield, new FragmentHomeHardwareUpdate());
//            	ft.remove(FragmentHome.this);
//            	ft.addToBackStack(null);
//				ft.commit();
//            }
        }  
    } 
    
    private class HomeListViewAdapter2 extends SimpleAdapter{
    	
		public HomeListViewAdapter2(Context context, List<Map<String, Object>> data, int resource, String[] from,int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view=super.getView(position, convertView, parent);
			view.setOnClickListener(new MyListener2(position));
			return view;
		}
    	
    }
    private class MyListener2 implements OnClickListener{  
        int mPosition;  
        public MyListener2(int inPosition){  
            mPosition= inPosition;  
        }  
        @Override  
        public void onClick(View v) {  
            if(0==mPosition){
            	//go to personal info
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentAccount());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }else  if(1==mPosition){
            	//go to personal info
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentRemind());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }else  if(2==mPosition){
            	//go to personal info
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHomePairInfo());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }else  if(3==mPosition){
            	//go to personal info
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHomeUpdate());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }else  if(4==mPosition){
            	//go to personal info
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHomeAbout());
            	ft.remove(FragmentHome.this);
            	ft.addToBackStack(null);
				ft.commit();
            }
            
            
        }  
          
    }  
    
    
    
    
    
    
    
    public void setHeight(BaseAdapter comAdapter,ListView l){  
        int listViewHeight = 0;  
        int adaptCount = comAdapter.getCount();  
        for(int i=0;i<adaptCount;i++){  
            View temp = comAdapter.getView(i,null,l);  
            temp.measure(0,0);  
            listViewHeight += temp.getMeasuredHeight(); 
        }  
        LayoutParams layoutParams = l.getLayoutParams();  
        layoutParams.width = LayoutParams.FILL_PARENT;  
        layoutParams.height = listViewHeight+2;  
        l.setLayoutParams(layoutParams);  
    }  
    
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < listTitle.length; i++) {
			map = new HashMap<String, Object>();
			map.put("item_image", listDrawable[i]);
			Utils.Log("listTitle = "+listTitle[i]);
			map.put("title_text", listTitle[i]);
			map.put("text_view1", "");
			map.put("image_view1", 0);
			map.put("image_view2", R.drawable.icon_next);
			list.add(map);
		}

       
        return list;
    }
	
	private List<Map<String, Object>> getData2() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < list2Title.length; i++) {
			map = new HashMap<String, Object>();
			Utils.Log("list2Title = "+list2Title[i]);
			map.put("item_image", list2Drawable[i]);
			map.put("title_text", list2Title[i]);
			map.put("image_view", R.drawable.icon_next);
			list.add(map);
		}

       
        return list;
    }
    
    private void httpGet(String url) {
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpClient httpClinet = new DefaultHttpClient();
			HttpResponse httpResponse = httpClinet.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				Utils.Log(" httpGet status " + httpResponse.getStatusLine());
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpGet start output 2");
				String result=EntityUtils.toString(entity, "UTF-8");
				// 下面这种方式写法更简单，可是没换行。
				Utils.Log("httpGet 2" + result);
				// 生成 JSON 对象
//				JSONArray jsonArray= new JSONArray(result);
				JSONObject jsonObject=new JSONObject(result);
				Message msg=new Message();
				msg.what=1;
				msg.arg1=1;
				msg.obj=jsonObject;
//				mHandler.sendEmptyMessage(1);
				mHandler.sendMessage(msg);
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpGet finish output 2"+jsonObject);
			}
		} catch (Exception e) {
			Utils.Log(TAG, "httpGet error:" + e);
		}
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