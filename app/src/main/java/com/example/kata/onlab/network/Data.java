package com.example.kata.onlab.network;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kata on 2017. 02. 11..
 */
public class Data extends RealmObject {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey
    public int id;
    public String place;
    public float  longitude;
    public float  latitude;
    public int ownerid;
    public String image;






    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }



    public Data(){}
    public Data(String place){this.place=place;}
}