package cn.code.notes.share;

import java.text.Collator;

public class WizDocument implements Comparable<WizDocument> {
	public long noteId;
	public String guid;
	public String title;
	public String location;
	public String dataMd5;
	public String url;
	public String tagGUIDs;
	public String dateCreated;
	public String dateModified;
	public String type;
	public String fileType;
	public int attachmentCount;
	public int serverChanged;
	public int localChanged;
	public long version;
	public boolean isEncryption;

	//
	public WizDocument() {
		attachmentCount = 0;
		serverChanged = 0;
		localChanged = 0;
		version = 0;
		isEncryption = false;
	}

	public int compareTo(WizDocument another) {
		Collator cmp2 = java.text.Collator.getInstance();
		if (title == null || another.title == null) {
			return cmp2.compare(dateModified, another.dateModified);
		}
		//
		return cmp2.compare(title, another.title);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WizDocument other = (WizDocument) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		return true;
	}

}
