package com.github.since1986.simplespinner;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.since1986.utils.ContextUtils;

public class BaseSpinner extends LinearLayout implements ISpinner {

    protected OnDropdownShowListener onDropdownShowListener;
    protected OnDropdownDismissListener onDropdownDismissListener;
    protected OnValueChangeListener onValueChangeListener;

    private TextView spinnerValueTextView;
    private ImageView spinnerTriggerImageView;
    private PopupWindow dropdownPopupWindow;
    private Bundle statesBundle;

    private Context context;

    public BaseSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs);
    }

    static class SavedState extends BaseSavedState {

        Bundle _bundle;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            _bundle = in.readBundle(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBundle(_bundle);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);

        savedState._bundle = statesBundle;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.statesBundle = savedState._bundle;

        boolean isActivated = statesBundle.getBoolean(KEY_STATE_IS_ACTIVATED);
        if (isActivated) { //恢复激活状态
            showDropdownPopupWindow();
        }
        spinnerValueTextView.setText(statesBundle.getString(KEY_STATE_VALUE)); //恢复值
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;

            case MotionEvent.ACTION_UP:
                if (statesBundle.getBoolean(KEY_STATE_IS_ACTIVATED)) {
                    dismissDropdownPopupWindow();
                } else {
                    showDropdownPopupWindow();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setActivated(boolean activated) {

        super.setActivated(activated);
        spinnerValueTextView.setActivated(activated);
        spinnerTriggerImageView.setActivated(activated);

        statesBundle.putBoolean(KEY_STATE_IS_ACTIVATED, activated);
    }


    @Override
    public <T> void setHint(T hint) {

        CharSequence hintString;
        if (hint instanceof CharSequence) {
            hintString = (CharSequence) hint;
        } else {
            hintString = hint.toString();
        }
        spinnerValueTextView.setText(hintString);
        statesBundle.putString(KEY_STATE_HINT, (String) hintString);
    }

    public void setDropdownPopupWindowContentView(View dropdownPopupWindowContentView) {
        dropdownPopupWindow.setContentView(dropdownPopupWindowContentView);
    }

    public void setDropdownPopupWindowContentView(@LayoutRes int dropdownPopupWindowContentViewRes) {
        setDropdownPopupWindowContentView(LayoutInflater.from(context).inflate(dropdownPopupWindowContentViewRes, null));
    }

    private void init(Context context, AttributeSet attrs) {

        //读取配置
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseSpinner);
        int dropdownHeight = typedArray.getLayoutDimension(R.styleable.BaseSpinner_dropdownHeight, ContextUtils.getScreenRealHeight(context) / 2);
        int dropdownWidth = typedArray.getLayoutDimension(R.styleable.BaseSpinner_dropdownWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        String hint = typedArray.getString(R.styleable.BaseSpinner_hint);
        typedArray.recycle();

        //建立默认状态
        if (statesBundle == null) {
            statesBundle = new Bundle();
            statesBundle.putBoolean(KEY_STATE_IS_ACTIVATED, false);
            statesBundle.putString(KEY_STATE_HINT, hint);
        }

        //建立View
        LinearLayout rootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.simple_spinner, this, true);
        spinnerValueTextView = (TextView) rootView.findViewById(R.id.simple_spinner_value);
        spinnerTriggerImageView = (ImageView) rootView.findViewById(R.id.simple_spinner_trigger);

        if (hint != null) {
            spinnerValueTextView.setText(hint);
        }

        spinnerTriggerImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.spinner_trigger));

        ColorStateList colorStateList = new ColorStateList(new int[][]{
                UNACTIVATED_STATE_SET,
                ACTIVATED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[]{
                ContextCompat.getColor(getContext(), android.R.color.black),
                ContextUtils.getColorPrimary(getContext()),
                ContextCompat.getColor(getContext(), android.R.color.black),
        });

        spinnerValueTextView.setTextColor(colorStateList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spinnerTriggerImageView.setImageTintList(colorStateList);
        }

        //建立Popup
        dropdownPopupWindow = new PopupWindow();
        dropdownPopupWindow.setHeight(dropdownHeight);
        dropdownPopupWindow.setWidth(dropdownWidth);
        dropdownPopupWindow.setBackgroundDrawable(getBackground());
        dropdownPopupWindow.setOutsideTouchable(true);
        dropdownPopupWindow.setFocusable(true);
        dropdownPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setActivated(false); //关闭popup时设置激活状态
                if (onDropdownDismissListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onDropdownDismissListener.onDropdownDismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void showDropdownPopupWindow() {

        post(new Runnable() {
            @Override
            public void run() {
                //打开popup
                dropdownPopupWindow.showAsDropDown(BaseSpinner.this);
            }
        });

        //设置为Activated
        setActivated(true);

        if (onDropdownShowListener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    onDropdownShowListener.onDropdownShow();
                }
            });
        }
    }

    @Override
    public void dismissDropdownPopupWindow() {
        if (dropdownPopupWindow != null) {
            dropdownPopupWindow.dismiss();
        }
    }

    //修改值
    private void changeSpinnerValue(final CharSequence spinnerValue) {

        final CharSequence originalValue = spinnerValueTextView.getText();
        spinnerValueTextView.setText(spinnerValue);
        statesBundle.putString(KEY_STATE_VALUE, spinnerValue.toString());
        if (onValueChangeListener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    onValueChangeListener.onValueChange(spinnerValueTextView, originalValue, spinnerValue);
                }
            });
        }
    }

    public void setOnDropdownShowListener(OnDropdownShowListener onDropdownShowListener) {
        this.onDropdownShowListener = onDropdownShowListener;
    }

    public void setOnDropdownDismissListener(OnDropdownDismissListener onDropdownDismissListener) {
        this.onDropdownDismissListener = onDropdownDismissListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }
}
