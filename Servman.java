/*C2: raspberry
      check every RESOLUTION seconds if raspberry is connected to laptop
                                     or raspberry is connected to client
      if yes
         if !ADV
        ADV -> T
        start ADV server/ADV
      else
         if ADV
        ADV -> F
        stop AD server/ADV
*/

class Servman extends Thread {
    private static String addr;
    private static final int PORT_TCPC = 50176;
    private static final int PORT_TCPC_BACK = 50277;
    private static boolean use_backup = false;
    private static boolean CONNECTED = false;
    private static boolean CONNECTED_LAST;
    private static boolean SERVER_IS_RUNNING = false;
    private static int CON_TIMEOUT = 1000;
    private static int RES_TIMEOUT = 1000;
    private static boolean DEBUG_OUTPUT = false;
    private static ServHost.HostThreadManager htm = new ServHost.HostThreadManager();
    private static Thread t;
    private static Thread SubServManThread;

    public static void main(String[] args) {
	addr = args[0];

	SubServerManager subserv = new SubServerManager();
	//subserv.start();
	TCPC.setParams(CON_TIMEOUT, RES_TIMEOUT, DEBUG_OUTPUT);
	CON_UPDATE();
	t = new Thread(htm);
	SubServManThread = new Thread(subserv);
	SubServManThread.start();
	try {
	    if(TCPC.check(addr, PORT_TCPC)) {
		use_backup = false;
		subserv.init = true;
		CONNECTED = true;
	    }
	} catch(Exception e) { System.err.println(e); }

	try {
	    if(!CONNECTED)
		if (TCPC.check(addr, PORT_TCPC_BACK) ){
		    use_backup = true;
		    subserv.init = true;
		    CONNECTED = true;
		}
	} catch (Exception e) { System.err.println(e); }
    	System.out.println("Subserver-Manager initialized: " + subserv.init);
    }

    public static void Run(String ip, boolean DEBUG_OUTPUT) {
	Servman.DEBUG_OUTPUT = DEBUG_OUTPUT;
	main(new String[] {ip});
    }

    static class SubServerManager implements Runnable {
	private volatile boolean init = false;
	private volatile boolean halt = false;
	private int user_port;
	@Override public void run() {
	    System.out.println("Initialised thread");
	    user_port = PORT_TCPC;
	    if(use_backup) user_port = PORT_TCPC_BACK;

	    while(!init) t_sleep(5);
	    System.out.println("Thread commenced");
	    
	    System.out.println("We start hosting a server now");
	    htm.init(); //start our own server
	    while(!halt) {
		if(!CONNECTED) {
		    try { 
			System.out.println("End thread");
			htm.halt();
			//t.stop();
			System.out.println("End thread_2");
		    }   
		    catch (Exception e) { e.printStackTrace();}
		    System.out.println("End thread_3");
		    return;
		}
		else { //we're connected
		    
		    try { CONNECTED = TCPC.check(addr, user_port); } //check connection

		    catch (Exception e) {e.printStackTrace(); }
		    		    System.out.println("Connected: " + CONNECTED);
		    CON_UPDATE();
		}
		t_sleep(3000);
	    }	    
	}
	
	private void t_sleep(int t) {
	    try { sleep(t) ; } catch (Exception e) {}
	}
	public boolean init() {
	    //check if the correct server exists
	    //do things
	    
	    //then flag as initilized / halt (maybe)
	    return (init = true);
	}
	
	public void halt() {
	    //do things

	    //then halt
	    this.halt = true;	    
	}
    }

    private static boolean CON_CHANGE() {
	return CONNECTED != CONNECTED_LAST;
    }
    private static void    CON_UPDATE() {
	CONNECTED_LAST = CONNECTED;
    }
}
