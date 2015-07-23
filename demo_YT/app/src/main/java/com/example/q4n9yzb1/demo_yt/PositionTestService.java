package com.example.q4n9yzb1.demo_yt;

/**
 * Created by q4N9YZB1 on 08-07-2015.
 */
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

public class PositionTestService extends Service implements SensorEventListener, View.OnTouchListener {
    private static final String TAG = "BackgroundVideoPosition";
    private static final long DURATION = 60*1000;

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private float rangeProximity;
    private Sensor mAccelerometer;
    private Sensor mLight;

    private boolean isFlat = false;
    private boolean isDark = false;
    private boolean isMoving = false;
    private boolean isFaceUp = true;
    private boolean userFacing = false;

    private boolean both = false;

    private int count = 0;

    private float lux = -1.0f;

    private int numTouch = 0;
    private View dummyView;

    @Override
    public void onDestroy() {
        super.onDestroy();

        flagAlreadyStarted = false;

        //stopSensors();
        stopOtherSensors();

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeView(dummyView);
        dummyView = null;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_ISSET_NAME, false);
        editor.putStringSet(PREF_SET_NAME, null);
        editor.commit();

        Log.d(TAG, "onDestroy Called");

//        Intent intent = new Intent()
    }

    private boolean flagAlreadyStarted = false;
//    private boolean flagDone = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"flagAlreadyStarted: "+flagAlreadyStarted);
        Log.d(TAG,"isAlreadyWriting: "+isAlreadyWriting);

        if(flagAlreadyStarted) {
//            if(flagDone) {
//                startSensors();
//            }
            return super.onStartCommand(intent,flags,startId);
        }

        userFacing = false;
        count = 0;
        //both = false;
        lightDone = false;
        lux = -1.0f;

        flagAlreadyStarted = true;

        if(!isAlreadyWriting) {
            startSensors(); //accelerometer
        }

        startOtherSensors();

        return super.onStartCommand(intent, flags, startId);
    }

    private void stopOtherSensors() {
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this, mProximity);
            mSensorManager.unregisterListener(this, mLight);
            Log.d(TAG,"Stop Other Sensors");
        }
    }

    private void startOtherSensors() {
        if(mSensorManager == null) { // should never happen
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        rangeProximity = mProximity.getMaximumRange();
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d(TAG,"Start other Sensors");
    }

    private void startSensors() {
//        if(!flagDone) {
//            return;
//        }
//        flagDone = false;

        Log.d(TAG, "NumTouch: "+numTouch);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d(TAG,"Start Accel Sensors");
        //Log.d(TAG,"mProximity: "+mProximity.getReportingMode());
        //Log.d(TAG,"mLight: "+mLight.getReportingMode());
    }

    private void stopSensors() {
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this, mAccelerometer);
            Log.d(TAG,"Stop Accel Sensors");
        }

//        flagDone = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        flagAlreadyStarted = false;
        isAlreadyWriting = false;

        both = false;

        getFgApp(true);

        dummyView = new LinearLayout(this);
        dummyView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        dummyView.setOnTouchListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1,
                1,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        //params.setTitle("Pocket/Table");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(dummyView, params);
    }

    public PositionTestService() {
    }

    private boolean lightDone = false;
    private String status = null;

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        //status = null;

        if(sensor.getType() == Sensor.TYPE_LIGHT) {
            //Log.d(TAG,"Got Light Sensor Reading!");
            lux = event.values[0];
            lightDone = true;

        } else if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float g[];
            g = event.values.clone();

            if(isAlreadyWriting) {
                if(os != null) {
                    String s = "" + g[0] + ","+g[1]+","+g[2]+"\n";
                    try {
//                    Log.d(TAG,"Writing: "+s);
                        os.write(s.getBytes());
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.d(TAG, "Got NPE. sync issue. not a problem");
                    }
                }
//                return;
            }

            if(!flagAlreadyStarted) {
                return;
            }

            //Log.d(TAG,"Accel reading");

            if(!both) {
                return;
            }

            ++count;

            float norm = (float) Math.sqrt(g[0]*g[0] +
                    g[1]*g[1] + g[2]*g[2]);

            g[0] /= norm;
            g[1] /= norm;
            g[2] /= norm;

            int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));

            int degrees = 10;

            if(inclination < degrees || inclination > 180 - degrees) {
                isFlat = true;
                isFaceUp = inclination < 25;
                if(!isDark && isFaceUp) {
                    status = "On Table (Face Up)";
                } else if (isDark && !isFaceUp) {
                    status = "On Table (Face Down)";
                } else if (!isDark && !isFaceUp) {
                    status = "In Hand (Face Away)";
                } else if (isDark && isFaceUp) {
                    status = "In Pocket";
                }
            } else {
                isFlat = false;
                int rotation = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[1])));
                if(!isDark) {
                    int arot = Math.abs(rotation);
                    if(inclination < 90 && arot < 20) {
                        status = "In Hand (Face Up) Portrait";
                        userFacing = true;
                    } else if(inclination < 75 && (arot > 50 && arot < 145)) {
                        status = "In Hand (Face Up) Landscape";
                        userFacing = true;
                    } else {
                        status = "In Hand (Face Away)";
                        userFacing = false;
                    }
                } else {
                    status = "In Pocket";
                }
            }
            Log.d(TAG, status);
        } else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //Log.d(TAG, "Got Proximity Reading!");
            both = true;
            float distance = event.values[0];
            Log.d(TAG, "Proximity: " + distance);
            isDark = distance < rangeProximity;
        }

        if(count > 10 && both && lightDone) {
            count = -1;
            func();
            //count = -1;
        }
    }

    private void func() {
        String fname = "xrci_log";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Player_xerox");
        dir.mkdirs();
        String filename = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/XrciVideo/" + fname /*+ "_" + sdf.format(new Date())*/ + ".csv";
        Log.d(TAG, "Set output file: " + filename);

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename,true));
        } catch (FileNotFoundException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

        String string = null;

        String fgApp = getFgApp(false);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = 100 * level / (float) scale;
        int brightness = -1;
        try {
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG,"Brightness Settings not Found");
        }
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        int battStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = battStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                battStatus == BatteryManager.BATTERY_STATUS_FULL;
        String charging = isCharging ? "Charging" : "Battery";

        string = new Date().toString() + "," + status + "," + batteryPct + "%," + charging + ","
                + brightness + "," + isScreenOn + ","
                + lux + " lx," + fgApp + ",T:" + numTouch + "\n";

        Log.d(TAG,"Writing: "+string);

        try {
            bos.write(string.getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        numTouch = 0;

        flagAlreadyStarted = false;
        stopOtherSensors();
        Log.d(TAG,"Set flagAlreadyStarted to: " + flagAlreadyStarted);

        if(!userFacing || !isScreenOn) {

            if(!isAlreadyWriting) {
                stopSensors();
            } else if(thread != null && thread.isAlive()) {
                thread.interrupt();
                stopWriting();
            }
            //stopSelf();
            return;
        }

        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.HOUR_OF_DAY) < 19 && now.get(Calendar.HOUR_OF_DAY) > 8) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss", Locale.ENGLISH);
            String date = sdf.format(new Date());

            writeAccelFile(date);
         /*   Intent intent = new Intent(getApplicationContext(), RecorderService.class);
            intent.putExtra("record",true);
            intent.putExtra("date",date);*/
//            PendingIntent pendingIntent = PendingIntent.getService(
//                    getApplicationContext(),
//                    0,
//                    intent,
//                    PendingIntent.FLAG_NO_CREATE);
//            if(pendingIntent == null)
        //    startService(intent);

        }
    }

    private GZIPOutputStream os = null;

    private boolean isAlreadyWriting = false;

    private void writeAccelFile(String date) {
        if(isAlreadyWriting) {
            return;
        }

        isAlreadyWriting = true;
        String fname = "xrci_accel";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss", Locale.ENGLISH);
        File dir = new File(Environment.getExternalStorageDirectory()
                + "/Player_xerox/Accel");
        dir.mkdirs();
        String filename = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Player_xerox/Accel/" + fname + "_" + (date) + ".csv.gz";
        Log.d(TAG, "Set output file: " + filename);

        try {
            os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Sleep Starting");
                    Thread.sleep(DURATION);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Got an interrupt! Stopping sleep");
                    //e.printStackTrace();
                    return;
                }
                Log.d(TAG, "Sleep Finished");

                if(isAlreadyWriting) {
                    stopWriting();
                }
            }
        });

        thread.start();
    }

    private Thread thread = null;
    private void stopWriting() {
        if (!flagAlreadyStarted) {
            stopSensors();
        }

        /*Intent intent = new Intent(getApplicationContext(), RecorderService.class);
        intent.putExtra("record", false);
        startService(intent);*/

        if (os != null) {
            try {
                String s = new Date().toString() + ",,";
                os.write(s.getBytes());
                os.flush();
                os.close();
                os=null;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        isAlreadyWriting = false;
        //stopSelf();
    }

    private static final String PREFS_NAME = "pref";
    private static final String PREF_ISSET_NAME = "isset";
    private static final String PREF_SET_NAME = "appset";

    private String getFgApp(boolean first) {
        String fgApp = "";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isset = settings.getBoolean(PREF_ISSET_NAME, false);

        if(first && isset)
            return "";

        Set<String> appset = null;
        if(isset) {
            appset = settings.getStringSet(PREF_SET_NAME, null);
        } else {
            appset = new HashSet<String>();
        }

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pacm = getPackageManager();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                String s = "";
                if(!isset) {
                    if(!this.getPackageName().equals(appProcess.processName)) {
                        appset.add(appProcess.processName);
                    } else {
                        fgApp += appProcess.processName;
                        try {
                            CharSequence c = pacm.getApplicationLabel(pacm.getApplicationInfo(appProcess.processName, PackageManager.GET_META_DATA));
                            s += "(" + c.toString() + ") ";
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        fgApp += s;
                    }
                } else {
                    if(!appset.contains(appProcess.processName)) {
                        fgApp += appProcess.processName;
                        try {
                            CharSequence c = pacm.getApplicationLabel(pacm.getApplicationInfo(appProcess.processName, PackageManager.GET_META_DATA));
                            s += "(" + c.toString() + ") ";
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        fgApp += s;
                    }
                }
            }
        }

        Log.d(TAG, fgApp);

        if(!isset) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet(PREF_SET_NAME, appset);
            editor.putBoolean(PREF_ISSET_NAME, true);
            editor.commit();
        }

        return fgApp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        numTouch++;
        Log.d(TAG,"Touch Event: "+numTouch);
        return false;
    }
}

