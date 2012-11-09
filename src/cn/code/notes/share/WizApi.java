package cn.code.notes.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import cn.code.notes.R;

import android.content.Context;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcNetworkException;
import redstone.xmlrpc.XmlRpcStruct;

@SuppressWarnings("unchecked")
public class WizApi {
	private boolean mApiStop = false;

	public static class WizApiException extends Exception {
		private static final long serialVersionUID = 1L;
		private int mStringID = 0;

		public WizApiException(int id, String err) {
			super(err);
			mStringID = id;
		}

		public int getStringID() {
			return mStringID;
		}
		/*
		 * public String getString(Context ctx) { if (0 == mStringID) return
		 * this.getMessage(); else if (-1 == mStringID) return ""; // try {
		 * return WizGlobals.getResourcesString(ctx, mStringID); } catch
		 * (Exception e) { return this.getMessage(); } }
		 */
	}

	public static class WizApiUserCancelException extends WizApiException {
		private static final long serialVersionUID = 1L;

		public WizApiUserCancelException() {
			super(R.string.wiz_exception_user_canceled, "User canceled");
		}
	}

	public void stopSync() {
		mApiStop = true;
	}

	public boolean isStoping() {
		return mApiStop;
	}

	//
	final static public String ApiMethodName_ClientLogin = "accounts.clientLogin";
	final static public String ApiMethodName_ClientLogout = "accounts.clientLogout";
	final static public String ApiMethodName_CreateAccount = "accounts.createAccount";
	final static public String ApiMethodName_GetAllCategories = "category.getAll";
	final static public String ApiMethodName_GetAllTags = "tag.getList";
	final static public String ApiMethodName_GetAllAttachments = "attachment.getList";
	final static public String ApiMethodName_DownloadDocumentList = "document.getSimpleList";
	final static public String ApiMethodName_DocumentsByCategory = "document.getSimpleListByCategory";
	final static public String ApiMethodName_DocumentsByTag = "document.getSimpleListByTag";
	final static public String ApiMethodName_DownloadData = "data.download";
	final static public String ApiMethodName_DownloadDeletedList = "deleted.getList";
	final static public String ApiMethodName_DocumentsByKey = "document.getSimpleListByKey";
	final static public String ApiMethodName_UploadDeletedList = "deleted.postList";
	final static public String ApiMethodName_UploadData = "data.upload";
	final static public String ApiMethodName_UploadDocumentInfo = "document.postSimpleData";
	final static public String ApiMethodName_UploadAttachmentInfo = "attachment.postSimpleData";
	final static public String ApiMethodName_UploadTags = "tag.postList";
	final static public String ApiMethodName_GetCert = "accounts.getCert";

	final static public int Api_Begin = 100;
	final static public int Api_End = 101;
	final static public int Api_Message = 102;
	final static public int ApiMethodID_ClientLogin = 1000;
	final static public int ApiMethodID_ClientLogout = 1001;
	final static public int ApiMethodID_CreateAccount = 1002;
	final static public int ApiMethodID_GetAllCategories = 1003;
	final static public int ApiMethodID_GetAllTags = 1004;
	final static public int ApiMethodID_GetAllAttachments = 1005;
	final static public int ApiMethodID_DownloadDocumentList = 1006;
	final static public int ApiMethodID_DocumentsByCategory = 1007;
	final static public int ApiMethodID_DocumentsByTag = 1008;
	final static public int ApiMethodID_UploadData = 1009;
	final static public int ApiMethodID_UploadDocumentInfo = 1010;
	final static public int ApiMethodID_UploadAttachmentInfo = 1011;
	final static public int ApiMethodID_UploadDeletedList = 1012;
	final static public int ApiMethodID_DownloadDeletedList = 1013;
	final static public int ApiMethodID_DocumentsByKey = 1014;
	final static public int ApiMethodID_DownloadData = 1015;
	final static public int ApiMethodID_UploadTags = 1016;
	final static public int ApiMethodID_GetCert = 1017;

	protected String mAccountUserId;
	protected Context mContext;
	protected String mAccountPassword;
	protected String mToken = null;
	protected String mKbGUID = null;
	protected String mCurrentXmlRpcMethod;
	protected android.os.Handler mHandler;

	WizApi(Context ctx, String accountUserId, String password,
			android.os.Handler handler) {
		mAccountUserId = accountUserId;
		mAccountPassword = password;
		mHandler = handler;
		mContext = ctx;
	}

	public static int xmlrpcMethodNameToID(String name) {
		try {
			if (name.equals(ApiMethodName_ClientLogin))
				return ApiMethodID_ClientLogin;
			if (name.equals(ApiMethodName_ClientLogout))
				return ApiMethodID_ClientLogout;
			if (name.equals(ApiMethodName_CreateAccount))
				return ApiMethodID_CreateAccount;
			if (name.equals(ApiMethodName_GetAllCategories))
				return ApiMethodID_GetAllCategories;
			if (name.equals(ApiMethodName_GetAllTags))
				return ApiMethodID_GetAllTags;
			if (name.equals(ApiMethodName_GetAllAttachments))
				return ApiMethodID_GetAllAttachments;
			if (name.equals(ApiMethodName_DownloadDocumentList))
				return ApiMethodID_DownloadDocumentList;
			if (name.equals(ApiMethodName_DocumentsByCategory))
				return ApiMethodID_DocumentsByCategory;
			if (name.equals(ApiMethodName_DocumentsByTag))
				return ApiMethodID_DocumentsByTag;
			if (name.equals(ApiMethodName_UploadDocumentInfo))
				return ApiMethodID_UploadDocumentInfo;
			if (name.equals(ApiMethodName_DownloadDeletedList))
				return ApiMethodID_DownloadDeletedList;
			if (name.equals(ApiMethodName_UploadDeletedList))
				return ApiMethodID_UploadDeletedList;
			if (name.equals(ApiMethodName_DocumentsByKey))
				return ApiMethodID_DocumentsByKey;
			if (name.equals(ApiMethodName_DownloadData))
				return ApiMethodID_DownloadData;
			if (name.equals(ApiMethodName_UploadData))
				return ApiMethodID_UploadData;
			if (name.equals(ApiMethodName_UploadAttachmentInfo))
				return ApiMethodID_UploadAttachmentInfo;
			if (name.equals(ApiMethodName_UploadTags))
				return ApiMethodID_UploadTags;
			if (name.equals(ApiMethodName_GetCert))
				return ApiMethodID_GetCert;
			//
			return 0;
		} catch (NullPointerException e) {
			return ApiMethodID_DownloadDeletedList;
		} catch (Exception e) {
			return 0;
		}
	}

	void addCommonParams(XmlRpcStruct postParams) {
		postParams.put("client_type", "android");
		postParams.put("program_type", "normal");
		postParams.put("api_version", "4");
		//
		if (mToken != null && mToken.length() > 0) {
			postParams.put("token", mToken);
		}
		if (mKbGUID != null && mKbGUID.length() > 0) {
			postParams.put("kb_guid", mKbGUID);
		}
	}

	//
	private String mDatabaseServerURL;
	private XmlRpcClient mAccountsServer;
	private XmlRpcClient mDatabaseServer;

	// 产生一个XmlRpcClient类型的数据（Url地址）
	synchronized XmlRpcClient getAccountsServer() {
		try {
			if (mAccountsServer != null)
				return mAccountsServer;
			//
			mAccountsServer = new XmlRpcClient(mContext,
					"http://service.wiz.cn/wizkm/xmlrpc", false);
			return mAccountsServer;
		} catch (Exception e) {
		}
		return null;
	}

	// 返回一个XmlRpcClient其中对象包含一个Url
	synchronized XmlRpcClient getDatabaseServer() {
		if (mDatabaseServer != null)
			return mDatabaseServer;
		//
		if (mDatabaseServerURL == null || mDatabaseServerURL.length() == 0)
			return null;
		try {
			mDatabaseServer = new XmlRpcClient(mContext, mDatabaseServerURL,
					false);
			return mDatabaseServer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object callXmlRpcMethod(XmlRpcClient server, String method,
			XmlRpcStruct postParams) throws WizApiException {

		for (int i = 0; i < 3; i++) {
			mCurrentXmlRpcMethod = method;
			//
			if (server == null)
				throw new WizApiException(0, "server is null");
			if (method == null)
				throw new WizApiException(0, "mthod is null");
			if (postParams == null)
				throw new WizApiException(0, "params is null");
			//
			try {
				Object ret = server.invoke(method, new Object[] { postParams });
				return ret;
			} catch (XmlRpcFault err) {
				throw new WizApiException(0, err.getMessage());
			} catch (XmlRpcNetworkException err) {
				continue;
			} catch (XmlRpcException err) {
				throw new WizApiException(0, err.getMessage());
			} catch (Exception err) {
				throw new WizApiException(0, err.getMessage());
			} catch (java.lang.Error err) {
				throw new WizApiException(0, err.getMessage());
			}
		}
		//
		throw new WizApiException(R.string.wiz_exception_network_error,
				"Network error");
	}

	private Object callAccountsServerMethod(String method,
			XmlRpcStruct postParams) throws WizApiException {
		return callXmlRpcMethod(getAccountsServer(), method, postParams);
	}

	// method是一个关键字标识操作类型postParams是一个hash数组
	private Object callDatabaseServerMethod(String method,
			XmlRpcStruct postParams) throws WizApiException {
		return callXmlRpcMethod(getDatabaseServer(), method, postParams);
	}

	//
	int getCurrentMethodID() {
		return xmlrpcMethodNameToID(mCurrentXmlRpcMethod);
	}

	void sendSucceededMessage(Object objRet) {
		android.os.Message msg = new android.os.Message();
		msg.what = getCurrentMethodID();
		msg.obj = objRet;
		msg.arg1 = 0;
		msg.arg2 = 0;
		//
		mHandler.sendMessage(msg);
	}

	public void sendSyncMessage(int stringId, String syncMessage) {
		android.os.Message msg = new android.os.Message();
		msg.what = Api_Message;
		msg.obj = syncMessage;
		msg.arg1 = 0;
		msg.arg2 = stringId;
		//
		mHandler.sendMessage(msg);
	}

	void sendErrorMessage(int stringID, String errorMessage) {
		android.os.Message msg = new android.os.Message();
		msg.what = getCurrentMethodID();
		msg.obj = errorMessage + "(" + mCurrentXmlRpcMethod + ")";
		msg.arg1 = 1;
		msg.arg2 = stringID;
		//
		mHandler.sendMessage(msg);
	}

	void sendBeginMessage() {
		android.os.Message msg = new android.os.Message();
		msg.what = Api_Begin;
		msg.obj = "Api begin";
		msg.arg1 = 0;
		msg.arg2 = 0;
		mHandler.sendMessage(msg);
	}

	void sendEndMessage(boolean succeeded) {
		android.os.Message msg = new android.os.Message();
		msg.what = Api_End;
		msg.obj = "Api End";
		msg.arg1 = 0;
		msg.arg2 = succeeded ? 0 : 1;
		//
		mHandler.sendMessage(msg);
	}

	// 获取到WizDocument的对象列表
	protected ArrayList<WizDocument> objToDocuments(Object obj) {
		XmlRpcArray arr = (XmlRpcArray) obj;
		//
		ArrayList<WizDocument> ret = new ArrayList<WizDocument>();
		//
		for (int i = 0; i < arr.size(); i++) {
			Object elem = arr.get(i);
			XmlRpcStruct data = (XmlRpcStruct) elem;
			WizDocument doc = new WizDocument();
			//
			doc.guid = data.getString("document_guid");
			doc.title = data.getString("document_title");
			doc.location = data.getString("document_location");
			doc.dataMd5 = data.getString("data_md5");
			doc.url = data.getString("document_url");
			doc.tagGUIDs = data.getString("document_tag_guids");
			doc.dateCreated = WizGlobals.getSQLDateTimeString(data
					.getDate("dt_created"));
			doc.dateModified = WizGlobals.getSQLDateTimeString(data
					.getDate("dt_modified"));
			doc.attachmentCount = Integer.parseInt(data
					.getString("document_attachment_count"));
			doc.type = data.getString("document_type");
			doc.fileType = data.getString("document_filetype");
			doc.version = Long.parseLong(data.getString("version"), 10);
			//
			if (doc.guid == null || doc.guid.length() == 0)
				continue;
			//
			ret.add(doc);
		}
		//
		return ret;
	}

	// 获取到WizTag的对象列表
	protected ArrayList<WizTag> objToTags(Object obj) {
		// 将获取网络上的Tag的信息的信息添加给arrayList
		XmlRpcArray arr = (XmlRpcArray) obj;
		//
		ArrayList<WizTag> ret = new ArrayList<WizTag>();
		//
		for (int i = 0; i < arr.size(); i++) {
			Object elem = arr.get(i);
			XmlRpcStruct data = (XmlRpcStruct) elem;
			WizTag tag = new WizTag();
			//
			tag.guid = data.getString("tag_guid");
			tag.name = data.getString("tag_name");
			tag.parentGuid = data.getString("tag_group_guid");
			tag.description = data.getString("tag_description");
			tag.version = Long.parseLong(data.getString("version"), 10);
			tag.dateModified = WizGlobals.getSQLDateTimeString(data
					.getDate("dt_info_modified"));
			//
			if (tag.guid == null || tag.guid.length() == 0)
				continue;
			//
			ret.add(tag);
		}
		//
		return ret;
	}

	// 获取到WizTag的对象列表
	protected WizCretData objToWizCret(XmlRpcStruct ret) {

		//
		WizCretData data = new WizCretData();
		//
		data.setmRsaN(ret.getString("n"));
		data.setmRsaE(ret.getString("e"));
		data.setmEncryptedD(ret.getString("d"));
		data.setmHINT(ret.getString("hint"));

		//
		return data;
	}

	// 获取到WizAttachment的对象列表
	protected ArrayList<WizAttachment> objToAttachments(Object obj) {
		// 将获取网络上的Tag的信息的信息添加给arrayList
		XmlRpcArray arr = (XmlRpcArray) obj;
		//
		ArrayList<WizAttachment> ret = new ArrayList<WizAttachment>();
		//
		for (int i = 0; i < arr.size(); i++) {
			Object elem = arr.get(i);
			XmlRpcStruct data = (XmlRpcStruct) elem;
			WizAttachment attachment = new WizAttachment();

			attachment.guid = data.getString("attachment_guid");
			attachment.name = data.getString("attachment_name");
			attachment.docGuid = data.getString("attachment_document_guid");
			attachment.version = Long.parseLong(data.getString("version"), 10);
			attachment.dataMd5 = data.getString("data_md5");
			attachment.dateModified = WizGlobals.getSQLDateTimeString(data
					.getDate("dt_data_modified"));
			attachment.description = data.getString("attachment_description");

			//
			if (attachment.guid == null || attachment.guid.length() == 0)
				continue;
			//
			ret.add(attachment);
		}
		//
		return ret;
	}

	protected ArrayList<WizDeletedGUID> objToDeletedGUIDs(Object obj) {
		XmlRpcArray arr = (XmlRpcArray) obj;
		//
		ArrayList<WizDeletedGUID> ret = new ArrayList<WizDeletedGUID>();
		//
		for (int i = 0; i < arr.size(); i++) {
			Object elem = arr.get(i);
			XmlRpcStruct data = (XmlRpcStruct) elem;
			WizDeletedGUID guid = new WizDeletedGUID();
			//
			guid.guid = data.getString("deleted_guid");
			guid.type = data.getString("guid_type");
			guid.version = Long.parseLong(data.getString("version"), 10);

			//
			if (guid.guid == null || guid.guid.length() == 0)
				continue;
			//
			ret.add(guid);
		}
		//
		return ret;
	}

	//

	int getListCount() {
		return 200;
	}

	//

	Object onXmlRpcReturn(Object ret) {
		sendSucceededMessage(ret);
		return ret;
	}

	Object onXmlRpcFailed(WizApiException err) {
		sendErrorMessage(err.getStringID(), err.getMessage());
		return null;
	}

	Object onXmlRpcFailed(Exception err) {
		sendErrorMessage(0, err.getMessage());
		return null;
	}

	// 登陆返回object值
	Object callClientLogin() {
		try {
			/* 判断网络状态 */
			if (!WizGlobals.isWifi(mContext)
					&& !WizGlobals.isCMWAPNetwork(mContext)) {
				mCurrentXmlRpcMethod = ApiMethodName_ClientLogin;
				throw new WizApiException(R.string.wiz_no_network,
						"Network unavaliable!");
			}

			if (WizGlobals.isEmptyString(mAccountUserId))
				throw new WizApiException(0, "No User ID");
			//
			if (WizGlobals.isEmptyString(mAccountPassword))
				mAccountPassword = WizSQLite.getAccountPassword(mContext);

			if (WizGlobals.isEmptyString(mAccountPassword))
				throw new WizApiException(0, "No Password");
			//
			mToken = null;
			XmlRpcStruct postParams = new XmlRpcStruct();// XmlRpcStruct->HashMap

			addCommonParams(postParams);
			postParams.put("user_id", mAccountUserId);
			postParams.put("password",
					WizGlobals.makeMD5Password(mAccountPassword));
			//
			Object ret = callAccountsServerMethod(ApiMethodName_ClientLogin,
					postParams);
			//
			XmlRpcStruct obj = (XmlRpcStruct) ret;

			// WizGlobals.setDataForLogin(mContext, mAccountUserId, obj);

			mKbGUID = obj.getString("kb_guid");
			mToken = obj.getString("token");
			mDatabaseServerURL = obj.getString("kapi_url");
			//
			return onXmlRpcReturn(ret);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	//
	Object callClientLogout() {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(-1, "Not login");
			}
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			//
			Object ret = callAccountsServerMethod(ApiMethodName_ClientLogout,
					postParams);
			//
			mKbGUID = null;
			mToken = null;
			mDatabaseServerURL = null;
			mDatabaseServer = null;
			//
			return onXmlRpcReturn(ret);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callAllCategories() {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			//
			XmlRpcStruct ret = (XmlRpcStruct) callDatabaseServerMethod(
					ApiMethodName_GetAllCategories, postParams);
			if (ret == null)
				throw new WizApiException(
						R.string.wiz_exception_can_not_get_folders,
						"Failed to call category.getAll");
			//
			String categories = ret.getString("categories") + "*/My Mobiles/";
			String[] arr = categories.split("\\*");
			//
			WizIndex.updateCategories(mContext, mAccountUserId, arr);
			//
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callAllTags(long version) {

		boolean tagsdataListEof = false;
		ArrayList<WizTag> arr = null;
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("count", getListCount());

			while (!isStoping() && !tagsdataListEof) {
				postParams.put("version", version);

				Object ret = callDatabaseServerMethod(ApiMethodName_GetAllTags,
						postParams);
				arr = objToTags(ret);
				if (arr == null || arr.size() < getListCount()) {
					tagsdataListEof = true;
				}
				long newVer = 0;
				for (int i = 0; i < arr.size(); i++) {
					newVer = Math.max(newVer, arr.get(i).version);
				}

				if (newVer >= version) {

					// 更新Tag的数据库表
					WizIndex.updateTags(mContext, mAccountUserId, arr, false);

					WizIndex.updateTagVersion(mContext, mAccountUserId, newVer,
							version);
					version = newVer + 1;
				}
			}
			//
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callUploadTags() {

		boolean tagUploadListEof = false;
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			//
			while (!isStoping() && !tagUploadListEof) {

				ArrayList<WizTag> arr = WizIndex.getTagForUpdate(mContext,
						mAccountUserId, 0);
				if (arr.size() == 0)
					break;

				XmlRpcArray wizTagArray = new XmlRpcArray();

				int tagCount = arr.size();
				if (tagCount < getListCount()) {
					tagUploadListEof = true;
				}
				for (int i = 0; i < tagCount; i++) {
					WizTag tag = arr.get(i);
					XmlRpcStruct objTag = new XmlRpcStruct();
					objTag.put("tag_guid", tag.guid);
					objTag.put("tag_group_guid", tag.parentGuid == null ? ""
							: tag.parentGuid);
					objTag.put("tag_name", tag.name == null ? "" : tag.name);
					objTag.put("tag_description", tag.description == null ? ""
							: tag.description);
					objTag.put(
							"dt_info_modified",
							tag.dateModified != null ? WizGlobals
									.getDateFromSqlDateTimeString(tag.dateModified)
									: new Date());
					wizTagArray.add(objTag);
				}

				addCommonParams(postParams);
				postParams.put("count", tagCount);
				postParams.put("tags", wizTagArray);

				callDatabaseServerMethod(ApiMethodName_UploadTags, postParams);

				// 更新Tag的数据库表
				WizIndex.updateTags(mContext, mAccountUserId, arr, false);

			}
			//
			postParams.remove("tags");
			return onXmlRpcReturn(postParams);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}

	}

	Object callAllAttachments(long version) {

		boolean attDataListEof = false;
		ArrayList<WizAttachment> arr = null;
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("count", getListCount());

			while (!isStoping() && !attDataListEof) {

				postParams.put("version", Long.toString(version));

				Object ret = callDatabaseServerMethod(
						ApiMethodName_GetAllAttachments, postParams);
				arr = objToAttachments(ret);
				if (arr == null || arr.size() < getListCount()) {
					attDataListEof = true;
				}

				long newVer = 0;
				for (int i = 0; i < arr.size(); i++) {
					newVer = Math.max(newVer, arr.get(i).version);
				}

				if (newVer >= version) {

					// 更新Attachments的数据库表
					WizIndex.updateAttachments(mContext, mAccountUserId, arr);

					// 更新Attachment的最高版本信息
					WizIndex.updateAttachmentsVersion(mContext, mAccountUserId,
							newVer, version);

					version = newVer + 1;
				}

			}
			//
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}

	}

	Object callDownloadDocumentList(long version) {
		int index = 0;
		boolean docDataListEof = false;
		ArrayList<WizDocument> arr = null;
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("count", getListCount());
			postParams.put("category", WizGlobals.TASK_ROOT_PATH);

			while (!isStoping() && !docDataListEof) {
				sendSyncMessage(R.string.download_document_list, (index
						* getListCount() + 1)
						+ "-" + (index + 1) * getListCount());
				index++;
				//
				postParams.put("version", Long.toString(version));
				Object ret = callDatabaseServerMethod(
						ApiMethodName_DownloadDocumentList, postParams);
				arr = objToDocuments(ret);
				if (arr == null || arr.size() < getListCount()) {
					docDataListEof = true;
				}

				long newVer = 0;
				for (int i = 0; i < arr.size(); i++) {
					newVer = Math.max(newVer, arr.get(i).version);
				}

				if (newVer >= version) {
					// 更新Document的数据表
					if (!downloadAllData(arr))
						return null;
					// 更新Document的最高版本信息
					newVer++;
					WizSQLite.updateDocumentsVersion(mContext, newVer);
					version = newVer;
				}
			}
			//
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	boolean downloadAllData(ArrayList<WizDocument> arr) {
		try {
			if (arr != null) {
				int arrLength = arr.size();
				for (int i = 0; i < arrLength; i++) {
					if (isStoping())
						break;
					WizDocument doc = arr.get(i);
					if (!WizSQLite.isDocumentServerChanged(mContext, doc.guid,
							doc.dataMd5))
						continue;
					if (null == downloadDocument(doc))
						return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	//
	Object callDownloadDeletedList(long version) {

		boolean dDeletedListEof = false;
		ArrayList<WizDeletedGUID> arr = null;
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			/**
			 * 新建一个hashMap,并向其中添加特定数据(addCommonParams(postParams))
			 */
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			/**
			 * 向hashMap中添加指定数据 getListCount()返回指定数量数据
			 */
			postParams.put("count", getListCount());

			while (!isStoping() && !dDeletedListEof) {

				postParams.put("version", Long.toString(version));

				Object ret = callDatabaseServerMethod(
						ApiMethodName_DownloadDeletedList, postParams);
				arr = objToDeletedGUIDs(ret);
				if (arr == null || arr.size() <= 0) {
					dDeletedListEof = true;
				} else {

					long newVer = 0;
					for (int i = 0; i < arr.size(); i++) {

						newVer = Math.max(newVer, arr.get(i).version);
					}
					if (newVer >= version) {
						WizSQLite.updateDeletedGUIDVersion(mContext, arr,
								++newVer, version);
						version = newVer;
					}
				}
			}
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
		//
	}

	Object callDocumentsByCategory(String location) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("count", 1000);
			postParams.put("category", location);
			Object ret = callDatabaseServerMethod(
					ApiMethodName_DocumentsByCategory, postParams);
			ArrayList<WizDocument> arr = objToDocuments(ret);
			WizIndex.updateDocuments(mContext, mAccountUserId, arr);
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callDocumentsByTag(String tagGUID) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("count", 1000);
			postParams.put("tag_guid", tagGUID);
			Object ret = callDatabaseServerMethod(ApiMethodName_DocumentsByTag,
					postParams);
			ArrayList<WizDocument> arr = objToDocuments(ret);
			WizIndex.updateDocuments(mContext, mAccountUserId, arr);
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	//
	@SuppressWarnings("unused")
	Object callDownloadMobileData(String documentGUID) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("document_guid", documentGUID);
			Object ret = null;
			if (null == ret) {
				// updateDocumentData(null, documentGUID);
				return null;
			}
			//
			XmlRpcStruct retData = (XmlRpcStruct) ret;
			//
			byte[] data = (byte[]) retData.get("document_zip_data");
			//
			// updateDocumentData(data, documentGUID);
			//
			retData.remove("document_zip_data");
			retData.put("document_guid", documentGUID);
			//
			return onXmlRpcReturn(retData);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	/*
	 * 下载数据！ 文档+附件
	 */
	static final long data_part_size = 1024 * 512;

	Object downloadData(File dataFile, String dataTitle, String dataGuid,
			String type) {

		boolean data_zip_eof = false;
		int data_start_pos = 0;
		long objSize = 0;
		long downSize = 0;

		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			FileOutputStream outStream = new FileOutputStream(dataFile);

			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("obj_guid", dataGuid);
			postParams.put("obj_type", type);
			postParams.put("part_size", data_part_size);

			XmlRpcStruct retData = null;
			Object ret = null;
			while (!data_zip_eof) {
				if (isStoping())
					throw new WizApiUserCancelException();

				postParams.put("start_pos", data_start_pos);

				ret = callDatabaseServerMethod(ApiMethodName_DownloadData,
						postParams);
				if (ret == null) {

					throw new WizApiException(
							R.string.wiz_exception_download_part_null,
							"Can not download part data");
				}
				retData = (XmlRpcStruct) ret;

				byte[] new_data = (byte[]) retData.get("data");
				String new_data_md5 = WizGlobals.makeMD5(new_data);

				if (!new_data_md5.equals(retData.getString("part_md5"))) {

					throw new WizApiException(
							R.string.wiz_exception_download_part_error,
							"Data part md5 does not match");
				}
				outStream.write(new_data);

				if (retData.get("eof").equals("1")) {
					data_zip_eof = true;

				}

				objSize = Long.parseLong((String) retData.get("obj_size"));
				downSize += Long.parseLong((String) retData.get("part_size"));

				sendSyncMessage(R.string.data_downloaded, dataTitle + ":"
						+ String.valueOf((int) (downSize * 100 / objSize))
						+ "%");

				String data_start_pos_str = (String) retData.get("part_size");
				data_start_pos += Integer.parseInt(data_start_pos_str);
			}

			outStream.close();

			retData.remove("data");

			return onXmlRpcReturn(retData);
		} catch (WizApiUserCancelException e) {
			return null;
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		} catch (IOException e) {
			return onXmlRpcFailed(e);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}

	}

	Object downloadDocument(WizDocument doc) {

		if (doc == null)
			return null;
		String mGUID = doc.guid;
		File dataFile = null;
		try {
			String new_file_name = WizGlobals.getAccountPath(mContext,
					mAccountUserId) + mGUID + ".zip";
			dataFile = new File(new_file_name);
			if (downloadData(dataFile, doc.title, mGUID,
					WizGlobals.DATA_INFO_TYPE_DOCUMENT) == null)
				return null;

			if (WizGlobals.isEncryptFile(dataFile))
				return null;

			String md5 = WizGlobals.makeMD5ForFile(new_file_name);

			if (WizSQLite.isDocumentServerChanged(mContext, mGUID, md5)) {
				if (!WizGlobals.unZipDocumentData(mContext, mAccountUserId,
						dataFile, mGUID))
					return null;
				WizGlobals.saveNote(mContext, doc);
				WizSQLite.UpdateDocumentMd5(mContext, mGUID, md5);
			}

			return "dataGuid";
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		} finally {
			dataFile.delete();
			String docFilePath = WizGlobals.getWizTaskCurrentPath(mContext,
					mGUID);
			WizGlobals.deleteDirectory(docFilePath);
		}

	}

	Object downloadAttachment(WizAttachment att) {

		File dataFile = null;
		try {

			String new_file_name = WizGlobals.getAccountPath(mContext,
					mAccountUserId) + att.guid + ".zip";

			dataFile = new File(new_file_name);

			if (downloadData(dataFile, att.name, att.guid,
					WizGlobals.DATA_INFO_TYPE_ATTACHMENT) == null)
				return null;

			if (!WizGlobals.unZipAttachmentData(mContext, mAccountUserId,
					dataFile, att))
				return null;

			if (!WizIndex.updateAttachment(mContext, mAccountUserId, att.guid))
				return null;

			return "dataGuid";
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		} finally {
			dataFile.delete();
		}
	}

	Object callUploadMobileData(WizDocument doc, String documentOrgFileName) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			String text = null;
			byte[] data = null;
			//
			if (doc.fileType.equals(".txt")) {
				text = WizGlobals
						.loadTextFromFile(documentOrgFileName, "utf-8");
			} else if (doc.fileType.equals(".png")
					|| doc.fileType.equals(".jpg")) {
				String documentMemoFileName = WizIndex.getDocumentOrgFileName(
						mContext, mAccountUserId, doc.guid, ".txt");
				if (WizGlobals.fileExists(documentMemoFileName)) {
					//
					text = WizGlobals.loadTextFromFile(documentMemoFileName,
							"utf-8");
				} else {
					text = doc.title;
				}
				// 读取字节文件到字节数组中
				data = WizGlobals.loadByteFromFile(documentOrgFileName);
			}
			//
			java.io.File file = new java.io.File(documentOrgFileName);
			Date dateModified = new Date(file.lastModified());
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("document_guid", doc.guid);
			postParams.put("document_title", doc.title == null ? "No title"
					: doc.title);
			postParams.put("document_url", doc.url == null ? "" : doc.url);
			postParams.put("document_type", doc.type == null ? "" : doc.type);
			postParams.put("document_filetype", doc.fileType == null ? ""
					: doc.fileType);
			postParams.put("dt_modified", dateModified == null ? new Date()
					: dateModified);
			postParams.put("document_category", doc.location == null ? ""
					: doc.location);
			//
			if (text != null) {
				postParams.put("document_body", text);
			} else {
				postParams.put("document_body", "");
			}
			//
			if (data != null) {
				postParams.put("document_data", data);
			} else {
				postParams.put("document_data", "");
			}
			//
			// callDatabaseServerMethod(ApiMethodName_UploadMobileData,
			// postParams);
			//
			WizIndex index = new WizIndex(mContext, mAccountUserId);
			try {
				index.setDocumentLocalChanged(doc.guid, false);
			} finally {
				index.closeDatabase();
			}
			//
			return onXmlRpcReturn(doc.guid);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	Object uploadData(String dataTitle, String data_guid, String type,
			File zipFile, Date dateModified, String data_md5) {

		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}

			// 文件的总size
			long data_size = zipFile.length();
			long uploadSize = 0;

			// 文件的块数
			int data_count = (int) (data_size / data_part_size);
			if ((data_size % data_part_size) != 0) {
				data_count = data_count + 1;
			}

			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("obj_size", data_size);
			postParams.put("obj_guid", data_guid);
			postParams.put("obj_type", type);
			postParams.put("obj_md5", data_md5);
			postParams.put("part_count", data_count);
			postParams.put("dt_info_modified",
					dateModified == null ? new Date() : dateModified);

			/* 分段读取文件 */
			FileInputStream in = new FileInputStream(zipFile);

			int data_sn = 0;

			byte[] part_data = new byte[(int) data_part_size];

			try {
				while (true) {
					if (isStoping())
						throw new WizApiUserCancelException();
					//
					int curr_size = in.read(part_data);
					if (curr_size <= 0)
						break;
					byte[] curr_data = null;
					//
					if (curr_size < part_data.length) {
						curr_data = new byte[curr_size];
						System.arraycopy(part_data, 0, curr_data, 0, curr_size);
					} else {
						curr_data = part_data;
					}

					String data_part_md5 = WizGlobals.makeMD5(curr_data);
					//
					postParams.put("part_sn", data_sn);
					postParams.put("part_size", curr_size);
					postParams.put("part_md5", data_part_md5);
					postParams.put("data", curr_data);

					callDatabaseServerMethod(ApiMethodName_UploadData,
							postParams);
					uploadSize += curr_size;

					sendSyncMessage(
							R.string.data_uploaded,
							dataTitle
									+ ":"
									+ String.valueOf((int) (uploadSize * 100 / data_size))
									+ "%");

					data_sn++;
				}
			} finally {
				in.close();
				zipFile.delete();
			}

			// postParams.remove("part_md5");
			// postParams.remove("part_size");
			// postParams.remove("part_sn");
			return onXmlRpcReturn(postParams);

		} catch (WizApiUserCancelException e) {
			return null;
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	Object uploadDocument(WizDocument doc) {// index.html
		try {
			if (doc == null)
				return null;

			String mCurrentGuid = doc.guid;
			if (WizGlobals.isEmptyString(mCurrentGuid))
				return null;

			File zipFile = WizGlobals.getDocZipFile(mContext, mAccountUserId,
					doc);
			if (zipFile == null)
				return null;

			String data_md5 = WizGlobals.makeMD5ForFile(zipFile);

			Date dateModified = WizGlobals
					.getDateFromSqlDateTimeString(doc.dateModified);

			if (null == uploadData(doc.title, mCurrentGuid,
					WizGlobals.DATA_INFO_TYPE_DOCUMENT, zipFile, dateModified,
					data_md5))
				return null;

			if (null == callUploadDocumentInfo(doc, true, data_md5))
				return null;

			return onXmlRpcReturn(mCurrentGuid);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	File getDocZipFile(String dataGuid, String dataOrgFileName) {

		if (WizGlobals.isEmptyString(dataGuid))
			return null;

		String basePath = WizGlobals.getDataRootPath(mContext);
		String zip_file_name = WizGlobals.pathAddBackslash(basePath) + dataGuid
				+ ".zip";
		File zipFile = new File(zip_file_name);

		String newFolderName = WizGlobals.pathAddBackslash(basePath) + dataGuid;
		WizGlobals.ensurePathExists(newFolderName);
		File newFolder = new File(newFolderName);

		String filePath = WizIndex.getDocumentImagePathEx(mContext,
				mAccountUserId, dataGuid, false);
		try {

			if (WizGlobals.fileExists(filePath)) {
				File index_filesFile[] = (new File(filePath)).listFiles();
				if (index_filesFile != null && index_filesFile.length > 0) {
					for (int j = 0; j < index_filesFile.length; j++) {

						String indexFilePath = index_filesFile[j].getPath();

						String indexFileName = index_filesFile[j].getName();

						String newFilePath = newFolderName + "/index_files/";

						WizGlobals.ensurePathExists(newFilePath);
						WizGlobals.copyFile(indexFilePath, newFilePath
								+ indexFileName);
					}
				}
			}

			int fileNameIndex = dataOrgFileName.lastIndexOf("/");
			String fileName = dataOrgFileName.substring(fileNameIndex + 1);
			String newFileName = WizGlobals.pathAddBackslash(newFolderName)
					+ fileName;

			WizGlobals.copyFile(dataOrgFileName, newFileName);
			WizGlobals.ZipByApache(newFolder, zipFile);

		} catch (FileNotFoundException e) {
			WizIndex.setDocumentInfoByUpload(mContext, mAccountUserId,
					dataGuid, "");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {

			/* 删除临时文件夹及其子文件 */
			WizGlobals.deleteDirectory(newFolderName);
		}

		return zipFile;
	}

	Object uploadAttachment(WizAttachment att, String dataOrgFileName) {
		try {
			String dataGuid = att.guid;
			if (WizGlobals.isEmptyString(dataGuid))
				return null;

			File zipFile = getAttachmentZipFile(dataGuid, dataOrgFileName);
			if (zipFile == null)
				return null;

			String data_md5 = WizGlobals.makeMD5ForFile(zipFile);

			// 压缩文件 的长度
			long data_size = zipFile.length();

			if (null == uploadData(att.name, dataGuid,
					WizGlobals.DATA_INFO_TYPE_ATTACHMENT, zipFile, new Date(),
					data_md5))
				return null;

			if (null == callUploadAttachmentInfo(att, dataOrgFileName, true,
					data_md5, data_size))
				return null;

			return onXmlRpcReturn(att.guid);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	File getAttachmentZipFile(String dataGuid, String dataOrgFileName) {
		if (WizGlobals.isEmptyString(dataGuid))
			return null;

		String basePath = WizGlobals.getDataRootPath(mContext);
		String zipFileName = WizGlobals.pathAddBackslash(basePath) + dataGuid
				+ ".zip";

		File zipFile = new File(zipFileName);

		String newFolderName = WizGlobals.pathAddBackslash(basePath) + dataGuid;
		WizGlobals.ensurePathExists(newFolderName);
		File newFolder = new File(newFolderName);

		int fileNameIndex = dataOrgFileName.lastIndexOf("/");
		String fileName = dataOrgFileName.substring(fileNameIndex + 1);
		String newFileName = WizGlobals.pathAddBackslash(newFolderName)
				+ fileName;
		try {

			WizGlobals.copyFile(dataOrgFileName, newFileName);
			WizGlobals.ZipByApache(newFolder, zipFile);

		} catch (FileNotFoundException e) {
			WizIndex.setAttachmentLocalChanged(mContext, mAccountUserId,
					dataGuid, "", false);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			WizGlobals.deleteDirectory(newFolderName);
		}
		return zipFile;
	}

	Object callUploadDocumentInfo(WizDocument doc, boolean updata,
			String document_zip_md5) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			String guid = doc.guid;
			String doc_tag_guids = "";
			if (doc.tagGUIDs != null)
				doc_tag_guids = doc.tagGUIDs.replace("*", ";");
			//
			Date dateModified = WizGlobals
					.getDateFromSqlDateTimeString(doc.dateModified);
			Date dateCreated = WizGlobals
					.getDateFromSqlDateTimeString(doc.dateCreated);

			if (dateModified == null)
				dateModified = new Date();

			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("document_guid", guid);
			postParams.put("document_title", doc.title == null ? "No title"
					: doc.title);
			postParams.put("document_type", doc.type == null ? "" : doc.type);
			postParams.put("document_filetype", doc.fileType == null ? ""
					: doc.fileType);
			postParams.put("document_category",
					doc.location == null ? WizGlobals.TASK_ROOT_PATH
							: doc.location);
			postParams.put("document_info", updata);/* 将来可能修改 */
			postParams.put("with_document_data", updata);
			postParams.put("document_zip_md5", document_zip_md5);
			postParams.put("document_attachment_count", doc.attachmentCount);
			postParams.put("document_tag_guids", doc_tag_guids);
			postParams.put("dt_created", dateCreated == null ? dateModified
					: dateCreated);
			postParams.put("dt_modified", dateModified);
			//
			callDatabaseServerMethod(ApiMethodName_UploadDocumentInfo,
					postParams);

			WizSQLite.updateTaskInfo(mContext, doc.noteId); //
			WizSQLite.UpdateDocumentMd5(mContext, guid, document_zip_md5);
			return onXmlRpcReturn(doc.guid);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callUploadAttachmentInfo(WizAttachment att,
			String documentOrgFileName, boolean updata,
			String attachment_zip_md5, long data_zip_size) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			java.io.File file = new java.io.File(documentOrgFileName);
			Date dateModified = new Date(file.lastModified());
			String data_md5 = WizGlobals.makeMD5ForFile(file);

			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("attachment_guid", att.guid);
			postParams.put("attachment_document_guid", att.docGuid);
			postParams.put("attachment_name", att.name);
			// postParams.put("dt_info_modified",
			// dateModified == null ? new Date() : dateModified);/* 将来可能要修改 */

			postParams.put("dt_modified", dateModified == null ? new Date()
					: dateModified);
			postParams.put("data_md5", data_md5);// 未压缩时的MD5
			postParams.put("attachment_zip_md5", attachment_zip_md5);
			// postParams.put("attachment_info", updata);/* 将来可能要修改 */
			postParams.put("attachment_data", updata);
			// postParams.put("attachment_zip_size", data_zip_size);
			//
			callDatabaseServerMethod(ApiMethodName_UploadAttachmentInfo,
					postParams);
			//
			WizIndex.setAttachmentLocalChanged(mContext, mAccountUserId,
					att.guid, data_md5, false);
			//
			return onXmlRpcReturn(att.guid);
		} catch (Exception e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callUploadDocInfo() {
		try {

		} catch (Exception e) {

			return onXmlRpcFailed(e);
		}
		return null;

	}

	Object callUploadDeletedGUIDs(ArrayList<WizDeletedGUID> guids) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			//
			XmlRpcArray arr = new XmlRpcArray();
			for (int i = 0; i < guids.size(); i++) {
				WizDeletedGUID guid = guids.get(i);
				//
				XmlRpcStruct data = new XmlRpcStruct();
				data.put("deleted_guid", guid.guid);
				data.put("guid_type", guid.type);
				data.put("dt_deleted", WizGlobals
						.getDateFromSqlDateTimeString(guid.dateDeleted));
				//
				arr.add(data);
			}
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("deleteds", arr);
			//
			Object ret = callDatabaseServerMethod(
					ApiMethodName_UploadDeletedList, postParams);
			//
			WizSQLite.clearDeletedGUIDs(mContext);
			//
			return onXmlRpcReturn(ret);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callCreateAccount(boolean isPhone) {
		try {
			if (mAccountUserId == null || mAccountUserId.length() == 0) {
				throw new WizApiException(0, "No User ID");
			}
			if (mAccountPassword == null || mAccountPassword.length() == 0) {
				throw new WizApiException(0, "No Password");
			}
			//
			mToken = null;
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("user_id", mAccountUserId);
			postParams.put("password", mAccountPassword);
			if (isPhone) {
				postParams.put("invite_code", "ae54537f");
				postParams.put("product_name", "androidPhone");
			} else {
				postParams.put("invite_code", "e4c7971a");
				postParams.put("product_name", "androidPad");
			}

			Object ret = callAccountsServerMethod(ApiMethodName_CreateAccount,
					postParams);
			return onXmlRpcReturn(ret);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	// 两个byte数组合成一个数组
	byte[] addByteToOrther(byte[] bArray1, byte[] bArray2) {

		byte[] dest = null;
		System.arraycopy(bArray1, 0, dest, 0, bArray1.length);
		System.arraycopy(bArray2, 0, dest, bArray1.length, bArray2.length);
		return dest;

	}

	// 解压文档
	// void updateDocumentData(byte[] data, String documentGUID) {
	// WizIndex index = new WizIndex(mContext, mAccountUserId);
	// try {
	// String documentPath = WizIndex.getDocumentFilePath(mContext,
	// mAccountUserId, documentGUID);
	// //
	// if (data != null) {
	// java.util.zip.ZipInputStream in = new java.util.zip.ZipInputStream(
	// new java.io.ByteArrayInputStream(data));
	// WizGlobals.unzip(in, documentPath);
	// }
	// //
	// index.updateDocumentData(null, documentGUID);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// index.closeDatabase();
	// }
	// //
	// }

	// 解压$
	// void unZipData(File data, String dataGuid, String type) {
	// WizIndex index = new WizIndex(mContext, mAccountUserId);
	// try {
	//
	// WizAttachment wizAttachment = null;
	//
	// String guid = null;
	// if (type.equals(WizGlobals.DATA_INFO_TYPE_ATTACHMENT)) {
	//
	// wizAttachment = index.attachmentFromGUID(dataGuid);
	// guid = wizAttachment.docGuid;
	//
	// } else if (type.equals(WizGlobals.DATA_INFO_TYPE_DOCUMENT)) {
	//
	// guid = dataGuid;
	// }
	//
	// String dataPath = WizIndex.getDocumentFilePath(mContext,
	// mAccountUserId, guid);
	//
	// if (data != null) {
	//
	// WizGlobals.unZipByApache(data, dataPath, wizAttachment);
	// }
	// //
	// if (type.equals(WizGlobals.DATA_INFO_TYPE_ATTACHMENT)) {
	//
	// index.updateAttachmentData(null, dataGuid);
	// } else if (type.equals(WizGlobals.DATA_INFO_TYPE_DOCUMENT)) {
	//
	// index.updateDocumentData(null, dataGuid);
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// index.closeDatabase();
	// }
	// //
	// }

	//
	public Object callDocumentsByKey(String text, String attributes) {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			if (attributes == null)
				attributes = "";
			//
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("key", text);
			postParams.put("first", 0);
			postParams.put("count", 200);
			// postParams.put("attributes", attributes);
			//
			Object ret = callDatabaseServerMethod(ApiMethodName_DocumentsByKey,
					postParams);
			//
			ArrayList<WizDocument> arr = objToDocuments(ret);
			//
			WizIndex.updateDocuments(mContext, mAccountUserId, arr);
			//
			return onXmlRpcReturn(arr);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}

	Object callGetCert() {
		try {
			if (WizGlobals.isEmptyString(mToken)) {
				throw new WizApiException(0, "Not login");
			}
			XmlRpcStruct postParams = new XmlRpcStruct();
			addCommonParams(postParams);
			postParams.put("user_id", mAccountUserId);
			postParams.put("password",
					WizGlobals.makeMD5Password(mAccountPassword));

			Object ret = callDatabaseServerMethod(ApiMethodName_GetCert,
					postParams);
			XmlRpcStruct obj = (XmlRpcStruct) ret;
			WizIndex.setWizCret(mContext, mAccountUserId, objToWizCret(obj));

			return onXmlRpcReturn(obj);
		} catch (WizApiException e) {
			return onXmlRpcFailed(e);
		}
	}
}
