package cn.code.notes.share;

public class WizAttachment {

	public String guid;
	public String docGuid;
	public String name;
	public String description;
	public String dataMd5;
	public String dateModified;
	public int serverChanged;
	public int localChanged;
	public long version;
	public String location;

	public WizAttachment() {
		serverChanged = 0;
		localChanged = 0;
		version = 0;
	}

}
