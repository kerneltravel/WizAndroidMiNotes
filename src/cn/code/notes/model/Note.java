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

package cn.code.notes.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import cn.code.notes.data.NoteColumnsInterface.ConnectColumns;
import cn.code.notes.data.NoteColumnsInterface.DataColumns;
import cn.code.notes.data.NoteColumnsInterface.NoteColumns;
import cn.code.notes.data.Notes;
import cn.code.notes.data.Notes.CallNote;
import cn.code.notes.data.Notes.TextNote;
import cn.code.notes.share.WizGlobals;

public class Note {
	private ContentValues mNoteDiffValues;
	private NoteData mNoteData;
	private static final String TAG = "Note";
	private NoteConn mNoteConnect;

	/**
	 * Create a new note id for adding a new note to databases
	 */
	public static synchronized long getNewNoteId(Context context, long folderId) {
		// Create a new note in the database
		ContentValues values = new ContentValues();
		long createdTime = System.currentTimeMillis();
		values.put(NoteColumns.CREATED_DATE, createdTime);
		values.put(NoteColumns.MODIFIED_DATE, createdTime);
		values.put(NoteColumns.TYPE, Notes.TYPE_NOTE);
		values.put(NoteColumns.LOCAL_MODIFIED, 1);
		values.put(NoteColumns.PARENT_ID, folderId);
		Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI,
				values);

		long noteId = 0;
		try {
			noteId = Long.valueOf(uri.getPathSegments().get(1));
		} catch (NumberFormatException e) {
			Log.e(TAG, "Get note id error :" + e.toString());
			noteId = 0;
		}
		if (noteId == -1) {
			throw new IllegalStateException("Wrong note id:" + noteId);
		}

		return noteId;
	}

	public Note() {
		mNoteDiffValues = new ContentValues();
		mNoteData = new NoteData();
		mNoteConnect = new NoteConn();
	}

	public void setNoteValue(String key, String value) {
		mNoteDiffValues.put(key, value);
		setNoteLongValue();
	}

	public void setNoteValue(String key, long value) {
		mNoteDiffValues.put(key, value);
		setNoteLongValue();
	}

	public void setNoteLongValue() {
		isEmptyNoteValue(NoteColumns.LOCAL_MODIFIED, 1);
		isEmptyNoteValue(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
	}

	public void setNoteLongValue(long localModified, long dtCreated,
			long dtModified) {
		mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, localModified);
		mNoteDiffValues.put(NoteColumns.CREATED_DATE, dtCreated);
		mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, dtModified);
	}

	public boolean isEmptyNoteValue(String key, String value) {
		String va = String.valueOf(mNoteDiffValues.get(key));
		if (WizGlobals.isEmptyString(va) || va.equals("null")) {
			setNoteValue(key, value);
		}
		return false;
	}

	public boolean isEmptyNoteValue(String key, long value) {
		String va = String.valueOf(mNoteDiffValues.get(key));
		if (WizGlobals.isEmptyString(va) || va.equals("null")) {
			setNoteValue(key, value);
		}
		return false;
	}

	public void setTextData(String key, String value) {
		mNoteData.setTextData(key, value);
	}

	public void setTextDataId(long id) {
		mNoteData.setTextDataId(id);
	}

	public void setConnectValues(String key, String value) {
		mNoteConnect.setConnectValues(key, value);
	}

	public void setConnectNoteId(long id) {
		mNoteConnect.setNoteId(id);
	}

	public void setConnectValueInt(String key, int value) {
		mNoteConnect.setConnectValueInt(key, value);
	}

	public boolean isEmptyConnectValue(String key) {
		return mNoteConnect.isEmptyConnectValue(key);
	}

	public boolean isEmptyConnectValue(String key, String value) {
		if (isEmptyConnectValue(key)) {
			setConnectValues(key, value);
			return true;
		}
		return false;
	}

	public boolean isEmptyConnectValueInt(String key, int value) {
		if (mNoteConnect.isEmptyConnectValue(key)) {
			setConnectValueInt(key, value);
			return true;
		}
		return false;
	}

	public long getTextDataId() {
		return mNoteData.mTextDataId;
	}

	public void setCallDataId(long id) {
		mNoteData.setCallDataId(id);
	}

	public void setCallData(String key, String value) {
		mNoteData.setCallData(key, value);
	}

	public boolean isLocalModified() {
		return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified()
				|| mNoteConnect.isLocalModified();
	}

	public boolean syncNote(Context context, long noteId) {
		if (noteId <= 0) {
			throw new IllegalArgumentException("Wrong note id:" + noteId);
		}

		if (!isLocalModified()) {
			return true;
		}

		/**
		 * In theory, once data changed, the note should be updated on
		 * {@link NoteColumns#LOCAL_MODIFIED} and
		 * {@link NoteColumns#MODIFIED_DATE}. For data safety, though update
		 * note fails, we also update the note data info
		 */
		if (context.getContentResolver().update(
				ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
				mNoteDiffValues, null, null) == 0) {
			Log.e(TAG, "Update note error, should not happen");
		}
		mNoteDiffValues.clear();

		if (mNoteData.isLocalModified()
				&& (mNoteData.pushIntoContentResolver(context, noteId) == null)) {
		}
		if (mNoteConnect.isLocalModified()
				&& (mNoteConnect.pushWizIntoContentResolver(context, noteId) == null)) {
			return false;
		}

		return true;
	}

	private class NoteData {
		private long mTextDataId;

		private ContentValues mTextDataValues;

		private long mCallDataId;

		private ContentValues mCallDataValues;

		private static final String TAG = "NoteData";

		public NoteData() {
			mTextDataValues = new ContentValues();
			mCallDataValues = new ContentValues();
			mTextDataId = 0;
			mCallDataId = 0;
		}

		boolean isLocalModified() {
			return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
		}

		void setTextDataId(long id) {
			if (id <= 0) {
				throw new IllegalArgumentException(
						"Text data id should larger than 0");
			}
			mTextDataId = id;
		}

		void setCallDataId(long id) {
			if (id <= 0) {
				throw new IllegalArgumentException(
						"Call data id should larger than 0");
			}
			mCallDataId = id;
		}

		void setCallData(String key, String value) {
			mCallDataValues.put(key, value);
		}

		void setTextData(String key, String value) {
			mTextDataValues.put(key, value);
		}

		Uri pushIntoContentResolver(Context context, long noteId) {
			/* Check for safety */
			if (noteId <= 0) {
				throw new IllegalArgumentException("Wrong note id:" + noteId);
			}

			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			ContentProviderOperation.Builder builder = null;

			if (mTextDataValues.size() > 0) {
				mTextDataValues.put(DataColumns.NOTE_ID, noteId);
				if (mTextDataId == 0) {
					mTextDataValues.put(DataColumns.MIME_TYPE,
							TextNote.CONTENT_ITEM_TYPE);
					Uri uri = context.getContentResolver().insert(
							Notes.CONTENT_DATA_URI, mTextDataValues);
					try {
						setTextDataId(Long
								.valueOf(uri.getPathSegments().get(1)));
					} catch (NumberFormatException e) {
						Log.e(TAG, "Insert new text data fail with noteId"
								+ noteId);
						mTextDataValues.clear();
						return null;
					}
				} else {
					builder = ContentProviderOperation
							.newUpdate(ContentUris.withAppendedId(
									Notes.CONTENT_DATA_URI, mTextDataId));
					builder.withValues(mTextDataValues);
					operationList.add(builder.build());
				}
				mTextDataValues.clear();
			}

			if (mCallDataValues.size() > 0) {
				mCallDataValues.put(DataColumns.NOTE_ID, noteId);
				if (mCallDataId == 0) {
					mCallDataValues.put(DataColumns.MIME_TYPE,
							CallNote.CONTENT_ITEM_TYPE);
					Uri uri = context.getContentResolver().insert(
							Notes.CONTENT_DATA_URI, mCallDataValues);
					try {
						setCallDataId(Long
								.valueOf(uri.getPathSegments().get(1)));
					} catch (NumberFormatException e) {
						Log.e(TAG, "Insert new call data fail with noteId"
								+ noteId);
						mCallDataValues.clear();
						return null;
					}
				} else {
					builder = ContentProviderOperation
							.newUpdate(ContentUris.withAppendedId(
									Notes.CONTENT_DATA_URI, mCallDataId));
					builder.withValues(mCallDataValues);
					operationList.add(builder.build());
				}
				mCallDataValues.clear();
			}

			if (operationList.size() > 0) {
				try {
					ContentProviderResult[] results = context
							.getContentResolver().applyBatch(Notes.AUTHORITY,
									operationList);
					return (results == null || results.length == 0 || results[0] == null) ? null
							: ContentUris.withAppendedId(
									Notes.CONTENT_NOTE_URI, noteId);
				} catch (RemoteException e) {
					Log.e(TAG,
							String.format("%s: %s", e.toString(),
									e.getMessage()));
					e.printStackTrace();
					return null;
				} catch (OperationApplicationException e) {
					Log.e(TAG,
							String.format("%s: %s", e.toString(),
									e.getMessage()));
					return null;
				}
			}
			return null;
		}

	}

	public class NoteConn {

		private static final String TAG = "NoteConnect";
		private ContentValues mConnValues;
		private ContentValues mConnCellValues;
		private long mConnId = 0;
		private long mConnCallId = 0;

		public NoteConn() {
			mConnValues = new ContentValues();
			mConnCellValues = new ContentValues();
		}

		boolean isLocalModified() {
			return mConnValues.size() > 0;
		}

		public void setNoteId(long id) {
			if (id <= 0) {
				throw new IllegalArgumentException(
						"Text data id should larger than 0");
			}
			mConnId = id;
		}

		void setConnectValues(String key, String value) {
			mConnValues.put(key, value);
		}

		void setConnectValueInt(String key, int value) {
			mConnValues.put(key, value);
		}

		boolean isEmptyConnectValue(String key) {
			String value = String.valueOf(mConnValues.get(key));
			return WizGlobals.isEmptyString(value) || value.equals("null");
		}

		Uri pushWizIntoContentResolver(Context context, long id) {
			/* Check for safety */
			if (id <= 0) {
				throw new IllegalArgumentException("Wrong note id:" + id);
			}

			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			ContentProviderOperation.Builder builder = null;

			if (mConnValues.size() > 0) {
				mConnValues.put(ConnectColumns.NOTE_ID, id);
				Note.this.isEmptyConnectValue(ConnectColumns.MODIFIED_DATE,
						WizGlobals.getCurrentSQLDateTimeString());
				if (mConnId == 0) {
					context.getContentResolver().insert(Notes.CONTENT_CONN_URI,
							mConnValues);
					// Uri uri =
					// try {
					// String ss = uri.getPathSegments().get(1);
					// Long lng = Long.valueOf(uri.getPathSegments().get(1));
					// setConnectNoteId(Long.valueOf(uri.getPathSegments()
					// .get(2)));
					// } catch (NumberFormatException e) {
					// Log.e(TAG, "Insert new text data fail with noteId" + id);
					// mConnValues.clear();
					// return null;
					// }
				} else {
					builder = ContentProviderOperation.newUpdate(ContentUris
							.withAppendedId(Notes.CONTENT_CONN_URI, id));
					builder.withValues(mConnValues);
					operationList.add(builder.build());
				}
				mConnValues.clear();
			}

			if (mConnCellValues.size() > 0) {
				mConnCellValues.put(ConnectColumns.NOTE_ID, id);
				if (mConnCallId == 0) {
					Note.this.isEmptyConnectValue(ConnectColumns.MODIFIED_DATE,
							WizGlobals.getCurrentSQLDateTimeString());
					Uri uri = context.getContentResolver().insert(
							Notes.CONTENT_CONN_URI, mConnCellValues);
					try {
						setCallDataId(Long
								.valueOf(uri.getPathSegments().get(1)));
					} catch (NumberFormatException e) {
						Log.e(TAG, "Insert new call data fail with noteId"
								+ mConnId);
						mConnCellValues.clear();
						return null;
					}
				} else {
					builder = ContentProviderOperation
							.newUpdate(ContentUris.withAppendedId(
									Notes.CONTENT_DATA_URI, mConnCallId));
					builder.withValues(mConnCellValues);
					operationList.add(builder.build());
				}
				mConnCellValues.clear();
			}

			if (operationList.size() > 0) {
				try {
					ContentProviderResult[] results = context
							.getContentResolver().applyBatch(Notes.AUTHORITY,
									operationList);
					return (results == null || results.length == 0 || results[0] == null) ? null
							: ContentUris.withAppendedId(
									Notes.CONTENT_CONN_URI, id);
				} catch (RemoteException e) {
					Log.e(TAG,
							String.format("%s: %s", e.toString(),
									e.getMessage()));
					return null;
				} catch (OperationApplicationException e) {
					Log.e(TAG,
							String.format("%s: %s", e.toString(),
									e.getMessage()));
					return null;
				} catch (Exception e) {
					return null;
				}
			}
			return null;
		}

	}

}
