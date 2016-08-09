import java.util.*;
import java.lang.*;

public class IW_select {
    String pw = "riotwaikato";
    
    public static void main(String[] args) {
	if(args.length != 1) { //we need a device name
	    System.out.println("Usage: ... [wlan#]");
	    return;
	}
	IW_scanfilter.dev = args[0]; //wlan device
	List<String[]> pairs = IW_scanfilter.scan_wlan(new String[] {"-v"}); // -v: print our output

	if(pairs == null) //we failed to scan or find any devices at all
	    return;

	/* check if we're already connected to our target device before doing this */
	
	for(int i = 0; i < pairs.size(); i++) {
	    if(pairs.get(i)[1].charAt(pairs.get(i)[1].length()-1) != 'A') {
		System.out.println("Skipping " + pairs.get(i)[1]);
		continue;
	    }
	    /* connect to the AP */
	    IW_scanfilter.connect_wpa_cli(pairs.get(i), IW_scanfilter.psw, true);

	    //delay a couple of seconds
	    try {
	    	Thread.sleep(10000);
	    }
	    catch (Exception e) {
	    	System.out.println(e);}
	    
	    String ip = null; //IW_scanfilter.get_server_ip(IW_scanfilter.dev);
	    if(ip == null) {
		String preface = "172.24.";
		String quad = pairs.get(i)[1].split("-")[2].substring(0, 3);
		System.out.println(quad);
		
		String identifier = ".1";

		ip = preface + Integer.parseInt(quad) + identifier;
		System.out.println("Converted ip: " + ip);
	    }
	    System.out.println(ip);
	    
	    /* then check if this AP has a connection to the target device */
	    /*if(Server_Test_Auth.checkCon(ip, 2158, 5000)) {
		System.out.println(pairs.get(i)[1] + " responded to auth request with valid token - maintaining connection");
		//if it does, then we exit this loop - we should have a fixed connection state 
		break;
	    }
	    else*/
	    Servman.Run(ip, true);
	    System.out.println(pairs.get(i)[1] + " did not respond to auth request with valid token");
	}
    }
}

