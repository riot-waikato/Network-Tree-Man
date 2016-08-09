import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

// Main port: 50176
// Backup   : 50277

class ServHost {

	private static ServerSocket sockListen;
	private static Random rand = new Random();

	public static void main(String[] args) {
	    int port;
	    if(available(50176))
		{
		    port = 50176;
		}
	    else if(available(50277))
		{
		    port = 50277;
		}
	    else
		{
		    port= -1;
		    System.out.println("Ports taken! Closing down!");
		    return;
		}
	    try { 
		sockListen = new ServerSocket(port);
		System.out.println("Listening on 50176");
		while(true)
		    {
			Socket connectedSocket = sockListen.accept();
			
			System.out.println("Connected to: " + connectedSocket.getPort());
			HostThread thisHost = new HostThread(connectedSocket);
			
			System.out.println("*Starting Thread");
			thisHost.start();
			System.out.println("Thread Finished");
		    }
	    } 
	    catch (Exception ex) {ex.printStackTrace();}
	}
   
    static class HostThreadManager implements Runnable {
	private boolean started = false;
	private boolean finished = false;

	public void init() {
	    started = true;
	}

	public void halt() {
	    finished = true;
	}

	@Override public void run() {
	    while(!started)
		try { Thread.sleep(5);}
		catch (Exception e){}
	    System.out.println("Test");
	    ////////////////////////////////////////////////////
	    int port;
	    if(available(50176))
		{
		    port = 50176;
		}
	    else if(available(50277))
		{
		    port = 50277;
		}
	    else
		{
		    port= -1;
		    System.out.println("Ports taken! Closing down!");
		    return;
		}
	    /////////////////////////////////////////////////////////
	    try {
		sockListen = new ServerSocket(port);
		System.out.println("Listening on 50176");
	    } catch (Exception e) {}
	    while(!finished) {
		try { 
		    while(!finished)
			{
			    Socket connectedSocket = sockListen.accept();
			    
			    System.out.println("Connected to: " + connectedSocket.getPort());
			    HostThread thisHost = new HostThread(connectedSocket);
			    
			    System.out.println("*Starting Thread");
			    thisHost.start();
			    System.out.println("Thread Finished");
			}
		} 
		catch (Exception ex) {ex.printStackTrace();}
	    }
	}
    }

    static class HostThread extends Thread
    {
	
	private final boolean DEBUG = true;
	private Socket connectedSocket;
	
	public HostThread(Socket connectedSocket)
	{
	    this.connectedSocket = connectedSocket;
	    if(connectedSocket == null)
		System.out.println("You fucked up!");
	    
	}
	private char[] genResponse(String challenge, String MAC, String PSK) {
	    char[] ch = new char[challenge.length()];
	    for(int i = 0; i < ch.length; i++) {
		int k = challenge.charAt(i);
		k += MAC.charAt(i%MAC.length());
		k *= PSK.charAt(i%PSK.length());
		k%=128;;
		ch[i] = (char)k;
		
	    }
	    return ch;
	}
	private char[] genResponseX(String challenge, String MAC, String PSK) {
	    char[] ch = new char[challenge.length()];
	    for(int i = 0; i < ch.length; i++) {
		int k = challenge.charAt(i);
		k += MAC.charAt(i%MAC.length());
		k *= PSK.charAt(i%PSK.length());
		ch[i] = (char) k;
	    }
	    for(int i = ch.length - 1; i > 0; i--) {
		ch[i] ^= ch[i-1];
		ch[i] = (char)(32 + ch[i]%95);
	    }
	    ch[0] = (char)(32  + ch[0] % 95);
	    return ch;
	}
	
	private char[] genChallenge(int len) {
	    char[] rands = new char[len];
	    for(int i = 0; i < len; i++) {
		rands[i] = (char) (32 + rand.nextInt(95));
	    }
	    return rands;
	}
	
	@Override
	    public void run()
	{
	    int chlen;
	    String challenge;
	    try{
		BufferedReader reader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
		DataOutputStream os = new DataOutputStream(connectedSocket.getOutputStream());
		/* Generate Challenge */
		challenge = new String(genChallenge(30));			
		System.out.println("Challenge: " + challenge);
		
		/* Generate response */
		char[] cs = genResponseX(challenge, AuthKeys.getMac(), AuthKeys.getPreSharedKey());
		
		/* Send challenge */
		os.writeBytes( challenge +"\n");
		System.out.println(  "Sent challenge:     " + challenge);
		
		
		/* get response */
		String nc = reader.readLine();
		System.out.println(  "Expected response:  " + new String(cs));
		System.out.println(  "Received response:  " + nc);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		String challenge_two = reader.readLine();
		System.out.println("\nRecieved challenge: " + challenge_two);
		
		char[] res = genResponseX(challenge_two, AuthKeys.getMac(), AuthKeys.getPreSharedKey());
		os.writeBytes(new String(res) + "\n");
		System.out.println(  "Sent response:      " + new String(res));
		
		if(new String(cs).equals(nc))
		    System.out.println("MATCH!");			}
	    catch(Exception ex){ex.printStackTrace();}
	}
    }
    
    public static boolean available(int port) {
	if (port < 0 || port > 65535) {
	    throw new IllegalArgumentException("Invalid start port: " + port);
	}
	
	ServerSocket ss = null;
	DatagramSocket ds = null;
	try {
	    ss = new ServerSocket(port);
	    ss.setReuseAddress(true);
	    ds = new DatagramSocket(port);
	    ds.setReuseAddress(true);
	    return true;
	} catch (IOException e) {
	} finally {
	    if (ds != null) {
		ds.close();
	    }
	    
	    if (ss != null) {
		try {
		    ss.close();
		} catch (IOException e) {
		    /* should not be thrown */
		}
	    }
	}
	
	return false;
    }
}

class AuthKeys {

	private static final String preSharedKey = "HelloWorldSecurePasswordBro";
	private static final String MAC = "90-2E-1C-06-0E-5E";
	
	
	public static String getPreSharedKey()
	{
		return preSharedKey;
	}
	
	public static String getMac()
	{
		return MAC;
	}
}