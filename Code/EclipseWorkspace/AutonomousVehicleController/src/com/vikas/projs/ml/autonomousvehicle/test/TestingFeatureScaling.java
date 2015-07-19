package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.IOException;

import com.vikas.projs.ml.autonomousvehicle.Utilities;

public class TestingFeatureScaling {

	public static void main(String[] args) {
		try {
			Utilities.performFeatureScalingForAllDataSets("D://Temp//AVC_TrainingData_2015-06-27_11-40-19_Resized-64-x-176.csv", "D://Temp//AVC_TrainingData_2015-06-27_11-40-19_Resized-64-x-176_scaled.csv", "D://Temp//Mean.txt", "D://Temp//StandardDeviation.txt");
		} catch (IOException e) {
			e.printStackTrace();
			
		}

	}

}
