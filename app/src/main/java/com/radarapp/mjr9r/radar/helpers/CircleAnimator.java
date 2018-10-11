package com.radarapp.mjr9r.radar.helpers;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.google.android.gms.maps.model.Circle;

public class CircleAnimator {

    public static void animateCircle(final Circle circle, double toRadius) {
        final float fromRadius = 0;
        final ValueAnimator animator = ValueAnimator.ofFloat(fromRadius, (float) toRadius);
        animator.setDuration(1000)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        circle.setRadius(value);
                    }
                });
        animator.start();
    }
}
