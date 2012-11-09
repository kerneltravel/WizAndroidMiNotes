package cn.code.notes.ui;

import java.util.List;

import cn.code.notes.R;
import cn.code.notes.model.SearchNote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("unused")
public class ListViewAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<SearchNote> list;
	private Context context;

	public ListViewAdapter(Context context, List<SearchNote> list) {
		inflater = LayoutInflater.from(context);
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.search_note_item, null);
		TextView content = (TextView) view.findViewById(R.id.lv_search_item);
		TextView time = (TextView) view.findViewById(R.id.lv_item_time);
		content.setText(list.get(position).getContent());
		time.setText(list.get(position).getTime());
		return view;
	}
}
