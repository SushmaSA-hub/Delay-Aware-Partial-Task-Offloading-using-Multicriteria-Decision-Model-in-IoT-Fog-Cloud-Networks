
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;;

// Taken As 3 Task Nodes and 6 Helper Nodes and 3 Number of Tasks

public class post_new{
    static ArrayList<Double> poisson_dist;
	static ArrayList<Double> exponential_dist;
    static int number_of_task_nodes; // Number of Task Nodes
    static int number_of_helper_nodes; // Number of Helper Nodes
    static int number_of_tasks; // Number of Tasks same as Number of Task Nodes
    public static double A[][]; // POST task Allocation Matrix
    public static double B[][]; // The Matrix Representing Connectivity between TNs and HNs

    //static double Bk; // bandwidth of HNs in MHz
    //static double data_transmission_power_TN; // Transmission Power of TN in mW
    static double noise_power; // White Noise Power in W
    static double alpha; // PathLoss factor
    static double distance; // Distance between two Task Nodes
    static double channel_gain; // Channel Gain

    //static double transmission_rate; // Store a Transmission Rate of Task Node to Helper Node
    static double[] Bandwidth_TN;
    static double[] Bandwidth_HN;
    static double[] Bandwidth_choices;
    static double[] computational_capabilities_TN;
    static double[] computational_capabilities_HN;

    static double computation_capability_HN[]; // Computational capability of HN in GHz
    static double computation_capability_TN[]; // Computational capability of TN in GHz

    static double size_of_task[]; // Size of Given Number of Tasks
    static double processing_density_Task[]; // Processing Density of Task Nodes i.e. the CPU cycles required to process
                                             // a unit bit of data in cycles/bit
    static double transmission_power_TN[];
    static double transmission_power_HN[];
    static double array_for_TN[]; // Store a strategy of Helper Nodes Temporary

    static double[] local_computational_time; // Store a Array of Local Computing Time Data for all Task Nodes

    static double[] r_TN;
    static double[] r_HN;
    static double price_of_anarchy;
    static int[] deadline;
    public static void main(String[] args) {

        readValues();
        helperArrayForInitialization();

        System.out.print("Initial Computational Time By Only Local Offloading Full Task : \n");
       /* for (int j = 0; j < number_of_task_nodes; j++) {
            System.out.print(local_computational_time[j] + " ");
        }*/
        System.out.println("\n");
        System.out.println(
                "completed reading------------------------------------------------------------------------- \n");
        for (int i = 0; i < number_of_task_nodes; i++) {
            allocationOfHelperNode(i);
        }

        output();

        System.out.print("Final Computational Time : \n");
       for (int j = 0; j < number_of_task_nodes; j++) {
            System.out.print(local_computational_time[j] + " ");
        }
        System.out.println();
     //   System.out.println("transmission_rate " + transmission_rate);
        price_of_anarchy = priceOfAnarchy();
        System.out.println();
        System.out.println("Price of Anarchy Value : " + price_of_anarchy);
     double delay = 0;
        double start_time =0.0;
				for(int i =0 ; i < number_of_tasks; i++)
				{
					if(start_time < poisson_dist.get(i))
						start_time = (double)poisson_dist.get(i);
				}
            
                int count = 0;
            for (int i = 0; i < local_computational_time.length; i++)
            {
               // System.out.println("Local length"+local_computational_time);
            delay = delay + local_computational_time[i]+start_time-(double)poisson_dist.get(i);
			 	if(local_computational_time[i]< deadline[i])
					{
						count++;
					}
        }
        
        System.out.println("Total Delay Calculation : " + delay + "\n");
        writeOutInt(number_of_task_nodes);
        int discarded =number_of_tasks-count;
        System.out.println("discarded: " + discarded);
        writeOutInt(discarded);
		writeOut(delay);
        String a = String.valueOf(number_of_task_nodes);
        String b = "POST";
        String c = String.valueOf(delay);
        String d= String.valueOf(discarded);
        writeCSV(a, b, c, d);
		
    }

    public static void writeOutInt(int res)
	{
		String filePath = "/input/POSToutput.txt";

        // Write the value to the file
        try {
            // Create a FileWriter object in append mode
            FileWriter fileWriter = new FileWriter(filePath, true);

            // Create a BufferedWriter object for efficient writing
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write the double value followed by a newline character
            bufferedWriter.write(String.valueOf(res));
            bufferedWriter.newLine();

            // Close the BufferedWriter
            bufferedWriter.close();

            System.out.println("Value has been written to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
	}

    public static void writeOut(double res)
	{
		String filePath = "/input/POSToutput.txt";

        // Write the value to the file
        try {
            // Create a FileWriter object in append mode
            FileWriter fileWriter = new FileWriter(filePath, true);

            // Create a BufferedWriter object for efficient writing
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write the double value followed by a newline character
            bufferedWriter.write(String.valueOf(res));
            bufferedWriter.newLine();

            // Close the BufferedWriter
            bufferedWriter.close();

            System.out.println("Value has been written to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
	}

    public static void readValues() {

        String basePath = "/input/inputPOST/";
        String filePath;
        String base1 = "/input/fullInput/";
	//	filePath = base1+ "Exponential_dist.txt";
		//exponential_dist = readNumbersFromFile1(filePath);
	filePath = base1+ "Poisson_dist.txt";
	poisson_dist = readNumbersFromFile1(filePath);

        filePath = basePath + "number_of_task_nodes.txt";
        number_of_task_nodes = readIntegerFromFile(filePath);
        filePath = basePath + "number_of_helper_nodes.txt";
        number_of_helper_nodes = readIntegerFromFile(filePath);
        filePath = basePath + "number_of_tasks.txt";
        number_of_tasks = readIntegerFromFile(filePath);

        A = new double[number_of_task_nodes][number_of_helper_nodes + 1];
        B = new double[number_of_task_nodes][number_of_helper_nodes];

        array_for_TN = new double[number_of_helper_nodes + 1];
        local_computational_time = new double[number_of_task_nodes];
        filePath = basePath + "noise_power.txt";
        noise_power = readDoubleFromFile(filePath);
        filePath = basePath + "alpha.txt";
        alpha = readDoubleFromFile(filePath);
        filePath = basePath + "distance.txt";
        distance = readDoubleFromFile(filePath);
        filePath = basePath + "channel_gain.txt";
        channel_gain = readDoubleFromFile(filePath);
        filePath = basePath + "AllocationVectorMatrix.txt";
        A = read2DArrayFromFile(filePath, number_of_task_nodes, number_of_helper_nodes + 1);
        filePath = basePath + "TN_HN_ConnectingMatrix.txt";
        B = read2DArrayFromFile(filePath, number_of_task_nodes, number_of_helper_nodes);

        filePath = basePath + "computation_capability_HN.txt";
        computation_capability_HN = read1DArrayFromFile(filePath, number_of_helper_nodes);
        filePath = basePath + "computation_capability_TN.txt";
        computation_capability_TN = read1DArrayFromFile(filePath, number_of_task_nodes);
        filePath = basePath + "size_of_task.txt";
        size_of_task = read1DArrayFromFile(filePath, number_of_task_nodes);
        filePath = basePath + "processing_density_Task.txt";
        processing_density_Task = read1DArrayFromFile(filePath, number_of_task_nodes);
        filePath = basePath + "local_computational_time.txt";
        local_computational_time =read1DArrayFromFile(filePath, number_of_task_nodes);
        filePath = basePath + "r_TN.txt";
        r_TN = new double[number_of_task_nodes];
        r_HN = new double[number_of_helper_nodes];
        r_TN=read1DArrayFromFile(filePath, number_of_task_nodes); 
        filePath = basePath + "r_HN.txt";
        r_HN=read1DArrayFromFile(filePath, number_of_helper_nodes); 
         base1 = "/input/inputSTS/";
        System.out.println("deadline");
        filePath = base1 + "deadline.txt";
        deadline = read1DIntFromFile(filePath,number_of_task_nodes);

    }

    // Reading Integer Data From File
    public static int readIntegerFromFile(String fileLocation) {
        try {
            File file = new File(fileLocation);
            Scanner scanner = new Scanner(file);
            int value = scanner.nextInt();
            scanner.close();
            return value;
        } catch (FileNotFoundException e) {
            System.err.println("Error reading integer from file: " + e.getMessage());
            return -1; // Or a different error value
        }
    }


    private static ArrayList<Double> readNumbersFromFile1(String filePath) {
        ArrayList<Double> res = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            String[] values = scanner.nextLine().split(" ");
            Double row = -1.0;
            for (String value : values) {
                row = Double.parseDouble(value);
                res.add(row);
            }
        

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }
    // Reading Double Valued Data From File
    public static double readDoubleFromFile(String fileLocation) {
        try {
            File file = new File(fileLocation);
            Scanner scanner = new Scanner(file);
            double value = scanner.nextDouble();
            scanner.close();
            return value;
        } catch (FileNotFoundException e) {
            System.err.println("Error reading double from file: " + e.getMessage());
            return -1.0; // Or a different default value for error
        }
    }

    // Reading 2D Array From File
    private static double[][] read2DArrayFromFile(String filePath, int rows, int columns) {
        double[][] res = new double[rows][columns];
        ArrayList<ArrayList<Double>> res1 = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(" ");
                ArrayList<Double> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Double.parseDouble(value));
                }
                res1.add(row);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                res[i][j] = res1.get(i).get(j);
            }
        }

        return res;
    }

    // Reading 1D Array From File
    public static double[] read1DArrayFromFile(String fileLocation, int size) {
        try {
            File file = new File(fileLocation);
            Scanner scanner = new Scanner(file);
            List<Double> numbers = new ArrayList<>();

            while (scanner.hasNextDouble()) {
                numbers.add(scanner.nextDouble());
            }

            scanner.close();

            // Check if the array is empty
            if (numbers.isEmpty()) {
                System.err.println("Error: No values read from the file.");
                return new double[0]; // Or handle it based on your preference
            }

            // Convert List to double array
            double[] array = numbers.stream().mapToDouble(Double::doubleValue).toArray();
            return array;
        } catch (FileNotFoundException e) {
            System.err.println("Error reading array from file: " + e.getMessage());
            return new double[0]; // Or handle it based on your preference
        }
    }

    public static int[] read1DIntFromFile(String fileLocation, int size) {
        try {
            File file = new File(fileLocation);
            Scanner scanner = new Scanner(file);
            List<Integer> numbers = new ArrayList<>();

            while (scanner.hasNextDouble()) {
                numbers.add(scanner.nextInt());
            }

            scanner.close();

            // Check if the array is empty
            if (numbers.isEmpty()) {
                System.err.println("Error: No values read from the file.");
                return new int[0]; // Or handle it based on your preference
            }

            // Convert List to double array
            int[] array = numbers.stream().mapToInt(Integer::intValue).toArray();
            return array;
        } catch (FileNotFoundException e) {
            System.err.println("Error reading array from file: " + e.getMessage());
            return new int[0]; // Or handle it based on your preference
        }
    }

    // To Print Final Allocation Matrix
    private static void output() {
        //System.out.println("Final Task Allocation Matrix A :");
        /*for (int i = 0; i < number_of_task_nodes; i++) {
            for (int j = 0; j < number_of_helper_nodes + 1; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();*/
    }

    // Function to Initialize Value of Helper Array of Task Nodes and Helper Nodes
    private static void helperArrayForInitialization() {
        for (int i = 0; i < number_of_helper_nodes; i++) {
            array_for_TN[i] = 0;
        }
    }

    // Equation11 calculation
    private static double calculateEquationPart1(ArrayList<Integer> set_of_active_HN, int task_node_number) {
        double result = 0;

        double numerator = 1;
        double denominator = 1;
        double temp_denominator = 0;
        double temp_numerator = 0;
        double term = 0;

        for (int i = 0; i < set_of_active_HN.size(); i++) {

            for (int j = 0; j < number_of_task_nodes; j++) {
                if (j != task_node_number) {
                    term = term + (A[j][set_of_active_HN.get(i) + 1]
                            * procesingTimeHN(set_of_active_HN.get(i), size_of_task[j], processing_density_Task[j],task_node_number));
                }
            }
            temp_numerator = temp_numerator + (term / procesingTimeHN(set_of_active_HN.get(i),
                    size_of_task[task_node_number], processing_density_Task[task_node_number],task_node_number));
            term = 0;
            temp_denominator = temp_denominator + (1 / procesingTimeHN(set_of_active_HN.get(i),
                    size_of_task[task_node_number], processing_density_Task[task_node_number],task_node_number));
        }

        denominator = 1
                + (temp_denominator * ((size_of_task[task_node_number] * processing_density_Task[task_node_number])
                        / (computation_capability_TN[task_node_number])));
        numerator = 1 + temp_numerator;

        result = numerator / denominator;
        return result;
    }

    // Equation11 Calculation
    private static double calculateEquationPart3(ArrayList<Integer> set_of_active_HN, int task_node_number,
            int helper_node_number) {
        double result = 0;

        double part1;
        double part2 = 0;
        double temp1; // Calculating (Zn * Gn / Fn)
        double temp2; // Calculating (1 / Onk)
        double temp3;

        double num2;
        double deno2;

        temp1 = size_of_task[task_node_number] * processing_density_Task[task_node_number]
                / computation_capability_TN[task_node_number];
        temp2 = (1 / procesingTimeHN(helper_node_number, size_of_task[task_node_number],
                processing_density_Task[task_node_number],task_node_number));

        part1 = temp2 * (temp1 - sumOfTimetoProcessTasktoHelperNode(task_node_number, helper_node_number + 1));

        double term_num2 = 0;
        double term_deno2 = 0;
        for (int i = 0; i < set_of_active_HN.size(); i++) {
            temp3 = (1 / procesingTimeHN(set_of_active_HN.get(i), size_of_task[task_node_number],
                    processing_density_Task[task_node_number],task_node_number));
            term_num2 = term_num2 + (temp3
                    * (temp1 - sumOfTimetoProcessTasktoHelperNode(task_node_number, set_of_active_HN.get(i) + 1)));
            term_deno2 = term_deno2 + temp3;
        }
        num2 = temp2 * temp1 * (term_num2);
        deno2 = 1 + (temp1 * term_deno2);
        part2 = num2 / deno2;

        result = part1 - part2;
        return result;
    }

    // Function to calculate a time to compute task using taken strategy
    private static double calculateEquationPartTime(int task_node_number, ArrayList<Integer> set_of_active_HN) {
        double time = 0;

        time = calculateEquationPart1(set_of_active_HN, task_node_number);
        time = time * ((size_of_task[task_node_number] * processing_density_Task[task_node_number])
                / computation_capability_TN[task_node_number]);

        return time;
    }

    // Equation 11 method
    private static double equation11(int task_node_number, int helper_node_number,
            ArrayList<Integer> set_of_active_HN) {
        double result = 0;
        double temp;

        temp = size_of_task[task_node_number] * processing_density_Task[task_node_number]
                / computation_capability_TN[task_node_number];

        if (helper_node_number == 0) {
            result = calculateEquationPart1(set_of_active_HN, task_node_number);
        } else if (B[task_node_number][helper_node_number - 1] == 1
                && set_of_active_HN.contains(helper_node_number - 1)) {
            double left = sumOfTimetoProcessTasktoHelperNode(task_node_number, helper_node_number);
            double right = calculateEquationPart1(set_of_active_HN, task_node_number) * temp;

            if (left >= right) {
                result = 0;
            } else if (left < right) {
                result = calculateEquationPart3(set_of_active_HN, task_node_number, helper_node_number - 1);
            }
        }
        return result;
    }

    // Function to Calculate Set of Active Helper Nodes to Given Task Node n i.e.
    // A_n Equation12
    private static ArrayList<Integer> setOfActiveHelperNode(int task_node_number) {
        ArrayList<Integer> set_of_active_HN = new ArrayList<>();

        double temp;
        temp = size_of_task[task_node_number] * processing_density_Task[task_node_number]
                / computation_capability_TN[task_node_number];

        for (int x = 0; x < number_of_helper_nodes; x++) {
            if (B[task_node_number][x] == 1) {
                double left_part = sumOfTimetoProcessTasktoHelperNode(task_node_number, x + 1);
                double right_part = calculateEquationPart1(set_of_active_HN, task_node_number) * temp;

                if (left_part < right_part) {
                    set_of_active_HN.add(x);
                }
            }
        }
        return set_of_active_HN;
    }

    // Function to calculate Total Delay of Task
    private static double totalDelayOfTask(int task_node_number) {
        double time_local = 0;
        time_local = localComputationTimeTN(task_node_number, size_of_task[task_node_number],
                computation_capability_TN[task_node_number]);
        System.out.println("Localdelay:"+time_local);
        double helper_delay = 0;
        for (int i = 0; i < number_of_helper_nodes; i++) {
            double t = A[task_node_number][i + 1]*50000
                    * procesingTimeHN(i, size_of_task[task_node_number], processing_density_Task[task_node_number],task_node_number);
            helper_delay = Math.max(t, helper_delay);
        }

        //return Math.max(time_local, helper_delay);
         return time_local + helper_delay;
    }

    // Function to allocate a Helper Nodes to Task Nodes
    private static void allocationOfHelperNode(int task_node_number) {
        ArrayList<Integer> set_of_active_HN;
        set_of_active_HN = setOfActiveHelperNode(task_node_number);

        // System.out.println();
       // System.out.print("Allocation starting :" + task_node_number + " \n");
        // for(int i=0;i<set_of_active_HN.size();i++) {
        // System.out.print(set_of_active_HN.get(i)+1 + " ");
        // }
        // System.out.println();

        double time_star = A[task_node_number][0] * size_of_task[task_node_number]
                * processing_density_Task[task_node_number] / computation_capability_TN[task_node_number];

        for (int x = 0; x < number_of_helper_nodes + 1; x++) {
            double temp = equation11(task_node_number, x, set_of_active_HN);
            array_for_TN[x] = temp;
        }
        // System.out.println();

        double time_new = calculateEquationPartTime(task_node_number, set_of_active_HN);

        // if(set_of_active_HN.size() == 0) {
        // A[task_node_number][0] = 0;
        // }

        if (time_star - time_new > 10e-3) {
            for(int i = 0; i < number_of_helper_nodes + 1; i++) {
                A[task_node_number][i] = array_for_TN[i];
            }
            local_computational_time[task_node_number] = totalDelayOfTask(task_node_number);
        }

        helperArrayForInitialization();
    }

    // Function to Calculate Value of Price of Anarchy
    private static double priceOfAnarchy() {
        double res = 1;
        double maxi = Arrays.stream(local_computational_time).max().orElseThrow(NoSuchElementException::new);
        for (int i = 0; i < number_of_task_nodes; i++) {
            res = res + local_computational_time[i];
        }
        return (maxi / res);
    }

    // Function to Calculate a SUM(Amk * Omk) for all Task Node Other than Task Node
    // N
    // Also Part of Equation 12
    private static double sumOfTimetoProcessTasktoHelperNode(int task_node_number, int helper_node_number) {
        double time_to_process_task = 0;

        for (int i = 0; i < number_of_task_nodes; i++) {
            if (i != task_node_number) {
                time_to_process_task = time_to_process_task + (A[i][helper_node_number]
                        * procesingTimeHN(helper_node_number - 1, size_of_task[i], processing_density_Task[i], task_node_number));
            }
        }
        return time_to_process_task;
    }

    // The Amount of Time That Task n Contributes to the total Time for Off-loading
    private static double procesingTimeHN(int helper_node_number, double size_of_task, double processing_density_task, int task_no) {
        double processing_time_task = 0;
      //  System.out.println(r_TN.length);
        processing_time_task = size_of_task
                * ((1 / r_TN[task_no]) + (processing_density_task / computation_capability_HN[helper_node_number]));
        return processing_time_task;
    }

    // Task Nodes Broadcasting Parameters to Helper Nodes
    private static double localComputationTimeTN(int task_node_no, double size_of_task, double computation_capability) {
        double local_computational_time_task = 0;
        local_computational_time_task = (A[task_node_no][0] * size_of_task * processing_density_Task[task_node_no])
                / computation_capability;
        return local_computational_time_task;
    }
    public static void writeCSV(String a, String b, String c,String d)
            {
                  String csvFile = "output_post.csv";

        // Appending data to CSV
        try (FileWriter fileWriter = new FileWriter(csvFile, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            // Append data row
           
            printWriter.print(a);
              printWriter.print(",");
            printWriter.print(b);
            printWriter.print(",");
            printWriter.print(c);
            printWriter.print(",");
            printWriter.print(d);
            printWriter.println();

            System.out.println("Data appended to CSV file successfully " + csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
            }
        }