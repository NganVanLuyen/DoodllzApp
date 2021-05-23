package com.example.doodlzapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.print.PrintHelper;

import java.util.HashMap;
import java.util.Map;

public class DoodleView extends View {

    private static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private final Paint paintScreen;
    private final Paint paintLine;

    // Maps of current Paths being drawn and Points in those Paths
    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Point> previousPointMap = new HashMap<>();

    // DoodleView constructor initializes the DoodleView
    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintScreen = new Paint();

        // set the initial display settings for the painted line
        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(10);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
    }

    // creates Bitmap and Canvas based on View's size
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    // clear the painting
    public void clear() {
        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    // set the painted line's color
    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    // return the painted line's color
    public int getDrawingColor() {
        return paintLine.getColor();
    }

    // set the painted line's width
    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    // return the painted line's width
    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    // perform custom drawing when the DoodleView is refreshed on screen
    @Override
    protected void onDraw(Canvas canvas) {
        // draw the background screen
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);
        for (Integer key : pathMap.keySet())
            canvas.drawPath(pathMap.get(key), paintLine);
    }

    // handle touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        // determine whether touch started, ended or is moving
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex),
                    event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate(); // redraw
        return true;
    }

    // called when the user touches the screen
    private void touchStarted(float x, float y, int lineID) {
        Path path;
        Point point;

        // if there is already a path for lineID
        if (pathMap.containsKey(lineID)) {
            path = pathMap.get(lineID);
            path.reset();
            point = previousPointMap.get(lineID);
        } else {
            path = new Path();
            pathMap.put(lineID, path);
            point = new Point();
            previousPointMap.put(lineID, point);
        }

        // move to the coordinates of the touch
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    // called when the user drags along the screen
    private void touchMoved(MotionEvent event) {
        // for each of the pointers in the given MotionEvent
        for (int i = 0; i < event.getPointerCount(); i++) {
            // get the pointer ID and pointer index
            int pointerID = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerID);

            // if there is a path associated with the pointer
            if (pathMap.containsKey(pointerID)) {
                // get the new coordinates for the pointer
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                // get the path and previous point associated with this ponter
                Path path = pathMap.get(pointerID);
                Point point = previousPointMap.get(pointerID);

                // calculate how far the user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                // if the distance is significant enough to matter
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    path.quadTo(point.x, point.y, (newX + point.x) / 2,
                            (newY + point.y) / 2);

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    // called when the user finishes a touch
    private void touchEnded(int lineID) {
        Path path = pathMap.get(lineID);
        bitmapCanvas.drawPath(path, paintLine);
        path.reset();
    }

    // save the current image to the Gallery
    public void saveImage() {
        final String name = "Paint" + System.currentTimeMillis() + ".jpg";

        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), bitmap, name,
                "Paint Drawing");

        if (location != null) {
            Toast message = Toast.makeText(getContext(),
                    R.string.message_saved,
                    Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        } else {
            Toast message = Toast.makeText(getContext(),
                    R.string.message_error_saving, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
    }

    // print the current image
    public void printImage() {
        if (PrintHelper.systemSupportsPrint()) {
            PrintHelper printHelper = new PrintHelper(getContext());

            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        } else {
            Toast message = Toast.makeText(getContext(),
                    R.string.message_error_printing, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
    }
}
