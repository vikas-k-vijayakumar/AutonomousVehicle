package com.vikas.projs.ml.autonomousvehicle.test;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestingArduinoConnection {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		connect("COM8");

	}

	
    public static void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open("TestingArduinoConnection",2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                //InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                out.write(0);
                Thread.sleep(1000);
                out.write(1);
                Thread.sleep(1000);
                out.write(2);
                Thread.sleep(1000);
                out.write(3);

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }
}
