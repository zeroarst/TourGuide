package tourguide.tourguide;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;

/**
 * Created by tanjunrong on 6/17/15.
 */
public class ToolTip {
    protected CharSequence mTitle, mDescription;
    protected int mBackgroundColor, mTitleColor, mDescriptionColor;
    protected int mTitleGravity, mDescriptionGravity;
    protected Animation mEnterAnimation, mExitAnimation;
    protected boolean mShadow;
    protected int mGravity;

    View.OnClickListener mOnClickListener;
    ViewGroup mCustomView;
    int mWidth;
    protected int mTooltipAndTargetViewOffset; // adjustment is that little overlapping area of tooltip and targeted view


    public ToolTip() {
        /* default values */
        mTitle = "";
        mDescription = "";
        mBackgroundColor = Color.parseColor("#3498db");
        mTitleColor = mDescriptionColor = Color.parseColor("#FFFFFF");

        mTitleGravity = Gravity.CENTER;
        mDescriptionGravity = Gravity.CENTER;

        mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(1000);
        mEnterAnimation.setFillAfter(true);
        mEnterAnimation.setInterpolator(new BounceInterpolator());
        mShadow = true;
        mWidth = -1;

        mGravity = Gravity.CENTER;

        mTooltipAndTargetViewOffset = 10;

        // TODO: exit animation
    }

    /**
     * Set title text
     *
     * @param title
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    /**
     * Set description text
     *
     * @param description
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setDescription(CharSequence description) {
        mDescription = description;
        return this;
    }

    /**
     * Set background color
     *
     * @param backgroundColor
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        return this;
    }

    /**
     * Set title color
     *
     * @param color
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setTitleColor(int color) {
        mTitleColor = color;
        return this;
    }

    /**
     * Set description color
     *
     * @param color
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setDescriptionColor(int color) {
        mDescriptionColor = color;
        return this;
    }

    public ToolTip setTitleGravity(int gravity) {
        this.mTitleGravity = gravity;
        return this;
    }

    public ToolTip setDescriptionGravity(int gravity) {
        this.mDescriptionGravity = gravity;
        return this;
    }

    /**
     * Set enter animation
     *
     * @param enterAnimation
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setEnterAnimation(Animation enterAnimation) {
        mEnterAnimation = enterAnimation;
        return this;
    }
    /**
     * Set exit animation
     * @param exitAnimation
     * @return return ToolTip instance for chaining purpose
     */
    //    TODO:
    //    public ToolTip setExitAnimation(Animation exitAnimation){
    //        mExitAnimation = exitAnimation;
    //        return this;
    //    }

    /**
     * Set the gravity, the setGravity is centered relative to the targeted button
     *
     * @param gravity Gravity.CENTER, Gravity.TOP, Gravity.BOTTOM, etc
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setGravity(int gravity) {
        mGravity = gravity;
        return this;
    }

    /**
     * Set if you want to have setShadow
     *
     * @param shadow
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setShadow(boolean shadow) {
        mShadow = shadow;
        return this;
    }

    /**
     * Method to set the width of the ToolTip
     *
     * @param px desired width of ToolTip in pixels
     * @return ToolTip instance for chaining purposes
     */
    public ToolTip setWidth(int px) {
        if (px >= 0)
            mWidth = px;
        return this;
    }

    public ToolTip setOffset(int overlapping) {
        this.mTooltipAndTargetViewOffset = overlapping;
        return this;
    }

    public ToolTip setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }

    public ViewGroup getCustomView() {
        return mCustomView;
    }

    public ToolTip setCustomView(ViewGroup view) {
        mCustomView = view;
        return this;
    }
}
