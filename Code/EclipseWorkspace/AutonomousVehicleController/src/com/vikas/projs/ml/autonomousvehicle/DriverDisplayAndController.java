package com.vikas.projs.ml.autonomousvehicle;


import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Combo;

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
	//Boolean field used to store the current status of connection with the Arduino / Controller
	private static boolean connectedToController = false;
	//Button which is used to initiate connection/disconnection to Sensor Device
	private static Button btnConnectToSensor;
	private static Button btnForward;
	private static Button btnLeft;
	private static Button btnReverse;
	private static Button btnRight;
	private static Button btnConnectToController;
	//Label where the Sensor Video frames are displayed.
	private static Label lblSensorVideoOut;
	//Define an RGB class to hold the 256 different (grey) values, pixel depth is 8.
	private RGB[] rgbGrayscale = new RGB[256];
	private static PaletteData paletteDataGrayscale;
	private static org.eclipse.swt.graphics.ImageData grayscaleFrameData;
	private SensorClient sensorClient;
	private VehicleController vehicleController;
	private DisplayTrainingData displayTrainingData;
	//To capture the driving mode
	private Text trainingDataDirectory;
	private Label lblTrainingDataDirectory;
	private final static String manualDrivingModeCode = "Manual";
	private final static String autoDrivingModeCode = "Automatic";
	private static String appDrivingMode = manualDrivingModeCode;
	
	//Java Blocking queue is used to send feature information from SWT main thread to the persister thread
	//Capacity of the queue is 200, which means the persister can lag behind by persisting upto 200 features before the SWT main
	//thread gets blocked.
	private static int featureQueueCapacity = 100;
	private static ArrayBlockingQueue<FeatureMessage> featureQueue = new ArrayBlockingQueue<FeatureMessage>(featureQueueCapacity);
	private static int featureQueueCapacityWarnPercent = 50;
	//Blocking queue to send the controls to Arduino from the SWT main thread in case of training and
	//prediction thread in case of auto mode
	private static int controllerQueueCapacity = 100;
	private static ArrayBlockingQueue<ControlMessage> controllerQueue = new ArrayBlockingQueue<ControlMessage>(controllerQueueCapacity);
	private static int controllerQueueCapacityWarnPercent = 50;
	
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
	private Label lblConnectionSetup;
	private Composite loggingComposite;
	private Label lblLogLevel;
	private static Button chkbtnErrorsWarnings;
	private static Button chkbtnInfoLogging;
	private static Boolean infoLoggingRequired=false;
	private Label lblStep;
	private Label lblStep_1;
	private Label lblArduinoPortName;
	private Text arduinoPortName;
	private TabFolder tabFolder;
	private TabItem TrainingReviewTab;
	private TabItem LiveTab;
	private Composite sensorConfigurationcomposite;
	private Composite navigationControlComposite;
	private Composite trainingDataReviewConfigComposite;
	private Label lblCapturedTrainingsetNavigation;
	private Composite trainingDataReviewNavgationDetails;
	private static Button btnPreviousTrainingDataImage;
	private static Button btnNextTrainingDataImage;
	private static Button btnDeleteTrainingDataImage;
	private Button btnGenerateSets;
	private Text capturedDataDirectoryName;
	private Button btnResizeTrainingFile;
	private Label lblPixelRowsToStripFromTopTrain;
	private Label lblPixelRowsToStripFromBotTrain;
	private Text pixelRowsToStripFromTopTraining;
	private Text pixelRowsToStripFromBottomTraining;
	private Label lblPredictedTrainingsetNavigation;
	private static Label lblTrainingDataPredictedSteeringDirection;
	private Label lblPredictNoOfHiddenLayers;
	private Label lblPredictWeightsForInputLayer;
	private Label lblPredictWeightsForFirstHiddenLayer;
	private Label lblPredictWeightsForSecondHiddenLayer;
	private Label lblPredictWeightsForThirdHiddenLayer;
	private Label lblInputsForPrediction;
	private Text textPredictWeightsForInputLayer;
	private Text textPredictWeightsForSecondHiddenLayer;
	private Text textPredictWeightsForFirstHiddenLayer;
	private Text textPredictWeightsForThirdHiddenLayer;
	private Text textPredictNumberOfHiddenLayers;
	private Composite predictionComposite;
	private static Button btnAssociatePredictionWeights;
	private static Boolean predictionInProgress = false;
	private PredictUsingNN predictUsingNN;
	private Button btnReassignDirection;
	private Combo comboReAssignDirection;
	private Label label;
	private Label label_1;
	private Label label_2;
	private Label label_3;
	private Label label_4;
	private static Text textTrainingPredictionConfidence;
	private Label lblPredictionConfidence;

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
		shell.setSize(1094, 655);
		shell.setLayout(new GridLayout(4, false));
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 4, 1));
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		lblNewLabel.setText("Autonomous Vehicle Desktop Application");
		new Label(shell, SWT.NONE);
		
		tabFolder = new TabFolder(shell, SWT.NONE);
		GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tabFolder.heightHint = 566;
		gd_tabFolder.widthHint = 664;
		tabFolder.setLayoutData(gd_tabFolder);
		
		LiveTab = new TabItem(tabFolder, SWT.NONE);
		LiveTab.setText("Live");
		
		Composite configurationComposite = new Composite(tabFolder, SWT.NONE);
		LiveTab.setControl(configurationComposite);
		configurationComposite.setLayout(new GridLayout(2, false));
		new Label(configurationComposite, SWT.NONE);
		
		lblSensorVideoOutput = new Label(configurationComposite, SWT.NONE);
		GridData gd_lblSensorVideoOutput = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
		gd_lblSensorVideoOutput.heightHint = 51;
		gd_lblSensorVideoOutput.widthHint = 119;
		lblSensorVideoOutput.setLayoutData(gd_lblSensorVideoOutput);
		lblSensorVideoOutput.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblSensorVideoOutput.setText("Sensor Video \r\n    Output");
		lblSensorVideoOutput.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		sensorConfigurationcomposite = new Composite(configurationComposite, SWT.NONE);
		sensorConfigurationcomposite.setLayout(new GridLayout(3, false));
		GridData gd_sensorConfigurationcomposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
		gd_sensorConfigurationcomposite.widthHint = 393;
		gd_sensorConfigurationcomposite.heightHint = 241;
		sensorConfigurationcomposite.setLayoutData(gd_sensorConfigurationcomposite);
		
		lblStep = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblStep.setText("Step 1 - Choose \r\nDriving Mode");
		lblStep.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblStep.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		Label lblDrivingMode = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblDrivingMode.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblDrivingMode.setText("Driving Mode");
		lblDrivingMode.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		final CCombo drivingMode = new CCombo(sensorConfigurationcomposite, SWT.BORDER);
		drivingMode.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		drivingMode.setBackground(SWTResourceManager.getColor(255, 250, 205));
		drivingMode.setEditable(false);
		drivingMode.setItems(new String[] {manualDrivingModeCode, autoDrivingModeCode});
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
		
		label_3 = new Label(sensorConfigurationcomposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_3 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_label_3.widthHint = 404;
		label_3.setLayoutData(gd_label_3);
		label_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		label_3.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		
		lblConnectionSetup = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblConnectionSetup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblConnectionSetup.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblConnectionSetup.setText("Step 2 - Connect \r\nto Sensor");
		lblConnectionSetup.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		Label label_IP = new Label(sensorConfigurationcomposite, SWT.NONE);
		label_IP.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		label_IP.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		label_IP.setText("Sensor IPv4 Address");
		
		ipV4Address = new Text(sensorConfigurationcomposite, SWT.BORDER);
		ipV4Address.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		ipV4Address.setBackground(SWTResourceManager.getColor(255, 250, 205));
		ipV4Address.setToolTipText("Eg: 192.168.0.51");
		new Label(sensorConfigurationcomposite, SWT.NONE);
		
		Label label_Port = new Label(sensorConfigurationcomposite, SWT.NONE);
		label_Port.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		label_Port.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		label_Port.setText("Sensor Streaming Port");
		
		streamingPort = new Text(sensorConfigurationcomposite, SWT.BORDER);
		streamingPort.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		streamingPort.setBackground(SWTResourceManager.getColor(255, 250, 205));
		streamingPort.setToolTipText("Eg: 6666");
		streamingPort.setText("6666");
		
		lblTrainingDataDirectory = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblTrainingDataDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblTrainingDataDirectory.setText("Training \r\nFeatures Dir");
		lblTrainingDataDirectory.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		trainingDataDirectory = new Text(sensorConfigurationcomposite, SWT.BORDER);
		GridData gd_trainingDataDirectory = new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1);
		gd_trainingDataDirectory.widthHint = 253;
		trainingDataDirectory.setLayoutData(gd_trainingDataDirectory);
		trainingDataDirectory.setBackground(SWTResourceManager.getColor(255, 250, 205));
		trainingDataDirectory.setToolTipText("Eg: D:\\Vikas\\TrainingData");
		
		Label lblPixelStripsFromTop = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblPixelStripsFromTop.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		lblPixelStripsFromTop.setText("Pixel Rows To Strip from Top");
		lblPixelStripsFromTop.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		pixelRowsToStripFromTop = new Text(sensorConfigurationcomposite, SWT.BORDER);
		GridData gd_pixelRowsToStripFromTop = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_pixelRowsToStripFromTop.widthHint = 38;
		pixelRowsToStripFromTop.setLayoutData(gd_pixelRowsToStripFromTop);
		pixelRowsToStripFromTop.setBackground(SWTResourceManager.getColor(255, 250, 205));
		pixelRowsToStripFromTop.setToolTipText("Eg: 6666");
		pixelRowsToStripFromTop.setText("0");
		
		Label lblPixelStripsFromBottom = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblPixelStripsFromBottom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		lblPixelStripsFromBottom.setText("Pixel Rows To Strip from Bottom");
		lblPixelStripsFromBottom.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		pixelRowsToStripFromBottom = new Text(sensorConfigurationcomposite, SWT.BORDER);
		GridData gd_pixelRowsToStripFromBottom = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_pixelRowsToStripFromBottom.widthHint = 38;
		pixelRowsToStripFromBottom.setLayoutData(gd_pixelRowsToStripFromBottom);
		pixelRowsToStripFromBottom.setBackground(SWTResourceManager.getColor(255, 250, 205));
		pixelRowsToStripFromBottom.setToolTipText("Eg: 6666");
		pixelRowsToStripFromBottom.setText("0");
		
		btnConnectToSensor = new Button(sensorConfigurationcomposite, SWT.NONE);
		GridData gd_btnConnectToSensor = new GridData(SWT.CENTER, SWT.CENTER, true, true, 3, 1);
		gd_btnConnectToSensor.widthHint = 130;
		btnConnectToSensor.setLayoutData(gd_btnConnectToSensor);
		btnConnectToSensor.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnConnectToSensor.setText("Connect");
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
						displayErrorMessageOnscreen("Sensor IPv4 Address must have a valid IP V4 Address");
					}else if(!Utilities.validateInteger(streamingPort.getText(), 5000, 55000)){
						displayErrorMessageOnscreen("Sensor Streaming Port must have a value between 5000 and 55000");
					}else if(!Utilities.validateInteger(pixelRowsToStripFromTop.getText(), 0, 176)){
						displayErrorMessageOnscreen("Pixel Rows to Strip from Top must have a value between 0 and 176");
					}else if(!Utilities.validateInteger(pixelRowsToStripFromBottom.getText(), 0, 176)){
						displayErrorMessageOnscreen("Pixel Rows to Strip from Bottom must have a value between 0 and 176");
					}else if((Integer.valueOf(pixelRowsToStripFromBottom.getText()) + Integer.valueOf(pixelRowsToStripFromBottom.getText())) > 176){
						displayErrorMessageOnscreen("The sum of Pixel Rows to be stripped from Top and Bottom cannot exceed 176");
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
		
		label_4 = new Label(sensorConfigurationcomposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_4 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_label_4.widthHint = 411;
		label_4.setLayoutData(gd_label_4);
		label_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		label_4.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		
		lblStep_1 = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblStep_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblStep_1.setText("Step 3 - Connect \r\nto Controller");
		lblStep_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblStep_1.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		lblArduinoPortName = new Label(sensorConfigurationcomposite, SWT.NONE);
		lblArduinoPortName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblArduinoPortName.setText("Serial Port Name");
		lblArduinoPortName.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		arduinoPortName = new Text(sensorConfigurationcomposite, SWT.BORDER);
		GridData gd_arduinoPortName = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_arduinoPortName.widthHint = 42;
		arduinoPortName.setLayoutData(gd_arduinoPortName);
		arduinoPortName.setBackground(SWTResourceManager.getColor(255, 250, 205));
		arduinoPortName.setToolTipText("Eg: 6666");
		arduinoPortName.setText("COM3");
		
		btnConnectToController = new Button(sensorConfigurationcomposite, SWT.NONE);
		GridData gd_btnConnectToController = new GridData(SWT.CENTER, SWT.CENTER, true, true, 3, 1);
		gd_btnConnectToController.widthHint = 129;
		btnConnectToController.setLayoutData(gd_btnConnectToController);
		btnConnectToController.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connectedToController){
					logInfoToApplicationDisplay("Info: DisconnectFromController button has been pressed");
					//Disconnect from Arduino / Controller
					if (vehicleController != null){
						vehicleController.disconnectFromController();
					}

				}else{
					logInfoToApplicationDisplay("Info: ConnectToController button has been pressed");
					//Create a thread to start the communication protocol with Sensor Device 
					vehicleController = new VehicleController(arduinoPortName.getText(), display, controllerQueue);
				}
			}
		});
		btnConnectToController.setText("Connect");
		btnConnectToController.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		
		lblSensorVideoOut = new Label(configurationComposite, SWT.NONE);
		GridData gd_lblSensorVideoOut = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_lblSensorVideoOut.widthHint = 45;
		gd_lblSensorVideoOut.minimumWidth = 200;
		gd_lblSensorVideoOut.minimumHeight = 200;
		lblSensorVideoOut.setLayoutData(gd_lblSensorVideoOut);
		lblSensorVideoOut.setBackground(SWTResourceManager.getColor(176, 224, 230));
		lblSensorVideoOut.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		navigationControlComposite = new Composite(configurationComposite, SWT.NONE);
		navigationControlComposite.setLayout(new GridLayout(3, false));
		GridData gd_navigationControlComposite = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		gd_navigationControlComposite.heightHint = 123;
		navigationControlComposite.setLayoutData(gd_navigationControlComposite);
		
		lblNavigationControl = new Label(navigationControlComposite, SWT.NONE);
		lblNavigationControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblNavigationControl.setSize(165, 0);
		lblNavigationControl.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblNavigationControl.setText("Navigation Control");
		lblNavigationControl.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		new Label(navigationControlComposite, SWT.NONE);
		
		//Forward Button
		btnForward = new Button(navigationControlComposite, SWT.NONE);
		btnForward.setSize(34, 15);
		btnForward.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		btnForward.setText(" \u2191 ");
		btnForward.setEnabled(false);
		new Label(navigationControlComposite, SWT.NONE);
		
		btnLeft = new Button(navigationControlComposite, SWT.NONE);
		btnLeft.setSize(30, 15);
		btnLeft.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
		btnLeft.setText("\u2190");
		btnLeft.setEnabled(false);
		//Register listener for button click
		btnLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				logInfoToApplicationDisplay("Info: Left button has been pressed");
				//Send the Control Message to VehicleController
				if(appDrivingMode.equals(manualDrivingModeCode)){
					try {
						//Send a warning if the controllerQueue capacity has reached the configured warning threshold
						float controlQueueCapacityPercent = (((controllerQueueCapacity - controllerQueue.remainingCapacity()) / controllerQueueCapacity) * 100);
						if(controlQueueCapacityPercent > controllerQueueCapacityWarnPercent){
							logWarningToApplicationDisplay("Warning: The ControllerQueue has reached "+controlQueueCapacityPercent+" of its capacity. Controls are not being processed fast enough");
						}
						ControlMessage controlMessage = new ControlMessage();
						controlMessage.setSteeringDirection(FeatureMessage.steerLeft);
						controllerQueue.put(controlMessage);
						logInfoToApplicationDisplay("Info: Successfully sent a ControlMessage to Steer Left");
					} catch (InterruptedException ex) {
						logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish ControlMessage to BlockingQueue");						
					}
				}
				//Push the features to BlockingQueue for persistence in case the mode is Manual
				if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
					FeatureMessage currentfeatureList = new FeatureMessage();
					currentfeatureList.setFrameWidth(grayscaleFrameData.width);
					currentfeatureList.setFrameHeight(grayscaleFrameData.height);
					currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
					currentfeatureList.setFramePixelData(grayscaleFrameData.data);
					currentfeatureList.setSteeringDirection(FeatureMessage.steerLeft);
					
					try {
						//Send a warning if the featureQueue capacity has reached the configured warning threshold
						float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
						if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
							logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
						}
						featureQueue.put(currentfeatureList);
						logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
					} catch (InterruptedException ex) {
						logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureMessage to BlockingQueue for Persistance");						
					}
				}				
			}
		});
		
				
				btnReverse = new Button(navigationControlComposite, SWT.NONE);
				btnReverse.setSize(34, 15);
				btnReverse.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
				btnReverse.setText(" \u2193 ");
				btnReverse.setEnabled(false);
				//Register listener for button click
				btnReverse.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						logInfoToApplicationDisplay("Info: Reverse button has been pressed");
						//Send the Control Message to VehicleController
						if(appDrivingMode.equals(manualDrivingModeCode)){
							try {
								//Send a warning if the controllerQueue capacity has reached the configured warning threshold
								float controlQueueCapacityPercent = (((controllerQueueCapacity - controllerQueue.remainingCapacity()) / controllerQueueCapacity) * 100);
								if(controlQueueCapacityPercent > controllerQueueCapacityWarnPercent){
									logWarningToApplicationDisplay("Warning: The ControllerQueue has reached "+controlQueueCapacityPercent+" of its capacity. Controls are not being processed fast enough");
								}
								ControlMessage controlMessage = new ControlMessage();
								controlMessage.setSteeringDirection(FeatureMessage.steerReverse);
								controllerQueue.put(controlMessage);
								logInfoToApplicationDisplay("Info: Successfully sent a ControlMessage to Steer Reverse");
							} catch (InterruptedException ex) {
								logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish ControlMessage to BlockingQueue");						
							}
						}
						//Push the features to BlockingQueue for persistence in case the mode is Manual
						if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
							FeatureMessage currentfeatureList = new FeatureMessage();
							currentfeatureList.setFrameWidth(grayscaleFrameData.width);
							currentfeatureList.setFrameHeight(grayscaleFrameData.height);
							currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
							currentfeatureList.setFramePixelData(grayscaleFrameData.data);
							currentfeatureList.setSteeringDirection(FeatureMessage.steerReverse);
							
							try {
								//Send a warning if the featureQueue capacity has reached the configured warning threshold
								float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
								if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
									logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
								}
								featureQueue.put(currentfeatureList);
								logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
							} catch (InterruptedException ex) {
								logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureMessage to BlockingQueue for Persistance");						
							}
						}				
					}
				});
				
						
						btnRight = new Button(navigationControlComposite, SWT.NONE);
						btnRight.setSize(30, 15);
						btnRight.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.BOLD));
						btnRight.setText("\u2192");
						btnRight.setEnabled(false);
						//Register listener for button click
						btnRight.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Right button has been pressed");
								//Send the Control Message to VehicleController
								if(appDrivingMode.equals(manualDrivingModeCode)){
									try {
										//Send a warning if the controllerQueue capacity has reached the configured warning threshold
										float controlQueueCapacityPercent = (((controllerQueueCapacity - controllerQueue.remainingCapacity()) / controllerQueueCapacity) * 100);
										if(controlQueueCapacityPercent > controllerQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The ControllerQueue has reached "+controlQueueCapacityPercent+" of its capacity. Controls are not being processed fast enough");
										}
										ControlMessage controlMessage = new ControlMessage();
										controlMessage.setSteeringDirection(FeatureMessage.steerRight);
										controllerQueue.put(controlMessage);
										logInfoToApplicationDisplay("Info: Successfully sent a ControlMessage to Steer Right");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish ControlMessage to BlockingQueue");						
									}
								}

								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureMessage currentfeatureList = new FeatureMessage();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureMessage.steerRight);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureMessage to BlockingQueue for Persistance");						
									}
								}				
							}
						});
						//Register listener for button click
						btnForward.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e){
								logInfoToApplicationDisplay("Info: Forward button has been pressed");
								//Send the Control Message to VehicleController
								if(appDrivingMode.equals(manualDrivingModeCode)){
									try {
										//Send a warning if the controllerQueue capacity has reached the configured warning threshold
										float controlQueueCapacityPercent = (((controllerQueueCapacity - controllerQueue.remainingCapacity()) / controllerQueueCapacity) * 100);
										if(controlQueueCapacityPercent > controllerQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The ControllerQueue has reached "+controlQueueCapacityPercent+" of its capacity. Controls are not being processed fast enough");
										}
										ControlMessage controlMessage = new ControlMessage();
										controlMessage.setSteeringDirection(FeatureMessage.steerforward);
										controllerQueue.put(controlMessage);
										logInfoToApplicationDisplay("Info: Successfully sent a ControlMessage to Steer Forward");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish ControlMessage to BlockingQueue");						
									}
								}
								//Push the features to BlockingQueue for persistence in case the mode is Manual
								if(appDrivingMode.equals(manualDrivingModeCode) && (grayscaleFrameData != null)){
									FeatureMessage currentfeatureList = new FeatureMessage();
									currentfeatureList.setFrameWidth(grayscaleFrameData.width);
									currentfeatureList.setFrameHeight(grayscaleFrameData.height);
									currentfeatureList.setPixelDepth(grayscaleFrameData.depth);
									currentfeatureList.setFramePixelData(grayscaleFrameData.data);
									currentfeatureList.setSteeringDirection(FeatureMessage.steerforward);
									
									try {
										//Send a warning if the featureQueue capacity has reached the configured warning threshold
										float featureQueueCapacityPercent = (((featureQueueCapacity - featureQueue.remainingCapacity()) / featureQueueCapacity) * 100);
										if(featureQueueCapacityPercent > featureQueueCapacityWarnPercent){
											logWarningToApplicationDisplay("Warning: The FeatureQueue has reached "+featureQueueCapacityPercent+" of its capacity. Features are not being persisted fast enough");
										}
										featureQueue.put(currentfeatureList);
										logInfoToApplicationDisplay("Info: Successfully sent a "+grayscaleFrameData.width+" X "+grayscaleFrameData.height+" frame for persistance");
									} catch (InterruptedException ex) {
										logErrorToApplicationDisplay(ex, "ERROR: InterruptedException when trying to publish FeatureMessage to BlockingQueue for Persistance");						
									}
								}
							}
						});
		
		TrainingReviewTab = new TabItem(tabFolder, SWT.NONE);
		TrainingReviewTab.setText("Training and Prediction Review");
		
		trainingDataReviewComposite = new Composite(tabFolder, SWT.NONE);
		TrainingReviewTab.setControl(trainingDataReviewComposite);
		trainingDataReviewComposite.setLayout(new GridLayout(2, false));
		
		trainingDataReviewConfigComposite = new Composite(trainingDataReviewComposite, SWT.NONE);
		trainingDataReviewConfigComposite.setLayout(new GridLayout(4, false));
		GridData gd_trainingDataReviewConfigComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_trainingDataReviewConfigComposite.widthHint = 390;
		trainingDataReviewConfigComposite.setLayoutData(gd_trainingDataReviewConfigComposite);
		
		lblTrainingDataReviewFrameHeight = new Label(trainingDataReviewConfigComposite, SWT.NONE);
		lblTrainingDataReviewFrameHeight.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblTrainingDataReviewFrameHeight.setText("Frame \r\nHeight");
		lblTrainingDataReviewFrameHeight.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		trainingDataReviewFrameHeight = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		trainingDataReviewFrameHeight.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		trainingDataReviewFrameHeight.setBackground(SWTResourceManager.getColor(255, 250, 205));
		trainingDataReviewFrameHeight.setToolTipText("Eg: 192.168.0.51");
		trainingDataReviewFrameHeight.setText("144");
		
		lblTrainingDataReviewFrameWidth = new Label(trainingDataReviewConfigComposite, SWT.NONE);
		lblTrainingDataReviewFrameWidth.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblTrainingDataReviewFrameWidth.setText("Frame \r\nWidth");
		lblTrainingDataReviewFrameWidth.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		trainingDataReviewFrameWidth = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		trainingDataReviewFrameWidth.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		trainingDataReviewFrameWidth.setBackground(SWTResourceManager.getColor(255, 250, 205));
		trainingDataReviewFrameWidth.setToolTipText("Eg: 192.168.0.51");
		trainingDataReviewFrameWidth.setText("176");
		
		lblTrainingFileName = new Label(trainingDataReviewConfigComposite, SWT.NONE);
		lblTrainingFileName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblTrainingFileName.setText("Training \r\nFile Name");
		lblTrainingFileName.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		trainingFileNameUnderReview = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		GridData gd_trainingFileNameUnderReview = new GridData(SWT.FILL, SWT.CENTER, true, true, 3, 1);
		gd_trainingFileNameUnderReview.widthHint = 249;
		trainingFileNameUnderReview.setLayoutData(gd_trainingFileNameUnderReview);
		trainingFileNameUnderReview.setBackground(SWTResourceManager.getColor(255, 250, 205));
		trainingFileNameUnderReview.setToolTipText("Eg: D:\\Vikas\\TrainingData");
		
		btnLoadTrainingDataFile = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		btnLoadTrainingDataFile.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 4, 1));
		btnLoadTrainingDataFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logInfoToApplicationDisplay("Info: Button to load Training Data File for review pressed");
				//Validate User Inputs
				if(!Utilities.validateInteger(trainingDataReviewFrameWidth.getText(), 0, 176)){
					displayErrorMessageOnscreen("Training data Frame Width must have a value between 0 and 176");
				}else if(!Utilities.validateInteger(trainingDataReviewFrameHeight.getText(), 0, 144)){
					displayErrorMessageOnscreen("Training data Frame Height must have a value between 0 and 144");
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
		btnLoadTrainingDataFile.setText("Load");
		
		label_2 = new Label(trainingDataReviewConfigComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_label_2.widthHint = 380;
		label_2.setLayoutData(gd_label_2);
		label_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		label_2.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		
		btnPreviousTrainingDataImage = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		GridData gd_btnPreviousTrainingDataImage = new GridData(SWT.RIGHT, SWT.CENTER, true, true, 2, 1);
		gd_btnPreviousTrainingDataImage.widthHint = 184;
		gd_btnPreviousTrainingDataImage.heightHint = 26;
		btnPreviousTrainingDataImage.setLayoutData(gd_btnPreviousTrainingDataImage);
		btnPreviousTrainingDataImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Display the PreviousImage in the Training Data File
				if(displayTrainingData != null){
					displayTrainingData.displayPreviousImage(predictUsingNN, predictionInProgress);					
				}
			}
		});
		btnPreviousTrainingDataImage.setText("Previous Training Set");
		btnPreviousTrainingDataImage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		
		btnNextTrainingDataImage = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		GridData gd_btnNextTrainingDataImage = new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);
		gd_btnNextTrainingDataImage.heightHint = 27;
		gd_btnNextTrainingDataImage.widthHint = 170;
		btnNextTrainingDataImage.setLayoutData(gd_btnNextTrainingDataImage);
		btnNextTrainingDataImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Display the NextImage in the Training Data File
				if(displayTrainingData != null){
					displayTrainingData.displayNextImage(predictUsingNN, predictionInProgress);
				}
			}
		});
		btnNextTrainingDataImage.setText("Next TrainingSet");
		btnNextTrainingDataImage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		
		btnDeleteTrainingDataImage = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		btnDeleteTrainingDataImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Delete the current image in the Training Data File
				if(displayTrainingData != null){
					displayTrainingData.deleteCurrentImage();
				}
			}
		});
		GridData gd_btnDeleteTrainingDataImage = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
		gd_btnDeleteTrainingDataImage.widthHint = 173;
		btnDeleteTrainingDataImage.setLayoutData(gd_btnDeleteTrainingDataImage);
		btnDeleteTrainingDataImage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnDeleteTrainingDataImage.setText("Delete Training Set");
		
		btnReassignDirection = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		btnReassignDirection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//ReAssign the direction which was captured for the current training data set
				//System.out.println(comboReAssignDirection.getText()+","+comboReAssignDirection.getText().trim());
				if(displayTrainingData != null){
					int newSteeringDirection = -1;
					if(comboReAssignDirection.getText().trim().startsWith("Forward")){
						logInfoToApplicationDisplay("INFO: Will reassign the steering direction to - Forward");
						newSteeringDirection = Integer.valueOf(FeatureMessage.steerforward);
					}else if(comboReAssignDirection.getText().trim().startsWith("Right")){
						logInfoToApplicationDisplay("INFO: Will reassign the steering direction to - Right");
						newSteeringDirection = Integer.valueOf(FeatureMessage.steerRight);
					}else if(comboReAssignDirection.getText().trim().startsWith("Left")){
						logInfoToApplicationDisplay("INFO: Will reassign the steering direction to - Left");
						newSteeringDirection = Integer.valueOf(FeatureMessage.steerLeft);
					}else{
						logWarningToApplicationDisplay("WARNING: Unable to understand the provided steering direction for reassignment");
					}
					
					if(newSteeringDirection != -1){
						displayTrainingData.reAssignSteeringDirection(newSteeringDirection);
					}
					
				}
			}
		});
		btnReassignDirection.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnReassignDirection.setText("ReAssign Direction");
		
		comboReAssignDirection = new Combo(trainingDataReviewConfigComposite, SWT.NONE);
		GridData gd_comboReAssignDirection = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_comboReAssignDirection.minimumWidth = 65;
		gd_comboReAssignDirection.widthHint = 87;
		comboReAssignDirection.setLayoutData(gd_comboReAssignDirection);
		comboReAssignDirection.add("Forward");
		comboReAssignDirection.add("Right");
		comboReAssignDirection.add("Left");
		
		label = new Label(trainingDataReviewConfigComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		label.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_label.widthHint = 379;
		label.setLayoutData(gd_label);
		
		lblPixelRowsToStripFromTopTrain = new Label(trainingDataReviewConfigComposite, SWT.NONE);
		lblPixelRowsToStripFromTopTrain.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPixelRowsToStripFromTopTrain.setText("Pixel Rows to \r\nStrip from Top");
		
		pixelRowsToStripFromTopTraining = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		pixelRowsToStripFromTopTraining.setBackground(SWTResourceManager.getColor(245, 245, 220));
		pixelRowsToStripFromTopTraining.setText("0");
		GridData gd_pixelRowsToStripFromTopTraining = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_pixelRowsToStripFromTopTraining.widthHint = 28;
		pixelRowsToStripFromTopTraining.setLayoutData(gd_pixelRowsToStripFromTopTraining);
		
		lblPixelRowsToStripFromBotTrain = new Label(trainingDataReviewConfigComposite, SWT.NONE);
		lblPixelRowsToStripFromBotTrain.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPixelRowsToStripFromBotTrain.setText("Pixel Rows to \r\nStrip from Bottom");
		
		pixelRowsToStripFromBottomTraining = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		pixelRowsToStripFromBottomTraining.setBackground(SWTResourceManager.getColor(245, 245, 220));
		pixelRowsToStripFromBottomTraining.setText("0");
		GridData gd_pixelRowsToStripFromBottomTraining = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_pixelRowsToStripFromBottomTraining.widthHint = 27;
		pixelRowsToStripFromBottomTraining.setLayoutData(gd_pixelRowsToStripFromBottomTraining);
		
		btnResizeTrainingFile = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		btnResizeTrainingFile.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 4, 1));
		btnResizeTrainingFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logInfoToApplicationDisplay("Info: Button to Resize Training Data File pressed");
				//Validate User Inputs
				if(!Utilities.validateInteger(String.valueOf((Integer.valueOf(pixelRowsToStripFromTopTraining.getText()) + Integer.valueOf(pixelRowsToStripFromBottomTraining.getText()))), 0, Integer.valueOf(trainingDataReviewFrameHeight.getText()))){
					displayErrorMessageOnscreen("Sum of pixels rows to be stripped from top and bottom cannot exceed the current image height");
				}else{
					//Cancel any currently running thread
					if(displayTrainingData != null){
						displayTrainingData.cancel();
					}
					//Resize file
					try{
						Utilities.resizeTrainingImage(trainingFileNameUnderReview.getText(), Integer.valueOf(trainingDataReviewFrameHeight.getText()), Integer.valueOf(trainingDataReviewFrameWidth.getText()), Integer.valueOf(pixelRowsToStripFromTopTraining.getText()), Integer.valueOf(pixelRowsToStripFromBottomTraining.getText()));
						displayInfoMessageOnscreen("Successfully finished resizing the images in the file");
					}catch (IOException e1){
						logErrorToApplicationDisplay(e1, "ERROR: IOException when trying to executing the resize");
					}
				}
			}
		});
		btnResizeTrainingFile.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnResizeTrainingFile.setText("Resize Images in TrainingSet");
		
		label_1 = new Label(trainingDataReviewConfigComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_label_1.widthHint = 375;
		label_1.setLayoutData(gd_label_1);
		label_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
		
		capturedDataDirectoryName = new Text(trainingDataReviewConfigComposite, SWT.BORDER);
		capturedDataDirectoryName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		capturedDataDirectoryName.setToolTipText("Directory where the captured data is present in .csv files");
		capturedDataDirectoryName.setBackground(SWTResourceManager.getColor(255, 228, 196));
		
		btnGenerateSets = new Button(trainingDataReviewConfigComposite, SWT.NONE);
		GridData gd_btnGenerateSets = new GridData(SWT.CENTER, SWT.CENTER, false, false, 4, 1);
		gd_btnGenerateSets.widthHint = 382;
		btnGenerateSets.setLayoutData(gd_btnGenerateSets);
		btnGenerateSets.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logInfoToApplicationDisplay("Info: Will start generating Training, Cross Validation and Testing data files");
				if(capturedDataDirectoryName.getText() != null){
					GenerateMachineLearningDataSets GMLD = new GenerateMachineLearningDataSets();
					GMLD.createDataFiles(capturedDataDirectoryName.getText(), display);
				}else{
					displayErrorMessageOnscreen("Supply a value for the directory where the captured data is present");
				}
			}
		});
		btnGenerateSets.setSize(349, 30);
		btnGenerateSets.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnGenerateSets.setText("Generate Training, Cross \r\nValidation and Testing sets");
		
		trainingDataReviewNavgationDetails = new Composite(trainingDataReviewComposite, SWT.NONE);
		trainingDataReviewNavgationDetails.setLayout(new GridLayout(2, false));
		trainingDataReviewNavgationDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		lblCapturedTrainingData = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		lblCapturedTrainingData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 2, 1));
		lblCapturedTrainingData.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblCapturedTrainingData.setText("TrainingSet Image");
		lblCapturedTrainingData.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		
		lblTrainingDataReview = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		GridData gd_lblTrainingDataReview = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_lblTrainingDataReview.widthHint = 255;
		gd_lblTrainingDataReview.minimumWidth = 200;
		gd_lblTrainingDataReview.minimumHeight = 200;
		lblTrainingDataReview.setLayoutData(gd_lblTrainingDataReview);
		lblTrainingDataReview.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblTrainingDataReview.setBackground(SWTResourceManager.getColor(176, 224, 230));
		
		lblCapturedTrainingsetNavigation = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		GridData gd_lblCapturedTrainingsetNavigation = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1);
		gd_lblCapturedTrainingsetNavigation.widthHint = 64;
		lblCapturedTrainingsetNavigation.setLayoutData(gd_lblCapturedTrainingsetNavigation);
		lblCapturedTrainingsetNavigation.setText("Actual\r\nDirection");
		lblCapturedTrainingsetNavigation.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblCapturedTrainingsetNavigation.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		
		lblPredictedTrainingsetNavigation = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		lblPredictedTrainingsetNavigation.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblPredictedTrainingsetNavigation.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictedTrainingsetNavigation.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblPredictedTrainingsetNavigation.setText("Predicted\r\nDirection");
		
		lblTrainingDataSteeringDirection = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		GridData gd_lblTrainingDataSteeringDirection = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		gd_lblTrainingDataSteeringDirection.widthHint = 133;
		lblTrainingDataSteeringDirection.setLayoutData(gd_lblTrainingDataSteeringDirection);
		lblTrainingDataSteeringDirection.setAlignment(SWT.CENTER);
		
		lblTrainingDataPredictedSteeringDirection = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		GridData gd_lblTrainingDataPredictedSteeringDirection = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblTrainingDataPredictedSteeringDirection.widthHint = -29;
		lblTrainingDataPredictedSteeringDirection.setLayoutData(gd_lblTrainingDataPredictedSteeringDirection);
		
		lblPredictionConfidence = new Label(trainingDataReviewNavgationDetails, SWT.NONE);
		lblPredictionConfidence.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictionConfidence.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblPredictionConfidence.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		lblPredictionConfidence.setText("Prediction Confidence");
		
		textTrainingPredictionConfidence = new Text(trainingDataReviewNavgationDetails, SWT.BORDER);
		textTrainingPredictionConfidence.setEditable(false);
		textTrainingPredictionConfidence.setBackground(SWTResourceManager.getColor(255, 255, 204));
		textTrainingPredictionConfidence.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		loggingComposite = new Composite(shell, SWT.NONE);
		loggingComposite.setLayout(new GridLayout(5, false));
		GridData gd_loggingComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
		gd_loggingComposite.widthHint = 339;
		gd_loggingComposite.heightHint = 568;
		loggingComposite.setLayoutData(gd_loggingComposite);
		
		Label lblApplicationLog = new Label(loggingComposite, SWT.NONE);
		lblApplicationLog.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 5, 1));
		lblApplicationLog.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblApplicationLog.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblApplicationLog.setText("Application Log");
		
		lblLogLevel = new Label(loggingComposite, SWT.NONE);
		lblLogLevel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblLogLevel.setText("Log Level");
		lblLogLevel.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		new Label(loggingComposite, SWT.NONE);
		new Label(loggingComposite, SWT.NONE);
		
		chkbtnInfoLogging = new Button(loggingComposite, SWT.CHECK);
		chkbtnInfoLogging.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
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
		chkbtnInfoLogging.setText("Info");
		
		chkbtnErrorsWarnings = new Button(loggingComposite, SWT.CHECK);
		chkbtnErrorsWarnings.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
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
		
		applicationLog = new StyledText(loggingComposite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd_applicationLog = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
		gd_applicationLog.heightHint = 499;
		gd_applicationLog.widthHint = 325;
		applicationLog.setLayoutData(gd_applicationLog);
		applicationLog.setEditable(false);
		applicationLog.setEnabled(true);
		applicationLog.setAlwaysShowScrollBars(true);
		applicationLog.setTextLimit(100);
		new Label(shell, SWT.NONE);
		
		predictionComposite = new Composite(shell, SWT.NONE);
		GridData gd_predictionComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_predictionComposite.widthHint = 557;
		predictionComposite.setLayoutData(gd_predictionComposite);
		predictionComposite.setLayout(new GridLayout(4, false));
		
		lblInputsForPrediction = new Label(predictionComposite, SWT.NONE);
		lblInputsForPrediction.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 4, 1));
		lblInputsForPrediction.setSize(147, 20);
		lblInputsForPrediction.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblInputsForPrediction.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblInputsForPrediction.setText("Inputs For Prediction");
		
		lblPredictNoOfHiddenLayers = new Label(predictionComposite, SWT.NONE);
		lblPredictNoOfHiddenLayers.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPredictNoOfHiddenLayers.setSize(145, 20);
		lblPredictNoOfHiddenLayers.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictNoOfHiddenLayers.setText("No Of Hidden Layers");
		
		textPredictNumberOfHiddenLayers = new Text(predictionComposite, SWT.BORDER);
		textPredictNumberOfHiddenLayers.setSize(78, 26);
		textPredictNumberOfHiddenLayers.setBackground(SWTResourceManager.getColor(255, 255, 204));
		textPredictNumberOfHiddenLayers.setText("1");
		
		btnAssociatePredictionWeights = new Button(predictionComposite, SWT.NONE);
		btnAssociatePredictionWeights.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(predictionInProgress){
					logInfoToApplicationDisplay("Info: Removing Prediction thread, will not predict anymore");
					//Disconnect from prediction
					if (predictUsingNN != null){
						predictUsingNN.cancel();
					}
					updatePredictionStatus("stopped");

				}else{
					logInfoToApplicationDisplay("Info: Will try to associate the weights for prediction");
					//Check if required information has been supplied
					if(!Utilities.validateInteger(String.valueOf((Integer.valueOf(textPredictNumberOfHiddenLayers.getText()))), 1, 3)){
						displayErrorMessageOnscreen("Number of hidden layer must be either 1, 2 or 3");
					}else if((Integer.valueOf(textPredictNumberOfHiddenLayers.getText()) == 1) && ((textPredictWeightsForFirstHiddenLayer.getText().isEmpty()) || (textPredictWeightsForInputLayer.getText().isEmpty()))){
						displayErrorMessageOnscreen("Provide weights for Input layer and first hidden layer");
					}else if((Integer.valueOf(textPredictNumberOfHiddenLayers.getText()) == 2) && ((textPredictWeightsForFirstHiddenLayer.getText().isEmpty()) || (textPredictWeightsForInputLayer.getText().isEmpty()) || (textPredictWeightsForSecondHiddenLayer.getText().isEmpty()))){
						displayErrorMessageOnscreen("Provide weights for Input layer, first and second hidden layers");
					}else if((Integer.valueOf(textPredictNumberOfHiddenLayers.getText()) == 3) && ((textPredictWeightsForFirstHiddenLayer.getText().isEmpty()) || (textPredictWeightsForInputLayer.getText().isEmpty()) || (textPredictWeightsForSecondHiddenLayer.getText().isEmpty()) || (textPredictWeightsForThirdHiddenLayer.getText().isEmpty()))){
						displayErrorMessageOnscreen("Provide weights for Input layer, first, second and third hidden layers");
					}else{
						//Create a thread to start the prediction 
						try{
							if(Integer.valueOf(textPredictNumberOfHiddenLayers.getText()) == 1){
								String[] weightFileNames = {textPredictWeightsForInputLayer.getText(), textPredictWeightsForFirstHiddenLayer.getText()};
								predictUsingNN = new PredictUsingNN(display, weightFileNames);
							}else if(Integer.valueOf(textPredictNumberOfHiddenLayers.getText()) == 2){
								String[] weightFileNames = {textPredictWeightsForInputLayer.getText(), textPredictWeightsForFirstHiddenLayer.getText(), textPredictWeightsForSecondHiddenLayer.getText()};
								predictUsingNN = new PredictUsingNN(display, weightFileNames);
							}else{
								String[] weightFileNames = {textPredictWeightsForInputLayer.getText(), textPredictWeightsForFirstHiddenLayer.getText(), textPredictWeightsForSecondHiddenLayer.getText(), textPredictWeightsForThirdHiddenLayer.getText()};
								predictUsingNN = new PredictUsingNN(display, weightFileNames);
							}
						}catch(FileNotFoundException e1){
							logErrorToApplicationDisplay(e1,"ERROR: Unable to find weights file");
						}catch(IOException e1){
							logErrorToApplicationDisplay(e1,"ERROR: When trying to read the file");
						}
						
					}
				}
				
			}
		});
		btnAssociatePredictionWeights.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnAssociatePredictionWeights.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnAssociatePredictionWeights.setText("Start Prediction");
		
		lblPredictWeightsForInputLayer = new Label(predictionComposite, SWT.NONE);
		lblPredictWeightsForInputLayer.setSize(87, 40);
		lblPredictWeightsForInputLayer.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictWeightsForInputLayer.setText("Weights for \r\nInput Layer");
		
		textPredictWeightsForInputLayer = new Text(predictionComposite, SWT.BORDER);
		GridData gd_textPredictWeightsForInputLayer = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textPredictWeightsForInputLayer.widthHint = 173;
		textPredictWeightsForInputLayer.setLayoutData(gd_textPredictWeightsForInputLayer);
		textPredictWeightsForInputLayer.setSize(180, 26);
		textPredictWeightsForInputLayer.setBackground(SWTResourceManager.getColor(255, 255, 204));
		
		lblPredictWeightsForFirstHiddenLayer = new Label(predictionComposite, SWT.NONE);
		lblPredictWeightsForFirstHiddenLayer.setSize(127, 40);
		lblPredictWeightsForFirstHiddenLayer.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictWeightsForFirstHiddenLayer.setText("Weights for \r\nFirst Hidden Layer");
		
		textPredictWeightsForFirstHiddenLayer = new Text(predictionComposite, SWT.BORDER);
		GridData gd_textPredictWeightsForFirstHiddenLayer = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textPredictWeightsForFirstHiddenLayer.widthHint = 172;
		textPredictWeightsForFirstHiddenLayer.setLayoutData(gd_textPredictWeightsForFirstHiddenLayer);
		textPredictWeightsForFirstHiddenLayer.setSize(180, 26);
		textPredictWeightsForFirstHiddenLayer.setBackground(SWTResourceManager.getColor(255, 255, 204));
		
		lblPredictWeightsForSecondHiddenLayer = new Label(predictionComposite, SWT.NONE);
		lblPredictWeightsForSecondHiddenLayer.setSize(146, 40);
		lblPredictWeightsForSecondHiddenLayer.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictWeightsForSecondHiddenLayer.setText("Weights for \r\nSecond Hidden Layer");
		
		textPredictWeightsForSecondHiddenLayer = new Text(predictionComposite, SWT.BORDER);
		textPredictWeightsForSecondHiddenLayer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textPredictWeightsForSecondHiddenLayer.setSize(255, 26);
		textPredictWeightsForSecondHiddenLayer.setBackground(SWTResourceManager.getColor(255, 255, 204));
		
		lblPredictWeightsForThirdHiddenLayer = new Label(predictionComposite, SWT.NONE);
		lblPredictWeightsForThirdHiddenLayer.setSize(133, 40);
		lblPredictWeightsForThirdHiddenLayer.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblPredictWeightsForThirdHiddenLayer.setText("Weights for \r\nThird Hidden Layer");
		
		textPredictWeightsForThirdHiddenLayer = new Text(predictionComposite, SWT.BORDER);
		textPredictWeightsForThirdHiddenLayer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textPredictWeightsForThirdHiddenLayer.setSize(654, 26);
		textPredictWeightsForThirdHiddenLayer.setBackground(SWTResourceManager.getColor(255, 255, 204));

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
		if(connectedToSensor){
			btnConnectToSensor.setText("Disconnect");
			if(connectedToController){
				//Enable direction buttons
				btnForward.setEnabled(true);
				btnLeft.setEnabled(true);
				btnReverse.setEnabled(true);
				btnRight.setEnabled(true);
			}else{
				//Disable direction buttons
				btnForward.setEnabled(false);
				btnLeft.setEnabled(false);
				btnReverse.setEnabled(false);
				btnRight.setEnabled(false);
			}

		}else{
			btnConnectToSensor.setText("Connect");
			//Disable direction buttons
			btnForward.setEnabled(false);
			btnLeft.setEnabled(false);
			btnReverse.setEnabled(false);
			btnRight.setEnabled(false);
		}
	}
	
	/**
	 * Update the current status of connection to the Arduino / Controller
	 * @param currentStatus
	 */
	protected static synchronized void updateControllerConnectionStatus(boolean connected){
		connectedToController = connected;
		if(connectedToController){
			btnConnectToController.setText("Disconnect");	
			if(connectedToSensor){
				//Enable direction buttons
				btnForward.setEnabled(true);
				btnLeft.setEnabled(true);
				btnReverse.setEnabled(true);
				btnRight.setEnabled(true);
			}else{
				//Disable direction buttons
				btnForward.setEnabled(false);
				btnLeft.setEnabled(false);
				btnReverse.setEnabled(false);
				btnRight.setEnabled(false);
			}
		}else{
			btnConnectToSensor.setText("Connect");
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
		if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerforward)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Forward.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerReverse)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Reverse.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerLeft)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Left.jpg"));
		}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerRight)){
			lblTrainingDataSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Right.jpg"));
		}else{
			logWarningToApplicationDisplay("Warning: Unable to understand the captured Steering Direction: "+steeringDirection);
		}
	}
	
	/**
	 * When reviewing the Training data, used to display the predicted Steering Direction
	 * @param steeringDirection
	 */
	protected static synchronized void displayPredictedTrainingDataSteeringDirection(String steeringDirection, Boolean resetImage){
		if(resetImage){
			lblTrainingDataPredictedSteeringDirection.setImage(null);
		}else{
			logInfoToApplicationDisplay("Info: Prediced SteeringDirection is: "+steeringDirection);
			if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerforward)){
				lblTrainingDataPredictedSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Forward.jpg"));
			}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerReverse)){
				lblTrainingDataPredictedSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Reverse.jpg"));
			}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerLeft)){
				lblTrainingDataPredictedSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Left.jpg"));
			}else if(Integer.valueOf(steeringDirection) == Integer.valueOf(FeatureMessage.steerRight)){
				lblTrainingDataPredictedSteeringDirection.setImage(SWTResourceManager.getImage(DriverDisplayAndController.class, "/com/vikas/projs/ml/autonomousvehicle/images/Right.jpg"));
			}else{
				logWarningToApplicationDisplay("Warning: Unable to understand the predicted Steering Direction: "+steeringDirection);
			}
		}

	}
	
	/**
	 * Used to display message on the screen to the user in a message box / message dialog
	 * @param message
	 */
	protected static synchronized void displayErrorMessageOnscreen(String message){	
		MessageDialog.openError(shell, "Error", message);
	}
	
	/**
	 * Used to display message on the screen to the user in a message box / message dialog
	 * @param message
	 */
	protected static synchronized void displayInfoMessageOnscreen(String message){	
		MessageDialog.openInformation(shell, "Info", message);
	}
	
	
	/**
	 * Update the button text for previous, current and next image/training set
	 * @param previous
	 * @param current
	 * @param next
	 */
	protected static synchronized void updateTrainingDataFrameButtonText(int previous, int current, int next){
		btnPreviousTrainingDataImage.setText("Previous TrainingSet "+"("+previous+")");
		btnDeleteTrainingDataImage.setText("Delete Training Set "+"("+current+")");
		btnNextTrainingDataImage.setText("Next TrainingSet "+"("+next+")");
	}
	
	
	/**
	 * Update the text of Prediction button
	 * @param currentStatus
	 */
	protected static synchronized void updatePredictionStatus(String currentStatus){
		if(currentStatus.equalsIgnoreCase("started")){
			predictionInProgress = true;
			btnAssociatePredictionWeights.setText("Stop Prediction");	
		}else{
			btnAssociatePredictionWeights.setText("Start Prediction");
			predictionInProgress = false;
		}
	}

	/**
	 * Update the background of the Training Data Review label to reflect the prediction status
	 * @param status
	 */
	protected static synchronized void updateTrainingDataReviewLabelBgd(String status){
		if(status.equalsIgnoreCase("GREEN")){
			lblTrainingDataReview.setBackground(new Color(display, 173, 255, 47));
		}else if(status.equalsIgnoreCase("RED")){
			lblTrainingDataReview.setBackground(new Color(display, 255, 140, 0));
		}else{
			lblTrainingDataReview.setBackground(new Color(display, 176, 224, 230));
		}
	}
	
	
	/**
	 * Update the Steering direction Prediction confidence
	 * @param predictionConfidence
	 */
	protected static synchronized void updateSteeringPredictionConfidence(int predictionConfidence){
		if(predictionConfidence == -1){
			textTrainingPredictionConfidence.setText("");
		}else{
			textTrainingPredictionConfidence.setText(String.valueOf(predictionConfidence)+" %");
		}
	}
}
