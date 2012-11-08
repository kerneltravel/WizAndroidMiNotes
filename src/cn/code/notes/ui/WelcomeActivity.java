package cn.code.notes.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import cn.code.notes.R;

public class WelcomeActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		getLoginButton().setOnClickListener(this);
		getCreateButton().setOnClickListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		iniGridViewData();
	}

	void iniGridViewData() {
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { //  ˙∆¡
			getButtonLinearLayout().setOrientation(LinearLayout.VERTICAL);
		} else {// ∫·∆¡ ±
			getButtonLinearLayout().setOrientation(LinearLayout.HORIZONTAL);
		}
	}

	LinearLayout getButtonLinearLayout() {
		return (LinearLayout) findViewById(R.id.buttonLinearLayout);
	}

	Button getLoginButton() {
		return (Button) findViewById(R.id.loginAccount);
	}

	Button getCreateButton() {
		return (Button) findViewById(R.id.createAccount);
	}

	static private final int REQ_LOGIN_ACCOUNTS = 0;
	static private final int REQ_CREATE_ACCOUNTS = 1;

	public void onClick(View v) {
		if (v.getId() == R.id.loginAccount)
			showActionActivity(REQ_LOGIN_ACCOUNTS);
		else if (v.getId() == R.id.createAccount) {
			showActionActivity(REQ_CREATE_ACCOUNTS);
		}
	}

	void showActionActivity(int index) {
		switch (index) {
		case REQ_LOGIN_ACCOUNTS:
			Intent intent = new Intent(this, WizAccountLoginActivity.class);
			startActivityForResult(intent, REQ_LOGIN_ACCOUNTS);
			break;

		case REQ_CREATE_ACCOUNTS:
			Intent it2 = new Intent(this, WizAccountCreateActivity.class);
			startActivityForResult(it2, REQ_CREATE_ACCOUNTS);//
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_CREATE_ACCOUNTS
				|| requestCode == REQ_LOGIN_ACCOUNTS) {
			if (resultCode == 1) {
				setResult(1);
				finish();
			}
		}
	}

}
