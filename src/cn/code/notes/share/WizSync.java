package cn.code.notes.share;

import java.util.ArrayList;

import cn.code.notes.R;

import android.content.Context;

/**
 * ͬ��ˢ��
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

		// ��½
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.wait_for_login, "");
			if (null == mApi.callClientLogin())
				return false;
		}
		// if (!isStopingSync()) {
		// // mApi.sendSyncMessage(R.string.wait_for_login, "");
		// if (null == mApi.callGetCert())
		// return false;
		// }
		// ͬ����������ɾ���ļ��б�
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.download_deleted_document_list, "");
			long deletedListVersion = WizSQLite.getDeletedGUIDVersion(mContext);
			if (null == mApi.callDownloadDeletedList(deletedListVersion))
				return false;
		}

		// ͬ�����ض��Ƿ�ɾ���ļ��б�
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.upload_deleted_document_list, "");
			if (null == mApi.callUploadDeletedGUIDs(WizSQLite
					.getAllDeletedGuid(mContext)))
				return false;
		}
		//
		// // ͬ���������˵�Ŀ¼
		// if (!isStopingSync()) {
		// // mApi.sendSyncMessage(R.string.download_location_list, "");
		// if (null == mApi.callAllCategories())
		// return false;
		// }
		//
		// // ͬ�����������޸ġ��½��ı�ǩ
		// if (!isStopingSync()) {
		// // mApi.sendSyncMessage(R.string.download_tag_list, "");
		// long tagVersion = WizIndex.getTAGVersion(mContext, mAccountUserId);
		// if (null == mApi.callAllTags(tagVersion))
		// return false;
		// }
		//
		// // ͬ���ϴ������½����޸ĵı�ǩ
		// if (!isStopingSync()) {
		// // mApi.sendSyncMessage(R.string.upload_tag_list, "");
		// if (null == mApi.callUploadTags())
		// return false;
		// }

		/* ��ѯ�����ĵ�version������ͬ�����������ĵ����޸��б� */
		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.download_document_list, "");
			long documentVersion = WizSQLite.getDocumentVersion(mContext);
			if (null == mApi.callDownloadDocumentList(/* true, */documentVersion))
				return false;
		}

		if (!isStopingSync()) {
			mApi.sendSyncMessage(R.string.upload_document_list, "");
			// ͬ���ϴ��ĵ�����
			if (!uploadDocumentData())
				return false;
		}
		/* ��ѯ���ĸ���version������ͬ���������˸������޸��б� */
		// if (!isStopingSync()) {
		// mApi.sendSyncMessage(R.string.download_attachment_list, "");
		// long attachmentVersion = WizIndex.getAttachmentVersion(mContext,
		// mAccountUserId);
		// if (null == mApi.callAllAttachments(attachmentVersion))
		// return false;
		// }
		//
		// if (!isStopingSync()) {
		// mApi.sendSyncMessage(R.string.upload_attachment_list, "");
		// ͬ���ϴ���������
		// if (!uploadAttachmentData())
		// return false;
		// }
		// if (!isStopingSync()) {
		// // �ж�wifi�Ƿ��
		// if (mWifiActivity && mWifiOnlyDownLoad != mWifiDownloadNone) {
		// // mApi.sendSyncMessage(R.string.upload_document_by_wifi, "");
		//
		// downloadAllData();
		// }
		// }
		//
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
			// �˳���¼
			mApi.callClientLogout();
		}
	}

	// �ϴ�����
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
	// private boolean uploadAttachmentData() {
	// ArrayList<WizAttachment> attachments = WizIndex
	// .getAttachmentsForUpdate(mContext, mAccountUserId);
	// //
	// for (int i = 0; i < attachments.size(); i++) {
	// WizAttachment att = attachments.get(i);
	// // �����ȡ�������ļ��ᵼ����Ƶ�޷��ϴ�
	// String orgFileName = WizIndex.getAttachmentOrgFileName(mContext,
	// mAccountUserId, att.docGuid, att.name);
	// //
	// if (isStopingSync())
	// break;
	// // mApi.sendSyncMessage(R.string.upload_data, att.name);
	// if (null == mApi.uploadAttachment(att, orgFileName))
	// return false;
	// }
	// //
	// return true;
	// }

	private final static int mWifiDownloadAllData = -1;
	private final static int mWifiDownloadNone = 0;
	private final static int mWifiDownloadRecentData = 1;

	// ������������
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
					// String mDownloasMessage = doc.title + "  (" + (i + 1) +
					// "/"
					// + arrLength + ")  " + ":0%";
					// mApi.sendSyncMessage(R.string.download_document,
					// mDownloasMessage);
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
