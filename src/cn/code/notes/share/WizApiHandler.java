package cn.code.notes.share;

import java.util.ArrayList;

import android.os.Message;

public class WizApiHandler extends android.os.Handler {
	protected String mAccountUserId;
	protected String mActionName;

	public WizApiHandler(String accountUserId, String actionName) {
		mAccountUserId = accountUserId;
		mActionName = actionName;
	}

	@Override
	public void handleMessage(Message msg) {
		try {
			if (msg.arg1 == 0) // succeeded
			{
				if (msg.obj == null)
					throw new Exception("XmlRpc return null");
				//
				// WizGlobals.showMessage(Integer.toString(msg.what));
				//
				switch (msg.what) {
				case WizApi.Api_Begin:
					onBegin();
					break;
				case WizApi.Api_End:
					onEnd(msg.arg2 == 0);
					break;
				case WizApi.Api_Message:
					onSyncMessage(msg.arg1, msg.arg2, msg.obj);
					break;
				case WizApi.ApiMethodID_ClientLogin:
					onClientLogin(msg.obj);
					break;
				case WizApi.ApiMethodID_ClientLogout:
					onClientLogout(msg.obj);
					break;
				case WizApi.ApiMethodID_CreateAccount:
					onCreateAccount(msg.obj);
					break;
				case WizApi.ApiMethodID_GetAllCategories:
					onAllCategories(msg.obj);
					break;
				case WizApi.ApiMethodID_GetAllTags:
					onAllTags(msg.obj);
					break;
				case WizApi.ApiMethodID_GetAllAttachments:
					onAllAttachments(msg.obj);
					break;
				case WizApi.ApiMethodID_DownloadDocumentList:
					onDownloadDocumentList(msg.obj);
					break;
				case WizApi.ApiMethodID_DocumentsByCategory:
					onDocumentsByCategory(msg.obj);
					break;
				case WizApi.ApiMethodID_DocumentsByTag:
					onDocumentsByTag(msg.obj);
					break;
				// case WizApi.ApiMethodID_DownloadMobileData:
				// onDownloadMobileData(msg.obj);
				// break;
				case WizApi.ApiMethodID_UploadData:
					onUploadData(msg.obj);
					break;
				case WizApi.ApiMethodID_UploadDocumentInfo:
					onUploadDocumentInfo(msg.obj);
					break;
				case WizApi.ApiMethodID_UploadAttachmentInfo:
					onUploadAttachmentInfo(msg.obj);
					break;
				case WizApi.ApiMethodID_UploadDeletedList:
					onUploadDeletedGUIDs(msg.obj);
					break;
				case WizApi.ApiMethodID_DownloadDeletedList:
					onDownloadDeletedList(msg.obj);
					break;
				case WizApi.ApiMethodID_DownloadData:
					onDownloadData(msg.obj);
					break;
				case WizApi.ApiMethodID_DocumentsByKey:
					onDocumentsByKey(msg.obj);
					break;
				case WizApi.ApiMethodID_UploadTags:
					onUploadTag(msg.obj);
					break;
				}
			} else {
				onXmlRpcError(msg.what, msg.arg2, (String) msg.obj);
			}
		} catch (Exception err) {
			onXmlRpcError(msg.what, 0, err.getMessage());
		}
	}

	void onXmlRpcError(int what, int stringID, String error) {
		WizApiEventsListener.sharedListener().onApiError(mAccountUserId,
				mActionName, stringID, error);
	}

	void onBegin() {
		WizApiEventsListener.sharedListener().onApiBegin(mAccountUserId,
				mActionName);
	}

	void onEnd(boolean succeeded) {
		WizApiEventsListener.sharedListener().onApiEnd(mAccountUserId,
				mActionName, succeeded);
	}

	void onSyncMessage(int arg1, int arg2, Object obj) {
		WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
				mActionName, arg1, arg2, obj.toString());
	}

	void onClientLogin(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String) obj);
	}

	void onClientLogout(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onAllCategories(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	//

	void onAllTags(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onAllAttachments(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onDownloadDocumentList(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onDownloadDeletedList(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onDownloadData(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onDocumentsByKey(Object obj) {
	}

	void onUploadTag(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onDocumentsByCategory(Object obj) {
	}

	void onDocumentsByTag(Object obj) {
	}

	void onDownloadMobileData(Object obj) {
	}

	void onUploadData(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onUploadDocumentInfo(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onUploadAttachmentInfo(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onUploadDeletedGUIDs(Object obj) {
		// WizApiEventsListener.sharedListener().onShowMessage(mAccountUserId,
		// mActionName, (String)obj);
	}

	void onCreateAccount(Object obj) {
	}

	//
	void updateDocumentList(ArrayList<WizDocument> documents) {

	}
}
