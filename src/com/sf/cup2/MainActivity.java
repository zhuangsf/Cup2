package com.sf.cup2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.sf.cup2.guide.GuideView;
import com.sf.cup2.login.LoginActivity;
import com.sf.cup2.utils.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import com.sf.cup2.FragmentHistory;

public class MainActivity extends Activity {
	private final static String TAG = MainActivity.class.getPackage() + "."
			+ MainActivity.class.getSimpleName();
	long lastTime = 0L;
	RadioGroup myTabRg;
	RadioButton myTabRadioButton;
	FragmentHome fHome;
	FragmentData fData;
	FragmentTime fTime;
	FragmentWater fWater;
//	FragmentMe fMe;
	private static final String TAG_HOME="TAG_HOME";
	private static final String TAG_DATA="TAG_DATA";
	private static final String TAG_TIME="TAG_TIME";
	private static final String TAG_WATER="TAG_WATER";
	private static final String TAG_ME="TAG_ME";

	FragmentHomeReset fHome_reset;

	/* 姣忎釜 tab 鐨� item */
	private List<Fragment> mTab = new ArrayList<Fragment>();

	private int[] mRadioButton = {
			R.id.rbTime,
			R.id.rbWater,
			R.id.rbData, 
			R.id.rbHome, 
			};
	private Fragment[] mFragmentArray = { 
			fTime,
			fWater,
			fData,
			fHome, 
			};
	private String[] mFragmentTag = { 
			TAG_TIME,
			TAG_WATER,
			TAG_DATA,
			TAG_HOME, 
			};

	private MediaPlayer mp;  
	
//	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	AlertDialog connectFailAlertDialog;
	AlertDialog timeUpAlertDialog;
	private Handler mHandler= new Handler()
 {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STOP_WAIT_BT:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				if (connectFailAlertDialog == null) {
					connectFailAlertDialog=new AlertDialog.Builder(MainActivity.this)
							.setTitle("温馨提示")
							.setMessage("蓝牙连接失败")
//							.setCancelable(false)
							.setPositiveButton("重连", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									 boolean result= MainActivity.this.reConnect();
									 if(!result){
										 Toast.makeText(MainActivity.this, "无法连接到蓝牙设备", Toast.LENGTH_SHORT).show();
									 }
								}
							})
							.setNegativeButton("取消",null).create();
				}
				try {
					connectFailAlertDialog.show();
				} catch (Exception e) {
					connectFailAlertDialog=null;
				}
				break;
			}
		}
	};
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 20000;
	
    
    

    //BT
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean iServiceBind=true;
    private static ProgressDialog progressDialog;// 等待进度圈
    private static final int MSG_STOP_WAIT_BT=2;
 // Stops waiting after 6 seconds.
    private static final long WAIT_PERIOD = 10000;//6000; //too short too connect
    
    //byte[] WriteBytes = null;
    byte[] WriteBytes = new byte[20];
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Utils.Log("Unable to initialize Bluetooth");
                finish();
            }
            
            boolean result=reConnect();
    		Utils.Log("onServiceConnected reConnect result=" + result);
            // Automatically connects to the device upon successful start-up initialization.
          /* boolean result =  mBluetoothLeService.connect(mDeviceAddress);
            
            if(result==true&&(progressDialog==null||!progressDialog.isShowing())){
				progressDialog = ProgressDialog.show(MainActivity.this, null, "等待蓝牙连接，请稍候...");
				// Stops sending after a pre-defined period.
				Message msg = new Message();
				msg.what = MSG_STOP_WAIT_BT;
				mHandler.sendMessageDelayed(msg, WAIT_PERIOD);
				Utils.Log(" mHandler.sendMessageDelayed="+mHandler.toString());
			}
			*/
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Utils.Log("xxxxxxxxxxxxxxxxxx BroadcastReceiver ACTION_GATT_CONNECTED mConnected:"+mConnected);
                if(connectFailAlertDialog!=null&&connectFailAlertDialog.isShowing()){
                	connectFailAlertDialog.dismiss();
                }
                if(progressDialog!=null&&progressDialog.isShowing()){
   					progressDialog.dismiss();
   					mHandler.removeMessages(MSG_STOP_WAIT_BT);//connect success remove the hint
   		        }
                Toast.makeText(MainActivity.this, "蓝牙水杯已连接", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Utils.Log("xxxxxxxxxxxxxxxxxx BroadcastReceiver ACTION_GATT_DISCONNECTED mConnected:"+mConnected);
                Toast.makeText(MainActivity.this, "蓝牙水杯已断开", Toast.LENGTH_SHORT).show();
                if(progressDialog!=null&&progressDialog.isShowing()){
           					progressDialog.dismiss();
           		}
              //  boolean result= MainActivity.this.reConnect();
				// if(!result){
			//		 Toast.makeText(MainActivity.this, "无法连接到蓝牙设备", Toast.LENGTH_SHORT).show();
			//	 }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                
                //sentMsgToBt("test","test","test");
                
                Utils.Log("ACTION_GATT_SERVICES_DISCOVERED");
                
                BluetoothGattService gattService=mBluetoothLeService.getGattService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
				BluetoothGattCharacteristic characteristic =gattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));   
				mBluetoothLeService.readCharacteristic(characteristic);

                // after discovered  set
                //service 0000ffe0-0000-1000-8000-00805f9b34fb
                //characteristic 0000ffe4-0000-1000-8000-00805f9b34fb
                
                /*     这里是上个项目的wuyx,可能uuid不对应,所以导致了会报错
                BluetoothGattService gattService=mBluetoothLeService.getGattService(UUID.fromString(Utils.BT_GET_SERVICE_UUID));
				BluetoothGattCharacteristic characteristic =gattService.getCharacteristic(UUID.fromString(Utils.BT_GET_CHARACTERISTIC_UUID));
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                Utils.Log("xxxxxxxxxxxxxxxxxx BroadcastReceiver ACTION_GATT_SERVICES_DISCOVERED");
                sentAskTemperature();//ask the temperature after bt discovered        may before notification?????
                if(progressDialog!=null&&progressDialog.isShowing()){
  					progressDialog.dismiss();
  					Utils.Log(" mHandler.removeMessages="+mHandler.hasMessages(MSG_STOP_WAIT_BT));
  					mHandler.removeMessages(MSG_STOP_WAIT_BT);//connect success remove the hint
  				}*/
            	try {
                //TODO  if receiver ACTION_GATT_SERVICES_DISCOVERED not my btdevice. i dont want to handle it now.
            	} catch (Exception e) {
            		Utils.Log(TAG,"ACTION_GATT_SERVICES_DISCOVERED   e="+e.toString());
            	}
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            	try {
	                Utils.Log("this is respone what i need:"+intent.getStringExtra(BluetoothLeService.EXTRA_DATA_NEED));
	                Utils.Log("ACTION_DATA_AVAILABLE :"+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
	                String responeString=intent.getStringExtra(BluetoothLeService.EXTRA_DATA_NEED);
	                String[] responeStringArray=responeString.split(" ");
	                if("02".equals(responeStringArray[1])){
	                }else if("88".equals(responeStringArray[1])){
	                }
	                //TODO  if receiver FF means the cup is out of power. but i dont want to handle it now.
            	} catch (Exception e) {
            	}
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            
            Utils.Log("displayGattServices start -------------------------");
            Utils.Log("displayGattServices uuid  "+uuid);
            Utils.Log("displayGattServices currentServiceData  "+currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                
                Utils.Log("displayGattServices uuid  "+uuid);
                Utils.Log("displayGattServices currentCharaData  "+currentCharaData);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            Utils.Log("displayGattServices end -------------------------");
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
    }
    
    
    public static String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }
	
    public void sentSetTemperature(int temperature){
    	int t=temperature*10;
    	String tHex=Integer.toHexString(t);
    	if(tHex.length()==1){
    		tHex="000"+tHex;	
    	}else if(tHex.length()==2){
    		tHex="00"+tHex;	
    	}else if(tHex.length()==3){
    		tHex="0"+tHex;	
    	}
    	sentMsgToBt("01",tHex.substring(2, 4),tHex.substring(0, 2));
    }
    public void sentAskTemperature(){
    	sentMsgToBt("02","00","00");
    }
    public void sentSetTime(int time){
    	String tHex=Integer.toHexString(time);
    	if(tHex.length()==1){
    		tHex="000"+tHex;	
    	}else if(tHex.length()==2){
    		tHex="00"+tHex;	
    	}else if(tHex.length()==3){
    		tHex="0"+tHex;	
    	}
    	sentMsgToBt("03",tHex.substring(2, 4),tHex.substring(0, 2));
    }
    
	private void sentMsgToBt(String action, String arg1, String arg2) {
		
		java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();  
		StackTraceElement[] ste = ts.get(Thread.currentThread());  
		Log.e("jockeyTrack", "--------------------start-------------------------"); 
		for (StackTraceElement s : ste) {  
		Log.e("jockeyTrack", s.toString()); 
		}  
		Log.e("jockeyTrack", "--------------------end-------------------------"); 
		
		
		try {
			if (mBluetoothLeService == null) {
				return ;
			}
			StringBuffer sb = new StringBuffer("");
			String sum = Integer.toHexString(Integer.parseInt(action, 16) + Integer.parseInt(arg1, 16)
					+ Integer.parseInt(arg2, 16) + Integer.parseInt("00", 16) + Integer.parseInt("00", 16));
			if(sum.length()==1){
				sum="0"+sum;	
	    	}
			sb.append("55") // head
			  .append("00") // action 01:set temp , 02:ask temp ,03 :set time
			  .append("01")
			  .append("00")
			  .append("00")
			  .append("00")
			  .append("00")
			  .append("00")
			  .append("01")
			  .append("aa");
			Utils.Log("xxxxxxxxxxxxxxxxxx sentMsgToBt:" + sb);
			BluetoothGattService gattService = mBluetoothLeService
					.getGattService(UUID.fromString(Utils.BT_SEND_SERVICE_UUID));
			BluetoothGattCharacteristic characteristic = gattService
					.getCharacteristic(UUID.fromString(Utils.BT_SEND_CHARACTERISTIC_UUID));
			byte[] value = new byte[20];
			value[0] = (byte) 0x00;
			characteristic.setValue(value[0], BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			characteristic.setValue(hex2byte(sb.toString().getBytes())); 
			mBluetoothLeService.writeCharacteristic(characteristic);
		} catch (Exception e) {
			Utils.Log("xxxxxxxxxxxxxxxxxx sentMsgToBt error:" + e);
		}
	}
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		// 1,is first open app start guide
		SharedPreferences p = Utils.getSharedPpreference(this);
		SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(this);
		int isFirst = p.getInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, 0);
		if (isFirst == 0) {
			Intent i = new Intent(this, GuideView.class);
			startActivity(i);
			finish();
			onDestroy();
			return;
		}
		// 2,login first
		/*     先关闭注册  wuyx
		String phonenum = p.getString(Utils.SHARE_PREFERENCE_CUP_PHONE, null);
		if (TextUtils.isEmpty(phonenum)) {
			Intent i = new Intent(this, LoginActivity.class);
			startActivity(i);
			finish();
			onDestroy();
			return;
		}
		*/
		// TODO 3,must connect bt                     get info from preference   try to connect bt direct. if can not connect bt  try to scan  #########################################
		if (false) {
		    Intent i = new Intent(this, DeviceScanActivity.class);
			startActivityForResult(i, DeviceScanActivity.REQUEST_SELECT_BT);
//			finish();
//			onDestroy();
//			return;
		}
		if (isFirst > 0) {
			e.putInt(Utils.SHARE_PREFERENCE_CUP_OPEN_COUNTS, isFirst + 1);
			e.commit();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		createFragment();
		initView();
		// !!!!!!! 处理屏幕旋转生成多个fragment问题。
		// !!!!!!! 如果用replace是要加这个，判断是否是activity重建。这里是由于在RadioGroup
		// 的状态改变，默认为第一个页签时没进onCheckedChanged，不为第一个页签 都会进onCheckedChanged切换下页签
		// if (savedInstanceState == null)
		{

			Utils.Log("xxxxxxxxxxxxxxxxxx onCreate home:" + fWater);
			getFragmentManager().beginTransaction().show(fData).commit();
		}

		// if from alarm show the dialog
		startFromAlarm(getIntent());

	
		//   喝水量数据测试
	/*
		DBAdapter db = new DBAdapter(this);
		db.open();
		long id;
		id = db.insertWaterData(
		"2015-01-01",
		"07:00",
		"500");
		id = db.insertWaterData(
		"2015-01-01",
		"08:00",
		"50");
		id = db.insertWaterData(
		"2015-01-01",
		"09:00",
		"450");
		id = db.insertWaterData(
		"2015-01-01",
		"10:30",
		"300");
		id = db.insertWaterData(
		"2016-08-03",
		"11:06",
		"100");
		id = db.insertWaterData(
		"2015-01-01",
		"13:00",
		"200");
		id = db.insertWaterData(
		"2015-01-01",
		"16:00",
		"50");
		id = db.insertWaterData(
		"2015-01-01",
		"18:00",
		"150");
		id = db.insertWaterData(
		"2015-01-01",
		"20:30",
		"300");
		id = db.insertWaterData(
		"2015-01-01",
		"23:06",
		"100");
		db.dumpData();
		db.close();
*/
		
		
		
		//TODO  开发阶段先关闭了，发布测试再打开
		//add bugly
		//CrashReport.initCrashReport(getApplicationContext(), "3aad7025f9", false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		
	//	 boolean result=reConnect();    先注释掉,等硬件来了再打开
 	//	Utils.Log("onResume reConnect result=" + result);
		/*
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Utils.Log("onResume Connect request result=" + result);
			if(result==true&&(progressDialog==null||!progressDialog.isShowing())){
				progressDialog = ProgressDialog.show(this, null, "等待蓝牙连接，请稍候...");
				// Stops sending after a pre-defined period.
				Message msg = new Message();
				msg.what = MSG_STOP_WAIT_BT;
				mHandler.sendMessageDelayed(msg, WAIT_PERIOD);
				Utils.Log(" mHandler.sendMessageDelayed="+mHandler.toString());
			}
		}*/
		
		
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == DeviceScanActivity.REQUEST_SELECT_BT && resultCode == Activity.RESULT_OK) {
    		mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
    		mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);
    		//one of this can be used
    		if(TextUtils.isEmpty(mDeviceName)&&TextUtils.isEmpty(mDeviceAddress)){
    			Toast.makeText(this, "未能找到对应蓝牙设备", Toast.LENGTH_SHORT).show();
    			return ;
    		}
    		//TODO it must be save  every time open activity try to connect bt auto.after it can not connect  it must rescan the bt

    		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
    		iServiceBind=bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    		
    		
    		boolean result=reConnect();
    		Utils.Log("onActivityResult reConnect request result=" + result);
    		/*if (mBluetoothLeService != null) {
    			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
    			Utils.Log("onActivityResult Connect request result=" + result);
    			if(result==true&&(progressDialog==null||!progressDialog.isShowing())){
    				progressDialog = ProgressDialog.show(this, null, "等待蓝牙连接，请稍候...");
    				// Stops sending after a pre-defined period.
    				Message msg = new Message();
    				msg.what = MSG_STOP_WAIT_BT;
    				mHandler.sendMessageDelayed(msg, WAIT_PERIOD);
    				Utils.Log(" mHandler.sendMessageDelayed="+mHandler.toString());
    			}
    		}*/
        }else if(requestCode == DeviceScanActivity.REQUEST_SELECT_BT && resultCode == Activity.RESULT_CANCELED){
        	Toast.makeText(this, "未能找到对应蓝牙设备", Toast.LENGTH_SHORT).show();
        	finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    
    public boolean reConnect(){
    	boolean result=false;
    	if (mBluetoothLeService != null) {
    		result = mBluetoothLeService.connect(mDeviceAddress);
			Utils.Log("reConnect result=" + result);
			  if(result==true&&(progressDialog==null||!progressDialog.isShowing())){
					progressDialog = ProgressDialog.show(MainActivity.this, null, "等待蓝牙连接，请稍候...");
					// Stops sending after a pre-defined period.
					Message msg = new Message();
					msg.what = MSG_STOP_WAIT_BT;
					mHandler.sendMessageDelayed(msg, WAIT_PERIOD);
					Utils.Log(" mHandler.sendMessageDelayed="+mHandler.toString());
				}
		}
    	return result;
    }
    
    
    
    
    
    
    
    
    
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
        if(iServiceBind){
        	unbindService(mServiceConnection);
        }
        }catch (Exception e)
        {
        	// if there is no bind this service  close this activity it will show error:service not registered.use iServiceBind to avoid this error
        }
        mBluetoothLeService = null;
        if(progressDialog!=null){
        	progressDialog.dismiss();
    	}
        progressDialog=null;
        if(connectFailAlertDialog!=null){
        	connectFailAlertDialog.dismiss();
    	}
        connectFailAlertDialog=null;
      //to avoid  android.view.WindowLeaked
    	if(timeUpAlertDialog!=null){
    		timeUpAlertDialog.dismiss();
    	}
    	timeUpAlertDialog=null;
    }
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
	
	
	private void createFragment() {
		FragmentManager fm=getFragmentManager();
		FragmentTransaction ft= fm.beginTransaction();
		Fragment fragment =null;
		
		
		fragment= fm.findFragmentByTag(TAG_TIME);
		if (fragment != null) {// 如果有，则使用，处理翻转之后状态未保存问题
			fTime = (FragmentTime) fragment;
		} else {// 如果为空，才去新建，不新建切换的时候就可以保存状态了。
			fTime = FragmentTime.newInstance(null);
			ft.add(R.id.fragmentfield, fTime, TAG_TIME);
		}
		ft.hide(fTime);
		mTab.add(fTime);
		

		fragment= fm.findFragmentByTag(TAG_WATER);
		if (fragment != null) {// 如果有，则使用，处理翻转之后状态未保存问题
			fWater = (FragmentWater) fragment;
		} else {// 如果为空，才去新建，不新建切换的时候就可以保存状态了。
			fWater = FragmentWater.newInstance(null);
			ft.add(R.id.fragmentfield, fWater, TAG_WATER);
		}
		ft.hide(fWater);
		mTab.add(fWater);
	
		

		
		
		fragment= fm.findFragmentByTag(TAG_DATA);
		if (fragment != null) {// 如果有，则使用，处理翻转之后状态未保存问题
			fData = (FragmentData) fragment;
		} else {// 如果为空，才去新建，不新建切换的时候就可以保存状态了。
			fData = FragmentData.newInstance(null);
			ft.add(R.id.fragmentfield, fData, TAG_DATA);
		}
		ft.hide(fData);
		mTab.add(fData);		
		
		
		fragment= fm.findFragmentByTag(TAG_HOME);
		if (fragment != null) {// 如果有，则使用，处理翻转之后状态未保存问题
			fHome = (FragmentHome) fragment;
		} else {// 如果为空，才去新建，不新建切换的时候就可以保存状态了。
			fHome = FragmentHome.newInstance(null);
			ft.add(R.id.fragmentfield, fHome, TAG_HOME);
		}
		ft.hide(fHome);
		mTab.add(fHome);
		
		
		//处理其他fragment
		fragment=fm.findFragmentById(R.id.fragmentfield);
		if(mTab!=null&&fragment!=null&&!mTab.contains(fragment) && fragment.isAdded()){
			ft.remove(fragment);
			int count=fm.getBackStackEntryCount();
			Utils.Log("fragment backstack count:"+count);
			for(int i=0;i<count;i++){
			fm.popBackStack();//切换也签，处理掉已有的backstack
			}
		}
		
		ft.commit();
		
	}
	
	
	private void initView() {
		myTabRg = (RadioGroup) findViewById(R.id.tab_menu);
		myTabRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				createFragment();
				for (int i = 0; i < mRadioButton.length; i++) {
					if (mRadioButton[i] == checkedId) {
						FragmentManager fm=getFragmentManager();
						Utils.Log("xxxxxxxxxxxxxxxxxx i:"+i);
						FragmentTransaction ft = fm.beginTransaction();
						// ft.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_left_out);
						ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//						ft.replace(R.id.fragmentfield, mTab.get(i),mFragmentTag[i]);
						ft.show( mTab.get(i));
//						 ft.addToBackStack(null);
						ft.commit();
						break;
					}
				}
			}
		});
	}
	
	
	
	
//	onNewIntent(Intent intent)方法里调用setIntent(intent)设置这个传来的最新的intent.
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Utils.Log("xxxxxxxxx onNewIntent"+"intent:"+intent+",getIntent:"+getIntent());
		setIntent(intent);
		startFromAlarm(intent);
	}
	private void showNotification(Context context, String title,String content, int notifyId) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Utils.IS_FROM_ALARM, false);
		intent.putExtra(Utils.FROM_ALARM_INDEX, -1);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notify = new Notification();
		long vibrate[] = { 500, 500 };
		Notification.Builder builder = new Notification.Builder(context);
		notify = builder.setContentIntent(pi).setContentTitle(title)
				.setContentText(content).setTicker(content)
				.setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_ALL)
				.setLights(0xffffffff, 300, 600).setVibrate(vibrate).build();
		notify.flags |= Notification.FLAG_SHOW_LIGHTS
				| Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notifyId, notify);
	}
	private void startFromAlarm(Intent intent) {
		boolean isAlarm = intent.getBooleanExtra(Utils.IS_FROM_ALARM, false);
		int index = intent.getIntExtra(Utils.FROM_ALARM_INDEX, -1);
		Utils.Log("isAlarm:" + isAlarm + " index:" + index+",intent:"+intent);
		if (isAlarm) {
			setIntent(null);//it must set intent null. single mainactivity's IS_FROM_ALARM always true.i dont know why
			// 如果是闹钟响起跳转来的，播个音乐
			// 初始化音乐资源
			try {
				showNotification(this,"喝水提醒","喝水时间到啦"+" 来自闹钟"+index,3344);
				timeUpAlertDialog=new AlertDialog.Builder(this).setMessage("亲！已到设定饮水时间咯！\n请及时享用哦 来自闹钟"+index).setTitle("温馨提示")
						.setPositiveButton("确定", null).create();
				timeUpAlertDialog.show();
				if(fTime != null)
				{
					fTime.setNextAlarm();
				}
/*
				// 创建MediaPlayer对象
				mp = new MediaPlayer();
				// 将音乐保存在res/raw/xingshu.mp3,R.java中自动生成{public static final int
				// xingshu=0x7f040000;}
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				// mp = MediaPlayer.create(this, notification);
				mp.setDataSource(this, notification);
				// 在MediaPlayer取得播放资源与stop()之后要准备PlayBack的状态前一定要使用MediaPlayer.prepeare()
				mp.prepare();
				// 开始播放音乐
//				mp.start();
				// 音乐播放完毕的事件处理
				mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						// 循环播放
						try {
							// mp.start();
							if(mp!=null){
								if(mp.isPlaying())
						            mp.stop();
						        mp.reset();
						        mp.release();
						        mp=null;
							}
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
					}
				});
				// 播放音乐时发生错误的事件处理
				mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
					public boolean onError(MediaPlayer mp, int what, int extra) {
						// 释放资源
						try {
							if(mp!=null){
								if(mp.isPlaying())
						            mp.stop();
						        mp.reset();
						        mp.release();
						        mp=null;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return false;
					}
				});
				*/
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	
	
	
	@Override
	public void onBackPressed()
	{
	    int count=getFragmentManager().getBackStackEntryCount();
		if(count==0){
			long l = System.currentTimeMillis();
	    if (l - lastTime > 2000L)
	    {
	      lastTime = l;
	      Toast.makeText(this, "再按一次返回，退出应用.", 0).show();
	      return;
	    }
		}
//		CrashReport.testJavaCrash();
		super.onBackPressed();
	    
	  }

}
