package com.example.ooplab3;

public class Result {
    private final int[][] WarpingPath;
    private final double Distance;

    public Result(final int[][] pWarpingPath, final double pDistance) {
        this.WarpingPath = pWarpingPath;
        this.Distance = pDistance;
    }

    public final int[][] getWarpingPath() {return this.WarpingPath;}
    public final double getDistance() {return this.Distance;}
}
