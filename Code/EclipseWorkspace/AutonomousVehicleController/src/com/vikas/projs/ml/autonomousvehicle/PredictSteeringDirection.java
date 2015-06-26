package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.swt.widgets.Display;

public class PredictSteeringDirection implements Runnable{

	private ArrayBlockingQueue<FeatureMessage> predictQueue;
	private Thread predictSteeringDirectionThread;
	private String trainingDataDirectory;
	private Display display;
	private String trainingFileName;
	private File trainingFile;
	private String trainingFileColumnSeperator = ",";
	private int pixelRowsToStripFromTop;
	private int pixelRowsToStripFromBottom;
	private ArrayList<RealMatrix> weightMatrixList;
	
	public PredictSteeringDirection(ArrayBlockingQueue<FeatureMessage> predictQueue, Display display, String[] weightFileNames) throws FileNotFoundException, IOException{
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
		
		
		//Until interrupted, continuously poll for FeatureLists from the BlockingQueue
		//In case there are none in queue, the thread will block in anticipation of a new message
		while(!predictSteeringDirectionThread.isInterrupted()){
			try {
				FeatureMessage currentFeatureList = predictQueue.take();
								
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
				
				//Check the predictedOutput for the prediction
				
			}catch (InterruptedException e) {
				logInfoToApplicationDisplay("Info: Predict Steering Direction thread has been interrupted, will not perform predictions anymore");
				this.cancel();
			}catch (DimensionMismatchException e){
				logInfoToApplicationDisplay("Error: Matrix operation failed");
				this.cancel();
			}
		}
	}
	
	private void cancel(){
		logInfoToApplicationDisplay("Info: Current Predict Steering Direction thread will be interuppted");
		predictSteeringDirectionThread.interrupt();
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

}
