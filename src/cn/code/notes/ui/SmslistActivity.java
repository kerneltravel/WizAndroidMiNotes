package cn.code.notes.ui;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import cn.code.notes.R;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

@SuppressWarnings("deprecation")
public class SmslistActivity extends ListActivity {

	private ListView listView;
	private SimpleAdapter adapter;
	private int VIEW_COUNT = 20;
	private int index = 0;
	private int totalCount;
	private int maxResult;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initResources();
	}

	private void initResources() {
		listView = getListView();
		totalCount = getCount();
		maxResult = getMaxResult();
		list = querySms(index, maxResult);
		adapter = new SimpleAdapter(this, list, R.layout.sms_item,
				new String[] { "address", "date", "content" },
				new int[] { R.id.sms_tv_author, R.id.sms_tv_time,
						R.id.sms_tv_item });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				HashMap<String, String> map = list.get(arg2);
				String content = map.get("content");
				Intent intent = new Intent(SmslistActivity.this,
						NoteEditActivity.class);
				intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
				intent.putExtra("smsContent", content);
				startActivity(intent);
				SmslistActivity.this.finish();
			}
		});
	}

	private ArrayList<HashMap<String, String>> querySms(int firstResult,
			int maxResult) {
		String[] projection = new String[] { "address", "date", "body" };
		try {
			String limit = " date desc limit " + firstResult + "," + maxResult
					+ "";
			System.out.println("SQL" + limit);
			Cursor myCursor = managedQuery(Uri.parse("content://sms/inbox"),
					projection, null, null, limit);
			return getData(myCursor);
		} catch (SQLiteException ex) {
		}
		return null;
	}

	private ArrayList<HashMap<String, String>> getData(Cursor cur) {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		if (cur.moveToFirst()) {

			String address;
			String date;
			String sms;

			int phoneColumn = cur.getColumnIndex("address");
			int dateColumn = cur.getColumnIndex("date");
			int smsColumn = cur.getColumnIndex("body");

			do {
				HashMap<String, String> map = new HashMap<String, String>();
				// Get the field values
				address = cur.getString(phoneColumn);
				date = cur.getString(dateColumn);
				sms = cur.getString(smsColumn);
				date = sdf.format(new Timestamp(Long.parseLong(date)));
				address = queryName(address);
				map.put("address", "From:" + address);
				map.put("date", date);
				map.put("content", " " + sms);
				list.add(map);
			} while (cur.moveToNext());
		}
		return list;
	}

	private String queryName(String id) {
		String name = id;
		String columns[] = new String[] { People.NAME };
		Uri mContacts = People.CONTENT_URI;
		Cursor cur = managedQuery(mContacts, columns, // 要返回的数据字段
				"People.NUMBER=?", // WHERE子句
				new String[] { id }, // WHERE 子句的参数
				People.NAME // Order-by子句
		);

		if (cur.getCount() != 0) {
			cur.moveToFirst();
			name = cur.getString(cur.getColumnIndex(People.NAME));
			// System.out.println("name---->"+name);
		}
		return name;
	}

	private int getMaxResult() {
		int totalPage = (totalCount + VIEW_COUNT - 1) / VIEW_COUNT;
		return totalCount - (totalPage - 1) * VIEW_COUNT;
	}

	private int getCount() {
		Cursor myCursor = managedQuery(Uri.parse("content://sms/inbox"), null,
				null, null, null);
		System.out.println("count" + myCursor.getCount());
		return myCursor.getCount();
	}

	class AsyncUpdateDatasTask extends
			AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				Void... params) {
			index += VIEW_COUNT;
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
			list = querySms(index, maxResult);
			return list;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			list.addAll(result);
			adapter.notifyDataSetChanged();
		}
	}
}
