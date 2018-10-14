
/*
 * Raman Sathiapalan, Anshika Singh, Usuma Thet
 * CS 6378.001
 * Project 2
 * Due: October 25, 2018
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Parser {
	// Initialize local variables
	int num_nodes = 0;
	int temp_num_nodes = 0;
	static String[][] info_nodes;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		Parser main = new Parser();

		File config_file = new File(
				"C:\\Users\\hinam\\Documents\\2018 Fall - CS 6378.001 - Advanced Operating System\\SampleInput.txt");
		info_nodes = main.ReadInput(config_file); // Run method.

		System.out.println(Arrays.deepToString(info_nodes)); // Print for testing. DELETE
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
}