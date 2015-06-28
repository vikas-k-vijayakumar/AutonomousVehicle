package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.swt.widgets.Display;

/**
 * Performs predictions  using Neural Networks. Can handle upto three hidden layers
 * Though testing has only been performed for neural networks with one hidden layer
 */
public class PredictUsingNN implements Runnable{

	private Thread predictSteeringDirectionThread;
	private Display display;
	private ArrayList<RealMatrix> weightMatrixList;
	
	public PredictUsingNN(Display display, String[] weightFileNames) throws FileNotFoundException, IOException{
		this.display = display;
		weightMatrixList =  new ArrayList<RealMatrix>();
		
		//Read weights from the file and add to ArrayList
		for(int i=0;i<weightFileNames.length;i++){
			BufferedReader br = new BufferedReader(new FileReader(weightFileNames[i]));
			String line = null;
			int noOfLinesInFile = 0;
			int noOfColumnsInFile = 0;
			//Find number of lines in the file
			while((line = br.readLine()) != null ){
				//Find the count of rows in the file, this is required for creating the matrix
				noOfLinesInFile++;
				//Find the count of columns in the file, it is enough to take the count from the first row
				if(noOfLinesInFile == 1){
					//The weights i.e. theta file created from octave starts with a space which causes the first field to be 
					//an empty string.
					String[] columnValues = line.split(" ");
					noOfColumnsInFile = columnValues.length - 1;
				}
			}
			br.close();
			
			//Create RealMatrix. 
			//Set Number of rows to number of lines in file. 
			//Set Number of columns to number of entries in each line
			RealMatrix realMatrix = MatrixUtils.createRealMatrix(noOfLinesInFile,noOfColumnsInFile);
			
			if(noOfLinesInFile > 0){
				BufferedReader br1 = new BufferedReader(new FileReader(weightFileNames[i]));
				int currentLineNumber = 0;
				while((line = br1.readLine()) != null ){
					String[] weightsTemp = line.split(" ");
					//The weights i.e. theta file created from octave starts with a space which causes the first field to be 
					//an empty string. The below piece of code is used to filter out the first field
					String[] weights = new String[weightsTemp.length - 1];
					for(int k=1;k<weightsTemp.length;k++){
						weights[k-1] = weightsTemp[k];
					}

					for(int j=0;j<weights.length;j++){
						realMatrix.setEntry(currentLineNumber, j, Double.valueOf(weights[j]));
						//System.out.println("Array value = "+weights[j]+"   Matrix Value = "+realMatrix.getEntry(currentLineNumber, j));
					}
					currentLineNumber++;
				}
				weightMatrixList.add(realMatrix);
				br1.close();
				logInfoToApplicationDisplay("Info: Successfully read weights from the following file - "+weightFileNames[i]+" into a matrix of size "+realMatrix.getRowDimension()+" X "+realMatrix.getColumnDimension());
				
			}
		}

		//Create and start a Thread
		predictSteeringDirectionThread = new Thread(this);
		predictSteeringDirectionThread.start();
		
	}
	
	@Override
	public void run() {
		updatePredictionStatus("started");
		//Until interrupted, do nothing
		while(!predictSteeringDirectionThread.isInterrupted()){
			//Do nothing
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logInfoToApplicationDisplay("Info: Current Prediction thread has been interuppted");
			}
		}
	}
	
	public void cancel(){
		logInfoToApplicationDisplay("Info: Current Prediction thread will be interuppted");
		//Remove the currently displayer prediction image
		removePredictedTrainingDataSteeringDirection();
		//Reset the background
		updateTrainingDataReviewLabelBgd("BLUE");
		predictSteeringDirectionThread.interrupt();
	}
	
	/**
	 * Predicts the Steering direction based on the feature
	 * @param FeatureMessage
	 * @return FeatureMessage
	 */
	public FeatureMessage predictSteeringDirection(FeatureMessage currentFeatureList){
		FeatureMessage returnFeatureMessage = new FeatureMessage();
		try {						
			//The Auto mode will send the images in the form of byte array whereas
			//training review mode will send the images in the form of int array
			int[] framePixelData = null;
			if(currentFeatureList.getFramePixelDataInt() != null){
				framePixelData = currentFeatureList.getFramePixelDataInt();
			}else{
				framePixelData = Utilities.byteToInt(currentFeatureList.getFramePixelData());
			}
			
			//Create input layer activations, add the bias unit as well
			RealMatrix activationsOfInputLayer = MatrixUtils.createRealMatrix(1,framePixelData.length+1);
			activationsOfInputLayer.setEntry(0, 0, Double.valueOf(1));
			for(int i=0;i<framePixelData.length;i++){
				activationsOfInputLayer.setEntry(0, i+1, Double.valueOf(framePixelData[i]));
			}
			logInfoToApplicationDisplay("Info: Size of the Input matrix is: "+activationsOfInputLayer.getRowDimension()+" X"+activationsOfInputLayer.getColumnDimension());
			
			//Start calculating the activations for other layers
			RealMatrix activationsOfFirstHiddenLayer = null;
			RealMatrix activationsOfSecondHiddenLayer = null;
			RealMatrix activationsOfThirdHiddenLayer = null;
			RealMatrix predictedOutput = null;

			//Size = Number of nodes in first hidden layer  X  1
			RealMatrix tempmatrix = this.weightMatrixList.get(0).multiply(activationsOfInputLayer.transpose());
			activationsOfFirstHiddenLayer = Utilities.sigmoid(tempmatrix);
			//activationsOfFirstHiddenLayer = Utilities.sigmoid(weightMatrixList.get(0).multiply(activationsOfInputLayer.transpose()));
			logInfoToApplicationDisplay("Info: Size of the First Hidden Layer activation matrix is: "+activationsOfFirstHiddenLayer.getRowDimension()+" X "+activationsOfFirstHiddenLayer.getColumnDimension());

			//If one hidden layer
			if(this.weightMatrixList.size() == 2){
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(this.weightMatrixList.get(1).multiply(Utilities.addBiasNodeInRow(activationsOfFirstHiddenLayer)));
				logInfoToApplicationDisplay("Info: Size of the Predicted Output Layer matrix is: "+predictedOutput.getRowDimension()+" X "+predictedOutput.getColumnDimension());
			}
			
			//If two hidden layers
			if(this.weightMatrixList.size() == 3){
				//Size = Number of nodes in second hidden layer  X  1
				activationsOfSecondHiddenLayer = Utilities.sigmoid(this.weightMatrixList.get(1).multiply(Utilities.addBiasNodeInRow(activationsOfFirstHiddenLayer)));
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(this.weightMatrixList.get(2).multiply(Utilities.addBiasNodeInRow(activationsOfSecondHiddenLayer)));
				logInfoToApplicationDisplay("Info: Size of the Second Hidden Layer activation matrix is: "+activationsOfSecondHiddenLayer.getRowDimension()+" X "+activationsOfSecondHiddenLayer.getColumnDimension());
				logInfoToApplicationDisplay("Info: Size of the Predicted Output Layer matrix is: "+predictedOutput.getRowDimension()+" X "+predictedOutput.getColumnDimension());
			}
			
			//If three hidden layers
			if(this.weightMatrixList.size() == 4){
				//Size = Number of nodes in second hidden layer  X  1
				activationsOfSecondHiddenLayer = Utilities.sigmoid(this.weightMatrixList.get(1).multiply(Utilities.addBiasNodeInRow(activationsOfFirstHiddenLayer)));
				//Size = Number of nodes in third hidden layer  X  1
				activationsOfThirdHiddenLayer = Utilities.sigmoid(this.weightMatrixList.get(2).multiply(Utilities.addBiasNodeInRow(activationsOfSecondHiddenLayer)));
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(this.weightMatrixList.get(3).multiply(Utilities.addBiasNodeInRow(activationsOfThirdHiddenLayer)));
				logInfoToApplicationDisplay("Info: Size of the Second Hidden Layer activation matrix is: "+activationsOfSecondHiddenLayer.getRowDimension()+" X "+activationsOfSecondHiddenLayer.getColumnDimension());
				logInfoToApplicationDisplay("Info: Size of the Third Hidden Layer activation matrix is: "+activationsOfThirdHiddenLayer.getRowDimension()+" X "+activationsOfThirdHiddenLayer.getColumnDimension());
				logInfoToApplicationDisplay("Info: Size of the Predicted Output Layer matrix is: "+predictedOutput.getRowDimension()+" X "+predictedOutput.getColumnDimension());
			}
			
			//Check the predictedOutput for the prediction and set it to the FeatureMessage
			//Assuming that first output node is for Forward, second output node is for right and third for left
			//System.out.println("Info: Following are the predicted weightages for forward, right and left - "+predictedOutput.getEntry(0, 0)+", "+predictedOutput.getEntry(1, 0)+", "+predictedOutput.getEntry(2, 0));
			if((predictedOutput.getEntry(0, 0) > predictedOutput.getEntry(1, 0)) && (predictedOutput.getEntry(0, 0) > predictedOutput.getEntry(2, 0))){
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Forward");
				returnFeatureMessage.setSteeringDirection(FeatureMessage.steerforward);
			}else if((predictedOutput.getEntry(1, 0) > predictedOutput.getEntry(0, 0)) && (predictedOutput.getEntry(1, 0) > predictedOutput.getEntry(2, 0))){
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Right");
				returnFeatureMessage.setSteeringDirection(FeatureMessage.steerRight);
			}else{
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Left");
				returnFeatureMessage.setSteeringDirection(FeatureMessage.steerLeft);
			}
			
		}catch (DimensionMismatchException e){
			logErrorToApplicationDisplay(e, "Error: Predict Steering Direction has failed");
			this.cancel();
		}
		
		return returnFeatureMessage;
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
	 * Proxy for the updatePredictionStatus function defined in DriverDisplayAndController
	 * @param currentStatus
	 */
	private void updatePredictionStatus(final String currentStatus){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updatePredictionStatus(currentStatus);
			}
		});
	}
	
	private void removePredictedTrainingDataSteeringDirection(){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayPredictedTrainingDataSteeringDirection("9", true);
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
}
