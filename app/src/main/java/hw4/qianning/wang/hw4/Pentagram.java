package hw4.qianning.wang.hw4;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;
import android.support.v4.content.ContextCompat;

/*
    Author: Qianning Wang
    Purpose: This class is used to implement the shape of pentagram.
 */
public class Pentagram extends Shape {
    private int strokeWidth;
    private final int fillColor;
    private ColorStateList strokeColor;
    private Path path;
    private Paint strokePaint;
    private Paint fillPaint;

    public Pentagram(Context context, boolean isBlurring) {
        this.strokeWidth = 10;
        this.fillColor = Color.GREEN;
        ColorStateList strokeColor = context.getResources().getColorStateList(isBlurring ? R.color.stroke_blurring : R.color.stroke);
        this.strokeColor = strokeColor;
        this.strokePaint = new Paint();
        this.strokePaint.setStyle(Paint.Style.STROKE);
        this.strokePaint.setColor(strokeColor.getColorForState(new int[0], 0));
        this.strokePaint.setStrokeJoin(Paint.Join.ROUND);
        this.strokePaint.setStrokeWidth(strokeWidth);

        this.fillPaint = new Paint();
        this.fillPaint.setStyle(Paint.Style.FILL);
        this.fillPaint.setColor(ContextCompat.getColor(context,isBlurring ? R.color.pentagramColorBlurring : R.color.pentagramColor));
    }

    public void setState(int[] stateList) {
        this.strokePaint.setColor(strokeColor.getColorForState(stateList, 0));
    }
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, fillPaint);
        canvas.drawPath(path, strokePaint);
    }

    @Override
    protected void onResize(float width, float height) {
        super.onResize(width, height);
        int radius = (int)width / 2;
        float radian = degree2Radian(36);// 36 degree is the angle of the pentagram
        float radius_in = (float) (radius * Math.sin(radian / 2) / Math
                .cos(radian)); // Radius of the middle pentagon
        path = new Path();
        path.moveTo((float) (radius * Math.cos(radian / 2)), 0);// This point is the starting point of the polygon
        path.lineTo((float) (radius * Math.cos(radian / 2) + radius_in
                        * Math.sin(radian)),
                (float) (radius - radius * Math.sin(radian / 2)));
        path.lineTo((float) (radius * Math.cos(radian / 2) * 2),
                (float) (radius - radius * Math.sin(radian / 2)));
        path.lineTo((float) (radius * Math.cos(radian / 2) + radius_in
                        * Math.cos(radian / 2)),
                (float) (radius + radius_in * Math.sin(radian / 2)));
        path.lineTo(
                (float) (radius * Math.cos(radian / 2) + radius
                        * Math.sin(radian)), (float) (radius + radius
                        * Math.cos(radian)));
        path.lineTo((float) (radius * Math.cos(radian / 2)),
                (float) (radius + radius_in));
        path.lineTo(
                (float) (radius * Math.cos(radian / 2) - radius
                        * Math.sin(radian)), (float) (radius + radius
                        * Math.cos(radian)));
        path.lineTo((float) (radius * Math.cos(radian / 2) - radius_in
                        * Math.cos(radian / 2)),
                (float) (radius + radius_in * Math.sin(radian / 2)));
        path.lineTo(0, (float) (radius - radius * Math.sin(radian / 2)));
        path.lineTo((float) (radius * Math.cos(radian / 2) - radius_in
                        * Math.sin(radian)),
                (float) (radius - radius * Math.sin(radian / 2)));
        path.close();
    }
    /**
     * Angle arcing formula
     *
     * @param degree
     * @return
     */
    private float degree2Radian(int degree) {
        return (float) (Math.PI * degree / 180);
    }
}
