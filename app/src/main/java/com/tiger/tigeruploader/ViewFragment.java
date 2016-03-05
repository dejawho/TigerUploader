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
                rootView = inflater.inflate(R.layout.upload, container, false);
            } else if (index == 1){
                rootView = inflater.inflate(R.layout.settings, container, false);
            } else if (index == 2){
                rootView = inflater.inflate(R.layout.information, container, false);
            }
            return rootView;
        }
    }