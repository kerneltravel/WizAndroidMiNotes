package cn.code.notes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.code.notes.R;
import cn.code.notes.share.MaxWidthLinearLayout;
import cn.code.notes.share.WizApiEvents;
import cn.code.notes.share.WizApiEventsListener;
import cn.code.notes.share.WizGlobals;
import cn.code.notes.share.WizSQLite;
import cn.code.notes.share.WizVerifyAccount;

//phoneÊ×Ò³
public class WizAccountLoginActivity extends Activity implements WizApiEvents {

	static private final int DIALOG_WAIT = 100;
	static private final int DIALOG_ERROR = 101;

	private String mLastErrorMessage = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_login);
		rSetLinearLayoutView();

		// getButtonBack().setOnClickListener(onBackButtonClick);
		getLoginButton().setOnClickListener(onLoginButtonClick);

		WizApiEventsListener.add(this);
	}

	public void onDestroy() {
		WizApiEventsListener.remove(this);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	void rSetLinearLayoutView() {

		if (WizGlobals.isPhone(this)) {
			getLoginLinearLayout().setLayoutParams(
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.FILL_PARENT));
		} else {
			getLoginLinearLayout().setLayoutParams(
					new LinearLayout.LayoutParams(
							MaxWidthLinearLayout.MAX_WIDTH + 100,
							LayoutParams.WRAP_CONTENT));
			getLoginLinearLayout().setMinimumHeight(
					MaxWidthLinearLayout.MAX_WIDTH);
		}

//		getHeaderTitle().setText(
//				WizGlobals
//						.getResourcesString(this, R.string.account_with_login));
	}

	LinearLayout getLoginLinearLayout() {
		return (LinearLayout) findViewById(R.id.loginLinearLayout);
	}

	EditText getUserIdEditText() {
		return (EditText) findViewById(R.id.userId);
	}

	EditText getPasswordEditText() {
		return (EditText) this.findViewById(R.id.password);
	}

	Button getLoginButton() {
		return (Button) findViewById(R.id.loginButton);
	}

	ImageView getButtonBack() {
		return (ImageView) findViewById(R.id.backButton);
	}

	TextView getHeaderTitle() {
		return (TextView) findViewById(R.id.headerTitle);
	}

	String getUserId() {
		return getUserIdEditText().getText().toString().trim()
				.replaceAll(" ", "");
	}

	String getPassword() {
		return getPasswordEditText().getText().toString().trim();
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
					mLastErrorMessage, onClick(DIALOG_ERROR, -1));
		}
		}
		return null;
	}

	android.content.DialogInterface.OnClickListener onClick(int dialogId,
			int isClick) {
		switch (dialogId) {
		case DIALOG_ERROR:
			return new android.content.DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					getPasswordEditText().setText("");
					removeDialog(DIALOG_ERROR);
				}
			};

		}
		return null;
	}

	void loginAccount(String userId) {

		serverVerifyUser(userId);

	}

	public void serverVerifyUser(String userId) {
		String password = getPassword();
		if (WizGlobals.isEmptyString(userId)) {
			WizGlobals.showMessage(this, R.string.message_enter_user_id, true);
			return;
		}
		if (WizGlobals.isEmptyString(password)) {
			WizGlobals.showMessage(this, R.string.message_enter_password, true);
			return;
		}
		WizVerifyAccount verify = new WizVerifyAccount(this, getUserId(),
				password);
		verify.start();
	}

	OnClickListener onLoginButtonClick = new OnClickListener() {

		public void onClick(View v) {
			String userId = getUserId();
			if (!WizGlobals.isEmptyString(userId)) {
				loginAccount(userId);
			} else {
				WizGlobals.showMessage(WizAccountLoginActivity.this,
						R.string.message_enter_user_id, true);
			}

		}
	};

	OnClickListener onBackButtonClick = new OnClickListener() {

		public void onClick(View v) {
			finish();
		}
	};

	public void onApiBegin(String userId, String actionName) {
		if (!WizGlobals.isEmptyString(userId)
				&& actionName.equals(WizGlobals.ActionNameForVerify)) {
			showDialog(DIALOG_WAIT);
		}
	}

	public void onShowMessage(String userId, String actionName, int arg1,
			int arg2, String mMessage) {

	}

	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		removeDialog(DIALOG_WAIT);
		//
		if (succeeded && actionName.equals(WizGlobals.ActionNameForVerify)) {
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
		mLastErrorMessage = WizGlobals.getErrorMessage(this, stringID,
				errorMessage);
		removeDialog(DIALOG_WAIT);
		if (!WizGlobals.isEmptyString(mLastErrorMessage))
			showDialog(DIALOG_ERROR);
	}

}