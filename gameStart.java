import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class gameStart extends Application {

	////////////////////////////////

	// TRUE FOR MASTER, FALSE FOR SLAVE
	static boolean runMaster = true;

	////////////////////////////////

    static SlaveTesting gameS;
    static Master gameM;
    static Socket clientSocket;

    public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage theStage) throws Exception {
		connect();
		if (runMaster) {
			gameM = new Master();
			gameM.start(theStage);
		} else {
			gameS = new SlaveTesting();
			gameS.gameStart(theStage);
		}
	}

	public static String connect() {
		try{
			if (runMaster) {
				clientSocket = new Socket("localhost", 5001);
			} else {
				clientSocket = new Socket("localhost", 5000);
			}
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			return inFromServer.readLine();
		} catch(Exception e) {
			e.printStackTrace();
		    Platform.exit();
		    return "";
		}
	}

	public static void sendString(String str) {
		PrintWriter outToServer;
		try {
			outToServer = new PrintWriter(clientSocket.getOutputStream(),true);
			outToServer.println(str);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		    Platform.exit();
		    return;
		}
	}

	public static String receiveString() {
		try {
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			return inFromServer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		    Platform.exit();
		    return "";
		}
	}
}
