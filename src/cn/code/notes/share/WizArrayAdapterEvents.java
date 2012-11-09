package cn.code.notes.share;

import android.view.View;
import android.view.ViewGroup;

public interface WizArrayAdapterEvents {
	abstract public View getView(int position, View convertView, ViewGroup parent);
}
