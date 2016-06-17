import java.io.*;
import java.util.*;

public class IW_scanfilter {    
    public  static String dev = "wlan0";
    public  static String fname = "wpa2.conf";
    public  static String psw = "riotwaikato";
    public static void main(String argv[]) {
	scan_wlan(argv);
    }

    public static List<String[]> scan_wlan(String[] args) {
	Runtime rt = Runtime.getRuntime();
	String[] argv = {"/bin/bash", "-c", "iw dev "+dev+" scan"};
	try {
	    Process proc = rt.exec(argv);
	    String line;
	    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	    List<String[]> strl = filter_scan(parse_scan(in));

	    boolean verbose = false;
	    boolean connect = false;
	    for(int i = 0; i < args.length; i++) {
		if(args[i].equals("-v"))
		    verbose = true;
		else if (args[i].equals("-c"))
		    connect = true;
	    }

	    if(verbose)
		print_list(strl);
	    if(connect) {
		connect_wpa_cli(strl.get(0), psw, verbose);
		get_server_ip(dev);
	    }
	    return strl;
	}
	catch(Exception e) {
	    System.err.println(e);
	    return null;
	}
    }

    public static String get_server_ip(String dev) {
	String[] argv1 = arg_argv("ip route show");
	
	List<String> l = null;
	try { l = (run(argv1));}
	catch (Exception e){
	    System.out.println(e);
	    System.out.println("Could not fetch ip...");
	}
	print_list_2(l);
	
	String match = "default via ";
	for(int i = 0; i < l.size(); i++) {
	    String s = l.get(i);
	    if(string_startswith(s, match)) {
		if(s.contains(dev)) {
			String target = s.substring(match.length()).split(" ")[0];
			System.out.println("***"+target);
			return target;
		}	    
	    }
	}
	return null;
    }
    
    public static void connect_wpa_cli(String[] pair, String pw, Boolean print) {
	String preface = "wpa_cli -i " + dev;
	String[] argv1 = arg_argv("wpa_cli -i " + dev + " disable_network 0");
	String[] argv2 = arg_argv("wpa_cli -i " + dev + "  remove_network 0");
	String[] argv3 = arg_argv("wpa_cli -p /var/run/wpa_supplicant -i "+dev+" add_network");
	String[] argv4 = arg_argv("wpa_cli -i " + dev + "  set_network 0 ssid '\"" + pair[1] + "\"'");
	String[] argv5 = arg_argv("wpa_cli -i " + dev + "  set_network 0 psk '\"" + pw + "\"'");
	String[] argv6 = arg_argv("wpa_cli  -i " + dev + " enable_network 0");

	/*String[] set = {preface + " disable_network 0",
		                  preface + "remove_network 0",
		                  "wpa_cli -p /var/run/wpa_supplicant -i " +dev+" add_network",
		                  preface + "set_network 0 ssid '\"" +pair[1] + "\"'",
		                  preface + "set_network 0 psk '\"" + pw + "\"'",
		                  preface + " enable_network 0"
				  };*/
	try {
	    //print_list_2(run_list(from_argset(set)));
	    List<List<String>> outset = new LinkedList<List<String>>();
	    outset.add(run(argv1));
	    outset.add(run(argv2));
	    outset.add(run(argv3));
	    outset.add(run(argv4));
	    outset.add(run(argv5));
	    outset.add(run(argv6));

	    if(print)
		for(int i = 0; i < 6; i++)
		    print_list_2(outset.get(i));
	    /*print_list_2(run(argv1));
	    print_list_2(run(argv2));
	    print_list_2(run(argv3));
	    print_list_2(run(argv4));
	    print_list_2(run(argv5));
	    print_list_2(run(argv6));*/
	}
	catch (Exception e) {
	    System.out.println(e);
	}
    }

    static String[][] from_argset(String[] cin) {
	String[][] cout = new String[cin.length][];
	for(int i = 0; i < cin.length; i++)
	    cout[i] = arg_argv(cin[i]);
	return cout;
    }

    static List<String> run_list(String[][] argv) throws Exception {
	List<String> l = new LinkedList<String>();
	for(int i = 0; i < argv.length; i++){
	    l.addAll(run(argv[i]));
	}
	return l;
    }
    static String[] arg_argv(String arg) {
	return new String[] {"/bin/bash", "-c", arg};
    }
    /*public static void connect_iw(String[] pair, String pw, String fn) {
	String[] argv  = {"/bin/bash", "-c", "rm " + fn};
	String[] argv3 = {"/bin/bash","-c","wpa_supplicant -B -D nl80211 -i" + dev + " -c" + fn};
	try {
	    System.out.println("Connecting to: " + pair[1]);
	    run(argv);
	    wpa_passphrase_adj(pair, pw, fn);
	    run(argv3);
	}
	catch(Exception e) {
	    System.out.println(e);
	}
	}*/

    /*private static void wpa_passphrase_adj(String[] pair, String pw, String fn) throws Exception {
	List<String> str = run(new String[] {"/bin/bash", "-c", "wpa_passphrase " + pair[1] + " " + pw});
	String rep1  =str.get(2).split("#")[0] + str.get(2).split("#")[1];
	String rep2  ="#" + str.get(3);
	str.set(2, rep1);
	str.set(3, rep2);
	
	print_list_2(str);

	for(int i = 0; i < str.size(); i++)
	    dunk_into_file(str.get(i), fn);
	    }*/

    private static void dunk_into_file (String str, String fn) throws Exception {
	run(new String[] {"/bin/bash", "-c", "echo '" + str + "' >> " + fn});
    }

    private static List<String> run(String[] argv) throws Exception {
	Runtime rt = Runtime.getRuntime();	
	Process proc = rt.exec(argv);
	String ln;
	BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	List<String> ls = new LinkedList<String>();
	
	while((ln = in.readLine()) != null)
	    ls.add(ln);
	return ls;
    }
    
    public static void print_list(List<String[]> strl) {
	for(int i = 0; i < strl.size(); i++) {
	    System.out.println(".." + i);
	    System.out.println(strl.get(i)[1]);
	    System.out.println("    " + strl.get(i)[0]);
	}
    }

    public static void print_list_2(List<String> strl) {
	for(int i = 0; i < strl.size(); i++)
	    System.out.println(strl.get(i));
    }
    
    static List<String[]> filter_scan (List<String[]> inp) {
	List<String[]> outp = new LinkedList<String[]>();
	for(int i = 0; i< inp.size(); i+=1) {
	    String[] s = inp.get(i);
	    if(string_startswith(s[1], "riot-waikato-")) {
		outp.add(s);
	    }
	}
	return outp;
    }

    static List<String[]> parse_scan (BufferedReader input) throws Exception {
	List<String[]> output = new LinkedList<String[]>();

	String[] set = new String[2];
	String[] get = {"BSS ", "	SSID:"};
	String[] del = {get[0], "\\(", get[1] + " "};
	String line;
	boolean state = false;
	while((line = input.readLine())!= null) {
	    //System.out.println("test");
	    if(string_startswith(line, get[0])) {
		set[0] = line.split(del[0])[1].split(del[1])[0];
		state = true;
		//System.out.println(set[0]);
	    }
	    else if(string_startswith(line, get[1])) {
		set[1] = line.split(del[2])[1];
		output.add(set);
		set = new String[2];
		//System.out.println(set[1]);
		state = false;
	    }
	}
	input.close();
	return output;
    }

    static boolean string_startswith(String target, String comparitor) {
	int i;
	for(i = 0; i < comparitor.length() && i < target.length(); i++)
	    if(comparitor.charAt(i) != target.charAt(i))
		return false;
	return (i == comparitor.length());
    }
}
