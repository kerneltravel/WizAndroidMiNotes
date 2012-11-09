package cn.code.notes.share;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import cn.code.notes.R;
import cn.code.notes.share.WizDeletedGUID;
import cn.code.notes.share.WizDocument;
import cn.code.notes.share.WizTag;

public class WizIndex {

	final String sqlTableLocation = "CREATE TABLE WIZ_LOCATION (\n"
			+ "[DOCUMENT_LOCATION] CHAR(255) NOT NULL COLLATE NOCASE,\n"
			+ "primary key (DOCUMENT_LOCATION)\n" + ")";

	final String sqlTableCreateLocation = "CREATE TABLE WIZ_CREATE_LOCATION (\n"
			+ "[DOCUMENT_LOCATION] CHAR(255) NOT NULL COLLATE NOCASE,\n"
			+ "primary key (DOCUMENT_LOCATION)\n" + ")";

	final String sqlTableTag = "CREATE TABLE WIZ_TAG (\n"
			+ "TAG_GUID                       char(36)                       not null,\n"
			+ "TAG_PARENT_GUID                 char(36),\n"
			+ "TAG_NAME                       varchar(150),\n"
			+ "TAG_DESCRIPTION                varchar(600),\n"
			+ "LOCAL_CHANGED				  int(1),\n"
			+ "DT_MODIFIED                    char(19),\n"
			+ "primary key (TAG_GUID)\n" + ")";

	final String sqlTableDocument = "CREATE TABLE WIZ_DOCUMENT (\n"
			+ "DOCUMENT_GUID                  char(36)                       not null,\n"
			+ "DOCUMENT_TITLE                 varchar(768)                   not null,\n"
			+ "DOCUMENT_LOCATION              varchar(768),\n"
			+ "DOCUMENT_URL                   varchar(2048),\n"
			+ "DOCUMENT_TAG_GUIDS             varchar(2048),\n"
			+ "DOCUMENT_TYPE                  varchar(20),\n"
			+ "DOCUMENT_FILE_TYPE             varchar(20),\n"
			+ "DT_CREATED                     char(19),\n"
			+ "DT_MODIFIED                    char(19),\n"
			+ "DOCUMENT_ATTACHEMENT_COUNT     int,\n"
			+ "DOCUMENT_DATA_MD5              char(32),\n"
			+ "ATTACHMENT_COUNT               int,\n"
			+ "SERVER_CHANGED                 int,\n"
			+ "LOCAL_CHANGED                  int,\n"
			+ "primary key (DOCUMENT_GUID)\n" + ")";

	final String sqlTableMeta = "CREATE TABLE WIZ_META (\n"
			+ "META_NAME                       varchar(50) NOT NULL COLLATE NOCASE,\n"
			+ "META_KEY                        varchar(50) NOT NULL COLLATE NOCASE,\n"
			+ "META_VALUE                      varchar(3000),\n"
			+ "primary key (META_NAME, META_KEY)\n" + ");";

	final String sqlTableDeletedGUID = "CREATE TABLE WIZ_DELETED_GUID (\n"
			+ "DELETED_GUID                   char(36)                       not null,\n"
			+ "GUID_TYPE                      int                            not null,\n"
			+ "DT_DELETED                     char(19),\n"
			+ "primary key (DELETED_GUID)\n" + ");";

	final String sqlTableDocumentATTACHMENT = "CREATE TABLE WIZ_DOCUMENT_ATTACHMENT (\n"
			+ "ATTACHMENT_GUID               char(36)                not null,\n"
			+ "DOCUMENT_GUID                 char(36)                not null,\n"
			+ "ATTACHMENT_NAME               varchar(768)            not null,\n"
			+ "ATTACHMENT_DATA_MD5           char(32),\n"
			+ "ATTACHMENT_DESCRIPTION        varchar(1000),\n"
			+ "DT_MODIFIED                    char(19),\n"
			+ "SERVER_CHANGED                int,\n"
			+ "LOCAL_CHANGED                 int,\n"
			+ "primary key (ATTACHMENT_GUID)\n" + ");";

	final String sqlFieldListDocument = "DOCUMENT_GUID, DOCUMENT_TITLE, DOCUMENT_LOCATION, DOCUMENT_URL, DOCUMENT_TAG_GUIDS, DOCUMENT_TYPE, DOCUMENT_FILE_TYPE, DT_CREATED, DT_MODIFIED, DOCUMENT_DATA_MD5, ATTACHMENT_COUNT, SERVER_CHANGED, LOCAL_CHANGED";
	final String sqlFieldListAttachment = "ATTACHMENT_GUID,DOCUMENT_GUID,ATTACHMENT_NAME,ATTACHMENT_DATA_MD5,ATTACHMENT_DESCRIPTION,DT_MODIFIED,SERVER_CHANGED, LOCAL_CHANGED";
	final String sqlFieldListDeletedGUID = "DELETED_GUID, GUID_TYPE, DT_DELETED";
	final String sqlFieldListTag = "TAG_GUID, TAG_PARENT_GUID, TAG_NAME, TAG_DESCRIPTION, LOCAL_CHANGED,DT_MODIFIED";

	//
	/*
	 * private class OpenHelper extends android.database.sqlite.SQLiteOpenHelper
	 * { public OpenHelper(Context context, String name, CursorFactory factory,
	 * int version) { super(context, name, factory, version); } //
	 * 
	 * @Override public void onCreate(SQLiteDatabase db) {
	 * mDB.execSQL(sqlTableLocation); mDB.execSQL(sqlTableTag);
	 * mDB.execSQL(sqlTableDocument); mDB.execSQL(sqlTableMeta);
	 * mDB.execSQL(sqlTableDeletedGUID); }
	 * 
	 * @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int
	 * newVersion) {
	 * 
	 * } // }
	 */
	//
	private SQLiteDatabase mDB;

	private String mAccountUserId;

	//
	public WizIndex(Context ctx, String accountUserId) {
		mAccountUserId = accountUserId;
		//
		String path = WizGlobals.pathAddBackslash(WizGlobals.getAccountPath(
				ctx, accountUserId));

		String fileName = path + "index.db";
		//
		if (!openDatabase(fileName)) {
			// android.content.res.Resources res = ctx.getResources();
			// WizGlobals.showMessage(ctx,
			// res.getString(R.string.wiz_failed_to_open_database));
		}
	}

	// 检索表
	private boolean tableExists(String tableName) {
		try {
			boolean exists = false;
			//
			Cursor cursor = mDB.rawQuery(
					"select count(*) from sqlite_master where type='table' and tbl_name='"
							+ tableName + "'", null);
			try {
				if (cursor.moveToNext()) {
					int count = cursor.getInt(0);
					exists = (count == 1);
				}
			} finally {
				cursor.close();
			}
			//
			return exists;
		} catch (SQLiteException err) {
			return false;
		}
	}

	private boolean checkTable(String tableName, String tableSql) {
		if (tableExists(tableName))
			return true;
		//
		return execSql(tableSql);
	}

	//
	private boolean openDatabase(String fileName) {
		if (mDB == null) {
			try {
				mDB = SQLiteDatabase.openOrCreateDatabase(fileName, null);
			} catch (Exception e) {
				return false;
			}
			//
			if (!checkTable("WIZ_DOCUMENT", this.sqlTableDocument))
				return false;
			if (!checkTable("WIZ_TAG", this.sqlTableTag))
				return false;
			if (!checkTable("WIZ_LOCATION", this.sqlTableLocation))
				return false;
			if (!checkTable("WIZ_CREATE_LOCATION", this.sqlTableCreateLocation))
				return false;
			if (!checkTable("WIZ_DELETED_GUID", this.sqlTableDeletedGUID))
				return false;
			if (!checkTable("WIZ_META", this.sqlTableMeta))
				return false;
			if (!checkTable("WIZ_DOCUMENT_ATTACHMENT",
					this.sqlTableDocumentATTACHMENT))
				return false;
		}
		//
		return true;

	}

	public void closeDatabase() {
		if (mDB != null) {
			mDB.close();
			mDB = null;
		}
	}

	//
	boolean hasRecord(String sql) {
		boolean ret = false;
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				if (cursor.moveToNext()) {
					ret = true;
				}
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		return ret;
	}

	ArrayList<String> getAllLocations() {
		String sql = "select DOCUMENT_LOCATION from WIZ_LOCATION";
		//
		return sqlToStringArray(sql);
	}

	ArrayList<String> getLocations(String Parentpath) {
		String sql = "select DOCUMENT_LOCATION from WIZ_LOCATION WHERE DOCUMENT_LOCATION LIKE "
				+ stringToSQLString(Parentpath + "%")
				+ " order by DOCUMENT_LOCATION";
		//
		ArrayList<String> arr = sqlToStringArray(sql);
		// Collator cmp2 = java.text.Collator.getInstance();
		// Collections.sort(arr, cmp2);
		return arr;
	}

	ArrayList<String> getCretateLocations() {
		String sql = "select DOCUMENT_LOCATION from WIZ_CREATE_LOCATION "
				+ " order by DOCUMENT_LOCATION asc";
		//
		return sqlToStringArray(sql);
	}

	ArrayList<WizTag> getTags(String parentGuid) {
		String sql = null;
		if (parentGuid == null || parentGuid.length() == 0) {
			sql = "select " + sqlFieldListTag
					+ " from WIZ_TAG WHERE TAG_PARENT_GUID is null"
					+ " order by TAG_NAME";

		} else {
			sql = "select " + sqlFieldListTag
					+ " from WIZ_TAG WHERE TAG_PARENT_GUID = "
					+ stringToSQLString(parentGuid) + " order by TAG_NAME";
		}

		//
		return sqlToTags(sql);
	}

	WizTag isExistsTag(String tagName, String parentGuid) {
		String sql = null;
		sql = "select " + sqlFieldListTag + " from WIZ_TAG WHERE TAG_NAME = "
				+ stringToSQLString(tagName) + " AND TAG_PARENT_GUID = "
				+ stringToSQLString(parentGuid);
		ArrayList<WizTag> mCurrenttagArr = sqlToTags(sql);
		if (mCurrenttagArr != null && mCurrenttagArr.size() > 0) {
			return mCurrenttagArr.get(0);
		} else {

			return null;
		}
		//
	}

	WizTag getAllTagByTagName(String tagName) {
		String sql = null;
		sql = "select " + sqlFieldListTag + " from WIZ_TAG WHERE TAG_NAME = "
				+ stringToSQLString(tagName);
		ArrayList<WizTag> mCurrenttagArr = sqlToTags(sql);
		if (mCurrenttagArr != null && mCurrenttagArr.size() > 0) {
			return mCurrenttagArr.get(0);
		} else {

			return null;
		}
		//
	}

	ArrayList<WizTag> getAllTags() {
		String sql = null;
		sql = "select " + sqlFieldListTag
				+ " from WIZ_TAG order by DT_MODIFIED desc";
		//
		return sqlToTags(sql);
	}

	static int countOfCharInString(String str, char c) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}

	ArrayList<String> getRootLocations() {
		ArrayList<String> arr = getAllLocations();

		for (int i = arr.size() - 1; i >= 0; i--) {
			String location = arr.get(i);
			if (2 != countOfCharInString(location, '/')) {
				arr.remove(i);
			}
		}
		//
		return arr;
	}

	ArrayList<String> getChildLocations(String parentLocation) {
		ArrayList<String> arr = getAllLocations();

		for (int i = arr.size() - 1; i >= 0; i--) {
			String location = arr.get(i);
			if (location.length() > parentLocation.length()
					&& location.substring(0, parentLocation.length()).equals(
							parentLocation)) {
				String subLocation = location.substring(
						parentLocation.length(), location.length());
				if (subLocation.length() - 1 != subLocation.indexOf('/')) {
					arr.remove(i);
				}
			}
		}
		//
		return arr;
	}

	boolean isLocationExists(String location) {
		String sql = "select DOCUMENT_LOCATION from WIZ_LOCATION where DOCUMENT_LOCATION="
				+ stringToSQLString(location);
		//
		return hasRecord(sql);
	}

	boolean isNewLocationExists(String location) {
		String sql = "select DOCUMENT_LOCATION from WIZ_CREATE_LOCATION where DOCUMENT_LOCATION="
				+ stringToSQLString(location);
		//
		return hasRecord(sql);
	}

	boolean addLocation(String sqlLocation) {
		if (isLocationExists(sqlLocation))
			return true;
		//
		String sql = "insert into WIZ_LOCATION (DOCUMENT_LOCATION) values ("
				+ stringToSQLString(sqlLocation) + ")";
		//
		return execSql(sql);
	}

	boolean addNewLocation(String sqlLocation) {
		if (isNewLocationExists(sqlLocation))
			return true;

		if (isLocationExists(sqlLocation))
			return true;
		//
		String sql = "insert into WIZ_CREATE_LOCATION (DOCUMENT_LOCATION) values ("
				+ stringToSQLString(sqlLocation) + ")";

		if (execSql(sql))
			sql = "insert into WIZ_LOCATION (DOCUMENT_LOCATION) values ("
					+ stringToSQLString(sqlLocation) + ")";
		//
		return execSql(sql);
	}

	boolean addLocations(String[] locations) {
		if (locations == null || locations.length <= 0)
			return true;
		for (int i = 0; i < locations.length; i++) {
			String location = locations[i];
			if (!addLocation(location))
				return false;
		}
		return true;
	}

	boolean addLocations(ArrayList<String> locations) {
		if (locations == null || locations.size() <= 0)
			return true;
		for (int i = 0; i < locations.size(); i++) {
			String location = locations.get(i);
			if (!addLocation(location))
				return false;
		}
		return true;
	}

	boolean addLocation(String parentLocation, String locationName) {
		String location = parentLocation;
		if (location == null)
			location = "";
		//
		if (location.length() == 0) {
			location += "/";
		}
		//
		location += locationName;
		//
		location += "/";
		//
		return addNewLocation(location);
	}

	boolean isDocumentExists(String documentGUID) {
		String sql = "select DOCUMENT_GUID from WIZ_DOCUMENT where DOCUMENT_GUID = '"
				+ documentGUID + "'";
		//
		return hasRecord(sql);
	}

	boolean isLocationEmpty(String location) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_LOCATION like "
				+ stringToSQLString(location + "%");
		//
		return hasRecord(sql);
	}

	ArrayList<WizDocument> getRecentDocuments() {

		return getRecentDocuments(100);
	}

	ArrayList<WizDocument> getRecentDocuments(int count) {
		String sql = "select "
				+ sqlFieldListDocument
				+ " from WIZ_DOCUMENT order by max(DT_CREATED, DT_MODIFIED) desc limit 0, "
				+ count;
		//
		return sqlToDocuments(sql);
	}

	// zsj 指定笔记集合
	public ArrayList<WizDocument> getDocuments(int start, int end) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT order by DT_MODIFIED desc limit " + start
				+ " , " + end;
		//
		return sqlToDocuments(sql);
	}

	public ArrayList<WizDocument> search(String searchText) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_TITLE like "
				+ stringToSQLString("%" + searchText + "%")
				+ " order by max(DT_CREATED, DT_MODIFIED) desc limit 0, 100";
		//
		return sqlToDocuments(sql);
	}

	// 根据组合条件查询
	public ArrayList<WizDocument> andOrsearch(String[] searchText) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_TITLE like ";
		String andSql = sql;
		String orSql = sql;
		for (int i = 0; i < searchText.length; i++) {
			if (i == searchText.length - 1) {
				String temporarySql = stringToSQLString("%" + searchText[i]
						+ "%");
				andSql = andSql + temporarySql;

				orSql = orSql + temporarySql;
			} else {
				andSql = andSql + stringToSQLString("%" + searchText[i] + "%")
						+ " and DOCUMENT_TITLE like ";

				orSql = orSql + stringToSQLString("%" + searchText[i] + "%")
						+ " or DOCUMENT_TITLE like ";
			}
		}
		final String sqlLast = " order by max(DT_CREATED, DT_MODIFIED) desc limit 0, 100";

		ArrayList<WizDocument> andArray = sqlToDocuments(andSql + sqlLast);

		if (andArray != null && andArray.size() != 0) {

			return andArray;

		} else {

			return sqlToDocuments(orSql + sqlLast);
		}
	}

	ArrayList<WizDocument> getDocumentsByLocation(String location) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_LOCATION like "
				+ stringToSQLString(location) + " order by DT_MODIFIED desc";
		//
		return sqlToDocuments(sql);
	}

	private ArrayList<WizDocument> getDocumentsByTag(String tagGUID) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_TAG_GUIDS like '%"
				+ tagGUID + "%' order by DT_MODIFIED desc";
		//
		return sqlToDocuments(sql);
	}

	// 获取到所有的文档列表
	ArrayList<WizDocument> getDocumentsForUpdate() {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where LOCAL_CHANGED=1";
		//
		return sqlToDocuments(sql);
	}

	// 获取到所有附件列表getAttachmentsForUpdate
	ArrayList<WizAttachment> getAttachmentsForUpdate() {
		String sql = "select " + sqlFieldListAttachment
				+ " from WIZ_DOCUMENT_ATTACHMENT where LOCAL_CHANGED=1";
		//
		return sqlToAttachments(sql);
	}

	// 获取到所有附件列表getAttachmentsForUpdate
	ArrayList<WizTag> getTagForUpdate(int count) {
		String sql = null;
		if (count != 0) {

			sql = "select top " + count + " " + sqlFieldListTag
					+ " from WIZ_TAG where LOCAL_CHANGED=1";
		} else {

			sql = "select " + sqlFieldListTag
					+ " from WIZ_TAG where LOCAL_CHANGED=1";
		}
		//
		return sqlToTags(sql);
	}

	// 获取到WizAttachment对象列表By DocumentGuid
	ArrayList<WizAttachment> getRecentWizAttachmentByDocGuid(String documentGUID) {
		String sql = "select " + sqlFieldListAttachment
				+ " from WIZ_DOCUMENT_ATTACHMENT where DOCUMENT_GUID = '"
				+ documentGUID + "'";
		//
		return sqlToAttachments(sql);// sqlToDocuments
	}

	boolean execSql(String sql) {
		boolean ret = false;
		try {
			mDB.execSQL(sql);
			ret = true;
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		return ret;
	}

	static String stringToSQLString(String str) {
		if (str == null)
			return "NULL";
		if (str.length() == 0)
			return "NULL";
		//
		str = str.replace("'", "''");
		//
		return "'" + str + "'";
	}

	ArrayList<String> sqlToStringArray(String sql) {
		ArrayList<String> arr = new ArrayList<String>();
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					String str = cursor.getString(0);

					arr.add(str);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		return arr;
	}

	ArrayList<WizDocument> sqlToDocuments(String sql) {
		ArrayList<WizDocument> arr = new ArrayList<WizDocument>();
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					WizDocument data = new WizDocument();
					//
					data.guid = cursor.getString(0);
					data.title = cursor.getString(1);
					data.location = cursor.getString(2);
					data.url = cursor.getString(3);
					data.tagGUIDs = cursor.getString(4);
					data.type = cursor.getString(5);
					data.fileType = cursor.getString(6);
					data.dateCreated = cursor.getString(7);
					data.dateModified = cursor.getString(8);
					data.dataMd5 = cursor.getString(9);
					data.attachmentCount = cursor.getInt(10);
					data.serverChanged = cursor.getInt(11);
					data.localChanged = cursor.getInt(12);

					arr.add(data);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//

		return arr;
	}

	ArrayList<WizAttachment> sqlToAttachments(String sql) {
		ArrayList<WizAttachment> arr = new ArrayList<WizAttachment>();
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					WizAttachment data = new WizAttachment();
					//
					data.guid = cursor.getString(0);
					data.docGuid = cursor.getString(1);
					data.name = cursor.getString(2);
					data.dataMd5 = cursor.getString(3);
					data.description = cursor.getString(4);
					data.dateModified = cursor.getString(5);
					data.serverChanged = cursor.getInt(6);
					data.localChanged = cursor.getInt(7);

					arr.add(data);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		return arr;
	}

	ArrayList<WizTag> sqlToTags(String sql) {
		ArrayList<WizTag> arr = new ArrayList<WizTag>();
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					WizTag data = new WizTag();
					//
					data.guid = cursor.getString(0);
					data.parentGuid = cursor.getString(1);
					data.name = cursor.getString(2);
					data.description = cursor.getString(3);
					// 可能存在错误
					data.dateModified = cursor.getString(5);
					data.namePath = null;
					data.version = -1;
					arr.add(data);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		Collections.sort(arr);

		//
		return arr;
	}

	// 获取单个文档信息
	public WizDocument documentFromGUID(String documentGUID) {
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DOCUMENT_GUID='" + documentGUID
				+ "'";
		//
		ArrayList<WizDocument> arr = sqlToDocuments(sql);
		if (arr == null)
			return null;
		if (arr.size() == 1)
			return arr.get(0);
		return null;
	}

	static public WizDocument documentFromGUID(Context ctx,
			String accountUserId, String documentGUID) {
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			return index.documentFromGUID(documentGUID);
		} finally {
			index.closeDatabase();
		}
	}

	// 获取单个的附件信息
	public WizAttachment attachmentFromGUID(String attGUID) {
		String sql = "select " + sqlFieldListAttachment
				+ " from WIZ_DOCUMENT_ATTACHMENT where ATTACHMENT_GUID='"
				+ attGUID + "'";
		//
		ArrayList<WizAttachment> arr = sqlToAttachments(sql);
		if (arr == null)
			return null;
		if (arr.size() == 1)
			return arr.get(0);
		return null;
	}

	boolean updateDocument(WizDocument data, boolean change) {
		String sql;
		//
		WizDocument dataExists = documentFromGUID(data.guid);
		if (dataExists != null) {
			if (!change && dataExists.localChanged != 0)
				return true;
			//
			if (data.dataMd5 == null)
				data.dataMd5 = "";
			if (dataExists.dataMd5 == null)
				dataExists.dataMd5 = "";
			//
			String strServerChanged = (dataExists.serverChanged != 0 || !data.dataMd5
					.equals(dataExists.dataMd5)) ? "1" : "0";
			String strLocalChanged = (data.localChanged != 0 || dataExists.localChanged != 0) ? "1"
					: "0";
			//
			if (!WizGlobals.isEmptyString(data.dateCreated))

				sql = "update WIZ_DOCUMENT set DOCUMENT_TITLE="
						+ stringToSQLString(data.title)
						+ ", DOCUMENT_LOCATION="
						+ stringToSQLString(data.location) + ", DOCUMENT_URL="
						+ stringToSQLString(data.url) + ", DOCUMENT_TAG_GUIDS="
						+ stringToSQLString(data.tagGUIDs) + ", DOCUMENT_TYPE="
						+ stringToSQLString(data.type)
						+ ", DOCUMENT_FILE_TYPE="
						+ stringToSQLString(data.fileType) + ", DT_CREATED="
						+ stringToSQLString(data.dateCreated)
						+ ", DT_MODIFIED="
						+ stringToSQLString(data.dateModified)
						+ ", DOCUMENT_DATA_MD5="
						+ stringToSQLString(data.dataMd5)
						+ ", ATTACHMENT_COUNT="
						+ Integer.toString(data.attachmentCount)
						+ ", SERVER_CHANGED=" + strServerChanged
						+ ", LOCAL_CHANGED=" + strLocalChanged
						+ " where DOCUMENT_GUID="
						+ stringToSQLString(data.guid);
			else

				sql = "update WIZ_DOCUMENT set DOCUMENT_TITLE="
						+ stringToSQLString(data.title)
						+ ", DOCUMENT_LOCATION="
						+ stringToSQLString(data.location) + ", DOCUMENT_URL="
						+ stringToSQLString(data.url) + ", DOCUMENT_TAG_GUIDS="
						+ stringToSQLString(data.tagGUIDs) + ", DOCUMENT_TYPE="
						+ stringToSQLString(data.type)
						+ ", DOCUMENT_FILE_TYPE="
						+ stringToSQLString(data.fileType) + ", DT_MODIFIED="
						+ stringToSQLString(data.dateModified)
						+ ", DOCUMENT_DATA_MD5="
						+ stringToSQLString(data.dataMd5)
						+ ", ATTACHMENT_COUNT="
						+ Integer.toString(data.attachmentCount)
						+ ", SERVER_CHANGED=" + strServerChanged
						+ ", LOCAL_CHANGED=" + strLocalChanged
						+ " where DOCUMENT_GUID="
						+ stringToSQLString(data.guid);

		} else {
			boolean bLocalChanged = data.localChanged != 0 ? true : false;
			boolean bServerChanged = true;
			String strLocalChanged = bLocalChanged ? "1" : "0";
			String strServerChanged = bServerChanged ? "1" : "0";

			sql = "insert into WIZ_DOCUMENT (DOCUMENT_GUID, DOCUMENT_TITLE, DOCUMENT_LOCATION, DOCUMENT_URL, DOCUMENT_TAG_GUIDS, DOCUMENT_TYPE, DOCUMENT_FILE_TYPE, DT_CREATED, DT_MODIFIED, DOCUMENT_DATA_MD5, ATTACHMENT_COUNT, SERVER_CHANGED, LOCAL_CHANGED) values ("
					+ stringToSQLString(data.guid)
					+ ", "
					+ stringToSQLString(data.title)
					+ ", "
					+ stringToSQLString(data.location)
					+ ", "
					+ stringToSQLString(data.url)
					+ ", "
					+ stringToSQLString(data.tagGUIDs)
					+ ", "
					+ stringToSQLString(data.type)
					+ ", "
					+ stringToSQLString(data.fileType)
					+ ", "
					+ stringToSQLString(data.dateCreated)
					+ ", "
					+ stringToSQLString(data.dateModified)
					+ ", "
					+ stringToSQLString(data.dataMd5)
					+ ", "
					+ Integer.toString(data.attachmentCount)
					+ ", "
					+ strServerChanged + ", " + strLocalChanged + "" + ") ";
		}
		//
		return execSql(sql);
	}

	String getNextDocumentForDownload() {
		String sql = "select DOCUMENT_GUID from WIZ_DOCUMENT where SERVER_CHANGED=1 order by DT_MODIFIED desc limit 0, 1";
		ArrayList<String> arr = sqlToStringArray(sql);
		if (arr == null)
			return null;
		if (arr.size() == 1)
			return arr.get(0);
		return null;
	}

	ArrayList<String> getAllDocumentsForDownload() {
		String sql = "select DOCUMENT_GUID from WIZ_DOCUMENT where SERVER_CHANGED=1 order by DT_MODIFIED desc";
		return sqlToStringArray(sql);
	}

	ArrayList<String> getDocumentsForDownloadByTime(int countMonth) {
		String currentTime = WizGlobals
				.getCurrentSQLDateTimePastStringForMonth(countMonth);
		String sql = "select DOCUMENT_GUID from WIZ_DOCUMENT where SERVER_CHANGED=1 AND DT_MODIFIED>"
				+ stringToSQLString(currentTime) + " order by DT_MODIFIED desc";
		return sqlToStringArray(sql);
	}

	ArrayList<WizDocument> getDocumentsByTime(int countDay) {
		String currentTime = WizGlobals
				.getCurrentSQLDateTimePastStringForDay(countDay);
		String sql = "select " + sqlFieldListDocument
				+ " from WIZ_DOCUMENT where DT_MODIFIED>"
				+ stringToSQLString(currentTime) + " order by DT_MODIFIED desc";
		return sqlToDocuments(sql);
	}

	public void updateDocuments(ArrayList<WizDocument> arr) {
		for (int i = 0; i < arr.size(); i++) {
			WizDocument data = arr.get(i);
			updateDocument(data, false);
		}
	}

	// TAG_GUID判断Tag是否存在,返回boolean值
	boolean isTagExists(String tagGUID) {
		String sql = "select TAG_GUID from WIZ_TAG where TAG_GUID = '"
				+ tagGUID + "'";
		//
		return hasRecord(sql);
	}

	boolean updateTag(WizTag data, boolean newTag) {
		String sql;

		String strLocalChanged = newTag ? "1" : "0";

		if (isTagExists(data.guid)) {
			// 如果存在就更新Tag内容
			sql = "update WIZ_TAG set TAG_NAME=" + stringToSQLString(data.name)
					+ ", TAG_DESCRIPTION="
					+ stringToSQLString(data.description)
					+ ", TAG_PARENT_GUID=" + stringToSQLString(data.parentGuid)
					+ ", LOCAL_CHANGED=" + strLocalChanged + ", DT_MODIFIED="
					+ stringToSQLString(data.dateModified) + " where TAG_GUID="
					+ stringToSQLString(data.guid);
		} else {
			// 如果不存在就插入Tag内容
			sql = "insert into WIZ_TAG (TAG_GUID, TAG_PARENT_GUID, TAG_NAME, TAG_DESCRIPTION, LOCAL_CHANGED,DT_MODIFIED) values ("
					+ stringToSQLString(data.guid)
					+ ", "
					+ stringToSQLString(data.parentGuid)
					+ ", "
					+ stringToSQLString(data.name)
					+ ", "
					+ stringToSQLString(data.description)
					+ ", "
					+ strLocalChanged
					+ ", "
					+ stringToSQLString(data.dateModified) + ")";
		}
		//
		return execSql(sql);
	}

	boolean updateTags(ArrayList<WizTag> tags, boolean newTags) {
		for (int i = 0; i < tags.size(); i++) {
			if (!updateTag(tags.get(i), newTags))
				return false;
		}
		//
		return true;
	}

	// ATTACHMENT_GUID判断ATTACHMENT是否存在，并返回boolean值
	boolean isAttachmentExists(String attachmentGUID) {
		String sql = "select ATTACHMENT_GUID from WIZ_DOCUMENT_ATTACHMENT where ATTACHMENT_GUID = "
				+ stringToSQLString(attachmentGUID);
		return hasRecord(sql);

	}

	// 更新WizAttachment表
	boolean updateAttachment(WizAttachment data) {
		String sql;

		WizAttachment wizAttExists = attachmentFromGUID(data.guid);

		if (wizAttExists != null) {
			// 如果存在就更新WizAttachment内容
			// !data.dataMd5
			// .equals(dataExists.dataMd5
			String strServerChanged = (wizAttExists.serverChanged != 0) ? "1"
					: "0";
			String strLocalChanged = (data.localChanged != 0 || wizAttExists.localChanged != 0) ? "1"
					: "0";
			sql = "update WIZ_DOCUMENT_ATTACHMENT set " + "ATTACHMENT_NAME="
					+ stringToSQLString(data.name) + ", DOCUMENT_GUID="
					+ stringToSQLString(data.docGuid)
					+ ", ATTACHMENT_DATA_MD5="
					+ stringToSQLString(data.dataMd5)
					+ ", ATTACHMENT_DESCRIPTION="
					+ stringToSQLString(data.description) + ", DT_MODIFIED="
					+ stringToSQLString(data.dateModified)
					+ ", SERVER_CHANGED=" + strServerChanged
					+ ", LOCAL_CHANGED=" + strLocalChanged
					+ " where ATTACHMENT_GUID=" + stringToSQLString(data.guid);
		} else {

			sql = insertIntoAttachment(data);
		}
		return execSql(sql);
	}

	// 向WIZ_DOCUMENT_ATTACHMENT表中插入数据的sql语句
	String insertIntoAttachment(WizAttachment data) {
		// 如果不存在就插入WizAttachment内容
		boolean bLocalChanged = data.localChanged != 0 ? true : false;
		boolean bServerChanged = data.serverChanged != 0 ? true : false;
		String strLocalChanged = bLocalChanged ? "1" : "0";
		String strServerChanged = bServerChanged ? "1" : "0";
		String sql = "insert into WIZ_DOCUMENT_ATTACHMENT (ATTACHMENT_GUID,DOCUMENT_GUID,ATTACHMENT_NAME,ATTACHMENT_DATA_MD5,ATTACHMENT_DESCRIPTION,DT_MODIFIED,SERVER_CHANGED, LOCAL_CHANGED) values ("
				+ stringToSQLString(data.guid)
				+ ", "
				+ stringToSQLString(data.docGuid)
				+ ", "
				+ stringToSQLString(data.name)
				+ ", "
				+ stringToSQLString(data.dataMd5)
				+ ","
				+ stringToSQLString(data.description)
				+ ","
				+ stringToSQLString(data.dateModified)
				+ ","
				+ strServerChanged
				+ ", " + strLocalChanged + ") ";
		return sql;
	}

	boolean updateAttachments(ArrayList<WizAttachment> attachments) {
		for (int i = 0; i < attachments.size(); i++) {

			if (!updateAttachment(attachments.get(i)))
				return false;
		}
		return true;

	}

	boolean getAllTagsPathForTree(String parentGUID, String parentTagPath,
			ArrayList<WizTag> tags) {
		String sql;
		if (parentGUID == null || parentGUID.length() == 0) {
			sql = "select " + sqlFieldListTag
					+ " from WIZ_TAG where TAG_PARENT_GUID is NULL";
		} else {
			sql = "select " + sqlFieldListTag
					+ " from WIZ_TAG where TAG_PARENT_GUID="
					+ stringToSQLString(parentGUID);
		}
		//
		boolean ret = false;
		//
		try {
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					WizTag data = new WizTag();
					data.guid = cursor.getString(0);
					data.parentGuid = cursor.getString(1);
					data.name = cursor.getString(2);
					data.description = cursor.getString(3);
					data.dateModified = cursor.getString(5);
					//
					String nameInPath = data.name;
					nameInPath = nameInPath.replace('/', '-');
					//
					String newTagPath = parentTagPath + nameInPath + "/";
					//
					data.namePath = newTagPath;

					tags.add(data);
					//
					getAllTagsPathForTree(data.guid, newTagPath, tags);
				}
				//
				ret = true;
			} finally {
				cursor.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		//
		return ret;
	}

	ArrayList<WizTag> getAllTagsForTree() {
		ArrayList<WizTag> tags = new ArrayList<WizTag>();
		getAllTagsPathForTree(null, "/", tags);
		//
		Collections.sort(tags);
		//
		return tags;
	}

	public boolean setDocumentLocalChanged(String documentGUID, boolean changed) {
		String strChanged = changed ? "1" : "0";
		String sql = "update WIZ_DOCUMENT set LOCAL_CHANGED=" + strChanged
				+ " where DOCUMENT_GUID='" + documentGUID + "'";
		//
		return execSql(sql);
	}

	public boolean setDocumentInfoByUpload(String documentGUID,
			boolean changed, String data_md5) {
		String strChanged = changed ? "1" : "0";
		String sql = "update WIZ_DOCUMENT set LOCAL_CHANGED=" + strChanged
				+ ", DOCUMENT_DATA_MD5='" + data_md5
				+ "' where DOCUMENT_GUID='" + documentGUID + "'";
		//
		return execSql(sql);
	}

	public boolean setAttachmentLocalChanged(String attGUID, String dataMd5,
			boolean changed) {
		String strChanged = changed ? "1" : "0";
		String sql = "update WIZ_DOCUMENT_ATTACHMENT set LOCAL_CHANGED="
				+ strChanged + ", ATTACHMENT_DATA_MD5="
				+ stringToSQLString(dataMd5) + " where ATTACHMENT_GUID='"
				+ attGUID + "'";
		//
		return execSql(sql);
	}

	public boolean setDocumentServerChanged(String documentGUID, boolean changed) {
		String strChanged = changed ? "1" : "0";
		String sql = "update WIZ_DOCUMENT set SERVER_CHANGED=" + strChanged
				+ " where DOCUMENT_GUID='" + documentGUID + "'";
		//
		return execSql(sql);
	}

	public boolean setAttachmentServerChanged(String attGUID, boolean changed) {
		String strChanged = changed ? "1" : "0";
		String sql = "update WIZ_DOCUMENT_ATTACHMENT set SERVER_CHANGED="
				+ strChanged + " where ATTACHMENT_GUID='" + attGUID + "'";
		//
		return execSql(sql);
	}

	boolean newDocument(String guid, String tagsGuid, String title,
			String type, String fileType, String filePath, int attachmentCount) {
		WizDocument data = new WizDocument();
		data.guid = guid;
		data.title = title;
		if (filePath == null || filePath.length() == 0) {
			data.location = "/My Mobiles/";
		} else {
			data.location = filePath;
		}
		data.dataMd5 = "";
		data.tagGUIDs = tagsGuid;
		data.dateCreated = WizGlobals.getCurrentSQLDateTimeString();
		data.dateModified = data.dateCreated;
		data.type = type;
		data.fileType = fileType;
		data.attachmentCount = attachmentCount;
		data.serverChanged = 0;
		data.localChanged = 1;
		//
		if (!updateDocument(data, false))
			return false;
		//
		return setDocumentServerChanged(guid, false);
	}

	boolean changeDocument(String guid, String tagsGuid, String title,
			String type, String fileType, String filePath, int attachmentCount) {
		WizDocument data = new WizDocument();
		data.guid = guid;
		data.title = title;
		if (filePath == null || filePath.length() == 0) {
			data.location = "/My Mobiles/";
		} else {
			data.location = filePath;
		}
		data.dataMd5 = "";
		data.tagGUIDs = tagsGuid;
		// data.dateCreated = WizGlobals.getCurrentSQLDateTimeString();
		data.dateModified = WizGlobals.getCurrentSQLDateTimeString();
		data.type = type;
		data.fileType = fileType;
		data.attachmentCount = attachmentCount;
		data.serverChanged = 0;
		data.localChanged = 1;
		//
		if (!updateDocument(data, true))
			return false;
		//
		return setDocumentServerChanged(guid, false);
	}

	//
	static public String autoGetTitle(String text, String title, String def) {
		if (title == null || title.length() == 0) {
			title = WizGlobals.getFirstLineOfText(text);
		}
		//
		if (title == null || title.length() == 0) {
			title = def;
		}
		//
		if (title == null || title.length() == 0) {
			title = "Unknown title";
		}
		//
		return title;

	}

	// 创建存在目录的文件
	static public String newNote(Context ctx, String accountUserId,
			String text, String title, String filePath)
			throws FileNotFoundException, IOException {
		int attachmentCount = 0;
		String guid = WizGlobals.genGUID();

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				guid);
		WizGlobals.ensurePathExists(documentPath);

		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, guid, ".txt");

		WizGlobals.saveTextToFile(documentOrgFileName, text, "utf-8");
		//
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, guid);
		//
		title = autoGetTitle(text, title,
				"New note (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		String html = WizGlobals.text2Html(text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(guid, "", title, "note", ".txt", filePath,
					attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return guid;
	}

	// 创建默认目录/My Mobiles/的文件
	static public String newNote(Context ctx, String accountUserId,
			String text, String title) throws FileNotFoundException,
			IOException {
		// 附件的个数
		int attachmentCount = 0;
		String guid = WizGlobals.genGUID();

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				guid);
		WizGlobals.ensurePathExists(documentPath);

		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, guid, ".txt");

		WizGlobals.saveTextToFile(documentOrgFileName, text, "utf-8");
		//
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, guid);
		//
		title = autoGetTitle(text, title,
				"New note (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		String html = WizGlobals.text2Html(text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(guid, "", title, "note", ".txt", "",
					attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return guid;
	}

	public static String newImage(Context ctx, String accountUserId,
			Bitmap bmp, String text, String title, String type, String filePath)
			throws FileNotFoundException, IOException {
		// 附件的个数
		int attachmentCount = 0;

		if (bmp.getWidth() > 1024 || bmp.getHeight() > 1024) {
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			double fWidth = width / 1024.0;
			double fHeight = height / 1024.0;
			double fRate = Math.max(fWidth, fHeight);
			int newWidth = (int) (width / fRate);
			int newHeight = (int) (height / fRate);
			//
			bmp = WizGlobals.resizeBitmap(bmp, newWidth, newHeight);
		}
		//
		//
		String documentGUID = WizGlobals.genGUID();

		/**
		 * 
		 */
		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);// 创建文档目录
		String imagePath = WizIndex.getDocumentImagePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(imagePath);// 创建image存放目录
		//
		String fileType = ".jpg";
		String documentOrgFileName = WizIndex.getDocumentOrgImageName(ctx,
				accountUserId, documentGUID, fileType);
		// String documentOrgFileName = imagePath + "index.jpg";

		WizGlobals.saveBitmap(bmp, CompressFormat.JPEG, documentOrgFileName);
		// 这次判断意义在那?
		String documentOrgFileNameTest = imagePath + "index.jpg";
		if (!documentOrgFileName.equals(documentOrgFileNameTest))
			return null;
		//
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);
		//
		title = autoGetTitle(text, title,
				"New picture (" + WizGlobals.getDateTimeFileTitle() + ")");
		// 将图片及文字添加到html
		String imagePathForHtml = "index_files/index.jpg";
		String html = WizGlobals.image2Html(imagePathForHtml, text, title);

		// 保存html文件
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");

		//
		if (text != null && text.length() > 0) {
			// 获得一个txt的文件名
			String documentMemoFileName = WizIndex.getDocumentOrgFileName(ctx,
					accountUserId, documentGUID, ".txt");
			// 保存一个txt
			WizGlobals.saveTextToFile(documentMemoFileName, text, "utf-8");
		}
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(documentGUID, "", title, type, fileType,
					filePath, attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return documentGUID;
	}

	public static String newImage(Context ctx, String accountUserId,
			Bitmap bmp, String text, String title, String type)
			throws FileNotFoundException, IOException {
		// 附件的个数
		int attachmentCount = 0;

		if (bmp.getWidth() > 1024 || bmp.getHeight() > 1024) {// 缩放图片
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			double fWidth = width / 1024.0;
			double fHeight = height / 1024.0;
			double fRate = Math.max(fWidth, fHeight);
			int newWidth = (int) (width / fRate);
			int newHeight = (int) (height / fRate);
			//
			bmp = WizGlobals.resizeBitmap(bmp, newWidth, newHeight);
		}

		// 取得Guid
		String documentGUID = WizGlobals.genGUID();

		/**
		 * 获取目录
		 */

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);// 创建目录

		//
		String fileType = ".jpg";
		// 获取格式文件Name
		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, fileType);

		// 保存图片
		WizGlobals.saveBitmap(bmp, CompressFormat.JPEG, documentOrgFileName);

		//
		String documentOrgFileNameTest = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, fileType);
		if (!documentOrgFileName.equals(documentOrgFileNameTest))
			return null;

		// 获取文件名..../index.html
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);

		// 获取文件标题
		title = autoGetTitle(text, title,
				"New picture (" + WizGlobals.getDateTimeFileTitle() + ")");

		//
		String html = WizGlobals.image2Html(documentOrgFileName, text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		if (text != null && text.length() > 0) {
			String documentMemoFileName = WizIndex.getDocumentOrgFileName(ctx,
					accountUserId, documentGUID, ".txt");
			WizGlobals.saveTextToFile(documentMemoFileName, text, "utf-8");
		}
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(documentGUID, "", title, type, fileType, "",
					attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return documentGUID;
	}

	// 创建笔记包含全部的附件内容
	public static String newNoteForAtt(Context ctx, String accountUserId,
			String text, String title, String mDefaultTitle, String filePath,
			ArrayList<WizAttachment> attArrayList,
			ArrayList<String> tagArraylist, String type)
			throws FileNotFoundException, IOException {

		// 附件的个数
		int attachmentCount = 0;

		// 取得Guid
		String documentGUID = WizGlobals.genGUID();

		/**
		 * 获取目录
		 */

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);// 创建目录

		//
		String fileType = "att";
		// 获取格式文件Name

		String mFilePath = WizGlobals.pathAddBackslash(WizGlobals
				.getDataRootPath(ctx));

		//
		attachmentCount = attachmentCount
				+ saveAttFiles(ctx, accountUserId, mFilePath, attArrayList,
						documentPath, documentGUID);

		// 获取文件名..../index.html
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);

		// 获取文件标题
		title = autoGetTitle(text, title,
				mDefaultTitle + "(" + WizGlobals.getDateTimeFileTitle() + ")");

		copyFile2OrtherFolder(documentPath,
				getDocumentImagePath(ctx, accountUserId, documentGUID), "Image");
		ArrayList<WizAttachment> imageArrayList = getSpecificAttachment(
				attArrayList, "Image");
		//
		String html = WizGlobals.noteImages2Html(imageArrayList, text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		if (text != null && text.length() > 0) {
			String documentMemoFileName = WizIndex.getDocumentOrgFileName(ctx,
					accountUserId, documentGUID, ".txt");
			WizGlobals.saveTextToFile(documentMemoFileName, text, "utf-8");
		}

		String tagGuidStr = "";
		if (tagArraylist != null && tagArraylist.size() > 0) {

			int tagsLength = tagArraylist.size();
			for (int i = 0; i < tagsLength; i++) {

				tagGuidStr = tagGuidStr + tagArraylist.get(i);

				if (i < tagsLength - 1) {
					tagGuidStr = tagGuidStr + "*";
				}
			}
		}
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(documentGUID, tagGuidStr, title, type, fileType,
					filePath, attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return documentGUID;
	}

	static public String editDocument(Context ctx, String accountUserId,
			String documentGUID, String title, String text,
			String mDefaultTitle, String filePath,
			ArrayList<String> tagArrayList,
			ArrayList<WizAttachment> attArrayList,
			ArrayList<String> deleteAttGuid, String type)
			throws FileNotFoundException, IOException {

		int attachmentCount = 0;
		String fileType = "att";

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);

		String mFilePath = WizGlobals.pathAddBackslash(WizGlobals
				.getDataRootPath(ctx));

		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, ".txt");

		WizGlobals.saveTextToFile(documentOrgFileName, text, "utf-8");
		//
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);
		//

		attachmentCount = attachmentCount
				+ saveAttFiles(ctx, accountUserId, mFilePath, attArrayList,
						documentPath, documentGUID);

		// 获取文件标题
		title = autoGetTitle(text, title,
				mDefaultTitle + "(" + WizGlobals.getDateTimeFileTitle() + ")");

		//
		copyFile2OrtherFolder(documentPath,
				getDocumentImagePath(ctx, accountUserId, documentGUID), "Image");
		ArrayList<WizAttachment> imageArrayList = getSpecificAttachment(
				attArrayList, "Image");
		// String html = WizGlobals.text2Html(text, title);
		String html = WizGlobals.noteImages2Html(imageArrayList, text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		String tagGuidStr = "";
		if (tagArrayList != null && tagArrayList.size() > 0) {

			int tagsLength = tagArrayList.size();
			for (int i = 0; i < tagsLength; i++) {

				tagGuidStr = tagGuidStr + tagArrayList.get(i);

				if (i < tagsLength - 1) {
					tagGuidStr = tagGuidStr + "*";
				}
			}
		}
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.changeDocument(documentGUID, tagGuidStr, title, type,
					fileType, filePath, attachmentCount);
			String attGuid = "";
			if (deleteAttGuid != null && deleteAttGuid.size() > 0) {

				for (int i = 0; i < deleteAttGuid.size(); i++) {
					attGuid = deleteAttGuid.get(i);
					if (index.isAttachmentExists(attGuid)) {
						index.logDeletedGUID(attGuid,
								WizGlobals.DATA_INFO_TYPE_ATTACHMENT);
						index.deleteAttachment(attGuid);
					}
				}
			}
		} finally {
			index.closeDatabase();
		}

		return documentGUID;
	}

	// 创建音频
	public static String newVoice(Context ctx, String accountUserId,
			String text, String title, String type, String filePath)
			throws FileNotFoundException, IOException {

		// 附件的个数
		int attachmentCount = 0;

		// 取得Guid
		String documentGUID = WizGlobals.genGUID();

		/**
		 * 获取目录
		 */

		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);// 创建目录

		//
		String fileType = ".amr";
		// 获取格式文件Name
		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, fileType);

		String voiceFilePath = WizGlobals.pathAddBackslash(WizGlobals
				.getDataRootPath(ctx));

		// 保存音频文件
		attachmentCount = attachmentCount
				+ getSpecificFiles(ctx, accountUserId, voiceFilePath, fileType,
						documentPath, documentGUID);

		//
		String documentOrgFileNameTest = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, fileType);
		if (!documentOrgFileName.equals(documentOrgFileNameTest))
			return null;

		// 获取文件名..../index.html
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);

		// 获取文件标题
		title = autoGetTitle(text, title,
				"New Voice (" + WizGlobals.getDateTimeFileTitle() + ")");

		//
		String html = WizGlobals.text2Html(text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		if (text != null && text.length() > 0) {
			String documentMemoFileName = WizIndex.getDocumentOrgFileName(ctx,
					accountUserId, documentGUID, ".txt");
			WizGlobals.saveTextToFile(documentMemoFileName, text, "utf-8");
		}
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.newDocument(documentGUID, "", title, type, fileType,
					filePath, attachmentCount);
		} finally {
			index.closeDatabase();
		}
		//
		return documentGUID;
	}

	// 创建默认目录的手指画
	static public String newFinger(Context ctx, String accountUserId,
			Bitmap bmp, String title) throws FileNotFoundException, IOException {
		String type = "finger";
		title = autoGetTitle("", title,
				"New paint (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		return newImage(ctx, accountUserId, bmp, "", title, type);
	}

	// 创建存在目录的手指画
	static public String newFinger(Context ctx, String accountUserId,
			Bitmap bmp, String title, String filePath)
			throws FileNotFoundException, IOException {
		String type = "finger";
		title = autoGetTitle("", title,
				"New paint (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		return newImage(ctx, accountUserId, bmp, "", title, type, filePath);
	}

	static public String newNoteForAtt(Context ctx, String accountUserId,
			String text, String title, String mDefaultTitle, String filePath,
			ArrayList<WizAttachment> attArrayList,
			ArrayList<String> tagArraylist) throws FileNotFoundException,
			IOException {
		String type = "document";
		title = autoGetTitle(text, title,
				mDefaultTitle + "(" + WizGlobals.getDateTimeFileTitle() + ")");
		return newNoteForAtt(ctx, accountUserId, text, title, mDefaultTitle,
				filePath, attArrayList, tagArraylist, type);

	}

	static public String editNoteForAtt(Context ctx, String accountUserId,
			String docGuid, String text, String title, String mDefaultTitle,
			String filePath, ArrayList<WizAttachment> attArrayList,
			ArrayList<String> tagArraylist, ArrayList<String> deleteAttGuids)
			throws FileNotFoundException, IOException {
		String type = "document";
		title = autoGetTitle(text, title,
				mDefaultTitle + "(" + WizGlobals.getDateTimeFileTitle() + ")");
		return editDocument(ctx, accountUserId, docGuid, title, text,
				mDefaultTitle, filePath, tagArraylist, attArrayList,
				deleteAttGuids, type);
	}

	static public String newPhoto(Context ctx, String accountUserId,
			Bitmap bmp, String text, String title)
			throws FileNotFoundException, IOException {
		String type = "photo";
		title = autoGetTitle(text, title,
				"New picture (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		return newImage(ctx, accountUserId, bmp, text, title, type);
	}

	static public String newPhoto(Context ctx, String accountUserId,
			Bitmap bmp, String text, String title, String filePath)
			throws FileNotFoundException, IOException {
		String type = "photo";
		title = autoGetTitle(text, title,
				"New picture (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		return newImage(ctx, accountUserId, bmp, text, title, type, filePath);
	}

	static public String newVoice(Context ctx, String accountUserId,
			String text, String title, String filePath)
			throws FileNotFoundException, IOException {
		String type = "audio";
		title = autoGetTitle(text, title,
				"New Voice (" + WizGlobals.getDateTimeFileTitle() + ")");
		//
		return newVoice(ctx, accountUserId, text, title, type, filePath);
	}

	/* 去掉参数mSaveIndex(用于调试) */
	// static int mSave = 0;

	// 转移或赋值指定文件并保存到指定目录
	public static int saveAttFiles(Context ctx, String accountUserId,
			String mWizAttDir, ArrayList<WizAttachment> attArrayList,
			String newFilePath, String docGuid) throws FileNotFoundException,
			IOException {
		int attCount = 0;
		/* 去掉参数mSaveIndex(用于调试) */
		// mSave += 1;

		if (attArrayList != null && attArrayList.size() > 0) {

			for (int i = 0; i < attArrayList.size(); i++) {
				String fileGetName = attArrayList.get(i).name;
				String fileGetPath = attArrayList.get(i).location;
				String mFileName = fileGetPath + fileGetName;

				fileGetPath = WizGlobals.pathAddBackslash(fileGetPath);
				newFilePath = WizGlobals.pathAddBackslash(newFilePath);

				if (newFilePath.equals(fileGetPath)
						|| newFilePath == fileGetPath) {
					attCount++;
					continue;
				}

				if (WizGlobals.fileExists(mFileName)) {

					String newFileName = newFilePath + fileGetName;
					// 保存文件

					/* 去掉参数mSaveIndex(用于调试) mSave >= 50 && */
					if (fileGetPath.equals(mWizAttDir)) {

						WizGlobals.moveSpecificFiles(mFileName, newFileName);
					} else {

						WizGlobals.copyFile(mFileName, newFileName);

					}

					WizIndex index = new WizIndex(ctx, accountUserId);

					try {

						index.addWizAttachmentSession(
								attArrayList.get(i).description, fileGetName,
								docGuid);
					} finally {
						index.closeDatabase();
					}
					attCount++;
				}
			}
		}
		return attCount;
	}

	/*
	 * 把文件转化成WizAttachment对象
	 */

	boolean addWizAttachmentSession(String description, String name,
			String docGuid) {

		WizAttachment wizAtts = new WizAttachment();
		// 获取到WizAttachment的GUID
		String attGUID = WizGlobals.genGUID();

		// 获得WizAttachment对象
		wizAtts.guid = attGUID;
		wizAtts.docGuid = docGuid;
		wizAtts.name = name;
		wizAtts.dataMd5 = "";
		wizAtts.description = description != null && description.length() != 0 ? description
				: name;
		wizAtts.dateModified = WizGlobals.getCurrentSQLDateTimeString();
		wizAtts.serverChanged = 0;
		wizAtts.localChanged = 1;
		return updateAttachment(wizAtts);
	}

	static void copyFile2OrtherFolder(String documentPath, String newPath,
			String type) throws FileNotFoundException, IOException {
		// ArrayList<String> fileNameArray = new ArrayList<String>();
		File files[] = (new File(documentPath)).listFiles();
		int fileCount = files.length;
		String fileGetName;
		String fileS;

		if (!WizGlobals.pathExists(newPath)) {
			WizGlobals.ensurePathExists(newPath);
		}

		for (int i = 0; i < fileCount; i++) {

			fileGetName = files[i].getName();
			if (fileGetName.lastIndexOf(".") == -1)
				continue;

			// 读取文件
			fileS = fileGetName.substring(fileGetName.lastIndexOf("."));

			if (type.equals("Image")) {

				if (fileS.equals(".png") || fileS.equals(".jpg")
						|| fileS.equals(".jpeg") || fileS.equals(".gif")) {
					WizGlobals.copyFile(
							WizGlobals.pathAddBackslash(documentPath)
									+ fileGetName,
							WizGlobals.pathAddBackslash(newPath) + fileGetName);
				}
			}
		}

	}

	// 获取指定的附件列表
	static ArrayList<WizAttachment> getSpecificAttachment(
			ArrayList<WizAttachment> attArraylist, String type) {
		ArrayList<WizAttachment> mArrayList = new ArrayList<WizAttachment>();
		if (attArraylist == null)
			return mArrayList;
		int listLength = attArraylist.size();
		for (int i = 0; i < listLength; i++) {
			String fileName = "";
			String fileType = "";
			fileName = attArraylist.get(i).name;
			if (fileName.lastIndexOf(".") == -1)
				continue;

			fileType = fileName.substring(fileName.lastIndexOf("."));

			if (type.equals("Image")) {
				if (fileType.equals(".png") || fileType.equals(".jpg")
						|| fileType.equals(".jpeg") || fileType.equals(".gif"))
					mArrayList.add(attArraylist.get(i));
			}
		}
		return mArrayList;

	}

	// 查找指定目录下的所有指定文件并保存到指定目录
	public static int getSpecificFiles(Context ctx, String accountUserId,
			String mWizAudioDir, String type, String newFilePath, String docGuid)
			throws FileNotFoundException, IOException {
		File files[] = (new File(mWizAudioDir)).listFiles();
		int voiceCount = files.length;

		if (files != null) {

			for (int i = 0; i < voiceCount; i++) {
				String fileGetName = files[i].getName();

				if (fileGetName.lastIndexOf(".") >= 0) {
					// 读取.amr文件
					String fileS = fileGetName.substring(fileGetName
							.lastIndexOf("."));

					if (fileS.toLowerCase().equals(type)) {

						String oldFileName = mWizAudioDir + fileGetName;
						String newFileName = newFilePath + fileGetName;
						// 保存音频

						WizGlobals.moveSpecificFiles(oldFileName, newFileName);

						WizIndex index = new WizIndex(ctx, accountUserId);

						try {

							index.voice2WizAttachment(fileGetName, fileGetName,
									docGuid);
						} finally {
							index.closeDatabase();
						}
					}
				}
			}
		}
		return voiceCount;
	}

	/*
	 * 把音频文件转化成WizAttachment对象
	 */

	boolean voice2WizAttachment(String description, String name, String docGuid) {

		WizAttachment wizAtts = new WizAttachment();
		// 获取到WizAttachment的GUID
		String attGUID = WizGlobals.genGUID();

		// 获得WizAttachment对象
		wizAtts.guid = attGUID;
		wizAtts.docGuid = docGuid;
		wizAtts.name = name;
		wizAtts.dataMd5 = "";
		wizAtts.dateModified = WizGlobals.getCurrentSQLDateTimeString();
		wizAtts.description = description != null && description.length() != 0 ? description
				: name;
		wizAtts.serverChanged = 0;
		wizAtts.localChanged = 1;

		return updateAttachment(wizAtts);

	}

	/*
	 * 向Wiz_Document_Attachment表中插入数据
	 */
	/*
	 * 删除指定目录下的指定文件
	 */
	public static void deleteSpecificFiles(String mWizAudioDir, String type) {
		File files[] = (new File(mWizAudioDir)).listFiles();

		if (files != null) {

			for (int i = 0; i < files.length; i++) {
				String fileGetName = files[i].getName();

				if (fileGetName.indexOf(".") >= 0) {
					// 读取.amr文件
					String fileS = fileGetName.substring(fileGetName
							.indexOf("."));

					if (fileS.toLowerCase().equals(type)) {
						String oldFileName = mWizAudioDir + fileGetName;
						// 删除音频
						WizGlobals.deleteFile(oldFileName);
					}
				}
			}
		}
	}

	public boolean changeDocumentType(String documentGUID, String title,
			String type, String fileType) {
		String sql = "update WIZ_DOCUMENT set DOCUMENT_TYPE='" + type
				+ "', DOCUMENT_FILE_TYPE='" + fileType + "', DT_MODIFIED='"
				+ WizGlobals.getCurrentSQLDateTimeString()
				+ "', DOCUMENT_TITLE=" + stringToSQLString(title)
				+ ", LOCAL_CHANGED=1 where DOCUMENT_GUID='" + documentGUID
				+ "'";
		//
		return execSql(sql);
	}

	static public void editDocument(Context ctx, String accountUserId,
			String documentGUID, String title, String text)
			throws FileNotFoundException, IOException {
		String documentPath = WizIndex.getDocumentFilePath(ctx, accountUserId,
				documentGUID);
		WizGlobals.ensurePathExists(documentPath);

		String documentOrgFileName = WizIndex.getDocumentOrgFileName(ctx,
				accountUserId, documentGUID, ".txt");

		WizGlobals.saveTextToFile(documentOrgFileName, text, "utf-8");
		//
		String documentFileName = WizIndex.getDocumentFileName(ctx,
				accountUserId, documentGUID);
		//
		if (title == null || title.length() == 0) {
			title = WizGlobals.getFirstLineOfText(text);
		}
		//
		if (title == null || title.length() == 0) {
			title = "New note (" + (new Date()).toLocaleString() + ")";
		}
		//
		String html = WizGlobals.text2Html(text, title);
		//
		WizGlobals.saveTextToFile(documentFileName, html, "utf-8");
		//
		WizIndex index = new WizIndex(ctx, accountUserId);
		try {
			index.changeDocumentType(documentGUID, title, "note", ".txt");
		} finally {
			index.closeDatabase();
		}
	}

	public boolean deleteDocument(Context ctx, String documentGUID) {
		try {
			String path = WizIndex.getDocumentFilePathEx(ctx, mAccountUserId,
					documentGUID, false);
			WizGlobals.deleteDirectory(path);
		} catch (Exception e) {

		}
		//
		//
		String sql = "delete from WIZ_DOCUMENT where DOCUMENT_GUID='"
				+ documentGUID + "'";
		return execSql(sql);
	}

	// 删除附件根据documentGUID
	public boolean deleteAttachments(String documentGUID) {

		String sql = "delete from WIZ_DOCUMENT_ATTACHMENT where DOCUMENT_GUID='"
				+ documentGUID + "'";
		return execSql(sql);
	}

	// 删除附件根据AttachmentDuid
	public boolean deleteAttachment(String attachmentGUID) {

		String sql = "delete from WIZ_DOCUMENT_ATTACHMENT where ATTACHMENT_GUID='"
				+ attachmentGUID + "'";
		return execSql(sql);
	}

	public boolean deleteTag(String tagGUID) {
		String sql = "delete from WIZ_TAG where TAG_GUID='" + tagGUID + "'";
		return execSql(sql);
	}

	public boolean deleteLocation(String location, boolean isDelete) {
		if (!isDelete && location.equals("/My Mobiles/"))
			return true;
		if (!isDelete && isLocationEmpty(location))
			return true;

		String sql = "delete from WIZ_LOCATION where DOCUMENT_LOCATION='"
				+ location + "'";
		return execSql(sql);
	}

	private boolean isMetaExists(String name, String key) {
		if (name == null || key == null)
			return false;
		if (name.length() == 0 || key.length() == 0)
			return false;
		//
		String sql = "select META_VALUE from WIZ_META where META_NAME='"
				+ name.toUpperCase() + "' and META_KEY='" + key.toUpperCase()
				+ "'";
		return hasRecord(sql);
	}

	synchronized private boolean setMeta(String name, String key, String value) {
		String sql = "";
		if (isMetaExists(name, key)) {
			sql = "update WIZ_META set META_VALUE='" + value
					+ "' where META_NAME='" + name.toUpperCase()
					+ "' and META_KEY='" + key.toUpperCase() + "'";
		} else {
			sql = "insert into WIZ_META (META_NAME, META_KEY, META_VALUE) values ('"
					+ name.toUpperCase()
					+ "', '"
					+ key.toUpperCase()
					+ "', '"
					+ value + "')";
		}
		//
		return execSql(sql);
	}

	synchronized private String getMeta(String name, String key) {
		try {
			String value = null;
			//
			String sql = "select META_VALUE from WIZ_META where META_NAME='"
					+ name + "' and META_KEY='" + key + "'";
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				if (cursor.moveToNext()) {
					value = cursor.getString(0);
				}
			} finally {
				cursor.close();
			}
			//
			return value;
		} catch (Exception err) {
			err.printStackTrace();
		}
		return null;
	}

	boolean dropTable(String tableName) {
		try {
			//
			String sql = "DROP TABLE " + stringToSQLString(tableName);
			return execSql(sql);
		} catch (Exception err) {
			err.printStackTrace();
		}
		return false;
	}

	boolean clearTable(String tableName) {
		try {
			return true;
		} catch (Exception err) {
			err.printStackTrace();
		}
		return false;
	}

	boolean updateDocumentData(byte[] data, String documentGUID) {
		try {

			return setDocumentServerChanged(documentGUID, false);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	//
	boolean updateAttachmentData(byte[] data, String attGUID) {
		try {

			return setAttachmentServerChanged(attGUID, false);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	ArrayList<WizDeletedGUID> sqlToDeletedGUIDs(String sql) {
		try {
			ArrayList<WizDeletedGUID> ret = new ArrayList<WizDeletedGUID>();
			//
			Cursor cursor = mDB.rawQuery(sql, null);
			//
			try {
				while (cursor.moveToNext()) {
					WizDeletedGUID data = new WizDeletedGUID();
					data.guid = cursor.getString(0);
					data.type = cursor.getString(1);
					data.dateDeleted = cursor.getString(2);
					//
					ret.add(data);
				}
			} finally {
				cursor.close();
			}
			//
			return ret;
		} catch (Exception err) {
			err.printStackTrace();
		}
		return null;
	}

	public boolean logDeletedGUID(String GUID, String type) {
		String sql = "insert into WIZ_DELETED_GUID (DELETED_GUID, GUID_TYPE, DT_DELETED) values ('"
				+ GUID
				+ "', '"
				+ type
				+ "', '"
				+ WizGlobals.getCurrentSQLDateTimeString() + "')";
		//
		return execSql(sql);
	}

	// 根据documentGuid删除添加附件删除信息
	public boolean attDeletedGUID(ArrayList<WizAttachment> wizAtts, String type) {

		if (wizAtts != null) {
			for (int i = 0; i < wizAtts.size(); i++) {

				WizAttachment wizAtt = wizAtts.get(i);
				logDeletedGUID(wizAtt.guid, type);
			}
			return true;
		}
		return false;
	}

	boolean clearDeletedGUIDs() {
		String sql = "delete from WIZ_DELETED_GUID";
		//
		return execSql(sql);
	}

	/*
	 * 用于测试
	 */
	boolean clearTags() {
		String sql = "delete from WIZ_TAG";
		//
		return execSql(sql);
	}

	boolean hasDeletedGUIDs() {
		return hasRecord("select from WIZ_DELETED_GUID");
	}

	ArrayList<WizDeletedGUID> getAllDeletedGUIDs() {
		String sql = "select " + sqlFieldListDeletedGUID
				+ " from WIZ_DELETED_GUID";
		return sqlToDeletedGUIDs(sql);
	}

	boolean removeDeletedGUID(String GUID) {
		String sql = "delete from WIZ_DELETED_GUID where DELETED_GUID='" + GUID
				+ "'";
		//
		return execSql(sql);
	}

	final static String KeyOfSyncVersion = "SYNC_VERSION";
	final static String DocumentNameOfSyncVersion = "DOCUMENT";
	final static String AttachmentNameOfSyncVersion = "ATTACHMENT";
	final static String TagNameOfSyncVersion = "TAG";
	final static String DeletedGUIDNameOfSyncVersion = "DELETED_GUID";

	/**
	 * 获取一个String数据，转化成long类并返回long类型数据
	 * 
	 * @param type
	 * @return
	 */
	long getSyncVersion(String type) {
		String str = getMeta(KeyOfSyncVersion, type);
		if (str == null)
			return 0;
		if (str.length() == 0)
			return 0;
		//
		return Long.parseLong(str, 10);
	}

	boolean setSyncVersion(String type, long ver) {
		String verString = Long.toString(ver);

		return setMeta(KeyOfSyncVersion, type, verString);
	}

	String getDocumentVersionString() {
		return Long.toString(getDocumentVersion());
	}

	String getDeletedGUIDVersionString() {
		return Long.toString(getDeletedGUIDVersion());
	}

	// 获取文件的最高版本信息
	long getDocumentVersion() {
		return getSyncVersion(DocumentNameOfSyncVersion);// DocumentNameOfSyncVersion=document
	}

	long getAttachmentVersion() {
		return getSyncVersion(AttachmentNameOfSyncVersion);
	}

	long getDeletedGUIDVersion() {
		return getSyncVersion(DeletedGUIDNameOfSyncVersion);
	}

	long getTagVersion() {
		return getSyncVersion(TagNameOfSyncVersion);
	}

	// 设置文件的最高的版本信息
	boolean setDocumentVersion(long ver) {
		return setSyncVersion(DocumentNameOfSyncVersion, ver);
	}

	boolean setAttachmentVersion(long ver) {
		return setSyncVersion(AttachmentNameOfSyncVersion, ver);
	}

	boolean setTagVersion(long ver) {
		return setSyncVersion(TagNameOfSyncVersion, ver);
	}

	boolean setDeletedGUIDVersion(long ver) {
		return setSyncVersion(DeletedGUIDNameOfSyncVersion, ver);
	}

	public boolean setWifiOnly(boolean b) {
		return setMetaBool("SYNC", "WifiOnly", b);
	}

	public boolean isWifiOnly() {
		return getMetaBoolDef("SYNC", "WifiOnly", true);
	}

	public boolean setWifiOnlyDownLoadData(String b) {
		return setMeta("SYNC", "WifiOnlyDownLoadData", b);
	}

	public String isWifiOnlyDownLoadData() {
		return getMeta("SYNC", "WifiOnlyDownLoadData");
	}

	public boolean setAutoSync(boolean b) {
		return setMetaBool("SYNC", "AutoSync", b);
	}

	public boolean isAutoSync() {
		return getMetaBoolDef("SYNC", "AutoSync", true);
	}

	private String isSyncTime() {
		String mSyncTime = getMeta("SYNC", "SyncTime");
		if (mSyncTime == null || mSyncTime.length() == 0) {
			// 设置现在的时间为上次同步时间
			String newTime = WizGlobals.getNowTime();
			setSyncTime(newTime);
			return newTime;
		} else {

			return mSyncTime;
		}

	}

	// 设置本次同步时间
	public boolean setSyncTime(String timeStr) {

		return setMeta("SYNC", "SyncTime", timeStr);
	}

	// 获取用户新建笔记自动同步
	private String isAutoSyncNewNote() {
		String mAutoSyncNewNote = getMeta("ACCOUNT", "AUTOSYNCNEWNOTE");
		if (mAutoSyncNewNote == null || mAutoSyncNewNote.length() == 0) {
			// 设置新建笔记是否自动同步0：不进行自动同步；1：即时自动同步，默认不进行自动同步；
			setAutoSyncNewNote("0");
			return "0";
		} else {
			return mAutoSyncNewNote;
		}

	}

	// 设置用户新建笔记自动同步
	public boolean setAutoSyncNewNote(String autoSyncNewNote) {

		return setMeta("ACCOUNT", "AUTOSYNCNEWNOTE", autoSyncNewNote);
	}

	// 获取用户阅读模式
	private String isReadingWay() {
		String mReadingWay = getMeta("ACCOUNT", "READINGWAY");
		if (mReadingWay == null || mReadingWay.length() == 0) {
			// 设置为简单阅读模式0：完全阅读模式；1：简单阅读模式；
			setReadingWay("0");
			return "0";
		} else {
			return mReadingWay;
		}

	}

	// 设置用户阅读模式
	public boolean setReadingWay(String readingWay) {

		return setMeta("ACCOUNT", "READINGWAY", readingWay);
	}

	// 查询同步时间间隔
	public int isAutoSyncTime() {
		String mAutoSyncTime = getMeta("SYNC", "AutoSyncTime");
		if (mAutoSyncTime == null || mAutoSyncTime.length() == 0) {

			// 设置AutoSyncTime为1
			setAutoSyncTime(1);
			return 1;
		} else {
			int result = Integer.parseInt(mAutoSyncTime);
			return result;
		}
	}

	// 设置同步时间间隔
	public boolean setAutoSyncTime(int i) {

		return setMeta("SYNC", "AutoSyncTime", String.valueOf(i));
	}

	// 查询上次同步信息
	public int isSyncShowMessage() {
		String mSyncShowMessage = getMeta("SYNC", "SyncShowMessage");
		if (mSyncShowMessage == null || mSyncShowMessage.length() == 0) {

			// 设置AutoSyncTime为1
			setSyncShowMessage(1);
			return 1;
		} else {
			int result = Integer.parseInt(mSyncShowMessage);
			return result;
		}
	}

	// 设置本次同步信息
	public boolean setSyncShowMessage(int i) {

		return setMeta("SYNC", "SyncShowMessage", String.valueOf(i));
	}

	public boolean isShowHelp() {

		return getMetaBoolDef("ACCOUNT", "HELP", true);
	}

	public boolean setShowHelp(boolean help) {

		return setMetaBool("ACCOUNT", "HELP", help);

	}

	public boolean isPasswordProtect() {
		return getMetaBoolDef("COMMON", "PasswordProtect", false);
	}

	public boolean setPasswordProtect(boolean b) {
		return setMetaBool("COMMON", "PasswordProtect", b);
	}

	// 查询用户登录的次数
	public int isLogInIndex() {
		String mLogInIndex = getMeta("ACCOUNT", "LOGININDEX");
		if (mLogInIndex == null || mLogInIndex.length() == 0) {

			// 设置LogInIndex为1
			setLogInIndex(0);
			return 0;
		} else {
			int result = Integer.parseInt(mLogInIndex);
			// 设置LogInIndex为result+1
			setLogInIndex(result + 1);
			return result;
		}
	}

	// 设置用户登录的次数
	public boolean setLogInIndex(int i) {
		return setMeta("ACCOUNT", "LOGININDEX", String.valueOf(i));

	}

	// 查询用户系统设置版本
	public boolean isSystemVersion(int version) {
		String mLogInIndex = getMeta("SYSTEM", "VERSION");
		if (mLogInIndex == null || mLogInIndex.length() == 0) {

			// 设置SystemVersion为version
			setSystemVersion(version);
			return false;
		} else {
			int result = Integer.parseInt(mLogInIndex);
			if (result == version)
				return true;
			else
				setSystemVersion(version);// 设置SystemVersion为version

			return false;
		}
	}

	// 设置用户系统设置版本
	public boolean setSystemVersion(int version) {
		return setMeta("SYSTEM", "VERSION", String.valueOf(version));

	}

	// 查询用户文档排序方式
	public int isWizDocumentOrderBy() {
		String mWizDocumentOrderBy = getMeta("DOCUMENT", "ORDERBY");
		if (mWizDocumentOrderBy == null || mWizDocumentOrderBy.length() == 0) {

			setWizDocumentOrderBy(WizGlobals.DOCUMENT_ORDER_BY_DATE);

			return WizGlobals.DOCUMENT_ORDER_BY_DATE;
		}
		return Integer.parseInt(mWizDocumentOrderBy);
	}

	// 设置用户文档排序方式
	public boolean setWizDocumentOrderBy(int orderBy) {

		return setMeta("DOCUMENT", "ORDERBY", String.valueOf(orderBy));
	}

	// 查询用户的邀请码信息
	public String isWizAccountInvitationCode() {
		String mAccountInvitationCode = getMeta("ACCOUNT", "INVITATE_CODE");
		if (mAccountInvitationCode == null
				|| mAccountInvitationCode.length() == 0) {
			return "";
		}
		return mAccountInvitationCode;
	}

	// 设置用户邀请码
	public boolean setWizAccountInvitationCode(String invitateCode) {

		return setMeta("ACCOUNT", "INVITATE_CODE", invitateCode);
	}

	// 查询附件表版本的信息
	public int isWizAttachmentTableVersion(int mWizAttachmentTableVersion) {
		String mAttachmentTableVersion = getMeta("TABLE_VERSION",
				"WIZ_DOCUMENT_ATTACHMENT");
		if (mAttachmentTableVersion == null
				|| mAttachmentTableVersion.length() == 0) {

			// 删除WIZ_DOCUMENT_ATTACHMENT表
			dropTable("WIZ_DOCUMENT_ATTACHMENT");
			// 设置Attachment的最大version为0
			setAttachmentVersion(0);
			// 设置附件表版本
			setWizAttachmentTableVersion(mWizAttachmentTableVersion);
			return -1;
		} else {
			int result = Integer.parseInt(mAttachmentTableVersion);
			if (result < mWizAttachmentTableVersion) {
				// 删除WIZ_DOCUMENT_ATTACHMENT表
				dropTable("WIZ_DOCUMENT_ATTACHMENT");
				// 设置Attachment的最大version为0
				setAttachmentVersion(0);
				// 设置附件表版本
				setWizAttachmentTableVersion(mWizAttachmentTableVersion);
			}
			return result;
		}
	}

	// 设置标签表版本的信息
	public boolean setWizAttachmentTableVersion(int i) {

		return setMeta("TABLE_VERSION", "WIZ_DOCUMENT_ATTACHMENT",
				String.valueOf(i));

	}

	// 查询CERT-N
	public String isWizCretN() {
		String CERT_DATA = getMeta("CERT", "N");
		if (!WizGlobals.isEmptyString(CERT_DATA)) {
			return CERT_DATA;
		}
		return "";
	}

	// 设置CERT-N
	public boolean setWizCretN(String CERT_DATA) {
		return setMeta("CERT", "N", CERT_DATA);
	}

	// 查询CERT-E
	public String isWizCretE() {
		String CERT_DATA = getMeta("CERT", "E");
		if (!WizGlobals.isEmptyString(CERT_DATA)) {
			return CERT_DATA;
		}
		return "";
	}

	// 设置CERT-E
	public boolean setWizCretE(String CERT_DATA) {
		return setMeta("CERT", "E", CERT_DATA);
	}

	// 查询CERT-ENCRYPTED_D
	public String isWizCretEncryptedD() {
		String CERT_DATA = getMeta("CERT", "ENCRYPTED_D");
		if (!WizGlobals.isEmptyString(CERT_DATA)) {
			return CERT_DATA;
		}
		return "";
	}

	// 设置CERT-ENCRYPTED_D
	public boolean setWizCretEncryptedD(String CERT_DATA) {
		return setMeta("CERT", "ENCRYPTED_D", CERT_DATA);
	}

	// 查询CERT-HINT
	public String isWizCretHINT() {
		String CERT_DATA = getMeta("CERT", "HINT");
		if (!WizGlobals.isEmptyString(CERT_DATA)) {
			return CERT_DATA;
		}
		return "";
	}

	// 设置CERT-HINT
	public boolean setWizCretHINT(String CERT_DATA) {
		return setMeta("CERT", "HINT", CERT_DATA);
	}

	// 查询Wiz 的版本号version_code
	public int isWizVersion() {
		return getMetaIntDef("WIZ", "VERSION_CODE", -1);
	}

	// 设置Wiz 的版本号version_code
	public boolean setWizVersion(int code) {
		return setMetaInt("WIZ", "VERSION_CODE", code);
	}

	// 查询标签表版本的信息
	public int isWizTagTableVersion(int mWizTagTableVersion) {
		String mTagTableVersion = getMeta("TABLE_VERSION", "WIZ_TAG");
		if (mTagTableVersion == null || mTagTableVersion.length() == 0) {

			// 删除Wiz_Tag表
			dropTable("WIZ_TAG");
			// 设置tag的最大version为0
			setTagVersion(0);
			// 设置LogInIndex为1
			setWizTagTableVersion(mWizTagTableVersion);
			return -1;
		} else {
			int result = Integer.parseInt(mTagTableVersion);
			if (result < mWizTagTableVersion) {
				// 删除Wiz_Tag表
				dropTable("WIZ_TAG");
				// 设置tag的最大version为0
				setTagVersion(0);
				// 设置标签表版本
				setWizTagTableVersion(mWizTagTableVersion);
			}
			return result;
		}
	}

	// 设置标签表版本的信息
	public boolean setWizTagTableVersion(int i) {

		return setMeta("TABLE_VERSION", "WIZ_TAG", String.valueOf(i));

	}

	public boolean setMetaBool(String name, String key, boolean b) {
		return setMeta(name, key, b ? "1" : "0");
	}

	public boolean getMetaBoolDef(String name, String key, boolean bDef) {
		String str = getMeta(name, key);
		if (str == null) {
			setMetaBool(name, key, bDef);
			return bDef;
		}
		if (str.length() == 0) {
			setMetaBool(name, key, bDef);
			return bDef;
		}
		//
		try {
			int n = Integer.parseInt(str, 10);
			return n == 1;
		} catch (Exception e) {
			return bDef;
		}
	}

	public boolean setMetaInt(String name, String key, int i) {
		return setMeta(name, key, String.valueOf(i));
	}

	public int getMetaIntDef(String name, String key, int bDef) {
		String str = getMeta(name, key);
		if (WizGlobals.isEmptyString(str)) {
			setMetaInt(name, key, bDef);
			return bDef;
		}
		//
		try {
			return Integer.parseInt(str, 10);
		} catch (Exception e) {
			return bDef;
		}
	}

	ArrayList<String> getAllLocationsForTree() {
		HashMap<String, String> map = new HashMap<String, String>();
		//
		ArrayList<String> locations = getAllLocations();
		for (int i = 0; i < locations.size(); i++) {
			String subLocation = locations.get(i);
			//
			while (subLocation.length() > 2) {
				map.put(subLocation.toLowerCase(), subLocation);
				//
				subLocation = WizGlobals.extractFilePath(subLocation);
				if (subLocation == null || subLocation.length() == 0)
					break;
				//
				if (subLocation.equals("/"))
					break;
				//
				subLocation += "/";
			}
		}
		//
		ArrayList<String> ret = new ArrayList<String>(map.values());
		//
		Collections.sort(ret);
		//
		return ret;
	}

	ArrayList<WizPDFOutlineElement> getAllLocationsActionForTree(Context ctx,
			String Parentpath, String parentId, int parentLevel,
			boolean checkDocument) {

		int index = 0;
		String location = "";

		ArrayList<WizPDFOutlineElement> mPdfOutlines = new ArrayList<WizPDFOutlineElement>();

		WizPDFOutlineElement pdfOutlineElement;
		Parentpath = WizGlobals.pathAddBackslash(Parentpath);
		ArrayList<String> locations = getLocations(Parentpath);
		WizLocation mCurrentLocation;

		String id;
		String outlineTitle;
		boolean mhasParent = false;
		boolean mhasChild = false;

		ArrayList<WizLocation> mLocations = WizGlobals.getLocationObject(ctx,
				locations, parentLevel);

		for (int i = 0; i < mLocations.size(); i++) {
			mhasParent = false;
			mhasChild = false;

			outlineTitle = mLocations.get(i).name;
			mCurrentLocation = mLocations.get(i);
			if (removeEqualPath(mPdfOutlines, outlineTitle)) {

				id = parentId + (new Integer(index)).toString();
				index++;

				if (Parentpath != null && !Parentpath.equals("")) {
					mhasParent = true;
				}

				location = getPathForParentPath(Parentpath, outlineTitle);

				location = WizGlobals.pathAddBackslash(location);

				mhasChild = isExistsDocumentChild(location, checkDocument);

				ArrayList<WizDocument> subDocArray = getChildForDoc(location);

				int mhasChildCount = 0;
				if (subDocArray != null && subDocArray.size() > 0)
					mhasChildCount = subDocArray.size();

				pdfOutlineElement = new WizPDFOutlineElement(id,
						mCurrentLocation, WizGlobals.DATA_INFO_TYPE_LOCATION,
						outlineTitle, mhasParent, mhasChild, mhasChildCount,
						parentId, location, parentLevel + 1, false);

				if (outlineTitle.equals(WizGlobals.mWizDeleteItems)) {

					// deletedItemsPdfOutlineElement = pdfOutlineElement;
				} else {
					mPdfOutlines.add(pdfOutlineElement);
				}
			}
		}
		// if (deletedItemsPdfOutlineElement != null) {
		// mPdfOutlines.add(deletedItemsPdfOutlineElement);
		// }

		return mPdfOutlines;

	}

	boolean isExistsDocumentChild(String path, boolean checkDocument) {

		if (checkDocument) {
			ArrayList<WizDocument> subDocArray = getChildForDoc(path);
			if (subDocArray != null && subDocArray.size() > 0) {

				return true;
			}
		}

		ArrayList<String> subLocations = getChildForLocation(path);
		if (subLocations != null && subLocations.size() > 1)
			return true;

		return false;

	}

	// 移除相同的元素
	boolean removeEqualPath(ArrayList<WizPDFOutlineElement> mPdfOutlines,
			String outlineTitle) {
		for (int i = 0; i < mPdfOutlines.size(); i++) {
			if (outlineTitle.equals(mPdfOutlines.get(i).getOutlineTitle())) {
				return false;
			}

		}
		return true;

	}

	// 根据目录获取文档(孩子节点)列表
	ArrayList<WizDocument> getChildForDoc(String path) {
		path = WizGlobals.pathAddBackslash(path);
		if (path == null || path.length() == 0)
			path = "/";
		return getDocumentsByLocation(path);
	}

	// 根据目录获取目录(孩子节点)列表
	ArrayList<String> getChildForLocation(String path) {
		return getLocations(path);

	}

	String getPathForParentPath(String Parentpath, String outlineTitle) {
		Parentpath = WizGlobals.pathAddBackslash(Parentpath);
		if (Parentpath == null || Parentpath.length() == 0)
			Parentpath = "/";
		String mChildPath = WizGlobals.pathAddBackslash(Parentpath
				+ outlineTitle);

		return mChildPath;
	}

	final static String wizTagNoParentGuid = "Wiz_Tag_Null_ParentGuid";

	ArrayList<WizPDFOutlineElement> getAllTagsActionForTree(String ParentGuid,
			String parentId, int parentLevel, boolean checkDocument) {

		ArrayList<WizPDFOutlineElement> mPdfOutlines = new ArrayList<WizPDFOutlineElement>();

		WizPDFOutlineElement pdfOutlineElement;

		ArrayList<WizTag> tags = getTags(ParentGuid);

		boolean mhasParent = false;
		boolean mhasChild = false;

		for (int i = 0; i < tags.size(); i++) {
			mhasParent = false;
			mhasChild = false;
			String id;
			String outlineTitle;

			WizTag subTag = tags.get(i);

			outlineTitle = subTag.name;

			id = parentId + (new Integer(i)).toString();

			int mhasChildCount = 0;
			mhasChild = isExistsTagChild(subTag.guid, checkDocument);

			ArrayList<WizDocument> docListForTag = getDocumentsByTag(subTag.guid);
			if (docListForTag != null && docListForTag.size() > 0)
				mhasChildCount = docListForTag.size();

			if (ParentGuid != null && ParentGuid.length() != 0)
				mhasParent = true;

			pdfOutlineElement = new WizPDFOutlineElement(id, subTag,
					WizGlobals.DATA_INFO_TYPE_TAG, outlineTitle, mhasParent,
					mhasChild, mhasChildCount, parentId, "", parentLevel + 1,
					false);

			mPdfOutlines.add(pdfOutlineElement);

		}

		return mPdfOutlines;

	}

	boolean isExistsTagChild(String tagGuid, boolean checkDocument) {
		ArrayList<WizTag> subTags = getTags(tagGuid);
		if (checkDocument) {
			ArrayList<WizDocument> docListForTag = getDocumentsByTag(tagGuid);
			if (docListForTag != null && docListForTag.size() > 0)
				return true;
		}
		if (subTags != null && subTags.size() > 0)
			return true;
		return false;
	}

	ArrayList<WizPDFOutlineElement> getDocumentsByLocation(String Parentpath,
			String parentId, int parentLevel) {

		ArrayList<WizPDFOutlineElement> mPdfOutlines = new ArrayList<WizPDFOutlineElement>();

		WizPDFOutlineElement pdfOutlineElement;

		ArrayList<WizDocument> docArray = getDocumentsByLocation(Parentpath);
		Collections.sort(docArray);

		String id = "";
		for (int i = 0; i < docArray.size(); i++) {

			WizDocument subDoc = docArray.get(i);
			id = parentId + String.valueOf(i);

			pdfOutlineElement = new WizPDFOutlineElement(id, subDoc,
					WizGlobals.DATA_INFO_TYPE_DOCUMENT, subDoc.title, true,
					false, 0, parentId, "", parentLevel + 1, false);

			mPdfOutlines.add(pdfOutlineElement);

		}

		return mPdfOutlines;

	}

	ArrayList<WizPDFOutlineElement> getDocumentsByTag(String ParentGuid,
			String parentId, int parentLevel) {

		ArrayList<WizPDFOutlineElement> mPdfOutlines = new ArrayList<WizPDFOutlineElement>();

		WizPDFOutlineElement pdfOutlineElement;

		ArrayList<WizDocument> docArray = getDocumentsByTag(ParentGuid);
		Collections.sort(docArray);

		String id = "";

		for (int i = 0; i < docArray.size(); i++) {

			WizDocument subDoc = docArray.get(i);
			id = parentId + String.valueOf(i);
			pdfOutlineElement = new WizPDFOutlineElement(id, subDoc,
					WizGlobals.DATA_INFO_TYPE_DOCUMENT, subDoc.title, true,
					false, 0, parentId, "", parentLevel + 1, false);

			mPdfOutlines.add(pdfOutlineElement);

		}

		return mPdfOutlines;

	}

	public static String getDocumentFilePathEx(Context ctx, String userId,
			String documentGUID, boolean create) {
		String accountPath = WizGlobals.getAccountPath(ctx, userId);// 取得用户目录
		accountPath = WizGlobals.pathAddBackslash(accountPath);// 添加反斜线
		//
		accountPath = accountPath + documentGUID;
		if (create) {
			WizGlobals.ensurePathExists(accountPath);// 创建目录
		}
		//
		return WizGlobals.pathAddBackslash(accountPath);

	}

	// 创建image的路径
	public static String getDocumentImagePathEx(Context ctx, String userId,
			String documentGUID, boolean create) {
		//
		String accountPath = getDocumentFilePathEx(ctx, userId, documentGUID,
				true);
		accountPath = WizGlobals.pathAddBackslash(accountPath);// 添加反斜线
		accountPath = accountPath + "index_files";
		//
		if (create && !WizGlobals.fileExists(accountPath)) {
			WizGlobals.ensurePathExists(accountPath);// 创建目录
		}
		return WizGlobals.pathAddBackslash(accountPath);

	}

	public static String getDocumentFilePath(Context ctx, String userId,
			String documentGUID) {
		return getDocumentFilePathEx(ctx, userId, documentGUID, true);
	}

	public static String getDocumentImagePath(Context ctx, String userId,
			String documentGUID) {
		return getDocumentImagePathEx(ctx, userId, documentGUID, true);
	}

	// 获取显示的文件也即wiz_mobile.html
	public static String getDocumentMobileFileName(Context ctx, String userId,
			String documentGUID) {
		return getDocumentFilePath(ctx, userId, documentGUID)
				+ "wiz_mobile.html";
	}

	// 获取显示的文件也即index.html
	public static String getDocumentFileName(Context ctx, String userId,
			String documentGUID) {
		return getDocumentFilePath(ctx, userId, documentGUID) + "index.html";
	}

	// 获取显示的文件也即wiz_abstract.html
	public static String getDocumentAbstractFileName(Context ctx,
			String userId, String documentGUID) {
		return getDocumentFilePath(ctx, userId, documentGUID)
				+ "wiz_abstract.html";
	}

	// 获取显示的文件也即index
	public static String getDocumentImageName(Context ctx, String userId,
			String documentGUID) {

		return getDocumentImagePath(ctx, userId, documentGUID) + "index";
	}

	// 获得类型文件地址/文件名
	public static String getDocumentOrgFileName(Context ctx, String userId,
			String documentGUID, String fileExt) {
		String documentFileName = getDocumentFileName(ctx, userId, documentGUID);
		return documentFileName + fileExt;
	}

	// 获得类型文件地址/文件名
	public static String getDocumentOrgImageName(Context ctx, String userId,
			String documentGUID, String fileExt) {
		String documentFileName = getDocumentImageName(ctx, userId,
				documentGUID);
		return documentFileName + fileExt;
	}

	//
	// // 获得类型文件地址/文件名
	// public static String getDocumentOrgImageName(String path, String fileExt)
	// {
	// String documentFileName = getDocumentImagePathEx(path);
	// documentFileName = documentFileName + "index.html";
	// return documentFileName + fileExt;
	// }

	// 获取附件的目录
	public static String getAttachmentPath(Context ctx, String userId,
			String documentGUID) {
		return getDocumentFilePath(ctx, userId, documentGUID);
	}

	// 获得附件文件地址/文件名
	public static String getAttachmentOrgFileName(Context ctx, String userId,
			String documentGUID, String fileName) {
		String attFilePath = getAttachmentPath(ctx, userId, documentGUID);
		attFilePath = WizGlobals.pathAddBackslash(attFilePath);
		return attFilePath + fileName;
	}

	// 设置到是否显示帮助页面的信息
	public static void setShowHelp(Context ctx, String userId, boolean help) {
		WizIndex index = new WizIndex(ctx, userId);// Context ctx,
		try {
			index.setShowHelp(help);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置到是否自动同步的数信息
	public static void setAutoSync(Context ctx, String userId, boolean b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setAutoSync(b);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户同步次数的信息
	public static void setAutoSyncTime(Context ctx, String userId, int i) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setAutoSyncTime(i);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户本次同步的时间
	public static void setSyncTime(Context ctx, String userId, String timeStr) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setSyncTime(timeStr);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户新建笔记自动同步
	public static void setAutoSyncNewNote(Context ctx, String userId,
			String autoSyncNewNote) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setAutoSyncNewNote(autoSyncNewNote);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户阅读模式
	public static void setReadingWay(Context ctx, String userId,
			String readingWay) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setReadingWay(readingWay);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置本次同步信息
	public static void setSyncShowMessage(Context ctx, String userId, int b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setSyncShowMessage(b);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置到是否打开wifi时自动下载文档的信息
	public static void setWifiOnly(Context ctx, String userId, boolean b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setWifiOnly(b);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置到是否打开wifi时自动下载文档的信息
	public static void setWifiOnlyDownLoadData(Context ctx, String userId,
			String b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setWifiOnlyDownLoadData(b);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置到是否有密码保护的信息
	public static void setPasswordProtect(Context ctx, String userId, boolean b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setPasswordProtect(b);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户同步次数的信息
	public static void setLogInIndex(Context ctx, String userId, int i) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setLogInIndex(i);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户文档排序方式
	public static void setWizDocumentOrderBy(Context ctx, String userId,
			int orderBy) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setWizDocumentOrderBy(orderBy);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置用户的邀请码信息
	public static void setWizAccountInvitationCode(Context ctx, String userId,
			String invitationCode) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setWizAccountInvitationCode(invitationCode);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置CERT_N
	public static boolean setWizCretN(Context ctx, String userId,
			String CERT_DATA) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setWizCretN(CERT_DATA);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置CERT_E
	public static boolean setWizCretE(Context ctx, String userId,
			String CERT_DATA) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setWizCretE(CERT_DATA);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置CERT_EncryptedD
	public static boolean setWizCretEncryptedD(Context ctx, String userId,
			String CERT_DATA) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setWizCretEncryptedD(CERT_DATA);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置CERT_HINT
	public static boolean setWizCretHINT(Context ctx, String userId,
			String CERT_DATA) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setWizCretHINT(CERT_DATA);
		} finally {
			index.closeDatabase();
		}
	}

	// 设置CERT_...
	public static boolean setWizCret(Context ctx, String userId,
			WizCretData data) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setWizCretN(data.getmRsaN());
			index.setWizCretE(data.getmRsaE());
			index.setWizCretEncryptedD(data.getmEncryptedD());
			return index.setWizCretHINT(data.getmHINT());
		} finally {
			index.closeDatabase();
		}
	}

	// 设置Wiz 的版本号version_code
	public static boolean setWizVersion(Context ctx, String userId, int code) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setWizVersion(code);
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到是否显示帮助页面的信息
	public static boolean isShowHelp(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isShowHelp();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到是否自动同步的数信息
	public static boolean isAutoSync(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isAutoSync();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到同步时间信息
	public static String isSyncTime(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isSyncTime();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到用户新建笔记是否进行自动同步
	public static String isAutoSyncNewNote(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isAutoSyncNewNote();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到用户阅读模式
	public static String isReadingWay(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isReadingWay();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到同步时间间隔信息
	public static int isAutoSyncTime(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isAutoSyncTime();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到上次同步信息
	public static int isSyncShowMessage(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isSyncShowMessage();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到是否打开wifi时自动下载文档的信息
	public static boolean isWifiOnly(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWifiOnly();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到打开wifi时自动下载文档的方式信息/*0标识不下载数据.1标识下载近期数据.-1标识下载全部数据,如果不存在就默认选择下载近期数据*/
	public static String isWifiOnlyDownLoadData(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			String isWifiOnly = index.isWifiOnlyDownLoadData();
			if (isWifiOnly == null || isWifiOnly.length() <= 0) {
				isWifiOnly = "1";
				index.setWifiOnlyDownLoadData(isWifiOnly);
			}
			return isWifiOnly;
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到是否有密码保护的信息
	public static boolean isPasswordProtect(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isPasswordProtect();
		} finally {
			index.closeDatabase();
		}
	}

	// 核对用户的系统版本信息
	public static boolean isSystemVersion(Context ctx, String userId,
			int version) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isSystemVersion(version);
		} finally {
			index.closeDatabase();
		}
	}

	// 获取到用户同步次数的信息
	public static int isLogInIndex(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isLogInIndex();
		} finally {
			index.closeDatabase();
		}
	}

	// 查找用户文档排序方式
	public static int isWizDocumentOrderBy(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizDocumentOrderBy();
		} finally {
			index.closeDatabase();
		}

	}

	public static String isWizAccountInvitationCode(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizAccountInvitationCode();
		} finally {
			index.closeDatabase();
		}

	}

	public static boolean isWizAttachmentTableVersion(Context ctx,
			String userId, int mWizAttachmentTableVersion) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index
					.isWizAttachmentTableVersion(mWizAttachmentTableVersion) >= mWizAttachmentTableVersion;
		} finally {
			index.closeDatabase();
		}

	}

	public static String isWizCretN(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizCretN();
		} finally {
			index.closeDatabase();
		}

	}

	public static String isWizCretE(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizCretE();
		} finally {
			index.closeDatabase();
		}

	}

	public static String isWizCretEncryptedD(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizCretEncryptedD();
		} finally {
			index.closeDatabase();
		}

	}

	public static String isWizCretHINT(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizCretHINT();
		} finally {
			index.closeDatabase();
		}

	}

	public static WizCretData isWizCret(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			WizCretData data = new WizCretData();
			data.setmRsaN(index.isWizCretN());
			data.setmRsaE(index.isWizCretE());
			data.setmEncryptedD(index.isWizCretEncryptedD());
			data.setmHINT(index.isWizCretHINT());

			return data;
		} finally {
			index.closeDatabase();
		}

	}

	// 查询Wiz 的版本号version_code
	public static int isWizVersion(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizVersion();
		} finally {
			index.closeDatabase();
		}
	}

	public static void deleteDocumentTable(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.dropTable("WIZ_DOCUMENT");
			index.setDocumentVersion(0);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean isWizTagTableVersion(Context ctx, String userId,
			int mWizTagTableVersion) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isWizTagTableVersion(mWizTagTableVersion) >= mWizTagTableVersion;
		} finally {
			index.closeDatabase();
		}

	}

	/**
	 * 得到并返回一个long类型数据最大的Document version
	 * 
	 * @param userId
	 * @return
	 */
	public static long getDocumentVersion(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentVersion();
		} finally {
			index.closeDatabase();
		}
	}

	/**
	 * 得到并返回一个long类型数据最大的Attachment version
	 * 
	 * @param userId
	 * @return
	 */

	public static long getAttachmentVersion(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAttachmentVersion();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取删除文件的版本信息
	public static long getDeletedGUIDVersion(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDeletedGUIDVersion();
		} finally {
			index.closeDatabase();
		}
	}

	// 获取标签的版本信息
	public static long getTAGVersion(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getTagVersion();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizDeletedGUID> getAllDeletedGUIDs(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllDeletedGUIDs();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizDocument> getDocumentsForUpdate(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsForUpdate();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizAttachment> getAttachmentsForUpdate(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAttachmentsForUpdate();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizTag> getTagForUpdate(Context ctx, String userId,
			int count) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getTagForUpdate(count);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<String> getAllDocumentsForDownload(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllDocumentsForDownload();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<String> getDocumentsForDownloadByTime(Context ctx,
			String userId, int countMonth) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsForDownloadByTime(countMonth);
		} finally {
			index.closeDatabase();
		}
	}

	// 根据天进行搜集数据信息
	public static ArrayList<WizDocument> getDocumentsByDay(Context ctx,
			String userId, int countDay) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsByTime(countDay);
		} finally {
			index.closeDatabase();
		}
	}

	// 根据周进行搜集数据信息
	public static ArrayList<WizDocument> getDocumentsByWeek(Context ctx,
			String userId, int countWeek) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsByTime(countWeek * 7);
		} finally {
			index.closeDatabase();
		}
	}

	// 根据月进行搜集数据信息
	public static ArrayList<WizDocument> getDocumentsByMonth(Context ctx,
			String userId, int countMonth) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsByTime(countMonth * 30);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean setDocumentServerChanged(Context ctx, String userId,
			String documentGUID, boolean b) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setDocumentServerChanged(documentGUID, b);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<String> getAllLocationsForTree(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllLocationsForTree();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizPDFOutlineElement> getAllLocationsActionForTree(
			Context ctx, String userId, String Parentpath, String parentId,
			int parentLevel, boolean checkDocument) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllLocationsActionForTree(ctx, Parentpath,
					parentId, parentLevel, checkDocument);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizPDFOutlineElement> getAllTagsActionForTree(
			Context ctx, String userId, String ParentGuid, String parentId,
			int Parentlevel, boolean checkDocument) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllTagsActionForTree(ParentGuid, parentId,
					Parentlevel, checkDocument);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizTag> getAllTagsForTree(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllTagsForTree();
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizTag> getAllTags(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllTags();
		} finally {
			index.closeDatabase();
		}
	}

	public static WizTag getAllTagByTagName(Context ctx, String userId,
			String tagName) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getAllTagByTagName(tagName);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean existsTagByParentGuid(Context ctx, String userId,
			String parentGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getTags(parentGuid) != null
					&& (index.getTags(parentGuid)).size() != 0;
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean existsDocumentByParentGuid(Context ctx,
			String userId, String parentGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsByTag(parentGuid) != null
					&& (index.getDocumentsByTag(parentGuid)).size() != 0;
		} finally {
			index.closeDatabase();
		}
	}

	public static WizTag existsTagByTagNameAndParentGuid(Context ctx,
			String userId, String tagName, String parentGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isExistsTag(tagName, parentGuid);
		} finally {
			index.closeDatabase();
		}
	}

	//
	public static ArrayList<WizDocument> getRecentDocuments(Context ctx,
			String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getRecentDocuments();
		} finally {
			index.closeDatabase();
		}
	}

	//
	public static ArrayList<WizDocument> getRecentDocuments(Context ctx,
			String userId, int count, int orderBy) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizDocument> docArray = index.getRecentDocuments(count);
			return WizGlobals.getDocumentsObject(docArray, orderBy);
		} finally {
			index.closeDatabase();
		}
	}

	// 添加
	public static ArrayList<WizDocument> getDocuments(Context ctx,
			String userId, int orderBy, int start, int end) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizDocument> docArray = index.getDocuments(start, end);

			return WizGlobals.getDocumentsObject(docArray, orderBy);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizDocument> getDocumentsByLocation(Context ctx,
			String userId, String location, int orderBy) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizDocument> docArray = index
					.getDocumentsByLocation(location);

			return WizGlobals.getDocumentsObject(docArray, orderBy);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizPDFOutlineElement> getDocumentsByLocation(
			Context ctx, String userId, String location, String parentId,
			int parentLevel) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index
					.getDocumentsByLocation(location, parentId, parentLevel);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizPDFOutlineElement> getDocumentsByTag(
			Context ctx, String userId, String tagGUID, String parentId,
			int parentLevel) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getDocumentsByTag(tagGUID, parentId, parentLevel);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizDocument> getDocumentsByTag(Context ctx,
			String userId, String tagGUID, int orderBy) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizDocument> docArray = index.getDocumentsByTag(tagGUID);

			return WizGlobals.getDocumentsObject(docArray, orderBy);
		} finally {
			index.closeDatabase();
		}
	}

	// 判断文件在服务器端是否改变
	public static boolean isDocumentServerChanged(Context ctx, String userId,
			String documentGUID) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			WizDocument doc = index.documentFromGUID(documentGUID);
			if (doc == null)
				return false;
			//
			return doc.serverChanged == 1;
		} finally {
			index.closeDatabase();
		}
	}

	// 判断附件在服务器端是否改变
	public static boolean isWizAttachmentServerChanged(Context ctx,
			String userId, String attGUID) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			WizAttachment att = index.attachmentFromGUID(attGUID);
			if (att == null)
				return false;
			//
			return att.serverChanged == 1;
		} finally {
			index.closeDatabase();
		}
	}

	// 获取附件通过GUId
	public static WizAttachment getWizAttachmentByGuid(Context ctx,
			String userId, String attGUID) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			WizAttachment att = index.attachmentFromGUID(attGUID);
			if (att == null)
				return null;
			//
			return att;
		} finally {
			index.closeDatabase();
		}
	}

	// 判断文件是否在本地已存在
	public static boolean isDocumentCached(Context ctx, String userId,
			String documentGUID) {
		String fileName = WizIndex.getDocumentFileName(ctx, userId,
				documentGUID);
		if (!WizGlobals.fileExists(fileName))
			return false;
		//
		return true;
	}

	//
	public static int getPathLevel(String path) {
		int n = countOfCharInString(path, '/');
		if (n < 2)
			return 0;
		return n - 2;
	}

	public static String getLocationLocaleName(Context ctx, String location) {
		int level = WizIndex.getPathLevel(location);
		//
		location = WizGlobals.pathRemoveBackslash(location);
		//
		String name = WizGlobals.extractFileName(location);
		//
		if (0 != level) {
			return name;
		}
		//
		android.content.res.Resources res = ctx.getResources();
		if (name.equals("My Notes"))
			return res.getString(R.string.my_notes);
		else if (name.equals("My Mobiles"))
			return res.getString(R.string.my_mobiles);
		else if (name.equals("My Drafts"))
			return res.getString(R.string.my_drafts);
		else if (name.equals("My Journals"))
			return res.getString(R.string.my_journals);
		else if (name.equals("My Events"))
			return res.getString(R.string.my_events);
		return name;
	}

	public static ArrayList<WizDocument> search(Context ctx, String userId,
			String searchText) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.search(searchText);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizDocument> andOrsearch(Context ctx,
			String userId, String[] searchText) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.andOrsearch(searchText);
		} finally {
			index.closeDatabase();
		}
	}

	public static ArrayList<WizAttachment> getAttachmentsByDocGuid(Context ctx,
			String userId, String docGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.getRecentWizAttachmentByDocGuid(docGuid);
		} finally {

			index.closeDatabase();
		}
	}

	// 删除文件
	public static void deleteCurrentFile(Context ctx, String userId, String id) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizAttachment> wizAtts = index
					.getRecentWizAttachmentByDocGuid(id);
			if (wizAtts != null && wizAtts.size() > 0) {
				index.attDeletedGUID(wizAtts,
						WizGlobals.DATA_INFO_TYPE_ATTACHMENT);
				index.deleteAttachments(id);
			}

			index.deleteDocument(ctx, id);
			index.logDeletedGUID(id, WizGlobals.DATA_INFO_TYPE_DOCUMENT);
			//
		} finally {
			index.closeDatabase();
		}
	}

	// 删除标签
	public static void deleteTagbyGuid(Context ctx, String userId,
			String tagGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.deleteTag(tagGuid);
			index.logDeletedGUID(tagGuid, WizGlobals.DATA_INFO_TYPE_TAG);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean addLocation(Context ctx, String userId,
			String parentLocation, String fileName) {

		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.addLocation(parentLocation, fileName);
		} finally {
			index.closeDatabase();
		}

	}

	public static boolean updateTags(Context ctx, String userId,
			ArrayList<WizTag> arr, boolean newTags) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.updateTags(arr, newTags);
		} finally {
			index.closeDatabase();
		}
		//
	}

	public static boolean updateTag(Context ctx, String userId, WizTag tag,
			boolean newTags) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.updateTag(tag, newTags);
		} finally {
			index.closeDatabase();
		}
	}

	/*
	 * 用于测试
	 */
	public static boolean clearTags(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			index.setTagVersion(0);
			return index.clearTags();
		} finally {
			index.closeDatabase();
		}

	}

	public static boolean setAttachmentLocalChanged(Context ctx, String userId,
			String attGuid, String dataMd5, boolean localChanged) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.setAttachmentLocalChanged(attGuid, dataMd5,
					localChanged);
		} finally {
			index.closeDatabase();
		}

	}

	public static boolean deleteAttachmentByGuid(Context ctx, String userId,
			String attGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			if (!index.isAttachmentExists(attGuid))
				return true;

			return index.deleteAttachments(attGuid);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean isTagHasChild(Context ctx, String userId,
			String tagGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			ArrayList<WizTag> subTags = index.getTags(tagGuid);
			ArrayList<WizDocument> docListForTag = index
					.getDocumentsByTag(tagGuid);
			if (docListForTag != null && docListForTag.size() > 0) {
				return true;
			} else if (subTags != null && subTags.size() > 0) {
				return true;
			} else {
				return false;
			}
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean changeDocumentInfo(Context ctx, String userId,
			String documentGUID, String tagsGuid) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			WizDocument doc = index.documentFromGUID(documentGUID);
			return index.changeDocument(documentGUID, tagsGuid, doc.title,
					doc.type, doc.fileType, doc.location, doc.attachmentCount);
		} finally {
			index.closeDatabase();
		}
	}

	public static void updateDocuments(Context mContext, String userId,
			ArrayList<WizDocument> arr) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.updateDocuments(arr);
		} finally {
			index.closeDatabase();
		}
	}

	public static void updateDocumentsVersion(Context mContext, String userId,
			long newVer, long version) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			if ((newVer + 1) > version) {
				index.setDocumentVersion(newVer + 1);
			}
		} finally {
			index.closeDatabase();
		}
	}

	// 更新Attachment的最高版本信息
	public static void updateTagVersion(Context mContext, String userId,
			long newVer, long version) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			if ((newVer + 1) > version) {

				index.setTagVersion(newVer + 1);
			}
		} finally {
			index.closeDatabase();
		}
	}

	public static void updateDeletedList(Context mContext, String userId,
			ArrayList<WizDeletedGUID> arr, long newVer, long version) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			if (arr.size() == 0)
				return;
			for (int i = 0; i < arr.size(); i++) {
				WizDeletedGUID data = arr.get(i);

				if (data.type.equals(WizGlobals.DATA_INFO_TYPE_DOCUMENT)) {
					index.deleteDocument(mContext, data.guid);
				} else if (data.type.equals(WizGlobals.DATA_INFO_TYPE_TAG)) {
					index.deleteTag(data.guid);
				} else if (data.type
						.equals(WizGlobals.DATA_INFO_TYPE_ATTACHMENT)) {
					index.deleteAttachment(data.guid);
				}
			}
			//
			if (newVer >= version) {

				index.setDeletedGUIDVersion(newVer + 1);
			}
		} finally {
			index.closeDatabase();
		}
	}

	public static void clearDeletedGUIDs(Context mContext, String userId) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.clearDeletedGUIDs();
		} finally {
			index.closeDatabase();
		}
	}

	public static void updateCategories(Context mContext, String userId,
			String[] arr) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.addLocations(arr);
		} finally {
			index.closeDatabase();
		}
		//
	}

	public static void setDocumentInfoByUpload(Context mContext, String userId,
			String guid, String docZipMd5) {
		//
		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.setDocumentInfoByUpload(guid, false, docZipMd5);
		} finally {
			index.closeDatabase();
		}
	}

	// 更新WizAttachment的数据库列表
	public static void updateAttachments(Context mContext, String userId,
			ArrayList<WizAttachment> arr) {

		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.updateAttachments(arr);
		} finally {
			index.closeDatabase();
		}
	}

	// 更新Attachment的最高版本信息
	public static void updateAttachmentsVersion(Context mContext,
			String userId, long newVer, long version) {
		WizIndex index = new WizIndex(mContext, userId);
		try {
			if ((newVer + 1) > version) {
				index.setAttachmentVersion(newVer + 1);
			}
		} finally {
			index.closeDatabase();
		}
	}

	// 更新笔记信息
	public static boolean updateDocument(Context mContext, String userId,
			String documentGuid) {

		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.updateDocumentData(null, documentGuid);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			index.closeDatabase();
		}
	}

	// 更新附件信息
	public static boolean updateAttachment(Context mContext, String userId,
			String attachmentGuid) {

		WizIndex index = new WizIndex(mContext, userId);
		try {
			index.updateAttachmentData(null, attachmentGuid);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean isExistsTagChild(Context ctx, String userId,
			String tagGuid, boolean checkDocument) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isExistsTagChild(tagGuid, checkDocument);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean isExistsDocumentChild(Context ctx, String userId,
			String path, boolean checkDocument) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.isExistsDocumentChild(path, checkDocument);
		} finally {
			index.closeDatabase();
		}
	}

	public static boolean dropCreateLocation(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		try {
			return index.dropTable("WIZ_CREATE_LOCATION");
		} finally {
			index.closeDatabase();
		}

	}

	// 删除同步下来的空根目录*将来修改过滤空文件夹的方式*
	public static void removeEmptyDirectories(Context ctx, String userId) {
		WizIndex index = new WizIndex(ctx, userId);
		ArrayList<String> mLocation;
		try {
			mLocation = index.getLocations("");
			if (mLocation == null || mLocation.size() <= 0)
				return;
			String location = "";
			for (int i = 0; i < mLocation.size(); i++) {
				location = mLocation.get(i);

				index.deleteLocation(location, false);
			}
			mLocation.clear();
		} finally {
			try {
				mLocation = index.getCretateLocations();
				if (mLocation == null || mLocation.size() <= 0)
					return;
				index.addLocations(mLocation);
			} finally {
				index.closeDatabase();
			}
		}
	}

}
