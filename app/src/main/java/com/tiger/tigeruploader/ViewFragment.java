package com.tiger.tigeruploader;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public  class ViewFragment extends Fragment {

        public static final String ARG_ENTRY_NUMBER = "selection_number";

        public ViewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = null;
            int index = getArguments().getInt(ARG_ENTRY_NUMBER);
            if (index == 0) {
                rootView = inflater.inflate(R.layout.drawer_upload_item, container, false);
            } else if (index == 1){
                rootView = inflater.inflate(R.layout.settings, container, false);
            } else if (index == 2){
                rootView = inflater.inflate(R.layout.information, container, false);
            }
            return rootView;
        }
    }