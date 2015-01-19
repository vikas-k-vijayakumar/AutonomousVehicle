package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.widgets.Display;

/**
 * Used to persist the captured training data
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class PersistTrainingData implements Runnable{

	private FileWriter fWriter;
	private BufferedWriter bWriter;
	private ArrayBlockingQueue<FeatureMessage> featureQueue;
	private Thread persistTrainingDataThread;
	private String trainingDataDirectory;
	private Display display;
	private String trainingFileName;
	private File trainingFile;
	private String trainingFileColumnSeperator = ",";
	private int pixelRowsToStripFromTop;
	private int pixelRowsToStripFromBottom;
	
	public PersistTrainingData(ArrayBlockingQueue<FeatureMessage> featureQueue, String trainingDataDirectory, Display display, int pixelRowsToStripFromTop, int pixelRowsToStripFromBottom){
		this.featureQueue = featureQueue;
		this.trainingDataDirectory = trainingDataDirectory;
		this.display = display;
		this.pixelRowsToStripFromTop = pixelRowsToStripFromTop;
		this.pixelRowsToStripFromBottom = pixelRowsToStripFromBottom;
		//Set the TrainingData filename
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String currentDate = dateFormat.format(new Date());
		this.trainingFileName = "AVC_TrainingData_"+currentDate.substring(0, currentDate.indexOf(" "))+"_"+currentDate.substring(currentDate.indexOf(" ")+1,currentDate.length())+".csv";
		
		//Create and start a Thread
		persistTrainingDataThread = new Thread(this);
		persistTrainingDataThread.start();
	}
	
	@Override
	public void run() {
		
		//Create the file for persistence
		try{
			trainingFile = new File(trainingDataDirectory+"//"+trainingFileName);
			trainingFile.createNewFile();
			fWriter = new FileWriter(trainingFile.getAbsoluteFile());
			bWriter = new BufferedWriter(fWriter);
			updateCurrentTrainingParams();
			
			//Until interrupted, continuously poll for FeatureLists from the BlockingQueue
			//In case there are none in queue, the thread will block in anticipation of a new message
			while(!persistTrainingDataThread.isInterrupted()){
				try {
					FeatureMessage currentFeatureList = featureQueue.take();
					logInfoToApplicationDisplay("Info: Successfully received a "+currentFeatureList.getFrameWidth()+" X "+currentFeatureList.getFrameHeight()+" frame for persistence");
					
					//Calculate the pixels to be removed based on the number of pixels rows to be removed from top and bottom
					if((pixelRowsToStripFromTop + pixelRowsToStripFromBottom) >= currentFeatureList.getFrameHeight()){
						pixelRowsToStripFromTop = currentFeatureList.getFrameHeight();
						pixelRowsToStripFromBottom = 0;
					}
					int noOfPixelsToSkipAtStart = currentFeatureList.getFrameWidth() * pixelRowsToStripFromTop;
					int noOfPixelsToSkipAtEnd = currentFeatureList.getFrameWidth() * pixelRowsToStripFromBottom;
					int pixelNumberToSkipFromAtEnd = (currentFeatureList.getFrameWidth() * currentFeatureList.getFrameHeight()) - noOfPixelsToSkipAtEnd;
					logInfoToApplicationDisplay("Info: Pixel Numbers "+noOfPixelsToSkipAtStart+" to "+pixelNumberToSkipFromAtEnd+" in the frame will be persisted ");
					Boolean skipPixels = false;
					if((noOfPixelsToSkipAtStart > 0) || (noOfPixelsToSkipAtEnd > 0)){
						skipPixels = true;
					}
					int updatedFrameHeight = currentFeatureList.getFrameHeight() - (pixelRowsToStripFromTop + pixelRowsToStripFromBottom);
					
					//Write every pixel value after converting the byte array into an integer array
					int[] intPixelData = byteToInt(currentFeatureList.getFramePixelData());
					for(int k=0;k<intPixelData.length;k++){
						if(skipPixels){
							if((k < noOfPixelsToSkipAtStart) || (k > pixelNumberToSkipFromAtEnd)){
								//Skip
							}else{
								bWriter.write(String.valueOf(intPixelData[k]));
								bWriter.write(trainingFileColumnSeperator);
							}
						}else{
							bWriter.write(String.valueOf(intPixelData[k]));
							bWriter.write(trainingFileColumnSeperator);
						}
					}			
					//Write the Steering direction
					bWriter.write(currentFeatureList.getSteeringDirection());
					bWriter.newLine();
					bWriter.flush();
					logInfoToApplicationDisplay("Info: Successfully persisted a "+currentFeatureList.getFrameWidth()+" X "+updatedFrameHeight+" frame");
				} catch (InterruptedException e) {
					logInfoToApplicationDisplay("Info: Persist TrainingData thread has been interrupted, will not persist TrainingData anymore");
					stopPersistance();
				} catch (IOException e){
					logErrorToApplicationDisplay(e, "ERROR: IOException when trying to persist Training Data into Training Data file: "+trainingFile.getAbsolutePath());
					stopPersistance();
				}
			}
			
		}catch(IOException e){
			logErrorToApplicationDisplay(e, "ERROR: IOException when trying to create the Training Data file: "+trainingFile.getAbsolutePath());
			stopPersistance();
		}
				
	}

	private void cancel(){
		//Sleep for 2000 milliseconds so that all the persistance operation is complete
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//Nothing to do
		}
		logInfoToApplicationDisplay("Info: Current Persist TrainingData thread will be interuppted");
		persistTrainingDataThread.interrupt();
	}
	
	protected void stopPersistance(){	
		this.cancel();
				
		if(fWriter != null){
			//Close OutStream
			try {
				fWriter.close();
				logInfoToApplicationDisplay("Info: Successfully closed the Persistance Output File Stream");
			} catch (IOException e) {				
				logErrorToApplicationDisplay(e, "ERROR: IOException when trying to close the Persistance Output File Stream");
			}
		}		
		if(bWriter != null){
			//Close Stream
			try {
				bWriter.close();
				logInfoToApplicationDisplay("Info: Successfully closed the Persistance Output Buffered File Stream");
			} catch (IOException e) {				
				logErrorToApplicationDisplay(e, "ERROR: IOException when trying to close the Persistance Output Buffered File Stream");
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
	 * This method cnverts a Byte array into Int array
	 * @param data
	 * @return
	 */
	protected int[] byteToInt(byte[] data) {
	    int[] ints = new int[data.length];
	    for (int i = 0; i < data.length; i++) {
	        ints[i] = (int) data[i] & 0xff;
	    }
	    return ints;
	} 
	
	private void updateCurrentTrainingParams(){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateCurrentTrainingParams(trainingDataDirectory+"\\"+trainingFileName);
			}
		});
	}
}
