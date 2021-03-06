package com.vikas.projs.ml.autonomousvehicle;

/**
 * This class defines the structure of a feature message
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class FeatureMessage {

	private int frameWidth;
	private int frameHeight;
	private int pixelDepth;
	private byte[] framePixelData;
	private String steeringDirection;
	private float[] framePixelDataFloat;
	private int steeringPredictionConfidence;
	
	public static final String steerRight = "2";
	public static final String steerLeft = "3";
	public static final String steerforward = "1";
	public static final String steerReverse = "4";
	
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
	public float[] getFramePixelDataFloat() {
		return framePixelDataFloat;
	}
	public void setFramePixelDataFloat(float[] framePixelDataFloat) {
		this.framePixelDataFloat = framePixelDataFloat;
	}
	public int getSteeringPredictionConfidence() {
		return steeringPredictionConfidence;
	}
	public void setSteeringPredictionConfidence(int steeringPredictionConfidence) {
		this.steeringPredictionConfidence = steeringPredictionConfidence;
	}
}
