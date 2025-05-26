package com.example.myapp;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String id, emailAddress, password , type, name, age, Store_name , Started , phone_number, address;

    public User(String id, String emailAddress, String password, String type, String name, String store_name, String started) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.password = password;
        this.type = type;
        this.name = name;
        this.age = age;
        this.Store_name = store_name;
        this.Started = started;
        this.phone_number = phone_number;
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStore_name() {
        return Store_name;
    }

    public void setStore_name(String store_name) {
        Store_name = store_name;
    }

    public String getStarted() {
        return Started;
    }

    public void setStarted(String started) {
        Started = started;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    protected User(Parcel in) {
        // Read all fields from parcel
        id = in.readString();
        emailAddress = in.readString();
        password = in.readString();
        type = in.readString();
        name = in.readString();
        Store_name = in.readString();
        Started = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(emailAddress);
        dest.writeString(password);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(Store_name);
        dest.writeString(Started);





        // Write all other fields...
    }
}
