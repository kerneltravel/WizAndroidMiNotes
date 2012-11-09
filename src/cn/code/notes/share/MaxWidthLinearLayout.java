package cn.code.notes.share;

import redstone.xmlrpc.XmlRpcMessages;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MaxWidthLinearLayout extends LinearLayout {
	public static final int MAX_WIDTH = Integer.parseInt(XmlRpcMessages
			.getString("MaxWidthLinearLayout.MaxWidth"));

	public MaxWidthLinearLayout(Context context) {
		super(context);
	}

	public MaxWidthLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		// int adjustedMaxWidth = (int) Utils.getDensitySize(MAX_WIDTH);
		int adjustedMaxWidth = MAX_WIDTH;
		int adjustedWidth = Math.min(measuredWidth, adjustedMaxWidth);
		int adjustedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
				adjustedWidth, mode);
		super.onMeasure(adjustedWidthMeasureSpec, heightMeasureSpec);
	}
}