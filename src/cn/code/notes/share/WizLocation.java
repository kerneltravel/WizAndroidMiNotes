package cn.code.notes.share;

import java.text.Collator;

public class WizLocation implements Comparable<WizLocation> {
	public String name;
	public String rName;
	public int mCount;

	//
	public WizLocation() {
		mCount = 0;
	}

	public int compareTo(WizLocation another) {
		Collator cmp2 = java.text.Collator.getInstance();
		if (rName == null || another.rName == null) {
			return cmp2.compare(name, another.name);
		}
		//
		return cmp2.compare(rName, another.rName);
	}
}
