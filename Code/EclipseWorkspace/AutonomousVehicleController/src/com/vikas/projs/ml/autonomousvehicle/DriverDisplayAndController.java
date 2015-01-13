package com.vikas.projs.ml.autonomousvehicle;


import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.CCombo;

/**
 * Implements the functionality required to provide a Front End Desktop application for the Autonomous car project
 * which will perform the following:
 * 		- Stream the Video and other sensory information from the Sensor Device on the Vehicle
 * 		- Allow User to train the Autonomous Vehicle by providing directional inputs in response
 * 		  to the displayed sensory data
 * 		- Process sensory information from the vehicle and take decisions to steer by making use of 
 * 		  trained machine learning algorithms  
 * 
 * @author Vikas K Vijayakumar (kvvikas@yahoo.co.in)
 * @version 1.0
 * 
 */
public class DriverDisplayAndController {

	private static Shell shell;
	private static Display display;
	private Text ipV4Address;
	private Text streamingPort;
	private static StyledText applicationLog;
	//Maximum number of lines which can be displayed in the application log on the screen
	private static int maxLinesInLog = 40;
	//Boolean field used to store the current status of connection with the sensor device
	private static boolean connectedToSensor = false;
	//Button which is used to initiate connection/disconnection to Sensor Device
	private static Button btnConnectToSensor;
	private static Button btnForward;
	private static Button btnLeft;
	private static Button btnReverse;
	private static Button btnRight;
	//Label where the Sensor Video frames are displayed.
	private static Label lblSensorVideoOut;
	//Define an RGB class to hold the 256 different (grey) values, pixel depth is 8.
	private RGB[] rgbGrayscale = new RGB[256];
	private static PaletteData paletteDataGrayscale;
	private static org.eclipse.swt.graphics.ImageData grayscaleFrameData;
	private SensorClient sensorClient;
	private DisplayTrainingData displayTrainingData;
	//To capture the driving mode
	private Text trainingDataDirectory;
	private Label lblTrainingDataDirectory;
	private final static String manualDrivingModeCode = "Manual";
	private final static String autoDrivingModeCode = "Automatic";
	private static String appDrivingMode = manualDrivingModeCode;
	//Java Blocking queue is used to exchange feature information between SWT main thread and the persister thread
	//Capacity of the queue is 200, which means the persister can lag behind by persisting upto 200 features before the SWT main
	//thread gets blocked.
	private static int featureQueueCapacity = 100;
	private static ArrayBlockingQueue<FeatureList> featureQueue = new ArrayBlockingQueue<FeatureList>(featureQueueCapacity);
	private static int featureQueueCapacityWarnPercent = 50;
	private static Text pixelRowsToStripFromTop;
	private static Text pixelRowsToStripFromBottom;
	private Label lblNavigationControl;
	private Label lblSensorVideoOutput;
	private Composite trainingDataReviewComposite;
	private static Label lblTrainingDataReview;
	private Label lblCapturedTrainingData;
	private Label lblTrainingFileName;
	private static Text trainingFileNameUnderReview;
	private Label lblTrainingDataReviewFrameWidth;
	private static Text trainingDataReviewFrameWidth;
	private Button btnLoadTrainingDataFile;
	private Label lblTrainingDataReviewFrameHeight;
	private static Text trainingDataReviewFrameHeight;
	
	//Default values for the Frame Width, Height and Depth. Any changes to this can have unexpected results
	private static final int defaultFrameWidth = 176;
	private static final int defaultFrameHeight = 144;
	private static final int defaultFrameDepth = 8;
	private static Label lblTrainingDataSteeringDirection;
	private Label label;
	private Label label_1;
	private Label label_2;
	private Label label_3;
	private Label label_4;
	private Label label_5;
	private Label label_6;
	private Label label_7;
	private Label label_8;
	private Label lblConnectionSetup;
	private Label label_9;
	private Composite loggingComposite;
	private Label lblLogLevel;
	private static Button chkbtnErrorsWarnings;
	private static Button chkbtnInfoLogging;
	private static Boolean infoLoggingRequired=false;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DriverDisplayAndController window = new DriverDisplayAndController();
			window.open();
			//An EventLoop is required in order to transfer user inputs from underlying
			//OS widgets to the SWT event system 
			window.createEventLoop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	private void open() {
		System.out.println("DriverDisplayAndController: About to get default display");
		display = Display.getDefault();
		//Set the Shell
		shell = new Shell();
		//Create the Shell contents
		createContents();
		//Open the Shell
		System.out.println("DriverDisplayAndController: About to open the shell");
		shell.open();

	}

	/**
	 * Create contents of the window.
	 */
	private void createContents() {
		
		//Build grey scale palette: 256 different grey values are generated. 
		for (int i = 0; i < 256; i++) {
		    rgbGrayscale[i] = new RGB(i, i, i);
		}
		//Construct a new indexed palette given an array of Grayscale RGB values.
		paletteDataGrayscale = new PaletteData(rgbGrayscale);
		
		System.out.println("DriverDisplayAndController: About to create contents of the shell");
		shell.setSize(1206, 741);
		shell.setLayout(new GridLayout(4, false));
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 4, 1));
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		lblNewLabel.setText("Autonomous Vehicle Controller cum Display");
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		Composite configurationComposite = new Composite(shell, SWT.NONE);
		GridData gd_configurationComposite = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2);
		gd_configurationComposite.heightHint = 365;
		gd_configurationComposite.widthHint = 553;
		configurationComposite.setLayoutData(gd_configurationComposite);
		
		Label label_IP = new Label(configurationComposite, SWT.NONE);
		label_IP.setBounds(255, 156, 124, 17);
		label_IP.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		label_IP.setText("Sensor IPv4 Address");
		
		ipV4Address = new Text(configurationComposite, SWT.BORDER);
		ipV4Address.setBounds(410, 155, 129, 21);
		ipV4Address.setToolTipText("Eg: 192.168.0.51");
		
		Label label_Port = new Label(configurationComposite, SWT.NONE);
		label_Port.setBounds(255, 193, 138, 17);
		label_Port.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		label_Port.setText("Sensor Streaming Port");
		
		streamingPort = new Text(configurationComposite, SWT.BORDER);
		streamingPort.setBounds(410, 192, 95, 21);
		streamingPort.setToolTipText("Eg: 6666");
		streamingPort.setText("6666");
		
		final CCombo drivingMode = new CCombo(configurationComposite, SWT.BORDER);
		drivingMode.setEditable(false);
		drivingMode.setItems(new String[] {manualDrivingModeCode, autoDrivingModeCode});
		drivingMode.setBounds(410, 231, 95, 21);
		//Add Selection Listener
		drivingMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				appDrivingMode = drivingMode.getText();
				logInfoToApplicationDisplay("Info: Selected Driving Mode is: "+appDrivingMode);
				
				//Enable the direction buttons and the Training Data directory only for Manual mode 
				if(appDrivingMode.equals(manualDrivingModeCode)){
					lblTrainingDataDirectory.setVisible(true);
					trainingDataDirectory.setVisible(true);
				}else if(appDrivingMode.equals(autoDrivingModeCode)){
					lblTrainingDataDirectory.setVisible(false);
					trainingDataDirectory.setVisible(false);					
				}else{
					//Do Nothing
				}
			}
		});
		
		Label lblDrivingMode = new Label(configurationComposite, SWT.NONE);
		lblDrivingMode.setText("Driving Mode");
		lblDrivingMode.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblDrivingMode.setBounds(255, 231, 138, 17);
		
		lblTrainingDataDirectory = new Label(configurationComposite, SWT.NONE);
		lblTrainingDataDirectory.setText("Training Features Dir");
		lblTrainingDataDirectory.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingDataDirectory.setBounds(21, 259, 138, 17);
		
		trainingDataDirectory = new Text(configurationComposite, SWT.BORDER);
		trainingDataDirectory.setToolTipText("Eg: D:\\Vikas\\TrainingData");
		trainingDataDirectory.setBounds(231, 258, 274, 21);
		
		Label lblPixelStripsFromTop = new Label(configurationComposite, SWT.NONE);
		lblPixelStripsFromTop.setText("Pixel Rows To Strip from Top");
		lblPixelStripsFromTop.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblPixelStripsFromTop.setBounds(21, 296, 192, 20);
		
		Label lblPixelStripsFromBottom = new Label(configurationComposite, SWT.NONE);
		lblPixelStripsFromBottom.setText("Pixel Rows To Strip from Bottom");
		lblPixelStripsFromBottom.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblPixelStripsFromBottom.setBounds(21, 322, 211, 21);
		
		pixelRowsToStripFromTop = new Text(configurationComposite, SWT.BORDER);
		pixelRowsToStripFromTop.setToolTipText("Eg: 6666");
		pixelRowsToStripFromTop.setBounds(231, 295, 68, 21);
		pixelRowsToStripFromTop.setText("0");
		
		pixelRowsToStripFromBottom = new Text(configurationComposite, SWT.BORDER);
		pixelRowsToStripFromBottom.setToolTipText("Eg: 6666");
		pixelRowsToStripFromBottom.setBounds(231, 321, 68, 21);
		pixelRowsToStripFromBottom.setText("0");
		
		btnConnectToSensor = new Button(configurationComposite, SWT.WRAP);
		btnConnectToSensor.setBounds(366, 305, 116, 38);
		btnConnectToSensor.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnConnectToSensor.setText("Connect To Sensor");
		
		lblSensorVideoOut = new Label(configurationComposite, SWT.NONE);
		lblSensorVideoOut.setBounds(21, 33, 200, 200);
		lblSensorVideoOut.setBackground(SWTResourceManager.getColor(176, 224, 230));
		lblSensorVideoOut.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		lblSensorVideoOutput = new Label(configurationComposite, SWT.NONE);
		lblSensorVideoOutput.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblSensorVideoOutput.setBounds(56, 10, 140, 17);
		lblSensorVideoOutput.setText("Sensor Video Output");
		lblSensorVideoOutput.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		lblNavigationControl = new Label(configurationComposite, SWT.NONE);
		lblNavigationControl.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblNavigationControl.setBounds(255, 10, 124, 17);
		lblNavigationControl.setText("Navigation Control");
		lblNavigationControl.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		//Forward Button
		btnForward = new Button(configurationComposite, SWT.NONE);
		btnForward.setBounds(296, 33, 34, 40);
		btnForward.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		btnForward.setText(" \u2191 ");
		btnForward.setEnabled(false);
		
				
				btnReverse = new Button(configurationComposite, SWT.NONE);
				btnReverse.setBounds(296, 79, 34, 40);
				btnReverse.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
				btnReverse.setText(" \u2193 ");
				btnReverse.setEnabled(false);
				
						
						btnRight = new Button(configurationComposite, SWT.NONE);
						btnRight.setBounds(336, 53, 34, 40);
						btnRight.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
						btnRight.setText("\u2192");
						btnRight.setEnabled(false);
						
						btnLeft = new Button(configurationComposite, SWT.NONE);
						btnLeft.setBounds(256, 53, 34, 40);
						btnLeft.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
						btnLeft.setText("\u2190");
						btnLeft.setEnabled(false);
						
						label = new Label(configurationComposite, SWT.SEPARATOR | SWT.VERTICAL);
						label.setBounds(241, 10, 2, 243);
						
						label_1 = new Label(configurationComposite, SWT.SEPARATOR | SWT.VERTICAL);
						label_1.setBounds(10, 10, 2, 350);
						
						label_2 = new Label(configurationComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
						label_2.setBounds(10, 358, 533, 2);
						
						label_3 = new Label(configurationComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
						label_3.setBounds(10, 10, 533, 2);
						
						label_7 = new Label(configurationComposite, SWT.SEPARATOR);
						label_7.setBounds(545, 12, 2, 350);
						
						lblConnectionSetup = new Label(configurationComposite, SWT.NONE);
						lblConnectionSetup.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
						lblConnectionSetup.setText("Initialization Parameters");
						lblConnectionSetup.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
						lblConnectionSetup.setBounds(312, 133, 156, 17);
						
						label_9 = new Label(configurationComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
						label_9.setBounds(10, 251, 233, 2);
						
						Label label_10 = new Label(configurationComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
						label_10.setBounds(241, 132, 302, 2);
						//Register listener for button click
						btnLeft.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Left button has been pressed");
								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureList currentfeatureList = new FeatureList();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureList.steerLeft);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureList to BlockingQueue for Persistance");						
									}
								}				
							}
						});
						//Register listener for button click
						btnRight.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Right button has been pressed");
								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureList currentfeatureList = new FeatureList();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureList.steerRight);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureList to BlockingQueue for Persistance");						
									}
								}				
							}
						});
						//Register listener for button click
						btnReverse.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Reverse button has been pressed");
								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureList currentfeatureList = new FeatureList();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureList.steerReverse);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureList to BlockingQueue for Persistance");						
									}
								}				
							}
						});
						//Register listener for button click
						btnForward.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Forward button has been pressed");
								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureList currentfeatureList = new FeatureList();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureList.steerforward);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureList to BlockingQueue for Persistance");						
									}
								}
							}
						});
						//Register listener for button click
						btnConnectToSensor.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								if(connectedToSensor){
									logInfoToApplicationDisplay("Info: DisconnectFromSensor button has been pressed");
									//Disconnect from sensor Device
									if (sensorClient != null){
										sensorClient.disconnectFromSensor();
									}

								}else{
									logInfoToApplicationDisplay("Info: ConnectToSensor button has been pressed");
									//Validate user inputs
									if(!Utilities.validateIPv4Address(ipV4Address.getText())){
										displayMessageOnscreen("Sensor IPv4 Address must have a valid IP V4 Address");
									}else if(!Utilities.validateInteger(streamingPort.getText(), 5000, 55000)){
										displayMessageOnscreen("Sensor Streaming Port must have a value between 5000 and 55000");
									}else if(!Utilities.validateInteger(pixelRowsToStripFromTop.getText(), 0, 176)){
										displayMessageOnscreen("Pixel Rows to Strip from Top must have a value between 0 and 176");
									}else if(!Utilities.validateInteger(pixelRowsToStripFromBottom.getText(), 0, 176)){
										displayMessageOnscreen("Pixel Rows to Strip from Bottom must have a value between 0 and 176");
									}else if((Integer.valueOf(pixelRowsToStripFromBottom.getText()) + Integer.valueOf(pixelRowsToStripFromBottom.getText())) > 176){
										displayMessageOnscreen("The sum of Pixel Rows to be stripped from Top and Bottom cannot exceed 176");
									}else{
										//Create a thread to start the communication protocol with Sensor Device 
										if(appDrivingMode.equals(manualDrivingModeCode)){
											sensorClient = new SensorClient(ipV4Address.getText(),Integer.valueOf(streamingPort.getText()), display, featureQueue,trainingDataDirectory.getText(), true, Integer.valueOf(pixelRowsToStripFromTop.getText()), Integer.valueOf(pixelRowsToStripFromBottom.getText()));
										}else{
											sensorClient = new SensorClient(ipV4Address.getText(),Integer.valueOf(streamingPort.getText()), display, featureQueue,trainingDataDirectory.getText(), false, Integer.valueOf(pixelRowsToStripFromTop.getText()), Integer.valueOf(pixelRowsToStripFromBottom.getText()));
										}
									}
								}				
							}
						});
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		loggingComposite = new Composite(shell, SWT.NONE);
		GridData gd_loggingComposite = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 2);
		gd_loggingComposite.widthHint = 577;
		gd_loggingComposite.heightHint = 282;
		loggingComposite.setLayoutData(gd_loggingComposite);
		
		applicationLog = new StyledText(loggingComposite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		applicationLog.setBounds(10, 44, 557, 228);
		applicationLog.setEditable(false);
		applicationLog.setEnabled(true);
		applicationLog.setAlwaysShowScrollBars(true);
		applicationLog.setTextLimit(100);
		
		Label lblApplicationLog = new Label(loggingComposite, SWT.NONE);
		lblApplicationLog.setBounds(244, 0, 98, 17);
		lblApplicationLog.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblApplicationLog.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblApplicationLog.setText("Application Log");
		
		lblLogLevel = new Label(loggingComposite, SWT.NONE);
		lblLogLevel.setText("Log Level");
		lblLogLevel.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblLogLevel.setBounds(10, 23, 67, 17);
		
		chkbtnInfoLogging = new Button(loggingComposite, SWT.CHECK);
		chkbtnInfoLogging.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(chkbtnInfoLogging.getSelection()){
					logInfoToApplicationDisplay("Info: Information logging is enabled");
					infoLoggingRequired = true;
				}else{
					logInfoToApplicationDisplay("Info: Information logging is disabled");
					infoLoggingRequired = false;
				}
			}
		});
		chkbtnInfoLogging.setBounds(83, 23, 67, 16);
		chkbtnInfoLogging.setText("Info");
		
		chkbtnErrorsWarnings = new Button(loggingComposite, SWT.CHECK);
		chkbtnErrorsWarnings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(chkbtnErrorsWarnings.getSelection()){
					logInfoToApplicationDisplay("Info: Errors & Warnings will be logged");
				}else{
					logInfoToApplicationDisplay("Info: Errors & Warnings will NOT be logged");
				}
			}
		});
		chkbtnErrorsWarnings.setEnabled(false);
		chkbtnErrorsWarnings.setSelection(true);
		chkbtnErrorsWarnings.setText("Errors, Warnings");
		chkbtnErrorsWarnings.setBounds(156, 22, 107, 16);
		new Label(shell, SWT.NONE);
		
		trainingDataReviewComposite = new Composite(shell, SWT.NONE);
		GridData gd_trainingDataReviewComposite = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2);
		gd_trainingDataReviewComposite.heightHint = 290;
		gd_trainingDataReviewComposite.widthHint = 562;
		trainingDataReviewComposite.setLayoutData(gd_trainingDataReviewComposite);
		
		lblTrainingDataReview = new Label(trainingDataReviewComposite, SWT.NONE);
		lblTrainingDataReview.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingDataReview.setBackground(SWTResourceManager.getColor(176, 224, 230));
		lblTrainingDataReview.setBounds(21, 23, 200, 200);
		
		lblCapturedTrainingData = new Label(trainingDataReviewComposite, SWT.NONE);
		lblCapturedTrainingData.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblCapturedTrainingData.setText("Captured Training Data Review");
		lblCapturedTrainingData.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblCapturedTrainingData.setBounds(250, 0, 200, 17);
		
		Button btnPreviousTrainingDataImage = new Button(trainingDataReviewComposite, SWT.NONE);
		btnPreviousTrainingDataImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Display the PreviousImage in the Training Data File
				if(displayTrainingData != null){
					displayTrainingData.displayPreviousImage();
				}
			}
		});
		btnPreviousTrainingDataImage.setText("Previous Image");
		btnPreviousTrainingDataImage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnPreviousTrainingDataImage.setBounds(250, 185, 116, 38);
		
		Button btnNextTrainingDataImage = new Button(trainingDataReviewComposite, SWT.NONE);
		btnNextTrainingDataImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Display the NextImage in the Training Data File
				if(displayTrainingData != null){
					displayTrainingData.displayNextImage();
				}
			}
		});
		btnNextTrainingDataImage.setText("Next Image");
		btnNextTrainingDataImage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnNextTrainingDataImage.setBounds(384, 185, 116, 38);
		
		lblTrainingFileName = new Label(trainingDataReviewComposite, SWT.NONE);
		lblTrainingFileName.setText("Training \r\nFile Name");
		lblTrainingFileName.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingFileName.setBounds(21, 239, 73, 34);
		
		trainingFileNameUnderReview = new Text(trainingDataReviewComposite, SWT.BORDER);
		trainingFileNameUnderReview.setToolTipText("Eg: D:\\Vikas\\TrainingData");
		trainingFileNameUnderReview.setBounds(100, 252, 347, 21);
		
		lblTrainingDataReviewFrameWidth = new Label(trainingDataReviewComposite, SWT.NONE);
		lblTrainingDataReviewFrameWidth.setText("Frame Width");
		lblTrainingDataReviewFrameWidth.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingDataReviewFrameWidth.setBounds(279, 125, 82, 17);
		
		trainingDataReviewFrameWidth = new Text(trainingDataReviewComposite, SWT.BORDER);
		trainingDataReviewFrameWidth.setToolTipText("Eg: 192.168.0.51");
		trainingDataReviewFrameWidth.setBounds(384, 121, 73, 21);
		trainingDataReviewFrameWidth.setText("176");
		
		btnLoadTrainingDataFile = new Button(trainingDataReviewComposite, SWT.NONE);
		btnLoadTrainingDataFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logInfoToApplicationDisplay("Info: Button to load Training Data File for review pressed");
				//Validate User Inputs
				if(!Utilities.validateInteger(trainingDataReviewFrameWidth.getText(), 0, 176)){
					displayMessageOnscreen("Training data Frame Width must have a value between 0 and 176");
				}else if(!Utilities.validateInteger(trainingDataReviewFrameHeight.getText(), 0, 144)){
					displayMessageOnscreen("Training data Frame Height must have a value between 0 and 144");
				}else{
					//Cancel any currently running thread
					if(displayTrainingData != null){
						displayTrainingData.cancel();
					}
					//Load file and display images
					displayTrainingData = new DisplayTrainingData(display, trainingFileNameUnderReview.getText(), Integer.valueOf(trainingDataReviewFrameWidth.getText()), Integer.valueOf(trainingDataReviewFrameHeight.getText()), DriverDisplayAndController.defaultFrameDepth);
				}
			}
		});
		btnLoadTrainingDataFile.setToolTipText("Load File");
		btnLoadTrainingDataFile.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnLoadTrainingDataFile.setBounds(451, 250, 49, 25);
		btnLoadTrainingDataFile.setText("Load");
		
		lblTrainingDataReviewFrameHeight = new Label(trainingDataReviewComposite, SWT.NONE);
		lblTrainingDataReviewFrameHeight.setText("Frame Height");
		lblTrainingDataReviewFrameHeight.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingDataReviewFrameHeight.setBounds(279, 154, 82, 17);
		
		trainingDataReviewFrameHeight = new Text(trainingDataReviewComposite, SWT.BORDER);		
		trainingDataReviewFrameHeight.setToolTipText("Eg: 192.168.0.51");
		trainingDataReviewFrameHeight.setBounds(384, 150, 73, 21);
		trainingDataReviewFrameHeight.setText("144");
		
		lblTrainingDataSteeringDirection = new Label(trainingDataReviewComposite, SWT.NONE);		
		lblTrainingDataSteeringDirection.setAlignment(SWT.CENTER);
		lblTrainingDataSteeringDirection.setBounds(221, 23, 50, 44);
		
		label_4 = new Label(trainingDataReviewComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_4.setBounds(10, 0, 544, 2);
		
		label_5 = new Label(trainingDataReviewComposite, SWT.SEPARATOR);
		label_5.setBounds(10, 0, 2, 280);
		
		label_6 = new Label(trainingDataReviewComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_6.setBounds(10, 278, 544, 2);
		
		label_8 = new Label(trainingDataReviewComposite, SWT.SEPARATOR);
		label_8.setBounds(552, 2, 2, 280);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);

	}
	
	/**
	 * Create the Event loop
	 */
	private void createEventLoop(){
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
            //System.out.println("Display Sleeping");
        }
        System.out.println("Info: About to dispose the display");
        display.dispose();
	}
	
	
	/**
	 * Update the current status of connection to the Sensor device
	 * @param currentStatus
	 */
	protected static synchronized void updateSensorConnectionStatus(boolean connected){
		connectedToSensor = connected;
		if(connected){
			btnConnectToSensor.setText("Disconnect From Sensor");
			//Enable direction buttons
			btnForward.setEnabled(true);
			btnLeft.setEnabled(true);
			btnReverse.setEnabled(true);
			btnRight.setEnabled(true);
		}else{
			btnConnectToSensor.setText("Connect To Sensor");
			//Disable direction buttons
			btnForward.setEnabled(false);
			btnLeft.setEnabled(false);
			btnReverse.setEnabled(false);
			btnRight.setEnabled(false);
		}
	}
	
	/**
	 * Display the frame received from Sensor Device on screen 
	 */
	protected static synchronized void displayFramesOnCanvas(int width, int height, int depth, byte[] frameData, boolean sensorVideo){

		if(sensorVideo){
			//Display Sensor Video
			//Initialize Imagedata
			grayscaleFrameData = new ImageData(width, height, depth, paletteDataGrayscale);
			//Assign ImageData
			grayscaleFrameData.data = frameData;
			//Create Image
			org.eclipse.swt.graphics.Image grayscaleFrame = new Image(display,grayscaleFrameData);
			//Paint Image
			lblSensorVideoOut.setImage(grayscaleFrame);		
			//Release OS resources sampleImage to the image/frame
			//grayscaleFrame.dispose();
			logInfoToApplicationDisplay("Info: Successfully painted a "+width+" X "+height+" frame from Sensor Device on Screen");
		} else{
			//Display captured training data image
			//Initialize Imagedata
			ImageData trainingFrameData = new ImageData(width, height, depth, paletteDataGrayscale);
			//Assign ImageData
			trainingFrameData.data = frameData;
			//Create Image
			org.eclipse.swt.graphics.Image trainingFrame = new Image(display,trainingFrameData);
			//Paint Image
			lblTrainingDataReview.setImage(trainingFrame);		
			//Release OS resources sampleImage to the image/frame
			//grayscaleFrame.dispose();
			logInfoToApplicationDisplay("Info: Successfully painted a "+width+" X "+height+" frame from capture training data on Screen");
		}
		
	}
	
	/**
	 * This method can be used to log Informational message onto to the Application Log on the screen
	 */
	protected static synchronized void logInfoToApplicationDisplay(String logEntry){
		if(infoLoggingRequired){
			if (applicationLog.getLineCount() < maxLinesInLog){
				//Log after prefixing with a new line
				applicationLog.insert(System.getProperty("line.separator")+logEntry);
				System.out.println(logEntry);
			}else{
				//Remove the oldest line from the logs and insert the new entry after prefixing with a new line
				String currentLogContent = applicationLog.getText();
				applicationLog.setText(currentLogContent.substring(0, currentLogContent.lastIndexOf(System.getProperty("line.separator"))));
				applicationLog.insert(System.getProperty("line.separator")+logEntry);
				System.out.println(logEntry);
			}
		}else{
			//Dont Log
		}	
	}
	
	/**
	 * This method can be used to log Exception Stack traces in a readable format on the Application Log
	 */
	protected static synchronized void logErrorToApplicationDisplay(Exception e, String informationMessage){
		if(chkbtnErrorsWarnings.getSelection()){
			if(infoLoggingRequired){
				StackTraceElement[] stackTraceLines = e.getStackTrace();
				for (int i=0;i<stackTraceLines.length;i++){
					logInfoToApplicationDisplay("\t"+"\t"+"\t"+"\t"+stackTraceLines[i].toString());
				}
				logInfoToApplicationDisplay(e.getMessage());
				logInfoToApplicationDisplay(informationMessage);
			}else{
				infoLoggingRequired = true;
				StackTraceElement[] stackTraceLines = e.getStackTrace();
				for (int i=0;i<stackTraceLines.length;i++){
					logInfoToApplicationDisplay("\t"+"\t"+"\t"+"\t"+stackTraceLines[i].toString());
				}
				logInfoToApplicationDisplay(e.getMessage());
				logInfoToApplicationDisplay(informationMessage);
				infoLoggingRequired = false;
			}			
		}else{
			//Dont Log
		}
	}
	
	/**
	 * This method can be used to log warnings on the Application Log
	 */
	protected static synchronized void logWarningToApplicationDisplay(String logEntry){
		if(chkbtnErrorsWarnings.getSelection()){
			if(infoLoggingRequired){
				logInfoToApplicationDisplay(logEntry);
			}else{
				infoLoggingRequired = true;
				logInfoToApplicationDisplay(logEntry);
				infoLoggingRequired = false;
			}			
		}else{
			//Dont Log
		}
	}
	
	/**
	 * Update the name of the current Training Data File on screen for review
	 * 
	 */
	protected static synchronized void updateCurrentTrainingParams(String trainingDataFileName){		
		trainingFileNameUnderReview.setText(trainingDataFileName);
		trainingDataReviewFrameWidth.setText(String.valueOf(DriverDisplayAndController.defaultFrameWidth));
		trainingDataReviewFrameHeight.setText(String.valueOf(DriverDisplayAndController.defaultFrameHeight - (Integer.valueOf(pixelRowsToStripFromTop.getText()) + Integer.valueOf(pixelRowsToStripFromBottom.getText()))));
	}
	
	/**
	 * When reviewing the Training data, used to display the Steering Direction which was chosen
	 * @param steeringDirection
	 */
	protected static synchronized void displayTrainingDataSteeringDirection(String steeringDirection){
		logInfoToApplicationDisplay("Info: SteeringDirection is: "+steeringDirection);
		if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureList.steerforward)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Forward.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureList.steerReverse)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Reverse.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureList.steerLeft)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Left.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureList.steerRight)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Right.jpg"));
		}else{
			logInfoToApplicationDisplay("Error: Unable to understand the captured Steering Direction: "+steeringDirection);
		}
	}
	
	/**
	 * Used to display message on the screen to the user in a message box / message dialog
	 * @param message
	 */
	protected static synchronized void displayMessageOnscreen(String message){	
		MessageDialog.openError(shell, "Error", message);
	}
}
