/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.code.notes.gtask.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cn.code.notes.data.Notes;
import cn.code.notes.data.NoteColumnsInterface.ConnectColumns;
import cn.code.notes.data.NoteColumnsInterface.DataColumns;
import cn.code.notes.data.NoteColumnsInterface.NoteColumns;
import cn.code.notes.data.NotesDatabaseHelper.TABLE;
import cn.code.notes.gtask.exception.ActionFailureException;

public class SqlConn {
	private static final String TAG = SqlConn.class.getSimpleName();

	private static final int INVALID_ID = -99999;

	public static final String[] PROJECTION_DATA = new String[] {
			ConnectColumns.NOTE_GUID, ConnectColumns.NOTE_ID,
			ConnectColumns.NOTE_DATA_MD5 };

	public static final int GUID_COLUMN = 0;
	public static final int ID_COLUMN = 1;
	public static final int MD5_COLUMN = 2;

	private ContentResolver mContentResolver;

	private boolean mIsCreate;

	private String mGuid;

	private long mId;
	private String mDataMD5;

	private ContentValues mDiffDataValues;

	public SqlConn(Context context) {
		mContentResolver = context.getContentResolver();
		mIsCreate = true;
		mGuid = "";
		mId = INVALID_ID;
		mDataMD5 = "";
		mDiffDataValues = new ContentValues();
	}

	public SqlConn(Context context, Cursor c) {
		mContentResolver = context.getContentResolver();
		mIsCreate = false;
		loadFromCursor(c);
		mDiffDataValues = new ContentValues();
	}

	private void loadFromCursor(Cursor c) {
		mGuid = c.getString(GUID_COLUMN);
		mId = c.getLong(ID_COLUMN);
		mDataMD5 = c.getString(MD5_COLUMN);

	}

	public void setContent(JSONObject js) throws JSONException {

		String dataGUID = js.has(ConnectColumns.NOTE_GUID) ? js
				.getString(ConnectColumns.NOTE_GUID) : "";
		if (mIsCreate || mGuid.equals(dataGUID))
			mDiffDataValues.put(ConnectColumns.NOTE_GUID, dataGUID);
		mGuid = dataGUID;

		long dataId = js.has(ConnectColumns.NOTE_ID) ? js
				.getLong(ConnectColumns.NOTE_ID) : INVALID_ID;
		if (mIsCreate || mId != dataId) {
			mDiffDataValues.put(ConnectColumns.NOTE_ID, dataId);
		}
		mId = dataId;

		String dataMD5 = js.has(ConnectColumns.NOTE_DATA_MD5) ? js
				.getString(ConnectColumns.NOTE_DATA_MD5) : "";
		if (mIsCreate || mDataMD5.equals(dataMD5))
			mDiffDataValues.put(ConnectColumns.NOTE_GUID, dataMD5);
		mGuid = dataMD5;
	}

	public JSONObject getContent() throws JSONException {
		if (mIsCreate) {
			Log.e(TAG, "it seems that we haven't created this in database yet");
			return null;
		}
		JSONObject js = new JSONObject();
		js.put(ConnectColumns.NOTE_GUID, mGuid);
		js.put(ConnectColumns.NOTE_ID, mId);
		js.put(ConnectColumns.NOTE_DATA_MD5, mDataMD5);

		return js;
	}

	public void commit(long noteId, boolean validateVersion, long version) {

		if (mIsCreate) {
			if (mId == INVALID_ID
					&& mDiffDataValues.containsKey(ConnectColumns.NOTE_ID)) {
				mDiffDataValues.remove(ConnectColumns.NOTE_ID);
			}

			mDiffDataValues.put(DataColumns.NOTE_ID, noteId);
			Uri uri = mContentResolver.insert(Notes.CONTENT_CONN_URI,
					mDiffDataValues);
			try {
				mId = Long.valueOf(uri.getPathSegments().get(1));
			} catch (NumberFormatException e) {
				Log.e(TAG, "Get note id error :" + e.toString());
				throw new ActionFailureException("create note failed");
			}
		} else {
			if (mDiffDataValues.size() > 0) {
				int result = 0;
				if (!validateVersion) {
					result = mContentResolver.update(ContentUris
							.withAppendedId(Notes.CONTENT_CONN_URI, mId),
							mDiffDataValues, null, null);
				} else {
					result = mContentResolver.update(
							ContentUris.withAppendedId(Notes.CONTENT_CONN_URI,
									mId),
							mDiffDataValues,
							" ? in (SELECT " + NoteColumns.ID + " FROM "
									+ TABLE.NOTE + " WHERE "
									+ NoteColumns.VERSION + "=?)",
							new String[] { String.valueOf(noteId),
									String.valueOf(version) });
				}
				if (result == 0) {
					Log.w(TAG,
							"there is no update. maybe user updates note when syncing");
				}
			}
		}

		mDiffDataValues.clear();
		mIsCreate = false;
	}

	public String getmGuid() {
		return mGuid;
	}

	public long getmId() {
		return mId;
	}

	public String getmDataMD5() {
		return mDataMD5;
	}
}
