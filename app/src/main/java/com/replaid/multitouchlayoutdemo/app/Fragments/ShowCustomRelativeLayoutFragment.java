package com.replaid.multitouchlayoutdemo.app.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.replaid.multitouchlayoutdemo.app.R;

public class ShowCustomRelativeLayoutFragment extends Fragment {
    private static final String TAG = "ShowCustomRelativeLayoutFragment";

    public static ShowCustomRelativeLayoutFragment newInstance() {
        Log.i(TAG, "Creating fragment");
        ShowCustomRelativeLayoutFragment fragment
                = new ShowCustomRelativeLayoutFragment();
        return  fragment;
    }

    public ShowCustomRelativeLayoutFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customrelativelayout, container, false);
        return view;
    }
}
