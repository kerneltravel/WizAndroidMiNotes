package cn.code.notes.share;

import java.util.ArrayList;

public class WizApiEventsListener implements WizApiEvents {

	ArrayList<WizApiEvents> mListener;

	private WizApiEventsListener() {
		mListener = new ArrayList<WizApiEvents>();
	}

	public void addListener(WizApiEvents events) {
		mListener.add(events);
	}

	public void removeListener(WizApiEvents events) {
		int index = mListener.indexOf(events);
		if (-1 == index)
			return;
		mListener.remove(index);
	}

	public void onApiBegin(String userId, String actionName) {

		if (actionName.equals("WizSync")) {
			WizGlobalData.sharedData().setSyncing(userId, true);
		}

		for (int i = 0; i < mListener.size(); i++) {
			mListener.get(i).onApiBegin(userId, actionName);
		}
	}

	public void onShowMessage(String userId, String actionName, int arg1,
			int arg2, String mMessage) {
		for (int i = 0; i < mListener.size(); i++) {
			mListener.get(i).onShowMessage(userId, actionName, arg1, arg2,
					mMessage);
		}
	}

	public void onApiEnd(String userId, String actionName, boolean succeeded) {
		
		if (actionName.equals("WizSync")) {
			WizGlobalData.sharedData().setSyncing(userId, false);
		}

		for (int i = 0; i < mListener.size(); i++) {
			mListener.get(i).onApiEnd(userId, actionName, succeeded);
		}
	}

	public void onApiError(String userId, String actionName, int stringID,
			String errorMessage) {
		for (int i = 0; i < mListener.size(); i++) {
			mListener.get(i).onApiError(userId, actionName, stringID,
					errorMessage);
		}
	}

	//
	private static WizApiEventsListener mEventsListener;

	public static WizApiEventsListener sharedListener() {
		if (mEventsListener == null) {
			mEventsListener = new WizApiEventsListener();
		}
		return mEventsListener;
	}

	public static void add(WizApiEvents events) { // 6 Ìí¼ÓÊÂ¼þ
		sharedListener().addListener(events);
	}

	public static void remove(WizApiEvents events) {
		sharedListener().removeListener(events);
	}
}