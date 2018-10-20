import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;


public class Broadcast_Nodes {

	/*
	 * Initialize local variables. Each dc machine (node) will have a unique
	 * combination of host name, port, and node neighbors
	 */
	static String nodeHostName = null;
	static int nodePortNumber = 0;
	static String nodeNeighbors = null;
	static String[] nodeNeighborsArray;
	static int nodeNumber = 0;

	static Socket server = null;
	static ArrayList<Socket> socClientsArray = new ArrayList<Socket>();
	static int[] neighborHopArray;

	static int numTimesUpdated = 0;
	
	//check number of updates
	static boolean serverUpdateFlag = true;
	static int clientthread = 0;
	static int updatesCounter = 0;
	final int[] updatedClientCounter = new int[1];
	
	int num_nodes = 0;
	int temp_num_nodes = 0;
	static String[][] info_nodes;
	static ArrayList<Socket> childArray = new ArrayList<Socket>();
	static Socket parentNode = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		Broadcast_Nodes n1 = new Broadcast_Nodes(); // Initialize class

		File config_file = new File("C:\\Personal Stuff\\ProjectWorkspace\\ProjectAssignmnet2\\src\\broadcastSystem\\SampleInput.txt");
		// Check if configuration file is available
		if (config_file.exists() == true) {
			System.out.println("Configuration file for input found.");
		} else {
			System.out.println("Configuration file for input not found.");
			System.exit(0); // Terminate code
		}

		// Run ReadInput function. Outputs cleaned configuration file contents in 2d
		// array		
		
		String[][] info_nodes = n1.ReadInput(config_file);

		System.out.println(Arrays.deepToString(info_nodes)); // Print for testing. DELETE

		try {
			// Capture host name of dc machines (node)
			nodeHostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		/*
		 * Identify node # running code. Configuration file information is partially
		 * extracted according to the node number.
		 */
		for (int i = 0; i < info_nodes.length; i++) {
			if (info_nodes[i][1].equals(nodeHostName)) { // Match node based on host name
				nodeNumber = Integer.parseInt(info_nodes[i][0]);
				nodePortNumber = Integer.parseInt(info_nodes[i][2]);
				nodeHostName = info_nodes[i][1];
				nodeNeighbors = info_nodes[i][3];
				break;
			}
		}

		/*
		 * Check if node information is initialized. Exit code if dc machine (node) not
		 * identified in configuration file
		 */
		if (nodePortNumber == 0) {
			System.out.println(
					"\nCould not find host name in the configuration file or port number does not match. Exiting. . .");
			System.exit(0); // Terminate code
		} else {
			System.out.println("\nHost " + nodeHostName + " on port #" + nodePortNumber
					+ " initialized.\nWelcome, Node #" + nodeNumber + "!");

			System.out.println("\n--------------------------------------\n");
		}
		
		nodeNeighborsArray = nodeNeighbors.split(" ");
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				neighborHopArray = new int[info_nodes.length];				
				n1.setServer(nodePortNumber, nodeNeighborsArray.length);
			}
		});

		Thread t2 = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(100);
					for(int i= 0; i<nodeNeighborsArray.length; i++) {
						for(int j = 0; j<info_nodes.length; j++) {
							if(info_nodes[j][0].equals(nodeNeighborsArray[i])) {
								n1.setClient(info_nodes[j][1], Integer.parseInt(info_nodes[j][2]));
							}
						}
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		t1.start();
		t2.start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FUNCTIONS
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String[][] ReadInput(File config_file) {
		try {
			// Initialize function variables
			boolean firstlinepassed = false;
			int lineCount = 0;
			String[] temp_splitarr;
			String hostname = "";
			String node;
			String node_neighbors;

			BufferedReader br = new BufferedReader(new FileReader(config_file));
			String temp_filerow;
			String temp_line;

			while ((temp_filerow = br.readLine()) != null) {

				// Handle # which denotes comments. Characters after # in line are ignored
				if (temp_filerow.contains("#")) {
					temp_filerow = temp_filerow.substring(0, temp_filerow.indexOf("#"));
				}
				temp_filerow = temp_filerow.trim(); // Handle leading and trailing white spaces

				if (!temp_filerow.isEmpty()) { // Ignore empty lines and lines beginning with #

					// First valid line of the configuration file contains one token denoting the
					// number of nodes in the system.
					if (firstlinepassed == false) {
						num_nodes = Integer.parseInt(temp_filerow.trim().split(" +")[0]);
						info_nodes = new String[num_nodes][4];
						firstlinepassed = true;

					} else { // first line has passed
						// Populate array containing node information
						temp_splitarr = temp_filerow.trim().split(" +", 3); // only split line 3 times to keep neighbors together

						// Assign host name, port, and neighbors to node number
						info_nodes[lineCount][0] = Integer.toString(temp_num_nodes);
						info_nodes[lineCount][1] = temp_splitarr[0].trim() + ".utdallas.edu"; // host name
						//info_nodes[lineCount][1] = temp_splitarr[0].trim();
						info_nodes[lineCount][2] = temp_splitarr[1].trim(); // port
						info_nodes[lineCount][3] = temp_splitarr[2].trim(); // neighbors

						lineCount++;
						temp_num_nodes++; // node number incremented for unique assignment
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info_nodes;
	}
	
	public void setServer(int nodePortNumber, int nodeNeighborsNumber) {	
		ServerSocket ssoc = null;
		try {
			ssoc = new ServerSocket(nodePortNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true) {
			try {				
				Socket soc = ssoc.accept();			
				socClientsArray.add(soc);
				counter++;
				if(counter == nodeNeighborsNumber){
					ArrayList<Socket> tempList = new ArrayList<>(socClientsArray);
					for(int i = 0; i<tempList.size(); i++) {
						final Socket iVal = tempList.get(i);
						//final CountDownLatch latch = new CountDownLatch(tempList.size());
						Thread commThread = new Thread(new Runnable() {
							public void run() {
								createSpanningTreeParent(iVal);
							}
						});				
						commThread.start();
					}
				}
			} catch (SocketException e1) {
				try{
					ssoc.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	

	public synchronized void createSpanningTreeParent(Socket iSoc) {
		try {
			Socket newSoc = iSoc;
			DataOutputStream dos = new DataOutputStream(iSoc.getOutputStream());
			dos.writeUTF("Can I be your parent");
			DataInputStream dis = new DataInputStream(iSoc.getInputStream());
			boolean answer = (dis.readUTF()).equals("YES") ? true : false;
			if(answer) {
				System.out.println("CHILD "+ newSoc);
				childArray.add(iSoc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setClient(String nodeHostName, int nodePortNumber) {
		final boolean parentVariable = (parentNode==null)? false : true;
		final Socket[] parentVar = new Socket[1]; 
		final CountDownLatch latch = new CountDownLatch(1);
		Thread singleClientThread = new Thread(new Runnable(){
			public void run(){
				try {
					Socket clientSocket = new Socket(nodeHostName, nodePortNumber);
					DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
					String question = dis.readUTF();
					DataOutputStream dos = null;
					if(question.equals("Can I be your parent")) {
						dos = new DataOutputStream(clientSocket.getOutputStream());
						if(parentVariable == false&& nodeNumber != 1) {
							parentVar[0] = clientSocket;
							dos.writeUTF("YES");
						}
						else {
							dos.writeUTF("NO");
						}
					}
					latch.countDown();
				} catch (SocketException e) {
					System.out.println(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		singleClientThread.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(parentVar[0] != null && nodeNumber != 1){
			parentNode = parentVar[0];
			System.out.println("parentNode : " +parentNode);
		}
	}
}
