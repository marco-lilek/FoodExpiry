package com.mllilek.foodexpiry;

public class MathHelper {
    static double range(double factor, double min, double max) {
        return factor * (max - min) + min;
    }

    static int range(int factor, int min, int max) {
        return factor * (max - min) + min;
    }
}
