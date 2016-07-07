package com.sagar.sunshineweather.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.data.WeatherDetails;
import com.sagar.sunshineweather.interfaces.IConstants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CityWeatherRecyclerAdapter extends RecyclerView.Adapter <CityWeatherRecyclerAdapter.CityWeatherViewHolder>
                implements IConstants {
    private ArrayList<WeatherDetails> mListWeatherData;
    private Context mContext;

    public CityWeatherRecyclerAdapter(ArrayList<WeatherDetails> listWeatherData, Context context) {
        this.mListWeatherData = listWeatherData;
        this.mContext = context;
    }

    @Override
    public CityWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.city_weather_list_item, parent, false );
        return new CityWeatherViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CityWeatherViewHolder holder, int position) {
        WeatherDetails weatherDetailsObj = mListWeatherData.get( position );

        Log.d( LOG_TAG, "Position: " +position+ ", Type: " +BASE_IMAGE_URL+ weatherDetailsObj.getIcon() );
        Picasso.with( mContext )
                .load( BASE_IMAGE_URL+ weatherDetailsObj.getIcon() )
                .into( holder.mImgViewType );
        holder.mTxtViewWeatherDate.setText( weatherDetailsObj.getDate() );
        holder.mTxtViewWeatherType.setText( weatherDetailsObj.getWeatherType() );
        holder.mTxtViewMax.setText( weatherDetailsObj.getMaxTemp() +"\u00b0" );
        holder.mTxtViewMin.setText( weatherDetailsObj.getMinTemp() +"\u00b0" );
    }

    @Override
    public int getItemCount() {
        return mListWeatherData.size();
    }

    class CityWeatherViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        AppCompatTextView mTxtViewWeatherDate, mTxtViewWeatherType, mTxtViewMax, mTxtViewMin;
        AppCompatImageView mImgViewType;

        public CityWeatherViewHolder(View v) {
            super(v);

            mImgViewType = (AppCompatImageView) v.findViewById( R.id. city_weather_data_list_item_image_view );
            mTxtViewWeatherDate = (AppCompatTextView) v.findViewById( R.id.city_weather_data_list_item_text_view_date );
            mTxtViewWeatherType = (AppCompatTextView) v.findViewById( R.id.city_weather_data_list_item_text_view_type );
            mTxtViewMax = (AppCompatTextView) v.findViewById( R.id.city_weather_data_list_item_text_view_max );
            mTxtViewMin = (AppCompatTextView) v.findViewById( R.id.city_weather_data_list_item_text_view_min );
        }
    }
}
