
import java.util.*;

import java.io.*;

public class random_new{
	static ArrayList<Double> poisson_dist;
	static ArrayList<Double> exponential_dist;
	static int no_OfTask;
	static int no_OfHelper;
	static int no_OfVM;
	static int outages;
	static ArrayList<ArrayList<Double>> r_n_m;// The data rate from node n to node m==>row task and column fog
	static ArrayList<ArrayList<Double>> r_m_n;// The data rate from node m to node n==>row fog and column task 
	static ArrayList<Double> r_t_n; // The transmitting data rate from node n to the cloud=>row task and column VM
	static ArrayList<Double> r_r_n;// The receiving data rate from the cloud to node n=>row VM and column task
	static ArrayList<Double> io_r_n;
	static ArrayList<Integer> ip_size;
	static ArrayList<ArrayList<Double>> pe_n_m;
	static ArrayList<ArrayList<Double>> pe_n_k;
	static ArrayList<Double> pe_n_l;
	static ArrayList<Double> pe_n_k_total;
	static ArrayList<Double> pd_n;
	static ArrayList<Double> f_m;
	static ArrayList<Double> f_k;
	static ArrayList<Double> f_n;
	static ArrayList<Double> p_t_n;
	static ArrayList<Double> p_t_m;
	static Double p_t_c;
	static ArrayList<ArrayList<Double>> distance_n_m;
	static ArrayList<Double> distance_n_c;
	static ArrayList<ArrayList<Integer>> allocated_list;
	static ArrayList<ArrayList<Double>> tav_all_list;
	static ArrayList<Integer> quota;
	static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_k;
	static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_m;
	static ArrayList<Map.Entry<Integer, Double>> tav_l;
	static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comm_delay_n_m;
	static ArrayList<Double>  comm_delay_n_c;
	static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comp_delay_n_m;
	static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comp_delay_n_k;
	static ArrayList<Map.Entry<Integer, Double>> comp_delay_n_l;
	static ArrayList<Double> total_delay_n;
	static ArrayList<Integer> deadline;
	
	public static void main(String[] args) {

		pe_n_m = new ArrayList<ArrayList<Double>>();// The processing efficiency of helper node m when executing task n
		pe_n_k = new ArrayList<ArrayList<Double>>();// The processing efficiency of VM k when executing task n
		pe_n_l = new ArrayList<Double>();// The processing efficiency of local node l when executing task n
		pe_n_k_total = new ArrayList<Double>();
		
		double[] f_n_choice = new double[] { 2.0e9, 1.8e9, 1.0e9, 1.5e9, 2.5e9 };
		double[] f_m_choice = new double[] { 2.5e9, 1.0e9, 1.8e9, 2.0e9, 1.5e9 };
		double[] f_k_choice = new double[] { 30e9, 15e9, 35e9, 25e9, 10e9, 20e9 };
		double[] pd_n_choice = new double[] { 3000, 500, 2000 };
		double[] bw_choice = new double[] { 20e6, 20e6, 20e6, 20e6, 20e6, 20e6};
	
		
		
		readValues();
		
		// calculating processing efficiency for helper node for all tasks
		for (int i = 0; i < no_OfTask; i++) {
			ArrayList<Double> row = new ArrayList<Double>();
			for (int j = 0; j < no_OfHelper; j++) {

				row.add((1 / r_m_n.get(j).get(i)) + ((double) pd_n.get(i) / (double) f_m.get(j)) + ((double) io_r_n.get(i) / r_m_n.get(j).get(i)));
			}
			pe_n_m.add(row);
		}
		//System.out.println("Quota: "+quota);
		// calculating processing efficiency for VM node for all tasks
		for (int i = 0; i < no_OfTask; i++) {
			ArrayList<Double> row = new ArrayList<Double>();
			for (int j = 0; j < no_OfVM; j++) {

				row.add(((1 / r_t_n.get(i))) + ((double) pd_n.get(i) / (double) f_k.get(j)) + ((double) io_r_n.get(i) / r_r_n.get(i)));
			}
			pe_n_k.add(row);
		}

		// calculating processing efficiency for local nodes(task nodes)
		for (int i = 0; i < no_OfTask; i++) {
			pe_n_l.add((double) pd_n.get(i) / (double) f_n.get(i));
		}

		// PCRC Algorithm...
		allocated_list = PCRC();
		tav_all_list = generate_tav_all();
		tav_l = tav_l_calc();
		tav_k = tav_k_calc();
		tav_m = tav_m_calc();
		
		comm_delay_n_m = comm_delay_m_calc();
		comm_delay_n_c = comm_delay_c_calc();
		comp_delay_n_m = comp_delay_m_calc();
		comp_delay_n_k = comp_delay_k_calc();
		comp_delay_n_l = comp_delay_l_calc();
		total_delay_n = total_delay_calc();
		
		writeOutInt(no_OfTask);
		writeOutInt(no_OfHelper);
		writeOut(no_OfVM);


		double total_delay = calc_complete_delay();
		writeOut(total_delay);

		String a = String.valueOf(no_OfTask);
        String b = "RANDOM";
		double local = local_only();
        System.out.println("local only: " + local);
        String c = String.valueOf(total_delay);
		String d=String.valueOf(outages);
        writeCSV(a, b, c,d);
		

	}
	 private static void writeCustomFormat(String filePath, ArrayList<ArrayList<Map.Entry<Integer, Double>>> data) {
	        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
	            for (ArrayList<Map.Entry<Integer, Double>> row : data) {
	                writer.print("[");
	                for (int i = 0; i < row.size(); i++) {
	                    Map.Entry<Integer, Double> entry = row.get(i);
	                    writer.print(entry.getKey() + "=" + entry.getValue());
	                    if (i < row.size() - 1) {
	                        writer.print(", ");
	                    }
	                }
	                writer.print("]");
	                writer.println();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    // Read data in the specified format from a file
	 private static ArrayList<ArrayList<Map.Entry<Integer, Double>>> readCustomFormat(String filePath) {
		    ArrayList<ArrayList<Map.Entry<Integer, Double>>> data = new ArrayList<>();

		    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
		        String line;
		        while ((line = reader.readLine()) != null) {
		            ArrayList<Map.Entry<Integer, Double>> row = new ArrayList<>();

		            // Remove square brackets and split by commas
		            String[] entries = line.substring(1, line.length() - 1).split(", ");

		            for (String entry : entries) {
		                // Check for empty string before attempting to split
		                if (!entry.isEmpty()) {
		                    String[] keyValue = entry.split("=");
		                    int key = Integer.parseInt(keyValue[0]);
		                    double value = Double.parseDouble(keyValue[1]);
		                    row.add(new AbstractMap.SimpleEntry<>(key, value));
		                }
		            }
		            data.add(row);
		        }
		    } catch (IOException | NumberFormatException e) {
		        e.printStackTrace();
		    }

		    return data;
		}

	
	 private static void writeArrayList2D(String filePath, ArrayList<ArrayList<Map.Entry<Integer, Double>>> data) {
	        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
	            for (ArrayList<Map.Entry<Integer, Double>> row : data) {
	                for (int i = 0; i < row.size(); i++) {
	                    Map.Entry<Integer, Double> entry = row.get(i);
	                    writer.print(entry.getKey() + ":" + entry.getValue());
	                    if (i < row.size() - 1) {
	                        writer.print(" ");
	                    }
	                }
	                writer.println();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	 private static ArrayList<ArrayList<Map.Entry<Integer, Double>>> readArrayList2D(String filePath) {
		    ArrayList<ArrayList<Map.Entry<Integer, Double>>> data = new ArrayList<>();

		    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
		        String line;
		        while ((line = reader.readLine()) != null) {
		            // Skip empty lines
		            if (line.trim().isEmpty()) {
		                continue;
		            }

		            ArrayList<Map.Entry<Integer, Double>> row = new ArrayList<>();
		            String[] values = line.split(" ");
		            for (String value : values) {
		                String[] keyValue = value.split(":");
		                // Check if both key and value parts exist
		                if (keyValue.length == 2) {
		                    int key = Integer.parseInt(keyValue[0]);
		                    double doubleValue = Double.parseDouble(keyValue[1]);
		                    row.add(new AbstractMap.SimpleEntry<>(key, doubleValue));
		                } else {
		                    // Handle the case where the line doesn't have the expected format
		                    System.out.println("Invalid format in line: " + line);
		                }
		            }
		            data.add(row);
		        }
		    } catch (IOException | NumberFormatException e) {
		        e.printStackTrace();
		    }

		    return data;
		}
	    
	    
	    private static void writeEntryArrayList(String filePath, ArrayList<Map.Entry<Integer, Double>> data) {
	        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
	            for (Map.Entry<Integer, Double> entry : data) {
	                writer.print(entry.getKey() + ":" + entry.getValue());
	                writer.println();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    // Read ArrayList<Map.Entry<Integer, Double>> from a file
	    private static ArrayList<Map.Entry<Integer, Double>> readEntryArrayList(String filePath) {
	        ArrayList<Map.Entry<Integer, Double>> data = new ArrayList<>();

	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                String[] keyValue = line.split(":");
	                int key = Integer.parseInt(keyValue[0]);
	                double value = Double.parseDouble(keyValue[1]);
	                data.add(new AbstractMap.SimpleEntry<>(key, value));
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return data;
	    }
		public static void writeOut(double res)
	{
		String filePath = "/input/Randomoutput.txt";

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

        //System.out.println("Value has been written to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
	}
	public static void writeOutInt(int res)
	{
		String filePath = "/input/Randomoutput.txt";

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
	public static void readValues()
	{
		String basePath = "/input/inputSTS/";
        String filePath;
        /*no_OfVM = 10;
		no_OfHelper = 20;// Helper nodes
		no_OfTask = 50;//
		*/
		String base1 = "/input/fullInput/";
		filePath = base1+ "Exponential_dist.txt";
		exponential_dist = readNumbersFromFile1(filePath);
		filePath = base1+ "Poisson_dist.txt";
		poisson_dist = readNumbersFromFile1(filePath);
	//	System.out.println("no_OfVM");
        filePath = basePath + "no_OfVM.txt";
        no_OfVM = readIntFromFile(filePath);
      //  System.out.println(no_OfVM);
      //  System.out.println("no_OfHelper");
        filePath = basePath + "no_OfHelper.txt";
        no_OfHelper = readIntFromFile(filePath);
      //  System.out.println(no_OfHelper);
       // System.out.println("no_OfTask");
        filePath = basePath + "no_OfTask.txt";
        no_OfTask = readIntFromFile(filePath);
       // System.out.println(no_OfTask);
       // System.out.println("r_n_m");
        filePath = basePath + "r_n_m.txt";
        r_n_m = readNumbersFromFile2(filePath);
        //printDouble2(r_n_m);
      //  System.out.println("r_m_n");
        filePath = basePath + "r_m_n.txt";
        r_m_n = readNumbersFromFile2(filePath);
        //printDouble2(r_m_n);
       // System.out.println("r_t_n");
        filePath = basePath + "r_t_n.txt";
        r_t_n = readNumbersFromFile1(filePath);
        //printDouble1(r_t_n);
       // System.out.println("r_r_n");
        filePath = basePath + "r_r_n.txt";
        r_r_n = readNumbersFromFile1(filePath);
        //printDouble1(r_r_n);
      //  System.out.println("io_r_n");
        filePath = basePath + "io_r_n.txt";
        io_r_n = readNumbersFromFile1(filePath);
        //printDouble1(io_r_n);
       // System.out.println("ip_size");
        filePath = basePath + "ip_size.txt";
        ip_size = readNumbersFromFileInt1(filePath);
        //printInt1(ip_size);
      //  System.out.println("pd_n");
        filePath = basePath + "pd_n.txt";
        pd_n = readNumbersFromFile1(filePath);
        //printDouble1(pd_n);
       // System.out.println("f_m");
        filePath = basePath + "f_m.txt";
        f_m = readNumbersFromFile1(filePath);
        //printDouble1(f_m);
       // System.out.println("deadline");
        filePath = basePath + "deadline.txt";
        deadline = readNumbersFromFileInt1(filePath);
     //   System.out.println("f_k");
        filePath = basePath + "f_k.txt";
        f_k = readNumbersFromFile1(filePath);
        //printDouble1(f_k);
       // System.out.println("f_n");
        filePath = basePath + "f_n.txt";
        f_n = readNumbersFromFile1(filePath);
        //printDouble1(f_n);
      //  System.out.println("p_t_n");
        filePath = basePath + "p_t_n.txt";
        p_t_n = readNumbersFromFile1(filePath);
        //printDouble1(p_t_n);
      //  System.out.println("p_t_m");
        filePath = basePath + "p_t_m.txt";
        p_t_m = readNumbersFromFile1(filePath);
        //printDouble1(p_t_m);
     //   System.out.println("p_t_c");
        filePath = basePath + "p_t_c.txt";
        p_t_c = readNumbersFromFile0(filePath);
        //printDouble0(p_t_c);
    //    System.out.println("distance_n_m");
        filePath = basePath + "distance_n_m.txt";
        distance_n_m = readNumbersFromFile2(filePath);
        //printDouble2(distance_n_m);
     //   System.out.println("distance_n_c");
        filePath = basePath + "distance_n_c.txt";
        distance_n_c = readNumbersFromFile1(filePath);
        //printDouble1(distance_n_c);
      //  System.out.println("quota");
        filePath = basePath + "quota.txt";
        quota = readNumbersFromFileInt1(filePath);
        //printInt1(quota);
	}
	
	public static void printDouble2(ArrayList<ArrayList<Double>> dist1) {
        // Display the read numbers
     //   System.out.println("Read Distances from File:");
        for (ArrayList<Double> row : dist1) {
            for (Double value : row) {
             //   System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public static void printDouble1(ArrayList<Double> row) {
        // Display the read numbers
      //  System.out.println("Read Distances from File:");
        for (Double value : row) {
          //  System.out.print(value + " ");
        }
        System.out.println();
    }

    public static void printInt1(ArrayList<Integer> row) {
        // Display the read numbers
     //   System.out.println("Read Distances from File:");
        for (Integer value : row) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    public static void printDouble0(Double dist1) {
        // Display the read numbers
       // System.out.println("Read Distances from File:");
     //   System.out.println(dist1);
        System.out.println();
    }

    private static Double readNumbersFromFile0(String filePath) {
        Double res = -1.0;

        try (Scanner scanner = new Scanner(new File(filePath))) {
            res = scanner.nextDouble();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }
    private static Integer readIntFromFile(String filePath)
    {
    	
            int result = 0;
            try {
                // Create a Scanner to read from the file
                Scanner scanner = new Scanner(new File(filePath));

                // Read the integer value from the file
                if (scanner.hasNextInt()) {
                    result = scanner.nextInt();
                } else {
                    System.out.println("File does not contain a valid integer.");
                }

                // Close the Scanner
                scanner.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return result;
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

    private static ArrayList<Integer> readNumbersFromFileInt1(String filePath) {
        ArrayList<Integer> res = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            String[] values = scanner.nextLine().split(" ");
            Integer row = -1;
            for (String value : values) {
                row = Integer.parseInt(value);
                res.add(row);
            }
           

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }

    private static ArrayList<ArrayList<Double>> readNumbersFromFile2(String filePath) {
        ArrayList<ArrayList<Double>> res = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(" ");
                ArrayList<Double> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Double.parseDouble(value));
                }
                res.add(row);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }
	// Random number genration
	public static int getRand(int min, int max) {
		Random random = new Random();
		return (int) random.nextInt(max + 1);
	}

	public static int generateRandomValueInRange(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}
	public static double generateRandomValueInDouble(double min, double max) {
		Random random = new Random();
		return min + (max - min) * random.nextDouble();
	}
	
	//Log base n calculation
	public static double logn(double value, double base)
	{
		return Math.log(value) / Math.log(base);
	}
	//channel gain i.e. path loss calculation
	/* @param d ->distance in km
	 * @param bw ->Bandwidth in KHz
	 */
	public static double calculate_channel_gain(double d, double bw)
	{
		//formula for path loss to chanel gain conversion is taken from meto paper
		double path_loss = (20* logn(d, 10)) + (20 * logn(bw, 10)) + 32.45;
		return Math.pow(10, -(path_loss/10));		
	}
	//Data Rate calculation
	/* @param bw -> bandwidth in hertz
	 * @param g -> channel gain -> calculated using function
	 * @param d -> distance needed to calculate path loss
	 * @param p_t ->transmitting power
	 * @param N_o ->noise power spectral density
	 * 
	 */
	public static double calculate_r(double bw, double d, double p_t, double N_o)
	{	double g = calculate_channel_gain(d, bw/1000);
		double t = logn(1 + ( (g * p_t) / (bw * N_o)), 2);
		return bw * t;
	}
	

	// PCRC Algorithm...
	public static ArrayList<ArrayList<Integer>> PCRC() {
		ArrayList<ArrayList<Integer>> waiting_list_n = new ArrayList<ArrayList<Integer>>();
		int remTotal = no_OfHelper+no_OfVM;
		ArrayList<Integer> TotalList = new ArrayList<Integer>();
		//ArrayList<Integer> helperList = new ArrayList<Integer>();
	//	System.out.println("Entered...");
		for(int i = 0; i < no_OfHelper+no_OfTask; i++)
		{
			TotalList.add(i);
		}
		Random random = new Random();
		// System.out.println("finally" + waiting_list_n);
		for(int i = 0; i < no_OfTask; i++)
		{
			ArrayList<Integer> curr_list = new ArrayList<Integer>();
			double[] r_choice = new double[] { 1,2,3,4,5,6,7,8,9,10 ,11};
			double local= r_choice[(int) (random.nextDouble()*11)];
			int q = quota.get(i);
			for(int j = 0; j < q-1; j++)
			{
				if(remTotal <1)
					break;
				int pos = generateRandomValueInRange(0,Math.min(10,remTotal)-1);
				curr_list.add(TotalList.get(pos));
				TotalList.remove(pos);
				remTotal--;
			}
		//	System.out.print(remTotal + ", ");
			
			
			waiting_list_n.add(curr_list);
		}
	//	System.out.println("exited.....");
		return waiting_list_n;

	}

	public static ArrayList<ArrayList<Double>> generate_tav_all()
	{
		Random random = new Random();
		ArrayList<ArrayList<Double>> full_alloc = new ArrayList<ArrayList<Double>>();
		for(int i = 0; i < no_OfTask; i++)
		{
			ArrayList<Double> numbers = new ArrayList<>();
	        // Generate n random numbers between 0 and 1
			int n = allocated_list.get(i).size();
			double[] r_choice = new double[] { 1,2,3,4,5,6,7,8,9,10 ,11};
			double local=  random.nextDouble()*30;
			numbers.add(local);//local
			double sum = local;
	        for (int j = 0; j < n; j++) {
	            double randomNumber = r_choice[(int) (random.nextDouble()*11)];//(max - min + 1) + min;
	            numbers.add(randomNumber);
	            sum += randomNumber;
	        }
		//	System.out.print(numbers.size() + ", ");
	        // Normalize the numbers so their sum is 1
	        for (int j = 0; j < n+1; j++) {
				double x = numbers.get(j) / sum;
	            numbers.set(j, x);
	        }
	        full_alloc.add(numbers);
		}
		
	//	System.out.println("complete list" +  full_alloc);
		return full_alloc;
	}

	public static void commDelay()
	{
		ArrayList<ArrayList<Double>> comm_dealy = new ArrayList<ArrayList<Double>>();
		//allocated_list
		for(int i =0; i < no_OfTask; i++)
		{
			for(int j = 0; j < allocated_list.get(i).size(); i++)
			{
				int offloaded_node =  allocated_list.get(i).get(j);
				if(offloaded_node < no_OfHelper)
				{
					double offloaded_ratio = tav_all_list.get(i).get(j);

				}
			}
		}

	}

		// pe_n_md for the given n and m;
		// TAV of helper nodes
		public static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_m_calc() {
			ArrayList<ArrayList<Map.Entry<Integer, Double>>> res = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
			//ArrayList<Double> pe_n_cnv = pe_n_cnv_calculation();
		
			for (int i = 0; i < no_OfTask; i++) {
				ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
				ArrayList<Integer> curr_list = allocated_list.get(i);
				for (int j = 0; j < curr_list.size(); j++) {
					if(curr_list.get(j) >= no_OfHelper)
						continue;
					int curr_VM = curr_list.get(j);
					
					double t = tav_all_list.get(i).get(j+1);
					temp.add(createEntry(curr_VM, t));
				}
				res.add(temp);
			}
		//	System.out.println("helper node allocation: " + res);
			return res;
		}

		// TAV of local nodes
		public static ArrayList<Map.Entry<Integer, Double>> tav_l_calc() {
			ArrayList<Map.Entry<Integer, Double>> tav_n_l = new ArrayList<Map.Entry<Integer, Double>>();
			for (int i = 0; i < no_OfTask; i++) {
				tav_n_l.add(createEntry(i, tav_all_list.get(i).get(0)));
			}
			//System.out.println("local Allocation:" + tav_n_l);
			return tav_n_l;
		}

		// find processing efficiency of task with respect to all its allocated VMs i.e
		// pe(n, c_n_v)
		/*
		 * @params allocated list
		 * 
		 * @params n-> for this task we need to calculate pe_n_cnv
		 */
		public static ArrayList<Double> pe_n_cnv_calculation() {

			ArrayList<Double> res = new ArrayList<Double>();
			for (int i = 0; i < no_OfTask; i++) {
				double t1 = 1 / r_t_n.get(i);
				// System.out.println("t1" + t1);
				double t2;

				double total_fk_n = 0;// total frequency of all vms allocated for this task
				int no_VM, no_H;
				ArrayList<Integer> curr_list = allocated_list.get(i);
				no_H = curr_list.get(0);
				no_VM = allocated_list.get(i).size() - no_H - 1;
				// System.out.println("Number of VM: " + no_VM);
				for (int j = 0; j < no_VM; j++) {
					int curr_VM = curr_list.get(no_H + 1 + j);
					// System.out.println(curr_VM);
					total_fk_n = total_fk_n + f_k.get(curr_VM);
				}
				t2 = pd_n.get(i) / total_fk_n;
				// System.out.println("pd_n.get(i):" + pd_n.get(i) + " total_fk_n:" + total_fk_n
				// + " t2" + t2);
				double t3 = io_r_n.get(i) / r_r_n.get(i);
				// System.out.println("t3" + t3);
				res.add(t1 + t2 + t3);
				// System.out.println("res" + res);
			}
			return res;
		}

		private static Map.Entry<Integer, Double> createEntry(Integer key, Double value) {
			return new HashMap.SimpleEntry<>(key, value);
		}

		
		public static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_k_calc() {
			ArrayList<ArrayList<Map.Entry<Integer, Double>>> res = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
			//ArrayList<Double> pe_n_cnv = pe_n_cnv_calculation();
		
			for (int i = 0; i < no_OfTask; i++) {
				ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
				ArrayList<Integer> curr_list = allocated_list.get(i);
				for (int j = 0; j < curr_list.size(); j++) {
					if(curr_list.get(j) <= no_OfHelper)
						continue;
					int curr_VM = curr_list.get(j);
					
					double t = tav_all_list.get(i).get(j+1);
					temp.add(createEntry(curr_VM-no_OfHelper, t));
				}
				res.add(temp);
			}
			//System.out.println("VM node allocation: " + res);
			return res;
		}



	//======>communication delay for helper nodes
		public static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comm_delay_m_calc()
		{
			ArrayList<ArrayList<Map.Entry<Integer, Double>>> comm_delay = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
		
			for(int i = 0; i < no_OfTask; i++)
			{
				ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
				//current allocation vector of the helper nodes for that particular task
				ArrayList<Map.Entry<Integer, Double>> curr_alloc_vector = tav_m.get(i);
				for (Map.Entry<Integer, Double> entry : curr_alloc_vector) 
				{
					Integer key = entry.getKey();
					Double value = entry.getValue();
					Double res;
					double t1 = value * ip_size.get(i)/r_n_m.get(i).get(key);
					double t2 = value * io_r_n.get(i) * ip_size.get(i)/r_m_n.get(key).get(i);
					res = t1+t2;				
					temp.add(createEntry(key, res));
				}
				comm_delay.add(temp);
			}
			//System.out.println("comm_delay_m : " + comm_delay);
			return comm_delay;
		}
		
		
		

		//======>communication delay for VM nodes
			public static ArrayList<Double>  comm_delay_c_calc()
			{
				ArrayList<Double> comm_delay = new ArrayList<Double>();
				for(int i = 0; i < no_OfTask; i++)
				{
					
					//current allocation vector of the VM nodes for that particular task
					ArrayList<Map.Entry<Integer, Double>> curr_alloc_vector = tav_k.get(i);
					Double u1 = 0.0;
					Double u2 = 0.0;
					for (Map.Entry<Integer, Double> entry : curr_alloc_vector) 
					{
						Integer key = entry.getKey();
						Double value = entry.getValue();
						Double res;
						u1 = u1 + value * ip_size.get(i);
						u2 = u2 + value * ip_size.get(i) * io_r_n.get(i);
					}
					Double res = (u1/r_t_n.get(i)) + (u2 /r_r_n.get(i)) ;
					comm_delay.add(res);
			
				}
			//	System.out.println("comm_delay_C : " + comm_delay);
				return comm_delay;
			}
			
			
		//===>computation delay of helper nodes
			
			public static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comp_delay_m_calc()
			{
				ArrayList<ArrayList<Map.Entry<Integer, Double>>> comp_delay = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
				for(int i = 0; i < no_OfTask; i++)
				{
					ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
					//current allocation vector of the helper nodes for that particular task
					ArrayList<Map.Entry<Integer, Double>> curr_alloc_vector = tav_m.get(i);
					for (Map.Entry<Integer, Double> entry : curr_alloc_vector) 
					{
						Integer key = entry.getKey();
						Double value = entry.getValue();
						Double res;
						double t1 = value * ip_size.get(i) * pd_n.get(i);
						res = t1/f_m.get(key);				
						temp.add(createEntry(key, res));
					}
					comp_delay.add(temp);
				}
				//System.out.println("comp_delay_m : " + comp_delay);
				return comp_delay;
			}	
			
			public static ArrayList<ArrayList<Map.Entry<Integer, Double>>>  comp_delay_k_calc()
			{
				ArrayList<ArrayList<Map.Entry<Integer, Double>>> comp_delay = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
				for(int i = 0; i < no_OfTask; i++)
				{
					ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
					//current allocation vector of the helper nodes for that particular task
					ArrayList<Map.Entry<Integer, Double>> curr_alloc_vector = tav_k.get(i);
					for (Map.Entry<Integer, Double> entry : curr_alloc_vector) 
					{
						Integer key = entry.getKey();
						Double value = entry.getValue();
						Double res;
						double t1 = value * ip_size.get(i) * pd_n.get(i);
						res = t1/f_k.get(key);				
						temp.add(createEntry(key, res));
					}
					comp_delay.add(temp);
				}
				//System.out.println("comp_delay_k : " + comp_delay);
				return comp_delay;
			}
			
			public static ArrayList<Map.Entry<Integer, Double>>  comp_delay_l_calc()
			{
				ArrayList<Map.Entry<Integer, Double>>  comp_delay = new ArrayList<Map.Entry<Integer, Double>>();
				
				for(int i = 0; i < no_OfTask; i++)
				{
					
					//current allocation vector of the helper nodes for that particular task
					Map.Entry<Integer, Double> curr_alloc_vector = tav_l.get(i);
					Double value = curr_alloc_vector.getValue();
					Double res;
					double t1 = value * ip_size.get(i) * pd_n.get(i);
					res = t1/f_n.get(i);				
					comp_delay.add(createEntry(i, res));
				}
			//	System.out.println("comp_delay_l : "+ comp_delay);
				return comp_delay;
			}
			
			
			public static ArrayList<Double> total_delay_calc()
			{
				ArrayList<Double> total = new ArrayList<Double>();
				ArrayList<Double> totalEnergy = new ArrayList<Double>();
				Double sumEnergy = 0.0;
				for(int i = 0; i < no_OfTask; i++)
				{
					//local delay for this task
					Double local_delay = comp_delay_n_l.get(i).getValue();
					ArrayList<Map.Entry<Integer, Double>> comm_delay_m = comm_delay_n_m.get(i);
					ArrayList<Map.Entry<Integer, Double>> comp_delay_m = comp_delay_n_m.get(i);
					
					int no_H = comm_delay_m.size();
					//maxi_h  represent the delay maxi delay by all helpers
					Double maxi_H = Double.MIN_VALUE;
					for(int j = 0; j < no_H; j ++)
					{
						Double comm = comm_delay_m.get(j).getValue();
						Double comp = comp_delay_m.get(j).getValue();
						if(comm + comp > maxi_H)
							maxi_H =  comm+comp;
					}
					
					
					
					Double comm_C = comm_delay_n_c.get(i);
					ArrayList<Map.Entry<Integer, Double>> comp_delay_k = comp_delay_n_k.get(i);
					Double maxi_K = Double.MIN_VALUE;
					int no_k = comp_delay_k.size();
					
					
					for(int j =0 ; j < no_k; j++)
					{
						Double comp_k = comp_delay_k.get(j).getValue();
						if(comp_k > maxi_K)
							maxi_K = comp_k;
					}
					//cloud total delay for this task
					Double total_C = maxi_K + comm_C;
					Double res = local_delay;
					if(maxi_H > res)
						res= maxi_H;
				    if(total_C > res)
				    		res = total_C;
					totalEnergy.add(res*p_t_n.get(i));
					total.add(res);
					}
						
						//System.out.println("total delay : " + total);
						//System.out.println("total energy: "+ totalEnergy);
						for(int i = 0;i < no_OfTask; i++)
						{
							sumEnergy = sumEnergy + totalEnergy.get(i);
						}
						writeOut(sumEnergy);

						return total;
			}
		
			
			public static double calc_complete_delay()
			{
				double res = 0.0;
				int count=0;
			
			//	System.out.println("dealine: "+deadline);
				double start_time =0.0;
				for(int i =0 ; i < no_OfTask; i++)
				{
					if(start_time < poisson_dist.get(i))
						start_time = poisson_dist.get(i);
				}
				for(int i =0; i < no_OfTask; i++)
				{
			if(total_delay_n.get(i) < deadline.get(i))
					{
						//res+=total_delay_n.get(i);
						res += total_delay_n.get(i)+start_time-poisson_dist.get(i);
						count++;
					}
				}
			//	System.out.println(res);
				outages=no_OfTask-count;
				writeOutInt(outages);
				return res;
			}
			public  static double local_only()
            {
                ArrayList<Double> res = new ArrayList<Double>();
				double total_local=0.0;
                for(int i = 0; i< no_OfTask;i++)
                {
                        double t1 = pd_n.get(i)*ip_size.get(i);
                        t1 = t1/f_n.get(i); 
						total_local += t1;
                        res.add(t1);   
					       
                }
			//	System.out.println("local" + res);  
                return total_local;
            }

			public static void writeCSV(String a, String b, String c,String d)
            {
                  String csvFile = "output_random.csv";

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
			