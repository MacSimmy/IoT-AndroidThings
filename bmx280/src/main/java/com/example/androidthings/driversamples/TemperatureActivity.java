/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.driversamples;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorManager.DynamicSensorCallback;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;

import java.io.IOException;

/**
 * TemperatureActivity is an example that use the driver for the BMP280 temperature sensor.
 */
public class TemperatureActivity extends Activity {
    private static final String TAG = TemperatureActivity.class.getSimpleName();

    private Bmx280SensorDriver mBmp280SensorDriver;
    private SensorManager mSensorManager;

    private static final float BAROMETER_RANGE_LOW = 965.f;
    private static final float BAROMETER_RANGE_HIGH = 1035.f;
    private static final float BAROMETER_RANGE_SUNNY = 1010.f;
    private static final float BAROMETER_RANGE_RAINY = 990.f;


    private float mLastTemperature;
    private float mLastPressure;

    private DynamicSensorCallback mDynamicSensorCallback = new DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                // Our sensor is connected. Start receiving temperature data.
                mSensorManager.registerListener(mTemperatureListener, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                // Our sensor is connected. Start receiving pressure data.
                mSensorManager.registerListener(mPressureListener, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        @Override
        public void onDynamicSensorDisconnected(Sensor sensor) {
            super.onDynamicSensorDisconnected(sensor);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting TemperatureActivity");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);

        try {
            mBmp280SensorDriver = new Bmx280SensorDriver(BoardDefaults.getI2CPort());
            mBmp280SensorDriver.registerTemperatureSensor();
            mBmp280SensorDriver.registerPressureSensor();

        } catch (IOException e) {
            Log.e(TAG, "Error configuring sensor", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Closing sensor");
        if (mBmp280SensorDriver != null) {
            mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
            mSensorManager.unregisterListener(mTemperatureListener);
            mSensorManager.unregisterListener(mPressureListener);
            mBmp280SensorDriver.unregisterTemperatureSensor();
            mBmp280SensorDriver.unregisterPressureSensor();
            try {
                mBmp280SensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensor", e);
            } finally {
                mBmp280SensorDriver = null;
            }
        }
    }

    // Callback when SensorManager delivers temperature data.
    private SensorEventListener mTemperatureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastTemperature = event.values[0];
            Log.d(TAG, "Temperature sensor changed: " + mLastTemperature);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Temperature sensor accuracy changed: " + accuracy);
        }
    };

    // Callback when SensorManager delivers pressure data.
    private SensorEventListener mPressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastPressure = event.values[0];
            Log.d(TAG, "Pressure sensor changed: " + mLastPressure);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Pressure sensor accuracy changed: " + accuracy);
        }
    };
}
