package com.vikas.projs.ml.autonomousvehicle;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.widgets.Display;

/**
 * Used to control the Arduino
 * 
 * @author Vikas_Vijayakumar
 *
 */
public class VehicleController implements Runnable{

	private ArrayBlockingQueue<ControlMessage> controllerQueue;
	private Thread vehicleControllerThread;
	private String arduinoPortName;
	private Display display;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private SerialPort serialPort;
	private OutputStream serialOutStream;
	
	public VehicleController(String arduinoPortName, Display display, ArrayBlockingQueue<ControlMessage> controllerQueue){
		this.controllerQueue = controllerQueue;
		this.arduinoPortName = arduinoPortName;
		this.display = display;
		//Create and start a Thread
		vehicleControllerThread = new Thread(this);
		vehicleControllerThread.start();
	}
	
	@Override
	public void run() {        
		try {
			//Check if the given Port exists and obtain the identifier
			portIdentifier = CommPortIdentifier.getPortIdentifier(arduinoPortName);
			//Try to open the connection to the Port
            commPort = portIdentifier.open("TestingArduinoConnection",2000);
            //Verify that the Port is a Serial Port
            if ( commPort instanceof SerialPort ){
            	serialPort = (SerialPort) commPort;
            	//Set the Serial Port Parameters. 
            	//First parameter is the Baud Rate which is the data rate in bits per second 
            	//Rest are the number of data bits, stop bits and parity bits respectively
	            serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	            //Open an OutputStream on the Serial Port    
	            serialOutStream = serialPort.getOutputStream();
	            logInfoToApplicationDisplay("Info: Successfully connected to Controller using Serial Port");
	            updateControllerConnectionStatus(true);
	            
				//Until interrupted, continuously poll for Controls from the BlockingQueue
				//In case there are none in queue, the thread will block in anticipation of a new message
				while(!vehicleControllerThread.isInterrupted()){
					ControlMessage currentControlMessage;
					try {
						//Block until a Control Message arrives
						currentControlMessage = controllerQueue.take();
						logInfoToApplicationDisplay("Info: Successfully Received a Control Message from the Controller Queue with Steering Direction = "+currentControlMessage.getSteeringDirection());
						//Send to Controller
						serialOutStream.write(Integer.valueOf(currentControlMessage.getSteeringDirection()));
						serialOutStream.flush();
						logInfoToApplicationDisplay("Info: Successfully Sent a Control Message to the Controller with Steering Direction = "+currentControlMessage.getSteeringDirection());
					} catch (InterruptedException e) {
						logInfoToApplicationDisplay("Info: VehicleController thread has been interrupted, will disconnect from the Controller");
						disconnectFromController();
					}					
				}
	                
	        }else{
	        	logWarningToApplicationDisplay("Warning: Provided Port "+arduinoPortName+" doesnt seem to be a Serial Port");
	        	disconnectFromController();
	        }

			
		} catch (NoSuchPortException e) {
			logErrorToApplicationDisplay(e, "ERROR: Provided Serial Port Name seems to be incorrect: ");
			disconnectFromController();
		} catch (PortInUseException e) {
			logErrorToApplicationDisplay(e, "ERROR: Provided Serial Port "+arduinoPortName+" is already in use, cannot establish connection");
			disconnectFromController();
		} catch (UnsupportedCommOperationException e) {
			logErrorToApplicationDisplay(e, "ERROR: Parameter setting for the Serial Port seems to be incorrect");
			disconnectFromController();
		} catch (IOException e) {
			logErrorToApplicationDisplay(e, "ERROR: IOException when communicating with the Serial Port");
			disconnectFromController();
		}
	}

	private void cancel(){
		logInfoToApplicationDisplay("Info: Current vehicleController thread will be interuppted");
		vehicleControllerThread.interrupt();
	}
	
	protected void disconnectFromController(){
		this.cancel();
		//Close allocated resources
		if(serialOutStream != null){
			try {
				serialOutStream.close();
				logInfoToApplicationDisplay("Info: Successfully closed the Output Stream of the Serial Port");
			} catch (IOException e) {				
				logErrorToApplicationDisplay(e, "ERROR: IOException when trying to close the Output Stream of the Serial Port");
			}
		}
		if(serialPort != null){
			serialPort.close();
			logInfoToApplicationDisplay("Info: Successfully closed the connection to the Serial Port");
		}
		if(commPort != null){
			commPort.close();
			logInfoToApplicationDisplay("Info: Successfully closed the resources allocated to the CommPort");
		}
		
		updateControllerConnectionStatus(false);
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
	
	/**
	 * Proxy for the logWarningToApplicationDisplay function defined in DriverDisplayAndController
	 * @param logEntry
	 */
	private void logWarningToApplicationDisplay(final String logEntry){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.logWarningToApplicationDisplay(logEntry);
			}
		});
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
	 * Proxy for the updateControllerConnectionStatus function defined in DriverDisplayAndController
	 * @param connectionStatus
	 */
	private void updateControllerConnectionStatus(final boolean connectionStatus){
		display.syncExec(new Runnable(){
			public void run(){
				DriverDisplayAndController.updateControllerConnectionStatus(connectionStatus);
			}
		});
	}
}
