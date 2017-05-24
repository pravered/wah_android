/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.weareholidays.bia.coachmarks;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.weareholidays.bia.R;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.utils.DebugUtils;

/**
 * A view which allows you to showcase areas of your app with an explanation.
 */
public class ShowcaseView extends RelativeLayout
        implements View.OnTouchListener, ShowcaseViewApi {

    private static final int HOLO_BLUE = Color.parseColor("#33B5E5");

    private final Button mEndButton;
    private final TextDrawer textDrawer;
    private final ShowcaseDrawer showcaseDrawer;
    private final ShowcaseAreaCalculator showcaseAreaCalculator;
    private final AnimationFactory animationFactory;
    private final ShotStateStore shotStateStore;

    // Showcase metrics
    private int showcaseX = -1;
    private int showcaseY = -1;
    private float scaleMultiplier = 1f;

    // Touch items
    private boolean hasCustomClickListener = false;
    private boolean blockTouches = true;
    private boolean hideOnTouch = false;
    private OnShowcaseEventListener mEventListener = OnShowcaseEventListener.NONE;

    private boolean hasAlteredText = false;
    private boolean hasNoTarget = false;
    private boolean shouldCentreText;
    private Bitmap bitmapBuffer;

    // Animation items
    private long fadeInMillis;
    private long fadeOutMillis;
    private boolean isShowing;
    private boolean alignBottom;
    private boolean isShowCaseFailed = false;

    protected ShowcaseView(Context context, boolean newStyle, boolean alignBottom) {
        this(context, null, R.styleable.CustomTheme_showcaseViewStyle, newStyle, alignBottom);
    }

    protected ShowcaseView(Context context, AttributeSet attrs, int defStyle, boolean newStyle, boolean alignBottom) {
        super(context, attrs, defStyle);

        ApiUtils apiUtils = new ApiUtils();
        animationFactory = new AnimatorAnimationFactory();
        showcaseAreaCalculator = new ShowcaseAreaCalculator();
        shotStateStore = new ShotStateStore(context);

        apiUtils.setFitsSystemWindowsCompat(this);
        getViewTreeObserver().addOnPreDrawListener(new CalculateTextOnPreDraw());
        getViewTreeObserver().addOnGlobalLayoutListener(new UpdateOnGlobalLayout());

        // Get the attributes for the ShowcaseView
        final TypedArray styled = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ShowcaseView, R.attr.showcaseViewStyle,
                        R.style.ShowcaseView);

        // Set the default animation times
        fadeInMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        fadeOutMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mEndButton = (Button) LayoutInflater.from(context).inflate(R.layout.showcase_button, null);
        mEndButton.setTextColor(getResources().getColor(R.color.white));
        this.alignBottom = alignBottom;
        if (newStyle) {
            showcaseDrawer = new NewShowcaseDrawer(getResources());
        } else {
            showcaseDrawer = new StandardShowcaseDrawer(getResources());
        }
        textDrawer = new TextDrawer(getResources(), showcaseAreaCalculator, getContext());

        updateStyle(styled, false);

        init();
    }

    private void init() {

        setOnTouchListener(this);

        if (mEndButton.getParent() == null && !isShowCaseFailed) {
            int margin = (int) getResources().getDimension(R.dimen.button_margin);
            RelativeLayout.LayoutParams lps = (LayoutParams) generateDefaultLayoutParams();
            if (alignBottom) {
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else {
                lps.addRule(RelativeLayout.CENTER_VERTICAL);
            }
            lps.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lps.setMargins(margin, margin, margin, margin);
            mEndButton.setLayoutParams(lps);
            mEndButton.setText(getResources().getString(R.string.coachmark_button_gotit));
            if (!hasCustomClickListener) {
                mEndButton.setOnClickListener(hideOnClickListener);
            }
            addView(mEndButton);
        }

    }

    private boolean hasShot() {
        return shotStateStore.hasShot();
    }

    void setShowcasePosition(Point[] point) {
        for (int j = 0; j < point.length; j++) {
            setShowcasePosition(point[j].x, point[j].y);
        }
    }

    void setShowcasePosition(int x, int y) {
        if (shotStateStore.hasShot()) {
            return;
        }
        showcaseX = x;
        showcaseY = y;
        //init();
        invalidate();
    }

    public void setTarget(Target[] markLocationByView, Point[] markLocationByOffset, Point[] markTextPoint,
                          String[] coachMarkTextArray, float[] circleRadius) {
        setShowcase(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius, false);
    }

    Point[] markPoints;
    Point[] markPointsByOffset;
    Point[] markTextPoints;
    String[] coachMarkText;
    float[] radius;

    public void setShowcase(final Target[] markLocationByView, final Point[] markLocationByOffset, final Point[] markTextPoint,
                            final String[] coachMarkTextArray, final float circleRadius[], final boolean animate) {
        postDelayed(new Runnable() {
            @Override
            public void run() {

                markPoints = new Point[markLocationByView.length];
                markPointsByOffset = new Point[markLocationByOffset.length];
                markTextPoints = new Point[markTextPoint.length];
                coachMarkText = new String[coachMarkTextArray.length];
                radius = new float[circleRadius.length];

                updateBitmap();

                for (int i = 0; i < markLocationByView.length; i++) {
                    Log.d("Test123", "" + coachMarkText[i]);

                    markPoints[i] = markLocationByView[i].getPoint();
                    markPointsByOffset[i] = markLocationByOffset[i];
                    markTextPoints[i] = markTextPoint[i];
                    coachMarkText[i] = coachMarkTextArray[i];
                    radius[i] = circleRadius[i];
                }

                if (markPoints != null) {
                    hasNoTarget = false;
                    if (animate) {
//                            animationFactory.animateTargetToPoint(ShowcaseView.this, targetPoint);
                    } else {
                        setShowcasePosition(markPoints);
                    }
                } else {
                    hasNoTarget = true;
                    invalidate();
                }

            }
        }, 100);
    }

    private void updateBitmap() {
        if (bitmapBuffer == null || haveBoundsChanged()) {
            if (bitmapBuffer != null)
                bitmapBuffer.recycle();
            if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateBitmap();
                        return;
                    }
                }, 200);
            }
            try {
                isShowCaseFailed = false;
                bitmapBuffer = Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            } catch (Exception e) {
                setVisibility(GONE);
                isShowCaseFailed = true;
                DebugUtils.logException(e);
            }
        }
    }

    private boolean haveBoundsChanged() {
        return getMeasuredWidth() != bitmapBuffer.getWidth() ||
                getMeasuredHeight() != bitmapBuffer.getHeight();
    }

    public boolean hasShowcaseView() {
        return (showcaseX != 1000000 && showcaseY != 1000000) && !hasNoTarget;
    }

    public void setShowcaseX(int x) {
        setShowcasePosition(x, showcaseY);
    }

    public void setShowcaseY(int y) {
        setShowcasePosition(showcaseX, y);
    }

    public int getShowcaseX() {
        return showcaseX;
    }

    public int getShowcaseY() {
        return showcaseY;
    }

    /**
     * Override the standard button click event
     *
     * @param listener Listener to listen to on click events
     */
    public void overrideButtonClick(OnClickListener listener) {
        if (shotStateStore.hasShot()) {
            return;
        }
        if (mEndButton != null) {
            if (listener != null) {
                mEndButton.setOnClickListener(listener);
            } else {
                mEndButton.setOnClickListener(hideOnClickListener);
            }
        }
        hasCustomClickListener = true;
    }

    public void setOnShowcaseEventListener(OnShowcaseEventListener listener) {
        if (listener != null) {
            mEventListener = listener;
        } else {
            mEventListener = OnShowcaseEventListener.NONE;
        }
    }

    public void setButtonText(CharSequence text) {
        if (mEndButton != null) {
            mEndButton.setText(getResources().getString(R.string.coachmark_button_gotit));
        }
    }

    private void recalculateText() {
        boolean recalculatedCling = showcaseAreaCalculator.calculateShowcaseRect(showcaseX, showcaseY, showcaseDrawer);
        boolean recalculateText = recalculatedCling || hasAlteredText;
        if (recalculateText) {
            textDrawer.calculateTextPosition(getMeasuredWidth(), getMeasuredHeight(), this, shouldCentreText);
        }
        hasAlteredText = false;
    }

    private void recalculateText(float x, float y) {
        boolean recalculatedCling = showcaseAreaCalculator.calculateShowcaseRect(x, y, showcaseDrawer);
        boolean recalculateText = recalculatedCling || hasAlteredText;
        if (recalculateText) {
            textDrawer.calculateTextPosition(getMeasuredWidth(), getMeasuredHeight(), this, shouldCentreText);
        }
        hasAlteredText = false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (showcaseX < 0 || showcaseY < 0 || shotStateStore.hasShot() || bitmapBuffer == null) {
            super.dispatchDraw(canvas);
            return;
        }

        //Draw background color
        showcaseDrawer.erase(bitmapBuffer);

        // Draw the showcase drawable
        if (!hasNoTarget) {
            for (int k = 0; k < markPoints.length; k++) {

                Log.d("Test123", "MarkPoint X = " + markPoints[k].x + "  " + " MarkPoint Y = " + markPoints[k].y);

                float x = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, markPointsByOffset[k].x, getResources().getDisplayMetrics());
                float y = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, markPointsByOffset[k].y, getResources().getDisplayMetrics());

                showcaseDrawer.drawShowcase(getContext(),bitmapBuffer, markPoints[k].x + x, markPoints[k].y + y, radius[k]);

            }
            showcaseDrawer.drawToCanvas(canvas, bitmapBuffer);
        }

        // Draw the Line and text on the screen, recalculating its position if necessary
        for (int k = 0; k < markPoints.length; k++) {

            Log.d("Test12345", " markPointsByOffset X = " + markPointsByOffset[k].x + "  " + " markPointsByOffset Y = " + markPointsByOffset[k].y);

////            Draw  Line
//            float showcaseRadius = getContext().getResources().getDimension(R.dimen.showcase_radius);
//            float showcaseAndLineMargin = getContext().getResources().getDimension(R.dimen.showcase_line_margin);
//            float showcaseLineLength = getContext().getResources().getDimension(R.dimen.showcase_line_length);
//
//            Paint paint = new Paint();
//            paint.setColor(Color.WHITE);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(4);
//            float x1 = markPoints[k].x + showcaseRadius + showcaseAndLineMargin;
//            float y1 = markPoints[k].y + showcaseRadius + showcaseAndLineMargin;
//            float x2 = x1 + showcaseLineLength;
//            float y2 = y1 + showcaseLineLength;
//
//            canvas.drawLine(x1, y1, x2, y2 ,paint);
//
//            Log.d("Test12345", "Text = " + coachMarkText[k]);

            // Draw Text
            setContentText(coachMarkText[k]);

//            textDrawer.setContentTitle(targetText[k]);
            float x = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, markPointsByOffset[k].x, getResources().getDisplayMetrics());
            float y = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, markPointsByOffset[k].y, getResources().getDisplayMetrics());

            textDrawer.draw(canvas, markPoints[k].x + x, markPoints[k].y + y, markTextPoints[k].x, markTextPoints[k].y, coachMarkText[k]);
        }

//        textDrawer.draw(canvas);


        super.dispatchDraw(canvas);

    }

    @Override
    public void hide() {
        clearBitmap();
        // If the type is set to one-shot, store that it has shot
        shotStateStore.storeShot();
        mEventListener.onShowcaseViewHide(this);
        fadeOutShowcase();
    }

    private void clearBitmap() {
        if (bitmapBuffer != null && !bitmapBuffer.isRecycled()) {
            bitmapBuffer.recycle();
            bitmapBuffer = null;
        }
    }

    private void fadeOutShowcase() {
        animationFactory.fadeOutView(this, fadeOutMillis, new AnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(View.GONE);
                isShowing = false;
                mEventListener.onShowcaseViewDidHide(ShowcaseView.this);
            }
        });
    }

    @Override
    public void show() {
        isShowing = true;
        mEventListener.onShowcaseViewShow(this);
        fadeInShowcase();
    }

    private void fadeInShowcase() {
        animationFactory.fadeInView(this, fadeInMillis,
                new AnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        float xDelta = Math.abs(motionEvent.getRawX() - showcaseX);
        float yDelta = Math.abs(motionEvent.getRawY() - showcaseY);
        double distanceFromFocus = Math.sqrt(Math.pow(xDelta, 2) + Math.pow(yDelta, 2));

        if (MotionEvent.ACTION_UP == motionEvent.getAction() &&
                hideOnTouch && distanceFromFocus > showcaseDrawer.getBlockedRadius()) {
            this.hide();
        }

        return blockTouches && distanceFromFocus > showcaseDrawer.getBlockedRadius();
    }

    private static void insertShowcaseView(ShowcaseView showcaseView, Activity activity) {
        View decorView = activity.getWindow().getDecorView().getRootView();
//        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//        decorView.setSystemUiVisibility(uiOptions);

//        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);

        ((ViewGroup) decorView).addView(showcaseView);

        if (!showcaseView.hasShot()) {
            showcaseView.show();
        } else {
            showcaseView.hideImmediate();
        }
    }

    private void hideImmediate() {
        isShowing = false;
        setVisibility(GONE);
    }

    @Override
    public void setContentTitle(CharSequence title) {
        textDrawer.setContentTitle(title);
    }

    @Override
    public void setContentText(CharSequence text) {
        textDrawer.setContentText(text);
    }

    private void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public void hideButton() {
        mEndButton.setVisibility(GONE);
    }

    public void showButton() {
        mEndButton.setVisibility(VISIBLE);
    }

    /**
     * Builder class which allows easier creation of {@link ShowcaseView}s.
     * It is recommended that you use this Builder class.
     */
    public static class Builder {

        final ShowcaseView showcaseView;
        private final Activity activity;

        public Builder(Activity activity) {
            this(activity, false, false);
        }

        public Builder(Activity activity, boolean useNewStyle, boolean alignBottom) {
            this.activity = activity;
            this.showcaseView = new ShowcaseView(activity, useNewStyle, alignBottom);
//            this.showcaseView.setTarget(Target.NONE);
        }

        public Builder(Activity activity, boolean useNewStyle, int[] circleNumber, int[] circleRadius, boolean alignBottom) {
            this.activity = activity;
            this.showcaseView = new ShowcaseView(activity, useNewStyle, alignBottom);
//            this.showcaseView.setTarget(Target.NONE);
        }


        /**
         * Create the {@link ShowcaseView} and show it.
         *
         * @return the created ShowcaseView
         */
        public ShowcaseView build() {
            insertShowcaseView(showcaseView, activity);
            return showcaseView;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setContentTitle(int resId) {
            return setContentTitle(activity.getString(resId));
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setContentTitle(CharSequence title) {
            showcaseView.setContentTitle(title);
            return this;
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setContentText(int resId) {
            return setContentText(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setContentText(CharSequence text) {
            showcaseView.setContentText(text);
            return this;
        }

        public Builder setTarget(Target[] markLocationByView, Point[] markLocationByOffset, Point[] markTextPoint,
                                 String[] coachMarkTextArray, float[] circleRadius) {
            showcaseView.setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray,circleRadius);
            return this;
        }

        /**
         * Set the style of the ShowcaseView. See the sample app for example styles.
         */
        public Builder setStyle(int theme) {
            showcaseView.setStyle(theme);
            return this;
        }

        /**
         * Set a listener which will override the button clicks.
         * <p/>
         * Note that you will have to manually hide the ShowcaseView
         */
        public Builder setOnClickListener(OnClickListener onClickListener) {
            showcaseView.overrideButtonClick(onClickListener);
            return this;
        }

        /**
         * Don't make the ShowcaseView block touches on itself. This doesn't
         * block touches in the showcased area.
         * <p/>
         * By default, the ShowcaseView does block touches
         */
        public Builder doNotBlockTouches() {
            showcaseView.setBlocksTouches(false);
            return this;
        }

        /**
         * Make this ShowcaseView hide when the user touches outside the showcased area.
         * This enables {@link #doNotBlockTouches()} as well.
         * <p/>
         * By default, the ShowcaseView doesn't hide on touch.
         */
        public Builder hideOnTouchOutside() {
            showcaseView.setBlocksTouches(true);
            showcaseView.setHideOnTouchOutside(true);
            return this;
        }

        /**
         * Set the ShowcaseView to only ever show once.
         *
         * @param shotId a unique identifier (<em>across the app</em>) to store
         *               whether this ShowcaseView has been shown.
         */
        public Builder singleShot(long shotId) {
            showcaseView.setSingleShot(shotId);
            return this;
        }

        public Builder setShowcaseEventListener(OnShowcaseEventListener showcaseEventListener) {
            showcaseView.setOnShowcaseEventListener(showcaseEventListener);
            return this;
        }
    }

    /**
     * Set whether the text should be centred in the screen, or left-aligned (which is the default).
     */
    public void setShouldCentreText(boolean shouldCentreText) {
        this.shouldCentreText = shouldCentreText;
        hasAlteredText = true;
        invalidate();
    }

    /**
     * @see ShowcaseView.Builder#setSingleShot(long)
     */
    private void setSingleShot(long shotId) {
        shotStateStore.setSingleShot(shotId);
    }

    /**
     * Change the position of the ShowcaseView's button from the default bottom-right position.
     *
     * @param layoutParams a {@link android.widget.RelativeLayout.LayoutParams} representing
     *                     the new position of the button
     */
    @Override
    public void setButtonPosition(RelativeLayout.LayoutParams layoutParams) {
        mEndButton.setLayoutParams(layoutParams);
    }

    /**
     * Set the duration of the fading in and fading out of the ShowcaseView
     */
    private void setFadeDurations(long fadeInMillis, long fadeOutMillis) {
        this.fadeInMillis = fadeInMillis;
        this.fadeOutMillis = fadeOutMillis;
    }

    /**
     * @see ShowcaseView.Builder#hideOnTouchOutside()
     */
    @Override
    public void setHideOnTouchOutside(boolean hideOnTouch) {
        this.hideOnTouch = hideOnTouch;
    }

    /**
     * @see ShowcaseView.Builder#doNotBlockTouches()
     */
    @Override
    public void setBlocksTouches(boolean blockTouches) {
        this.blockTouches = blockTouches;
    }

    /**
     * @see ShowcaseView.Builder#setStyle(int)
     */
    @Override
    public void setStyle(int theme) {
        TypedArray array = getContext().obtainStyledAttributes(theme, R.styleable.ShowcaseView);
        updateStyle(array, true);
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    private void updateStyle(TypedArray styled, boolean invalidate) {
        int backgroundColor = styled.getColor(R.styleable.ShowcaseView_sv_backgroundColor, Color.argb(128, 80, 80, 80));
        int showcaseColor = styled.getColor(R.styleable.ShowcaseView_sv_showcaseColor, HOLO_BLUE);
        String buttonText = styled.getString(R.styleable.ShowcaseView_sv_buttonText);
        if (TextUtils.isEmpty(buttonText)) {
            buttonText = getResources().getString(android.R.string.ok);
        }
        boolean tintButton = styled.getBoolean(R.styleable.ShowcaseView_sv_tintButtonColor, true);

        int titleTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_titleTextAppearance,
                R.style.TextAppearance_ShowcaseView_Title);
        int detailTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_detailTextAppearance,
                R.style.TextAppearance_ShowcaseView_Detail);

        styled.recycle();

        showcaseDrawer.setShowcaseColour(showcaseColor);
        showcaseDrawer.setBackgroundColour(backgroundColor);
        tintButton(showcaseColor, tintButton);
        mEndButton.setText(buttonText);
        textDrawer.setTitleStyling(titleTextAppearance);
        textDrawer.setDetailStyling(detailTextAppearance);
        hasAlteredText = true;

        if (invalidate) {
            invalidate();
        }
    }

    private void tintButton(int showcaseColor, boolean tintButton) {
//        if (tintButton) {
//            mEndButton.getBackground().setColorFilter(showcaseColor, PorterDuff.Mode.MULTIPLY);
//        } else {
//            mEndButton.getBackground().setColorFilter(HOLO_BLUE, PorterDuff.Mode.MULTIPLY);
//        }
    }

    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            if (!shotStateStore.hasShot()) {

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateBitmap();

                    }
                }, 200);
            }
        }

    }

    private class CalculateTextOnPreDraw implements ViewTreeObserver.OnPreDrawListener {

        @Override
        public boolean onPreDraw() {
            recalculateText();
            return true;
        }
    }

    private OnClickListener hideOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hide();
        }
    };

}
