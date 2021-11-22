package com.example.ooplab3;

import androidx.annotation.NonNull;

public class TimeRecord {
    public Float x;
    public Float y;
    public Float z;

    public Long ms;

    @NonNull
    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", ms=" + ms +
                '}';
    }
}

