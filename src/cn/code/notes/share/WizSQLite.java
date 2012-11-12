package cn.code.notes.share;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.code.notes.data.NoteColumnsInterface.ConnectColumns;
import cn.code.notes.data.NoteColumnsInterface.DeletedColumns;
import cn.code.notes.data.NoteColumnsInterface.MetaColumns;
import cn.code.notes.data.NoteColumnsInterface.NoteColumns;
import cn.code.notes.data.Notes;

public class WizSQLite {

	Context mContext;

	public WizSQLite(Context ctx) {
		mContext = ctx;
	}

	ContentValues iniContentValues(String name, String key, String value) {
		ContentValues values = new ContentValues();
		values.put(MetaColumns.META_NAME, name);
		values.put(MetaColumns.META_KEY, key);
		values.put(MetaColumns.META_VALUE, value);
		values.put(MetaColumns.DT_MODIFIED,
				WizGlobals.getCurrentSQLDateTimeString());

		return values;
	}

	boolean setMetaValue(String name, String key, String value) {
		ContentValues values = iniContentValues(name, key, value);

		String selection = MetaColumns.META_NAME + "=? AND "
				+ MetaColumns.META_KEY + "=?";
		String[] selectionArgs = { name, key };
		Uri uriMeta = Notes.CONTENT_META_URI;
		return updateValue(uriMeta, selection, selectionArgs, values, true);
	}

	boolean updateValue(Uri uri, String selection, String[] selectionArgs,
			ContentValues values, boolean newDate) {
		long recNo = -1;
		if (isDataExists(uri, selection, selectionArgs)) {
			recNo = mContext.getContentResolver().update(uri, values,
					selection, selectionArgs);
		} else if (newDate) {
			Uri u = mContext.getContentResolver().insert(uri, values);
			recNo = Long.parseLong(u.getPathSegments().get(1));
		}
		if (recNo < 0)
			return false;
		return true;
	}

	boolean addDeletedInfo(long id) {
		Uri uri = Notes.CONTENT_CONN_URI;
		String selection = ConnectColumns.NOTE_ID + "=" + id;
		if (isDataExists(uri, selection, null)) {
			String guid = getStringValue(uri, selection, null, 1);
			mContext.getContentResolver().delete(uri, selection, null);
			return insertInToDeleted(guid);
		}
		return true;
	}

	boolean insertInToDeleted(String guid) {
		ContentValues values = WizGlobals.iniDeletedContentValues(guid);
		long recNo = -1;
		Uri uri = Notes.CONTENT_DELETED_URI;
		uri = mContext.getContentResolver().insert(uri, values);
		recNo = Long.parseLong(uri.getPathSegments().get(1));
		if (recNo < 0)
			return false;
		return true;
	}

	boolean isDataExists(Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		try {
			cursor = getQueryCursor(uri, selection, selectionArgs);
			while (cursor.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			cursor.close();
		}
		return false;
	}

	// 条件查询META表
	String isMetaValue(String name, String key) {
		String selection = MetaColumns.META_NAME + "=? AND "
				+ MetaColumns.META_KEY + "=?";
		String[] selectionArgs = { name, key };
		return getStringValue(Notes.CONTENT_META_URI, selection, selectionArgs,
				2);
	}

	// 得到账户名
	String getStringValue(Cursor cursor, int index) {
		String value = "";
		try {
			while (cursor.moveToNext()) {
				value = cursor.getString(index);
			}
		} catch (Exception e) {
			value = "";
		}
		/*
		 * if (WizGlobals.isEmptyString(value)) value = "";
		 */
		return value;
	}

	String getStringValue(Uri uri, String selection, String[] selectionArgs,
			int index) {
		Cursor cursor = null;
		try {
			cursor = getQueryCursor(uri, selection, selectionArgs);
			return getStringValue(cursor, index);
		} catch (Exception e) {
		} finally {
			cursor.close();
		}
		return "";
	}

	long getLongValue(Uri uri, String selection, String[] selectionArgs,
			int index) {
		Cursor cursor = null;
		String value = "";
		try {
			cursor = getQueryCursor(uri, selection, selectionArgs);
			value = getStringValue(cursor, index);
		} catch (Exception e) {
			value = "";
		} finally {
			cursor.close();
		}
		if (WizGlobals.isEmptyString(value))
			return 0;
		return Long.parseLong(value);
	}

	Cursor getQueryCursor(Uri uri, String selection, String[] selectionArgs) {
		return mContext.getContentResolver().query(uri, null, selection,
				selectionArgs, null);
	}

	boolean deleteDocument(String selection, String[] selectionArgs) {
		Uri uri = Notes.CONTENT_CONN_URI;
		Cursor cursor = null;
		int recNo = -1;
		try {
			cursor = getQueryCursor(uri, selection, selectionArgs);
			while (cursor.moveToNext()) {
				long noteId = cursor.getLong(2);
				if (noteId > 0)
					recNo = mContext.getContentResolver().delete(
							Notes.CONTENT_NOTE_URI,
							NoteColumns.ID + "=" + noteId, null);
				recNo = mContext.getContentResolver().delete(uri, selection,
						selectionArgs);
			}
		} catch (Exception e) {
			return false;
		} finally {
			cursor.close();
		}
		return recNo >= 0;
	}

	boolean updateDcoumentMd5(String guid, String md5) {
		String selection = ConnectColumns.NOTE_GUID + "=?";
		String[] selectionArgs = { guid, };
		ContentValues values = new ContentValues();
		values.put(ConnectColumns.NOTE_DATA_MD5, md5);
		try {
			return mContext.getContentResolver().update(Notes.CONTENT_CONN_URI,
					values, selection, selectionArgs) > 0;
		} catch (Exception e) {
		}
		return false;
	}

	boolean isDocumentServerChanged(String guid, String md5) {
		String selection = ConnectColumns.NOTE_GUID + "=?";
		String[] selectionArgs = { guid, };
		Cursor cursor = getQueryCursor(Notes.CONTENT_CONN_URI, selection,
				selectionArgs);
		while (cursor.moveToNext()) {
			String md51 = cursor.getString(7);
			if (!WizGlobals.isEmptyString(md51) && md51.equals(md5))
				return false;
			else
				return true;
		}
		return true;
	}

	WizDocument getDocumentFromCursor(String selection, String[] selectionArgs) {
		Cursor cursor = null;
		Uri uri = Notes.CONTENT_CONN_URI;
		WizDocument doc = new WizDocument();
		int id = -1;
		try {
			cursor = getQueryCursor(uri, selection, selectionArgs);
			while (cursor.moveToNext()) {
				id = cursor.getInt(0);
				doc.guid = cursor.getString(1);
				doc.noteId = cursor.getLong(2);
				doc.title = cursor.getString(3);
				doc.location = WizGlobals
						.getLocation(cursor.getString(4), true);
				doc.type = cursor.getString(5);
				doc.fileType = cursor.getString(6);
				doc.dataMd5 = cursor.getString(7);
				doc.attachmentCount = cursor.getInt(8);
				doc.tagGUIDs = cursor.getString(9);
				doc.dateCreated = cursor.getString(10);
				doc.dateModified = cursor.getString(11);
			}
			if (id <= -1)
				return null;
		} catch (Exception e) {
			return null;
		} finally {
			cursor.close();
		}
		return doc;
	}

	ArrayList<WizDocument> getDocumentArray(Cursor cursor) {
		ArrayList<WizDocument> arr = new ArrayList<WizDocument>();

		WizDocument currentDoc = new WizDocument();
		String currentId = "-1";
		String selection = ConnectColumns.NOTE_ID + "=?";
		String[] selectionArgs = new String[1];
		try {
			while (cursor.moveToNext()) {
				currentId = cursor.getString(0);
				selectionArgs[0] = currentId;
				currentDoc = getDocumentFromCursor(selection, selectionArgs);
				if (currentDoc != null) {
					currentDoc.noteId = Long.parseLong(currentId);
					arr.add(currentDoc);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	ArrayList<WizDeletedGUID> getDeletedGuidArray() {
		ArrayList<WizDeletedGUID> arr = new ArrayList<WizDeletedGUID>();
		WizDeletedGUID deletedGuid = null;
		Cursor cursor = null;
		try {
			cursor = getQueryCursor(Notes.CONTENT_DELETED_URI, null, null);
			while (cursor.moveToNext()) {
				deletedGuid = new WizDeletedGUID();
				deletedGuid.guid = cursor.getString(0);
				deletedGuid.type = cursor.getString(1);
				deletedGuid.dateDeleted = cursor.getString(2);
				arr.add(deletedGuid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return arr;
	}

	boolean clearDeletedGUIDs() {
		ArrayList<WizDeletedGUID> arr = getDeletedGuidArray();
		if (arr == null || arr.size() == 0)
			return true;
		String selection = DeletedColumns.DELETED_GUID + "=?";
		String[] selectionArgs = new String[1];
		try {
			for (int i = 0; i < arr.size(); i++) {
				selectionArgs[0] = arr.get(i).guid;
				mContext.getContentResolver().delete(Notes.CONTENT_DELETED_URI,
						selection, selectionArgs);
			}
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	final static String KeyOfSyncVersion = "SYNC_VERSION";
	final static String DocumentNameOfSyncVersion = "DOCUMENT";
	final static String AttachmentNameOfSyncVersion = "ATTACHMENT";
	final static String TagNameOfSyncVersion = "TAG";
	final static String DeletedGUIDNameOfSyncVersion = "DELETED_GUID";

	long getSyncVersion(String key) {
		String version = isMetaValue(KeyOfSyncVersion, key);
		if (WizGlobals.isEmptyString(version))
			return 0;
		return Long.parseLong(version);
	}

	boolean setSyncVersion(String key, long version) {
		String versionString = Long.toString(version);
		return setMetaValue(KeyOfSyncVersion, key, versionString);
	}

	long getDocumentVersion() {
		return getSyncVersion(DocumentNameOfSyncVersion);
	}

	boolean updateDocumentsVersion(long version) {
		return setSyncVersion(DocumentNameOfSyncVersion, version);
	}

	long getDeletedGUIDVersion() {
		return getSyncVersion(DeletedGUIDNameOfSyncVersion);
	}

	boolean updateDeletedGUIDVersion(long version) {
		return setSyncVersion(DeletedGUIDNameOfSyncVersion, version);
	}

	String getAccountUserId(String name, String key) {
		return isMetaValue(name, key);
	}

	boolean updateAccountUserId(String name, String key, String userId) {
		return setMetaValue(name, key, userId);
	}

	String getAccountPassword(String name, String key) {
		return isMetaValue(name, key);
	}

	boolean updateAccountPassword(String name, String key, String Password) {
		return setMetaValue(name, key, Password);
	}

	// 更新document的最高Version
	public static boolean updateDocumentsVersion(Context ctx, long version) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.updateDocumentsVersion(version);
	}

	// 获取document的最高Version
	public static long getDocumentVersion(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getDocumentVersion();
	}

	public static boolean updateAccountUserId(Context ctx, String userId) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.updateAccountUserId("ACCOUNT", "USER_ID", userId);
	}

	public static String getAccountUserId(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getAccountUserId("ACCOUNT", "USER_ID");
	}

	public static boolean updateAccountPassword(Context ctx, String password) {
		WizSQLite sql = new WizSQLite(ctx);
		password = WizGlobals.makeMD5Password(password);
		return sql.updateAccountPassword("ACCOUNT", "PASSWORD", password);
	}

	public static String getAccountPassword(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getAccountPassword("ACCOUNT", "PASSWORD");
	}

	public static boolean isDataExists(Context ctx, Uri uri, String selection,
			String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.isDataExists(uri, selection, selectionArgs);
	}

	public static String getLocationFromCursor(Context ctx, Uri uri,
			String selection, String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getStringValue(uri, selection, selectionArgs, 8);
	}

	public static ArrayList<WizDocument> getDocumentArrayForUpdate(Context ctx) {
		String selection = NoteColumns.LOCAL_MODIFIED + "=?" + "AND "
				+ NoteColumns.TYPE + "=?";
		String[] selectionArgs = { String.valueOf("1"), String.valueOf("0"), };
		WizSQLite sql = new WizSQLite(ctx);
		Cursor cursor = null;
		Uri uriNote = Notes.CONTENT_NOTE_URI;
		try {
			cursor = sql.getQueryCursor(uriNote, selection, selectionArgs);
			return sql.getDocumentArray(cursor);
		} finally {
			cursor.close();
		}
	}

	static public WizDocument getDocument(Context ctx, String selection,
			String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getDocumentFromCursor(selection, selectionArgs);
	}

	static public boolean deleteDocument(Context ctx, String selection,
			String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.deleteDocument(selection, selectionArgs);
	}

	
	static public String getHtmlText(Context ctx, String selection,
			String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		Uri uri = Notes.CONTENT_NOTE_URI;
		return sql.getStringValue(uri, selection, selectionArgs, 8);
	}

	
	public static String getAlertTime(Context ctx, String selection,
			String[] selectionArgs) {
		WizSQLite sql = new WizSQLite(ctx);
		Uri uri = Notes.CONTENT_NOTE_URI;
		return sql.getStringValue(uri, selection, selectionArgs, 2);
	}

	static public boolean updateTaskInfo(Context ctx, long id) {
		WizSQLite sql = new WizSQLite(ctx);
		String selection = NoteColumns.ID + "=?";
		String[] selectionArgs = { String.valueOf(id) };
		Uri uri = Notes.CONTENT_NOTE_URI;
		ContentValues values = new ContentValues();
		values.put(NoteColumns.LOCAL_MODIFIED, 0);
		return sql.updateValue(uri, selection, selectionArgs, values, false);
	}

	static public long getNoteIdFromConnect(Context ctx, String guid) {
		WizSQLite sql = new WizSQLite(ctx);
		Uri uri = Notes.CONTENT_CONN_URI;
		String selection = ConnectColumns.NOTE_GUID + "=?";
		String[] selectionArgs = { guid };
		return sql.getLongValue(uri, selection, selectionArgs, 2);
	}

	static public long getFolderIdFromConnect(Context ctx, String value) {
		WizSQLite sql = new WizSQLite(ctx);
		Uri uri = Notes.CONTENT_NOTE_URI;
		String selection = NoteColumns.SNIPPET + "=?";
		String[] selectionArgs = { value };
		return sql.getLongValue(uri, selection, selectionArgs, 0);
	}

	public static boolean isEmptyFolder(Context ctx, long folderId) {
		WizSQLite sql = new WizSQLite(ctx);
		Uri uri = Notes.CONTENT_NOTE_URI;
		String selection = NoteColumns.PARENT_ID + "=" + folderId;
		return !sql.isDataExists(uri, selection, null);
	}

	static public boolean addDeletedInfo(Context ctx, long id) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.addDeletedInfo(id);
	}

	static public ArrayList<WizDeletedGUID> getAllDeletedGuid(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getDeletedGuidArray();
	}

	static public long getDeletedGUIDVersion(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.getDeletedGUIDVersion();
	}

	static public boolean updateDeletedGUIDVersion(Context ctx,
			ArrayList<WizDeletedGUID> arr, long newVer, long version) {
		WizSQLite sql = new WizSQLite(ctx);
		if (arr == null || arr.size() == 0)
			return true;
		for (int i = 0; i < arr.size(); i++) {
			WizDeletedGUID data = arr.get(i);
			if (data.type.equals(WizGlobals.DATA_INFO_TYPE_DOCUMENT)) {
				String selection = ConnectColumns.NOTE_GUID + "=?";
				String[] selectionArgs = { data.guid, };
				sql.deleteDocument(selection, selectionArgs);
			} else if (data.type.equals(WizGlobals.DATA_INFO_TYPE_TAG)) {
			} else if (data.type.equals(WizGlobals.DATA_INFO_TYPE_ATTACHMENT)) {
			}
		}
		if (newVer > version)
			return sql.updateDeletedGUIDVersion(newVer);

		return true;
	}

	static public boolean clearDeletedGUIDs(Context ctx) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.clearDeletedGUIDs();
	}

	static public boolean isDocumentServerChanged(Context ctx, String guid,
			String md5) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.isDocumentServerChanged(guid, md5);
	}

	static public boolean UpdateDocumentMd5(Context ctx, String guid, String md5) {
		WizSQLite sql = new WizSQLite(ctx);
		return sql.updateDcoumentMd5(guid, md5);
	}

}
