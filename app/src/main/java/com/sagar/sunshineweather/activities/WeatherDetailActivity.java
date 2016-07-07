package com.sagar.sunshineweather.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.data.WeatherDetails;
import com.sagar.sunshineweather.interfaces.IConstants;
import com.squareup.picasso.Picasso;

/**
 *
 * The below activity shows the detailed weather information.
 */

public class WeatherDetailActivity extends AppCompatActivity implements IConstants {
    private AppCompatTextView mTextViewDate, mTextViewMax, mTextViewMin, mTextViewType, mTextViewPressure,
            mTextViewHumidity, mTextViewWind, mTextViewCityName;
    private AppCompatImageView mImageViewType;

    private String mCityName, mActivityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_weather_detail);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            mTextViewDate = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_date);
            mTextViewMax = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_max);
            mTextViewMin = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_min);
            mTextViewType = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_type);
            mTextViewPressure = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_pressure);
            mTextViewHumidity = (AppCompatTextView) findViewById(R.id.weather_detail_activity_text_view_humidity);
            mTextViewCityName = (AppCompatTextView) findViewById( R.id.weather_detail_activity_text_view_city_name );
            mTextViewWind = (AppCompatTextView) findViewById( R.id.weather_detail_activity_text_view_wind );
            mImageViewType = (AppCompatImageView) findViewById(R.id.weather_detail_activity_image_view_type);

            Intent intent = getIntent();
            mCityName = intent.getExtras().getString( "CITY_NAME" );
            mActivityName = intent.getExtras().getString( "ACTIVITY_NAME" );
            WeatherDetails weatherDetailsObj = (WeatherDetails) intent.getSerializableExtra("WEATHER_DETAIL_OBJECT");

            String text;

            mTextViewDate.setText(weatherDetailsObj.getDate());
            mTextViewCityName.setText( mCityName );
            mTextViewType.setText(weatherDetailsObj.getWeatherType());
            text = weatherDetailsObj.getMaxTemp() + "\u00b0";
            mTextViewMax.setText( text );
            text = weatherDetailsObj.getMinTemp() + "\u00b0";
            mTextViewMin.setText( text );
            text = "Pressure: " +weatherDetailsObj.getPressure() +" hPa";
            mTextViewPressure.setText( text );
            text = "Humidity: " +weatherDetailsObj.getHumidity() + " %";
            mTextViewHumidity.setText( text );
            text = "Wind: " +weatherDetailsObj.getWind() + " meter/sec";
            mTextViewWind.setText( text );
            Picasso.with(this)
                    .load(BASE_IMAGE_URL + weatherDetailsObj.getIcon())
                    .into(mImageViewType);

        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception WeatherDetailActivity onCreate: " +Log.getStackTraceString( e ));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_detail, menu);
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
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception WeatherDetailActivity onOptionsItemSelected: " +Log.getStackTraceString( e ));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();

            if( mActivityName.contains( "Main" )) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent );
                finish();
            } else {
                Intent intent = new Intent(this, CityWeatherActivity.class);
                intent.putExtra( "CITY_NAME", mCityName );
                startActivity( intent );
                finish();
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception WeatherDetailActivity onBackPressed: " +Log.getStackTraceString( e ));
        }
    }
}