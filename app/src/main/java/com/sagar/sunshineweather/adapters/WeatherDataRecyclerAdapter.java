package com.sagar.sunshineweather.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.data.WeatherDetails;
import com.sagar.sunshineweather.interfaces.IConstants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WeatherDataRecyclerAdapter extends RecyclerView.Adapter <WeatherDataRecyclerAdapter.WeatherDataViewHolder>
                implements IConstants {
    private ArrayList <WeatherDetails> mListWeatherData;
    private Context mContext;

    public WeatherDataRecyclerAdapter(ArrayList<WeatherDetails> listWeatherData, Context context) {
        this.mListWeatherData = listWeatherData;
        this.mContext = context;
    }

    @Override
    public WeatherDataRecyclerAdapter.WeatherDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.weather_data_list_item, parent, false );
        return new WeatherDataRecyclerAdapter.WeatherDataViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeatherDataRecyclerAdapter.WeatherDataViewHolder holder, int position) {
        WeatherDetails weatherDetailsObj = mListWeatherData.get( position );

        Picasso.with( mContext )
                .load( BASE_IMAGE_URL+ weatherDetailsObj.getIcon() )
                .into( holder.mImgViewType );
        holder.mTxtViewWeatherDate.setText( weatherDetailsObj.getDate() );
        holder.mTxtViewWeatherType.setText( weatherDetailsObj.getWeatherType() );

        SharedPreferences sharedPreferences = mContext.getSharedPreferences( SHARED_PREFERENCES_WEATHER,
                Context.MODE_PRIVATE );
        String unitSaved = sharedPreferences.getString( UNIT_STORED, "Metric" );

        String tempToShow;
        if( unitSaved.equals( "Metric" )) {
            tempToShow = "Max   " +weatherDetailsObj.getMaxTemp() +"\u00b0";
            holder.mTxtViewMax.setText( tempToShow );
            tempToShow = "Min     " +weatherDetailsObj.getMinTemp() +"\u00b0";
            holder.mTxtViewMin.setText( tempToShow );
        } else {
            tempToShow = "Max   " +weatherDetailsObj.getMaxTemp() +"  F";
            holder.mTxtViewMax.setText( tempToShow );
            tempToShow = "Min   " +weatherDetailsObj.getMinTemp() +"  F";
            holder.mTxtViewMin.setText( tempToShow );
        }
    }

    @Override
    public int getItemCount() {
        return mListWeatherData.size();
    }

    class WeatherDataViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView mTxtViewWeatherDate, mTxtViewWeatherType, mTxtViewMax, mTxtViewMin;
        AppCompatImageView mImgViewType;

        public WeatherDataViewHolder(View v) {
            super(v);

            mImgViewType = (AppCompatImageView) v.findViewById( R.id. weather_data_list_item_image_view );
            mTxtViewWeatherDate = (AppCompatTextView) v.findViewById( R.id. weather_data_list_item_text_view_date );
            mTxtViewWeatherType = (AppCompatTextView) v.findViewById( R.id.weather_data_list_item_text_view_type );
            mTxtViewMax = (AppCompatTextView) v.findViewById( R.id.weather_data_list_item_text_view_max );
            mTxtViewMin = (AppCompatTextView) v.findViewById( R.id.weather_data_list_item_text_view_min );
        }
    }
}