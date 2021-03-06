package mypkg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.*;
import javax.microedition.io.*;


/**
 * Class that implements an SPP Server which accepts single line of message from
 * an SPP client and sends a single line of response to the client.
 */
public class SimpleSPPServer {

	// start server
	private void startServer() throws IOException {

		// Create a UUID for SPP
		UUID uuid = new UUID("1101", true);
		// Create the servicve url
		String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";

		// open server url
		StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector
				.open(connectionString);

		// Wait for client connection
		System.out.println("\nServer Started. Waiting for clients to connect...");
		StreamConnection connection = streamConnNotifier.acceptAndOpen();

		RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
		System.out.println("Remote device address: " + dev.getBluetoothAddress());
		System.out.println("Remote device name: " + dev.getFriendlyName(true));

		// read string from spp client
		InputStream inStream = connection.openInputStream();
		BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
		String lineRead = bReader.readLine();
		System.out.println(lineRead);

		// send response to spp client
		OutputStream outStream = connection.openOutputStream();
		PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outStream));

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String sendStr = "";
		char cbuf[] = new char[255];

		while (!(sendStr.equalsIgnoreCase("exit"))) {
			if(br.ready()){
				sendStr = br.readLine();
				pWriter.write(sendStr + "\n");
				pWriter.flush();
				System.out.println("Sent text\n");
			}
			if(bReader.ready()){
				bReader.read(cbuf);
				System.out.println(cbuf);
				sendStr = cbuf.toString();
				for(int i = 0; i < cbuf.length; i++)
					cbuf[i] = 0;
			}
		}

		streamConnNotifier.close();

	}

	public static void main(String[] args) throws IOException {

		// display local device address and name
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		SimpleSPPServer sampleSPPServer = new SimpleSPPServer();
		sampleSPPServer.startServer();

	}
}