package com.example.lxy.test;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Created by lxy on 2017/5/3.
 */

public class LocationData extends DataSupport implements Parcelable{

    private String latitude;
    private String longitude;
    private String loctype;
    private float accuracy;
    private long timestamp;
    //private int flag1;
    //private int flag2;

    protected LocationData(Parcel in){
        latitude = in.readString();
        longitude = in.readString();
        loctype = in.readString();
        accuracy = in.readFloat();
        timestamp = in.readLong();
        //flag1 = in.readInt();
        //flag2 = in.readInt();
    }

    protected LocationData(){

    }


    public String getLatitude(){
        return latitude;
    }
    public void setLatitude(String latitude){
        this.latitude = latitude;
    }

    public String getLongitude(){
        return longitude;
    }
    public void setLongitude(String  longitude){
        this.longitude = longitude;
    }

    public String getLoctype(){
        return loctype;
    }
    public void setLoctype(String loctype){
        this.loctype = loctype;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getTimestamp(){
        return  timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

 /*
    public int getFlag1(){
        return flag1;
    }
    public void setFlag1(int flag1){
        this.flag1 = flag1;
    }

    public int getFlag2(){
        return flag2;
    }
    public void setFlag2(int flag2){
        this.flag2 = flag2;
    }*/

    public static final Creator<LocationData> CREATOR = new Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(loctype);
        dest.writeFloat(accuracy);
        dest.writeLong(timestamp);
        //dest.writeInt(flag1);
        //dest.writeInt(flag2);
    }
}
