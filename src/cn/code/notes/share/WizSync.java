package cn.code.notes.share;

import java.util.ArrayList;

import cn.code.notes.R;

import android.content.Context;

/**
 * 同步刷新
 * 
 * @author iwom
 */
@SuppressWarnings("unused")
public class WizSync extends WizXmlRpcThread {
	private String mAccountUserId;
	// private boolean mWifiActivity = false;
	private boolean mStop = false;
	private Context mContext;
	private int mWifiOnlyDownLoad = 1;

	public WizSync(Context ctx, String accountUserId, String password) {
		super(ctx, accountUserId, password, "WizSync");
		mAccountUserId = accountUserId;
		mContext = ctx;

	}

	synchronized public boolean isStopingSync() {
		return mStop;
	}

	synchronized public void stopSync() {
		mStop = true;
		mApi.stopSync();
	}

	private boolean workEx() {

		// 登陆
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.wait_for_login, "");
			if (null == mApi.callClientLogin())
				return false;
		}
		// 同步服务器端删除文件列表
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.download_deleted_document_list, "");
			long deletedListVersion = WizSQLite.getDeletedGUIDVersion(mContext);
			if (null == mApi.callDownloadDeletedList(deletedListVersion))
				return false;
		}

		// 同步本地端是否删除文件列表
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.upload_deleted_document_list, "");
			if (null == mApi.callUploadDeletedGUIDs(WizSQLite
					.getAllDeletedGuid(mContext)))
				return false;
		}

		/* 查询最大的文档version；用以同步服务器端文档的修改列表 */
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.download_document_list, "");
			long documentVersion = WizSQLite.getDocumentVersion(mContext);
			if (null == mApi.callDownloadDocumentList(/* true, */documentVersion))
				return false;
		}

		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.upload_document_list, "");
			// 同步上传文档数据
			if (!uploadDocumentData())
				return false;
		}
		return true;
	}

	@Override
	protected boolean work() {

		try {
			boolean ret = workEx();
			if (isStopingSync())
				return true;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 退出登录
			mApi.callClientLogout();
		}
	}

	// 上传数据
	private boolean uploadDocumentData() {
		ArrayList<WizDocument> documents = WizSQLite
				.getDocumentArrayForUpdate(mContext);

		if (documents == null || documents.size() == 0)
			return true;

		for (int i = 0; i < documents.size(); i++) {
			WizDocument doc = documents.get(i);
			if (isStopingSync())
				break;

			mApi.sendSyncMessage(R.string.upload_data, doc.title);

			if (null == mApi.uploadDocument(doc))
				return false;
		}
		return true;
	}

	static final String data_doc_type = "document";
	static final String data_att_type = "attachment";

	private final static int mWifiDownloadAllData = -1;
	private final static int mWifiDownloadNone = 0;
	private final static int mWifiDownloadRecentData = 1;

	private void downloadAllData() {
		try {
			ArrayList<String> arr = new ArrayList<String>();
			if (mWifiOnlyDownLoad == mWifiDownloadRecentData) {
				arr = WizIndex.getDocumentsForDownloadByTime(mContext,
						mAccountUserId, 1);
			} else if (mWifiOnlyDownLoad == mWifiDownloadAllData) {

				arr = WizIndex.getAllDocumentsForDownload(mContext,
						mAccountUserId);
			}
			if (arr != null) {

				int arrLength = arr.size();
				for (int i = 0; i < arrLength; i++) {
					if (isStopingSync())
						break;

					String documentGUID = arr.get(i);
					WizDocument doc = WizIndex.documentFromGUID(mContext,
							mAccountUserId, documentGUID);
					if (null == mApi.downloadDocument(doc))
						continue;

					if (!WizIndex.setDocumentServerChanged(mContext,
							mAccountUserId, documentGUID, false))
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
