package cn.code.notes.share;

import android.graphics.Bitmap;

public class WizAbstract {
	private String mDocumentGuid;
	private String mAbstractType;
	private String mAbstractText;
	private Bitmap mAbstractImage;

	public String getmDocumentGuid() {
		return mDocumentGuid;
	}

	public void setmDocumentGuid(String mDocumentGuid) {
		this.mDocumentGuid = mDocumentGuid;
	}

	public String getmAbstractType() {
		return mAbstractType;
	}

	public void setmAbstractType(String mAbstractType) {
		this.mAbstractType = mAbstractType;
	}

	public String getmAbstractText() {
		return mAbstractText;
	}

	public void setmAbstractText(String mAbstractText) {
		this.mAbstractText = mAbstractText;
	}

	public Bitmap getmAbstractImage() {
		return mAbstractImage;
	}

	public void setmAbstractImage(Bitmap mAbstractImage) {
		this.mAbstractImage = mAbstractImage;
	}
}
