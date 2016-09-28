package com.sf.cup2.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class Utils {
	private final static boolean isDebug = true;
	private final static String TAG = Utils.class.getPackage() + "."
			+ Utils.class.getSimpleName();
	public final static String APK_NAME_SUFFIX = "apk";
	public final static String SEPARATOR_DOT = ".";
	public final static String SEPARATOR_SLASH = "/";
	public final static String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	public final static String URL_PATH="http://121.199.75.79:8280";
	public static final int TARGET_ACTIVITY = 0;
	public static final int TARGET_SERVICE = 1;
	public static final String FROM = "extra.from";
	public static final int FROM_EXTERNAL = 0;
	public static final int FROM_INTERNAL = 1;
	public static final String SMS_APP_KEY="c104bd01f0ba";
	public static final String SMS_APP_SECRET="35cca6958f0f1192aac5ddf7c4bebab9";
	
	public static final String SHARE_PREFERENCE_CUP_OPEN_COUNTS="OPEN_COUNTS";
	
	//user
	public static final String SHARE_PREFERENCE_CUP="CUP";
	public static final String SHARE_PREFERENCE_CUP_PHONE="PHONE";
	public static final String SHARE_PREFERENCE_CUP_BIRTHDAY="BIRTHDAY";
	public static final String SHARE_PREFERENCE_CUP_NICKNAME="NICKNAME";
	public static final String SHARE_PREFERENCE_CUP_HEIGHT="HEIGHT";
	public static final String SHARE_PREFERENCE_CUP_CITY="CITY";
	public static final String SHARE_PREFERENCE_CUP_ACCOUNTID="ACCOUNTID";
	public static final String SHARE_PREFERENCE_CUP_SEX="SEX";
	public static final String SHARE_PREFERENCE_CUP_SCENE="SCENE";
	public static final String SHARE_PREFERENCE_CUP_CONSTITUTION="CONSTITUTION";
	public static final String SHARE_PREFERENCE_CUP_WEIGHT="WEIGHT";
	public static final String SHARE_PREFERENCE_CUP_AVATAR="AVATAR";
	public static final String SHARE_PREFERENCE_CUP_PLAN="PLAN";
	/**
	 * upload to server  when avatar modify
	 * @deprecated
	 */
	@Deprecated
	public static final String SHARE_PREFERENCE_CUP_AVATAR_IS_MODIFY="AVATAR_IS_MODIFY"; //true:after modify and wont upload 
	public static final String SHARE_PREFERENCE_CUP_AVATAR_WEB_PATH="AVATAR_WEB_PATH";//for web    fix the server  
	
	//water
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_ENABLE="TEMPERATURE_MODE_ENABLE";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE="TEMPERATURE_MODE";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO1="TEMPERATURE_MODE_INFO1";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE1="TEMPERATURE_MODE_VALUE1";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO2="TEMPERATURE_MODE_INFO2";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE2="TEMPERATURE_MODE_VALUE2";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO3="TEMPERATURE_MODE_INFO3";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE3="TEMPERATURE_MODE_VALUE3";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO4="TEMPERATURE_MODE_INFO4";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE4="TEMPERATURE_MODE_VALUE4";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO5="TEMPERATURE_MODE_INFO5";
	public static final String SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE5="TEMPERATURE_MODE_VALUE5";
	
	public static final String[] SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO={
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO1,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO2,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO3,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO4,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_INFO5
	};
	public static final String[] SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE={
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE1,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE2,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE3,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE4,
			SHARE_PREFERENCE_CUP_TEMPERATURE_MODE_VALUE5
	};
	
	public static final String[] SHARE_PREFERENCE_CUP_PERSON_1={
			SHARE_PREFERENCE_CUP_NICKNAME,
			SHARE_PREFERENCE_CUP_SEX,
			SHARE_PREFERENCE_CUP_PHONE
	};
	
	public static final String[] SHARE_PREFERENCE_CUP_PERSON_2={
			SHARE_PREFERENCE_CUP_SCENE,
			SHARE_PREFERENCE_CUP_CONSTITUTION,
			SHARE_PREFERENCE_CUP_HEIGHT,
			SHARE_PREFERENCE_CUP_WEIGHT,
			SHARE_PREFERENCE_CUP_BIRTHDAY
	};
	
	//time
	public static String IS_FROM_ALARM="IS_FROM_ALARM";
	public static String FROM_ALARM_INDEX="FROM_ALARM_INDEX";
	public static final String SHARE_PREFERENCE_CUP_ALARM_IS_ON="ALARMON";
	public static final String SHARE_PREFERENCE_CUP_ALARM_TIME="ALARMTIME";
	public static final String SHARE_PREFERENCE_CUP_ALARM_ENABLE="ALARM_ENABLE";
	public static final String SHARE_PREFERENCE_CUP_ALARM_VISIBILE="ALARM_VISIBILE";
	//bluetooth
	public static final String BT_GET_SERVICE_UUID="0000ffe0-0000-1000-8000-00805f9b34fb";
	public static final String BT_GET_CHARACTERISTIC_UUID="0000ffe1-0000-1000-8000-00805f9b34fb";
	public static final String BT_SEND_SERVICE_UUID="0000ffe0-0000-1000-8000-00805f9b34fb";
	public static final String BT_SEND_CHARACTERISTIC_UUID="0000ffe1-0000-1000-8000-00805f9b34fb";
	
	//msg define
	public static final int COUNT_DOWN_MSG=0x8001; //login count down msg
	public static final int GET_SUCCESS_MSG=0x8002; //get success msg
	public static final int POST_SUCCESS_MSG=0x8003; //post success msg
	public static final int PUT_SUCCESS_MSG=0x8004; //put success msg
	public static final int UPLOAD_SUCCESS_MSG=0x8005; //put success msg
	/**
	 * running log
	 * 
	 * @param s
	 */
	public static void Log(String s) {
		if (isDebug) {
			Log.i("jockey", s);
		}
	}

	/**
	 * error log
	 * 
	 * @param tag
	 * @param s
	 */
	public static void Log(String tag, String s) {
		Log.d(tag, s);
	}


	/**
	 * 0-1
	 * @return
	 */
	 public static final float frand() {
	        return (float) Math.random();
	    }
	  /**
	   * 
	   * @param n
	   * @return  random num  0-n
	   */
	  public static final int irand(int n){
		  return new Random().nextInt(n);
	  }
	  
	  /**
	   * 
	   * @param n
	   * @return random num m-n
	   */
	  public static final int irand(int m,int n){
		  return new Random().nextInt(n)+m;
	  }
	
	  @SuppressWarnings("deprecation")
	public static void httpGet(String url,Handler mHandler) {
		Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpGet url:"+url);
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
//					JSONArray jsonArray= new JSONArray(result);
					JSONObject jsonObject=new JSONObject(result);
					if (mHandler != null) {
					Message msg=new Message();
					msg.what=GET_SUCCESS_MSG;
					msg.arg1=1;
					msg.obj=jsonObject;
//					mHandler.sendEmptyMessage(1);
					mHandler.sendMessage(msg);
					}
					Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpGet finish output 2"+jsonObject);
				}
			} catch (Exception e) {
				Utils.Log(TAG, "httpGet error:" + e);
			}
		}
	 
	/**
	 * Post
	 * 
	 * @param url
	 * @param paramList
	 */
	@SuppressWarnings("deprecation")
	public static void httpPost(String url, JSONObject jsonObj,Handler mHandler) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		long timestamp = System.currentTimeMillis();
		try {
			// 设置参数
//			httpPost.setEntity(new StringEntity(DesEncrypt.encrypt(jsonObj.toString(), DesEncrypt.KEY), HTTP.UTF_8));
			httpPost.setEntity(new StringEntity(jsonObj.toString(), HTTP.UTF_8));
			httpPost.addHeader("content-type", "application/json");
			httpPost.addHeader("authorization","Basic "+Base64.encodeToString((jsonObj.getString("phone")+":phone").toString().getBytes(),Base64.NO_WRAP));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				Utils.Log(" httpPost status " + httpResponse.getStatusLine());
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPost start output ");
				String entitySrc = EntityUtils.toString(entity, "UTF-8");
				JSONObject jsonObject=new JSONObject(entitySrc);
				if (mHandler != null) {
				Message msg=new Message();
				msg.what=POST_SUCCESS_MSG;
				msg.arg1=1;
				msg.obj=jsonObject;
//				mHandler.sendEmptyMessage(1);
				mHandler.sendMessage(msg);
				}
				Utils.Log("entitySrc  " + entitySrc);
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPost finish output ");
			}
		} catch (ConnectTimeoutException e) {
			Utils.Log(TAG, "httpPost time out error:" + e);
		} catch (Exception e) {
			Utils.Log(TAG, "httpPost error:" + e);
		}
		Utils.Log("httpPost spend time:"
				+ (System.currentTimeMillis() - timestamp) + "ms");
	}
	 
	/**
	 * Put
	 * 
	 * @param url
	 * @param paramList
	 */
	@SuppressWarnings("deprecation")
	public static void httpPut(String url, JSONObject jsonObj,Handler mHandler) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url);
		long timestamp = System.currentTimeMillis();
		try {
			// 设置参数
//			httpPost.setEntity(new StringEntity(DesEncrypt.encrypt(jsonObj.toString(), DesEncrypt.KEY), HTTP.UTF_8));
			httpPut.setEntity(new StringEntity(jsonObj.toString(), HTTP.UTF_8));
//			httpPut.setHeader("accountid",accountid);
			httpPut.addHeader("content-type", "application/json");
			httpPut.addHeader("authorization","Basic "+Base64.encodeToString((jsonObj.getString("phone")+":phone").toString().getBytes(),Base64.NO_WRAP));
			HttpResponse httpResponse = httpClient.execute(httpPut);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				Utils.Log(" httpPut status " + httpResponse.getStatusLine());
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPut start output ");
				String entitySrc = EntityUtils.toString(entity, "UTF-8");
				JSONObject jsonObject=new JSONObject(entitySrc);
				if (mHandler != null) {
				Message msg=new Message();
				msg.what=PUT_SUCCESS_MSG;
				msg.arg1=1;
				msg.obj=jsonObject;
//				mHandler.sendEmptyMessage(1);
				mHandler.sendMessage(msg);
				}
				Utils.Log("entitySrc  " + entitySrc);
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPut finish output ");
			}
		} catch (ConnectTimeoutException e) {
			Utils.Log(TAG, "httpPut time out error:" + e);
		} catch (Exception e) {
			Utils.Log(TAG, "httpPut error:" + e);
		}
		Utils.Log("httpPut spend time:"
				+ (System.currentTimeMillis() - timestamp) + "ms");
	}
	 
	
	/**
	 * httpPostFile
	 * 
	 * @param url
	 * @param paramList
	 */
	@SuppressWarnings("deprecation")
	public static void httpPostFile(String url, String filePath,Handler mHandler,String accountid,String phone) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		long timestamp = System.currentTimeMillis();
		try {
			Utils.Log(" httpPostFile filePath " + filePath);
			// 设置参数
			MultipartEntity me=new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			me.addPart("file", new FileBody(new File(filePath)));
			
			httpPost.setEntity(me);
//			httpPost.addHeader("content-type","multipart/form-data" );
			httpPost.addHeader("accountid",accountid );
			httpPost.addHeader("authorization","Basic "+Base64.encodeToString((phone+":phone").toString().getBytes(),Base64.NO_WRAP));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPostFile httpPost me length:"+ me.getContentLength());
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				Utils.Log(" httpPostFile status " + httpResponse.getStatusLine());
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPostFile start output ");
				String entitySrc = EntityUtils.toString(entity, "UTF-8");
				JSONObject jsonObject=new JSONObject(entitySrc);
				if (mHandler != null) {
				Message msg=new Message();
				msg.what=UPLOAD_SUCCESS_MSG;
				msg.arg1=1;
				msg.obj=jsonObject;
//				mHandler.sendEmptyMessage(1);
				mHandler.sendMessage(msg);
				}
				Utils.Log("entitySrc  " + entitySrc);
				Utils.Log(" xxxxxxxxxxxxxxxxxxxxx http httpPostFile finish output ");
			}
		} catch (ConnectTimeoutException e) {
			Utils.Log(TAG, "httpPostFile time out error:" + e);
		} catch (Exception e) {
			Utils.Log(TAG, "httpPostFile error:" + e);
		}
		Utils.Log("httpPostFile spend time:"
				+ (System.currentTimeMillis() - timestamp) + "ms");
	}
	
	public static void httpUpload(String url, String  fileUrlString,Handler mHandler) {
		Map<String, String> textParams = new HashMap<String, String>();
		Map<String, File> fileparams = new HashMap<String, File>();
		long timestamp = System.currentTimeMillis();
		Utils.Log("httpUpload start url:"+url+",file:"+fileUrlString);
		try {
			// 创建一个URL对象
			URL u = new URL(url);
			textParams = new HashMap<String, String>();
			fileparams = new HashMap<String, File>();
			// 要上传的图片文件
			File file = new File(fileUrlString);
			fileparams.put("image", file);
			// 利用HttpURLConnection对象从网络中获取网页数据
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			// 设置连接超时（记得设置连接超时,如果网络不好,Android系统在超过默认时间会收回资源中断操作）
			conn.setConnectTimeout(5000);
			// 设置允许输出（发送POST请求必须设置允许输出）
			conn.setDoOutput(true);
			// 设置使用POST的方式发送
			conn.setRequestMethod("POST");
			// 设置不使用缓存（容易出现问题）
			conn.setUseCaches(false);
			conn.setRequestProperty("Charset", "UTF-8");//设置编码   
			// 在开始用HttpURLConnection对象的setRequestProperty()设置,就是生成HTML文件头
			conn.setRequestProperty("ser-Agent", "Fiddler");
			// 设置contentType
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + NetUtil.BOUNDARY);
			OutputStream os = conn.getOutputStream();
			DataOutputStream ds = new DataOutputStream(os);
			NetUtil.writeStringParams(textParams, ds);
			NetUtil.writeFileParams(fileparams, ds);
			NetUtil.paramsEnd(ds);
			// 对文件流操作完,要记得及时关闭
			os.close();
			// 服务器返回的响应吗
			int code = conn.getResponseCode(); // 从Internet获取网页,发送请求,将网页以流的形式读回来
			Utils.Log(" httpUpload status " +code);
			// 对响应码进行判断
			if (code == 200) {// 返回的响应码200,是成功
				// 得到网络返回的输入流
				InputStream is = conn.getInputStream();
				if (mHandler != null) {
				Message msg=new Message();
				msg.what=UPLOAD_SUCCESS_MSG;
				msg.arg1=1;
				msg.obj=NetUtil.readString(is);
//				mHandler.sendEmptyMessage(1);
				mHandler.sendMessage(msg);
				}
			} else {
//				Toast.makeText(mContext, "请求URL失败！", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.Log(TAG,"httpUpload error:"+e);
		}
		Utils.Log("httpUpload spend time:"
				+ (System.currentTimeMillis() - timestamp) + "ms");
	}
	
	
	
	
	/**
	 * 根据Key获取值.
	 * 
	 * @return 如果key不存在, 并且如果def不为空则返回def否则返回空字符串
	 * @throws IllegalArgumentException
	 *             如果key超过32个字符则抛出该异常
	 */
	public static String getSystemProperties(Context context, String key,
			String def) throws IllegalArgumentException {
		String ret = def;
		try {
			ClassLoader cl = context.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class SystemProperties = cl
					.loadClass("android.os.SystemProperties");
			// 参数类型
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[2];
			paramTypes[0] = String.class;
			paramTypes[1] = String.class;
			@SuppressWarnings("unchecked")
			Method get = SystemProperties.getMethod("get", paramTypes);
			// 参数
			Object[] params = new Object[2];
			params[0] = new String(key);
			params[1] = new String(def);
			ret = (String) get.invoke(SystemProperties, params);
		} catch (IllegalArgumentException iAE) {
			Utils.Log(TAG, "IllegalArgumentException:" + iAE);
		} catch (Exception e) {
			ret = def;
			Utils.Log(TAG, "Exception:" + e);
		}
		return ret;
	}
	
	
	/**
	 * 获取连接方式
	 * none:-1 mobile:0 wifi:1    
	 * @param context
	 * @return
	 */
	public static int getNetWorkType(Context context) {  
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();  
        if (networkInfo != null && networkInfo.isConnected()) {
        	Utils.Log("#### network type:"+(networkInfo.getType()==ConnectivityManager.TYPE_WIFI?"wifi":"others:"+networkInfo.getType()));
        	return networkInfo.getType();
        }
       	Utils.Log("#### network connect fail");
        return -1;
    } 
	
	
	
	public static boolean getScreenOn(Context context) {  
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		Utils.Log("### getScreenOn:"+isScreenOn);
		return isScreenOn;
	}
	/**
	 * 删除指定文件
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
		}
	}
	/** 
     * 判断指定的文件是否存在。 
     * @param fileName 要判断的文件的文件名 
     * @return 存在时返回true，否则返回false。 
     */  
    public static boolean isFileExist(String fileName) {  
      return new File(fileName).isFile();  
    }  
    /**
     * 判断apk是否已存在
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPkgInstalled(Context context, String packageName) {

        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
	
	
	
	 /**
     *调用未安装apk 的方法
     *仅处理manifest中第一个activity 或 service 由type：TARGET_ACTIVITY，TARGET_SERVICE 决定
     *目前还未处理生命周期
     *入口仅限onCreate  
     */
	public static void launchTargetAPK(Context context, final String apkFilePath,
			int type) throws Exception {
		Utils.Log("launchTargetAPK 1:" + apkFilePath);
		File dexOutputDir = context.getDir("dex", 0);
		final String dexOutputPath = dexOutputDir.getAbsolutePath();
		Utils.Log("dexOutputDir = " + dexOutputDir + "  dexOutputPath:"
				+ dexOutputPath);
		// dexOutputDir = /data/data/com.freecom.add/app_dex
		// dexOutputPath:/data/data/com.freecom.add/app_dex
		ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
		DexClassLoader dexClassLoader = new DexClassLoader(apkFilePath,
				dexOutputPath, null, localClassLoader);
		PackageInfo packageInfo;
		String activityName = null;
		if (type == TARGET_ACTIVITY) {
			packageInfo = context.getPackageManager().getPackageArchiveInfo(
					apkFilePath, PackageManager.GET_ACTIVITIES);
			if ((packageInfo != null) && (packageInfo.activities != null)
					&& (packageInfo.activities.length > 0)) {
				activityName = packageInfo.activities[0].name;// 仅处理manifest中第一个activity
			}
		} else if (type == TARGET_SERVICE) {
			packageInfo = context.getPackageManager().getPackageArchiveInfo(
					apkFilePath, PackageManager.GET_SERVICES);
			if ((packageInfo != null) && (packageInfo.services != null)
					&& (packageInfo.services.length > 0)) {
				activityName = packageInfo.services[0].name;// 仅处理manifest中第一个services
			}
		}
		Utils.Log("launchTargetAPK 3:" + activityName);
		if (activityName != null) {
			try {
				Class<?> localClass = dexClassLoader.loadClass(activityName);
				Constructor<?> localConstructor = localClass
						.getConstructor(new Class[] {});
				Object instance = localConstructor.newInstance(new Object[] {});

				Method setProxy = localClass.getMethod("setProxy",
						new Class[] { Context.class });
				setProxy.setAccessible(true);
				setProxy.invoke(instance, new Object[] { context });
				Bundle bundle = new Bundle();
				bundle.putInt(FROM, FROM_EXTERNAL);
				Method onCreate ;
				if (type == TARGET_ACTIVITY){
					onCreate= localClass.getDeclaredMethod("onCreate",
							new Class[] { Bundle.class });
					onCreate.setAccessible(true);
					onCreate.invoke(instance, new Object[] { bundle });
				}else if(type == TARGET_SERVICE){
					//service 的抽象了下，仅关注domission即可
					onCreate= localClass.getDeclaredMethod("doMission",
							new Class[] {});
					onCreate.setAccessible(true);
					onCreate.invoke(instance, new Object[] {});	
				}
			} catch (InvocationTargetException ite) {
				Throwable t = ite.getTargetException();// 获取目标异常
				t.printStackTrace();
			} catch (Exception e) {
				Utils.Log(TAG, "launchTargetAPK Exception:" + e);
			}
		}
	}
	
	/**
	 * 
	 * @param httpUrl
	 * @param id 文件命名的唯一标识
	 * @return
	 */
	public static File downLoadFile(String httpUrl,String dirPath,String fileName) {
		Utils.Log("downLoadFile start");
		File dir =new File(dirPath);
		final File file = new File(dir.getAbsolutePath(),fileName);
		try {
			URL url = new URL(httpUrl);
			try {
				if(!dir.exists()){
					dir.mkdirs();
					dir.setWritable(Boolean.TRUE);
				}
				if (!file.exists()) {
					file.createNewFile();
					file.setWritable(Boolean.TRUE);
				}
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				InputStream is = conn.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				conn.connect();
				long timestamp = System.currentTimeMillis();
				int len;
				if (conn.getResponseCode() >= 400) {
					// Toast.makeText(mContext, "time out",
					// Toast.LENGTH_SHORT).show();
					Utils.Log(TAG, "downLoadFile time out error");
				} else {
					while ((len = is.read(buf)) != -1) {
						fos.write(buf, 0, len);
					}
				}
				Utils.Log("downLoadFile spend time:"
						+ (System.currentTimeMillis() - timestamp) + "ms");
				conn.disconnect();
				fos.close();
				is.close();
			} catch (IOException e) {
				Utils.Log(TAG, "downLoadFile IOException error:" + e);
			}
		} catch (MalformedURLException e) {
			Utils.Log(TAG, "downLoadFile MalformedURLException error:" + e);
		} catch (Exception e) {
			Utils.Log(TAG, "downLoadFile Exception error:" + e);
		}
		Utils.Log("downLoadFile finish"+file);
		return file;
	}
	
	
	  /*
     * 毫秒转化时分秒毫秒
     */
    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
        
        StringBuffer sb = new StringBuffer();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }
        if(minute > 0) {
            sb.append(minute+"分钟");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
//        if(milliSecond > 0) {
//            sb.append(milliSecond+"毫秒");
//        }
        
        if(TextUtils.isEmpty(sb.toString())){
            sb.append("1秒");
        }
        return sb.toString();
    }
    
    
    public static SharedPreferences getSharedPpreference(Context c){
    	SharedPreferences p;
		p = c.getSharedPreferences(Utils.SHARE_PREFERENCE_CUP,Context.MODE_PRIVATE);
		return p;
    }
    public static SharedPreferences.Editor getSharedPpreferenceEdit(Context c){
    	SharedPreferences.Editor e;
		e = getSharedPpreference(c).edit();
		return e;
	}
    
    
    
    public static int getDisplayDensity(Context c) {  
        DisplayMetrics metric = new DisplayMetrics();  
        metric=c.getApplicationContext().getResources().getDisplayMetrics();  
        return metric.densityDpi;  
    }
}
