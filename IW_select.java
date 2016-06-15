import java.io.*;
import java.util.*;

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

	//test
	IW_scanfilter.connect_wpa_cli(pairs.get(0), pw);
	//end
	
	/* check if we're already connected to our target device before doing this */
	
	//for(int i = 0; i < pairs.size(); i++) {
	    /* connect to the AP */
	//  IW_scanfilter.connect_iw(pairs.get(i), IW_scanfilter.psw, IW_scanfilter.fname);
	    /* then check if this AP has a connection to the target device */
	//    if(true)
		/*if it does, then we exit this loop - we should have a fixed connection state */
	//	break;
	//}
    }
}
