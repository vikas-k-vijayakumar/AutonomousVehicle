package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.swt.widgets.Display;

public class PredictUsingNN implements Runnable{

	private ArrayBlockingQueue<FeatureMessage> predictQueue;
	private Thread predictSteeringDirectionThread;
	private Display display;
	private ArrayList<RealMatrix> weightMatrixList;
	
	public PredictUsingNN(ArrayBlockingQueue<FeatureMessage> predictQueue, Display display, String[] weightFileNames) throws FileNotFoundException, IOException{
		this.predictQueue = predictQueue;
		this.display = display;
		weightMatrixList =  new ArrayList<RealMatrix>();
		
		//Read weights from the file and add to ArrayList
		for(int i=0;i<weightFileNames.length;i++){
			BufferedReader br = new BufferedReader(new FileReader(weightFileNames[i]));
			String line = null;
			int noOfLinesInFile = 0;
			//Find number of lines in the file
			while((line = br.readLine()) != null ){
				noOfLinesInFile++;
			}
			br.close();
			
			if(noOfLinesInFile > 0){
				BufferedReader br1 = new BufferedReader(new FileReader(weightFileNames[i]));
				RealMatrix realMatrix = null;
				while((line = br1.readLine()) != null ){
					String[] weights = line.split(" ");
					int noOfWeights = weights.length;
					//Create RealMatrix. 
					//Set Number of rows to number of lines in file. 
					//Set Number of columns to number of entries in each line

					realMatrix = MatrixUtils.createRealMatrix(noOfLinesInFile,noOfWeights);
					for(int j=0;j<weights.length;j++){
						realMatrix.setEntry(i, j, Double.valueOf(weights[j]));
					}
				}
				weightMatrixList.add(realMatrix);
				br1.close();
				logInfoToApplicationDisplay("Info: Successfully read weights from the following file - "+weightFileNames[i]);
				logInfoToApplicationDisplay("Info: into a matrix of size "+realMatrix.getRowDimension()+" X "+realMatrix.getColumnDimension());
			}
		}

		//Create and start a Thread
		predictSteeringDirectionThread = new Thread(this);
		predictSteeringDirectionThread.start();
		
	}
	
	@Override
	public void run() {
		//Until interrupted, do nothing
		while(!predictSteeringDirectionThread.isInterrupted()){
			//Do nothing
		}
	}
	
	private void cancel(){
		logInfoToApplicationDisplay("Info: Current Predict Steering Direction thread will be interuppted");
		predictSteeringDirectionThread.interrupt();
	}
	
	/**
	 * Predicts the Steering direction based on the feature
	 * @param FeatureMessage
	 * @return FeatureMessage
	 */
	protected FeatureMessage predictSteeringDirection(FeatureMessage currentFeatureList){

		try {						
			//The Auto mode will send the images in the form of byte array whereas
			//training review mode will send the images in the form of int array
			int[] framePixelData = null;
			if(currentFeatureList.getFramePixelDataInt() != null){
				framePixelData = currentFeatureList.getFramePixelDataInt();
			}else{
				framePixelData = PersistTrainingData.byteToInt(currentFeatureList.getFramePixelData());
			}
			
			//Create input layer activations, add the bias unit as well
			RealMatrix activationsOfInputLayer = MatrixUtils.createRealMatrix(1,framePixelData.length+1);
			activationsOfInputLayer.setEntry(0, 0, Double.valueOf(1));
			for(int i=0;i<framePixelData.length;i++){
				activationsOfInputLayer.setEntry(0, i+1, Double.valueOf(i));
			}
			
			//Start calculating the activations for other layers
			RealMatrix activationsOfFirstHiddenLayer = null;
			RealMatrix activationsOfSecondHiddenLayer = null;
			RealMatrix activationsOfThirdHiddenLayer = null;
			RealMatrix predictedOutput = null;

			//Size = Number of nodes in first hidden layer  X  1
			activationsOfFirstHiddenLayer = Utilities.sigmoid(weightMatrixList.get(0).multiply(activationsOfInputLayer.transpose()));

			//If one hidden layer
			if(weightMatrixList.size() == 2){
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(weightMatrixList.get(1).multiply(Utilities.addBiasNode(activationsOfFirstHiddenLayer)));
			}
			
			//If two hidden layers
			if(weightMatrixList.size() == 3){
				//Size = Number of nodes in second hidden layer  X  1
				activationsOfSecondHiddenLayer = Utilities.sigmoid(weightMatrixList.get(1).multiply(Utilities.addBiasNode(activationsOfFirstHiddenLayer)));
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(weightMatrixList.get(2).multiply(Utilities.addBiasNode(activationsOfSecondHiddenLayer)));
			}
			
			//If three hidden layers
			if(weightMatrixList.size() == 4){
				//Size = Number of nodes in second hidden layer  X  1
				activationsOfSecondHiddenLayer = Utilities.sigmoid(weightMatrixList.get(1).multiply(Utilities.addBiasNode(activationsOfFirstHiddenLayer)));
				//Size = Number of nodes in third hidden layer  X  1
				activationsOfThirdHiddenLayer = Utilities.sigmoid(weightMatrixList.get(2).multiply(Utilities.addBiasNode(activationsOfSecondHiddenLayer)));
				//Size = Number of output nodes X 1
				predictedOutput = Utilities.sigmoid(weightMatrixList.get(3).multiply(Utilities.addBiasNode(activationsOfThirdHiddenLayer)));
			}
			
			//Check the predictedOutput for the prediction and set it to the FeatureMessage
			//Assuming that first output node is for Forward, second output node is for right and third for left
			if((predictedOutput.getEntry(0, 0) > predictedOutput.getEntry(1, 0)) && (predictedOutput.getEntry(0, 0) > predictedOutput.getEntry(2, 0))){
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Forward");
				currentFeatureList.setSteeringDirection(FeatureMessage.steerforward);
			}else if((predictedOutput.getEntry(1, 0) > predictedOutput.getEntry(0, 0)) && (predictedOutput.getEntry(1, 0) > predictedOutput.getEntry(2, 0))){
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Right");
				currentFeatureList.setSteeringDirection(FeatureMessage.steerRight);
			}else{
				logInfoToApplicationDisplay("Info: Steering Prediction is Steer Left");
				currentFeatureList.setSteeringDirection(FeatureMessage.steerLeft);
			}
			
		}catch (DimensionMismatchException e){
			logInfoToApplicationDisplay("Error: Predict Steering Direction has failed");
			this.cancel();
		}
		
		return currentFeatureList;
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

}
