package com.ubitech.ihomedev;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.ubitech.ihomedev.R;
import com.ubitech.ihomedev.data.SPFData;
import com.ubitech.ihomedev.data.SmartHomeDeviceHelper;
import com.ubitech.ihomedev.fragment.AddressFragment;
import com.ubitech.ihomedev.fragment.SettingFragment;
import com.ubitech.ihomedev.fragment.SosFragment;
import com.ubitech.ihomedev.fragment.WeiControlFragment;
import com.ubitech.ihomedev.service.HeartbeatService;
import com.ubitech.ihomedev.service.ReceiverService;
import com.ubitech.ihomedev.util.Tool;

public class MainActivity extends FragmentActivity {
	private RadioGroup mRadioGroup;
	private RadioButton mWeiControlRadio;
	public List<Fragment> fragments = new ArrayList<Fragment>();
	private Context context;
	private MyExitDialogClickListener mListener;

	private SmartHomeDeviceHelper mDevHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		getWindow().setFlags(0x08000000, 0x08000000);// 显示menu键

		// FragmentTabAdapter tabAdapter = new FragmentTabAdapter(this,
		// fragments, R.id.fragment_container, mRadioGroup);
		//
		// tabAdapter.setOnRgsExtraCheckedChangedListener(new
		// FragmentTabAdapter.OnRgsExtraCheckedChangedListener() {
		// @Override
		// public void OnRgsExtraCheckedChanged(RadioGroup radioGroup, int
		// checkedId, int index) {
		// System.out.println("Extra---- " + index + " checked!!! ");
		// }
		// });
		//
		int hasDev = getIntent().getIntExtra("hasDev", 1);
		if (hasDev == 0) {
			int userId = mDevHelper.queryLoginIdforDevId(SPFData.getCurDevId(this));
			if (userId != SPFData.getUserId(this)) {
				showBindDeviceDialog();
			}
		}
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				switch (checkedId) {
				case R.id.radiobtn_weicontrol:
					WeiControlFragment fragment = new WeiControlFragment();
					transaction.replace(R.id.fragment_container, fragment, "weiControl_fragment");
					transaction.commit();
					break;
				case R.id.radiobtn_share:
					SosFragment sosFragment = new SosFragment();
					transaction.replace(R.id.fragment_container, sosFragment, "sos_fragment");
					transaction.commit();
					break;
				case R.id.radiobtn_address:
					AddressFragment addressFragment = new AddressFragment();
					transaction.replace(R.id.fragment_container, addressFragment,
							"address_fragment");
					transaction.commit();
					break;
				case R.id.radiobtn_setting:
					SettingFragment settingFragment = new SettingFragment();
					transaction.replace(R.id.fragment_container, settingFragment,
							"setting_fragment");
					transaction.commit();
					break;
				default:
					break;
				}
			}
		});

		mWeiControlRadio.setChecked(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, getResources().getString(R.string.user_management));
		menu.add(0, 2, 2, getResources().getString(R.string.about));
		menu.add(0, 3, 3, getResources().getString(R.string.more));
		menu.add(0, 4, 4, getResources().getString(R.string.exit));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO menu点击事件
		int id = item.getItemId();
		switch (id) {
		case 1:
			Toast.makeText(this, "用户管理被点击了", Toast.LENGTH_SHORT).show();
			break;
		case 2:
			Toast.makeText(this, "关于被点击了", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, GuideActivity.class);
			intent.putExtra("about", true);
			startActivity(intent);
			break;
		case 3:
			Toast.makeText(this, "更多被点击了被点击了", Toast.LENGTH_SHORT).show();
			break;
		case 4:
			showExitDialog();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showBindDeviceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("没有设备");
		builder.setMessage("是否要绑定设备");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(context, BindActivity.class));
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	public void showExitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(getResources().getString(R.string.hint));
		builder.setPositiveButton(getResources().getString(R.string.sure), mListener);
		builder.setNegativeButton(getResources().getString(R.string.cancel), mListener);
		builder.setMessage(getResources().getString(R.string.really_exit));
		builder.create().show();
	}

	private void init() {
		context = this;
		mListener = new MyExitDialogClickListener();
		mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
		mWeiControlRadio = (RadioButton) findViewById(R.id.radiobtn_weicontrol);
		fragments.add(new WeiControlFragment());
		fragments.add(new SosFragment());
		fragments.add(new AddressFragment());
		fragments.add(new SettingFragment());

		mDevHelper = new SmartHomeDeviceHelper(this);

	}

	class MyExitDialogClickListener implements android.content.DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON_POSITIVE) {
				stopService(new Intent(context, ReceiverService.class));
				stopService(new Intent(context, HeartbeatService.class));
				finish();
				Tool.getInstance().exit();
				System.exit(0);
			}
		}
	}

}
