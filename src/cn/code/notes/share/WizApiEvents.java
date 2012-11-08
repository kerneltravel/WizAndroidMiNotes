package cn.code.notes.share;

public interface WizApiEvents {
	// userId��ʾ��ǰ������Id��actionName��ʾ��ǰ�Ĳ�������
	public abstract void onApiBegin(String userId, String actionName);

	public abstract void onShowMessage(String userId, String actionName,
			int arg1, int arg2, String mMessage);

	public abstract void onApiEnd(String userId, String actionName,
			boolean succeeded);

	public abstract void onApiError(String userId, String actionName,
			int stringID, String errorMessage);
}
