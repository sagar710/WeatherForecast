package com.sagar.sunshineweather.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.adapters.CityRecyclerAdapter;
import com.sagar.sunshineweather.helper.RecyclerItemClickListener;
import com.sagar.sunshineweather.helper.WrapContentLinearLayoutManager;
import com.sagar.sunshineweather.interfaces.IConstants;

import java.util.ArrayList;

public class AddCitiesActivity extends AppCompatActivity implements IConstants {
    private AppCompatEditText mEditTextAddCity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CityRecyclerAdapter mAdapter;
    private AppCompatButton mBtnAddCity;

    private ArrayList<String> mListCities;

    private String mCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_cities);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            fnInit();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception AddCitiesActivity onCreate: " + Log.getStackTraceString(e));
        }
    }

    private void fnInit() {
        try {
            mEditTextAddCity = (AppCompatEditText) findViewById(R.id.add_cities_activity_edit_text_add_city);
            mBtnAddCity = (AppCompatButton) findViewById(R.id.add_cities_activity_btn_add_city);
            mRecyclerView = (RecyclerView) findViewById(R.id.add_cities_activity_recycler_view);

            mListCities = new ArrayList<>();
            mAdapter = new CityRecyclerAdapter(mListCities, this);

            mRecyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), mRecyclerView,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(AddCitiesActivity.this, CityWeatherActivity.class);
                            intent.putExtra("CITY_NAME", mListCities.get(position));
                            startActivity(intent);
                        }

                        @Override
                        public void onItemLongClick(View view, final int position) {
                            AlertDialog.Builder builder = new AlertDialog.Builder( AddCitiesActivity.this )
                                    .setTitle( "Warning" )
                                    .setMessage( "Remove " +mListCities.get( position) + " ? " )
                                    .setCancelable(false)
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mListCities.remove( position );
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                            builder.show();
                        }
                    })
            );
            mBtnAddCity.setOnClickListener(new BtnAddCityListener());
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception AddCitiesActivity fnInit: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            Log.d(LOG_TAG, "OnResume");
            mAdapter.notifyDataSetChanged();
//            mAdapter.notifyItemInserted( mListCities.size() - 1 );
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception AddCitiesActivity onResume: " + Log.getStackTraceString(e));
        }
    }

    private void fnShowData() {
        try {
            if (fnCheckCityExists()) {
                fnShowAlertMessage(mCityName + " already exists.");
            } else {
                mListCities.add(mCityName);
                Log.d(LOG_TAG, "Item inserted at: " + (mListCities.size() - 1));
                mAdapter.notifyDataSetChanged();
//                mAdapter.notifyItemInserted( mListCities.size() - 1 );
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception AddCitiesActivity fnShowData: " + Log.getStackTraceString(e));
        }
    }

    private boolean fnCheckCityExists() {
        for (String city : mListCities) {
            Log.d(LOG_TAG, "City name:" + city + ", City to check: " + mCityName);
            if (city.equals(mCityName)) {
                return true;
            }
        }
        return false;
    }

    private void fnShowAlertMessage(String messageToShow) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(messageToShow)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
            builder.show();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception AddCitiesActivity fnShowData: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_cities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                onBackPressed();
                return true;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception CityWeather onOptionsItemSelected: " + Log.getStackTraceString(e));
        }
        return super.onOptionsItemSelected(item);
    }

    private class BtnAddCityListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                String cityName[];
                mCityName = mEditTextAddCity.getText().toString();
                mCityName = mCityName.trim();

                if (mCityName.trim().isEmpty()) {
                    fnShowAlertMessage("City name cannot be empty.");
                } else {
                    cityName = mCityName.split(" ");
                    Log.d(LOG_TAG, "City name modified: " + cityName[0]);

                    mCityName = cityName[0];
                    fnShowData();
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception AddCitiesActivity BtnOnClick: " + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent( this, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent );
        finish();
    }
}