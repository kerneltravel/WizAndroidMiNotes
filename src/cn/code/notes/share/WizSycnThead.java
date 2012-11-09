package cn.code.notes.share;

import android.content.Context;

public class WizSycnThead extends Thread {
	protected WizApi mApi;
	private String mAccountUserId;
	private Context mContext;
	private String mAccountPassword;
	// private String mActionName;
	private int isSyncTime;

	public WizSycnThead(Context ctx, String accountUserId, String password,
	/* String actionName, */int syncTime) {
		mAccountUserId = accountUserId;
		mAccountPassword = password;
		mContext = ctx;
		// mActionName = actionName;
		isSyncTime = syncTime;
	}

	@Override
	public void run() {

		try {
			while (isSyncTime > 0) {
				WizSync sync = new WizSync(mContext, mAccountUserId,
						mAccountPassword);

				sync.start();
				long sleepTime = isSyncTime * 60 * 60 * 1000;
				Thread.sleep(sleepTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}