package com.sagar.sunshineweather.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sagar.sunshineweather.R;
import com.sagar.sunshineweather.interfaces.IConstants;

import java.util.ArrayList;

public class CityRecyclerAdapter extends RecyclerView.Adapter <CityRecyclerAdapter.CityViewHolder> implements IConstants {
    private ArrayList <String> mListCities;
    private Context mContext;

    public CityRecyclerAdapter(ArrayList<String> listCities, Context context ) {
        this.mListCities = listCities;
        this.mContext = context;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.add_city_list_item, parent, false );
        return new CityViewHolder(v);
    }

    @Override
    public void onBindViewHolder( CityViewHolder holder, int position) {
        Log.d( LOG_TAG, "City name to add: " +mListCities.get( position ) +" for position: " +position );
        holder.mTxtViewCity.setText( mListCities.get( position ));
    }

    @Override
    public int getItemCount() {
        Log.d( LOG_TAG, "Size of city list: " +mListCities.size() );
        return mListCities.size();
    }

    public void removeItem( int position ) {
        mListCities.remove( position );
        notifyItemRemoved( position );
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        AppCompatTextView mTxtViewCity;

        public CityViewHolder(View v) {
            super(v);

            mTxtViewCity = (AppCompatTextView) v.findViewById( R.id.add_cities_list_item_text_view );
        }
    }
}