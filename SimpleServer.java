import java.io.*;
import java.net.*;
import java.util.*;

/*
  boolean   check             (challenge, addr, port)   check if connected, return bool result
                                                        print based on DEBUG_OUTPUT
							socket timeouts on CON/RES_TIMEOUT

  void      sendMessage       (stream, in, print, tag)         
  String    getMessage        (stream, tag)
  char[]    genChallenge      (length)
  void      numprint          (arr)                     print message, char array
  void      numprintmessage   (arr, message)            print message, char array
  char[]    genResponse       (challenge, MAC, PSK)     generate challenge response (no xor)
  char[]    genResponseX      (challenge, MAC, PSK)     generate challenge response with xor
  void      cprint            ()                        print if DEBUG_OUTPUT
  void      cprintln          ()                        print line if DEBUG_OUTPUT
 */

class TCPC
{
    static String out;
    private static int CON_TIMEOUT;
    private static int RES_TIMEOUT;
    private static boolean DEBUG_OUTPUT;
    private static int phase;
    private static final String[] phaseres = new String[] {"couldn't connect to client", "target did not initiate challenge",
							   "target did not respond to challenge"};
    
    public static  int get_con_timeout() {
	return CON_TIMEOUT;
    }
    public static  int get_res_timeout() {
	return RES_TIMEOUT;
    }
    public static  boolean get_debug_output() {
	return DEBUG_OUTPUT;
    }

    public static void setParams(int _CON_TIMEOUT, int _RES_TIMEOUT, boolean _DEBUG_OUTPUT) {
	CON_TIMEOUT = _CON_TIMEOUT;
	RES_TIMEOUT = _RES_TIMEOUT;
	DEBUG_OUTPUT = _DEBUG_OUTPUT;
    }

    public static void main(String argv[]) throws Exception
    {
	try {
	    RES_TIMEOUT = 1000;
	    CON_TIMEOUT = 1000;
	    DEBUG_OUTPUT = true;
	    check(argv[0], Integer.parseInt(argv[1]));
	}
	catch (Exception e) {
	    System.out.println("Usage: addr, port");
	    System.out.println(e);
	}
    }

    public static boolean check(String addr, int port) throws Exception
    {
	String MAC = TestKeys.MAC;
	String PSK = TestKeys.PSK;
	phase = 0;
	boolean passed = false;

	try {
	    Socket clientSocket = new Socket();
	    clientSocket.connect(new InetSocketAddress(addr, port),
			   CON_TIMEOUT);
	    clientSocket.setSoTimeout(RES_TIMEOUT);

	    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	    BufferedReader inFromServer = new BufferedReader(
					  new InputStreamReader(clientSocket.getInputStream()));

	    phase = 1;

	    //////////////////////////////////////////////////////////////////////////////////////////
	    String challenge = getMessage(inFromServer,  "CHALLENGE: ");                            // get challenge
	    String response  = new String(genResponseX(challenge, MAC, PSK));                       // generate response
	    sendMessage(outToServer, response, response, "RESPONSE:  ");                            // send response
	                                                                                            // 
	    int chlen = challenge.length();                                                         // get length of challenge
	    String challenge_two = new String(genChallenge(chlen));                                 // generate new challenge
	                                                                                            //
	    phase = 2;                                                                              // 
	    String expected = new String(genResponseX(challenge_two, MAC, PSK));                    // generate expected response
	    sendMessage(outToServer, challenge_two, expected, "EXPECTED:  ");                       // send challenge
	    out = getMessage(inFromServer, "RESPONSE:  ");                                          // get response
	    //////////////////////////////////////////////////////////////////////////////////////////
	    clientSocket.close();

	    if(out.equals(expected)) return true;
	    return false;
	}
	catch(SocketTimeoutException e) {
	    cprintln("Socket timeout out at phase " + (phase + 1) + "\n" +
		     phaseres[phase]);
	    return false;
	}
	catch(Exception e) {
	    throw e;
	}
    }
    
    private static char[] genChallenge(int len) {
	Random rand = new Random();
	char[] challenge = new char[len];
	for(int i = 0; i < len; i++)
	    challenge[i] = (char)(32 + rand.nextInt(95));
	return challenge;
    }
    private static void sendMessage(DataOutputStream outToServer, String in, String ch, String tag) throws Exception {
	outToServer.writeBytes(in + '\n');
	numprintmessage(ch.toCharArray(), tag);
    }
    private static String getMessage(BufferedReader sock, String tag) throws Exception {
	String out = sock.readLine();
	numprintmessage(out.toCharArray(), tag);
	return out;
    }
    private static void numprintmessage(char[] ch, String message) {
	cprint(message);
	numprint(ch);
    }
    private static void numprint(char[] ch) {
	for(int i = 0; i < ch.length; i++)
	    if(i != ch.length - 1)
		cprint((int)ch[i] + " ");
	    else
		cprintln((int)ch[i] + "");
    }

    private static char[] genResponse(String challenge, String MAC, String PSK) {
	char[] ch = new char[challenge.length()];
	for(int i = 0; i < ch.length; i++) {
	    int k = challenge.charAt(i);
	    k += MAC.charAt(i%MAC.length());
	    k *= PSK.charAt(i%PSK.length());
	    k%=128;
	    ch[i] = (char)k;
	}
	return ch;
    }

    private static char[] genResponseX(String challenge, String MAC, String PSK) {
	char[] ch = new char[challenge.length()];
	for(int i = 0; i < ch.length; i++) {
	    int k = challenge.charAt(i);
	    k += MAC.charAt(i%MAC.length());
	    k *= PSK.charAt(i%PSK.length());
	    ch[i] = (char)k;
	}
	for(int i = ch.length - 1; i > 0; i--){
	    ch[i] ^= ch[i-1];
	    ch[i] = (char)(32 + ch[i] % 95);
	}
	ch[0] = (char)(32 + ch[0]%95);
	return ch;
    }


    private static void cprintln(String message) {
	if(DEBUG_OUTPUT) System.out.println(message);
    }

    private static void cprint(String message) {
	if(DEBUG_OUTPUT) System.out.print(message);
    }
}

class TestKeys {
    public static String PSK = "HelloWorldSecurePasswordBro";
    public static String MAC = "90-2E-1C-06-0E-5E";
}