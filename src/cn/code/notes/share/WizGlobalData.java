package cn.code.notes.share;

import java.util.HashMap;

import android.content.Context;

public class WizGlobalData {

	//
	static String DataTypeOfSyncing = "Syncing";
	static String DataTypeOfSyncThread = "SyncThread";
	/*
	 * static String DataTypeOfSync = "Sync"; static String
	 * DataTypeOfCreateAccount = "CreateAccount"; static String
	 * DataTypeOfVerifyAccount = "VerifyAccount"; static String
	 * DataTypeOfDownloadDocument = "DownloadDocument"; static String
	 * DataTypeOfDocumentsByLocation = "DocumentsByLocation"; static String
	 * DataTypeOfDocumentsByTag = "DocumentsByTag"; static String
	 * DataTypeOfDownloadRecentDocuments = "DownloadRecentDocuments"; // static
	 * String DataTypeOfIndex = "Index";
	 */

	HashMap<String, Object> mDict;

	public WizGlobalData() {
		mDict = new HashMap<String, Object>();
	}

	static WizGlobalData mData;

	//
	static public WizGlobalData sharedData() {
		if (mData == null) {
			mData = new WizGlobalData();
		}
		//
		return mData;
	}

	//
	synchronized public Object getDataOfAccount(String userId, String dataType) {
		String key = getKeyOfAccount(userId, dataType);// Á´½Ó×Ö·û
		return mDict.get(key);
	}

	//

	synchronized public void setDataOfAccount(String userId, String dataType,
			Object data) {
		String key = getKeyOfAccount(userId, dataType);
		//
		mDict.put(key, data);// mAccountUserId_Syncing=key
	}

	//

	public boolean isSyncing(String userId) {
		Boolean b = (Boolean) getDataOfAccount(userId, DataTypeOfSyncing);
		if (b != null)
			return b;
		//
		b = new Boolean(false);
		//
		setDataOfAccount(userId, DataTypeOfSyncing, b);
		return b;
	}

	public void setSyncing(String userId, boolean bCurr) {
		Object b = getDataOfAccount(userId, DataTypeOfSyncing);
		if (null == b) {
			b = new Boolean(false);
		}
		b = bCurr;
		setDataOfAccount(userId, DataTypeOfSyncing, b);
	}

	synchronized public WizSync getSyncThread(Context ctx, String userId,
			boolean forceNew) {
		WizSync sync = (WizSync) getDataOfAccount(userId, DataTypeOfSyncThread);
		//
		if (forceNew || sync == null) {
			sync = new WizSync(ctx, userId, WizSQLite.getAccountPassword(ctx));
			//
			setDataOfAccount(userId, DataTypeOfSyncThread, sync);
		}
		//
		return sync;
	}

	//
	/*
	 * public WizIndex indexData(String userId) { WizIndex data =
	 * (WizIndex)getDataOfAccount(userId, DataTypeOfIndex); if (data != null)
	 * return data; // data = new WizIndex(userId); // setDataOfAccount(userId,
	 * DataTypeOfIndex, data); return data; }
	 */

	static private String getKeyOfAccount(String userId, String dataType) {
		return userId + "_" + dataType;
	}
}
