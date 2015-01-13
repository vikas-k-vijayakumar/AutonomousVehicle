package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.widgets.Display;

/**
 * Will display the captured training data on screen for review
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class DisplayTrainingData implements Runnable{
	private String trainingDataFileName;
	private Thread displayTrainingDataThread;
	private Display display;
	private int frameWidth;
	private int frameHeight;
	private int frameDepth;
	private static int displayImageNumber=1;
	private BufferedReader br;
	private static ArrayBlockingQueue<String> trainingDataQueue = new ArrayBlockingQueue<String>(5);
	
	public DisplayTrainingData(Display display, String trainingDataFileName, int frameWidth, int frameHeight, int frameDepth){
		this.trainingDataFileName = trainingDataFileName;
		this.display = display;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.frameDepth = frameDepth;
		
		//Create and start a Thread
		displayTrainingDataThread = new Thread(this);
		displayTrainingDataThread.start();
		
	}

	@Override
	public void run() {
		
		try {					
			while(!displayTrainingDataThread.isInterrupted()){
				br = new BufferedReader(new FileReader(trainingDataFileName));
				//Wait for button press
				trainingDataQueue.take();
				int currentImageNumber = 1;
				
				while(currentImageNumber <= displayImageNumber){
					String line = br.readLine();
					
					if(line == null){
						if(displayImageNumber == 1){
							displayImageNumber = 1;
						}else{
							displayImageNumber--;
						}
						break;
					}else if (currentImageNumber == displayImageNumber){
						String[] stringPixeldata = line.split(",");
						logInfoToApplicationDisplay("Info: Number of pixels in frame = "+stringPixeldata.length);
						byte[] bytePixelData = new byte[frameWidth * frameHeight];
						for(int i=0;i<frameWidth * frameHeight;i++){
							int intPixelData = Integer.valueOf(stringPixeldata[i]);
							bytePixelData[i] = (byte) intPixelData;
						}
												
						displayFramesOnCanvas(frameWidth, frameHeight, frameDepth, bytePixelData);
						displayTrainingDataSteeringDirection(stringPixeldata[frameWidth * frameHeight]);

					}else{
						//Do nothing
					}
					currentImageNumber++;
				}
				
				br.close();
			}
		} catch (FileNotFoundException e) {
			logErrorToApplicationDisplay(e, "ERROR: File "+trainingDataFileName+" Not Found");
		} catch (InterruptedException e) {
			logInfoToApplicationDisplay("Info: Display TrainingData thread has been interrupted");
		} catch(IOException e){
			logErrorToApplicationDisplay(e, "ERROR: IO Exception when reading from File "+trainingDataFileName);
		} catch(ArrayIndexOutOfBoundsException e){
			logErrorToApplicationDisplay(e, "ERROR: Provided values for the training data frame size seem to be incorrect");
		}
		
	}
	
	public void cancel(){
		try {
			br.close();
		} catch (IOException e) {
			logErrorToApplicationDisplay(e, "ERROR: IO Exception when closing the stream from file "+trainingDataFileName);
		}
		logInfoToApplicationDisplay("Info: Current Display TrainingData thread will be interuppted");
		displayTrainingDataThread.interrupt();
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
	 * Proxy for the displayFramesOnCanvas function defined in DriverDisplayAndController
	 * @param width
	 * @param height
	 * @param pixelData
	 */
	private void displayFramesOnCanvas(final int width, final int height, final int depth, final byte[] pixelData){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayFramesOnCanvas(width, height, depth, pixelData, false);
			}
		});
	}
	
	private void displayTrainingDataSteeringDirection(final String steeringDirection){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayTrainingDataSteeringDirection(steeringDirection);
			}
		});
	}
	
	protected synchronized void displayNextImage(){
		displayImageNumber++;
		//Notify the thread to wake up
		try {
			trainingDataQueue.put(String.valueOf(displayImageNumber));
		} catch (InterruptedException e) {
			logErrorToApplicationDisplay(e, "Error: Interrupted when trying to process request for displaying Next training data image");
		}
	}
	
	protected synchronized void displayPreviousImage(){
		if(displayImageNumber == 1){
			displayImageNumber = 1;
		}else{
			displayImageNumber--;
		}
		//Notify the thread to wake up
		try {
			trainingDataQueue.put(String.valueOf(displayImageNumber));
		} catch (InterruptedException e) {
			logErrorToApplicationDisplay(e, "Error: Interrupted when trying to process request for displaying Previous training data image");
		}
	}

}
