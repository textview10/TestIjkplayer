package com.test.ijkplayer_view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by xu.wang
 * Date on  2018/8/15 11:13:21.
 *
 * @Desc
 */

public class NotDispatchRelativelayout extends RelativeLayout {
    public NotDispatchRelativelayout(Context context) {
        super(context);
    }

    public NotDispatchRelativelayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotDispatchRelativelayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
