package cn.code.notes.ui;

import cn.code.notes.R;
import cn.code.notes.share.MaxWidthLinearLayout;
import cn.code.notes.share.WizApiEvents;
import cn.code.notes.share.WizApiEventsListener;
import cn.code.notes.share.WizCreateAccount;
import cn.code.notes.share.WizGlobals;
import cn.code.notes.share.WizSQLite;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

//phoneÊ×Ò³
public class WizAccountCreateActivity extends Activity implements WizApiEvents {

	static private final int DIALOG_WAIT = 100;
	static private final int DIALOG_ERROR = 101;
	private static final int MAX_LENGTH = MaxWidthLinearLayout.MAX_WIDTH + 100;

	private String mLastErrorMessage = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_create);
		rSetLinearLayoutView();

	//	getButtonBack().setOnClickListener(onBackButtonClick);
		getLoginButton().setOnClickListener(onLoginButtonClick);
//		getHeaderTitle().setText(
//				WizGlobals.getResourcesString(this,
//						R.string.account_with_cerate));

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
					new LinearLayout.LayoutParams(MAX_LENGTH,
							LayoutParams.WRAP_CONTENT));
			getLoginLinearLayout().setMinimumHeight(MAX_LENGTH);
		}
	}

	LinearLayout getLoginLinearLayout() {
		return (LinearLayout) findViewById(R.id.loginLinearLayout);
	}

	ImageView getButtonBack() {
		return (ImageView) findViewById(R.id.backButton);
	}

	EditText getUserIdEditText() {
		return (EditText) findViewById(R.id.userId);
	}

	EditText getPasswordEditText() {
		return (EditText) this.findViewById(R.id.password);
	}

	EditText getConfirmPasswordEditText() {
		return (EditText) this.findViewById(R.id.confirmPassword);
	}

	Button getLoginButton() {
		return (Button) findViewById(R.id.loginButton);
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

	String getConfirmPassword() {
		return getConfirmPasswordEditText().getText().toString().trim();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAIT: {
			return WizGlobals.createProgressDialog(this,
					R.string.create_account, R.string.wait_for_login, true,
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

	void clearEditText() {
		getUserIdEditText().setText("");
		getPasswordEditText().setText("");
		getConfirmPasswordEditText().setText("");
	}

	void loginAccount() {
		serverVerifyUser();
	}

	public void serverVerifyUser() {
		String userId = getUserId();
		String password = getPassword();
		String password2 = getConfirmPassword();

		if (WizGlobals.isEmptyString(userId)) {
			WizGlobals.showMessage(this, R.string.message_enter_user_id, true);
			return;
		}
		if (WizGlobals.isEmptyString(password)) {
			WizGlobals.showMessage(this, R.string.message_enter_password, true);
			return;
		}
		if (WizGlobals.isEmptyString(password2)) {
			WizGlobals.showMessage(this,
					R.string.message_enter_confirm_password, true);
			return;
		}
		if (!TextUtils.equals(password, password2)) {
			WizGlobals
					.showMessage(this, R.string.password_does_not_match, true);
			return;
		}

		WizCreateAccount verify = new WizCreateAccount(this, getUserId(),
				getPassword(), WizGlobals.isPhone(this));
		//
		verify.start();

	}

	OnClickListener onBackButtonClick = new OnClickListener() {

		public void onClick(View v) {
			finish();
		}
	};

	OnClickListener onLoginButtonClick = new OnClickListener() {

		public void onClick(View v) {
			loginAccount();

		}
	};

	public void onApiBegin(String userId, String actionName) {
		if (!WizGlobals.isEmptyString(userId)
				&& actionName.equals(WizGlobals.ActionNameForCreate)) {
			showDialog(DIALOG_WAIT);
		}
	}

	
	
	public void onShowMessage(String userId, String actionName, int arg1,
			int arg2, String mMessage) {

	}

	
	
	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		removeDialog(DIALOG_WAIT);
		if (!getUserId().equals(userId)
				|| !actionName.equals(WizGlobals.ActionNameForCreate))
			return;
		if (succeeded) {
			String id = getUserId();
			WizSQLite.updateAccountUserId(this, id);
			WizSQLite.updateAccountPassword(this, getPassword());
			setResult(1);
			finish();
		}
	}

	public void onApiError(String userId, String actionName, int stringID,
			String errorMessage) {
		removeDialog(DIALOG_WAIT);
		mLastErrorMessage = WizGlobals.getErrorMessage(this, stringID,
				errorMessage);
		if (!WizGlobals.isEmptyString(mLastErrorMessage))
			showDialog(DIALOG_ERROR);
		clearEditText();
	}

}