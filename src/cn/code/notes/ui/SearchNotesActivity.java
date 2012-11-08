package cn.code.notes.ui;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.code.notes.R;
import cn.code.notes.model.SearchNote;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.TextView;

public class SearchNotesActivity extends Activity implements
		OnItemSelectedListener {
	private TextView searchTitle;
	private ListView listView;
	private List<SearchNote> list = new ArrayList<SearchNote>();
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_note_list);
		initResources();
		String keyWord = getIntent().getStringExtra(SearchManager.QUERY);
		list = queryNotes(keyWord);
		upDateTitle(count, keyWord);
	}

	private void upDateTitle(int count2, String query) {
		String title = getString(R.string.search_title, count2, query);
		searchTitle.setText(title);
	}

	private void initResources() {
		searchTitle = (TextView) findViewById(R.id.tv_search_title);
		listView = (ListView) findViewById(R.id.search_notes_list);
	}

	@Override
	protected void onStart() {
		super.onStart();
		ListViewAdapter listViewAdapter = new ListViewAdapter(
				getApplicationContext(), list);
		listView.setAdapter(listViewAdapter);

	}

	private List<SearchNote> queryNotes(String keyWord) {

		List<SearchNote> list1 = new ArrayList<SearchNote>();
		if (!TextUtils.isEmpty(keyWord)) {
			ContentResolver contentResolver = getContentResolver();
			Uri uri = Uri.parse("content://code_notes/note");
			Cursor cursor = contentResolver.query(uri, null, "snippet like ?",
					new String[] { "%" + keyWord + "%" }, "modified_date");
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex("_id"));
				String content = cursor.getString(cursor
						.getColumnIndex("snippet"));
				long time = cursor.getLong(cursor
						.getColumnIndex("modified_date"));
				String date = formatTime(time);
				String modifiedDate = date + "";
				SearchNote searchNote = new SearchNote();
				searchNote.setId(id);
				searchNote.setContent(content);
				searchNote.setTime(modifiedDate);
				list1.add(searchNote);
				count++;
			}
			cursor.close();
		}
		return list1;
	}

	private String formatTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = sdf.format(new Date(time));
		return date;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
