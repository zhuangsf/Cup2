package com.sf.cup2;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.sf.cup2.uploadpic.SelectPicPopupWindow;
import com.sf.cup2.utils.FileUtil;
import com.sf.cup2.utils.Utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentHomePerson extends Fragment {
	private final static String TAG = FragmentHomePerson.class.getPackage().getName() + "." + FragmentHomePerson.class.getSimpleName();

	ListView personlist_view_pic;
	ListView personlist_view1;
	ListView personlist_view2;
	String[] list1Title;
	String[] list2Title;

	private int[] list1Drawable;

	PersonListViewAdapter1 hlva1;

	List<Map<String, Object>> personList1 = new ArrayList<Map<String, Object>>(); // list
																					// view
																					// 就是一直玩弄这个

	EditText person_info;

	AlertDialog ad;

	RelativeLayout mainLayout;
	LinearLayout avatar_layout;
	ImageView avatar_image;
	private ImageView goBack;
	
	private SelectPicPopupWindow menuWindow; // 自定义的头像编辑弹出框
	private static final String IMAGE_FILE_NAME = "avatarImage.jpg";// 头像文件名称
	private static final String IMAGE_FILE_NAME_CROP = "avatarImage_crop.jpg";// 头像文件名称
	private String urlpath; // 图片本地路径
	private String resultStr = ""; // 服务端返回结果集
	private static ProgressDialog pd;// 等待进度圈
	private static final int REQUESTCODE_PICK = 0; // 相册选图标记
	private static final int REQUESTCODE_TAKE = 1; // 相机拍照标记
	private static final int REQUESTCODE_CUTTING = 2; // 图片裁切标记

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message paramAnonymousMessage) {
			switch (paramAnonymousMessage.what) {
			case 1:
				// alertdialog with edittext cant not open im.
				try {
					Thread.sleep(200);
					person_info.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, person_info.getRight(),
							person_info.getRight() + 5, 0));
					person_info.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, person_info.getRight(),
							person_info.getRight() + 5, 0));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				break;
			case 2:
				break;
			case Utils.UPLOAD_SUCCESS_MSG:
				JSONObject jsonObject = (JSONObject) paramAnonymousMessage.obj;
				// upload pic success
				String picUrl = jsonObject.optString("url", "");
				if (!TextUtils.isEmpty(picUrl)) {
					SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
					e.putString(Utils.SHARE_PREFERENCE_CUP_AVATAR_WEB_PATH, picUrl);
					e.commit();
				}
				break;
			}
		}
	};

	public static FragmentHomePerson newInstance(Bundle b) {
		FragmentHomePerson fd = new FragmentHomePerson();
		fd.setArguments(b);
		return fd;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources();
		list1Title = res.getStringArray(R.array.person_list_title1);
		list2Title = res.getStringArray(R.array.person_list_title2);
		list1Drawable = new int[] { R.drawable.icon_head, R.drawable.icon_nickname, R.drawable.icon_gender, R.drawable.icon_height, R.drawable.icon_weight, R.drawable.icon_age };


	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.tab_home_person_info, null);

		initList1();

		personlist_view1 = (ListView) view.findViewById(R.id.persionlist_view1);
		hlva1 = new PersonListViewAdapter1(this.getActivity(), getData1(), R.layout.tab_home_list_item_top, new String[] { "item_image", "title_text", "text_view1", "image_view1", "image_view2" },
				new int[] { R.id.item_image, R.id.title_text, R.id.text_view1, R.id.image_view1, R.id.image_view2 });
		setHeight(hlva1, personlist_view1);
		personlist_view1.setAdapter(hlva1);


		mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);

		
		goBack = (ImageView)view.findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            	FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            	ft.add(R.id.fragmentfield, new FragmentHome());
            	ft.remove(FragmentHomePerson.this);
            	ft.addToBackStack(null);
				ft.commit();
			}
		});
		
		return view;
	}



	// save and edit pic uri can not be same or it will 0byte
	private Uri getTakePicSaveUri() {
		String filePath = Utils.getInternelStoragePath(getActivity());
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}

		return Uri.fromFile(new File(filePath, IMAGE_FILE_NAME));
	}

	private Uri getCropPicSaveUri() {

		String filePath = Utils.getInternelStoragePath(getActivity());
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}

		return Uri.fromFile(new File(filePath, IMAGE_FILE_NAME_CROP));
	}

	// 为弹出窗口实现监听类
	private OnClickListener itemsOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			// 拍照
			case R.id.takePhotoBtn:
				Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// 下面这句指定调用相机拍照后的照片存储的路径
				takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTakePicSaveUri());
				startActivityForResult(takeIntent, REQUESTCODE_TAKE);
				break;
			// 相册选择图片
			case R.id.pickPhotoBtn:
				Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
				// 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
				pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(pickIntent, REQUESTCODE_PICK);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_PICK:// 直接从相册获取
			try {
				startPhotoZoom(data.getData());
			} catch (NullPointerException e) {
				e.printStackTrace();// 用户点击取消操作
			}
			break;
		case REQUESTCODE_TAKE:// 调用相机拍照
			startPhotoZoom(getTakePicSaveUri());
			break;
		case REQUESTCODE_CUTTING:// 取得裁剪后的图片
			try {
				setPicToView(data);
			} catch (Exception e) {
				e.printStackTrace();// 用户点击取消操作
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);// 去黑边
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		// the return data true may waste logs of mem
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, getCropPicSaveUri());
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			// 取得SDCard图片路径做显示
			// Bitmap photo = extras.getParcelable("data");
			Bitmap photo = getBitmapFromUri(getCropPicSaveUri(), getActivity());
			Drawable drawable = new BitmapDrawable(null, photo);
			urlpath = FileUtil.saveFile(getActivity(), Utils.getInternelStoragePath(getActivity()), IMAGE_FILE_NAME, photo);
			SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
			e.putString(Utils.SHARE_PREFERENCE_CUP_AVATAR, urlpath);
			// e.putBoolean(Utils.SHARE_PREFERENCE_CUP_AVATAR_IS_MODIFY, true);
			e.commit();

			// 更新头像
			hlva1.notifyDataSetChanged();

			try {
				// send to server
				new Thread(new Runnable() {
					@Override
					public void run() {
						SharedPreferences p = Utils.getSharedPpreference(getActivity());
						final String phone = p.getString(Utils.SHARE_PREFERENCE_CUP_PHONE, "");
						final String accountid = p.getString(Utils.SHARE_PREFERENCE_CUP_ACCOUNTID, "");

						// http://121.199.75.79:8280//user/updateProfile.do
						if (!TextUtils.isEmpty(urlpath)) {
							Utils.httpPostFile(Utils.URL_PATH + "/user/updateProfile.do", urlpath, mHandler, accountid, phone);
						}
					}
				}).start();
			} catch (Exception ee) {
				Utils.Log(TAG, "xxxxxxxxxxxxxxxxxx httpPut error:" + ee);
				ee.printStackTrace();
			}
			// 新线程后台上传服务端
			// pd = ProgressDialog.show(mContext, null, "正在上传图片，请稍候...");
			// new Thread(uploadImageRunnable).start();
		}
	}

	public static Bitmap getBitmapFromUri(Uri uri, Context mContext) {
		try {
			// 读取uri所在的图片
			Bitmap bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
			bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private class PersonListViewAdapter1 extends SimpleAdapter {
		public PersonListViewAdapter1(Context context, List<Map<String, Object>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			view.setOnClickListener(new MyListener1(position));

			TextView textView;
			final ImageView imageView1;
			final ImageView imageView2;
			textView = (TextView) view.findViewById(R.id.text_view1);
			imageView1 = (ImageView) view.findViewById(R.id.image_view1);
			imageView2 = (ImageView) view.findViewById(R.id.image_view2);

			if (position == 0) {
				textView.setVisibility(View.GONE);
				imageView1.setVisibility(View.VISIBLE);

				// SharedPreferences p =
				// Utils.getSharedPpreference(getActivity());
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
				imageView2.setVisibility(View.GONE);
			} else if (position == 1) {
				textView.setVisibility(View.VISIBLE);
				
				
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				final String nickName = p.getString(Utils.SHARE_PREFERENCE_CUP_NICKNAME, "");
				
				textView.setText(nickName);

				imageView1.setVisibility(View.GONE);
				imageView2.setVisibility(View.GONE);
			} else if (position == 2) {
				textView.setVisibility(View.GONE);

				imageView1.setVisibility(View.VISIBLE);
				imageView2.setVisibility(View.VISIBLE);
				
				imageView1.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						imageView1.setImageResource(R.drawable.button_man2);
						imageView2.setImageResource(R.drawable.button_woman1);
						

						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_SEX, "mail");
						e.commit();
						

					}
				});
				imageView2.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						imageView1.setImageResource(R.drawable.button_man1);
						imageView2.setImageResource(R.drawable.button_woman2);
						
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_SEX, "mail");
						e.commit();
						
					}
				});
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String sex = p.getString(Utils.SHARE_PREFERENCE_CUP_SEX, "femail");

				if ("femail".equals(sex)) {
					imageView1.setImageResource(R.drawable.button_man1);
					imageView2.setImageResource(R.drawable.button_woman2);
				} else {
					imageView1.setImageResource(R.drawable.button_man2);
					imageView2.setImageResource(R.drawable.button_woman1);
				}
			} else if (position == 3) {
				textView.setVisibility(View.VISIBLE);
				
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String height = p.getString(Utils.SHARE_PREFERENCE_CUP_HEIGHT, "160");
				textView.setText(height);

				imageView1.setVisibility(View.GONE);
				imageView2.setVisibility(View.GONE);
			} else if (position == 4) {
				textView.setVisibility(View.VISIBLE);
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String height = p.getString(Utils.SHARE_PREFERENCE_CUP_WEIGHT, "45");
				textView.setText(height);

				imageView1.setVisibility(View.GONE);
				imageView2.setVisibility(View.GONE);
			} else if (position == 5) {
				textView.setVisibility(View.VISIBLE);
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String birthday = p.getString(Utils.SHARE_PREFERENCE_CUP_BIRTHDAY, "1990-01-01");
				textView.setText(birthday);

				imageView1.setVisibility(View.GONE);
				imageView2.setVisibility(View.GONE);
			}

			return view;
		}

	}

	private class MyListener1 implements OnClickListener {
		int mPosition;

		public MyListener1(int inPosition) {
			mPosition = inPosition;
		}

		@Override
		public void onClick(View v) {

			switch (mPosition) {
			case 0: {
				menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
				menuWindow.showAtLocation(mainLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			}
				break;
			case 1: {
				
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.tab_home_person_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				TextView person_title = (TextView) layout.findViewById(R.id.person_title);
				person_info = (EditText) layout.findViewById(R.id.person_info);
				person_info.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
				person_info.setText("");  //默认为空的好了
				person_title.setText(list1Title[mPosition]);
				ad = new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_NICKNAME, person_info.getText().toString());
						e.commit();
						doUpdate1();
					}
				}).setNegativeButton(R.string.cancel, null).create();
				ad.setTitle(R.string.personal_setting);
				ad.setView(layout);
				ad.show();
//				Message msg = new Message();
//				msg.what = 1;
//				msg.arg1 = 1;
//				mHandler.sendMessage(msg);
			}
				break;
			case 3:
			{
				
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.tab_home_person_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				TextView person_title = (TextView) layout.findViewById(R.id.person_title);
				person_title.setText(list1Title[mPosition]);
				person_info = (EditText) layout.findViewById(R.id.person_info);
				person_info.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
				person_info.setInputType(InputType.TYPE_CLASS_NUMBER);
				ad = new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_HEIGHT, person_info.getText().toString());
						e.commit();
						doUpdate1();
					}
				}).setNegativeButton(R.string.cancel, null).create();
				ad.setTitle(R.string.personal_setting);
				ad.setView(layout);
				ad.show();
//				Message msg = new Message();
//				msg.what = 1;
//				msg.arg1 = 1;
//				mHandler.sendMessage(msg);
			}
				break;
			case 4:
			{
				
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.tab_home_person_dialog, (ViewGroup) v.findViewById(R.id.dialog));
				TextView person_title = (TextView) layout.findViewById(R.id.person_title);
				person_title.setText(list1Title[mPosition]);
				person_info = (EditText) layout.findViewById(R.id.person_info);
				person_info.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
				person_info.setInputType(InputType.TYPE_CLASS_NUMBER);
				ad = new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_WEIGHT, person_info.getText().toString());
						e.commit();
						doUpdate1();
					}
				}).setNegativeButton(R.string.cancel, null).create();
				ad.setTitle(R.string.personal_setting);
				ad.setView(layout);
				ad.show();
//				Message msg = new Message();
//				msg.what = 1;
//				msg.arg1 = 1;
//				mHandler.sendMessage(msg);
			}
				break;
				
			case 5:
			{
				Calendar c = Calendar.getInstance();
				SharedPreferences p = Utils.getSharedPpreference(getActivity());
				String birthday = p.getString(Utils.SHARE_PREFERENCE_CUP_BIRTHDAY, "1990-01-01");
				String[] dateSpilt = (TextUtils.isEmpty(birthday) ? "1990-01-01" : birthday).split("-");

				DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
						// et.setText("您选择了：" + year + "年" + (month+1) +
						// "月" +
						// dayOfMonth + "日");
						String monthString = (month+1)<10?("0"+(month+1)):(month+1+"");
						String dayOfMonthString = dayOfMonth<10?("0"+dayOfMonth):(dayOfMonth+"");
						String dateFormat = year + "-" + monthString + "-" + dayOfMonthString;
						SharedPreferences.Editor e = Utils.getSharedPpreferenceEdit(getActivity());
						e.putString(Utils.SHARE_PREFERENCE_CUP_BIRTHDAY, dateFormat);
						e.commit();
						doUpdate1();
					}
				}, Integer.parseInt(dateSpilt[0]), // 传入年份
						Integer.parseInt(dateSpilt[1]) - 1, // 传入月份
						Integer.parseInt(dateSpilt[2]) // 传入天数
				);
				dialog.show();
			}
			break;
			}
		}
	}



	private TextView addPredefineButton(String text) {
		final TextView btn = new TextView(getActivity());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 5);
		btn.setLayoutParams(layoutParams);
		btn.setGravity(Gravity.CENTER);
		btn.setMinWidth(100);
		// btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,25);
		btn.setText(text);
		btn.setTextColor(0xFFFFFFFF);
		btn.setBackground(getResources().getDrawable(R.drawable.predefine_shape));
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				person_info.setText(btn.getText());
			}
		});
		return btn;
	}

	public void setHeight(BaseAdapter comAdapter, ListView l) {
		int listViewHeight = 0;
		int adaptCount = comAdapter.getCount();
		for (int i = 0; i < adaptCount; i++) {
			View temp = comAdapter.getView(i, null, l);
			temp.measure(0, 0);
			listViewHeight += temp.getMeasuredHeight();
		}
		LayoutParams layoutParams = l.getLayoutParams();
		layoutParams.width = LayoutParams.FILL_PARENT;
		layoutParams.height = listViewHeight + 2;
		l.setLayoutParams(layoutParams);
	}

	private List<Map<String, Object>> getData1() {
		return personList1;
	}

	private void initList1() {
		Map<String, Object> map;
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		for (int i = 0; i < list1Title.length; i++) {
			map = new HashMap<String, Object>();

			map.put("item_image", list1Drawable[i]);
			Utils.Log("listTitle = " + list1Title[i]);
			map.put("title_text", list1Title[i]);
			map.put("text_view1", "");
			map.put("image_view1", 0);
			map.put("image_view2", 0);

			personList1.add(map);
		}
	}



	private void doUpdate1() {
		if (hlva1 != null) {
			Utils.Log("doUpdate1:" + personList1);
			hlva1.notifyDataSetChanged();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences p = Utils.getSharedPpreference(getActivity());
		final JSONObject result = new JSONObject();
		String nickname = p.getString(Utils.SHARE_PREFERENCE_CUP_NICKNAME, "");
		String sex = p.getString(Utils.SHARE_PREFERENCE_CUP_SEX, "");
		final String phone = p.getString(Utils.SHARE_PREFERENCE_CUP_PHONE, "");


		String height = p.getString(Utils.SHARE_PREFERENCE_CUP_HEIGHT, "");
		String weight = p.getString(Utils.SHARE_PREFERENCE_CUP_WEIGHT, "");
		String birthday = p.getString(Utils.SHARE_PREFERENCE_CUP_BIRTHDAY, "");

		final String accountid = p.getString(Utils.SHARE_PREFERENCE_CUP_ACCOUNTID, "");
		final String avatarwebpath = p.getString(Utils.SHARE_PREFERENCE_CUP_AVATAR_WEB_PATH, "");

		final String avatar = p.getString(Utils.SHARE_PREFERENCE_CUP_AVATAR, "");


		if (TextUtils.isEmpty(accountid) || TextUtils.isEmpty(phone)) {
			// it must be a bug missing the accountid
			return;
		}
		try {
	//		result.put("accountid", accountid);

	//		result.put("avatar", avatarwebpath);// it must be upload this text
	//											// every time

			result.put("nickname", nickname);
			result.put("sex", sex);
			result.put("phone", phone);

			result.put("height", height);
			result.put("weight", weight);
			result.put("birthday", birthday);

			Utils.Log("xxxxxxxxxxxxxxxxxx httpPut result:" + result + ",avatar:" + avatar );
			// send to server
			new Thread(new Runnable() {
				@Override
				public void run() {
					// http://121.199.75.79:8280/user/saveme
					Utils.httpPut(Utils.URL_PATH + "/user/saveme", result, mHandler);

					if (!TextUtils.isEmpty(avatar)) {
						// Utils.httpPostFile(Utils.URL_PATH
						// +"/user/updateProfile.do", avatar,
						// mHandler,accountid,phone);
					}
				}
			}).start();
		} catch (Exception e) {
			Utils.Log(TAG, "xxxxxxxxxxxxxxxxxx httpPut error:" + e);
			e.printStackTrace();
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
