/** This file is part of TigerUploader, located at
 * https://github.com/dejawho/TigerUploader

 TigerUploader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Foobar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Foobar.  If not, see <http://www.gnu.org/licenses/>.**/
package com.tiger.tigeruploader;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public  class ViewFragment extends Fragment {

        public static final String ARG_ENTRY_NUMBER = "selection_number";

        public class SettingsChangedListener implements TextWatcher {

            private View checkedWidget;

            public SettingsChangedListener(View checkedWidget){
                this.checkedWidget = checkedWidget;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity activity = (MainActivity) getActivity();
                if (checkedWidget.getId() == R.id.baseURL){
                    activity.getPreferenceManager().storeValue(URLResolver.BASE_URL_PREF_KEY, s.toString());
                } else if (checkedWidget.getId() == R.id.uploadPage){
                    activity.getPreferenceManager().storeValue(URLResolver.UPLOAD_PAGE_PREF_KEY, s.toString());
                }  else if (checkedWidget.getId() == R.id.galleryPage){
                    activity.getPreferenceManager().storeValue(URLResolver.GALLERY_PAGE_PREF_KEY, s.toString());
                } else if (checkedWidget.getId() == R.id.sizePage){
                    activity.getPreferenceManager().storeValue(URLResolver.SIZE_PAGE_PREF_KEY, s.toString());
                } else if (checkedWidget.getId() == R.id.httpTimeout){
                    activity.getPreferenceManager().storeValue(URLResolver.HTTP_TIMEOUT_PREF_KEY, s.toString());
                }
            }
        };

        public ViewFragment() {
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int index = getArguments().getInt(ARG_ENTRY_NUMBER);
            ((MainActivity)getActivity()).switchPage(index);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = null;
            int index = getArguments().getInt(ARG_ENTRY_NUMBER);
            if (index == 0) {
                rootView = inflater.inflate(R.layout.upload, container, false);
            } else if (index == 1){
                rootView = inflater.inflate(R.layout.settings, container, false);

                EditText baseURL = (EditText) rootView.findViewById(R.id.baseURL);
                baseURL.addTextChangedListener(new SettingsChangedListener(baseURL));

                EditText uploadPage = (EditText) rootView.findViewById(R.id.uploadPage);
                uploadPage.addTextChangedListener(new SettingsChangedListener(uploadPage));

                EditText galleryPage = (EditText) rootView.findViewById(R.id.galleryPage);
                galleryPage.addTextChangedListener(new SettingsChangedListener(galleryPage));

                EditText sizePage = (EditText) rootView.findViewById(R.id.sizePage);
                sizePage.addTextChangedListener(new SettingsChangedListener(sizePage));

                EditText httpTimeout = (EditText) rootView.findViewById(R.id.httpTimeout);
                httpTimeout.addTextChangedListener(new SettingsChangedListener(httpTimeout));

                CheckBox showPreview = (CheckBox) rootView.findViewById(R.id.showPreview);
                showPreview.addTextChangedListener(new SettingsChangedListener(showPreview));
                showPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.getPreferenceManager().setShowPreview(isChecked);
                    }
                });
            } else if (index == 2){
                rootView = inflater.inflate(R.layout.information, container, false);
            }
            return rootView;
        }
    }