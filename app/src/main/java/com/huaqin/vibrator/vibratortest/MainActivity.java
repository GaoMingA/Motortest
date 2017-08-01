package com.huaqin.vibrator.vibratortest;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

/**
 * Created by gsoft2-3 on 17-7-31.
 * vibrator test for motor
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Vibrator";

    private Spinner mSpinner;
    private Vibrator mVibrator;
    private Button mButtonStart;
    private Button mButtonStop;

    /**
     * 测试时间 mVibratorTime为震动时间 mIntervalTime为间隔时间
     */
    public static long mVibratorTime = 2000;
    public static long mIntervalTime = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mButtonStart = (Button) findViewById(R.id.playpause);
        mButtonStart.setOnClickListener(this);
        mButtonStop = (Button) findViewById(R.id.stop);
        mButtonStop.setOnClickListener(this);

        /**
         * 初始化spinner下拉菜单,根据选择值确定震动时间和间隔时间
         */
        mSpinner = (Spinner) findViewById(R.id.myspinner);
        String[] items = getResources().getStringArray(R.array.spinnername);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "selected:" + i);
                if (i == 0) {
                    mVibratorTime = 2000;
                    mIntervalTime = 1000;
                } else if (i == 1) {
                    mVibratorTime = 1000;
                    mIntervalTime = 2000;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, VibratorService.class));
        mVibrator.cancel();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        /**
         * intent 用于Service,该Service负责执行震动
         */
        Intent intent = new Intent(this, VibratorService.class);

        int id = view.getId();
        switch (id) {
            case R.id.playpause:
                if (!isServiceWork(this,"com.huaqin.vibrator.vibratortest.VibratorService")) {
                    startService(intent);
                }
                break;
            case R.id.stop:
                stopService(intent);
                break;
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = activityManager.getRunningServices(40);
        if (list.size() <= 0) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            String mName = list.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
