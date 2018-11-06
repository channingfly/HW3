package hw4.qianning.wang.hw4;

/*
    Author: Qianning Wang
    Purpose: This class is used to store the position of matching pieces.
 */
public class MyPoint {
    int x;
    int y;

    public MyPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MyPoint)){
            return false;
        } else {
            MyPoint two = (MyPoint)obj;
            return this.x == two.x && this.y == two.y;
        }
    }
}
