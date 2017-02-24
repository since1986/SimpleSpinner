package com.github.since1986.simplespinner;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.since1986.utils.ContextUtils;

import java.util.List;

public class SimpleSpinner extends LinearLayout implements ISpinner {

    private static final String KEY_STATE_CHECKED_ITEM_POSITION = "CHECKED_ITEM_POSITION";

    private static final int STATE_CHECKED_NOTHING = -1;

    private OnDropdownShowListener onDropdownShowListener;
    private OnDropdownDismissListener onDropdownDismissListener;
    private OnDropdownItemClickListener onDropdownItemClickListener;
    private OnValueChangeListener onValueChangeListener;

    private TextView spinnerValueTextView;
    private ImageView spinnerTriggerImageView;
    private PopupWindow dropdownPopupWindow;
    private Bundle statesBundle;

    public SimpleSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    //用户主动设置选中状态
    public void select(int position) {

        ListView dropdownContentView = ((ListView) dropdownPopupWindow.getContentView());
        SpinnerAdapter adapter = (SpinnerAdapter) dropdownContentView.getAdapter();
        if (position > STATE_CHECKED_NOTHING && position < adapter.getCount()) {
            dropdownContentView.setItemChecked(position, true);
            statesBundle.putInt(KEY_STATE_CHECKED_ITEM_POSITION, position);
            changeSpinnerValue(adapter.getItem(position).toString());
        }
    }

    //用户主动清除选择
    public void clearSelection() {

        ((ListView) dropdownPopupWindow.getContentView()).clearChoices();
        statesBundle.putInt(KEY_STATE_CHECKED_ITEM_POSITION, STATE_CHECKED_NOTHING);
        spinnerValueTextView.setText(statesBundle.getString(KEY_STATE_HINT));
    }


    //绑定数据
    public <T> void bindData(List<T> data) {
        ((SpinnerAdapter) ((ListView) dropdownPopupWindow.getContentView()).getAdapter()).addAll(data);
    }

    public <T> void bindData(T... data) {
        ((SpinnerAdapter) ((ListView) dropdownPopupWindow.getContentView()).getAdapter()).addAll(data);
    }

    private void init(Context context, AttributeSet attrs) {

        //读取配置
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleSpinner);
        int dropdownHeight = typedArray.getLayoutDimension(R.styleable.SimpleSpinner_dropdownHeight, ContextUtils.getScreenRealHeight(context) / 2);
        int dropdownWidth = typedArray.getLayoutDimension(R.styleable.SimpleSpinner_dropdownWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        String hint = typedArray.getString(R.styleable.SimpleSpinner_hint);
        typedArray.recycle();

        //建立默认状态
        if (statesBundle == null) {
            statesBundle = new Bundle();
            statesBundle.putBoolean(KEY_STATE_IS_ACTIVATED, false);
            statesBundle.putInt(KEY_STATE_CHECKED_ITEM_POSITION, STATE_CHECKED_NOTHING);
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
        final ListView popupContentView = (ListView) LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_dropdown, null);
        SpinnerAdapter adapter = new SpinnerAdapter(context, android.R.layout.simple_list_item_single_choice);

        popupContentView.setAdapter(adapter); //设置popup中ListView的Adapter
        popupContentView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //列表项被选中的处理
        popupContentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, final long id) {

                //选中处理
                statesBundle.putInt(KEY_STATE_CHECKED_ITEM_POSITION, position);
                if (onDropdownItemClickListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onDropdownItemClickListener.onDropdownItemClick(view, position, id);
                        }
                    });
                }

                //设置值，值改变处理
                final CharSequence newValue = ((TextView) view).getText();
                changeSpinnerValue(newValue);

                //关闭popup
                dropdownPopupWindow.dismiss();
            }
        });
        dropdownPopupWindow.setContentView(popupContentView);
    }

    //打开popup
    public void showDropdownPopupWindow() {

        post(new Runnable() {
            @Override
            public void run() {
                //打开popup
                dropdownPopupWindow.showAsDropDown(SimpleSpinner.this);

                //设置选中
                int checkedItemPosition = statesBundle.getInt(KEY_STATE_CHECKED_ITEM_POSITION);
                if (checkedItemPosition != STATE_CHECKED_NOTHING) {
                    ((ListView) dropdownPopupWindow.getContentView()).setItemChecked(checkedItemPosition, true);
                }
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

    //关闭popup
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

    public interface OnDropdownItemClickListener {
        void onDropdownItemClick(View view, int position, long id);

        void onDropdownItemClick(int position);
    }

    public void setOnDropdownShowListener(OnDropdownShowListener onDropdownShowListener) {
        this.onDropdownShowListener = onDropdownShowListener;
    }

    public void setOnDropdownDismissListener(OnDropdownDismissListener onDropdownDismissListener) {
        this.onDropdownDismissListener = onDropdownDismissListener;
    }

    public void setOnDropdownItemClickListener(OnDropdownItemClickListener onDropdownItemClickListener) {
        this.onDropdownItemClickListener = onDropdownItemClickListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }


    class SpinnerAdapter extends ArrayAdapter {

        private final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
        private final int[] UNCHECKED_STATE_SET = {-android.R.attr.state_checked};

        SpinnerAdapter(Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
            view.setCheckMarkDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.spinner_item_check_mark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setCheckMarkTintList(new ColorStateList(new int[][]{
                        UNCHECKED_STATE_SET,
                        CHECKED_STATE_SET,
                        EMPTY_STATE_SET
                }, new int[]{
                        ContextCompat.getColor(getContext(), android.R.color.transparent),
                        ContextUtils.getColorPrimary(getContext()),
                        ContextCompat.getColor(getContext(), android.R.color.transparent),
                }));
            }
            view.setTextColor(new ColorStateList(new int[][]{
                    UNCHECKED_STATE_SET,
                    CHECKED_STATE_SET,
                    EMPTY_STATE_SET
            }, new int[]{
                    ContextCompat.getColor(getContext(), android.R.color.black),
                    ContextUtils.getColorPrimary(getContext()),
                    ContextCompat.getColor(getContext(), android.R.color.black),
            }));
            return view;
        }
    }
}
