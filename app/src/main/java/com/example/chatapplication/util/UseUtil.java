package com.example.chatapplication.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.chatapplication.R;

public class UseUtil {
    public static int changeFragmentAnimation1st = R.animator.enter_from_right;
    public static int changeFragmentAnimation2nd = R.animator.exit_to_left;
    public static int changeFragmentAnimation3rd = R.animator.enter_from_right;
    public static int changeFragmentAnimation4th = R.animator.exit_to_left;

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
