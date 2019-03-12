package com.radarapp.mjr9r.radar.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.model.Filter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    Fragment caller;
    private MapsActivity mainActivity;
    public Fragment getCaller() {
        return caller;
    }
    public void setCaller(Fragment caller) {
        this.caller = caller;
    }

    //UI ELEMENTS
    private Switch qd_switch;
    private Spinner qd_category_spinner;
    private EditText qd_distance;
    private EditText qd_duration;
    private TextView qd_category_txt;
    private TextView qd_distance_txt;
    private TextView qd_duration_txt;
    private TextView qd_distance_suffix;
    private TextView qd_duration_suffix;

    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mainActivity = (MapsActivity) this.getActivity();

        //LOAD SHARED PREFERENCES
        sharedPreferences = mainActivity.getSharedPref();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actiobar_settings, menu);
        mainActivity.getSupportActionBar().setTitle(R.string.actionbar_title_settings);
        mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mainActivity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //Assign Variables to Views in Layout
        View qd_cardview = view.findViewById(R.id.settings_qd_cardview);
        qd_switch = qd_cardview.findViewById(R.id.settings_qd_switch);
        qd_category_spinner = qd_cardview.findViewById(R.id.settings_qd_categ_spinner);
        qd_distance = qd_cardview.findViewById(R.id.settings_qd_distance_edit);
        qd_duration = qd_cardview.findViewById(R.id.settings_qd_duration_edit);
        qd_category_txt = qd_cardview.findViewById(R.id.settings_qd_categ_text);
        qd_distance_txt = qd_cardview.findViewById(R.id.settings_qd_distance_text);
        qd_duration_txt = qd_cardview.findViewById(R.id.settings_qd_duration_text);
        qd_distance_suffix = qd_cardview.findViewById(R.id.settings_qd_distance_suffix);
        qd_duration_suffix = qd_cardview.findViewById(R.id.settings_qd_duration_suffix);

        //INITIALIZE SWITCH & SET UP LISTENER
        // TODO: 02.03.2019 NEXT LINE HAS TO BE SHARED PREFERENCES CHECK

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean(getString(R.string.shared_prefs_qd), true)) {
            Log.v("SHAREDPREFS", Boolean.toString(sharedPreferences.getBoolean(getString(R.string.shared_prefs_qd), true)));
            qd_switch.setChecked(true);
            this.setSubsettingsStatus(true);
        }
        else {
            Log.v("SHAREDPREFS", Boolean.toString(sharedPreferences.getBoolean(getString(R.string.shared_prefs_qd), true)));
            qd_switch.setChecked(false);
            this.setSubsettingsStatus(false);
        }

        qd_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    setSubsettingsStatus(true);
                    editor.putBoolean(getString(R.string.shared_prefs_qd), true);
                    editor.commit();

                }
                else {
                    setSubsettingsStatus(false);
                    editor.putBoolean(getString(R.string.shared_prefs_qd), false);
                    editor.commit();
                }
            }
        });


        //INITIALIZE SPINNER
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.filters, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qd_category_spinner.setAdapter(adapter);
        qd_category_spinner.setSelection(Filter.valueOf(sharedPreferences.getString(getString(R.string.shared_prefs_qd_category), getString(R.string.label_cute))).ordinal());
        qd_category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                editor.putString(getString(R.string.shared_prefs_qd_category), (String) qd_category_spinner.getItemAtPosition(pos));
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //INITIALIZE DISTANCE AND DURATION
        qd_distance.setText(sharedPreferences.getString(getString(R.string.shared_prefs_qd_distance), "1000"));
        qd_duration.setText(sharedPreferences.getString(getString(R.string.shared_prefs_qd_duration), "60"));
        qd_duration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(getString(R.string.shared_prefs_qd_duration), editable.toString());
                editor.commit();
            }
        });

        qd_distance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(getString(R.string.shared_prefs_qd_distance), editable.toString());
                editor.commit();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v("HOMEBUTTON", "BUTTON PRESSED");
                mainActivity.closeSettings(getCaller());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSubsettingsStatus(Boolean status) {
        if(status) {
            qd_category_spinner.setEnabled(true);
            qd_distance.setEnabled(true);
            qd_duration.setEnabled(true);
            qd_category_txt.setTextColor(getResources().getColor(R.color.activeText));
            qd_distance_txt.setTextColor(getResources().getColor(R.color.activeText));
            qd_duration_txt.setTextColor(getResources().getColor(R.color.activeText));
            qd_distance_suffix.setTextColor(getResources().getColor(R.color.smallText));
            qd_duration_suffix.setTextColor(getResources().getColor(R.color.smallText));
        }
        else {
            qd_category_spinner.setEnabled(false);
            qd_distance.setEnabled(false);
            qd_duration.setEnabled(false);
            qd_category_txt.setTextColor(getResources().getColor(R.color.inactiveText));
            qd_distance_txt.setTextColor(getResources().getColor(R.color.inactiveText));
            qd_duration_txt.setTextColor(getResources().getColor(R.color.inactiveText));
            qd_distance_suffix.setTextColor(getResources().getColor(R.color.inactiveText));
            qd_duration_suffix.setTextColor(getResources().getColor(R.color.inactiveText));
        }
    }

}
