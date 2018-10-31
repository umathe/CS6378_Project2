Raman Sathiapalan, Anshika Singh, Usuma Thet
CS 6378.001 - Advanced Operating Systems - F18
Project 2
Due: October 30, 2018

------------------------------------------

Code compilation instructions (Windows):
-----
1. Open PuTTY. Reference the configuration file and open the same number of instances as the number of nodes. 
   Connect each node with their respective their host names and ports. 

2. For each PuTTY instance, login with your UTD netID and password.

3. Create SampleInput.txt file in PuTTY and copy the contents of the configuration file. Save SampleInput.txt.

4. Create Broadcast_Nodes.java file in PuTTY and copy the contents of the submitted java file.

5. Compile code using the following command: javac Broadcast_Nodes.java

6. Run code in each PuTTY instance. Use the following command: java Broadcast_Nodes

------------------------------------------

Code compilation instructions (Mac/Linux): 
-----
1. Save project folder on DC machine. 

2. Save config file (SampleInput.txt) inside the project folder.

3. Setup passwordless login. Create a folder "launcher" on the home machine with the scripts launcher.sh and cleanup.sh, with required changes based on filepaths. 

4. Save config file in "launcher" folder. 

5. Compile code in DC machine using the following command: javac Broadcast_Nodes.java

6. In the home machine, run the launcher script (command: ./launcher.sh) and cleanup script (./cleanup.sh).