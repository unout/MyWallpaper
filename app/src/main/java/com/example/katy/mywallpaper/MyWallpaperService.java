package com.example.katy.mywallpaper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyWallpaperService extends WallpaperService {

    private final int mId = 1091;

    @Override
    public Engine onCreateEngine() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.launcher)
                        .setContentTitle("YourWallpaper")
                        .setContentText("Customize!");

        Intent resultIntent = new Intent(this, MyPreferencesActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MyPreferencesActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
        return new MyWallpaperServiceEngine();
    }

    private class MyWallpaperServiceEngine extends WallpaperService.Engine {

        private static final int FPS = 32;
        private static final float STROKE_WIDTH = 32f;
        private static final int RADIUS = 2;

        private final Handler drawHandler = new Handler();

        private List<MyCircle> circles;
        private Paint paintCircles = new Paint();
        private Paint paintTouch = new Paint();
        private int width, height, minMetric;
        private boolean visible = true;
        private boolean touchEnabled;
        private boolean circlesEnabled;

        private int backgroundColor = getResources().getColor(R.color.background);
        private int circlesColor = getResources().getColor(R.color.circlesColor);
        private int touchColor = getResources().getColor(R.color.touchColor);

        private float probability = getResources().getInteger(R.integer.def_circle_probability);
        private int maxNumber = getResources().getInteger(R.integer.def_num_circles);
//        private int defBackgroundColor = getResources().getColor(R.color.background);
//        private int defCirclesColor = getResources().getColor(R.color.circlesColor);
//        private int defTouchColor = getResources().getColor(R.color.touchcolor);
//        private int backgroundColor;
//        private int circlesColor;
//        private int touchColor;

//        private int maxNumber;
//        private float probability;
//        private int defProbability = getResources().getInteger(R.integer.def_circle_probability);
//        private int defCirclesNum = getResources().getInteger(R.integer.def_num_circles);

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        public MyWallpaperServiceEngine() {
            checkPref();
            circles = new ArrayList<>();

            paintCircles.setAntiAlias(true);
            paintCircles.setColor(circlesColor);
            paintCircles.setStyle(Paint.Style.STROKE);
            paintCircles.setStrokeJoin(Paint.Join.ROUND);
            paintCircles.setStrokeWidth(STROKE_WIDTH);
            paintCircles.setStrokeCap(Paint.Cap.ROUND);

            paintTouch.set(paintCircles);
            paintTouch.setColor(touchColor);

            drawHandler.post(drawRunner);
        }

        private void checkPref() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyWallpaperService.this);
            String scn = prefs.getString("numberOfCircles", "");
            String spc = prefs.getString("probabilityOfCircle", "");

            try {
                if (scn.length() != 0) maxNumber = Integer.parseInt(scn);
                if (spc.length() != 0) probability = Integer.parseInt(spc) / 100f;
            } catch (NumberFormatException ignored) {
            }

            int sbc = prefs.getInt("backgroundColor", -1);
            int scc = prefs.getInt("circlesColor", -1);
            int stc = prefs.getInt("touchColor", -1);

            if (sbc != -1) backgroundColor = sbc;
            if (scc != -1) circlesColor = scc;
            if (stc != -1) touchColor = stc;

            touchEnabled = prefs.getBoolean("touch", true);
            circlesEnabled = prefs.getBoolean("circles", true);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                checkPref();
                drawHandler.post(drawRunner);
            } else {
                drawHandler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            drawHandler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            this.width = width;
            this.height = height;
            this.minMetric = width < height ? width : height;
            checkPref();
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (touchEnabled) {
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(backgroundColor);
                        int eventX = (int) event.getX();
                        int eventY = (int) event.getY();
                        circles.add(new MyCircle(eventX, eventY, RADIUS, paintTouch));
                        drawCircles(canvas, circles);
                    }
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
                super.onTouchEvent(event);
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(backgroundColor);
                    drawCircles(canvas, circles);
                    for (int i = 0; i < circles.size(); i++) {
                        if (circles.get(i).radius > minMetric) {
                            circles.remove(i);
                        }
                    }
                    if (circlesEnabled) {
                        Random r = new Random();
                        if (circles.size() <= maxNumber && r.nextFloat() > (1 - probability)) {
                            int x = (int) (width * r.nextFloat());
                            int y = (int) (height * r.nextFloat());
                            circles.add(new MyCircle(x, y, RADIUS, paintCircles));
                        }
                    }
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }

            drawHandler.removeCallbacks(drawRunner);
            if (visible) {
                drawHandler.postDelayed(drawRunner, 1000 / FPS);
            }
        }

        private void drawCircles(Canvas canvas, List<MyCircle> circles) {
            for (MyCircle p : circles) {
                p.color.setShader(new RadialGradient(p.x, p.y, p.radius, p.color.getColor(), backgroundColor, Shader.TileMode.MIRROR));
                canvas.drawCircle(p.x, p.y, p.radius, p.color);
                p.radius += minMetric / FPS;
            }
        }
    }

}
