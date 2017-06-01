package de.slg.klausurplan;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import de.slg.leoapp.R;
import de.slg.startseite.MainActivity;


public class NumberPickerPref extends DialogPreference {

    public static final int MAX_VALUE = 12;
    public static final int MIN_VALUE = 1;
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker picker;
    private int value;

    public NumberPickerPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPickerPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setMinValue(MIN_VALUE);
        picker.setMaxValue(MAX_VALUE);
        picker.setDisplayedValues( new String[] { "1 "+ MainActivity.ref.getString(R.string.month), "2 "+ MainActivity.ref.getString(R.string.months), "3 "+MainActivity.ref.getString(R.string.months), "4 "+MainActivity.ref.getString(R.string.months), "5 "+MainActivity.ref.getString(R.string.months), "6 "+MainActivity.ref.getString(R.string.months), "7 "+MainActivity.ref.getString(R.string.months), "8 "+MainActivity.ref.getString(R.string.months), "9 "+MainActivity.ref.getString(R.string.months), "10 "+MainActivity.ref.getString(R.string.months), "11 "+MainActivity.ref.getString(R.string.months), "1 "+MainActivity.ref.getString(R.string.year)} );
        picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, MIN_VALUE);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }
}