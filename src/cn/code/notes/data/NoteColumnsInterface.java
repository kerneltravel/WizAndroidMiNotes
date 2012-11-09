package cn.code.notes.data;

public class NoteColumnsInterface {
	/* Note表 */
	public interface NoteColumns {
		/* The unique ID for a row<P>Type: INTEGER (long)</P> */
		public static final String ID = "_id";

		/* The parent's id for note or folder<P>Type: INTEGER (long)</P> */
		public static final String PARENT_ID = "parent_id";

		/* Created data for note or folder<P>Type: INTEGER (long)</P> */
		public static final String CREATED_DATE = "created_date";

		/* Latest modified date<P>Type: INTEGER (long)</P> */
		public static final String MODIFIED_DATE = "modified_date";

		/* Alert date<P>Type: INTEGER (long)</P> */
		public static final String ALERTED_DATE = "alert_date";

		/* Folder's name or text content of note<P>Type: TEXT</P> */
		public static final String SNIPPET = "snippet";

		/* Note's widget id<P>Type: INTEGER (long)</P> */
		public static final String WIDGET_ID = "widget_id";

		/* Note's widget type<P>Type: INTEGER (long)</P> */
		public static final String WIDGET_TYPE = "widget_type";

		/* Note's background color's id<P>Type: INTEGER (long)</P> */
		public static final String BG_COLOR_ID = "bg_color_id";

		/*
		 * For text note, it doesn't has attachment, for multi-media note, it
		 * has at least one attachment<P>Type: INTEGER</P>
		 */
		public static final String HAS_ATTACHMENT = "has_attachment";

		/* Folder's count of notes<P>Type: INTEGER (long)</P> */
		public static final String NOTES_COUNT = "notes_count";

		/* The file type: folder or note<P>Type: INTEGER</P> */
		public static final String TYPE = "type";

		/* The last sync id <P>Type: INTEGER (long)</P> */
		public static final String SYNC_ID = "sync_id";

		/* Sign to indicate local modified or not<P>Type: INTEGER</P> */
		public static final String LOCAL_MODIFIED = "local_modified";

		/*
		 * Original parent id before moving into temporary
		 * folder<P>Type:INTEGER</P>
		 */
		public static final String ORIGIN_PARENT_ID = "origin_parent_id";

		/* The gtask id<P>Type:TEXT</P> */
		public static final String GTASK_ID = "gtask_id";

		/* The version code<P>Type : INTEGER (long)</P> */
		public static final String VERSION = "version";
	}

	/* DATA表 */
	public interface DataColumns {
		/* The unique ID for a row<P>Type: INTEGER (long)</P> */
		public static final String ID = "_id";

		/* The MIME type of the item represented by this row.<P>Type: Text</P> */
		public static final String MIME_TYPE = "mime_type";

		/*
		 * The reference id to note that this data belongs to<P>Type: INTEGER
		 * (long)</P>
		 */
		public static final String NOTE_ID = "note_id";

		/* Created data for note or folder<P>Type: INTEGER (long)</P> */
		public static final String CREATED_DATE = "created_date";

		/* Latest modified date<P>Type: INTEGER (long)</P> */
		public static final String MODIFIED_DATE = "modified_date";

		/* Data's content<P>Type: TEXT</P> */
		public static final String CONTENT = "content";

		/**
		 * Generic data column, the meaning is {@link #MIMETYPE} specific, used
		 * for integer data type
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DATA1 = "data1";

		/**
		 * Generic data column, the meaning is {@link #MIMETYPE} specific, used
		 * for integer data type
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DATA2 = "data2";

		/**
		 * Generic data column, the meaning is {@link #MIMETYPE} specific, used
		 * for TEXT data type
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATA3 = "data3";

		/**
		 * Generic data column, the meaning is {@link #MIMETYPE} specific, used
		 * for TEXT data type
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATA4 = "data4";

		/**
		 * Generic data column, the meaning is {@link #MIMETYPE} specific, used
		 * for TEXT data type
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATA5 = "data5";
	}

	/* CONNECT表 */
	public interface ConnectColumns {
		/**
		 * The unique ID for a row
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String CONNECT_ID = "ID";
		/**
		 * The unique ID for a row
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String NOTE_CONNECT_ID = "CONNECT_ID";
		/**
		 * The unique ID for a row
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String NOTE_GUID = "NOTE_GUID";

		/**
		 * The reference id to note that this data belongs to
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String NOTE_ID = "NOTE_ID";

		/**
		 * note data md5
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String NOTE_DATA_MD5 = "NOTE_DATA_MD5";

		/**
		 * Created data for note or folder
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String CREATED_DATE = "CREATED_DATE";

		/**
		 * Latest modified date
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String MODIFIED_DATE = "MODIFIED_DATE";
		/**
		 * Note title
		 * <P>
		 * Type: String
		 * </P>
		 */
		public static final String NOTE_TITLE = "NOTE_TITLE";
		/**
		 * Note tagGuids
		 * <P>
		 * Type: String
		 * </P>
		 */
		public static final String NOTE_TAG_GUIDS = "NOTE_TAG_GUIDS";
		/**
		 * Note LOCATION
		 * <P>
		 * Type: String
		 * </P>
		 */
		public static final String NOTE_LOCATION = "NOTE_LOCATION";
		/**
		 * Note TYPE
		 * <P>
		 * Type: String
		 * </P>
		 */
		public static final String NOTE_TYPE = "NOTE_TYPE";
		/**
		 * Note filetype
		 * <P>
		 * Type: String
		 * </P>
		 */
		public static final String NOTE_FILE_TYPE = "NOTE_FILE_TYPE";
		/**
		 * Note attCount
		 * <P>
		 * Type: INTEGER(int)
		 * </P>
		 */
		public static final String NOTE_ATT_COUNT = "NOTE_ATT_COUNT";
	}

	/* Meta表 */
	public interface MetaColumns {
		public static final String META_NAME = "META_NAME";
		public static final String META_KEY = "META_KEY";
		public static final String META_VALUE = "META_VALUE";
		public static final String DT_MODIFIED = "DT_MODIFIED";
	}

	/* DELETED表 */
	public interface DeletedColumns {
		public static final String DELETED_GUID = "DELETED_GUID";
		public static final String GUID_TYPE = "GUID_TYPE";
		public static final String DT_DELETED = "DT_DELETED";
	}

}