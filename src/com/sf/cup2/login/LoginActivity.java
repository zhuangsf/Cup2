package com.sf.cup2.login;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.sf.cup2.MainActivity;
import com.sf.cup2.R;
import com.sf.cup2.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends Activity {
	private final static String TAG = LoginActivity.class.getPackage().getName() + "."
			+ LoginActivity.class.getSimpleName();
	Button btnLogin;
	EditText phone_num;
	Button send_code;
	EditText check_code;
	String phoneNumString;
	int repeatSend=90;
	private long timestamp=0;
	boolean isCountDown=false;
	Thread countDownThread;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
 
		//start sms init
        SMSSDK.initSDK(this,  Utils.SMS_APP_KEY, Utils.SMS_APP_SECRET);
    	EventHandler eh=new EventHandler(){
			@Override
			public void afterEvent(int event, int result, Object data) {
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				handler.sendMessage(msg);
			}
			
		};
		SMSSDK.registerEventHandler(eh);
		
		//send code button may have 3 status  1:sending  2:wait 90seconds 3:send code
        send_code=(Button)findViewById(R.id.send_code);
        send_code.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1 network connect
				if(Utils.getNetWorkType(LoginActivity.this)==-1){
					Toast.makeText(LoginActivity.this, "�������", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				//2�������Ƿ���ȷ
				
				//3�����÷���֤��ӿ�
				phoneNumString=phone_num.getText().toString();
				if (!TextUtils.isEmpty(phoneNumString)) {
					SMSSDK.getVerificationCode("86", phoneNumString);
					send_code.setEnabled(false);
					send_code.setBackgroundResource(R.drawable.long_button_shape_disable);
					timestamp = System.currentTimeMillis();
					countDownThread = new Thread(new Runnable() {
						@Override
						public void run() {
							isCountDown=true;
							countDown(timestamp, mHandler);
						}
					});
					countDownThread.start();

				}else {
					Toast.makeText(LoginActivity.this, "�绰����Ϊ��", Toast.LENGTH_SHORT).show();
				}
				Utils.Log("verification phone ==>>"+phoneNumString);
			}
		});
        
        phone_num = (EditText) findViewById(R.id.phone_num);
        phone_num.setText("");
        phone_num.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0&& !isCountDown) {
					send_code.setEnabled(true);
					send_code.setBackgroundResource(R.drawable.long_button_selector);
				} else {
					send_code.setEnabled(false);
					send_code.setBackgroundResource(R.drawable.long_button_shape_disable);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
        phone_num.requestFocus();
		if (phone_num.getText().length() > 0&& !isCountDown) {
			send_code.setEnabled(true);
				send_code.setBackgroundResource(R.drawable.long_button_selector);
		}

		check_code = (EditText) findViewById(R.id.check_code);
		check_code.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (s.length() > 0 ) {
						btnLogin.setEnabled(true);
						btnLogin.setBackgroundResource(R.drawable.long_button_selector);
					} else {
						btnLogin.setEnabled(false);
						btnLogin.setBackgroundResource(R.drawable.long_button_shape_disable);
					}
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		
        
        
        
		btnLogin=(Button)findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!TextUtils.isEmpty(check_code.getText().toString())){
					SMSSDK.submitVerificationCode("86", phoneNumString, check_code.getText().toString());
				}else {
					Toast.makeText(LoginActivity.this, "��֤�벻��Ϊ��",Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	
	Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int event = msg.arg1;
			int result = msg.arg2;
			Object data = msg.obj;
			Log.e("event", "event="+event);
			if (result == SMSSDK.RESULT_COMPLETE) {
				//����ע��ɹ��󣬷���MainActivity,Ȼ����ʾ�º���
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//�ύ��֤��ɹ�
					final  HashMap<String,Object>  phoneMap = (HashMap<String, Object>) data;
	                new Thread(new Runnable() {
						@Override
						public void run() {
							Utils.httpGet(Utils.URL_PATH+"/user/phonelogin?phone="+(String) phoneMap.get("phone"), mHandler);
						}
					}).start();
	                
//					Toast.makeText(getApplicationContext(), "�ύ��֤��ɹ�", Toast.LENGTH_SHORT).show();
//					textView2.setText("�ύ��֤��ɹ�");
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
					boolean issmart=(Boolean)data;//������֤�� trueΪ������֤��falseΪ��ͨ�·�����
					//������������֤����
					Toast.makeText(getApplicationContext(), "��֤���Ѿ�����", Toast.LENGTH_SHORT).show();
//					textView2.setText("��֤���Ѿ�����");
					Utils.Log("verification VERIFICATION_CODE send ok,issmart:"+issmart);
				}else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//����֧�ַ�����֤��Ĺ����б�
					Toast.makeText(getApplicationContext(), "��ȡ�����б�ɹ�", Toast.LENGTH_SHORT).show();
//					countryTextView.setText(data.toString());
					
				}
			} else {
				((Throwable) data).printStackTrace();
				//#if def{lang} == cn
				// ���ݷ��������ص�������󣬸�toast��ʾ
				//#elif def{lang} == en
				// show toast according to the error code
				//#endif
				try {
				     Throwable throwable = (Throwable) data;
				     throwable.printStackTrace();
				     JSONObject object = new JSONObject(throwable.getMessage());
				     String des = object.optString("detail");//��������
				     int status = object.optInt("status");//�������
				     if (status > 0 && !TextUtils.isEmpty(des)) {
					Toast.makeText(LoginActivity.this, des, Toast.LENGTH_SHORT).show();
					Utils.Log(TAG,"verification error status:"+status+" des:"+des);
					return;
				     }
				} catch (Exception e) {
				     //do something							
				}
			}
			
		}
		
	};
	Handler mHandler= new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);                    
            if(msg.what ==Utils.COUNT_DOWN_MSG) {  
            	int lefttime=msg.arg1;
            	if(msg.arg1>0){
            	send_code.setText(lefttime+"�������·���");
            	}else
            	{
            		send_code.setText("������֤��");
            		send_code.setEnabled(true);
            		send_code.setBackgroundResource(R.drawable.long_button_selector);
            	}
            }else if(msg.what ==Utils.GET_SUCCESS_MSG){
            	JSONObject jsonObject=(JSONObject)msg.obj;
            	//1,������Ҫ����Щ��д��preferrence �������Ľ�����ʾ���á�
            	Utils.Log("login success jsonObject:"+jsonObject);
            	final String avatar=saveAccountImfo(jsonObject);
            	
            	//2,Ȼ����Ҫȥ���ظ�ͷ��
            	SharedPreferences p=Utils.getSharedPpreference(LoginActivity.this);
            	final String avatarPath=p.getString(Utils.SHARE_PREFERENCE_CUP_AVATAR, "");
            	if(!TextUtils.isEmpty(avatar)&&TextUtils.isEmpty(avatarPath)){
            		new Thread(new Runnable() {
						@Override
						public void run() {
							File f=Utils.downLoadFile(avatar,Environment.getExternalStorageDirectory() + "/8CUP/web/","avatar.jpg");
							SharedPreferences.Editor e=Utils.getSharedPpreferenceEdit(LoginActivity.this);
							e.putString(Utils.SHARE_PREFERENCE_CUP_AVATAR, f.getAbsolutePath());
							e.commit();
						}
					}).start();
            	}
            	
            	//3,����Ӧ��
            	Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            	
            	//4�����鶼������֮�� ���Լ�ɾ�ˡ�
            	finish();
            }
        }  	
	};
	private String saveAccountImfo(JSONObject j){
		SharedPreferences p;
		SharedPreferences.Editor e;
		p = getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
		e = p.edit();
		String avatar=null;
		try {
		JSONObject result=new JSONObject(j.toString());
		String phone= result.optString("phone","");
		String birthday =  result.optString("birthday","");
		String nickname =result.optString("nickname","");
		 avatar = result.optString("avatar","");
		String height = result.optString("height","");
		String city = result.optString("city","");
		String accountid = result.optString("accountid","");
		String sex =result.optString("sex","");
		String scene =result.optString("scene","");
		String constitution =result.optString("constitution","");
		String weight =result.optString("weight","");

		e.putString(Utils.SHARE_PREFERENCE_CUP_PHONE, phone);
		e.putString(Utils.SHARE_PREFERENCE_CUP_BIRTHDAY, birthday);
		e.putString(Utils.SHARE_PREFERENCE_CUP_NICKNAME, nickname);
		e.putString(Utils.SHARE_PREFERENCE_CUP_AVATAR_WEB_PATH, avatar);
		e.putString(Utils.SHARE_PREFERENCE_CUP_HEIGHT, height);
		e.putString(Utils.SHARE_PREFERENCE_CUP_CITY, city);
		e.putString(Utils.SHARE_PREFERENCE_CUP_ACCOUNTID, accountid);
		e.putString(Utils.SHARE_PREFERENCE_CUP_SEX, sex);
		e.putString(Utils.SHARE_PREFERENCE_CUP_SCENE, scene);
		e.putString(Utils.SHARE_PREFERENCE_CUP_CONSTITUTION, constitution);
		e.putString(Utils.SHARE_PREFERENCE_CUP_WEIGHT, weight);
		
		e.commit();
		} catch (JSONException e1) {
			Utils.Log(TAG,"saveAccountImfo error :"+e1);
		}
		
		return avatar;
		
	}
	private void countDown(long starttime,Handler h){
		boolean isrun=true;
		try {
			while (isrun) {
				Thread.sleep(100);
				int passtime = (int) (System.currentTimeMillis() - starttime) / 1000;
				int lefttime = repeatSend - passtime;
				Message msg = new Message();
				if (lefttime > 0) {
					msg.arg1 = lefttime;
				} else {
					msg.arg1 = 0;
					isrun=false;
					isCountDown=false;
				}
				msg.what = Utils.COUNT_DOWN_MSG;
				h.sendMessage(msg);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SMSSDK.unregisterAllEventHandler();
	}
}
