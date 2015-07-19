package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.swt.widgets.Display;

import com.vikas.projs.ml.autonomousvehicle.FeatureMessage;
import com.vikas.projs.ml.autonomousvehicle.PredictUsingNN;
import com.vikas.projs.ml.autonomousvehicle.Utilities;

public class TestJavaPredictionsAgainstOctave {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		//Test Bias
		double[][] matrixData= {{1,2,3},{4,5,6}};
		RealMatrix Testing = MatrixUtils.createRealMatrix(matrixData);
		RealMatrix withColumnBias = Utilities.addBiasNodeInColumn(Testing);
		RealMatrix withRowBias = Utilities.addBiasNodeInRow(Testing);
		
		String trainingFileName = "D:\\MyProjects\\Git\\AutonomousVehicle\\Code\\Octave\\Data\\Training\\AVC_TrainingData_2015-06-27_11-40-19_Resized-64-x-176.csv";
		String weight1FileName = "D:\\MyProjects\\Git\\AutonomousVehicle\\Code\\Octave\\NNWeights_Theta1.txt";
		String weight2FileName = "D:\\MyProjects\\Git\\AutonomousVehicle\\Code\\Octave\\NNWeights_Theta2.txt";
		String octavePredictionsFile = "D:\\MyProjects\\Git\\AutonomousVehicle\\Code\\Octave\\PredictedLabels.txt";
		
		Display display = new Display();
		String[] weightFileNames = {weight1FileName, weight2FileName};
		PredictUsingNN predictUsingNN = new PredictUsingNN(display, weightFileNames);
		
		BufferedReader brTrainingFile = new BufferedReader(new FileReader(trainingFileName));
		BufferedReader brOctavePredictionsFile = new BufferedReader(new FileReader(octavePredictionsFile));
		String trainingSet = null;
		int trainingFileLineNumber = 0;
		int octaveMismatchWithActual = 0;
		int javaMismatchWithOctave = 0;
		
		while((trainingSet = brTrainingFile.readLine()) != null){
			trainingFileLineNumber++;
			
			String[] columnValues = trainingSet.split(",");
			int[] framePixelData = new int[columnValues.length - 1];
			int actualSteeringDirection = -1;
			
			for(int i=0;i<columnValues.length;i++){
				if(i == (columnValues.length - 1)){
					actualSteeringDirection = Integer.valueOf(columnValues[i]);
				}else{
					framePixelData[i] = Integer.valueOf(columnValues[i]);
				}
			}
			
			int octavePredictedSteeringDirection = Double.valueOf(brOctavePredictionsFile.readLine()).intValue();
			
			FeatureMessage featureMessage = new FeatureMessage();
			featureMessage.setFramePixelDataInt(framePixelData);
			FeatureMessage returnedFeatureMessage = predictUsingNN.predictSteeringDirection(featureMessage);
			int javaPredictedSteeringDirection = Integer.valueOf(returnedFeatureMessage.getSteeringDirection());
			
			if(actualSteeringDirection != octavePredictedSteeringDirection){
				octaveMismatchWithActual++;
			}
			if(octavePredictedSteeringDirection != javaPredictedSteeringDirection){
				javaMismatchWithOctave++;
			}
			
			System.out.println("Line Number "+trainingFileLineNumber+" ActualSteeringDirection = "+actualSteeringDirection+" OctavePrediction = "+octavePredictedSteeringDirection+" JavaPrediction = "+javaPredictedSteeringDirection);
			
		}
		
		System.out.println("Number of times octave predictions were wrong = "+octaveMismatchWithActual+" out of "+trainingFileLineNumber);
		System.out.println("Number of mismatches between octave and java predictions ="+javaMismatchWithOctave);
		
		brTrainingFile.close();
		brOctavePredictionsFile.close();

		predictUsingNN.cancel();
	}

}
