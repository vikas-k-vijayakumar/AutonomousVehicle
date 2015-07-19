package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.vikas.projs.ml.autonomousvehicle.PersistTrainingData;

public class TestingDataFormat {
	
	public static void main(String[] args){
		
		DataOutputStream dOutStream;
		DataInputStream dInStream;
		Socket socket;
		String sensorIPV4Address;
		int sensorPort;
		Display display;
		org.eclipse.swt.graphics.ImageData grayscaleFrameImageData;
		
		RGB[] rgbGrayscale = new RGB[256];
		PaletteData paletteDataGrayscale;
		
		//Build grey scale palette: 256 different grey values are generated. 
		for (int i = 0; i < 256; i++) {
		    rgbGrayscale[i] = new RGB(i, i, i);
		}
		//Construct a new indexed palette given an array of Grayscale RGB values.
		paletteDataGrayscale = new PaletteData(rgbGrayscale);
		
		try {
			socket = new Socket("172.20.10.3", 6666);
			dOutStream = new DataOutputStream(socket.getOutputStream());
			
			if(dOutStream != null){
				dOutStream.writeUTF(Inet4Address.getLocalHost().getHostAddress());
				dOutStream.flush();
			}			
			
			dInStream = new DataInputStream(socket.getInputStream());
			String sensorHandshake = dInStream.readUTF();
			
			while(true){
				try {
					int frameWidth = dInStream.readInt();
					int frameHeight = dInStream.readInt();
					int frameDepth = dInStream.readInt();
					int framePixelCount = frameWidth * frameHeight;
					byte[] grayscaleFrameData = new byte[framePixelCount];
					
					//Keep reading until we get framePixelCount worth data
					int readPixels = 0;
					while(readPixels < framePixelCount){
						readPixels = readPixels + dInStream.read(grayscaleFrameData, readPixels, framePixelCount - readPixels);
						System.out.println("readPixels");
					}	
					
					grayscaleFrameImageData = new ImageData(frameWidth, frameHeight, frameDepth, paletteDataGrayscale);
					//Assign ImageData
					grayscaleFrameImageData.data = grayscaleFrameData;
					
					int[] integerPixels = byteToInt(grayscaleFrameData);

					System.out.println("Done");
					
				} catch (IOException e) {
					System.out.println("IOException");
				} 
			}
			
		} catch (NumberFormatException e) {			
			System.out.println("NumberFormatException");
		} catch (UnknownHostException e) {			
			System.out.println("UnknownHostException");
		} catch (IOException e) {			
			System.out.println("IOException");
		}

		
	}

	private static int[] byteToInt(byte[] data) {
	    int[] ints = new int[data.length];
	    for (int i = 0; i < data.length; i++) {
	        ints[i] = (int) data[i] & 0xff;
	    }
	    return ints;
	} 
	
}
