package com.sagar.sunshineweather.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.interfaces.IConstants;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements IConstants {
    private AppCompatSpinner mSpinnerUnits;

    private List<String> mListUnits;

    private String unitSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerUnits = (AppCompatSpinner) findViewById(R.id.settings_activity_spinner_choose_units);
        mListUnits = Arrays.asList(getResources().getStringArray(R.array.pref_units_options));

        fnShowUnitsDropDown();
    }

    /* The below method is used to show the units in the dropdown.. */

    private void fnShowUnitsDropDown() {
        ArrayAdapter <String> adapter = new ArrayAdapter<>( this, R.layout.spinner_items, mListUnits );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerUnits.setAdapter( adapter );

        mSpinnerUnits.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view,
                                        int position, long id ) {
                unitSaved = mListUnits.get( position );
                Log.d(LOG_TAG, "Unit selected: " +mListUnits.get( position ) );
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if ( id == android.R.id.home ) {
                onBackPressed();
                return true;
            } else if ( id == R.id.menu_settings_save ) {
                SharedPreferences sharedPreferences = getSharedPreferences( SHARED_PREFERENCES_WEATHER, MODE_PRIVATE );
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString( UNIT_STORED, unitSaved );
                editor.apply();

                Toast.makeText( this, "Preferences saved", Toast.LENGTH_SHORT ).show();
                onBackPressed();
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception AddCities onOptionsItemSelected: " +Log.getStackTraceString( e ));
        }
        return super.onOptionsItemSelected(item);
    }
}