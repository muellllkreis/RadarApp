package com.radarapp.mjr9r.radar.helpers;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class MarkerAnimator {
    private static Handler handler = new Handler();

    public Handler getHandler() {
        return handler;
    }

    public static void stopAnimation() {
        handler.removeCallbacksAndMessages(null);
    }

    public static void pulseMarker(final Bitmap markerIcon, final Marker marker, final long onePulseDuration) {
     //   handler = new Handler();
        final long startTime = System.currentTimeMillis();

        final Interpolator interpolator = new CycleInterpolator(1f);
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / onePulseDuration);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaleBitmap(markerIcon, 1f + 0.05f * t)));
                handler.postDelayed(this, 16);
            }
        });
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float scaleFactor) {
        final int sizeX = Math.round(bitmap.getWidth() * scaleFactor);
        final int sizeY = Math.round(bitmap.getHeight() * scaleFactor);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);
        return bitmapResized;
    }
}
