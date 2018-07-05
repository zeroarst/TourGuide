package tourguide.tourguide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

/**
 * Created by tanjunrong on 2/10/15.
 */
public class TourGuide {

    @Nullable
    public ToolTip mToolTip;

    @Nullable
    public Pointer mPointer;

    @Nullable
    public Overlay mOverlay;

    @Nullable
    protected Technique mTechnique;

    @Nullable
    protected View mHighlightedView;

    @Nullable
    protected MotionType mMotionType;

    @Nullable
    protected Target mTarget;

    public enum Target {
        VIEW, WINDOW
    }

    @Nullable
    private ViewGroup mPopupWindowOverlayLayout;

    protected FrameLayoutWithHole mFrameLayout;

    private Activity mActivity;


    @Nullable
    private View mToolTipViewGroup;

    @Nullable
    private TextView mToolTipTitleTextView;
    @Nullable
    private TextView mToolTipDescTextView;
    private DialogFragment mDialogFragment;

    private boolean mIsPopupWindow = false;

    public TourGuide isPopupWindow(boolean yesNo) {
        this.mIsPopupWindow = yesNo;
        return this;
    }

    /* Constructor */
    public TourGuide(Activity activity) {
        mActivity = activity;
    }

    public TourGuide(DialogFragment dialogFragment) {
        mDialogFragment = dialogFragment;
        mActivity = dialogFragment.getActivity();
    }

    /*************
     *
     * Public API
     *
     *************/

    /* Static builder */
    public static TourGuide init(Activity activity) {
        return new TourGuide(activity);
    }

    public static TourGuide init(DialogFragment dialogFragment) {
        return new TourGuide(dialogFragment);
    }

    /**
     * Setter for the animation to be used
     *
     * @param technique Animation to be used
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide with(Technique technique) {
        mTechnique = technique;
        return this;
    }

    /**
     * Sets which motion type is motionType
     *
     * @param motionType
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide motionType(MotionType motionType) {
        mMotionType = motionType;
        return this;
    }

    /**
     * Sets the TourGuide to be played on whole screen instead of a target view.
     * @return
     */
    public TourGuide play() {
        return playOn(null);
    }

    /**
     * Sets the targeted view for TourGuide to play on
     *
     * @param targetView the view in which the tutorial button will be placed on top of
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide playOn(@Nullable View targetView) {
        mTarget = targetView == null ? Target.WINDOW : Target.VIEW;
        mHighlightedView = targetView;
        setupView();
        return this;
    }

    /**
     * Set the Pointer
     *
     * @param pointer this pointer object should contain the attributes of the Pointer, such as the pointer color, pointer gravity, etc, refer to
     * @return return TourGuide instance for chaining purpose
     * @Link{pointer}
     */
    public TourGuide setPointer(Pointer pointer) {
        mPointer = pointer;
        return this;
    }

    /**
     * Clean up the tutorial that is added to the activity
     */
    public void cleanUp() {
        if (mFrameLayout != null) {
            mFrameLayout.cleanUp();
            mFrameLayout = null;
        }

        if (mPopupWindowOverlayLayout != null) {
            getWindow().getWindowManager().removeView(mPopupWindowOverlayLayout);
            // TODO not sure if required?
            mPopupWindowOverlayLayout = null;
        } else if (mToolTipViewGroup != null) {
            ((ViewGroup) getDecoView()).removeView(mToolTipViewGroup);
            // TODO not sure if required?
            mToolTipViewGroup = null;
        }
    }

    /**
     * @return FrameLayoutWithHole that is used as overlay
     */
    public FrameLayoutWithHole getOverlay() {
        return mFrameLayout;
    }

    /**
     * Sets the overlay
     *
     * @param overlay this overlay object should contain the attributes of the overlay, such as background color, animation, Style, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setOverlay(Overlay overlay) {
        mOverlay = overlay;
        return this;
    }

    /**
     * @return the ToolTip container View
     */
    @Nullable
    public View getToolTip() {
        return mToolTipViewGroup;
    }

    /**
     * Set the toolTip
     *
     * @param toolTip this toolTip object should contain the attributes of the ToolTip, such as, the title text, and the description text,
     *                background color, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setToolTip(ToolTip toolTip) {
        mToolTip = toolTip;
        if (mPopupWindowOverlayLayout != null) {
            getWindow().getWindowManager().removeView(mPopupWindowOverlayLayout);
            setupToolTip();
        } else if (mToolTipViewGroup != null) {
            ((ViewGroup) getDecoView()).removeView(mToolTipViewGroup);
            setupToolTip();
        }
        return this;
    }

    @Nullable
    public TextView getToolTipTitleTextView() {
        return mToolTipTitleTextView;
    }

    @Nullable
    public TextView getToolTipDescTextView() {
        return mToolTipDescTextView;
    }

    private Animator getDefaultUpdateToolTipTextOutAnimator() {
        final ObjectAnimator oa = new ObjectAnimator();
        oa.setPropertyName("alpha");
        oa.setFloatValues(1f, 0f);
        oa.setDuration(100);
        return oa;
    }

    private Animator getDefaultUpdateToolTipTextInAnimator() {
        final ObjectAnimator oa = new ObjectAnimator();
        oa.setPropertyName("alpha");
        oa.setFloatValues(0f, 1f);
        oa.setDuration(100);
        return oa;
    }

    public void updateToolTipTitleText(CharSequence text) {
        if (mToolTipTitleTextView == null)
            return;
        updateViewText(mToolTipTitleTextView, text, getDefaultUpdateToolTipTextOutAnimator(), getDefaultUpdateToolTipTextInAnimator());
    }

    public void updateToolTipDescText(CharSequence text) {
        if (mToolTipDescTextView == null)
            return;
        updateViewText(mToolTipDescTextView, text, getDefaultUpdateToolTipTextOutAnimator(), getDefaultUpdateToolTipTextInAnimator());
    }

    public void updateViewText(final TextView tv, final CharSequence text,
        @Nullable final Animator outAnimator,
        @Nullable final Animator InAnimator) {
        if (tv == null)
            return;
        if (outAnimator != null) {
            outAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tv.setText(text);
                    outAnimator.removeListener(this);
                    if (InAnimator != null) {
                        InAnimator.setTarget(tv);
                        InAnimator.start();
                    }
                }
            });
            outAnimator.setTarget(tv);
            outAnimator.start();
        } else if (InAnimator != null) {
            InAnimator.setTarget(tv);
            InAnimator.start();
        }
    }

    /******
     *
     * Private methods
     *
     *******/
    //TODO: move into Pointer
    private int getXBasedOnGravity(int width, @Nullable int[] highlightedViewPos) {
        if (highlightedViewPos == null) {
            highlightedViewPos = getHighlightedViewPos();
        }
        int x = highlightedViewPos[0];
        if ((mPointer.mGravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return x + mHighlightedView.getWidth() - width;
        } else if ((mPointer.mGravity & Gravity.LEFT) == Gravity.LEFT) {
            return x;
        } else { // this is center
            return x + mHighlightedView.getWidth() / 2 - width / 2;
        }
    }

    //TODO: move into Pointer
    private int getYBasedOnGravity(int height, @Nullable int[] highlightedViewPos) {
        if (highlightedViewPos == null) {
            highlightedViewPos = getHighlightedViewPos();
        }
        int y = highlightedViewPos[1];

        if ((mPointer.mGravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return y + mHighlightedView.getHeight() - height;
        } else if ((mPointer.mGravity & Gravity.TOP) == Gravity.TOP) {
            return y;
        } else { // this is center
            return y + mHighlightedView.getHeight() / 2 - height / 2;
        }
    }

    private int[] getHighlightedViewPos() {
        if (mHighlightedView == null)
            return new int[]{0, 0};
        int[] pos = new int[2];
        mHighlightedView.getLocationOnScreen(pos);
        return pos;
    }

    protected void setupView() {
        // TourGuide can only be setup after all the views is ready and obtain it's position/measurement
        // so when this is the 1st time TourGuide is being added,
        // else block will be executed, and ViewTreeObserver will make TourGuide setup process to be delayed until everything is ready
        // when this is run the 2nd or more times, if block will be executed
        if (mHighlightedView == null || ViewCompat.isAttachedToWindow(mHighlightedView)) {
            startView();
        } else {
            final ViewTreeObserver viewTreeObserver = mHighlightedView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        mHighlightedView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mHighlightedView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    startView();
                }
            });
        }
    }

    private void startView() {
        /* Initialize a frame layout with a hole */
        mFrameLayout = new FrameLayoutWithHole(mActivity, mHighlightedView, mMotionType, mOverlay);

        /* handle click disable */
        handleDisableClicking(mFrameLayout);

        /* setup floating action button */
        if (mPointer != null) {
            final FloatingActionButton fab = setupAndAddFABToFrameLayout(mFrameLayout);
            performAnimationOn(fab);
        }

        if (mIsPopupWindow) {

            // To fix the issue tooltip flickers. Also tool tip shown under the popup window if it is listPopupWindow.
            getPopupWindowOverlayLayout().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getPopupWindowOverlayLayout().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        getPopupWindowOverlayLayout().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    /* setup tooltip view */
                    setupToolTip();
                }
            });


            setupFrameLayout();

        } else {


            setupFrameLayout();

            /* setup tooltip view */
            setupToolTip();
        }


    }

    private GestureDetector mGestureDetector;

    private void handleDisableClicking(final FrameLayoutWithHole frameLayoutWithHole) {
        if (mOverlay == null)
            return;

        if (mOverlay.hasTargetListeners() || mOverlay.mOnClickOutsideTargetListener != null || mOverlay.mDisableClickThrough || mOverlay
            .mDisableInteractWithTarget) {
            Log.w("tourguide", "Overlay's default OnClickListener is null, it will proceed to next tourguide when it is clicked");

            mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    if (frameLayoutWithHole.isWithinTargetBoundary(e) && mOverlay.mOnLongClickTargetListener != null) {
                        mOverlay.mOnLongClickTargetListener.onLongClick(frameLayoutWithHole);
                    }
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    boolean withinTarget = frameLayoutWithHole.isWithinTargetBoundary(e);
                    if (withinTarget && mOverlay.mOnClickTargetListener != null) {
                        mOverlay.mOnClickTargetListener.onClick(frameLayoutWithHole);
                        return true;
                    } else if (mOverlay.mOnClickOutsideTargetListener != null) {
                        mOverlay.mOnClickOutsideTargetListener.onClick(frameLayoutWithHole);
                        return true;
                    } else if (!withinTarget && mOverlay.mClickOutsideTargetToCancel) {
                        if (mOverlay.mOnClickOutsideCancelListener != null)
                            mOverlay.mOnClickOutsideCancelListener.onCancel(mActivity, TourGuide.this);
                        else
                            cleanUp();
                    }
                    return false;
                }
            });

            frameLayoutWithHole.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (frameLayoutWithHole.isWithinTargetBoundary(event) && mOverlay.mOnTouchTargetListener != null) {
                        if (mOverlay.mOnTouchTargetListener.onTouch(v, event))
                            return true;
                    }
                    return mGestureDetector.onTouchEvent(event);

                }
            });

            frameLayoutWithHole.setViewHole(mHighlightedView);
            frameLayoutWithHole.setSoundEffectsEnabled(false);
        }


        /* Original source */
        // // 1. if user provides an overlay listener, use that as 1st priority
        // if (mOverlay.mOnClickListener != null) {
        //     frameLayoutWithHole.setClickable(true);
        //     frameLayoutWithHole.setOnClickListener(mOverlay.mOnClickListener);
        // }
        // // 2. if overlay listener is not provided, check if it's disabled
        // if (mOverlay.mDisableClick) {
        //     Log.w("tourguide", "Overlay's default OnClickListener is null, it will proceed to next tourguide when it is clicked");
        //     frameLayoutWithHole.setViewHole(mHighlightedView);
        //     frameLayoutWithHole.setSoundEffectsEnabled(false);
        //     frameLayoutWithHole.setOnClickListener(new View.OnClickListener() {
        //         @Override
        //         public void onClick(View v) {
        //         } // do nothing, disabled.
        //     });
        // }
    }

    private void setupToolTip() {
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);

        if (mToolTip != null) {
            /* inflate and get views */

            ViewGroup parent = (ViewGroup) getDecoView();

            LayoutInflater layoutInflater = mActivity.getLayoutInflater();

            if (mToolTip.getCustomView() == null) {
                mToolTipViewGroup = layoutInflater.inflate(tourguide.tourguide.R.layout.tooltip, null);
                View toolTipContainer = mToolTipViewGroup.findViewById(tourguide.tourguide.R.id.toolTip_container);
                mToolTipTitleTextView = (TextView) mToolTipViewGroup.findViewById(tourguide.tourguide.R.id.title);
                mToolTipDescTextView = (TextView) mToolTipViewGroup.findViewById(tourguide.tourguide.R.id.description);

                /* set tooltip attributes */

                toolTipContainer.setBackgroundColor(mToolTip.mBackgroundColor);

                mToolTipTitleTextView.setTextColor(mToolTip.mTitleColor);
                mToolTipTitleTextView.setGravity(mToolTip.mTitleGravity);

                mToolTipDescTextView.setTextColor(mToolTip.mDescriptionColor);
                mToolTipDescTextView.setGravity(mToolTip.mDescriptionGravity);

                if (mToolTip.mTitle == null || mToolTip.mTitle.length() == 0) {
                    mToolTipTitleTextView.setVisibility(View.GONE);
                } else {
                    mToolTipTitleTextView.setVisibility(View.VISIBLE);
                    mToolTipTitleTextView.setText(mToolTip.mTitle);
                }

                if (mToolTip.mDescription == null || mToolTip.mDescription.length() == 0) {
                    mToolTipDescTextView.setVisibility(View.GONE);
                } else {
                    mToolTipDescTextView.setVisibility(View.VISIBLE);
                    mToolTipDescTextView.setText(mToolTip.mDescription);
                }

                if (mToolTip.mWidth != -1) {
                    layoutParams.width = mToolTip.mWidth;
                }
            } else {
                mToolTipViewGroup = mToolTip.getCustomView();
            }

            mToolTipViewGroup.startAnimation(mToolTip.mEnterAnimation);

            /* add setShadow if it's turned on */
            if (mToolTip.mShadow) {

                // mToolTipViewGroup.setBackgroundDrawable(mActivity.getResources().getDrawable(tourguide.tourguide.R.drawable.drop_shadow));
                mToolTipViewGroup.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.bg_trans_shadow));
            }

            /* position and size calculation */
            int targetViewX = -1;
            int targetViewY = -1;
            switch (mTarget) {
                case WINDOW:
                    break;
                case VIEW:
                    int[] pos = new int[2];
                    mHighlightedView.getLocationOnScreen(pos);
                    targetViewX = pos[0];
                    targetViewY = pos[1];
                    break;
            }

            // get measured size of tooltip
            // mToolTipViewGroup.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            /* Fix the issue the TextView's height is not correctly calculated
             * https://stackoverflow.com/questions/19908003/getting-height-of-text-view-before-rendering-to-layout
             * https://stackoverflow.com/questions/30591053/measure-height-of-multi-line-textview-before-rendering
             * */
            int widthSpec = View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.AT_MOST);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mToolTipViewGroup.measure(widthSpec, heightSpec);

            int toolTipMeasuredWidth = mToolTip.mWidth != -1 ? mToolTip.mWidth : mToolTipViewGroup.getMeasuredWidth();
            int toolTipMeasuredHeight = mToolTipViewGroup.getMeasuredHeight();

            Point resultPoint = new Point(); // this holds the final position of tooltip
            float density = mActivity.getResources().getDisplayMetrics().density;
            final float adjustment = mToolTip.mTooltipAndTargetViewOffset * density; //adjustment is that little overlapping area of tooltip and
            // targeted button

            // calculate x position, based on gravity, tooltipMeasuredWidth, parent max width, x position of target view, adjustment
            if (toolTipMeasuredWidth > parent.getWidth()) {
                resultPoint.x = getXForTooTip(mToolTip.mGravity, parent.getWidth(), targetViewX, adjustment);
            } else {
                resultPoint.x = getXForTooTip(mToolTip.mGravity, toolTipMeasuredWidth, targetViewX, adjustment);
            }

            resultPoint.y = getYForTooTip(mToolTip.mGravity, toolTipMeasuredHeight, targetViewY, adjustment);

            // add view to parent
            //            ((ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(mToolTipViewGroup,
            // layoutParams);
            // parent.addView(mToolTipViewGroup, layoutParams);

            // 1. width < screen check
            if (toolTipMeasuredWidth > parent.getWidth()) {
                layoutParams.width = parent.getWidth();
                toolTipMeasuredWidth = parent.getWidth();
            }
            // 2. x left boundary check
            if (resultPoint.x < 0) {
                resultPoint.x = 0;
            }
            // 3. x right boundary check
            int tempRightX = resultPoint.x + toolTipMeasuredWidth;
            if (tempRightX > parent.getWidth()) {
                layoutParams.width = toolTipMeasuredWidth;
                resultPoint.x = parent.getWidth() - toolTipMeasuredWidth;
            }

            // pass toolTip onClickListener into toolTipViewGroup
            // We wrap the tooltip's click listener in order to prevent overlay's click outside to cancel listener.
            mToolTipViewGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mToolTip.mOnClickListener != null)
                        mToolTip.mOnClickListener.onClick(v);
                }
            });

            // TODO: no boundary check for height yet, this is a unlikely case though
            // height boundary can be fixed by user changing the gravity to the other size, since there are plenty of space vertically compared to
            // horizontally

            if (mIsPopupWindow && mToolTipViewGroup != null) {

                // // To fix the issue ListPopupWindow tool tip does not position correctly..
                getPopupWindowOverlayLayout().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    int[] mLastPos = new int[]{0, 0};

                    @Override
                    public void onGlobalLayout() {
                        if (mToolTipViewGroup == null) {
                            removeOnGlobalLayoutListener(mPopupWindowOverlayLayout, this);
                            return;
                        }
                        int[] pos = getHighlightedViewPos();

                        if (pos[0] != mLastPos[0] || pos[1] != mLastPos[1]) {
                            mLastPos = pos;
                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mToolTipViewGroup.getLayoutParams();
                            int y = getYForTooTip(mToolTip.mGravity, mToolTipViewGroup.getHeight(), pos[1], adjustment);
                            int x = getXForTooTip(mToolTip.mGravity, mToolTipViewGroup.getWidth(), pos[0], adjustment);
                            lp.setMargins(x, y, 0, 0);
                            mToolTipViewGroup.setLayoutParams(lp);
                        }
                    }
                });
            }


            /* Legacy */
            // this needs an viewTreeObserver, that's because TextView measurement of it's vertical height is not accurate (didn't take into
            // account of multiple lines yet) before it's rendered.
            // re-calculate height again once it's rendered
            // mToolTipViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            //     @Override
            //     public void onGlobalLayout() {
            //         // make sure this only run once
            //         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //             //noinspection deprecation
            //             mToolTipViewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            //         } else {
            //             mToolTipViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            //         }
            //
            //         int fixedY;
            //         int toolTipHeightAfterLayouted = mToolTipViewGroup.getHeight();
            //         fixedY = getYForTooTip(mToolTip.mGravity, toolTipHeightAfterLayouted, targetViewY, adjustment);
            //         layoutParams.setMargins((int) mToolTipViewGroup.getX(), fixedY, 0, 0);
            //         mToolTipViewGroup.setLayoutParams(layoutParams);
            //     }
            // });

            // set the position using setMargins on the left and top
            layoutParams.setMargins(resultPoint.x, resultPoint.y, 0, 0);

            if (mIsPopupWindow) {
                getPopupWindowOverlayLayout().addView(mToolTipViewGroup, layoutParams);
            } else
                parent.addView(mToolTipViewGroup, layoutParams);

        }

    }

    private ViewGroup getPopupWindowOverlayLayout() {
        if (mPopupWindowOverlayLayout == null) {
            mPopupWindowOverlayLayout = new FrameLayout(mActivity);
            addToWindowManager(mPopupWindowOverlayLayout);
        }
        return mPopupWindowOverlayLayout;
    }

    private void addToWindowManager(View v) {
        WindowManager wm = getWindow().getWindowManager();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT);
        lp.packageName = mActivity.getPackageName();
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        lp.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        wm.addView(v, lp);
    }

    private int getXForTooTip(int gravity, int toolTipMeasuredWidth, int targetViewX, float adjustment) {
        int x = 0;

        switch (mTarget) {
            case WINDOW:
                if ((gravity & Gravity.START) == Gravity.START) {
                    x = 0;
                } else if ((gravity & Gravity.END) == Gravity.END) {
                    x = getScreenWidth() - toolTipMeasuredWidth - (int) adjustment;
                } else {
                    x = getScreenWidth() / 2 - toolTipMeasuredWidth / 2 - (int) adjustment;
                }
                break;
            case VIEW:
                if ((gravity & Gravity.START) == Gravity.START) {
                    x = targetViewX - toolTipMeasuredWidth + (int) adjustment;
                } else if ((gravity & Gravity.END) == Gravity.END) {
                    x = targetViewX + mHighlightedView.getWidth() - (int) adjustment;
                } else {
                    x = targetViewX + mHighlightedView.getWidth() / 2 - toolTipMeasuredWidth / 2;
                }
                break;
        }

        return x;
    }

    private int getYForTooTip(int gravity, int toolTipMeasuredHeight, int targetViewY, float adjustment) {
        int y = 0;

        switch (mTarget) {
            case WINDOW:
                switch (gravity) {
                    case Gravity.TOP:
                        y = 0;
                        break;
                    case Gravity.CENTER:
                        y = getScreenHeight() / 2 - toolTipMeasuredHeight / 2 - (int) adjustment;
                        break;
                    case Gravity.BOTTOM:
                        y = getScreenHeight() - toolTipMeasuredHeight - (int) adjustment;
                        break;
                }
                break;
            case VIEW:
                if ((gravity & Gravity.TOP) == Gravity.TOP) {

                    if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                        y = targetViewY - toolTipMeasuredHeight + (int) adjustment;
                    } else {
                        y = targetViewY - toolTipMeasuredHeight - (int) adjustment;
                    }
                } else { // this is center
                    if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                        y = targetViewY + mHighlightedView.getHeight() - (int) adjustment;
                    } else {
                        y = targetViewY + mHighlightedView.getHeight() + (int) adjustment;
                    }
                }
                break;
        }
        return y;
    }

    private FloatingActionButton setupAndAddFABToFrameLayout(final FrameLayoutWithHole frameLayoutWithHole) {
        // invisFab is invisible, and it's only used for getting the width and height
        final FloatingActionButton invisFab = new FloatingActionButton(mActivity);
        invisFab.setSize(FloatingActionButton.SIZE_MINI);
        invisFab.setVisibility(View.INVISIBLE);

        ((ViewGroup) getDecoView()).addView(invisFab);

        // fab is the real fab that is going to be added
        final FloatingActionButton fab = new FloatingActionButton(mActivity);
        fab.setBackgroundColor(Color.BLUE);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setColorNormal(mPointer.mColor);
        fab.setStrokeVisible(false);
        fab.setClickable(false);

        // When invisFab is layouted, it's width and height can be used to calculate the correct position of fab
        invisFab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // make sure this only run once
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    //noinspection deprecation
                    invisFab.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    invisFab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

                final int[] pos = getHighlightedViewPos();

                // measure size of image to be placed
                params.setMargins(getXBasedOnGravity(invisFab.getWidth(), pos), getYBasedOnGravity(invisFab.getHeight(), pos), 0, 0);

                frameLayoutWithHole.addView(fab, params);

                // To fix the issue ListPopupWindow tool tip does not position correctly..
                if (mIsPopupWindow && mHighlightedView != null && mFrameLayout != null) {
                    getPopupWindowOverlayLayout().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        int[] mLastPos = new int[]{0, 0};

                        @Override
                        public void onGlobalLayout() {
                            if (mFrameLayout == null) {
                                removeOnGlobalLayoutListener(mPopupWindowOverlayLayout, this);
                                return;
                            }
                            final int[] pos = getHighlightedViewPos();

                            if (pos[0] != mLastPos[0] || pos[1] != mLastPos[1]) {
                                mLastPos = pos;
                                final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
                                lp.setMargins(getXBasedOnGravity(fab.getWidth(), pos), getYBasedOnGravity(fab.getHeight(), pos), 0, 0);
                                fab.setLayoutParams(lp);
                            }
                        }
                    });
                }
            }
        });


        return fab;
    }

    private void setupFrameLayout() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);

        if (mIsPopupWindow) {
            getPopupWindowOverlayLayout().addView(mFrameLayout, layoutParams);
        } else {
            ViewGroup contentArea = (ViewGroup) getDecoView().findViewById(android.R.id.content);

            int[] pos = new int[2];
            contentArea.getLocationOnScreen(pos);
            // frameLayoutWithHole's coordinates are calculated taking full screen height into account
            // but we're adding it to the content area only, so we need to offset it to the same Y value of contentArea

            layoutParams.setMargins(0, -pos[1], 0, 0);
            contentArea.addView(mFrameLayout, layoutParams);
        }
    }

    private void performAnimationOn(final View view) {

        if (mTechnique != null && mTechnique == Technique.HORIZONTAL_LEFT) {

            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long goLeftXDuration = 2000;
            long fadeOutDuration = goLeftXDuration;
            float translationX = getScreenWidth() / 2;

            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX2 = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX2.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);

            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(goLeftX).with(fadeOutAnim).after(scaleDownY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(goLeftX2).with(fadeOutAnim2).after(scaleDownY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            mFrameLayout.addAnimatorSet(animatorSet);
            mFrameLayout.addAnimatorSet(animatorSet2);
        } else if (mTechnique != null && mTechnique == Technique.HORIZONTAL_RIGHT) { //TODO: new feature

        } else if (mTechnique != null && mTechnique == Technique.VERTICAL_UPWARD) {//TODO: new feature

        } else if (mTechnique != null && mTechnique == Technique.VERTICAL_DOWNWARD) {//TODO: new feature

        } else { // do click for default case
            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long fadeOutDuration = 800;
            long delay = 1000;

            final ValueAnimator delayAnim = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim.setDuration(delay);
            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator delayAnim2 = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim2.setDuration(delay);
            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX2 = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY2 = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY2.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);
            view.setAlpha(0);
            animatorSet.setStartDelay(mToolTip != null ? mToolTip.mEnterAnimation.getDuration() : 0);
            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(scaleUpX).with(scaleUpY).with(fadeOutAnim).after(scaleDownY);
            animatorSet.play(delayAnim).after(scaleUpY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(scaleUpX2).with(scaleUpY2).with(fadeOutAnim2).after(scaleDownY2);
            animatorSet2.play(delayAnim2).after(scaleUpY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            mFrameLayout.addAnimatorSet(animatorSet);
            mFrameLayout.addAnimatorSet(animatorSet2);
        }
    }

    private int getScreenWidth() {
        if (mActivity != null) {
            return mActivity.getResources().getDisplayMetrics().widthPixels;
        } else {
            return 0;
        }
    }

    private int getScreenHeight() {
        if (mActivity != null) {
            return mActivity.getResources().getDisplayMetrics().heightPixels;
        } else {
            return 0;
        }
    }

    private View getDecoView() {
        return getWindow().getDecorView();
    }

    private Window getWindow() {
        if (mDialogFragment != null && mDialogFragment.getDialog() != null && mDialogFragment.getDialog().getWindow() != null) {
            return mDialogFragment.getDialog().getWindow();
        } else
            return mActivity.getWindow();
    }

    /**
     * This describes the animation techniques
     */
    public enum Technique {
        CLICK, HORIZONTAL_LEFT, HORIZONTAL_RIGHT, VERTICAL_UPWARD, VERTICAL_DOWNWARD
    }

    /**
     * This describes the allowable motion, for example if you want the users to learn about clicking, but want to stop them from swiping, then use
     * CLICK_ONLY
     */
    public enum MotionType {
        ALLOW_ALL, CLICK_ONLY, SWIPE_ONLY
    }

    private void removeOnGlobalLayoutListener(@Nullable View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (v == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }

}
