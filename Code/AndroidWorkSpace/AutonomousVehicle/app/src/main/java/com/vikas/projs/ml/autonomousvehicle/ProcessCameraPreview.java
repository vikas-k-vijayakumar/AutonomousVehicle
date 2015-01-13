package com.vikas.projs.ml.autonomousvehicle;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Vikas K Vijayakumar(kvvikas@yahoo.co.in) on 12/21/2014.
 */
public class ProcessCameraPreview extends ViewGroup implements SurfaceHolder.Callback, Camera.PreviewCallback{

    SurfaceView deviceSurfaceView;
    SurfaceHolder deviceSurfaceHolder;
    Camera deviceCamera;
    //Used to get and set the camera settings
    private Camera.Parameters parameters;
    //Used to store the camera preview size
    private Camera.Size previewSize;
    //Used to store the GrayScale pixels as hexadecimal pairs
    private byte[] grayScalePixels;
    //BlockingQueue on which the Sensor Data is published
    //Java Blocking queue is used to exchange SensorData information between this thread and the StreamSensorData thread
    //Capacity of the queue is 500, which means the streaming thread can lag behind by persisting upto 500 features before things gets bad!
    private ArrayBlockingQueue<SensorData> sensorDataQueue = new ArrayBlockingQueue<SensorData>(500);

    List<Camera.Size> supportedPreviewSizes;

    //Number of cameras on the device
    int numberOfCameras;
    // The first rear facing camera
    int defaultCameraId;
    //Variable used to capture if a client is currently connected to consume the streams.
    //If not connected, then there is no need to publish the Sensor Data to the StreamSensorData thread
    private static boolean streamingClientConnected = false;
    StreamSensorData streamSensorData;
    int streamingPortNumber;

    ProcessCameraPreview(Context context, int streamingPortNumber) {
        super(context);
        this.streamingPortNumber = streamingPortNumber;
        Log.i("ProcessCameraPreview","Started the constructor Method");
        deviceSurfaceView = new SurfaceView(context);
        addView(deviceSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        deviceSurfaceHolder = deviceSurfaceView.getHolder();
        deviceSurfaceHolder.addCallback(this);
        //mobileSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("ProcessCameraPreview","Started the surfaceCreated Method");

        numberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
                Log.i("ProcessCameraPreview", "ID of the Back Facing Camera = "+String.valueOf(defaultCameraId));
            }
        }

        // The Surface has been created, acquire the camera and tell it where  to draw.
        //Very important to set the deviceCamera instance to null, else can lead to the 'failed to connect to camera' error
        if (deviceCamera != null) {
            deviceCamera.release();
            deviceCamera = null;
        }
        try{
            Log.i("ProcessCameraPreview","About to open the device camera");
            deviceCamera = Camera.open(defaultCameraId);
            Log.i("ProcessCameraPreview","Successfully opened the device camera");
        }catch(Exception e){
            Log.e("ProcessCameraPreview", "Exception caused when trying to open the camera", e);
        }
        if (deviceCamera != null){
            try {
                Log.i("ProcessCameraPreview","About to set the camera preview display");
                deviceCamera.setPreviewDisplay(holder);

                //sets the camera callback to be the one defined in this class
                deviceCamera.setPreviewCallback(this);

                ///initialize the variables
                parameters = deviceCamera.getParameters();
                previewSize = parameters.getPreviewSize();
                grayScalePixels = new byte[previewSize.width * previewSize.height];
                Log.i("ProcessCameraPreview","successfully set the camera preview display");

            } catch (IOException exception) {
                deviceCamera.release();
                deviceCamera = null;
                Log.e("ProcessCameraPreview", "IOException caused by setPreviewDisplay()", exception);
            }

            //Create the thread to Stream SensorData
            Log.i("ProcessCameraPreview", "About to launch the thread for StreamSensorData");
            streamSensorData = new StreamSensorData(sensorDataQueue, streamingPortNumber);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("ProcessCameraPreview","Started the surfaceChanged Method");
        // Now that the size is known, set up the camera parameters and begin  the preview.
        parameters.setPreviewSize(width, height);
        requestLayout();
        //set the camera's settings
        deviceCamera.setParameters(parameters);
        deviceCamera.startPreview();
        Log.i("ProcessCameraPreview","successfully started the camera preview");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("ProcessCameraPreview","Started the surfaceDestroyed Method");
        // Surface will be destroyed when we return, so stop the preview.
        if (deviceCamera != null){
            deviceCamera.stopPreview();
            deviceCamera.release();
            deviceCamera = null;
        }
        Log.i("ProcessCameraPreview","Successfully stopped the camera preview");

        //Interrupt the thread to Stream SensorData
        Log.i("ProcessCameraPreview", "About to cancel the thread for StreamSensorData");
        if(streamSensorData != null){
            streamSensorData.cancel();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i("ProcessCameraPreview","Started the onLayout Method");
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }
            Log.i("ProcessCameraPreview","Width = "+String.valueOf(width)+"/Height = "+String.valueOf(height)+"/previewWidth = "+String.valueOf(previewWidth)+"previewHeight = "+String.valueOf(previewHeight));

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                Log.i("ProcessCameraPreview","scaledChildWidth = "+String.valueOf(scaledChildWidth));
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                Log.i("ProcessCameraPreview","scaledChildHeight = "+String.valueOf(scaledChildHeight));
                child.layout(0, (height - scaledChildHeight) / 2, width,
                        (height + scaledChildHeight) / 2);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("ProcessCameraPreview","Started the onMeasure Method");
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);

        if (supportedPreviewSizes != null) {
            previewSize = getMinimumPreviewSize(supportedPreviewSizes, width,height);
            setMeasuredDimension(previewSize.width, previewSize.height);
            Log.i("ProcessCameraPreview","Following Measure Dimension has been set: "+"Width = "+String.valueOf(previewSize.width)+"/Height = "+String.valueOf(previewSize.height));
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.i("ProcessCameraPreview","Started the onPreviewFrame Method");
        //Transform YUV420 NV21 pixel data into Grayscale
        if ((data.length) >= (previewSize.width * previewSize.height)){
            applyGrayScale(grayScalePixels, data,previewSize.width,previewSize.height);

            //Send sensor Data to BlockingQueue only if a client is connected to consume the stream
            if(getOrSetStreamConnStatus(true, true)){
                Log.i("ProcessCameraPreview","About to put SensorData to BlockingQueue");
                SensorData currentSensorData = new SensorData();
                currentSensorData.setFrameWidth(previewSize.width);
                currentSensorData.setFrameHeight(previewSize.height);
                currentSensorData.setPixelDepth(8);
                currentSensorData.setPixelData(grayScalePixels);
                try {
                    sensorDataQueue.put(currentSensorData);
                } catch (InterruptedException e) {
                    Log.e("ProcessCameraPreview", "InterrruptedException caused when sending SensorData to BlockingQueue", e);
                }
            }
        }
    }

    /**
     * Converts YUV420 NV21 to Y888 (RGB8888). The grayscale image still holds 3 bytes on the pixel.
     *
     * @param   pixels output array with the converted array of grayscale pixels
     * @param data byte array on YUV420 NV21 format.
     * @param width pixels width
     * @param height pixels height
     */
    public static void applyGrayScale(byte[] pixels, byte [] data, int width, int height) {
        Log.i("ProcessCameraPreview","Started the applyGrayScale Method");
        int p;
        int size = width*height;
        for(int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            int pixelIntValue = 0xff000000 | p<<16 | p<<8 | p;
            pixels[i] = (byte) pixelIntValue;
        }
    }

    public void setCamera(Camera camera) {
        Log.i("ProcessCameraPreview","Started the setCamera Method");
        deviceCamera = camera;
        if (deviceCamera != null) {
            supportedPreviewSizes = deviceCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    private Camera.Size getMinimumPreviewSize(List<Camera.Size> sizes, int w, int h) {
        Log.i("ProcessCameraPreview","Started the getMinimumPreviewSize Method");
        if (sizes == null){
            return null;
        }
        int minWidth = Integer.MAX_VALUE;

        Camera.Size optimalSize = null;
        // Try to find the min size
        for (Camera.Size size : sizes) {
            if (size.width < minWidth) {
                optimalSize = size;
                minWidth = size.width;
            }
        }
        return optimalSize;
    }

    protected static synchronized boolean getOrSetStreamConnStatus(boolean get, boolean status){
        if(get){
            return streamingClientConnected;
        }else{
            streamingClientConnected = status;
            return streamingClientConnected;
        }
    }
}
