package hw4.qianning.wang.hw4;

/*
    Author: Qianning Wang
    Purpose: This class is used to store the data when landscape-portrait switch.
 */

public class Helper {
    public static Thing[][] saveThingGrid = null;

    public static int score = 0;

    public static void saveThingGrid(Thing[][] things){
        saveThingGrid = things;
    }

    public static void saveScore(int s){
        score = s;
    }
}
