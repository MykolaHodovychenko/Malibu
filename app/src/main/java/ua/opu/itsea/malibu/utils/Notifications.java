package ua.opu.itsea.malibu.utils;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Malibu, created by NickGodov on 12.09.2015.
 * This software is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this program, or any portion of it, may result in severe
 * civil and criminal penalties, and will be prosecuted to the maximum extent possible under law.
 */
public class Notifications {

    public static void showErrorNotification(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View v = snackbar.getView();
        v.setBackgroundColor(Color.parseColor("#F50057"));
        snackbar.show();
    }

    public static void showOkNotification(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        v.setBackgroundColor(Color.parseColor("#00C853"));
        snackbar.show();
    }
}
