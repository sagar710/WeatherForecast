package com.sagar.sunshineweather.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.adapters.CityWeatherRecyclerAdapter;
import com.sagar.sunshineweather.data.WeatherDetails;
import com.sagar.sunshineweather.helper.RecyclerItemClickListener;
import com.sagar.sunshineweather.interfaces.IConstants;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * The below activity shows weather summary for city entered by the user.
 */

public class CityWeatherActivity extends AppCompatActivity implements IConstants {
    private RecyclerView mRecyclerView;
    private AppCompatTextView mTextViewDate, mTextViewMax, mTextViewMin, mTextViewType, mTextViewCityName;
    private AppCompatImageView mImageView;

    private ArrayList <WeatherDetails> mListWeatherData;
    private Calendar mCalendar;

    private String mCityName, mUnitSaved, mPlaceName;
    private int mHourOfDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_city_weather);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            SharedPreferences sharedPreferences = getSharedPreferences( SHARED_PREFERENCES_WEATHER, MODE_PRIVATE );
            mUnitSaved = sharedPreferences.getString( UNIT_STORED, "Metric" );
            mCityName = getIntent().getExtras().getString( "CITY_NAME" );

            mListWeatherData = new ArrayList<>();
            mCalendar = Calendar.getInstance();
            mRecyclerView = (RecyclerView) findViewById( R.id.city_weather_activity_recycler_view );
            mTextViewDate = (AppCompatTextView) findViewById( R.id.city_weather_activity_text_view_date );
            mTextViewMax = (AppCompatTextView) findViewById( R.id.city_weather_activity_text_view_max );
            mTextViewMin = (AppCompatTextView) findViewById( R.id.city_weather_activity_text_view_min );
            mTextViewType = (AppCompatTextView) findViewById( R.id.city_weather_activity_text_view_type );
            mTextViewCityName = (AppCompatTextView) findViewById( R.id.city_weather_activity_text_view_city_name );
            mImageView = (AppCompatImageView) findViewById( R.id.city_weather_activity_image_view );
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception CityWeather onCreate: " +Log.getStackTraceString( e ));
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            mHourOfDay = mCalendar.get( Calendar.HOUR_OF_DAY );
            if( fnIsConnected() ) {
                new GetWeatherDetailsAsyncTask().execute();
            } else {
                fnShowAlertMessage( "Unable to connect to internet. Please verify your internet connection." );
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception CityWeather onResume: " +Log.getStackTraceString( e ));
        }
    }

     /* Below method checks whether device is connected to the network */

    private boolean fnIsConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void fnShowAlertMessage( String messageToShow ) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder( this )
                    .setTitle( "Error" )
                    .setMessage( messageToShow )
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
            builder.show();
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception AddCitiesActivity fnShowData: " +Log.getStackTraceString( e ));
        }
    }

    private class GetWeatherDetailsAsyncTask extends AsyncTask<Void, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                String weatherURL;

                if ( mUnitSaved.equals( "Metric" )) {
                    weatherURL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" +mCityName.trim()+
                            "&units=metric&cnt=14&appid=696ab700932df75c781cbfc3493adac7";
                } else {
                    weatherURL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" +mCityName.trim()+
                            "&units=imperial&cnt=14&appid=696ab700932df75c781cbfc3493adac7";
                }

                URL url = new URL( weatherURL );

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return forecastJsonStr;
            } catch ( Exception e) {
                Log.d( LOG_TAG, "Error: " +Log.getStackTraceString( e ));
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d( LOG_TAG, "Error closing stream: " +Log.getStackTraceString( e ));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute( String response ) {
            super.onPostExecute( response );

            fnShowWeatherData( response );
        }
    }

    private void fnShowWeatherData( String response ) {
        try {
            getWeatherDataFromJson( response, 14 );

            CityWeatherRecyclerAdapter adapter = new CityWeatherRecyclerAdapter( mListWeatherData, this);

            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter( adapter );

            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getApplicationContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent( CityWeatherActivity.this, WeatherDetailActivity.class );
                            intent.putExtra( "WEATHER_DETAIL_OBJECT", mListWeatherData.get( position ));
                            intent.putExtra( "CITY_NAME", mPlaceName );
                            intent.putExtra( "ACTIVITY_NAME", "City" );
                            startActivity( intent );
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    })
            );
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception CityWeather fnShowWeatherData: " +Log.getStackTraceString( e ));
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr, int numDays) {
        try {
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_CITY = "city";
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            final String OWM_PRESSURE = "pressure";
            final String OWM_HUMIDITY = "humidity";
            final String OWM_WIND = "speed";
            final String OWM_ICON = "id";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject jsonObjectCity = forecastJson.getJSONObject( OWM_CITY );
            mPlaceName = jsonObjectCity.getString( "name" );
            mTextViewCityName.setText( mPlaceName );

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                int id = weatherObject.getInt( OWM_ICON );

                String icon = "01d.png";

                if ( id >= 200 && id <= 232 ) {     // For id between 200 - 232. ( 11d images)
                    icon = "11d.png";
                } else if ( id >= 300 && id <= 321 || id >= 520 && id <= 531 ) {     // For id between 300 - 321 or 520 - 531. ( 09d images)
                    icon = "09d.png";
                } else if( id == 511 || id >= 600 && id <= 622 ) {      // For id 511, 600 - 622. ( 13d images)
                    icon = "13d.png";
                } else if( id >= 701 && id <= 781 ) {      // For id between 701 - 781. ( 50d images)
                    icon = "50d.png";
                } else if( id >= 500 && id <= 504 ) {      // For id between 500 - 504. ( 10d images)
                    icon = "10d.png";
                } else if ( id == 800 ) {       // For id 800 (1d or 1n images)
                    if( mHourOfDay > 5 && mHourOfDay <= 17 ) {
                        icon = "01d.png";
                    } else {
                        icon = "01n.png";
                    }
                } else if( id == 801 ) {        // For id 801 (2d or 2n images)
                    if( mHourOfDay > 5 && mHourOfDay <= 17) {
                        icon = "02d.png";
                    } else {
                        icon = "02n.png";
                    }
                } else if ( id == 802) {        // For id 802 (3d images)
                    icon = "03d.png";
                } else if( id == 803 ) {        // For id 803 (3d or 4d images)
                    if( mHourOfDay > 5 && mHourOfDay <= 17 ) {
                        icon = "04d.png";
                    } else {
                        icon = "03d.png";
                    }
                } else if ( id == 804 ) {        // For id 804 (4d images)
                    icon = "04d.png";
                }

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = Math.round( temperatureObject.getDouble(OWM_MAX));
                double low = Math.round( temperatureObject.getDouble(OWM_MIN));

                int humidity = dayForecast.getInt( OWM_HUMIDITY );
                double pressure = dayForecast.getDouble( OWM_PRESSURE );
                double wind = dayForecast.getDouble( OWM_WIND );

                if( i == 0 ) {
                    mTextViewDate.setText( day );

                    if( mUnitSaved.equals( "Metric" )) {
                        mTextViewMin.setText( low +"\u00b0" );
                        mTextViewMax.setText( high +"\u00b0" );
                    } else {
                        mTextViewMax.setText( "Max    " +high+ " F");
                        mTextViewMin.setText( "Min     " +low+ " F");
                    }
                    mTextViewType.setText( description );
                    Picasso.with( this )
                            .load( BASE_IMAGE_URL+ icon )
                            .into( mImageView );
                } else {
                    mListWeatherData.add( new WeatherDetails( day, description, icon, high, low, pressure, wind, id, humidity ));
                }
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception CityWeather getWeatherDataFromJson: " +Log.getStackTraceString( e ));
        }
    }

    private String getReadableDateString(long time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
        return shortenedDateFormat.format(time);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_city_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if ( id == R.id.menu_city_weather_settings ) {
                startActivity( new Intent( this, SettingsActivity.class ));
                return true;
            } else if ( id == android.R.id.home ) {
                onBackPressed();
                return true;
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception CityWeather onOptionsItemSelected: " +Log.getStackTraceString( e ));
        }
        return super.onOptionsItemSelected(item);
    }
}