package com.mllilek.foodexpiry;

public class MathHelper {
    public static double range(double factor, double min, double max) {
        return factor * (max - min) + min;
    }
}
