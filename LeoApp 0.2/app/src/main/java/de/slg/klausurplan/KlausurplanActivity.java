package de.slg.klausurplan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

import de.slg.essensqr.WrapperQRActivity;
import de.slg.leoapp.List;
import de.slg.leoapp.PreferenceActivity;
import de.slg.leoapp.R;
import de.slg.leoapp.Utils;
import de.slg.messenger.OverviewWrapper;
import de.slg.schwarzes_brett.SchwarzesBrettActivity;
import de.slg.startseite.MainActivity;
import de.slg.stimmungsbarometer.StimmungsbarometerActivity;
import de.slg.stundenplan.WrapperStundenplanActivity;

public class KlausurplanActivity extends AppCompatActivity {

    private ListView lvKlausuren;
    public List<Klausur> klausurList;
    private FloatingActionButton fabAdd;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Snackbar snackbar;
    private boolean confirmDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_klausurplan);

        initList();
        initToolbar();
        initListView();
        initNavigationView();
        initAddButton();
        initSnackbar();

        löscheAlteKlausuren(MainActivity.pref.getInt("pref_key_delete", -1));
        filternNachStufe(Utils.getUserStufe());

        KlausurActivity.klausurplanActivity = this;

        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if (mi.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (mi.getItemId() == R.id.action_load) {
            ladeKlausuren();
        } else if (mi.getItemId() == R.id.action_delete) {
            confirmDelete = true;
            this.löscheAlleKlausuren();
            snackbar.show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.klausurplan, menu);
        return true;
    }

    private void ladeKlausuren() {
        if (Utils.checkNetwork()) {
            try {
                KlausurenImportieren k = new KlausurenImportieren(getApplicationContext());
                k.execute();//Klausuren werden aus dem Internet geladen und
                List<Klausur> result = k.get();
                for (result.toFirst(); result.hasAccess(); result.next())
                    add(result.getContent(), false);//Klausuren werden aus der Ergebnisliste in die Klausurliste gefügt
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.snackbar_no_connection_info, Toast.LENGTH_SHORT).show();
        }
        filternNachStufe(Utils.getUserStufe());
        refresh();
    }

    private void löscheAlleKlausuren() {
        lvKlausuren.setAdapter(new KlausurenAdapter(getApplicationContext(), new List<Klausur>(), -1));
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitle(getString(R.string.title_testplan));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().findItem(R.id.klausurplan).setChecked(true);

        navigationView.getMenu().findItem(R.id.nachhilfe).setEnabled(MainActivity.isVerified());
        navigationView.getMenu().findItem(R.id.messenger).setEnabled(MainActivity.isVerified());
        navigationView.getMenu().findItem(R.id.klausurplan).setEnabled(MainActivity.isVerified());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Intent i = null;
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
                        i = null;
                        break;
                    case R.id.startseite:
                        i = null;
                        break;
                    case R.id.settings:
                        i = new Intent(getApplicationContext(), PreferenceActivity.class);
                        break;
                    default:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                if (i != null)
                    startActivity(i);
                finish();
                return true;
            }
        });
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        username.setText(Utils.getUserName());
        ImageView mood = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        mood.setImageResource(Utils.getCurrentMoodRessource());
    }

    private void initListView() {
        lvKlausuren = (ListView) findViewById(R.id.listViewKlausuren);
        lvKlausuren.setVerticalScrollBarEnabled(true);
        lvKlausuren.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KlausurActivity.currentKlausur = klausurList.getObjectAt(position);
                startActivity(new Intent(getApplicationContext(), KlausurActivity.class));
            }
        });
    }

    private void initSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.snack), getString(R.string.snackbar_gelöscht), Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        snackbar.setAction(getString(R.string.snackbar_undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete = false;
                snackbar.dismiss();
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (confirmDelete) {
                    klausurList = new List<>();
                    writeToFile();
                }
                refresh();
            }
        });
    }

    private void initAddButton() {
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KlausurActivity.currentKlausur = new Klausur("", null, "", "");
                startActivity(new Intent(getApplicationContext(), KlausurActivity.class));
            }
        });
    }

    private void initList() {
        klausurList = new List<>();
        readFromFile();
    }

    public void refresh() {
        writeToFile();
        lvKlausuren.setAdapter(new KlausurenAdapter(getApplicationContext(), klausurList, findeNächsteKlausur()));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lvKlausuren.smoothScrollToPositionFromTop(findeNächsteWoche(), 0);
            }
        }, 50);
    }

    public void add(Klausur k, boolean refresh) {
        if (!klausurList.contains(k)) {
            for (klausurList.toFirst(); klausurList.hasAccess(); klausurList.next())
                if (klausurList.getContent().after(k)) {
                    klausurList.insert(k);
                    if (refresh)
                        refresh();
                    return;
                }
            klausurList.append(k);
            if (refresh)
                refresh();
        }
    } //fügt Klausuren an der richtigen Stelle in die Klausurliste

    private int findeNächsteKlausur() {
        Date heute = new Date();
        int i = 0;
        klausurList.toFirst();
        while (klausurList.hasAccess() && heute.after(klausurList.getContent().datum)) {
            klausurList.next();
            i++;
        }
        return i;
    }

    private int findeNächsteWoche() {
        Date heute = new Date();
        int i = 0;
        klausurList.toFirst();
        while (klausurList.hasAccess() && heute.after(klausurList.getContent().datum)) {
            klausurList.next();
            i++;
        }
        while (klausurList.hasPrevious() && klausurList.getContent().istGleicheWoche(klausurList.getPrevious())) {
            i--;
            klausurList.previous();
        }
        return i;
    }

    public void remove(Klausur k) {
        for (klausurList.toFirst(); klausurList.hasAccess(); klausurList.next())
            if (klausurList.getContent().equals(k)) {
                klausurList.remove();
                refresh();
                break;
            }
    }

    public void löscheAlteKlausuren(int monate) {
        if (monate < 0)
            return;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -monate);

        for (klausurList.toFirst(); klausurList.hasAccess(); klausurList.next()) {
            if (calendar.getTime().after(klausurList.getContent().datum))
                klausurList.remove();
        }
    }

    private void filternNachStufe(String stufe) {
        for (klausurList.toFirst(); klausurList.hasAccess(); ) {
            if (stufe.equals("EF") && (klausurList.getContent().istQ1Klausur() || klausurList.getContent().istQ2Klausur()))
                klausurList.remove();
            else if (stufe.equals("Q1") && (klausurList.getContent().istEFKlausur() || klausurList.getContent().istQ2Klausur()))
                klausurList.remove();
            else if (stufe.equals("Q2") && (klausurList.getContent().istEFKlausur() || klausurList.getContent().istQ1Klausur()))
                klausurList.remove();
            else
                klausurList.next();
        }
    }

    private void readFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(getString(R.string.klausuren_filemane))));
            String input = "";
            String line;
            while ((line = reader.readLine()) != null) {
                input += line + "_";
            }
            reader.close();
            String[] split = input.split("_");
            for (int i = 0; i < split.length; i++) {
                String[] current = split[i].split(";");
                if (current.length == 4) {
                    add(new Klausur(current[0], new Date(Long.parseLong(current[1])), current[2], current[3]), false);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //holt die Klausuren aus der Textdatei

    public void writeToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(getString(R.string.klausuren_filemane), MODE_PRIVATE)));
            for (klausurList.toFirst(); klausurList.hasAccess(); klausurList.next()) {
                writer.write(klausurList.getContent().getWriterString());
                writer.newLine();
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //schreibt die Klausuren in die Textdatei
}