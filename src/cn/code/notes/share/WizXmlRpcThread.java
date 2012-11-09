package cn.code.notes.share;

import android.content.Context;

public abstract class WizXmlRpcThread extends Thread {
	protected WizApi mApi;

	public WizXmlRpcThread(Context ctx, String accountUserId, String password,
			String actionName) {
		WizApiHandler handler = new WizApiHandler(accountUserId, actionName);
		mApi = new WizApi(ctx, accountUserId, password, handler);
	}

	public void run() {
		boolean ret = false;
		try {
			mApi.sendBeginMessage();
			ret = work();
		} catch (java.lang.NullPointerException e) {
			mApi.sendErrorMessage(0, "Null Pointer");
		} catch (Exception e) {
			mApi.sendErrorMessage(0, e.getMessage());
		} finally {
			mApi.sendEndMessage(ret);
		}
	}

	abstract protected boolean work();
}
