package com.vikas.projs.ml.autonomousvehicle;

/**
 * Created by Vikas_Vijayakumar on 12/28/2014.
 */
public class SensorData {

    private int frameWidth;
    private int frameHeight;
    private int pixelDepth;
    private byte[] pixelData;

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

    public int getPixelDepth() {
        return pixelDepth;
    }

    public void setPixelDepth(int pixelDepth) {
        this.pixelDepth = pixelDepth;
    }

    public byte[] getPixelData() {
        return pixelData;
    }

    public void setPixelData(byte[] pixelData) {
        this.pixelData = pixelData;
    }
}
