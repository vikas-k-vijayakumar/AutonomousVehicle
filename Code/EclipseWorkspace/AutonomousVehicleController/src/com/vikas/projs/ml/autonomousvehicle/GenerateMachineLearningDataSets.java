package com.vikas.projs.ml.autonomousvehicle;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.swt.widgets.Display;

/**
 * Used to generate Training, Cross Validation and Testing data files from the captured data
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class GenerateMachineLearningDataSets {

	private ArrayList<File> capturedDataFileList =  new ArrayList<File>();
	private Display display;
	
	public void createDataFiles(String capturedDataDirectoryName, Display display, int percentInTrainingFile, int percentInCrossValidationFile, int percentInTestingFile){
		this.display = display;
		//Get all csv files under the directory
		getAllCSVFiles(capturedDataDirectoryName, capturedDataFileList);
		logInfoToApplicationDisplay("Total number of files to process = "+capturedDataFileList.size());
		
		if (capturedDataFileList.size() != 0){
			//Create Training, CrossValidation and Testing data file
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
			String currentDate = dateFormat.format(new Date());
			String trainingDataFileName = "AVC_TrainingData_"+currentDate.substring(0, currentDate.indexOf(" "))+"_"+currentDate.substring(currentDate.indexOf(" ")+1,currentDate.length())+".csv";
			String crossValidationDataFileName = "AVC_CrossValidationData_"+currentDate.substring(0, currentDate.indexOf(" "))+"_"+currentDate.substring(currentDate.indexOf(" ")+1,currentDate.length())+".csv";
			String testingDataFileName = "AVC_TestingData_"+currentDate.substring(0, currentDate.indexOf(" "))+"_"+currentDate.substring(currentDate.indexOf(" ")+1,currentDate.length())+".csv";
			

			try {	
				File trainingFile = new File(capturedDataDirectoryName+"/"+trainingDataFileName);		
				FileWriter fWriterTraining;
				BufferedWriter bWriterTraining;
				trainingFile.createNewFile();
				fWriterTraining = new FileWriter(trainingFile.getAbsoluteFile());
				bWriterTraining = new BufferedWriter(fWriterTraining);
				
				File cvFile = new File(capturedDataDirectoryName+"/"+crossValidationDataFileName);		
				FileWriter fWriterCV;
				BufferedWriter bWriterCV;
				cvFile.createNewFile();
				fWriterCV = new FileWriter(cvFile.getAbsoluteFile());
				bWriterCV = new BufferedWriter(fWriterCV);
				
				File testingFile = new File(capturedDataDirectoryName+"/"+testingDataFileName);		
				FileWriter fWriterTesting;
				BufferedWriter bWriterTesting;
				testingFile.createNewFile();
				fWriterTesting = new FileWriter(testingFile.getAbsoluteFile());
				bWriterTesting = new BufferedWriter(fWriterTesting);
				
				//Read all files. Store each line of the file using a random key into a hashmap. 
				int fileCount=1;
				int noOfDatasets=0;
				HashMap<Integer, String> featureHash = new HashMap<>();
				Random randomIntGenerator = new Random();
				for (File csvFile : capturedDataFileList){
					BufferedReader br = new BufferedReader(new FileReader(csvFile));
					String dataSet = null;
					
					while((dataSet = br.readLine()) != null ){
						//Write one line into hashmap
						if(dataSet != null){
							//Generate a random key and ensure that is not already present in hashmap
							int key = randomIntGenerator.nextInt(100000);
							while(true){
								if(featureHash.get(key) != null){
									key = randomIntGenerator.nextInt(100000);
								}else{
									break;
								}
							}
							featureHash.put(key, dataSet);
							noOfDatasets++;
						}
					}
					logInfoToApplicationDisplay("Successfully finished processing file number "+fileCount+" out of a total of "+capturedDataFileList.size());
					fileCount++;
					br.close();
				}
				
				//Distribute the contents of the hashmap into the various files based on the provided percentages
				int noOfDatasetsInTestingFile = (int) Math.floor((noOfDatasets / 100) * percentInTestingFile);
				int noOfDatasetsInCVFile = (int) Math.floor((noOfDatasets / 100) * percentInCrossValidationFile);
				int noOfDatasetsInTrainingFile = noOfDatasets - (noOfDatasetsInTestingFile + noOfDatasetsInCVFile);
				Iterator<String> featureIterator = featureHash.values().iterator();
				int noOfLinesProcessed=0;
				while(featureIterator.hasNext()){
					noOfLinesProcessed++;
					if(noOfLinesProcessed < noOfDatasetsInTestingFile){
						bWriterTesting.write(featureIterator.next());
						bWriterTesting.newLine();
					}else if(noOfLinesProcessed < (noOfDatasetsInTestingFile + noOfDatasetsInCVFile)){
						bWriterCV.write(featureIterator.next());
						bWriterCV.newLine();
					}else{
						bWriterTraining.write(featureIterator.next());
						bWriterTraining.newLine();
					}

				}
				logInfoToApplicationDisplay("Finished distributing "+noOfDatasetsInTestingFile+"datasets into Testing file, "+noOfDatasetsInCVFile+""
						+ " datasets into Cross Validation file and "+noOfDatasetsInTrainingFile+" into Training file from a total of "+noOfDatasets+" datasets");
				
				//Open files one by one and distribute the training sets amongst Training, Cross Validation
				//and Testing data file
				/*int fileCount=1;
				for (File csvFile : capturedDataFileList){
					BufferedReader br = new BufferedReader(new FileReader(csvFile));
					
					String dataSet = null;
					while((dataSet = br.readLine()) != null ){
						//Write one line into Training data file
						if(dataSet != null){
							bWriterTraining.write(dataSet);
							bWriterTraining.newLine();
							//Write one line into CrossValidation data file
							dataSet = br.readLine();
							if(dataSet != null){
								bWriterCV.write(dataSet);
								bWriterCV.newLine();
								//Write one line into Testing data file
								dataSet = br.readLine();
								if(dataSet != null){
									bWriterTesting.write(dataSet);
									bWriterTesting.newLine();
								}
							}
						}
					}
					
					logInfoToApplicationDisplay("Successfully finished processing file number "+fileCount+" out of a total of "+capturedDataFileList.size());
					br.close();
					fileCount++;
				}*/
				bWriterTraining.flush();
				bWriterCV.flush();
				bWriterTesting.flush();
				
				fWriterTraining.close();
				fWriterCV.close();
				fWriterTesting.close();
				bWriterTraining.close();
				bWriterCV.close();
				bWriterTesting.close();
				
				displayInfoMessageOnscreen("Successfully finished processing a total of "+capturedDataFileList.size()+" files");
			} catch (IOException e1) {
				logErrorToApplicationDisplay(e1, "ERROR: when trying to create or read from or write to file");
			}				

		}else{
			displayErrorMessageOnscreen("No captured data files found in the provided directory");
		}
				
	}
	
	public void getAllCSVFiles(String topLevelDirectory, ArrayList<File> files){
		File directory = new File(topLevelDirectory);
		
	    // get all csv files in the directory and its sub directories at all levels
	    File[] fileList = directory.listFiles();
	    if(fileList != null){
		    for (File file : fileList) {
		        if (file.isFile()) {
		        	if(file.getName().endsWith(".csv")){
		        		files.add(file);
		        	}	            
		        } else if (file.isDirectory()) {
		        	getAllCSVFiles(file.getAbsolutePath(), files);
		        }
		    }
	    }
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
	
	private void displayInfoMessageOnscreen(final String message){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayInfoMessageOnscreen(message);
			}
		});
	}
	
	private void displayErrorMessageOnscreen(final String message){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.displayErrorMessageOnscreen(message);
			}
		});
	}
}
