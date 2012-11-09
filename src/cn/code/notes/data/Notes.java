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

package cn.code.notes.data;

import cn.code.notes.data.NoteColumnsInterface.DataColumns;
import cn.code.notes.data.NotesDatabaseHelper.TABLE;
import android.net.Uri;

public class Notes {
	public static final String AUTHORITY = "code_notes";
	public static final String TAG = "Notes";
	public static final int TYPE_NOTE = 0;
	public static final int TYPE_FOLDER = 1;
	public static final int TYPE_SYSTEM = 2;

	/**
	 * Following IDs are system folders' identifiers
	 * {@link Notes#ID_ROOT_FOLDER } is default folder
	 * {@link Notes#ID_TEMPARAY_FOLDER } is for notes belonging no folder
	 * {@link Notes#ID_CALL_RECORD_FOLDER} is to store call records
	 */
	public static final int ID_ROOT_FOLDER = 0;
	public static final int ID_TEMPARAY_FOLDER = -1;
	public static final int ID_CALL_RECORD_FOLDER = -2;
	public static final int ID_TRASH_FOLER = -3;

	public static final String INTENT_EXTRA_ALERT_DATE = "cn.code.notes.alert_date";
	public static final String INTENT_EXTRA_BACKGROUND_ID = "cn.code.notes.background_color_id";
	public static final String INTENT_EXTRA_WIDGET_ID = "cn.code.notes.widget_id";
	public static final String INTENT_EXTRA_FOLDER_ID = "cn.code.notes.folder_id";
	public static final String INTENT_EXTRA_WIDGET_TYPE = "cn.code.notes.widget_type";
	public static final String INTENT_EXTRA_CALL_DATE = "cn.code.notes.call_date";

	public static final int TYPE_WIDGET_INVALIDE = -1;
	public static final int TYPE_WIDGET_2X = 0;
	public static final int TYPE_WIDGET_4X = 1;

	public static class DataConstants {
		public static final String NOTE = TextNote.CONTENT_ITEM_TYPE;
		public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE;
	}

	/* Uri to query all notes and folders */
	public static final Uri CONTENT_NOTE_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE.NOTE);

	/* Uri to query data */
	public static final Uri CONTENT_DATA_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE.DATA);

	/* Uri to query wiz data */
	public static final Uri CONTENT_CONN_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE.CONN);

	/* Uri to query wiz data */
	public static final Uri CONTENT_META_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE.META);

	/* Uri to query wiz data */
	public static final Uri CONTENT_DELETED_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE.DELETED_GUID);

	public static final class TextNote implements DataColumns {
		/**
		 * Mode to indicate the text in check list mode or not
		 * <P>
		 * Type: Integer 1:check list mode 0: normal mode
		 * </P>
		 */
		public static final String MODE = DATA1;

		public static final int MODE_CHECK_LIST = 1;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/text_note");
	}

	public static final class CallNote implements DataColumns {
		/**
		 * Call date for this record
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String CALL_DATE = DATA1;

		/**
		 * Phone number for this record
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String PHONE_NUMBER = DATA3;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/call_note");
	}
}
