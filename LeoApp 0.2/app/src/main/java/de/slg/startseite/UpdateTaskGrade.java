package de.slg.startseite;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateTaskGrade extends AsyncTask<String, Void, Boolean> {

    private boolean connection;
    private Context c;

    public UpdateTaskGrade(Context c) {

        this.c = c;

    }

    @Override
    protected Boolean doInBackground(String... params) {
        BufferedReader in = null;
        String result = "";

        connection = hasActiveInternetConnection();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);

        if (!connection)
            return false;

        try {

            int id = pref.getInt("pref_key_general_id", -1);
            String klasse = String.valueOf(pref.getInt("pref_key_level_general", 0));
            klasse = klasse.replace("10", "EF");
            klasse = klasse.replace("11", "Q1");
            klasse = klasse.replace("12", "Q2");

            URL interfaceDB = new URL("http://moritz.liegmanns.de/updateKlasse.php?key=5453&userid="+id+"&userklasse="+klasse);

            in = null;
            in = new BufferedReader(new InputStreamReader(interfaceDB.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.contains("<"))
                    result += inputLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
        }

        return !result.startsWith("-");
    }

    @Override
    protected void onPostExecute(Boolean b) {

        if(!b) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor e = pref.edit();
            e.putBoolean("pref_key_level_has_to_be_synchronized", true);
            e.apply();

        }

    }

    public boolean hasActiveInternetConnection() {

        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.lunch.leo-ac.de").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return urlc.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
