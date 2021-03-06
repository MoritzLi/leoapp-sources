package de.slg.leoapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.slg.startseite.MainActivity;

public class Utils {
    public static Context context;

    public static boolean checkNetwork() {
        ConnectivityManager c = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (c != null) {
            NetworkInfo n = c.getActiveNetworkInfo();
            if (n != null) {
                return n.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    public static User getCurrentUser() {
        return new User(getUserID(), "Du", getUserStufe(), getUserPermission());
    }

    public static int getUserID() {
        return MainActivity.pref.getInt("pref_key_general_id", -1);
    }

    public static String getUserName() {
        return MainActivity.pref.getString("pref_key_username_general", context.getString(R.string.drawer_placeholder));
    }

    public static String getUserStufe() {
        int i = MainActivity.pref.getInt("pref_key_level_general", -1);
        if (i >= 5)
            return new String[]{"5", "6", "7", "8", "9", "EF", "Q1", "Q2"}[i - 5];
        return "";
    }

    public static int getUserPermission() {
        return MainActivity.pref.getInt("pref_key_general_permission", 0);
    }

    public static int getCurrentMoodRessource() {
        int i = MainActivity.pref.getInt("pref_key_general_vote", -1);
        switch (i) {
            case 1:
                return R.drawable.ic_sentiment_very_satisfied_black_24dp;
            case 2:
                return R.drawable.ic_sentiment_satisfied_black_24dp;
            case 3:
                return R.drawable.ic_sentiment_neutral_black_24dp;
            case 4:
                return R.drawable.ic_sentiment_dissatisfied_black_24dp;
            case 5:
                return R.drawable.ic_mood_bad_black_24dp;
            default:
                return R.drawable.ic_account_circle_black_24dp;
        }
    }

    public static String getLastVote() {
        return MainActivity.pref.getString("pref_key_general_last_vote", "00.00");
    }

    public static String getString(int id) {

        return context.getString(id);

    }

    public static String getCurrentDate(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static boolean isVerified() {
        return getUserID() > -1;
    }
}
