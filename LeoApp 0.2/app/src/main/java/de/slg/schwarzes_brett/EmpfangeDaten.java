package de.slg.schwarzes_brett;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class EmpfangeDaten extends AsyncTask<Void, Void, Void> {

    private Context c;
    private String[] res;

    public EmpfangeDaten(Context c) {
        this.c = c;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            SQLiteConnector db = new SQLiteConnector(c);
            SQLiteDatabase dbh = db.getWritableDatabase();
            dbh.execSQL("DELETE FROM " + SQLiteConnector.tableResult.tableName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://moritz.liegmanns.de/schwarzesBrett/meldungen.php").openConnection().getInputStream(), "UTF-8"));
            String erg = "";
            String l;
            while ((l = reader.readLine()) != null)
                erg += l;
            String[] result = erg.split("_next_");
            Log.e("Tag", erg);
            for (String s : result) {
                res = s.split(";");
                dbh.execSQL("INSERT INTO " + SQLiteConnector.tableResult.tableName + " Values(null, '" + res[0] + "','" + res[1] + "','" + res[2] + "','" + res[3] + "000" + "', '" + res[4] + "000" + "')");
            }
            reader.close();
            db.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

