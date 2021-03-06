package de.slg.leoapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.slg.essensqr.Auth;
import de.slg.essensqr.WrapperQRActivity;
import de.slg.klausurplan.KlausurplanActivity;
import de.slg.messenger.OverviewWrapper;
import de.slg.schwarzes_brett.SchwarzesBrettActivity;
import de.slg.startseite.MainActivity;
import de.slg.startseite.UpdateTaskGrade;
import de.slg.startseite.UpdateTaskName;
import de.slg.stimmungsbarometer.StimmungsbarometerActivity;
import de.slg.stundenplan.WrapperStundenplanActivity;

@SuppressWarnings("deprecation")
public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ProgressBar progressBar;
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    private static String currentUsername;
    protected SharedPreferences pref;
    private DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        addPreferencesFromResource(R.xml.preferences);
        initToolbar();
        initNavigationView();

        pref = getPreferenceScreen().getSharedPreferences();

        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        hideProgressBar();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        int permission = pref.getInt("pref_key_general_permission", 0);
        currentUsername = pref.getString("pref_key_username_general", "");

        String res = String.valueOf(pref.getInt("pref_key_level_general", 0));
        res = res.replace("10", "EF");
        res = res.replace("11", "Q1");
        res = res.replace("12", "Q2");
        if(res.equals("0"))
            res = "N/A";
        findPreference("pref_key_level_general").setSummary(res);
        findPreference("pref_key_username_general").setSummary(currentUsername);

        if(permission == 2 || !MainActivity.isVerified()) {
            findPreference("pref_key_level_general").setEnabled(false);
            findPreference("pref_key_username_general").setSummary("N/A");
        }

        if(!MainActivity.isVerified())
            findPreference("pref_key_username_general").setEnabled(false);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        int verCode = pInfo.versionCode;

        findPreference("pref_key_version_app").setSummary(version + " ("+verCode+")");

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (!getPreferenceScreen().getSharedPreferences().getString("pref_key_qr_id", "").equals("")) {

            Preference connectionPref = findPreference("pref_key_qr_id");
            connectionPref.setSummary(getPreferenceScreen().getSharedPreferences().getString("pref_key_qr_id", ""));

        }

        if (!getPreferenceScreen().getSharedPreferences().getString("pref_key_qr_pw", "").equals("")) {

            Preference connectionPref = findPreference("pref_key_qr_pw");
            connectionPref.setSummary(getRepl(getPreferenceScreen().getSharedPreferences().getString("pref_key_qr_pw", "passwort")));

        }

        Preference connectionPref = findPreference("pref_key_qr_autofade_time");
        connectionPref.setEnabled(getPreferenceScreen().getSharedPreferences().getBoolean("pref_key_qr_autofade", false));

        Preference notifPref = findPreference("pref_key_notification_time");
        notifPref.setEnabled(getPreferenceScreen().getSharedPreferences().getBoolean("pref_key_notification", false));

        SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
        editor.putBoolean("pref_key_status_loggedin", getPreferenceScreen().getSharedPreferences().getBoolean("pref_key_status_loggedin", false));
        editor.apply();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initToolbar() {
        Toolbar actionBar = (Toolbar) findViewById(R.id.toolbarSettings);
        actionBar.setTitleTextColor(getResources().getColor(android.R.color.white));
        actionBar.setTitle(getString(R.string.title_settings));
        setActionBar(actionBar);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().findItem(R.id.settings).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Intent i;
                switch (menuItem.getItemId()) {
                    case R.id.foodmarks:
                        i = new Intent(getApplicationContext(), WrapperQRActivity.class);
                        break;
                    case R.id.messenger:
                        i = new Intent(getApplicationContext(), OverviewWrapper.class);
                        break;
                    case R.id.newsboard:
                        i = new Intent(getApplicationContext(), SchwarzesBrettActivity.class);
                        break;
                    case R.id.nachhilfe:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        break;
                    case R.id.stundenplan:
                        i = new Intent(getApplicationContext(), WrapperStundenplanActivity.class);
                        break;
                    case R.id.barometer:
                        i = new Intent(getApplicationContext(), StimmungsbarometerActivity.class);
                        break;
                    case R.id.klausurplan:
                        i = new Intent(getApplicationContext(), KlausurplanActivity.class);
                        break;
                    case R.id.startseite:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        break;
                    case R.id.settings:
                        i = new Intent(getApplicationContext(), PreferenceActivity.class);
                        break;
                    default:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                startActivity(i);
                finish();
                return true;
            }
        });
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        username.setText(Utils.getUserName());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference connectionPref;
        SharedPreferences pref = getPreferenceScreen().getSharedPreferences();

        switch (key) {

            case "pref_key_qr_id":
                if (sharedPreferences.getString("pref_key_qr_id", "").length() != 5 || !contains(sharedPreferences.getString("pref_key_qr_id", ""), "0123456789")) {

                    Toast t = Toast.makeText(getApplicationContext(), getString(R.string.invalidId), Toast.LENGTH_LONG);
                    t.show();

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("pref_key_qr_id", "");
                    editor.apply();
                    connectionPref = findPreference(key);
                    connectionPref.setSummary(getString(R.string.settings_summary_customid));

                } else {

                    connectionPref = findPreference(key);
                    connectionPref.setSummary(sharedPreferences.getString(key, ""));

                    progressBar.setVisibility(View.VISIBLE);

                    PreferenceTask t = new PreferenceTask();
                    t.execute();

                }
                break;
            case "pref_key_qr_pw":
                PreferenceTask t = new PreferenceTask();

                progressBar.setVisibility(View.VISIBLE);

                connectionPref = findPreference(key);
                connectionPref.setSummary(getRepl(sharedPreferences.getString(key, "passwort")));

                t.execute();
                break;
            case "pref_key_qr_autofade":
                connectionPref = findPreference("pref_key_qr_autofade_time");
                connectionPref.setEnabled(sharedPreferences.getBoolean(key, false));
                break;
            case "pref_key_notification":
                Preference notifPref = findPreference("pref_key_notification_time");
                notifPref.setEnabled(sharedPreferences.getBoolean(key, false));
                break;
            case "pref_key_notification_time":
                NotificationService.actualize();
                break;

            case "pref_key_level_general":
                new UpdateTaskGrade(this).execute();
                String res = String.valueOf(pref.getInt(key, 0));
                res = res.replace("10", "EF");
                res = res.replace("11", "Q1");
                res = res.replace("12", "Q2");
                if(res.equals("0"))
                    res = "N/A";
                findPreference("pref_key_level_general").setSummary(res);
                break;
            case "pref_key_username_general":

                showProgressBar();

                UpdateTaskName task = new de.slg.startseite.UpdateTaskName(this, currentUsername);
                task.execute();

                break;

        }

    }

    private String getRepl(String s) {

        StringBuilder b = new StringBuilder();

        for(int i = 0; i < s.length(); i++) {

            b.append("*");

        }

        return b.toString();

    }

    private boolean contains(String s, String reg) {

        for(char c : s.toCharArray()) {

            boolean ok = false;
            for(char d : reg.toCharArray()) {

                if(c == d)
                    ok = true;

            }
            if (!ok)
                return false;
        }

        return true;
    }

    private void showSnackbar() {

        final Snackbar cS = Snackbar.make(findViewById(R.id.coords), R.string.snackbar_no_connection_info_check, Snackbar.LENGTH_LONG);
        cS.setActionTextColor(Color.WHITE);
        cS.setAction(getString(R.string.snackbar_no_connection_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cS.dismiss();
            }
        });
        cS.show();

    }

    private void showSnackbar2() {

        final Snackbar cS = Snackbar.make(findViewById(R.id.coords), R.string.snackbar_not_correct_info, Snackbar.LENGTH_LONG);
        cS.setActionTextColor(Color.WHITE);
        cS.setAction(getString(R.string.snackbar_no_connection_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cS.dismiss();
            }
        });
        cS.show();

    }

    private class PreferenceTask extends AsyncTask<Void, Void, Auth> {


        @Override
        protected Auth doInBackground(Void... params) {

            if (hasActiveInternetConnection()) {

                String pw = getPreferenceScreen().getSharedPreferences().getString("pref_key_qr_pw", "");
                try {
                    byte[] contents = pw.getBytes("UTF-8");
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] enc = md.digest(contents);
                    BufferedReader in;
                    String md5 = bytesToHex(enc);
                    Log.d("LeoApp", md5);
                    URL interfaceDB = new URL("http://www.moritz.liegmanns.de/essenqr/qr_checkval.php?id=" + pref.getString("pref_key_qr_id", "00000") + "&auth=RW6SlQ&pw="+md5);
                    Log.d("LeoApp", interfaceDB.toString());
                    in = new BufferedReader(new InputStreamReader(interfaceDB.openStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.contains("true")) {
                            Log.d("LeoApp", "valid");
                            return Auth.VALID;
                        }
                        if(inputLine.contains("false")) {
                            Log.d("LeoApp", "invalid");
                            return Auth.NOT_VALID;
                        }
                    }
                    in.close();

                } catch (NoSuchAlgorithmException | IOException e) {

                    e.printStackTrace();

                }


            } else
                return Auth.NO_CONNECTION;

            return Auth.NOT_VALID;
        }

        public boolean hasActiveInternetConnection() {

            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.lunch.leo-ac.de").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public void onPostExecute(Auth result) {

            progressBar.setVisibility(View.INVISIBLE);

            MainActivity.service = null;

            switch (result) {

                case VALID:
                    setLogin(true);
                    Toast t = Toast.makeText(getApplicationContext(), getString(R.string.login_success), Toast.LENGTH_LONG);
                    t.show();
                    break;
                case NOT_VALID:
                    setLogin(false);
                    showSnackbar2();
                    break;
                case NO_CONNECTION:
                    showSnackbar();

            }
        }
    }

    private void setLogin(boolean b) {

        SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
        editor.putBoolean("pref_key_status_loggedin", b);
        editor.apply();

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void hideProgressBar() {
        findViewById(R.id.progressBar2).setVisibility(View.GONE);
    }

    public void showProgressBar() {
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
    }

    public static void setCurrentUsername(String newName) {

        currentUsername = newName;

    }

    public View getCoordinatorLayout() {

        return findViewById(R.id.coords);

    }

}
