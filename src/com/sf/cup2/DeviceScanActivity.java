/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sf.cup2;

import java.util.ArrayList;

import com.sf.cup2.view.CircleWaveView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends Activity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private boolean isFindBtDevices=false;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_SELECT_BT = 2;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    
    LinearLayout bt_device_layout;
    TextView device_status_text;
    TextView device_status_text2;
    CircleWaveView device_circle_wave_view;
    ListView device_list;
    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.scan_bt);
        mHandler = new Handler() {
    	    @Override
    		public void handleMessage(Message msg)
    	    {
    	     switch (msg.what)
    	     {
				case 1:
					if (alertDialog == null) {
						
						LayoutInflater inflater = DeviceScanActivity.this.getLayoutInflater();
						final View layout = inflater.inflate(R.layout.red_title_dialog, null);
						
						TextView title = (TextView)layout.findViewById(R.id.title);
						title.setText("温馨提示");
						TextView summary = (TextView)layout.findViewById(R.id.summary);
						summary.setText(R.string.not_find_device);
						TextView ok = (TextView) layout.findViewById(R.id.ok);
						ok.setText(R.string.retry);
						ok.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								scanLeDevice(true);
								alertDialog.dismiss();
										
							}
						});
						
						TextView cancel = (TextView) layout.findViewById(R.id.cancel);
						cancel.setText("打开应用");
						cancel.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								
/*								Intent intent = new Intent();
								setResult(RESULT_CANCELED, intent);
								alertDialog.dismiss();
								finish();*/
								  Intent intent=new Intent();  
							        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, "");
							        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, "");
							        setResult(RESULT_OK, intent);  
							        finish();
							}
						});
						
/*						cancel.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								  Intent intent=new Intent();  
							        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, "");
							        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, "");
							        setResult(RESULT_OK, intent);  
							        finish();
								return false;
							}
						});*/
							
						AlertDialog.Builder alertBuiler = new AlertDialog.Builder(DeviceScanActivity.this);
						alertDialog = alertBuiler.create();
						alertDialog.setView(layout);
						
						
						
						
//						alertDialog=new AlertDialog.Builder(DeviceScanActivity.this)
//								.setTitle(R.string.tips)
//								.setMessage(R.string.not_find_device)
//								.setCancelable(false)
//								.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog, int which) {
//										scanLeDevice(true);
//									}
//								})
//								.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog, int which) {
//										Intent intent = new Intent();
//										setResult(RESULT_CANCELED, intent);
//										finish();
//									}
//								}).create();
					}
					try {
						if(!isFindBtDevices){
							alertDialog.show();
							
//							alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnLongClickListener(new OnLongClickListener() {
//								@Override
//								public boolean onLongClick(View v) {
//									  Intent intent=new Intent();  
//								        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, "");
//								        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, "");
//								        setResult(RESULT_OK, intent);  
//								        finish();
//									return false;
//								}
//							});
						}
					} catch (Exception e) {
						alertDialog=null;
					}
					//there is a bug  that  the dialog cant dismiss  unless click the button  >>>> fix it  setCancelable(false)
					break;
				}
    	    }
    	  };

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        
        
        bt_device_layout=(LinearLayout)findViewById(R.id.bt_device_layout);
        
        device_status_text=(TextView)findViewById(R.id.device_status_text);
        device_status_text2=(TextView)findViewById(R.id.device_status_text2);
        device_circle_wave_view=(CircleWaveView)findViewById(R.id.device_circle_wave_view);
        
        device_list=(ListView)findViewById(R.id.device_list);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        if (!mScanning) {
//            menu.findItem(R.id.menu_stop).setVisible(false);
//            menu.findItem(R.id.menu_scan).setVisible(true);
//            menu.findItem(R.id.menu_refresh).setActionView(null);
//        } else {
//            menu.findItem(R.id.menu_stop).setVisible(true);
//            menu.findItem(R.id.menu_scan).setVisible(false);
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_indeterminate_progress);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_scan:
//                mLeDeviceListAdapter.clear();
//                scanLeDevice(true);
//                break;
//            case R.id.menu_stop:
//                scanLeDevice(false);
//                break;
//        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }else{
	        scanLeDevice(true);
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        device_list.setAdapter(mLeDeviceListAdapter);
        
        
    	//add for umeng
		MobclickAgent.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        scanLeDevice(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        
        
      //add for umeng
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	//to avoid  android.view.WindowLeaked
    	if(alertDialog!=null){
    		alertDialog.dismiss();
    	}
    	alertDialog=null;
    }

   

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mHandler.sendEmptyMessage(1);
                }
            }, SCAN_PERIOD);
            isFindBtDevices=false;
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
        	String deviceName = device.getName();
            if(!mLeDevices.contains(device)) {
            	if(deviceName.startsWith("BT05"))
            	{
            		mLeDevices.add(device);
            	}
            }
            else{
            	if(!deviceName.startsWith("BT05"))
            	{
            		mLeDevices.remove(device);
            	}
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                //viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
            {
                //viewHolder.deviceName.setText(R.string.unknown_device);
                viewHolder.deviceName.setText(device.getAddress());
            }
            //viewHolder.deviceAddress.setText(device.getAddress());

            
            view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onListItemClick(i);
				}
			});
            return view;
        }
        @Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			device_status_text.setText("发现智能水杯");
			device_status_text2.setVisibility(View.GONE);
			device_circle_wave_view.setVisibility(View.GONE);
			isFindBtDevices=true;
		}
    }
    protected void onListItemClick(int position) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
//        final Intent intent = new Intent(this, DeviceControlActivity.class);
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//        startActivity(intent);

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        Intent intent=new Intent();  
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        setResult(RESULT_OK, intent);  
        finish();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}