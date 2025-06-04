package com.example.myapp;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class productobject implements Parcelable {
    private String id;
    private String name;
    private String price;
    private String total_price;
    private String quantity;
    private String Overquantity , date;
    private String category;
    private Bitmap image;

    public productobject(String id, String name, String price, String total_price, String quantity, String overQuantity, Bitmap image, String date, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.total_price = total_price;
        this.quantity = quantity;
        this.Overquantity = overQuantity;
        this.image = image;
        this.date = date;
        this.category = category;
    }


    // Add getter and setter for image


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getOverquantity() {
        return Overquantity;
    }

    public void setOverquantity(String overquantity) {
        Overquantity = overquantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getters (and setters if needed)





    @Override
    public int describeContents() {
        return 0;
    }




    protected productobject(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readString();
        quantity = in.readString();
        category = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(quantity);
        dest.writeString(category);
    }

    public static final Creator<productobject> CREATOR = new Creator<productobject>() {
        @Override
        public productobject createFromParcel(Parcel in) {
            return new productobject(in);
        }

        @Override
        public productobject[] newArray(int size) {
            return new productobject[size];
        }
    };


}
