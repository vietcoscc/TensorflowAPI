package com.example.demo.model;

public class RectF {
    public float left;
    public float top;
    public float right;
    public float bottom;

    public RectF(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public RectF(RectF location) {
        this.left = location.left;
        this.right = location.right;
        this.top = location.top;
        this.bottom = location.bottom;
    }

    public String toString() {
        return "RectF[" + left + ", " + top + ", "
                + right + ", " + bottom + "]";
    }

}
