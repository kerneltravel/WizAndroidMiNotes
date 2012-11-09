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

package cn.code.notes.ui;

import cn.code.notes.R;
import android.app.Activity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class DropdownMenu extends Activity {
	// private Button mButton;
	// private PopupMenu mPopupMenu;
	// private Menu mMenu;
	//
	// public DropdownMenu(Context context, Button button, int menuId) {
	// mButton = button;
	// mButton.setBackgroundResource(R.drawable.dropdown_icon);
	// mPopupMenu = new PopupMenu(context, mButton);
	// mMenu = mPopupMenu.getMenu();
	// mPopupMenu.getMenuInflater().inflate(menuId, mMenu);
	// mButton.setOnClickListener(new OnClickListener() {
	// public void onClick(View v) {
	// mPopupMenu.show();
	// }
	// });
	// }
	//
	// public void setOnDropdownMenuItemClickListener(
	// OnMenuItemClickListener listener) {
	// if (mPopupMenu != null) {
	// mPopupMenu.setOnMenuItemClickListener(listener);
	// }
	// }
	//
	// public MenuItem findItem(int id) {
	// return mMenu.findItem(id);
	// }
	//
	// public void setTitle(CharSequence title) {
	// mButton.setText(title);
	// }

	// 该方法在注册的view被被长按时创建该view的上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_list, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// 当菜单某个选项被点击时调用该方法
	@SuppressWarnings("unused")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Auto-generated method stub
		AdapterContextMenuInfo infor = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_new_folder:
			return true;
		case R.id.menu_export_text:
			return true;
		case R.id.menu_sync:
			return true;
		case R.id.menu_setting:
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	// 当上下文菜单关闭时调用的方法
	@Override
	public void onContextMenuClosed(Menu menu) {
		// Auto-generated method stub
		super.onContextMenuClosed(menu);
	}

}
