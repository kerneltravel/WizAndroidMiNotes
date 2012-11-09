package cn.code.notes.share;

import android.content.Context;

public class WizDownloadAttachment extends WizXmlRpcThread {
	String mAttachmentGUID;
	String mAccountUsrId;
	private Context mContext;

	public WizDownloadAttachment(Context ctx, String accountUserId,
			String password, String attachmentGUID) {
		super(ctx, accountUserId, password, "WizDownloadAttachment");
		mAccountUsrId = accountUserId;
		mAttachmentGUID = attachmentGUID;
		mContext = ctx;
	}

	static final String data_att_type = "attachment";

	@Override
	protected boolean work() {

		// ÅÐ¶ÏµÇÂ¼
		if (null == mApi.callClientLogin())
			return false;
		//
		WizAttachment att = WizIndex.getWizAttachmentByGuid(mContext,
				mAccountUsrId, mAttachmentGUID);
		if (null == mApi.downloadAttachment(att))
			return false;
		mApi.callClientLogout();
		//
		return true;
	}
}
