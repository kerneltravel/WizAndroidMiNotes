package cn.code.notes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.code.notes.R;
import cn.code.notes.share.WizApiEvents;
import cn.code.notes.share.WizApiEventsListener;
import cn.code.notes.share.WizGlobals;
import cn.code.notes.share.WizSQLite;
import cn.code.notes.share.WizVerifyAccount;

public class SetupAccountActivity extends Activity implements WizApiEvents {

	String mLastErrorMessage;
	TextView createAccountTextView;

	private static final int DIALOG_WAIT = 0;
	private static final int DIALOG_ERROR = 1;

	static final String isDownLoad = "1";
	static final boolean isAutoSync = true;
	static final boolean isPasswordProtect = false;

	private String createNewAccountUrl;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_account);

		//
		createAccountTextView = getCreateAccountTextView();

		createNewAccountUrl = WizGlobals.getResourcesString(this,
				R.string.create_new_account) + ">>";
		createAccountTextView.setText(Html.fromHtml("<u>" + createNewAccountUrl
				+ "</u>"));
		isAddAccount();

		setOnClickListener();
		//
		WizApiEventsListener.add(this);
	}

	@Override
	public void onDestroy() {
		WizApiEventsListener.remove(this);
		super.onDestroy();
	}

	Button getLoginButton() {
		return (Button) findViewById(R.id.loginButton);
	}

	void setButtonInfo() {
		getLoginButton().setText(
				WizGlobals
						.getResourcesString(this, R.string.account_with_login));
	}

	void setOnClickListener() {
		// 设置按钮的基本信息
		setButtonInfo();
		// 监听按钮
		getLoginButton().setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				save();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAIT: {
			return WizGlobals.createProgressDialog(this,
					R.string.setup_account, R.string.wait_for_login, true,
					false);
		}
		case DIALOG_ERROR: {
			return WizGlobals.createAlertDialog(this, R.string.app_name,
					mLastErrorMessage, null);
		}
		}
		return null;
	}

	//
	public void save() {
		String userId = getUserId();
		String password = getPassword();
		if (userId == null || userId.length() == 0) {
			WizGlobals.showMessage(this, R.string.message_enter_user_id, true);
			return;
		}
		if (password == null || password.length() == 0) {
			WizGlobals.showMessage(this, R.string.message_enter_password, true);
			return;
		}
		//
		showDialog(DIALOG_WAIT);
		//
		WizVerifyAccount verify = new WizVerifyAccount(this, getUserId(),
				getPassword());
		//
		verify.start();
	}

	// 获取String类型数组
	String[] getStringArray(String str) {
		String[] textCutOutString = null;

		textCutOutString = str.split(" ");
		return textCutOutString;
	}

	// 从数组中取出数据连接成一个String字符串
	String getUserId(String str) {

		String editTextAccountUserId = "";

		String[] UserIdArray = getStringArray(str);

		for (int i = 0; i < UserIdArray.length; i++) {

			editTextAccountUserId = editTextAccountUserId + UserIdArray[i];
		}

		return editTextAccountUserId;

	}

	String getUserId() {
		String editAccountUserId = ((EditText) findViewById(R.id.firstAccountForUserId))
				.getText().toString();
		if (editAccountUserId.indexOf(" ") != -1) {

			editAccountUserId = getUserId(editAccountUserId);
		}
		return editAccountUserId;
	}

	String getPassword() {
		return ((EditText) findViewById(R.id.firstAccountForPassword))
				.getText().toString();
	}

	TextView getCreateAccountTextView() {
		return (TextView) findViewById(R.id.setupAccountAddAccount);
	}

	void isAddAccount() {
		createAccountTextView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showCreateAccountActivity();
			}
		});
	}

	private final static int WIZ_CREATE_ACCOUNT = 101;

	void showCreateAccountActivity() {
		Intent intent = new Intent();
		intent.setClass(this, CreateAccountActivity.class);
		startActivityForResult(intent, WIZ_CREATE_ACCOUNT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case WIZ_CREATE_ACCOUNT:
			if (resultCode == 0) {
				setResult(0);
				finish();
			} else if (resultCode == 1) {
				setResult(1);
				finish();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onApiBegin(String userId, String actionName) {
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForVerify))
			return;
	}

	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForVerify))
			return;
		//
		removeDialog(DIALOG_WAIT);
		//
		if (succeeded) {
			String id = getUserId();
			//
			WizSQLite.updateAccountUserId(this, id);
			WizSQLite.updateAccountPassword(this, getPassword());
			//
			setResult(1);
			finish();
		}
	}

	public void onApiError(String userId, String actionName, int stringID,
			String errorMessage) {
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForVerify))
			return;
		//
		mLastErrorMessage = WizGlobals.getErrorMessage(this, stringID,
				errorMessage);
		if (stringID != -1)
			showDialog(DIALOG_ERROR);
	}

	public void onShowMessage(String userId, String actionName, int arg1,
			int arg2, String mMessage) {

	}
}
