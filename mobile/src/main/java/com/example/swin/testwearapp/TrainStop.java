package com.example.swin.testwearapp;

/**
 * Created by swin on 15/11/2016.
 */

public class TrainStop
{
    private String stop_name;
    private String stop_id;
    private double stop_longitude;
    private double stop_latitude;

    public TrainStop(String name, String id, Double lng, Double lat)
    {
        this.stop_id = id;
        this.stop_name = name;
        this.stop_longitude = lng;
        this.stop_latitude = lat;
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getStop_id() {
        return stop_id;
    }

    public double getStop_longitude() {
        return stop_longitude;
    }

    public double getStop_latitude() {
        return stop_latitude;
    }
}
