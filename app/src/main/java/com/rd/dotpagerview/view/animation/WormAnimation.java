package com.rd.dotpagerview.view.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;

public class WormAnimation extends AbsAnimation<AnimatorSet> {

    private static final int ANIMATION_DURATION = 175;

    private int fromValue;
    private int toValue;
    private int radius;
    private boolean isRightSide;

    private int rectLeftX;
    private int rectRightX;

    public WormAnimation(@NonNull ValueAnimation.UpdateListener listener) {
        super(listener);
    }

    @NonNull
    @Override
    public AnimatorSet createAnimator() {
        AnimatorSet animator = new AnimatorSet();
        animator.setInterpolator(new DecelerateInterpolator());

        return animator;
    }

    @Override
    public long getAnimationDuration() {
        return ANIMATION_DURATION;
    }

    public WormAnimation with(int fromValue, int toValue, int radius, boolean isRightSide) {
        if (hasChanges(fromValue, toValue, radius, isRightSide)) {
            animator = createAnimator();

            this.fromValue = fromValue;
            this.toValue = toValue;
            this.radius = radius;
            this.isRightSide = isRightSide;

            AnimationValues values = createAnimationValues(isRightSide);
            ValueAnimator straightAnimator = createValueAnimator(values.fromX, values.toX, false);
            ValueAnimator reverseAnimator = createValueAnimator(values.reverseFromX, values.reverseToX, true);

            animator.playSequentially(straightAnimator, reverseAnimator);
        }
        return this;
    }

    @Override
    public void progress(float progress) {
        if (animator != null) {
            long fullDuration = ANIMATION_DURATION * animator.getChildAnimations().size();
            long playTimeLeft = (long) (progress * fullDuration);

            for (Animator anim : animator.getChildAnimations()) {
                ValueAnimator valueAnimator = (ValueAnimator) anim;

                if (playTimeLeft < 0) {
                    playTimeLeft = 0;
                }

                long currPlayTime = playTimeLeft;
                if (currPlayTime >= valueAnimator.getDuration()) {
                    currPlayTime = valueAnimator.getDuration();
                }

                valueAnimator.setCurrentPlayTime(currPlayTime);
                playTimeLeft -= currPlayTime;
            }
        }
    }

    private ValueAnimator createValueAnimator(int fromX, int toX, final boolean isReverseAnimator) {
        ValueAnimator animator = ValueAnimator.ofInt(fromX, toX);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();

                if (!isReverseAnimator) {
                    if (isRightSide) {
                        rectRightX = value;
                    } else {
                        rectLeftX = value;
                    }

                } else {
                    if (isRightSide) {
                        rectLeftX = value;
                    } else {
                        rectRightX = value;
                    }
                }

                listener.onWormAnimationUpdated(rectLeftX, rectRightX);
            }
        });

        return animator;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean hasChanges(int fromValue, int toValue, int radius, boolean isRightSide) {
        if (this.fromValue != fromValue) {
            return true;
        }

        if (this.toValue != toValue) {
            return true;
        }

        if (this.radius != radius) {
            return true;
        }

        if (this.isRightSide != isRightSide) {
            return true;
        }

        return false;
    }

    @NonNull
    private AnimationValues createAnimationValues(boolean isRightSide) {
        int fromX;
        int toX;

        int reverseFromX;
        int reverseToX;

        if (isRightSide) {
            fromX = fromValue + radius;
            toX = toValue + radius;

            reverseFromX = fromValue - radius;
            reverseToX = toValue - radius;

        } else {
            fromX = fromValue - radius;
            toX = toValue - radius;

            reverseFromX = fromValue + radius;
            reverseToX = toValue + radius;
        }

        return new AnimationValues(fromX, toX, reverseFromX, reverseToX);
    }

    private class AnimationValues {

        public final int fromX;
        private final int toX;

        private final int reverseFromX;
        private final int reverseToX;

        public AnimationValues(int fromX, int toX, int reverseFromX, int reverseToX) {
            this.fromX = fromX;
            this.toX = toX;

            this.reverseFromX = reverseFromX;
            this.reverseToX = reverseToX;
        }
    }
}