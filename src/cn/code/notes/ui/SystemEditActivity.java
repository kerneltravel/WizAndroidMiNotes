package cn.code.notes.ui;

import cn.code.notes.R;
import cn.code.notes.share.WizApiEvents;
import cn.code.notes.share.WizApiEventsListener;
import cn.code.notes.share.WizGlobalData;
import cn.code.notes.share.WizGlobals;
import cn.code.notes.share.WizSQLite;
import cn.code.notes.share.WizSync;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SystemEditActivity extends PreferenceActivity implements
		WizApiEvents {
	public static final String PREFERENCE_NAME = "notes_preferences";

	public static final String PREFERENCE_SYNC_ACCOUNT_NAME = "sync_account_name";
	public static final String PREFERENCE_LAST_SYNC_TIME = "pref_last_sync_time";
	public static final String PREFERENCE_SET_BG_COLOR_KEY = "pref_key_bg_random_appear";
	public static final String PREFERENCE_SYNC_ACCOUNT_KEY = "pref_sync_account_key";

	public Preference mAccountPreference;
	private boolean isCancel = false;
	private String mAccountUserId = "";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.system_edit);
		mAccountPreference = (Preference) findPreference(PREFERENCE_SYNC_ACCOUNT_NAME);
		WizApiEventsListener.add(this);
	}

	@Override
	protected void onDestroy() {
		WizApiEventsListener.remove(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		freshUI();
	}

	boolean isEmptyAccount() {
		mAccountUserId = getSyncAccountName(this);
		String password = WizSQLite.getAccountPassword(this);
		return WizGlobals.isEmptyString(password)
				|| WizGlobals.isEmptyString(mAccountUserId);

	}

	private void loadAccountPreference() {

		mAccountPreference.setEnabled(isEmptyAccount());
		mAccountPreference
				.setTitle(getString(R.string.preferences_account_title));
		mAccountPreference.setSummary(getString(
				R.string.preferences_account_summary_wiz, mAccountUserId));
		mAccountPreference.setOnPreferenceClickListener(mPreferenceOnClick);
	}

	android.preference.Preference.OnPreferenceClickListener mPreferenceOnClick = new android.preference.Preference.OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			if (isEmptyAccount()) {
				setupAccount();
			} else {
				WizGlobals.showMessage(
						SystemEditActivity.this,
						getString(
								R.string.preferences_toast_success_set_accout,
								mAccountUserId));
			}
			return true;
		}
	};

	private final static int WIZ_SETUP_ACCOUNT = 101;

	void setupAccount() {
		Intent intent = new Intent();
		intent.setClass(this, WelcomeActivity.class);
		startActivityForResult(intent, WIZ_SETUP_ACCOUNT);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WIZ_SETUP_ACCOUNT && resultCode == 1) {
			mAccountUserId = getSyncAccountName(SystemEditActivity.this);
		}
	}

	private void freshUI() {
		mAccountUserId = getSyncAccountName(this);
		loadAccountPreference();
		// loadSyncButton();
	}

	public static void setLastSyncTime(Context context, long time) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(PREFERENCE_LAST_SYNC_TIME, time);
		editor.commit();
	}

	public static String getSyncAccountName(Context context) {
		return WizSQLite.getAccountUserId(context);
	}

	void sync() {
		startSync();
	}

	void startSync() {
		if (WizGlobals.isEmptyString(mAccountUserId))
			return;
		if (isCancel)
			return;
		WizSync syncOld = WizGlobalData.sharedData().getSyncThread(this,
				mAccountUserId, false);
		if (syncOld.isAlive()) {
			isCancel = true;
			syncOld.stopSync();
			return;
		}
		//
		WizSync syncNew = WizGlobalData.sharedData().getSyncThread(this,
				mAccountUserId, true);
		syncNew.start();
	}

	boolean isSyncing() {
		WizSync syncOld = WizGlobalData.sharedData().getSyncThread(this,
				mAccountUserId, false);
		return syncOld.isAlive();
	}

	public void onApiBegin(String userId, String actionName) {

	}

	public void onShowMessage(String userId, String actionName, int arg1,
			int arg2, String mMessage) {
		if (WizGlobals.isEmptyString(userId))
			return;
		if (actionName.equals(WizGlobals.ActionNameForSync)) {
		}
	}

	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		if (WizGlobals.isEmptyString(userId))
			return;
		if (actionName.equals(WizGlobals.ActionNameForSync) && succeeded) {
			setLastSyncTime(this, System.currentTimeMillis());
		}

	}

	public void onApiError(String userId, String actionName, int stringID,
			String errorMessage) {
		if (WizGlobals.isEmptyString(userId))
			return;
		if (actionName.equals(WizGlobals.ActionNameForSync)) {
			setLastSyncTime(this, System.currentTimeMillis());
		}

	}
}
