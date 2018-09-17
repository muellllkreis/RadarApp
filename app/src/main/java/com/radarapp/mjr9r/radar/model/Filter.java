package com.radarapp.mjr9r.radar.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.radarapp.mjr9r.radar.BuildConfig;
import com.radarapp.mjr9r.radar.R;

import java.net.URL;

public enum Filter {
    CUTE ("CUTE"),
    DEAL ("DEAL"),
    LOL ("LOL"),
    EDUCATIONAL ("EDUCATIONAL"),
    EVENT ("EVENT"),
    SCAVENGER ("SCAVENGER"),
    YUMYUM ("YUMYUM");

    private final String name;
    private final String color;
    private final int iconID;

    Filter(String name) {
        this.name = name;
        this.color = Filter.chooseColor(name);

        switch(name) {
            case "CUTE":
                this.iconID = R.drawable.cute_icon;
                break;
            case "DEAL":
                this.iconID = R.drawable.deal_icon;
                break;
            case "LOL":
                this.iconID = R.drawable.lol_icon;
                break;
            case "EDUCATIONAL":
                this.iconID = R.drawable.education_icon;
                break;
            case "EVENT":
                this.iconID = R.drawable.event_icon;
                break;
            case "SCAVENGER":
                this.iconID = R.drawable.scavenger_icon;
                break;
            case "YUMYUM":
                this.iconID = R.drawable.food_icon;
                break;
            default:
                this.iconID = R.drawable.lol_icon;
                break;
        }
    }

    private static String chooseColor(String filter) {
        switch(filter) {
            case "CUTE":
                return "#FFAAFF";
            case "DEAL":
                return "#E8CA4D";
            case "LOL":
                return "#E8A051";
            case "EDUCATIONAL":
                return "#FF6B47";
            case "EVENT":
                return "#718DE8";
            case "SCAVENGER":
                return "#705549";
            case "YUMYUM":
                return "#478A46";
            default:
                return "#FFFFFF";
        }
    }

    public static float chooseMarkerColor(String filter) {
        switch(filter) {
            case "CUTE":
                return BitmapDescriptorFactory.HUE_ROSE;
            case "DEAL":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "LOL":
                return BitmapDescriptorFactory.HUE_RED;
            case "EDUCATIONAL":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "EVENT":
                return BitmapDescriptorFactory.HUE_BLUE;
            case "SCAVENGER":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "YUMYUM":
                return BitmapDescriptorFactory.HUE_GREEN;
            default:
                return BitmapDescriptorFactory.HUE_ROSE;
        }
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getIconID() {
        return iconID;
    }
}
