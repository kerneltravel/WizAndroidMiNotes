package cn.code.notes.share;

import android.content.Context;

public class WizVerifyAccount extends WizXmlRpcThread {
	public WizVerifyAccount(Context ctx, String accountUserId, String password) {
		super(ctx, accountUserId, password, "WizVerifyAccount");
	}

	@Override
	protected boolean work() {
		if (null == mApi.callClientLogin())
			return false;
		mApi.callClientLogout();
		return true;
	}
}
