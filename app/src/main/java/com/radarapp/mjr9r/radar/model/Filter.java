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
    LEARN ("LEARN"),
    EVENT ("EVENT"),
    SECRET ("SECRET"),
    FOOD ("FOOD");

    private final String name;
    private final String color;
    private final int iconID;

    Filter(String name) {
        this.name = name;
        this.color = Filter.chooseColor(name);

        switch(name) {
            case "CUTE":
                this.iconID = R.drawable.ic_bottomsheet_cute_icon;
                break;
            case "DEAL":
                this.iconID = R.drawable.ic_bottomsheet_deal_icon;
                break;
            case "LOL":
                this.iconID = R.drawable.ic_bottomsheet_lol_icon;
                break;
            case "LEARN":
                this.iconID = R.drawable.ic_bottomsheet_educational_icon;
                break;
            case "EVENT":
                this.iconID = R.drawable.ic_bottomsheet_event_icon;
                break;
            case "SECRET":
                this.iconID = R.drawable.ic_bottomsheet_scavenger_icon;
                break;
            case "FOOD":
                this.iconID = R.drawable.ic_bottomsheet_yumyum_icon;
                break;
            default:
                this.iconID = R.drawable.ic_bottomsheet_lol_icon;
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
            case "LEARN":
                return "#FF6B47";
            case "EVENT":
                return "#718DE8";
            case "SECRET":
                return "#705549";
            case "FOOD":
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
            case "LEARN":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "EVENT":
                return BitmapDescriptorFactory.HUE_BLUE;
            case "SECRET":
                return BitmapDescriptorFactory.HUE_VIOLET;
            case "FOOD":
                return BitmapDescriptorFactory.HUE_GREEN;
            default:
                return BitmapDescriptorFactory.HUE_ROSE;
        }
    }

    public static int chooseCircleStrokeColor(String filter) {
        switch(filter) {
            case "CUTE":
                return R.color.cute;
            case "DEAL":
                return R.color.deal;
            case "LOL":
                return R.color.lol;
            case "LEARN":
                return R.color.learn;
            case "EVENT":
                return R.color.event;
            case "SECRET":
                return R.color.secret;
            case "FOOD":
                return R.color.food;
            default:
                return R.color.cute;
        }
    }

    public static int chooseCircleFillColor(String filter) {
        switch(filter) {
            case "CUTE":
                return R.color.transp_cute;
            case "DEAL":
                return R.color.transp_deal;
            case "LOL":
                return R.color.transp_lol;
            case "LEARN":
                return R.color.transp_learn;
            case "EVENT":
                return R.color.transp_event;
            case "SECRET":
                return R.color.transp_secret;
            case "FOOD":
                return R.color.transp_food;
            default:
                return R.color.transp_cute;
        }
    }

    public static int chooseMarkerIcon(String filter) {
        switch(filter) {
            case "CUTE":
                return R.drawable.ic_marker_cute;
            case "DEAL":
                return R.drawable.ic_marker_deal;
            case "LOL":
                return R.drawable.ic_marker_lol;
            case "LEARN":
                return R.drawable.ic_marker_educational;
            case "EVENT":
                return R.drawable.ic_marker_event;
            case "SECRET":
                return R.drawable.ic_marker_scavenger;
            case "FOOD":
                return R.drawable.ic_marker_yumyum;
            default:
                return R.drawable.ic_marker_cute;
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
