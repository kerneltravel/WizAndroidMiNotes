package cn.code.notes.share;

import java.text.Collator;

public class WizTag implements Comparable<WizTag> {
	public String name;
	public String guid;
	public String parentGuid;
	public String description;
	public String namePath;
	public String dateModified;
	public long version;

	//
	public WizTag() {
		version = 0;
	}

	public int compareTo(WizTag another) {
		Collator cmp2 = java.text.Collator.getInstance();//try default locale
		if (namePath == null || another.namePath == null)
		{
			return cmp2.compare(name, another.name);
		}
		//
		return cmp2.compare(namePath, another.namePath);
	}
}
