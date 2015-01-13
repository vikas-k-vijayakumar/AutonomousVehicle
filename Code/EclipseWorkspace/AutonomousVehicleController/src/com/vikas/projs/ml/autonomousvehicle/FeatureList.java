package com.vikas.projs.ml.autonomousvehicle;

/**
 * This class defines a feature
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class FeatureList {

	private int frameWidth;
	private int frameHeight;
	private int pixelDepth;
	private byte[] framePixelData;
	private String steeringDirection;
	
	public static final String steerRight = "2";
	public static final String steerLeft = "3";
	public static final String steerforward = "0";
	public static final String steerReverse = "1";
	
	public int getFrameWidth() {
		return frameWidth;
	}
	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}
	public int getFrameHeight() {
		return frameHeight;
	}
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}
	public byte[] getFramePixelData() {
		return framePixelData;
	}
	public void setFramePixelData(byte[] framePixelData) {
		this.framePixelData = framePixelData;
	}
	public String getSteeringDirection() {
		return steeringDirection;
	}
	public void setSteeringDirection(String steeringDirection) {
		this.steeringDirection = steeringDirection;
	}
	public int getPixelDepth() {
		return pixelDepth;
	}
	public void setPixelDepth(int pixelDepth) {
		this.pixelDepth = pixelDepth;
	}
}
