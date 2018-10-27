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

	static ArrayList<Socket> socClientsArray = new ArrayList<Socket>();
	static int[] neighborHopArray;
	
	int num_nodes = 0;
	int temp_num_nodes = 1;
	static String[][] info_nodes;
	static ArrayList<Socket> childArray = new ArrayList<Socket>();
	static Socket parentNode = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		Broadcast_Nodes n1 = new Broadcast_Nodes(); // Initialize class

		File config_file = new File("SampleInput.txt");
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
			e.printStackTrace();
		} catch (IOException e) {
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
				//System.out.println("in setServer");				
				socClientsArray.add(soc);
				counter++;
				if(counter == nodeNeighborsNumber){
					ArrayList<Socket> tempList = new ArrayList<>(socClientsArray);
					final CountDownLatch latch = new CountDownLatch(tempList.size());
					for(int i = 0; i<tempList.size(); i++) {
						final Socket iVal = tempList.get(i);
						Thread commThread = new Thread(new Runnable() {
							public void run() {
								//System.out.println("in threadServer");
								createSpanningTreeParent(iVal);
								latch.countDown();
							}
						});				
						commThread.start();
						//System.out.println("i " + i + "templist size " + tempList.size());
						if(i == tempList.size()-1){
							try {
								//System.out.println("in server broadcast");
								latch.await();
								Thread.sleep(500);
								sendReceiveBroadCastMessage("Hello 5");
								serverRecieveMessages(childArray);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
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
	
	public void serverRecieveMessages(ArrayList<Socket> tempList){
		for(Socket clientSoc : tempList) {
			final Socket iVal = clientSoc;
			Thread commThread = new Thread(new Runnable() {
				public void run() {
					try { 
						//System.out.println("in server Receive msgs");
			
						DataOutputStream dos = null;
						DataInputStream dis = null;
						while(true){
							System.out.println("Enter while");
							dis = new DataInputStream(iVal.getInputStream());
							String clientReply = dis.readUTF();
							System.out.println("Message Recieved from "+ iVal + " is " + clientReply);
							if(childArray.size() != 0) {	
								for(Socket socket:childArray) {
									if(socket != iVal) {											
										System.out.println("Sending it forward to my neighbor "+ socket);
										dos = new DataOutputStream(socket.getOutputStream());
										dos.writeUTF(clientReply);
									}
								}
								if(iVal != parentNode) {
									System.out.println("Sending it forward to my parent "+ parentNode);
									dos = new DataOutputStream(parentNode.getOutputStream());
									dos.writeUTF(clientReply);
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});				
			commThread.start();
		}
	}
	
	public synchronized void createSpanningTreeParent(Socket iSoc) {
		try {
			Socket newSoc = iSoc;
			DataOutputStream dos = null;
			DataInputStream dis = null;
			dos = new DataOutputStream(newSoc.getOutputStream());
			//System.out.println("in cspt");
			dos.writeUTF("Can I be your parent");
			dis = new DataInputStream(newSoc.getInputStream());
			String clientReply = dis.readUTF();
			if(clientReply.equals("YES") || clientReply.equals("NO")){
				boolean answer = (clientReply).equals("YES") ? true : false;
				if(answer) {
					System.out.println("CHILD "+ newSoc);
					childArray.add(iSoc);
				}
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
					DataInputStream in = null;
					DataOutputStream out  = null;
					while(true) {
						//System.out.println("in client");
						in = new DataInputStream(clientSocket.getInputStream());
						String messageReceived = in.readUTF();
						if(messageReceived.equals("Can I be your parent")) {
							//System.out.println("in parent question");
							out = new DataOutputStream(clientSocket.getOutputStream());
							if(parentVariable == false&& nodeNumber != 1) {
								parentVar[0] = clientSocket;
								out.writeUTF("YES");
							}
							else {
								out.writeUTF("NO");
							}
						}
						else {
							//System.out.println("in broadcast client");
							System.out.println("Message Recieved from "+ clientSocket + " is " + messageReceived);
							if(childArray.size() != 0) {	
								for(Socket socket:childArray) {
									if(socket != clientSocket) {
										System.out.println("Sending it forward to my neighbor "+ socket);
										out = new DataOutputStream(socket.getOutputStream());
										out.writeUTF(messageReceived);
									}
								}
								if(clientSocket != parentNode) {
									System.out.println("Sending it forward to my parent "+ parentNode);
									out = new DataOutputStream(parentNode.getOutputStream());
									out.writeUTF(messageReceived);
								}
							}
						}
						latch.countDown();
					}
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
	
	public void sendReceiveBroadCastMessage(String message) {
		for(Socket s:childArray) {
			try {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF("HELLO 5");
				System.out.println("msg sent to " + s);
				/* DataInputStream dis = new DataInputStream(s.getInputStream());
				String reply = dis.readUTF();
				System.out.println("message received " + reply);
				System.out.println("Message sent to and acknowledgement "+ reply +" recieved from " + s); */
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			//System.out.println("parentNode: " + parentNode);
			DataOutputStream dos = new DataOutputStream(parentNode.getOutputStream());
			dos.writeUTF("HELLO 5");
			System.out.println("msg sent to " + parentNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("msg sent ");
	}
}
