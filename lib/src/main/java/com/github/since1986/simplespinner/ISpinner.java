package com.github.since1986.simplespinner;

import android.support.annotation.LayoutRes;
import android.view.View;

public interface ISpinner {

    String KEY_STATE_IS_ACTIVATED = "IS_ACTIVATED";
    String KEY_STATE_VALUE = "VALUE";
    String KEY_STATE_HINT = "HINT";

    int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    int[] UNACTIVATED_STATE_SET = {-android.R.attr.state_activated};

    <T> void setHint(T hint);

    void showDropdownPopupWindow();

    void dismissDropdownPopupWindow();
}
