package com.huaqin.vibrator.vibratortest;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by gsoft2-3 on 17-7-31.
 * Service 负责启动一个线程执行震动
 */

public class VibratorService extends Service {
    private static final String TAG = "Vibrator";
    private VibratorThread mVibratorThread;
    private Vibrator mVibrator;
    /**
     * wakeLock 灭屏时保证CPU正常运转,可以持续震动
     */
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        //PARTIAL_WAKE_LOCK参数表示 无论屏幕的状态甚至是用户按了电源钮, CPU 都会继续工作
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VibratorWakeLock");
        //获取wakeLock
        wakeLock.acquire();

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        /**
         * 启动Service为前台服务,保证Service在灭屏后的优先级,显示通知栏
         */
        showNotification();

        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        startRingerVibrator(MainActivity.mVibratorTime, MainActivity.mIntervalTime);
        return flags;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        stopRingerVibrator();
        mVibrator.cancel();
        //释放wakeLock
        wakeLock.release();
        super.onDestroy();
    }

    private void showNotification(){
        //定义一个notification
        Notification notification = new Notification.Builder(this.getApplicationContext())
                .setContentText("VibratorService")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        //把该service创建为前台service
        startForeground(1, notification);
    }


    /**
     * 震动线程 mStop 为true时无限震动
     */
    private class VibratorThread extends Thread {
        boolean mStop = false;
        public long startTime = 0;
        public long stopTime = 0;

        public void run() {
            Log.d(TAG, "RingerVibrator mVibratorThread :"
                    + "  mStop:" + mStop);
            while (!mStop) {
                mVibrator.vibrate(startTime);
                SystemClock.sleep(startTime + stopTime);
            }
        }
    }

    /**
     * 启动线程开始震动
     *
     * @param start
     *        震动时长
     * @param stop
     *        间隔时长
     */
    private void startRingerVibrator(long start, long stop) {
        if (start < 10 || stop < 10) {
            return;
        }

        if (mVibratorThread == null) {
            mVibratorThread = new VibratorThread();
        }

        mVibratorThread.startTime = start;
        mVibratorThread.stopTime = stop;
        Log.d(TAG, "startRingerVibrator - starting vibrator...");
        mVibratorThread.start();
    }

    /**
     * 将线程 mStop置为false 结束循环,线程自动运行结束退出
     */
    public void stopRingerVibrator() {
        Log.d(TAG, "stopRingerVibrator - stop vibrator... mVibratorThread:" + mVibratorThread);
        if (mVibratorThread != null) {
            mVibratorThread.mStop = true;
            mVibratorThread = null;
        }
    }
}
