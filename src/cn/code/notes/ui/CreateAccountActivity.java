package cn.code.notes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cn.code.notes.R;
import cn.code.notes.share.WizApiEvents;
import cn.code.notes.share.WizApiEventsListener;
import cn.code.notes.share.WizCreateAccount;
import cn.code.notes.share.WizGlobals;
import cn.code.notes.share.WizSQLite;

public class CreateAccountActivity extends Activity implements WizApiEvents {

	String mLastErrorMessage;

	private static final int DIALOG_WAIT = 0;
	private static final int DIALOG_ERROR = 1;

	int downloadInt = 1;

	static final String isDownLoad = "1";
	static final boolean isAutoSync = true;
	static final boolean isPasswordProtect = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		setOnClickListener();

		//
		WizApiEventsListener.add(this);
	}

	@Override
	public void onDestroy() {
		WizApiEventsListener.remove(this);
		super.onDestroy();
	}

	Button getCreateButton() {
		return (Button) findViewById(R.id.loginButton);
	}

	void setButtonInfo() {
		getCreateButton().setText(
				WizGlobals
						.getResourcesString(this, R.string.account_with_login));
	}

	void setOnClickListener() {
		// 设置按钮的基本信息
		setButtonInfo();
		// 监听按钮
		getCreateButton().setOnClickListener(new View.OnClickListener() {

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
		String password2 = getPassword2();
		if (userId == null || userId.length() == 0) {
			WizGlobals.showMessage(this, R.string.message_enter_user_id, true);
			return;
		}
		if (password == null || password.length() == 0) {
			WizGlobals.showMessage(this, R.string.message_enter_password, true);
			return;
		}
		if (password2 == null || password2.length() == 0) {
			WizGlobals.showMessage(this,
					R.string.message_enter_confirm_password, true);
			return;
		}
		if (!password.equals(password2)) {
			WizGlobals
					.showMessage(this, R.string.password_does_not_match, true);
			return;
		}
		//
		showDialog(DIALOG_WAIT);
		//

		WizCreateAccount verify = new WizCreateAccount(this, getUserId(),
				getPassword(), WizGlobals.isPhone(this));
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
		String editAccountUserId = ((EditText) findViewById(R.id.editTextUserID))
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

	String getPassword2() {
		return ((EditText) findViewById(R.id.editTextConfirmPassword))
				.getText().toString();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onApiBegin(String userId, String actionName) {

	}

	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		//
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForCreate))
			return;
		//
		removeDialog(DIALOG_WAIT);
		//
		if (succeeded) {
			String id = getUserId();
			//
			WizSQLite.updateAccountUserId(this, id);
			WizSQLite.updateAccountPassword(this, getPassword());
			setResult(1);
			finish();
		}
	}

	public void onApiError(String userId, String actionName, int stringID,
			String errorMessage) {
		//
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForCreate))
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