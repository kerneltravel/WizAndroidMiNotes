package cn.code.notes.share;

public class WizPDFOutlineElement {
	private int level;// ��ǰ�ڵ����ڲ��
	private int mhasChildCount;// �ýڵ��´��ڵ��ӽڵ�(�������ĵ���������ǩ��Ŀ¼)��
	private String id;// ��ǰid
	private String objType;// �ڵ�����
	private String outlineTitle;// �ڵ�������ʾ����Ϣ
	private String location;// ��ǰ�ڵ��Ŀ¼������ļ��д��ڱ���������ĵ�Ϊ����
	private String parent;// ���ڵ�id
	private boolean mhasParent;// �Ƿ��и��׽ڵ�
	private boolean mhasChild;// �Ƿ��к��ӽڵ�
	private boolean expanded;// �ж��Ƿ�չ��
	private Object doc;// �ļ���guid���ļ��еĴ�ֵΪnull

	public WizPDFOutlineElement(String id, Object doc, String type,
			String outlineTitle, boolean mhasParent, boolean mhasChild,
			int mhasChildCount, String parent, String mLocation, int level,
			boolean expanded) {
		super();
		this.level = level;
		this.mhasChildCount = mhasChildCount;
		this.id = id;
		this.objType = type;
		this.outlineTitle = outlineTitle;
		this.location = mLocation;
		this.parent = parent;
		this.mhasParent = mhasParent;
		this.mhasChild = mhasChild;
		this.expanded = expanded;
		this.doc = doc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getDoc() {
		return doc;
	}

	public void setDoc(Object doc) {
		this.doc = doc;
	}

	public String getObjType() {
		return objType;
	}

	public void setObjType(String objType) {
		this.objType = objType;
	}

	public String getOutlineTitle() {
		return outlineTitle;
	}

	public void setOutlineTitle(String outlineTitle) {
		this.outlineTitle = outlineTitle;
	}

	public boolean isMhasParent() {
		return mhasParent;
	}

	public void setMhasParent(boolean mhasParent) {
		this.mhasParent = mhasParent;
	}

	public boolean isMhasChild() {
		return mhasChild;
	}

	public void setMhasChild(boolean mhasChild) {
		this.mhasChild = mhasChild;
	}

	public int getMhasChildCount() {
		return mhasChildCount;
	}

	public void setMhasChildCount(int mhasChildCount) {
		this.mhasChildCount = mhasChildCount;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
