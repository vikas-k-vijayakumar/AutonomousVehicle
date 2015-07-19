package com.vikas.projs.ml.autonomousvehicle.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class SampleServer {

	public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(6666);
        try {
            while (true) {
                new FeatureStreamer(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class FeatureStreamer extends Thread {
        private Socket socket;
        private int clientNumber;

        public FeatureStreamer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        public void run() {
            try {

                DataInputStream din = new DataInputStream(socket.getInputStream());
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

                dout.writeUTF(Inet4Address.getLocalHost().getHostAddress());
                
                String handshake = din.readUTF();
                log("Handshake received from: "+handshake);
                
                Random randomPixelValues = new Random();
                for(int iteration=0;iteration<100;iteration++){
                    dout.writeInt(200);
                    dout.writeInt(200);
                    dout.writeInt(8);
                    byte[] pixelData = new byte[40000];
                	for(int i=0;i<40000;i++){
                		pixelData[i] = (byte) randomPixelValues.nextInt(255);             			
                	}
                	dout.write(pixelData, 0, 40000);
                	dout.flush();
                }

                while (true) {
                	int input = din.read();
                    
                    if (input == -1) {
                        break;
                    }
                    
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        private void log(String message) {
            System.out.println(message);
        }
    }
	
}
