package com.example.myapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public interface OnIndecatorUpdate extends Parcelable {
    void Update_indecator();

    @Override
    default int describeContents() {
        return 0;
    }

    @Override
    default void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
