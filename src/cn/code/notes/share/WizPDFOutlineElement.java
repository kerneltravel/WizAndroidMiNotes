package cn.code.notes.share;

public class WizPDFOutlineElement {
	private int level;// 当前节点所在层次
	private int mhasChildCount;// 该节点下存在的子节点(这里是文档不包括标签、目录)数
	private String id;// 当前id
	private String objType;// 节点类型
	private String outlineTitle;// 节点上面显示的信息
	private String location;// 当前节点的目录如果是文件夹存在本身，如果是文档为“”
	private String parent;// 父节点id
	private boolean mhasParent;// 是否有父亲节点
	private boolean mhasChild;// 是否有孩子节点
	private boolean expanded;// 判断是否展开
	private Object doc;// 文件的guid，文件夹的此值为null

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
