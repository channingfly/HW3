package hw4.qianning.wang.hw4;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/*
    Author: Qianning Wang
    Purpose: This class is used to implement the logic of the game panel.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int SHAPE_HEART = 0;
    public static final int SHAPE_PENTAGRAM = 1;
    public static final int SHAPE_SQUARE = 2;
    public static final int SHAPE_CIRCLE = 3;

    private static final int[] selectedState = {android.R.attr.state_selected};
    private static final int[] unselectedState = {};

    private Timer timer;

    private SurfaceHolder holder;

    private int screenWidth;
    private int screenHeight;

    private float gameWidth;
    private float unitWidth;

    private float gamePanelLeft;
    private float gamePanelTop;
    private float gamePanelRight;
    private float gamePanelBottom;

    private float touchX;
    private float touchY;

    private float offsetX;
    private float offsetY;

    private Rect selectRect;

    private Context context;

    private Thing[][] thingGrid = new Thing[8][8];

    private Random random = new Random();
    private int selectedColPosition;
    private int selectedRowPosition;
    private Thing selectedThing;

    private boolean blink = false;

    private boolean blurring = false;

    private static final float movePercent = 0.7f;  //The reference is used to determine the range of movement

    private int orientation;

    private boolean isDraw;
    private boolean isPause;
    private Canvas canvas;

    private boolean isLock = false;

    private int score = 0;
    private List<MyPoint> removeList;

    private float touchSLop = 0;
    private float distanceX;
    private float distanceY;
    private int dX;
    private int dY;
    private boolean action = false;

    int moveType = 0;
    private final float px;

    private Thing[][] initGridThing;
    public GamePanel(Context context, int screenOrientation,Thing[][] initGridThing) {
        super(context);
        this.context = context;
        this.orientation = screenOrientation;
        this.initGridThing = initGridThing;
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
        removeList = new ArrayList<>();
        touchSLop = context.getResources().getDimension(R.dimen.touch_slop);
        float dip = 1;
        Resources r = getResources();
        px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }
    public Thing[][] getThingGrid() {
        return thingGrid;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        timer = new Timer();
        timer.schedule(new DrawTask(), 0, 25);
        if (calculateData() > 0) {
            new Thread(blurringer).start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        timer.cancel();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = getWidth();
        int height = getHeight();
        initParam(width, height);
        initGrid(false);
    }

        //  Initialize the parameter
    public void initParam(int width, int height) {
        screenHeight = height;
        screenWidth = width;

        //  The length of the grid is the 9/10 of the width
        //  Draw the point
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gameWidth = height * 9 / 10;
            unitWidth = gameWidth / 8;
            //  Get left-top and right-down point in landscape state
            gamePanelLeft = width / 2 - gameWidth / 2;
            gamePanelTop = height / 10 * 0.5f;
            gamePanelRight = width / 2 + gameWidth / 2;
            gamePanelBottom = height / 10 * 9.5f;
        } else {
            gameWidth = width * 9 / 10;
            unitWidth = gameWidth / 8;
            //  Get the left-top and right-down point in portrait state
            gamePanelLeft = width / 10 * 0.5f;
            gamePanelTop = height / 2 - gameWidth / 2;
            gamePanelRight = width / 10 * 9.5f;
            gamePanelBottom = height / 2 + gameWidth / 2;
        }
    }
        //  Initialize the gamepanel and add content to the grid cell
    public void initGrid(boolean isRefresh) {
        if(initGridThing == null || isRefresh) {
            for (int i = 0; i < thingGrid.length; i++) {
                for (int j = 0; j < thingGrid[i].length; j++) {
                    thingGrid[i][j] = createNewThing(i, j);
                }
            }
        } else {
            for (int i = 0; i < thingGrid.length; i++) {
                for (int j = 0; j < thingGrid[i].length; j++) {
                    thingGrid[i][j] = createNewThing(i, j,initGridThing[i][j].getType());
                }
            }
            score = Helper.score;
        }
        if (calculateData() > 0) {
            new Thread(blurringer).start();
        }
    }

    public Thing createNewThing(int i, int j) {
        Rect rect = calculateActualRect(gamePanelLeft + j * unitWidth, gamePanelTop + i * unitWidth, unitWidth);
        return new Thing(createNewType(), rect);
    }

    public Thing createNewThing(int i, int j,Thing.Type type) {
        Rect rect = calculateActualRect(gamePanelLeft + j * unitWidth, gamePanelTop + i * unitWidth, unitWidth);
        return new Thing(type, rect);
    }

    public Thing.Type createNewType() {
        int randomType = random.nextInt(4);
        Thing.Type type;
        switch (randomType) {
            case SHAPE_HEART:
                type = Thing.Type.Heart;
                break;
            case SHAPE_PENTAGRAM:
                type = Thing.Type.Pentagram;
                break;
            case SHAPE_SQUARE:
                type = Thing.Type.Square;
                break;
            case SHAPE_CIRCLE:
                type = Thing.Type.Circle;
                break;
            default:
                type = Thing.Type.N;
        }
        return type;
    }

    //  Draw the grid
    public void drawGamePane(Canvas canvas, Paint paint) {
        canvas.drawRect(gamePanelLeft, gamePanelTop, gamePanelRight, gamePanelBottom, paint);
        for (int i = 1; i <= 7; i++) {
            canvas.drawLine(gamePanelLeft, gamePanelTop + unitWidth * i, gamePanelRight, gamePanelTop + unitWidth * i, paint);
            canvas.drawLine(gamePanelLeft + unitWidth * i, gamePanelTop, gamePanelLeft + unitWidth * i, gamePanelBottom, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isPause) {
                    return true;
                }

                touchX = event.getX();
                touchY = event.getY();

                //  Calculate the selected position first
                //  Mark it as black before it moves
                float distanceInitX = touchX - gamePanelLeft;
                float distanceInitY = touchY - gamePanelTop;
                if (distanceInitX > 0 && distanceInitY > 0) {
                    //  Column position
                    selectedColPosition = (int) ((distanceInitX) / unitWidth);
                    //Row position
                    selectedRowPosition = (int) ((distanceInitY) / unitWidth);


                    Log.e("position", "Column position:" + selectedColPosition + "Row position:" + selectedRowPosition);
                    if (selectedColPosition >= 0 && selectedRowPosition >= 0 && selectedColPosition < 8 && selectedRowPosition < 8 && thingGrid[selectedRowPosition][selectedColPosition].getType() != Thing.Type.N) {
                        int left = (int) (gamePanelLeft + selectedColPosition * unitWidth);
                        int top = (int) (gamePanelTop + selectedRowPosition * unitWidth);
                        int right = (int) (left + unitWidth);
                        int bottom = (int) (top + unitWidth);
                        selectRect = new Rect(left, top, right, bottom);
                        new Thread(blinker).start();
                        selectedThing = new Thing(thingGrid[selectedRowPosition][selectedColPosition].getType(), thingGrid[selectedRowPosition][selectedColPosition].getBounds());
                    } else {
                        //  Outside the gamepanel, right and down
                        clickOut();
                    }
                } else {
                    //  Outside the gamepanel, left and top
                    clickOut();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("move", "x:" + event.getX() + "--y:" + event.getY());
                if (isPause) {
                    return true;
                }
                float moveX = event.getX();
                float moveY = event.getY();
                distanceX = moveX - touchX;
                distanceY = moveY - touchY;
                if (!isLock && isInGrid(touchX, touchY)) {
                    //  Move
                    if (Math.abs(distanceX) > unitWidth * movePercent || Math.abs(distanceY) > unitWidth * movePercent) {
                        isLock = true;
                        if (Math.abs(distanceX) > Math.abs(distanceY)) {
                            //  Lateral exchange
                            int move = distanceX > 0 ? 1 : -1;
                            dX = selectedRowPosition;
                            dY = selectedColPosition + move;
                            moveType = distanceX > 0 ? 2 : 1;
                        } else {
                            //  Vertical exchange
                            int move = distanceY > 0 ? 1 : -1;
                            dX = selectedRowPosition + move;
                            dY = selectedColPosition;
                            moveType = distanceY > 0 ? 4 : 3;
                        }

                        if (dX < 0 || dY < 0 || dX >= 8 || dY >= 8) {
                            isLock = false;
                            return true;
                        }
                        new Thread(MoveAction).start();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isInGrid(touchX, touchY) && (Math.abs(distanceX) > touchSLop || Math.abs(distanceY) > touchSLop)) {
                    score -= 10;
                    initGrid(true);
                } else {
                    pause();
                }
                distanceX=-1;
                distanceY=-1;
                break;
        }
        performClick();
        return true;
    }

    public boolean isInGrid(float touchX, float touchY) {
        return touchX > gamePanelLeft && touchY > gamePanelTop && touchX < gamePanelRight && touchY < gamePanelBottom;
    }

    public void resetToSelected() {
        if (selectedColPosition >= 0 && selectedRowPosition >= 0) {
            thingGrid[selectedRowPosition][selectedColPosition].setBounds(selectedThing.getBounds());
        }
    }

    //  This method is used to exchange the content of the grid cell
    public void switchTwoUnitThing(int oneX, int oneY, int twoX, int twoY) {

        //  Point exchange
        Thing tempThing = thingGrid[oneX][oneY];
        thingGrid[oneX][oneY] = thingGrid[twoX][twoY];
        thingGrid[twoX][twoY] = tempThing;
    }

    //  This method is used to exchange the content of the grid cell and the position of the image
    public void switchTwoUnitContent(int oneX, int oneY, int twoX, int twoY) {
        Thing t1 = thingGrid[oneX][oneY];
        Thing t2 = thingGrid[twoX][twoY];

        //  Position exchange
        Rect tempRect = t1.getBounds();
        t1.setBounds(t2.getBounds());
        t2.setBounds(tempRect);

        //  Point exchange
        Thing tempThing = thingGrid[oneX][oneY];
        thingGrid[oneX][oneY] = thingGrid[twoX][twoY];
        thingGrid[twoX][twoY] = tempThing;
    }

    private void clickOut() {
        selectRect = null;
        selectedThing = null;
        selectedColPosition = -1;
        selectedRowPosition = -1;
    }

    private void pause() {
        isPause = selectRect == null && !isPause;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }



    private Drawable createHeart(Context context, boolean isBlurring) {
        final Heart h = new Heart(context, isBlurring);
        return new ShapeDrawable(h) {
            @Override
            protected boolean onStateChange(int[] stateSet) {
                h.setState(stateSet);
                return super.onStateChange(stateSet);
            }

            @Override
            public boolean isStateful() {
                return true;
            }
        };
    }

    private Drawable createPentagram(Context context, boolean isBlurring) {
        final Pentagram p = new Pentagram(context, isBlurring);
        return new ShapeDrawable(p) {
            @Override
            protected boolean onStateChange(int[] stateSet) {
                p.setState(stateSet);
                return super.onStateChange(stateSet);
            }

            @Override
            public boolean isStateful() {
                return true;
            }
        };
    }

    //  This method is used to draw the image according to the type
    private Drawable createDrawable(Thing thing, boolean isBlurring) {
        Drawable drawable = null;
        if (thing.getType() == Thing.Type.Heart) {
            drawable = createHeart(context, isBlurring);
        } else if (thing.getType() == Thing.Type.Pentagram) {
            drawable = createPentagram(context, isBlurring);
        } else if (thing.getType() == Thing.Type.Square) {
            drawable = getResources().getDrawable(R.drawable.square);
        } else if (thing.getType() == Thing.Type.Circle) {
            drawable = getResources().getDrawable(R.drawable.circle);
        }
        if (drawable != null) {
            drawable.setBounds(thing.getBounds());
        }
        return drawable;
    }

    private Rect calculateActualRect(float left, float top, float unitWidth) {
        int shapeUnitLength = (int) (unitWidth * 0.8);
        int actualLeft = (int) left + shapeUnitLength / 8;
        int actualTop = (int) top + shapeUnitLength / 8;
        int right = actualLeft + shapeUnitLength;
        int bottom = actualTop + shapeUnitLength;
        return new Rect(actualLeft, actualTop, right, bottom);
    }

    //  The blinking method
    private Runnable blinker = new Runnable() {
        @Override
        public void run() {
            try {
                blink = true;
                Thread.sleep(250);
                blink = false;
                Thread.sleep(250);
                blink = true;
                Thread.sleep(250);
                blink = false;
            } catch (InterruptedException e) {
                blink = false;
            }
        }
    };

    //  The move method
    private Runnable MoveAction = new Runnable() {
        @Override
        public void run() {
            try {
                Rect oldNow = new Rect(thingGrid[selectedRowPosition][selectedColPosition].getBounds());
                Rect oldD = new Rect(thingGrid[dX][dY].getBounds());
                for (int i = (int) unitWidth + 1; i >= 0; i--) {
                    Thread.sleep(10);
                    Rect rectNow = new Rect(thingGrid[selectedRowPosition][selectedColPosition].getBounds());
                    Rect rectD = new Rect(thingGrid[dX][dY].getBounds());
                    int offset = 1;
                    switch (moveType) {
                        case 1:
                            // left
                            rectNow.offset(-offset, 0);
                            rectD.offset(offset, 0);
                            break;
                        case 2:
                            // right
                            rectNow.offset(offset, 0);
                            rectD.offset(-offset, 0);
                            break;
                        case 3:
                            // up
                            rectNow.offset(0, -offset);
                            rectD.offset(0, offset);
                            break;
                        case 4:
                            // down
                            rectNow.offset(0, offset);
                            rectD.offset(0, -offset);
                            break;
                    }
                    thingGrid[dX][dY].setBounds(rectD);
                    thingGrid[selectedRowPosition][selectedColPosition].setBounds(rectNow);
                }

                thingGrid[dX][dY].setBounds(oldNow);
                thingGrid[selectedRowPosition][selectedColPosition].setBounds(oldD);
                switchTwoUnitThing(selectedRowPosition, selectedColPosition, dX, dY);
                if (calculateData() > 0) {
                    new Thread(blurringer).start();
                } else {
                    switchTwoUnitContent(selectedRowPosition, selectedColPosition, dX, dY);
                    isLock = false;
                }
            } catch (InterruptedException e) {
                blink = false;
            }
        }
    };

    //  Blank the image
    private Runnable blurringer = new Runnable() {
        @Override
        public void run() {
            try {
                blurring = true;
                Thread.sleep(1000);
                blurring = false;
                isLock = false;
                removeAndDrop();
            } catch (InterruptedException e) {
                blurring = false;
            }
        }
    };

    //  This method is used to calculate the movement state
    public int calculateData() {
        Thing.Type initType = null;
        int countHorizontal = 0;
        boolean horizontalAdd = false;
        int countVertical = 0;
        boolean verticalAdd = false;
        removeList.clear();
        //  Horizontal check
        for (int i = 0; i < 8; i++) {
            Log.e("cal", "-----------------------------------------------------------" + (i + 1));
            for (int j = 0; j < 8; j++) {
                if (initType != thingGrid[j][i].getType()) {
                    initType = thingGrid[j][i].getType();
                    countHorizontal = 0;
                    horizontalAdd = false;
                } else {
                    if (horizontalAdd) {
                        removeList.add(new MyPoint(j, i));
                        score += 1;
                        continue;
                    }
                    countHorizontal++;
                }

                if (countHorizontal >= 2) {
                    Log.e("cal", "lastI:" + j + "--countHorizontal:" + countHorizontal);
                    for (int k = countHorizontal; k >= 0; k--) {
                        Log.e("Point", "x:" + (j - k) + "--y:" + i);
                        removeList.add(new MyPoint(j - k, i));
                        score += 1;
                    }
                    horizontalAdd = true;
                }
            }
            initType = null;
            countHorizontal = 0;
            horizontalAdd = false;
        }


        initType = null;
        //  Vertical check
        for (int i = 0; i < 8; i++) {
            Log.e("cal", "-----------------------------------------------------------" + (i + 1));
            for (int j = 0; j < 8; j++) {
                if (initType != thingGrid[i][j].getType()) {
                    initType = thingGrid[i][j].getType();
                    countVertical = 0;
                    verticalAdd = false;
                } else {
                    if (verticalAdd) {
                        removeList.add(new MyPoint(i, j));
                        score += 1;
                        continue;
                    }
                    countVertical++;
                }

                if (countVertical >= 2) {
                    Log.e("cal", "lastI:" + j + "--countVertical:" + countVertical);
                    for (int k = countVertical; k >= 0; k--) {
                        Log.e("Point", "x:" + i + "--y:" + (j - k));
                        removeList.add(new MyPoint(i, j - k));
                        score += 1;
                    }
                    verticalAdd = true;
                }
            }
            initType = null;
            countVertical = 0;
            verticalAdd = false;
        }

        if (removeList.size() == 0) {
            isLock = false;
            blurring = false;
            return 0;
        } else {
            return removeList.size();
        }
    }

    //  This method is used to implement the remove and shif down function
    public void removeAndDrop() {
        //  Remove the image
        for (int i = 0; i < removeList.size(); i++) {
            thingGrid[removeList.get(i).x][removeList.get(i).y].setType(Thing.Type.N);
        }

        printPoint(removeList);

        //  Shift Down
        for (int i = 0; i < 8; i++) {
            int count = 7;
            Thing.Type[] newType = new Thing.Type[8];
            for (int j = 7; j >= 0; j--) {
                if (thingGrid[j][i].getType() != Thing.Type.N) {
                    newType[count] = thingGrid[j][i].getType();
                    count--;
                }
            }

            if (count == -1) {
                continue;
            }

            for (int k = count; k >= 0; k--) {
                newType[k] = createNewType();
            }

            for (int m = 0; m < newType.length; m++) {
                thingGrid[m][i].setType(newType[m]);
            }
        }

        selectedRowPosition = -1;
        selectedColPosition = -1;
        selectRect = null;
        selectedThing = null;

        if (calculateData() > 0) {
            new Thread(blurringer).start();
        }
    }

    public void onResume() {
        isDraw = true;
    }

    public void onPause() {
        isDraw = false;
        Helper.saveThingGrid(thingGrid);
        Helper.saveScore(score);
    }

    //  This class is used to draw the layout
    class DrawTask extends TimerTask {
        Paint paint;

        public DrawTask() {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);// 空心
            paint.setStrokeWidth(2);
            paint.setTextSize(25);
        }

        @Override
        public void run() {
            canvas = holder.lockCanvas();
            if (canvas == null) {
                return;
            }
            canvas.drawColor(Color.WHITE);
            //  Draw the grid
            for (int i = 0; i < thingGrid.length; i++) {
                for (int j = 0; j < thingGrid[i].length; j++) {
                    Thing thing = thingGrid[i][j];
                    if (thing == null || thing.getType() == Thing.Type.N) {
                        continue;
                    }
                    boolean isRemove = removeList != null && removeList.size() > 0 && removeList.contains(new MyPoint(i, j));
                    Drawable drawable = createDrawable(thing, isRemove);
                    if (drawable != null) {
                        if (selectedColPosition >= 0 && selectedRowPosition >= 0 && blink && thing.getType() == thingGrid[selectedRowPosition][selectedColPosition].getType()) {
                            drawable.setState(selectedState);
                        } else {
                            drawable.setState(unselectedState);
                        }
                        if (isRemove) drawable.setAlpha(128);
                        drawable.draw(canvas);
                    }
                }
            }

            //  Draw the selected rectangle
            if (selectRect != null) {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(4);
                canvas.drawRect(selectRect, paint);
                paint.setColor(Color.RED);
                paint.setStrokeWidth(2);
            }

            //  Draw the point
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // The point displays on side of the grid when landscape
                float size = paint.getTextSize();
                paint.setTextSize(55);
                canvas.drawText("Score:" + score, 100, gamePanelTop, paint);
                paint.setTextSize(size);
            } else {
                //  The point displays on top of the grid when portrait
                float size = paint.getTextSize();
                paint.setTextSize(55);
                canvas.drawText("Score:" + score, gamePanelLeft, 100, paint);
                paint.setTextSize(size);
            }

            //  Draw the pause state
            if (isPause) {
                int color = paint.getColor();
                paint.setColor(ContextCompat.getColor(context, R.color.pauseColor));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                Rect targetRect = new Rect(0, 0, screenWidth, screenHeight);
                canvas.drawRect(targetRect, paint);
                paint.setStyle(Paint.Style.STROKE);

                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(3);

                float size = paint.getTextSize();
                Paint.Align textAlign = this.paint.getTextAlign();

                paint.setTextSize(80);
                String testString = "Pause";
                Paint.FontMetricsInt fontMetrics = this.paint.getFontMetricsInt();
                int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(testString, targetRect.centerX(), baseline, this.paint);

                paint.setTextSize(size);
                paint.setTextAlign(textAlign);
                this.paint.setColor(color);
            }

            //  Draw two pieces moving animation
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void printGrid() {
        Log.e("printGrid", "===========================================================");
        for (int i = 0; i < thingGrid.length; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < thingGrid[i].length; j++) {
                line.append(thingGrid[i][j].getType()).append(" ");
            }
            Log.e("printGrid", line.toString());
        }
    }

    private void printPoint(List<MyPoint> list) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            line.append("Px:").append(list.get(i).x + 1).
                    append("==Py:").append(list.get(i).y + 1).append("\n");
        }
        Log.e("point", line.toString());
    }

    private void printType(Thing.Type[] a) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            line.append(a[i]).append(" ");
        }
        Log.e("type", line.toString());
    }
}
