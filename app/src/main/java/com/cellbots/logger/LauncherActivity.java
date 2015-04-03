/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cellbots.logger;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.Accelerometer.Axis;
import com.mbientlab.metawear.api.controller.Accelerometer.MovementData;
import com.mbientlab.metawear.api.controller.MechanicalSwitch;
//import com.cellbots.logger.SettingsFragment.SettingsState;

import com.cellbots.logger.localServer.ServerControlActivity;

import java.util.Locale;

/**
 * A simple Activity for choosing which mode to launch the data logger in.
 * 
 * @author clchen@google.com (Charles L. Chen)
 */
public class LauncherActivity extends Activity  implements ServiceConnection,MWScannerFragment.ScannerCallback {
    private MetaWearBleService mwService= null;
    private CheckBox useZipCheckbox;
    private MetaWearController mwCtrllr;
    private final String MW_MAC_ADDRESS= "F5:49:5E:07:04:D2";
    private Accelerometer accelCtrllr;
    private LocalBroadcastManager broadcastManager= null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///< Bind the MetaWear service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.main);

        useZipCheckbox = (CheckBox) findViewById(R.id.useZip);

        final Activity self = this;
        Button launchVideoFrontButton = (Button) findViewById(R.id.launchVideoFront);
        launchVideoFrontButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLoggingActivity(LoggerActivity.MODE_VIDEO_FRONT, useZipCheckbox.isChecked());
            }
        });
        Button launchVideoBackButton = (Button) findViewById(R.id.launchVideoBack);
        launchVideoBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLoggingActivity(LoggerActivity.MODE_VIDEO_BACK, useZipCheckbox.isChecked());
            }
        });

        Button connectMW = (Button) findViewById(R.id.mwconnect);
        connectMW.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MWScannerFragment().show(getFragmentManager(), "metawear_scanner_fragment");
            }
        });

        final EditText pictureDelayEditText = (EditText) findViewById(R.id.pictureDelay);
        Button launchPictureButton = (Button) findViewById(R.id.launchPicture);
        launchPictureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(self, LoggerActivity.class);
                i.putExtra(LoggerActivity.EXTRA_MODE, LoggerActivity.MODE_PICTURES);
                i.putExtra(LoggerActivity.EXTRA_USE_ZIP, useZipCheckbox.isChecked());
                int delay = 30;
                try {
                    delay = Integer.parseInt(pictureDelayEditText.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(self,
                            "Error parsing picture delay time. Using default delay of 30 seconds.",
                            Toast.LENGTH_LONG).show();
                }
                i.putExtra(LoggerActivity.EXTRA_PICTURE_DELAY, delay);
                startActivity(i);
                finish();
            }
        });
        // The code we are using for taking video through the front camera
        // relies on APIs added in SDK 9. Don't offer the front video option to
        // users on devices older than that OR to devices who have only one
        // camera. Currently assume that if only one camera is present, it is
        // the back camera.
        if (Build.VERSION.SDK_INT < 9 || Camera.getNumberOfCameras() == 1) {
            launchVideoFrontButton.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mwService != null) {
            ///< Don't forget to unregister the MetaWear receiver
            mwService.unregisterReceiver(MetaWearBleService.getMetaWearBroadcastReceiver());
        }
        getApplicationContext().unbindService(this);
    }
    @Override
    public void btDeviceSelected(BluetoothDevice mwBoard) {
        //mainFragment.setBtDevice(device);
        Log.i("debuglog", "device received");
        mwCtrllr= mwService.getMetaWearController(mwBoard);
        mwCtrllr.setRetainState(false);
        ///< Register the callback, log message will appear when connected
        mwCtrllr.addDeviceCallback(new MetaWearController.DeviceCallbacks() {
            @Override
            public void connected() {
                Log.i("logdebug", "A selected Bluetooth LE connection has been established!");
                Toast.makeText(getApplicationContext(), R.string.toast_connected, Toast.LENGTH_SHORT).show();
                accelCtrllr= ((Accelerometer) mwCtrllr.getModuleController(Module.ACCELEROMETER));

                Accelerometer.SamplingConfig config = accelCtrllr.enableXYZSampling().withFullScaleRange(Accelerometer.SamplingConfig.FullScaleRange.FSR_8G).withOutputDataRate(Accelerometer.SamplingConfig.OutputDataRate.ODR_50_HZ);
                accelCtrllr.startComponents();

            }

            @Override
            public void disconnected() {
                Log.i("logdebug", "Lost the selected Bluetooth LE connection!");
            }

        }).addModuleCallback(new Accelerometer.Callbacks() {
            @Override
            public void receivedDataValue(short x, short y, short z) {
                //Log.i("logdebug", "received data value");
                if(((LoggerApplication) getApplication()).isrecording()) {
                    Log.i("logdebug", String.format(Locale.US, "(%.3f, %.3f, %.3f)", x / 1000.0, y / 1000.0, z / 1000.0));
                }
            }
        });

        mwCtrllr.connect();
        Log.i("logdebug","connecting..");
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("logdebug","service connected started");
        mwService= ((MetaWearBleService.LocalBinder) service).getService();

        broadcastManager= LocalBroadcastManager.getInstance(mwService);
        broadcastManager.registerReceiver(MetaWearBleService.getMetaWearBroadcastReceiver(),
                MetaWearBleService.getMetaWearIntentFilter());
        mwService.useLocalBroadcastManager(broadcastManager);


        final BluetoothManager btManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice mwBoard = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        mwCtrllr= mwService.getMetaWearController(mwBoard);
        mwCtrllr.setRetainState(false);
        ///< Register the callback, log message will appear when connected
        mwCtrllr.addDeviceCallback(new MetaWearController.DeviceCallbacks() {
                       @Override
                       public void connected() {
                           Log.i("logdebug", "A Bluetooth LE connection has been established!");
                           Toast.makeText(getApplicationContext(), R.string.toast_connected, Toast.LENGTH_SHORT).show();
                           accelCtrllr= ((Accelerometer) mwCtrllr.getModuleController(Module.ACCELEROMETER));

                           //Accelerometer.SamplingConfig config = accelCtrllr.enableXYZSampling().withFullScaleRange(Accelerometer.SamplingConfig.FullScaleRange.FSR_8G).withOutputDataRate(Accelerometer.SamplingConfig.OutputDataRate.ODR_50_HZ);
                           //accelCtrllr.startComponents();

                       }

                       @Override
                       public void disconnected() {
                           Log.i("logdebug", "Lost the Bluetooth LE connection!");
                       }

        }).addModuleCallback(new Accelerometer.Callbacks() {
            @Override
            public void receivedDataValue(short x, short y, short z) {
                //Log.i("logdebug", "received data value");
                //Log.i("logdebug", String.format(Locale.US, "(%.3f, %.3f, %.3f)",x / 1000.0, y / 1000.0, z / 1000.0));
            }
        });

        //mwCtrllr.connect();
        Log.i("logdebug","connecting..");
    }



    ///< Don't need this callback method but we must implement it
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i("logdebug", "service disconnected");
    }

    private void launchLoggingActivity(int mode, boolean useZip) {
        Intent i = new Intent(LauncherActivity.this, LoggerActivity.class);
        i.putExtra(LoggerActivity.EXTRA_MODE, mode);
        i.putExtra(LoggerActivity.EXTRA_USE_ZIP, useZip);
        startActivity(i);
        //finish();
    }
}