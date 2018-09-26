package com.radarapp.mjr9r.radar.helpers;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias on 21.09.2018.
 */

public class ViewTagHelper {
    public static List<View> findViewWithTagRecursively(ViewGroup root, Object tag){
        List<View> allViews = new ArrayList<View>();

        final int childCount = root.getChildCount();
        for(int i=0; i<childCount; i++){
            final View childView = root.getChildAt(i);

            if(childView instanceof ViewGroup){
                allViews.addAll(findViewWithTagRecursively((ViewGroup)childView, tag));
            }
            else{
                final Object tagView = childView.getTag();
                if(tagView != null && tagView.equals(tag))
                    allViews.add(childView);
            }
        }

        return allViews;
    }
}
