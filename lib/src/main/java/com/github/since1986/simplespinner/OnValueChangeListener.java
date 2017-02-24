package com.github.since1986.simplespinner;

import android.view.View;

public interface OnValueChangeListener {
    void onValueChange(View view, CharSequence originalValue, CharSequence newValue);
}