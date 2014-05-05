package com.echolabstech.testrotate.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";
    private SparseArray<PointF> mActivePointers = new SparseArray();

    private float mlastPointerX = 0.0f;
    private float mLastPointerY = 0.0f;
    private float mLastAngle = 0.0f;
    private int mScreenHeight;
    private int mScreenWidth;

    private final int Q1 = 0;
    private final int Q2 = 1;
    private final int Q3 = 2;
    private final int Q4 = 3;
    private final int CW = 4;
    private final int CCW = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String LOCALTAG = TAG+"-onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        //mScreenHeight = size.y - (int) (size.y * 0.05f);
    }

    private int getQuadrant(float x, float y)
    {
        final String LOCALTAG = TAG+"-getQuandrant";

        float xoffset = (int) (mScreenWidth * .15);
        float xmax = mScreenWidth - xoffset;
        float xmedian = mScreenWidth / 2;
        float xmin = 0 + xoffset;

        float yoffset = (int) (mScreenHeight * .20);
        float ymax = mScreenHeight - yoffset;
        float ymedian = mScreenHeight / 2;
        float ymin = 0 + yoffset + yoffset;

        int quadrant = -1;

        Log.v(LOCALTAG, "x:"+x+", y:"+y);
        //Log.v(LOCALTAG, "xmax:"+xmax);
        //Log.v(LOCALTAG, "xmedian:"+xmedian);
        //Log.v(LOCALTAG, "ymax:"+ymax);
        //Log.v(LOCALTAG, "ymedian:"+ymedian);

        //check for left side
        if (x < xmedian && x >= 0)
        {
            //check for q1
            if (y >= 0 && y < ymedian)
            {
                Log.v(LOCALTAG, "Quandrant:Q1");
                quadrant = Q1;
            }
            //q4
            else if (y >= ymedian && y <= ymax)
            {
                Log.v(LOCALTAG, "Quandrant:Q4");
                quadrant = Q4;
            }
        }
        //check for right side
        else if (x <= xmax && x >= xmedian)
        {
            //check for q2
            if (y >= 0 && y < ymedian)
            {
                Log.v(LOCALTAG, "Quandrant:Q2");
                quadrant = Q2;
            }
            //q3
            else if (y <= ymax && y >= ymedian)
            {
                Log.v(LOCALTAG, "Quandrant:Q3");
                quadrant = Q3;
            }
        }

        return quadrant;
    }

    private int getDirection(int quadrant, float newx, float newy, float oldx, float oldy)
    {
        final String LOCALTAG = TAG+"-getRotation";

        int rotate = 0;
        switch(quadrant)
        {
            case Q1:
                //need to fix. In-accurate.

                if (newx < oldx && newy < oldy)
                {
                    Log.v(LOCALTAG, "rotate CW");
                    rotate = CW;
                }
                else if (newx > oldx && newy > oldy)
                {
                    Log.v(LOCALTAG, "rotate CCW");
                    rotate = CCW;
                }

                break;
            case Q2:
                if (newx < oldx && newy > oldy)
                {
                    Log.v(LOCALTAG, "rotate CW");
                    rotate = CW;
                }
                else if (newx > oldx && newy < oldy)
                {
                    Log.v(LOCALTAG, "rotate CCW");
                    rotate = CCW;
                }
                break;
            case Q3:
                if (newx < oldx && newy > oldy)
                {
                    Log.v(LOCALTAG, "rotate CW");
                    rotate = CW;
                }
                else if (newx > oldx && newy < oldy)
                {
                    Log.v(LOCALTAG, "rotate CCW");
                    rotate = CCW;
                }
                break;
            case Q4:
                if (newx < oldx && newy < oldy)
                {
                    Log.v(LOCALTAG, "rotate CW");
                    rotate = CW;
                }
                else if (newx > oldx && newy > oldy)
                {
                    Log.v(LOCALTAG, "rotate CCW");
                    rotate = CCW;
                }
                break;
        }

        return rotate;
    }

    private int getRotation(float direction)
    {
        if (direction == CW)
            return 5;
        else if (direction == CCW)
            return (-5);
        else
            return 0;
    }

    /*gets the size of the status bar

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    */

    @Override
    protected void onResume()
    {
        final String LOCALTAG = TAG+"-onResume";
        super.onResume();

        final ImageView diagram = (ImageView) this.findViewById(R.id.diagram);
        ImageView diagrammask = (ImageView) findViewById(R.id.diagrammask);

        diagrammask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final String LOCALTAG = TAG + "-onTouch";

                // get pointer index from the event object
                int pointerIndex = event.getActionIndex();

                // get pointer ID
                int pointerId = event.getPointerId(pointerIndex);

                // get masked (not specific to a pointer) action
                int maskedAction = event.getActionMasked();

                switch (maskedAction) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        // We have a new pointer. Lets add it to the list of pointers
                        PointF f = new PointF();
                        f.x = event.getX(pointerIndex);
                        f.y = event.getY(pointerIndex);
                        mActivePointers.put(pointerId, f);

                        mlastPointerX = mActivePointers.get(0).x;
                        mLastPointerY = mActivePointers.get(0).y;

                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // a pointer was moved
                        if (event.getPointerCount() == 2) {
                            int q = getQuadrant(mlastPointerX, mLastPointerY);
                            int direction = getDirection(q, event.getX(), event.getY(), mlastPointerX, mLastPointerY);
                            int rotate = getRotation(direction);
                            diagram.setRotation(diagram.getRotation() + rotate);

                            Log.v(LOCALTAG, "rotate:" + rotate + ", rotation:" + v.getRotation());

                            mlastPointerX = event.getX();
                            mLastPointerY = event.getY();
                        }

                        for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                            PointF point = mActivePointers.get(event.getPointerId(i));
                            if (point != null) {
                                point.x = event.getX(i);
                                point.y = event.getY(i);
                            }
                        }

                        //float angle = (float) Math.toDegrees(Math.atan2(mLastPointerY, mlastPointerX));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        mActivePointers.remove(pointerId);
                        break;
                    }
                }

                //v.invalidate();

                return true;
            }
        });

        /*
        diagram.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (v.getRotation() >= 360)
                    v.setRotation(0);

                v.setRotation(v.getRotation() + 1);

                return false;
            }
        });
        */
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
