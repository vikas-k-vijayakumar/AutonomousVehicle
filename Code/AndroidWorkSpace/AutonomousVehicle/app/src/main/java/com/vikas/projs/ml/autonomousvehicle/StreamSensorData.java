package com.vikas.projs.ml.autonomousvehicle;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Vikas K Vijayakumar(kvvikas@yahoo.co.in) on 12/28/2014.
 */
public class StreamSensorData implements  Runnable{
    private ArrayBlockingQueue<SensorData> sensorDataQueue;
    private Thread streamSensorDataThread;
    private int streamingPortNumber;
    private ServerSocket streamingServerSocket;
    private Socket streamingSocket;

    public StreamSensorData(ArrayBlockingQueue<SensorData> sensorDataQueue,int streamingPortNumber){
        this.sensorDataQueue = sensorDataQueue;
        this.streamingPortNumber = streamingPortNumber;

        //Create and start a Thread
        streamSensorDataThread = new Thread(this);
        streamSensorDataThread.start();
    }

    @Override
    public void run() {

        try{
            //Create and start a Sever Socket to listen for connections
            streamingServerSocket = new ServerSocket(streamingPortNumber);
            streamingSocket = streamingServerSocket.accept();
            Log.i("StreamSensorData", "Accepted a connection to stream data from: "+streamingSocket.getInetAddress());

            //Define input and output streams
            DataInputStream din = new DataInputStream(streamingSocket.getInputStream());
            DataOutputStream dout = new DataOutputStream(streamingSocket.getOutputStream());

            //Send a Handshake and expect one in response
            dout.writeUTF(Inet4Address.getLocalHost().getHostAddress());
            dout.flush();
            String handshake = din.readUTF();
            Log.i("StreamSensorData", "Handshake received from: "+handshake);

            //Enable streaming
            Log.i("StreamSensorData", "About to enable streaming of Sensor Data");
            ProcessCameraPreview.getOrSetStreamConnStatus(false, true);

            //Stream Data, not expecting to read anything from the client
            while(!streamSensorDataThread.isInterrupted()){
                //Get SensorData from the BlockingQueue
                SensorData currentSensorData = sensorDataQueue.take();
                Log.i("StreamSensorData", "Received Sensor Data from Blocking Queue");
                //Write the Frame Width, Frame Height, Pixel Depth and Pixel byte data in that order
                dout.writeInt(currentSensorData.getFrameWidth());
                dout.writeInt(currentSensorData.getFrameHeight());
                dout.writeInt(currentSensorData.getPixelDepth());
                int pixelCount = currentSensorData.getFrameWidth() * currentSensorData.getFrameHeight();
                dout.write(currentSensorData.getPixelData(),0,pixelCount);
                dout.flush();
                Log.i("StreamSensorData", "Successfully streamed a "+currentSensorData.getFrameWidth()+" X "+currentSensorData.getFrameHeight()+" frame to client");
            }
        }catch(IOException e){
            Log.e("StreamSensorData", "IOException caused when trying to stream sensor data", e);
        }catch(InterruptedException e){
            Log.e("StreamSensorData", "Got Interrupted, will stop streaming data to client", e);
        }finally{
            //Disable streaming
            Log.i("StreamSensorData", "About to disable streaming of Sensor Data");
            ProcessCameraPreview.getOrSetStreamConnStatus(false, false);

            try{
                streamingSocket.close();
                streamingServerSocket.close();
                Log.i("StreamSensorData", "Successfully closed the socket connections to client");
            }catch(IOException e){
                Log.e("StreamSensorData", "IOException caused when trying to close connection to client", e);
            }
        }
    }

    public void cancel(){
        Log.i("StreamSensorData", "Current SensorClient Thread will be interrupted");
        streamSensorDataThread.interrupt();
    }
}
