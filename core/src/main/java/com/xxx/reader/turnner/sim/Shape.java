package com.xxx.reader.turnner.sim;


public class Shape {
    public static final int BOUNDARY = 960;

    public boolean isLandscape;
    public int width;
    public int height;

    public Shape(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Shape(boolean isLandscape, int width, int height) {
        this.isLandscape = isLandscape;
        this.width = width;
        this.height = height;
    }

    public int getCalculateWidth() {
        return isLandscape ? width >> 1 : width;
    }

    public double getDiagonal() {
        return Math.hypot(width, height);
    }


    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o != null && o instanceof Shape) {
            Shape s = (Shape) o;
            result = s.width == width && s.height == height;
        }

        return result;
    }


}