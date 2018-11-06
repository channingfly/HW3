package hw4.qianning.wang.hw4;

import android.graphics.Rect;

/*
    Author: Qianning Wang
    Purpose: This class is used to record the type of the image.
 */
public class Thing {
    public enum Type {
        Square,  //Square shape
        Circle,  //Circle shape
        Heart, //Heart shape
        Pentagram, //Pentagram shape
        N
    }

    private Type type;
    private Rect bounds;

    public Thing(Type type, Rect bounds) {
        this.type = type;
        this.bounds = bounds;
    }

    public void setType(Thing.Type type){
        this.type = type;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public Rect getBounds() {
        return bounds;
    }

    public Type getType() {
        return type;
    }
}
