package de.slg.essensqr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import de.slg.leoapp.R;

public class ScanActivity extends Fragment {

    private Button btn;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout rootView = (RelativeLayout) inflater.inflate(
                R.layout.fragment_scan, container, false);

        btn = (Button) rootView.findViewById(R.id.scan_button);

        WrapperQRActivity.scan = btn;

        return rootView;

    }



}
