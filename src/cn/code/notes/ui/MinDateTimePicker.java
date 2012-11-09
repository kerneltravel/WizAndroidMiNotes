package cn.code.notes.ui;

import java.util.Calendar;
import cn.code.notes.R;

import android.content.Context;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class MinDateTimePicker extends FrameLayout {
	private DatePicker mDadePicker;
	private TimePicker mTimePicker;
	private Calendar mDate;
	private int year;
	private int monthOfYear;
	private int dayOfMonth;
	private OnMinDateTimeChangedListener mMinDateTimeChangedListener;

	public MinDateTimePicker(Context context) {
		this(context, System.currentTimeMillis());
	}

	public MinDateTimePicker(Context context, long date) {
		super(context);
		mDate = Calendar.getInstance();
		inflate(context, R.layout.newdatetime_picker, this);
		year = mDate.get(Calendar.YEAR);
		monthOfYear = mDate.get(Calendar.MONTH);
		dayOfMonth = mDate.get(Calendar.DAY_OF_MONTH);
		mDate.get(Calendar.HOUR_OF_DAY);
		mDate.get(Calendar.MINUTE);

		mDadePicker = (DatePicker) findViewById(R.id.dp_date);
		mTimePicker = (TimePicker) findViewById(R.id.tp_time);

		mTimePicker.setIs24HourView(true);

		mDadePicker.init(year, monthOfYear, dayOfMonth,
				new OnDateChangedListener() {
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						onDateTimeChanged();
					}
				});

		mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hour, int minute) {
				/*
				 * try { Field mHourSpinner = view.getClass().getDeclaredField(
				 * "mHourSpinner"); mHourSpinner.setAccessible(true); Object
				 * value = mHourSpinner.get(view);
				 * 
				 * Field mCurrentScrollOffset = value.getClass()
				 * .getDeclaredField("mCurrentScrollOffset");
				 * mCurrentScrollOffset.setAccessible(true);
				 * 
				 * Field mCurrentHour = view.getClass().getDeclaredField(
				 * "mCurrentHour"); mCurrentHour.setAccessible(true);
				 * 
				 * timeChanged(mCurrentScrollOffset, mCurrentHour, value, view,
				 * hour, minute); } catch (Exception e) { e.printStackTrace(); }
				 */
				onDateTimeChanged();
			}

		});
	}

	public void setOnDateTimeChangedListener(
			OnMinDateTimeChangedListener callback) {
		mMinDateTimeChangedListener = callback;
	}

	public interface OnMinDateTimeChangedListener {
		void onDateTimeChanged(MinDateTimePicker view, int year, int month,
				int dayOfMonth, int hourOfDay, int minute);
	}

	private void onDateTimeChanged() {
		if (mMinDateTimeChangedListener != null) {
			mMinDateTimeChangedListener.onDateTimeChanged(this,
					getCurrentYear(), getCurrentMonth(), getCurrentDay(),
					getCurrentHourOfDay(), getCurrentMinute());
		}
	}

	private int getCurrentYear() {
		return mDadePicker.getYear();
	}

	private int getCurrentMinute() {
		return mTimePicker.getCurrentMinute();
	}

	private int getCurrentHourOfDay() {
		return mTimePicker.getCurrentHour();
	}

	private int getCurrentDay() {
		return mDadePicker.getDayOfMonth();
	}

	private int getCurrentMonth() {
		return mDadePicker.getMonth();
	}

	public void setCurrentDate(long date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
	}
}
