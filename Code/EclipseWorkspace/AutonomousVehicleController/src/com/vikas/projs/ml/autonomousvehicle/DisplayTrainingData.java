package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	private Boolean predictionInProgress;
	private PredictUsingNN predictUsingNN;
	
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
		updateTrainingDataReviewLabelBgd("BLUE");
		try {					
			while(!displayTrainingDataThread.isInterrupted()){				
				//Wait for button press
				String queueCommand = trainingDataQueue.take();
				int currentImageNumber = 1;
				
				//If data set needs to be deleted from the image
				if(queueCommand == "DELETE"){
					BufferedReader brdelete = new BufferedReader(new FileReader(trainingDataFileName));
					//Delete image from the training data
					//create a temporary file, fill it with all sets except the one to be deleted and then rename the file
					File tempTrainingFile = new File(trainingDataFileName+".temp");
					tempTrainingFile.createNewFile();
					FileWriter fWriter = new FileWriter(tempTrainingFile.getAbsoluteFile());
					BufferedWriter bWriter = new BufferedWriter(fWriter);
					
					String line = null;
					while((line = brdelete.readLine()) != null){
						if (currentImageNumber == displayImageNumber){
							//Skip training set image
						}else{
							bWriter.write(line);
							bWriter.newLine();
						}
						currentImageNumber++;
					}
					bWriter.flush();
					
					//Close reader to original training data file
					brdelete.close();
					//Close writer to temp training data file
					fWriter.close();
					bWriter.close();
					
					//Rename temp file to original after deleting the original					
					File originalTrainingFile = new File(trainingDataFileName);
					if(originalTrainingFile.delete()){
						logInfoToApplicationDisplay("Info: Successfully deleted original training data file");
					}else{
						logWarningToApplicationDisplay("WARNING: Error when trying to delete original training data file");
					}
					if(tempTrainingFile.renameTo(new File(trainingDataFileName))){
						logInfoToApplicationDisplay("Info: Successfully renamed temporary training data file to original training data file");
					}else{
						logWarningToApplicationDisplay("WARNING: Error when trying to rename temporary training data file to original training data file");
					}					
					
				}
								
				//If the captured steering direction for the data set needs to be changed
				if(queueCommand.startsWith("REASSIGN")){
					String newSteeringDirection = queueCommand.substring(queueCommand.indexOf(",")+1, queueCommand.length());
					BufferedReader brdelete = new BufferedReader(new FileReader(trainingDataFileName));
					//create a temporary file, fill it with all sets, for the current data set reassign the steering direction
					File tempTrainingFile = new File(trainingDataFileName+".temp");
					tempTrainingFile.createNewFile();
					FileWriter fWriter = new FileWriter(tempTrainingFile.getAbsoluteFile());
					BufferedWriter bWriter = new BufferedWriter(fWriter);
					
					String line = null;
					while((line = brdelete.readLine()) != null){
						if (currentImageNumber == displayImageNumber){
							//ReAssign steering direction
							String[] columnValues = line.split(",");
							for(int i=0;i<columnValues.length;i++){
								//If Steering direction
								if(i == (columnValues.length - 1)){
									bWriter.write(newSteeringDirection);
								}else{
									bWriter.write(columnValues[i]);
									bWriter.write(",");
								}
							}
							bWriter.newLine();
						}else{
							bWriter.write(line);
							bWriter.newLine();
						}
						currentImageNumber++;
					}
					bWriter.flush();
					
					//Close reader to original training data file
					brdelete.close();
					//Close writer to temp training data file
					fWriter.close();
					bWriter.close();
					
					//Rename temp file to original after deleting the original					
					File originalTrainingFile = new File(trainingDataFileName);
					if(originalTrainingFile.delete()){
						logInfoToApplicationDisplay("Info: Successfully reassigned the steering direction");
					}else{
						logWarningToApplicationDisplay("WARNING: Error when trying to reassign the steering direction");
					}
					if(tempTrainingFile.renameTo(new File(trainingDataFileName))){
						logInfoToApplicationDisplay("Info: Successfully renamed temporary training data file to original training data file");
					}else{
						logWarningToApplicationDisplay("WARNING: Error when trying to rename temporary training data file to original training data file");
					}					
					
				}
				
				
				//Display image
				br = new BufferedReader(new FileReader(trainingDataFileName));
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
						int[] integerPixelData = new int[frameWidth * frameHeight];
						float[] floatPixelData = new float[frameWidth * frameHeight];
						for(int i=0;i<frameWidth * frameHeight;i++){
							int intPixelData = Integer.valueOf(stringPixeldata[i]);
							bytePixelData[i] = (byte) intPixelData;
							integerPixelData[i] = intPixelData;
							floatPixelData[i] = Float.valueOf(intPixelData);
						}
						
						//Display the image on canvas
						displayFramesOnCanvas(frameWidth, frameHeight, frameDepth, bytePixelData);
						//Display the capture steering direction
						displayTrainingDataSteeringDirection(stringPixeldata[frameWidth * frameHeight]);
						
						//Check if predictions are required
						if((predictionInProgress) && (predictUsingNN != null)){
							//Predict the steering direction based on the neural network configuration details provided
							FeatureMessage featureMessage = new FeatureMessage();
							featureMessage.setFramePixelDataFloat(floatPixelData);
							FeatureMessage predictedFeatureMessage = predictUsingNN.predictSteeringDirection(featureMessage);
							if(predictedFeatureMessage != null){
								//Display the predicted steering direction
								displayPredictedTrainingDataSteeringDirection(predictedFeatureMessage.getSteeringDirection());
								//Display the confidence of the Neural Network in predicting the steering direction
								updateSteeringPredictionConfidence(predictedFeatureMessage.getSteeringPredictionConfidence());
								//Change the background color of canvas
								//Green if the captured steering direction matches with the predicted steering direction
								//Red if the captured steering direction doesn't match with the predicted steering direction
								if(Integer.valueOf(predictedFeatureMessage.getSteeringDirection()) == Integer.valueOf(stringPixeldata[frameWidth * frameHeight])){
									updateTrainingDataReviewLabelBgd("GREEN");
								}else{
									updateTrainingDataReviewLabelBgd("RED");
								}
							}
							
						}
												
						//Update the button text
						updateTrainingDataFrameButtonText(currentImageNumber-1,currentImageNumber,currentImageNumber+1);

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
			if(br != null){
				updateTrainingDataReviewLabelBgd("BLUE");
				br.close();
				logInfoToApplicationDisplay("Info: Current Display TrainingData thread will be interuppted");
				displayTrainingDataThread.interrupt();
			}			
		} catch (IOException e) {
			logErrorToApplicationDisplay(e, "ERROR: IO Exception when closing the stream from file "+trainingDataFileName);
			updateTrainingDataReviewLabelBgd("BLUE");
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
	 * Proxy for the logWarningToApplicationDisplay function defined in DriverDisplayAndController
	 * @param logEntry
	 */
	private void logWarningToApplicationDisplay(final String logEntry){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.logWarningToApplicationDisplay(logEntry);
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
	
	private void displayPredictedTrainingDataSteeringDirection(final String steeringDirection){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayPredictedTrainingDataSteeringDirection(steeringDirection, false);
			}
		});
	}
	
	protected synchronized void displayNextImage(PredictUsingNN predictUsingNN, Boolean predictionInProgress){
		this.predictUsingNN = predictUsingNN;
		this.predictionInProgress = predictionInProgress;
		displayImageNumber++;
		//Notify the thread to wake up
		try {
			trainingDataQueue.put(String.valueOf(displayImageNumber));
		} catch (InterruptedException e) {
			logErrorToApplicationDisplay(e, "Error: Interrupted when trying to process request for displaying Next training data image");
		}
	}
	
	protected synchronized void displayPreviousImage(PredictUsingNN predictUsingNN, Boolean predictionInProgress){
		this.predictUsingNN = predictUsingNN;
		this.predictionInProgress = predictionInProgress;
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
	
	protected synchronized void deleteCurrentImage(){
		//Notify the thread to wake up
		try {
			trainingDataQueue.put("DELETE");
		} catch (InterruptedException e) {
			logErrorToApplicationDisplay(e, "Error: Interrupted when trying to process request for deleting current training data image");
		}
	}
	
	protected synchronized void reAssignSteeringDirection(int newSteeringDirection){
		//Notify the thread to wake up
		try {
			trainingDataQueue.put("REASSIGN"+","+newSteeringDirection);
		} catch (InterruptedException e) {
			logErrorToApplicationDisplay(e, "Error: Interrupted when trying to process request for reassigning the steering direction");
		}
	}
	
	private void updateTrainingDataFrameButtonText(final int previous, final int current, final int next){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateTrainingDataFrameButtonText(previous, current, next);
			}
		});
	}
	
	private void updateTrainingDataReviewLabelBgd(final String status){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateTrainingDataReviewLabelBgd(status);
			}
		});
	}
	
	private void updateSteeringPredictionConfidence(final int predictionConfidence){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateSteeringPredictionConfidence(predictionConfidence);
			}
		});
	}

}
