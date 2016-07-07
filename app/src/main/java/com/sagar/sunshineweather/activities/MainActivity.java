package com.sagar.sunshineweather.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.helper.RecyclerItemClickListener;
import com.sagar.sunshineweather.adapters.WeatherDataRecyclerAdapter;
import com.sagar.sunshineweather.data.WeatherDetails;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements IConstants, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private RecyclerView mRecyclerView;
    private AppCompatTextView mTextViewDate, mTextViewMax, mTextViewMin, mTextViewType, mTextViewPlaceName;
    private AppCompatImageView mImageView;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private ArrayList <WeatherDetails> mListWeatherData;
    private Calendar mCalendar;

    private String unitSaved, mPlaceName;
    private double latitude, longitude;
    private int mHourOfDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mTextViewDate = (AppCompatTextView) findViewById( R.id.main_activity_text_view_date );
            mTextViewMax = (AppCompatTextView) findViewById( R.id.main_activity_text_view_max );
            mTextViewMin = (AppCompatTextView) findViewById( R.id.main_activity_text_view_min );
            mTextViewType = (AppCompatTextView) findViewById( R.id.main_activity_text_view_type );
            mTextViewPlaceName = (AppCompatTextView) findViewById( R.id.main_activity_text_view_place_name );
            mImageView = (AppCompatImageView) findViewById( R.id.main_activity_image_view );

            mCalendar = Calendar.getInstance();
            SharedPreferences sharedPreferences = getSharedPreferences( SHARED_PREFERENCES_WEATHER, MODE_PRIVATE );
            unitSaved = sharedPreferences.getString( UNIT_STORED, "Metric" );
            Log.d( LOG_TAG, "Unit saved for conversion: " +unitSaved );

            // First we need to check availability of play services
            if (checkPlayServices()) {
                // Building the GoogleApi client
                buildGoogleApiClient();
            }
            mListWeatherData = new ArrayList<>();
            mRecyclerView = (RecyclerView) findViewById( R.id.main_activity_recycler_view );

        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity onCreate: " +Log.getStackTraceString( e ));
        }
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                Log.d( LOG_TAG, "Latitude: " +latitude+ " --> Longitude: " +longitude );
                fnGetAddress();

                new GetWeatherDetailsAsyncTask().execute();
            } else {
                Log.d( LOG_TAG, "Couldn't get location" );
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity displayLocation: " +Log.getStackTraceString( e ));
        }
    }

    /* Below method checks whether device is connected to the network */

    private boolean fnIsConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity buildGoogleApiClient: " +Log.getStackTraceString( e ));
        }
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This device is not supported.", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                return false;
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity checkPlayServices: " +Log.getStackTraceString( e ));
        }
        return true;
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            mHourOfDay = mCalendar.get( Calendar.HOUR_OF_DAY );
            checkPlayServices();
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity onResume: " +Log.getStackTraceString( e ));
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity onStart: " +Log.getStackTraceString( e ));
        }
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d( LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        if( fnIsConnected() ) {
            displayLocation();
        } else {
            fnShowAlertMessage( "Unable to connect to internet. Please verify your internet connection." );
        }
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

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
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

                if( unitSaved.equals( "Metric" )) {
                    weatherURL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=" +latitude+
                            "&lon=" +longitude+ "&units=metric&cnt=14&appid=696ab700932df75c781cbfc3493adac7";
                } else {
                    weatherURL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=" +latitude+
                            "&lon=" +longitude+ "&units=imperial&cnt=14&appid=696ab700932df75c781cbfc3493adac7";
                }

                URL url = new URL( weatherURL );
                Log.d( LOG_TAG, "URL: " +weatherURL );
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
                Log.e( LOG_TAG, "Error: " +Log.getStackTraceString( e ));
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
                        Log.e( LOG_TAG, "Error closing stream: " +Log.getStackTraceString( e ));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute( String response ) {
            super.onPostExecute( response );

            Log.d( LOG_TAG, "SunshineWeather: " +response );
            fnShowWeatherData( response );
        }
    }

    private void fnShowWeatherData( String response ) {
        try {
            getWeatherDataFromJson( response, 14 );

            WeatherDataRecyclerAdapter adapter = new WeatherDataRecyclerAdapter( mListWeatherData, this);

            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter( adapter );

            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getApplicationContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent( MainActivity.this, WeatherDetailActivity.class );
                            intent.putExtra( "WEATHER_DETAIL_OBJECT", mListWeatherData.get( position ));
                            intent.putExtra( "CITY_NAME", mPlaceName );
                            intent.putExtra( "ACTIVITY_NAME", "Main" );
                            startActivity( intent );
                            finish();
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    })
            );
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity fnShowWeatherData: " +Log.getStackTraceString( e ));
        }
    }

    private void fnGetAddress() {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation( latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                Log.d( LOG_TAG, "" + strReturnedAddress.toString());
            } else {
                Log.d( LOG_TAG, "No Address returned!");
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception fnGetAddress:" +Log.getStackTraceString( e ));
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
            JSONObject jsonObjectCity = forecastJson.getJSONObject( OWM_CITY );
            mPlaceName = jsonObjectCity.getString( "name" );
            Log.d( LOG_TAG, "City name: " +mPlaceName );
            mTextViewPlaceName.setText( mPlaceName );
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                int id = weatherObject.getInt( OWM_ICON );

                String icon = "01d.png";
                Log.d( LOG_TAG, "Weather icon: " +id );

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

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                int humidity = dayForecast.getInt( OWM_HUMIDITY );
                double pressure = dayForecast.getDouble( OWM_PRESSURE );
                double wind = dayForecast.getDouble( OWM_WIND );

                highAndLow = formatHighLows(high, low);

                if( i == 0 ) {
                    mTextViewDate.setText( day );

                    if( unitSaved.equals( "Metric" )) {
                        mTextViewMin.setText( low +"\u00b0" );
                        mTextViewMax.setText( high +"\u00b0" );
                    } else {
                        mTextViewMax.setText( "Max    " +high+ " F");
                        mTextViewMin.setText( "Min     " +low+ " F");
                    }
                    mTextViewType.setText( description );
                    Picasso.with( this )
                            .load( BASE_IMAGE_URL +icon )
                            .into( mImageView );
                } else {
                    mListWeatherData.add( new WeatherDetails( day, description, icon, high, low, pressure, wind, id, humidity ));
                }
                Log.d( LOG_TAG, "Type: " +description+ ", Humidity: " +humidity+ ", Pressure: " +pressure+ ", Wind: " +wind
                        + ", Max: " +high+ ", Min: " +low+ ",Day: " +day );
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception parsing JSON: " +Log.getStackTraceString( e ));
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time) {
        try {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
            return shortenedDateFormat.format(time);
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity getReadableDate: " +Log.getStackTraceString( e ));
        }
        return "";
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.

        try {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity formatHighLow: " +Log.getStackTraceString( e ));
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if ( id == R.id.settings ) {
                startActivity( new Intent( this, SettingsActivity.class ));
                return true;
            } else if ( id == R.id.add_cities ) {
                startActivity( new Intent( this, AddCitiesActivity.class ));
                finish();
            }
        } catch ( Exception e ) {
            Log.d( LOG_TAG, "Exception mainActivity onOptionsItemSelected: " +Log.getStackTraceString( e ));
        }
        return super.onOptionsItemSelected(item);
    }
}