package com.weareholidays.bia.widgets;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * Created by Teja on 21/07/15.
 */
public class TabLayoutCustom extends TabLayout {

    private boolean overrideMaxWidth;

    public TabLayoutCustom(Context context) {
        super(context);
    }

    public TabLayoutCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(overrideMaxWidth){
            changeMaxWidth();
        }
    }

    private void changeMaxWidth(){
        try {
            Field field = TabLayout.class.getDeclaredField("mTabMaxWidth");
            field.setAccessible(true);
            field.set(this,this.getMeasuredWidth());
            field.setAccessible(false);
        } catch (Exception e) {

        }
    }

    public void setOverrideMaxWidth(boolean overrideMaxWidth) {
        this.overrideMaxWidth = overrideMaxWidth;
    }
}
