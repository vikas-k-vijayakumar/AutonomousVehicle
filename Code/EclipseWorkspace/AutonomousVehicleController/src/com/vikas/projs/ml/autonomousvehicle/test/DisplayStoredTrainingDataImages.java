package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;

import com.vikas.projs.ml.autonomousvehicle.FeatureMessage;

public class DisplayStoredTrainingDataImages {

	protected Shell shell;
	private static RGB[] rgbGrayscale = new RGB[256];
	private static PaletteData paletteDataGrayscale;
	private static Display display;
	private static Label lblNewLabel;
	int imageNumber=1;
	private Button btnPreviousImage;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DisplayStoredTrainingDataImages window = new DisplayStoredTrainingDataImages();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		
		shell.open();			
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(495, 374);
		shell.setText("SWT Application");
		shell.setLayout(new GridLayout(8, false));
		
		//Build grey scale palette: 256 different grey values are generated. 
		for (int i = 0; i < 256; i++) {
		    rgbGrayscale[i] = new RGB(i, i, i);
		}
		//Construct a new indexed palette given an array of Grayscale RGB values.
		paletteDataGrayscale = new PaletteData(rgbGrayscale);
		
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBackground(SWTResourceManager.getColor(176, 224, 230));
		GridData gd_lblSensorVideoOut = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
		gd_lblSensorVideoOut.widthHint = 200;
		gd_lblSensorVideoOut.heightHint = 200;
		lblNewLabel.setLayoutData(gd_lblSensorVideoOut);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		btnPreviousImage = new Button(shell, SWT.NONE);
		btnPreviousImage.setText("Previous Image");
		btnPreviousImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if(imageNumber == 1){
					imageNumber = 1;
				}else{
					imageNumber--;
				}
				displayImages();				
			}
		});
		
		Button btnNextImage = new Button(shell, SWT.NONE);
		btnNextImage.setText("Next Image");
		btnNextImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				displayImages();
				imageNumber++;
			}
		});

	}
	
	
	protected void displayImages(){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("D:\\Temp\\AVC_TrainingData_2015-01-0_23-31-48.csv"));
			int currentLineNumber=1;
			
			while(currentLineNumber <= imageNumber){
				String line = br.readLine();
				
				if(line == null){
					if(imageNumber == 1){
						imageNumber = 1;
					}else{
						imageNumber--;
					}
					break;
				}else if (currentLineNumber == imageNumber){
					String[] stringPixeldata = line.split(",");
					int[] intPixelData = new int[25344];
					byte[] bytePixelData = new byte[25344];
					for(int i=0;i<25344;i++){
						intPixelData[i] = Integer.valueOf(stringPixeldata[i]);
						bytePixelData[i] = (byte) intPixelData[i];
					}
					
					
					org.eclipse.swt.graphics.ImageData grayscaleFrameData = new ImageData(176, 144, 8, paletteDataGrayscale);
					//Assign ImageData
					grayscaleFrameData.data = bytePixelData;
					//Create Image
					org.eclipse.swt.graphics.Image grayscaleFrame = new Image(display,grayscaleFrameData);
					//Paint Image
					lblNewLabel.setImage(grayscaleFrame);

				}else{
					//Do nothing
				}
				currentLineNumber++;
			}
						
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
	}

}
