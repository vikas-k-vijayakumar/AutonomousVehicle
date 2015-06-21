package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class TestingRandomAccessFile {
	public static void main(String args[]){
		try{
			RandomAccessFile fileStore = new RandomAccessFile("D:\\MyProjects\\AutonomousVehicle\\PrivateMaterial\\TestingRandomFile.csv", "rw");
			fileStore.seek(2);
			fileStore.writeChars("Vikas");
			fileStore.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	
}