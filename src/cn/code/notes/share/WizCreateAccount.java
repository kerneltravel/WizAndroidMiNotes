package cn.code.notes.share;

import android.content.Context;

public class WizCreateAccount extends WizXmlRpcThread {
	private boolean isPhone = true;

	public WizCreateAccount(Context ctx, String accountUserId, String password,
			boolean mIsPhone) {
		super(ctx, accountUserId, password, "WizCreateAccount");
		isPhone = mIsPhone;
	}

	@Override
	protected boolean work() {
		if (null == mApi.callCreateAccount(isPhone))
			return false;
		return true;
	}
}
