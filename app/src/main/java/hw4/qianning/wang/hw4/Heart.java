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
    Purpose: This class is used to implement the shape of heart.
 */
public class Heart extends Shape {
    private int strokeWidth;
    private final int fillColor;
    private ColorStateList strokeColor;
    private Path path;
    private Paint strokePaint;
    private Paint fillPaint;

    public Heart(Context context, boolean isBlurring) {
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
        this.fillPaint.setColor(ContextCompat.getColor(context,isBlurring ? R.color.heartColorBlurring : R.color.heartColor));
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
        int diameter = (int)width;
        path = new Path();
        path.moveTo(diameter / 2, diameter / 5);
        path.quadTo(diameter, 0, diameter / 2, diameter / 1.0f);
        path.quadTo(0, 0, diameter / 2, diameter / 5);
        path.close();
    }
}
