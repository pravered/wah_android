package com.weareholidays.bia.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;

/**
 * Created by Teja on 26/05/15.
 */
public class FloatLabelLayout extends FrameLayout {

    private static final long ANIMATION_DURATION = 150;
    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 12f;

    private static final String SAVED_SUPER_STATE = "SAVED_SUPER_STATE";
    private static final String SAVED_LABEL_VISIBILITY = "SAVED_LABEL_VISIBILITY";
    private static final String SAVED_HINT = "SAVED_HINT";
    private static final String SAVED_LABEL_TEXT = "SAVED_LABEL_TEXT";

    private EditText mEditText;
    private TextView mLabel;
    private Trigger mTrigger;
    private CharSequence mHint;
    private CharSequence mLabelText;

    private int focusedLabelColor;
    private int normalLabelColor;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){

        final int sidePadding;
        final int labelAppearance;
        int trigger;


        if(attrs == null){
            sidePadding = getDefaultSidePadding();
            labelAppearance = getDefaultLabelAppearance();
            focusedLabelColor = getDefaultLabelFocusedAppearance();
            trigger = getDefaultTrigger();
        }
        else{
            final TypedArray a = context
                    .obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);
            sidePadding = a.getDimensionPixelSize(
                    R.styleable.FloatLabelLayout_floatLabelSidePadding,
                    getDefaultSidePadding());
            labelAppearance = a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                    getDefaultLabelAppearance());
            focusedLabelColor = a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextFocusedColor,
                    getDefaultLabelFocusedAppearance());
            trigger = a.getInt(R.styleable.FloatLabelLayout_floatLabelTrigger, getDefaultTrigger());
            mLabelText = a.getString(R.styleable.FloatLabelLayout_floatLabelText);
            a.recycle();
        }

        focusedLabelColor = getResources().getColor(focusedLabelColor);

        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(View.INVISIBLE);

        mLabel.setTextAppearance(context,labelAppearance);

        normalLabelColor = mLabel.getCurrentTextColor();

        mTrigger = Trigger.fromValue(trigger);

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private int getDefaultSidePadding(){
        return dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP);
    }

    private int getDefaultTrigger(){
        return Trigger.TYPE.getValue();
    }


    private int getDefaultLabelAppearance(){
        return R.style.TextAppearance_App_FloatLabel;
    }

    private int getDefaultLabelFocusedAppearance(){
        return R.color.focused_float_label_color;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(SAVED_LABEL_VISIBILITY, mLabel.getVisibility());
        bundle.putCharSequence(SAVED_HINT, mHint);
        if(mLabelText != null){
            bundle.putCharSequence(SAVED_LABEL_TEXT,mLabelText);
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //noinspection ResourceType
            mLabel.setVisibility(bundle.getInt(SAVED_LABEL_VISIBILITY));
            mHint = bundle.getCharSequence(SAVED_HINT);
            mLabelText = bundle.getCharSequence(SAVED_LABEL_TEXT);
            // retrieve super state
            state = bundle.getParcelable(SAVED_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // If we already have an EditText, throw an exception
            if (mEditText != null) {
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            }

            // Update the layout params so that the EditText is at the bottom, with enough top
            // margin to show the label
            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = (int) mLabel.getTextSize();
            params = lp;

            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    protected void setEditText(EditText editText) {
        mEditText = editText;

        if(mLabelText == null)
            mLabel.setText(mEditText.getHint());
        else
            mLabel.setText(mLabelText);

        if (mHint == null) {
            mHint = mEditText.getHint();
        }

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(mTextWatcher);

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        // if view already had focus we need to manually call the listener
        if (mTrigger == Trigger.FOCUS && mEditText.isFocused()) {
            mOnFocusChangeListener.onFocusChange(mEditText, true);
        }
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */
    protected void showLabel() {
        if (mLabel.getVisibility() != View.VISIBLE) {
            mLabel.setVisibility(View.VISIBLE);
            mLabel.setAlpha(0f);
            mLabel.setTranslationY(mLabel.getHeight());
            mLabel.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(null).start();
        }
    }

    /**
     * Hide the label using an animation
     */
    protected void hideLabel() {
        mLabel.setAlpha(1f);
        mLabel.setTranslationY(0f);
        mLabel.animate()
                .alpha(0f)
                .translationY(mLabel.getHeight())
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLabel.setVisibility(View.GONE);
                    }
                }).start();
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean focused) {

            if(focused){
                mLabel.setTextColor(focusedLabelColor);
            }
            else{
                mLabel.setTextColor(normalLabelColor);
            }
            mLabel.setActivated(focused);

            if (mTrigger == Trigger.FOCUS) {
                if (focused) {
                    mEditText.setHint("");
                    showLabel();
                } else {
                    if (TextUtils.isEmpty(mEditText.getText())) {
                        mEditText.setHint(mHint);
                        hideLabel();
                    }
                }
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            // only takes affect if mTrigger is set to TYPE
            if (mTrigger != Trigger.TYPE) {
                return;
            }

            if (TextUtils.isEmpty(s)) {
                hideLabel();
            } else {
                showLabel();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    };

    public static enum Trigger {
        TYPE(0),
        FOCUS(1);

        private final int mValue;

        private Trigger(int i) {
            mValue = i;
        }

        public int getValue() {
            return mValue;
        }

        public static Trigger fromValue(int value) {
            Trigger[] triggers = Trigger.values();
            for (int i = 0; i < triggers.length; i++) {
                if (triggers[i].getValue() == value) {
                    return triggers[i];
                }
            }

            throw new IllegalArgumentException(value + " is not a planValid value for " + Trigger.class.getSimpleName());
        }
    }
}
