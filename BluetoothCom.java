package com.example.scalecontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

public class BluetoothCom extends Thread{
	
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	static OutputStream outStream = null;
	
	// Well known SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Insert your server's MAC address
	private static String address = "00:1A:7D:DA:71:09";
	
	public static Handler inputHandler, outputHandler;
	
	public BluetoothCom(Handler msgHandler){
		inputHandler = msgHandler;
		outputHandler = new MessageHandler();
		Log.d("DEBUG", "...In onCreate()..."); 	
	}
	
	@Override
	public void run() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	   	if(btConnect())
	   		mbtReadWrite();
	}
	
	private void mbtReadWrite(){
		InputStream inStream;
		Bundle b = new Bundle(2);
		Message msg;
		
		try {
			inStream = btSocket.getInputStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
			String lineRead = "";
			Log.d("DEBUG", "Listening to server\n");
			
			while(!(lineRead.equalsIgnoreCase("exit"))){
				lineRead = null;
				do{
					if(bReader.ready())
						lineRead = bReader.readLine();
				}while(lineRead == null);
				b.putString("key1", lineRead);
				msg = inputHandler.obtainMessage(MessageHandler.MsgType.WEIGHT_DATA.ordinal());
				msg.setData(b);
				inputHandler.sendMessage(msg);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("DEBUG", "IO exception occurred while trying to get input stream");
			return;
		}
		//out.append("Done listening\n");
		Log.d("DEBUG", "...In onPause()...");

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				Log.d("ERROR",
						"In onPause() and failed to flush output stream: "
						+ e.getMessage() + ".");
				return;
			}
		}

		try {
			btSocket.close();
		} catch (IOException e2) {
			Log.d("ERROR", "In onPause() and failed to close socket."
					+ e2.getMessage() + ".");
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	private boolean btConnect(){
		ParcelUuid uuids[];

		Log.d("DEBUG", "...In onResume...\n...Attempting client connect...");

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		
		if(device.fetchUuidsWithSdp()){
			Log.d("DEBUG", "UUIDs fetched");
			Log.d("DEBUG", "UUIDs:");
			
			uuids = device.getUuids();
			try{
			if(uuids.length > 0){
				for (int i = 0; i < uuids.length; i++){
					Log.d("DEBUG", uuids[i].getUuid().toString());
				}
			}
			}catch(NullPointerException e){
				e.printStackTrace();
				return false;
			}
		}

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Log.d("ERROR", "In onResume() and socket create failed: "
					+ e.getMessage() + ".");
			return false;
		}
		Log.d("DEBUG", "btsocket created");
		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();
		// Establish the connection. This will block until it connects.
		try {
			btSocket.connect();
			Log.d("DEBUG", "...Connection established and data link opened...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d("ERROR", "In onResume() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
			return false;
		}

		// Create a data stream so we can talk to server.
		String message = "Hello from Android.\n";

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d("ERROR",  "In onResume() and output stream creation failed:"
							+ e.getMessage() + ".");
			return false;
		}
		sendBtMsg(message);
		Bundle b = new Bundle(1);
		Message msg = inputHandler.obtainMessage(MessageHandler.MsgType.WEIGHT_DATA.ordinal());
		b.putString("key1", "Connected");
		msg.setData(b);
		inputHandler.sendMessage(msg);
		
		return true;
	}
	
	public static void sendBtMsg(String s){
		byte[] msgBuffer = s.getBytes();
		Log.d("DEBUG", "Attempting to send message to server");
		Log.d("DEBUG", "The message is: " + s);
		try{
			outStream.write(msgBuffer);
		}catch(IOException e){
			Log.d("ERROR", "Error sending message to server: " + e.getMessage());
		}
	}
	
}
