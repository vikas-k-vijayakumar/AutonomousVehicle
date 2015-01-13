package com.vikas.projs.ml.autonomousvehicle;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;

/**
 * Created by Vikas K Vijayakumar(kvvikas@yahoo.co.in) on 12/21/2014.
 */
public class ProcessSensorData extends ActionBarActivity {

    private ProcessCameraPreview devicePreview;
    Camera deviceCamera;
    //Number of cameras on the device
    int numberOfCameras;
    // The first rear facing camera
    int defaultCameraId;
    int streamingPortNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("ProcessSensorData", "Started the onCreate Method");
        //requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = getIntent();
        streamingPortNumber = intent.getIntExtra(MainActivity.STREAMING_PORT_NUMBER, 6666);
        devicePreview = new ProcessCameraPreview(this, streamingPortNumber);
        setContentView(devicePreview);

        numberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
                Log.i("ProcessSensorData", "ID of the Back Facing Camera = "+String.valueOf(defaultCameraId));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("ProcessSensorData", "Started the onCreateOptionsMenu Method");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_process_camera_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("ProcessSensorData", "Started the onOptionsItemSelected Method");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.i("ProcessSensorData", "Started the onResume Method");
        super.onResume();
        if (deviceCamera != null) {
            deviceCamera.release();
            deviceCamera = null;
        }
        deviceCamera = Camera.open(defaultCameraId);
        devicePreview.setCamera(deviceCamera);
    }

    @Override
    protected void onPause() {
        Log.i("ProcessSensorData", "Started the onPause Method");
        super.onPause();
        if (deviceCamera != null) {
            devicePreview.setCamera(null);
            deviceCamera.release();
            deviceCamera = null;
        }
    }
}
