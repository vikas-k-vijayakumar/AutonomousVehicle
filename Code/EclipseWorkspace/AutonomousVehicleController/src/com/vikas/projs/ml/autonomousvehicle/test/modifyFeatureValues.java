package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.vikas.projs.ml.autonomousvehicle.GenerateMachineLearningDataSets;

public class modifyFeatureValues {

	public static void main(String[] args) {
		ArrayList<File> capturedDataFileList =  new ArrayList<File>();
		GenerateMachineLearningDataSets gmd = new GenerateMachineLearningDataSets();
		String baseDirectory = "D:\\MyProjects\\Git\\AutonomousVehicle\\Code\\Octave\\Data\\Captured\\Original";
		gmd.getAllCSVFiles(baseDirectory, capturedDataFileList);

		
		if (capturedDataFileList.size() != 0){
			try {	
				
				int fileCount=1;
				for (File csvFile : capturedDataFileList){
					BufferedReader br = new BufferedReader(new FileReader(csvFile));
					
					File tempFile = new File(csvFile.getAbsolutePath()+".tmp");		
					FileWriter fWriterTraining;
					BufferedWriter bWriterTraining;
					tempFile.createNewFile();
					fWriterTraining = new FileWriter(tempFile.getAbsoluteFile());
					bWriterTraining = new BufferedWriter(fWriterTraining);
					
					System.out.println("Starting to process the following files: "+csvFile.getAbsolutePath());
					String dataSet = null;
					while((dataSet = br.readLine()) != null ){
						//Write one line into Training data file
						if(dataSet != null){
							String[] features = dataSet.split(",");
							for(int k=0;k<features.length;k++){
								if(k == (features.length - 1)){
									if(Integer.valueOf(features[k]) == 0){
										bWriterTraining.write("1");
									}else{
										bWriterTraining.write(features[k]);
									}
								}else{
									bWriterTraining.write(features[k]);
									bWriterTraining.write(",");
								}
								
							}
							
							bWriterTraining.newLine();
						}
					}
					bWriterTraining.flush();
					br.close();
					//fWriterTraining.close();
					bWriterTraining.close();
					fileCount++;
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}				

		}else{
			System.out.println("No captured data files found in the provided directory");
		}

		
	}

}
