import java.io.*;
import java.net.*;

public class Server_Test_Auth {
    public static void main(String[] argv) {
	checkCon(argv[0], 2158, 2500);


    }

    public static boolean checkCon(String IP, int port, int timeout) {
	try {
	    String input_token = "asdf";
	    String expected_response = "fdsa";
	    
	    Socket clientSocket = new Socket();
	    clientSocket.setSoTimeout(timeout);
	    clientSocket.connect(new InetSocketAddress(IP, port), timeout);


	    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    
	    outToServer.writeBytes(input_token + "\n");
	    String response = inFromServer.readLine();
	    System.out.println("Response matches input: " + expected_response.equals(response));
	    clientSocket.close();
	    
	    return expected_response.equals(response);
	}
	catch (Exception e) {
	    System.out.println(e);
	    return false;
	}
    }

}
