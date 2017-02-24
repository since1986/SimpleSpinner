package com.github.since1986.utils;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Created by since1986 on 2017/2/24.
 */

public final class ContextUtils {

    public static @ColorInt int getColorPrimary(Context context){

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.colorPrimary, value, true);
        return value.data;
    }

    public static int getScreenRealHeight(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRealSize(point);
        return point.y;
    }
}
