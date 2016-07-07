package com.sagar.sunshineweather.helper;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.sagar.sunshineweather.interfaces.IConstants;

public class WrapContentLinearLayoutManager extends LinearLayoutManager implements IConstants {

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    //... constructor
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.d( LOG_TAG, "meet a IOOBE in RecyclerView" );
        }
    }
}