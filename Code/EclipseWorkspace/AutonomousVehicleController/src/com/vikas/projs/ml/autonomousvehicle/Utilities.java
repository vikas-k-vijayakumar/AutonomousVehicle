package com.vikas.projs.ml.autonomousvehicle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Generic Utilities
 * @author Vikas_Vijayakumar
 *
 */
public class Utilities {
	
	/**
	 * Used to validate if a value is a Integer, additionally if its between a certain inclusive range
	 * @param integerValue
	 * @param limitFrom
	 * @param limitTo
	 * @return
	 */
	public static Boolean validateInteger(String integerValue, int limitFrom, int limitTo){
		try{
			int value = Integer.valueOf(integerValue);
			if((value >= limitFrom) && (value <= limitTo)){
				return true;
			}else{
				return false;
			}
		} catch(NumberFormatException e){
			return false;
		}
	}
	
	/**
	 * Used to validate an IP Address
	 * @param ipAddress
	 * @return
	 */
	public static Boolean validateIPv4Address(String ipAddress){
		final String ipV4PATTERN = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	    Pattern pattern = Pattern.compile(ipV4PATTERN);
	    Matcher matcher = pattern.matcher(ipAddress);
	    return matcher.matches();
	}
	
	/**
	 * Resize a training image
	 * 
	 * @param trainingFileName
	 * @param currentFrameHeight
	 * @param currentFrameWidth
	 * @param pixelRowsToStripFromTop
	 * @param pixelRowsToStripFromBottom
	 * @throws IOException
	 */
	public static void resizeTrainingImage(String trainingFileName, int currentFrameHeight, int currentFrameWidth, int pixelRowsToStripFromTop, int pixelRowsToStripFromBottom) throws IOException{
		
		String trainingFileColumnSeperator = ",";
		File trainingFile = new File(trainingFileName);
		BufferedReader br = new BufferedReader(new FileReader(trainingFile));
		
		File resizedFile = new File(trainingFileName+".tmp");
		FileWriter fWriter = new FileWriter(resizedFile.getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		resizedFile.createNewFile();
		
		String dataSet = null;
		while((dataSet = br.readLine()) != null ){
			
			//Calculate the pixels to be removed based on the number of pixels rows to be removed from top and bottom
			if((pixelRowsToStripFromTop + pixelRowsToStripFromBottom) >= currentFrameHeight){
				pixelRowsToStripFromTop = currentFrameHeight;
				pixelRowsToStripFromBottom = 0;
			}
			int noOfPixelsToSkipAtStart = currentFrameWidth * pixelRowsToStripFromTop;
			int noOfPixelsToSkipAtEnd = currentFrameWidth * pixelRowsToStripFromBottom;
			int pixelNumberToSkipFromAtEnd = (currentFrameWidth * currentFrameHeight) - noOfPixelsToSkipAtEnd;
			
			Boolean skipPixels = false;
			if((noOfPixelsToSkipAtStart > 0) || (noOfPixelsToSkipAtEnd > 0)){
				skipPixels = true;
			}
			int updatedFrameHeight = currentFrameHeight - (pixelRowsToStripFromTop + pixelRowsToStripFromBottom);
			
			//Write every pixel value after converting the byte array into an integer array
			String[] stringPixelData = dataSet.split(",");
			for(int k=0;k<stringPixelData.length-1;k++){
				if(skipPixels){
					if((k < noOfPixelsToSkipAtStart) || (k >= pixelNumberToSkipFromAtEnd)){
						//Skip
					}else{
						bWriter.write(stringPixelData[k]);
						bWriter.write(trainingFileColumnSeperator);
					}
				}else{
					bWriter.write(stringPixelData[k]);
					bWriter.write(trainingFileColumnSeperator);
				}
			}			
			//Write the Steering direction
			bWriter.write(stringPixelData[stringPixelData.length-1]);
			bWriter.newLine();
			
		}
		bWriter.flush();
		fWriter.close();
		bWriter.close();
		
		br.close();
		
		trainingFile.delete();
		resizedFile.renameTo(new File(trainingFileName));
	}
	
	/**
	 * Applies the sigmoid function on each element of the matrix
	 * @param RealMatrix
	 * @return Sigmoid of the provided matrix
	 */
	public static RealMatrix sigmoid(RealMatrix z){
		// g = 1.0 ./ (1.0 + exp(-z));
		RealMatrix m = z.copy();
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				double y = m.getEntry(i, j);
				double g = 1.0 / (1.0 + Math.exp(-y));
				m.setEntry(i, j, g);
			}
		}
		return m;
	}

	/**
	 * Adds the bias node as the first column
	 * @param RealMatrix
	 * @return Matrix with added bias column
	 */
	public static RealMatrix addBiasNodeInColumn(RealMatrix z){
		RealMatrix m = MatrixUtils.createRealMatrix(z.getRowDimension(), z.getColumnDimension()+1);
		for (int i = 0; i < z.getRowDimension(); i++) {
			//Add Bias Unit
			m.setEntry(i, 0, 1);
			for (int j = 0; j < z.getColumnDimension(); j++) {
				m.setEntry(i, j+1, z.getEntry(i, j));
			}
		}
		return m;
	}
	
	/**
	 * Adds the bias node as the first row
	 * @param RealMatrix
	 * @return Matrix with added bias row
	 */
	public static RealMatrix addBiasNodeInRow(RealMatrix z){
		RealMatrix m = MatrixUtils.createRealMatrix(z.getRowDimension()+1, z.getColumnDimension());
		for (int i = 0; i < z.getColumnDimension(); i++) {
			//Add Bias Unit
			m.setEntry(0, i, 1);
			for (int j = 0; j < z.getRowDimension(); j++) {
				m.setEntry(j+1, i, z.getEntry(j, i));
			}
		}
		return m;
	}
	
	/**
	 * This method converts a Byte array into Int array
	 * @param data
	 * @return
	 */
	public static int[] byteToInt(byte[] data) {
	    int[] ints = new int[data.length];
	    for (int i = 0; i < data.length; i++) {
	        ints[i] = (int) data[i] & 0xff;
	    }
	    return ints;
	} 
	
	/**
	 * This method converts a Byte array into Float array
	 * @param data
	 * @return
	 */
	public static float[] byteToFloat(byte[] data) {
	    int[] ints = new int[data.length];
	    float[] flts = new float[data.length];
	    for (int i = 0; i < data.length; i++) {
	        ints[i] = (int) data[i] & 0xff;
	        flts[i] = Float.valueOf(ints[i]);
	    }
	    return flts;
	} 
	
	/**
	 * Scales the image pixel value features using mean normalization for all datasets in the training file
	 * 
	 * @param trainingFileName
	 * @param featureScaledFileName
	 * @param meanFileName
	 * @param standardDeviationFileName
	 * @throws IOException
	 */
	public static void performFeatureScalingForAllDataSets(String trainingFileName, String featureScaledFileName, String meanFileName, String standardDeviationFileName) throws IOException{
		
		String trainingFileColumnSeperator = ",";
		File trainingFile = new File(trainingFileName);
		BufferedReader br = new BufferedReader(new FileReader(trainingFile));
		
		File rescaledFile = new File(featureScaledFileName);
		FileWriter fWriter = new FileWriter(rescaledFile.getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		rescaledFile.createNewFile();
		
		File meanFile = new File(meanFileName);
		FileWriter meanfWriter = new FileWriter(meanFile.getAbsoluteFile());
		BufferedWriter meanbWriter = new BufferedWriter(meanfWriter);
		meanFile.createNewFile();
		
		File sdFile = new File(standardDeviationFileName);
		FileWriter sdfWriter = new FileWriter(sdFile.getAbsoluteFile());
		BufferedWriter sdbWriter = new BufferedWriter(sdfWriter);
		sdFile.createNewFile();
				
		//Calculate the number of features
		String dataSet = null;
		int numberOfFeatures=0;
		String[] stringPixelData = null;
		if((dataSet = br.readLine()) != null ){
			stringPixelData = dataSet.split(",");
			//Ignore the last entry which is for steering direction
			numberOfFeatures = stringPixelData.length - 1;
		}else{
			bWriter.flush();
			fWriter.close();
			bWriter.close();
			meanfWriter.close();
			meanbWriter.close();
			sdfWriter.close();
			sdbWriter.close();
			br.close();
			return;
		}
		
		//Calculate the mean for each feature
		int numberOfDataSets=0;
		float[] sumOfFeatureValues = new float[numberOfFeatures];
		float[] meanOfFeatureValue = new float[numberOfFeatures];
		for(int i=0;i<stringPixelData.length-1;i++){
			sumOfFeatureValues[i] = Float.valueOf(stringPixelData[i]);
		}
		numberOfDataSets++;
		while((dataSet = br.readLine()) != null ){
			numberOfDataSets++;
			stringPixelData = dataSet.split(",");
			for(int i=0;i<stringPixelData.length-1;i++){
				sumOfFeatureValues[i] = sumOfFeatureValues[i] + Float.valueOf(stringPixelData[i]);
			}
		}
		for(int i=0;i<sumOfFeatureValues.length;i++){
			meanOfFeatureValue[i] = sumOfFeatureValues[i] / Float.valueOf(numberOfDataSets);
			//System.out.println("meanOfFeatureValue["+i+"]"+" = "+meanOfFeatureValue[i]);
			meanbWriter.write(String.valueOf(meanOfFeatureValue[i]));
			if(i < (sumOfFeatureValues.length - 1)){
				meanbWriter.write(trainingFileColumnSeperator);
			}			
		}
		br.close();
		
		//Calculate the Standard Deviation for each feature
		float[] sumOfSquaredDeviationValues = new float[numberOfFeatures];
		float[] standardDeviationValues = new float[numberOfFeatures];
		br = new BufferedReader(new FileReader(trainingFile));
		while((dataSet = br.readLine()) != null ){
			stringPixelData = dataSet.split(",");
			for(int i=0;i<stringPixelData.length-1;i++){
				sumOfSquaredDeviationValues[i] = sumOfSquaredDeviationValues[i] + (Float.valueOf(stringPixelData[i]) - meanOfFeatureValue[i]) * (Float.valueOf(stringPixelData[i]) - meanOfFeatureValue[i]);
			}
		}
		for(int i=0;i<sumOfSquaredDeviationValues.length;i++){
			standardDeviationValues[i] = (float) Math.sqrt(sumOfSquaredDeviationValues[i] / Float.valueOf(numberOfDataSets));
			//System.out.println("sumOfSquaredDeviationValues["+i+"]"+" = "+sumOfSquaredDeviationValues[i]);
			//System.out.println("standardDeviationValues["+i+"]"+" = "+standardDeviationValues[i]);
			sdbWriter.write(String.valueOf(standardDeviationValues[i]));
			if(i < (sumOfSquaredDeviationValues.length - 1)){
				sdbWriter.write(trainingFileColumnSeperator);
			}	
		}
		br.close();
		
		//Scale the feature values. New feature value = (current feature value - mean value) / standard deviation
		br = new BufferedReader(new FileReader(trainingFile));
		while((dataSet = br.readLine()) != null ){
			stringPixelData = dataSet.split(",");
			//Scale each feature image pixel value
			for(int k=0;k<stringPixelData.length-1;k++){
				bWriter.write(String.valueOf(((Float.valueOf(stringPixelData[k]) - meanOfFeatureValue[k]) / standardDeviationValues[k])));
				bWriter.write(trainingFileColumnSeperator);
			}			
			//Write the Steering direction
			bWriter.write(stringPixelData[stringPixelData.length-1]);
			bWriter.newLine();
		}
		
		bWriter.flush();
		meanbWriter.flush();
		sdbWriter.flush();
		
		fWriter.close();
		bWriter.close();
		meanfWriter.close();
		meanbWriter.close();
		sdfWriter.close();
		sdbWriter.close();
		
		br.close();		
	}
}
