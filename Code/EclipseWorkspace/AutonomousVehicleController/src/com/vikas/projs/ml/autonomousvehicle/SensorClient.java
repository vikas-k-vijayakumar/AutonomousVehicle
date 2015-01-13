package com.vikas.projs.ml.autonomousvehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.widgets.Display;

/**
 * This class defines and implements the protocol for communication between the Display and the Sensor device
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class SensorClient implements Runnable{
	
	private DataOutputStream dOutStream;
	private DataInputStream dInStream;
	private Socket socket;
	private String sensorIPV4Address;
	private int sensorPort;
	private Display display;
	private Thread sensorClientThread;
	private ArrayBlockingQueue<FeatureList> featureQueue;
	private String trainingDataDirectory;
	private boolean persistData;
	private PersistTrainingData persistTrainingData;
	private int pixelRowsToStripFromTop;
	private int pixelRowsToStripFromBottom;
	
	public SensorClient(String ipV4Address, int port, Display display, ArrayBlockingQueue<FeatureList> featureQueue, String trainingDataDirectory, boolean persistData, int pixelRowsToStripFromTop, int pixelRowsToStripFromBottom){
		this.sensorIPV4Address = ipV4Address;
		this.sensorPort = port;
		this.display = display;
		this.featureQueue = featureQueue;
		this.trainingDataDirectory = trainingDataDirectory;
		this.persistData = persistData;
		this.pixelRowsToStripFromTop = pixelRowsToStripFromTop;
		this.pixelRowsToStripFromBottom = pixelRowsToStripFromBottom;
		
		//Create and start a thread
		sensorClientThread = new Thread(this, "SensorClient_"+ipV4Address+":"+port);
		sensorClientThread.start();
	}

	@Override
	public void run(){		
		logInfoToApplicationDisplay("Info: Started the SensorClient Thread");
		connectToSensor();		
	}
	
	public void cancel(){
		System.out.println("Info: Current SensorClient Thread will be interuppted");
		//Stop the Persistence Thread first
		if(persistTrainingData != null){
			persistTrainingData.cancel();
		}
		sensorClientThread.interrupt();
	}
	
	protected void disconnectFromSensor(){	
		this.cancel();
				
		if(dOutStream != null){
			//Disconnect OutStream
			try {
				dOutStream.close();
				logInfoToApplicationDisplay("Info: Successfully closed the DataOutStream");
			} catch (IOException e) {				
				logErrorToApplicationDisplay(e, "ERROR: IOException when trying to close the DataOutStream");
			}
		}
		

		if(dInStream != null){
			//Disconnect InStream
			try {
				dInStream.close();
				logInfoToApplicationDisplay("Info: Successfully closed the DataInStream");
			} catch (IOException e) {				
				logErrorToApplicationDisplay(e, "ERROR: IOException when trying to close the DataInStream");
			}
		}
		
		updateSensorConnectionStatus(false);
		
	}
	
	private void connectToSensor(){				
		/*
		 * Following is the protocol which will be used in the interaction between the VehicleController & SensorDevice
		 * 1) VehicleController initiates by connecting to SensorDevice and  sending a Handshake.This Handshake will be sent 
		 * and received as a UTF8 String.
		 * 2) SensorDevice will send back a Handshake as a UTF8 String.
		 * 3)Sensor device will send one set of Sensor data using the following:
		 * 		- Width of grayscale frame in pixels as an Integer
		 * 		- Height of grayscale frame in pixels as an Integer
		 * 		- Brightness value for each pixel. Totally (Width * Height) number of values will be sent as an Integer   
		 */
		
		//Try to create a connection to the Sensor device
		try {
			logInfoToApplicationDisplay("Info: About to connect to the Sensor Device");
			socket = new Socket(sensorIPV4Address, sensorPort);
			dOutStream = new DataOutputStream(socket.getOutputStream());
			
			//Send a Handshake to the SensorDevice first
			sendHandshake();
			logInfoToApplicationDisplay("Info: Sent Handshake to the Sensor Device");
			
			//Receive Handshake from Sensor Device
			receiveHandshake();

			updateSensorConnectionStatus(true);
			
			//Create a thread to persist Sensor Data if required i.e for Manual runs
			if(persistData){
				persistTrainingData = new PersistTrainingData(featureQueue,trainingDataDirectory,display,pixelRowsToStripFromTop,pixelRowsToStripFromBottom);
			}
			
			if (dInStream != null){
				receiveSensorData();
			}
			
		} catch (NumberFormatException e) {			
			logErrorToApplicationDisplay(e, "ERROR: Supplied value of streaming port appears to be in an incorrect format.");
			disconnectFromSensor();
		} catch (UnknownHostException e) {			
			logErrorToApplicationDisplay(e, "ERROR: Supplied Hostname for the sensor device cannot be resolved to an IP Address");
			disconnectFromSensor();
		} catch (IOException e) {			
			logErrorToApplicationDisplay(e, "ERROR: IOException when trying to connect to the Sensor device");
			disconnectFromSensor();
		}
		
	}
	
	/**
	 * Send a Handshake to let the Sensor Device know about the connector
	 */
	private void sendHandshake() throws UnknownHostException, IOException{
		if(dOutStream != null){
			dOutStream.writeUTF(Inet4Address.getLocalHost().getHostAddress());
			dOutStream.flush();
		}
	}

	/**
	 * Receive a Handshake from the Sensor Device
	 */
	private void receiveHandshake() throws IOException{
		logInfoToApplicationDisplay("Info: Attempting to receive Handshake from the Sensor Device");
		dInStream = new DataInputStream(socket.getInputStream());
		String sensorHandshake = dInStream.readUTF();
		logInfoToApplicationDisplay("Info: Successfully negotiated a handshake with the Sensor Device running at: "+sensorHandshake);
	}
	
	/**
	 * Receive Sensor Data i.e frames and accelerometer outputs
	 */
	private void receiveSensorData(){		
		//Continuously listen for SensorData
		while(!sensorClientThread.isInterrupted()){
			try {
				int frameWidth = dInStream.readInt();
				int frameHeight = dInStream.readInt();
				int frameDepth = dInStream.readInt();
				int framePixelCount = frameWidth * frameHeight;
				byte[] grayscaleFrameData = new byte[framePixelCount];
				
				//Keep reading until we get framePixelCount worth data
				int readPixels = 0;
				while(readPixels < framePixelCount){
					readPixels = readPixels + dInStream.read(grayscaleFrameData, readPixels, framePixelCount - readPixels);
				}
				
				logInfoToApplicationDisplay("Info: Successfully obtained a "+frameWidth+" X "+frameHeight+" frame from Sensor Device");		
				
				//Display the Sensor data on canvas
				displayFramesOnCanvas(frameWidth, frameHeight, frameDepth, grayscaleFrameData);
			} catch (IOException e) {
				if(sensorClientThread.isInterrupted()){
					logInfoToApplicationDisplay("Info: SensorData Receiver for "+sensorIPV4Address+":"+sensorPort+" has been interrupted, will not try to receive any more data");
				}else{
					logErrorToApplicationDisplay(e, "ERROR: IO exception when trying to obtain frame data from Sensor Device");
					disconnectFromSensor();
				}
			} 
		}
	}
	
	/**
	 * Proxy for the logInfoToApplicationDisplay function defined in DriverDisplayAndController
	 * @param logEntry
	 */
	private void logInfoToApplicationDisplay(final String logEntry){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.logInfoToApplicationDisplay(logEntry);
			}
		});
	}
	
	/**
	 * Proxy for the logErrorToApplicationDisplay function defined in DriverDisplayAndController
	 * @param e
	 */
	private void logErrorToApplicationDisplay(final Exception e, final String informationMessage){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.logErrorToApplicationDisplay(e, informationMessage);
			}
		});
	}
	
	/**
	 * Proxy for the updateSensorConnectionStatus function defined in DriverDisplayAndController
	 * @param connectionStatus
	 */
	private void updateSensorConnectionStatus(final boolean connectionStatus){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateSensorConnectionStatus(connectionStatus);
			}
		});
	}
	
	/**
	 * Proxy for the displayFramesOnCanvas function defined in DriverDisplayAndController
	 * @param width
	 * @param height
	 * @param pixelData
	 */
	private void displayFramesOnCanvas(final int width, final int height, final int depth, final byte[] pixelData){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayFramesOnCanvas(width, height, depth, pixelData, true);
			}
		});
	}
}
