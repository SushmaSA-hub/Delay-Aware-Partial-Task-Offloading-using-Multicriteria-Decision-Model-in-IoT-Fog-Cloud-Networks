
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
public class proposed {
	static ArrayList<Double> poisson_dist;
	static ArrayList<Double> exponential_dist;
	static int no_OfTask;
	static int no_OfHelper;
	static int no_OfVM;
	static int no_OfCriteria;
	static ArrayList<Integer> deadline;
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
	static int outages=0;
	static ArrayList<ArrayList<Double>> distance_n_m;
	static ArrayList<Double> distance_n_c;
	static ArrayList<ArrayList<Integer>> allocated_list;
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
	static ArrayList<ArrayList<Double>> metrics;
	static ArrayList<Double> weight;
	static ArrayList<Integer> best;
	static ArrayList<Integer> worst;
		public static void main(String[] args) {

		
		no_OfCriteria = 2;
		
		pe_n_m = new ArrayList<ArrayList<Double>>();// The processing efficiency of helper node m when executing task n
		pe_n_k = new ArrayList<ArrayList<Double>>();// The processing efficiency of VM k when executing task n
		pe_n_l = new ArrayList<Double>();// The processing efficiency of local node l when executing task n
		pe_n_k_total = new ArrayList<Double>();
		readValues();
		
		

		// calculating processing efficiency for helper node for all tasks
		for (int i = 0; i < no_OfTask; i++) {
			ArrayList<Double> row = new ArrayList<Double>();
			for (int j = 0; j < no_OfHelper; j++) {

				row.add((1 / r_m_n.get(j).get(i)) + ((double) pd_n.get(i) / (double) f_m.get(j)) + ((double) io_r_n.get(i) / r_m_n.get(j).get(i)));
			}
			pe_n_m.add(row);
		}

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
		System.out.println("-------------------------");
		/*System.out.println("pe_n_l" + pe_n_l);
		System.out.println("pe_n_m" + pe_n_m);
		System.out.println("pe_n_k" + pe_n_k);*/
		System.out.println("-------------------------");
		// PCRC Algorithm...
		allocated_list = PCRC();
		//System.out.println("allocated list" + allocated_list);

		tav_l = tav_l_calc();
		//System.out.println("hello");
		tav_m = tav_m_calc();
		
		tav_k = tav_k_calc();
		
		
		//System.out.println("tav_k : " + tav_k);
		//System.out.println("tav_m : " + tav_m);
		//System.out.println("tav_l : " + tav_l);
		
		
		comm_delay_n_m = comm_delay_m_calc();
		comm_delay_n_c = comm_delay_c_calc();
		comp_delay_n_m = comp_delay_m_calc();
		comp_delay_n_k = comp_delay_k_calc();
		comp_delay_n_l = comp_delay_l_calc();
		total_delay_n = (total_delay_calc());
		writeOutInt(no_OfTask);
		writeOutInt(no_OfHelper);
		writeOut(no_OfVM);


	double total_delay = calc_complete_delay();
		//writeOut(total_delay);
		double local = local_only();
        //System.out.println("local only: " + local);	
		writeOut(local);
		String a = String.valueOf(no_OfTask);
        String b = "PROPOSED";
		String c = String.valueOf(total_delay);
      // String c = String.valueOf(total_delay);
	   String d=String.valueOf(outages);
        writeCSV(a, b, c, d);
		
	}

	public static void writeOut(double res)
	{
		String filePath = "/input/STSoutput.txt";

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
	public static void writeOutInt(int res)
	{
		String filePath = "/input/STSoutput.txt";

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
        System.out.println("**********************");
		String base1 = "/input/fullInput/";
		filePath = base1+ "Exponential_dist.txt";
		exponential_dist = readNumbersFromFile1(filePath);
		filePath = base1+ "Poisson_dist.txt";
		poisson_dist = readNumbersFromFile1(filePath);
        System.out.println("no_OfVM");
        filePath = basePath + "no_OfVM.txt";
        no_OfVM = readIntFromFile(filePath);
        System.out.println(no_OfVM);
        System.out.println("no_OfHelper");
        filePath = basePath + "no_OfHelper.txt";
        no_OfHelper = readIntFromFile(filePath);
        System.out.println(no_OfHelper);
        System.out.println("no_OfTask");
        filePath = basePath + "no_OfTask.txt";
        no_OfTask = readIntFromFile(filePath);
        System.out.println(no_OfTask);
        //System.out.println("r_n_m");
        filePath = basePath + "r_n_m.txt";
        r_n_m = readNumbersFromFile2(filePath);
        //printDouble2(r_n_m);
       // System.out.println("r_m_n");
        filePath = basePath + "r_m_n.txt";
        r_m_n = readNumbersFromFile2(filePath);
        //printDouble2(r_m_n);
        //System.out.println("r_t_n");
        filePath = basePath + "r_t_n.txt";
        r_t_n = readNumbersFromFile1(filePath);
        //printDouble1(r_t_n);
        //System.out.println("r_r_n");
        filePath = basePath + "r_r_n.txt";
        r_r_n = readNumbersFromFile1(filePath);
        //printDouble1(r_r_n);
       // System.out.println("io_r_n");
        filePath = basePath + "io_r_n.txt";
        io_r_n = readNumbersFromFile1(filePath);
        //printDouble1(io_r_n);
        //System.out.println("ip_size:");
        filePath = basePath + "ip_size.txt";
        ip_size = readNumbersFromFileInt1(filePath);
        printInt1(ip_size);
        //System.out.println("deadline");
        filePath = basePath + "deadline.txt";
        deadline = readNumbersFromFileInt1(filePath);
        //printInt1(deadline);
        //System.out.println("pd_n");
        filePath = basePath + "pd_n.txt";
        pd_n = readNumbersFromFile1(filePath);
        //printDouble1(pd_n);
        //System.out.println("f_m");
        filePath = basePath + "f_m.txt";
        f_m = readNumbersFromFile1(filePath);
        //printDouble1(f_m);
        //System.out.println("f_k");
        filePath = basePath + "f_k.txt";
        f_k = readNumbersFromFile1(filePath);
        //printDouble1(f_k);
        //System.out.println("f_n");
        filePath = basePath + "f_n.txt";
        f_n = readNumbersFromFile1(filePath);
        //printDouble1(f_n);
        //System.out.println("p_t_n");
        filePath = basePath + "p_t_n.txt";
        p_t_n = readNumbersFromFile1(filePath);
        //printDouble1(p_t_n);
        //System.out.println("p_t_m");
        filePath = basePath + "p_t_m.txt";
        p_t_m = readNumbersFromFile1(filePath);
        //printDouble1(p_t_m);
        //System.out.println("p_t_c");
        filePath = basePath + "p_t_c.txt";
        p_t_c = readNumbersFromFile0(filePath);
        //printDouble0(p_t_c);
        //System.out.println("distance_n_m");
        filePath = basePath + "distance_n_m.txt";
        distance_n_m = readNumbersFromFile2(filePath);
        //printDouble2(distance_n_m);
       // System.out.println("distance_n_c");
        filePath = basePath + "distance_n_c.txt";
        distance_n_c = readNumbersFromFile1(filePath);
        //printDouble1(distance_n_c);
        //System.out.println("quota");
        filePath = basePath + "quota.txt";
        quota = readNumbersFromFileInt1(filePath);
        printInt1(quota);
	}
	
	public static void printDouble2(ArrayList<ArrayList<Double>> dist1) {
        // Display the read numbers
     //   System.out.println("Read Distances from File:");
        for (ArrayList<Double> row : dist1) {
            for (Double value : row) {
              //  System.out.print(value + " ");
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
          //  System.out.print(value + " ");
        }
        System.out.println();
    }

    public static void printDouble0(Double dist1) {
        // Display the read numbers
       // System.out.println("Read Distances from File:");
      //  System.out.println(dist1);
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
	
	//=====================>>>CRITIC AND TOPSIS
	public static void initCritic(int isHelper, int number)
	{
		
		//no_OfHelper = no_OfFog + no_OfVM;
		no_OfCriteria = 2;
		metrics = new ArrayList<ArrayList<Double>>();

		if(isHelper == 1)
		{
			//ArrayList<ArrayList<Double>> metrics1 = new ArrayList<ArrayList<Double>>();
			for(int j = 0; j < no_OfTask; j++)
			{
				ArrayList<Double> temp = new ArrayList<Double>();
				temp.add((double)deadline.get(j));
				temp.add(pe_n_m.get(j).get(number));
				metrics.add(temp);
			}
		//	metrics.add(metrics1);
		}
		else
		{
			//ArrayList<ArrayList<Double>> metrics1 = new ArrayList<ArrayList<Double>>();
			for(int j = 0; j < no_OfTask; j++)
			{
				ArrayList<Double> temp = new ArrayList<Double>();
				temp.add((double)deadline.get(j));
				temp.add(pe_n_k.get(j).get(number));
				metrics.add(temp);
			}
//			metrics.add(metrics1);
		}
		//System.out.println("Started");
		
		
		//weight  = deepCopy(metrics);
			int b_d=0;
			int b_pe=0;
			int w_d=no_OfTask-1;
			int w_pe =no_OfTask-1;
			//for(ArrayList<ArrayList<Double>> metrics1 : metrics)
			
				//for deadline find best
					for(int r = 0; r < no_OfTask; r++)
					{
					//	System.out.println(metrics.get(r).get(0) + "  " + metrics.get(w_d).get(0));
						if(metrics.get(r).get(0) < metrics.get(b_d).get(0))
						{
							b_d = r;
						}
						if(metrics.get(r).get(0) > metrics.get(w_d).get(0))
						{
							w_d = r;
						}
						if(metrics.get(r).get(1) < metrics.get(b_pe).get(1))
						{
							b_pe = r;
						}
						if(metrics.get(r).get(1) > metrics.get(w_pe).get(1))
						{
							w_pe = r;
						}
						
					}
				
			
		best = new ArrayList<>(Arrays.asList(b_d,b_pe));
		worst = new ArrayList<>(Arrays.asList(w_d,w_pe));
		//System.out.println(metrics);
		//System.out.println(weight);
		//System.out.println(best);
		//System.out.println(worst);
		
	}
		private static ArrayList<Map.Entry<Integer, Double>> test(ArrayList<Map.Entry<Integer, Double>> row) {
	        // Example: Sorting the ArrayList (replace this with your actual logic)
	        Collections.sort(row, Comparator.comparing(Map.Entry::getValue));

	        return row;
	    }
	
		
	//@param h=> helper node number
	private static ArrayList<Map.Entry<Integer, Double>> fogOrdering(int isHelper, int number)
	{
		//ArrayList<Map.Entry<Integer, Double>> r1;
		initCritic(isHelper, number);
		ArrayList<ArrayList<Double>> normWght = new ArrayList<ArrayList<Double>>();
		//bike
		for(int i = 0; i < no_OfTask; i++)
		{
			ArrayList<Double> temp = new ArrayList<Double>();
			for(int j = 0; j < no_OfCriteria; j++)
			{
				Double res ;
				int wrst = worst.get(j);
				int bst = best.get(j);
				if(metrics.get(bst).get(j) - metrics.get(wrst).get(j) == 0)
						System.out.println("I am inside ..."+i+" "+j + " "+ metrics.get(bst).get(j) +  " "+ metrics.get(wrst).get(j));
				res = ( (metrics.get(i).get(j) - metrics.get(wrst).get(j)) / (metrics.get(bst).get(j) - metrics.get(wrst).get(j)));
				temp.add(res);
			}
			normWght.add(temp);
		}
		//System.out.println("for std" + normWght);
		//weight = deepCopy(normWght);
		//System.out.println(weight);
		//currWght.clear();
		ArrayList<Double> std;
		std = Stdeva(normWght);
		//System.out.println("std" + std);
		ArrayList<ArrayList<Double>> corelncoeff = new ArrayList<ArrayList<Double>>();
		//Initialize the above with 0
		for(int i =0; i < no_OfCriteria; i++)
		{
			ArrayList<Double> t =new ArrayList<Double>();
			for(int j =0; j < no_OfCriteria; j++)
			{
				t.add(1.0);
			}
			corelncoeff.add(t);
		}
		for(int i =0 ; i< no_OfCriteria; i++)
		{
			for(int j = 1; j < no_OfCriteria; j++)
			{
				ArrayList<Double> x = new ArrayList<Double>();
				ArrayList<Double> y = new ArrayList<Double>();
				for(int r = 0; r < no_OfTask;r++)
				{
					x.add(normWght.get(r).get(i));
				}
				for(int r = 0; r < no_OfTask;r++)
				{
					y.add(normWght.get(r).get(j));
				}
				double c = calCorelnCoeff(x,y);
				corelncoeff.get(i).set(j, c);
				corelncoeff.get(j).set(i, c);
				
			}
		}
		//System.out.println(corelncoeff);
		//calculate the measure of conflict created by criterion j w.r.t
		for(int i =0 ;i < no_OfCriteria; i++)
		{
			for(int j = 0; j < no_OfCriteria; j++)
			{
				double v = corelncoeff.get(i).get(j);
				corelncoeff.get(i).set(j, 1-v);
			}
		}
		//System.out.println(corelncoeff);
		ArrayList<Double> conflict = new ArrayList<Double>();
		for(int i =0; i < no_OfCriteria; i++)
		{
			double sum = 0.0;
			for(int j =0; j< no_OfCriteria; j++)
			{
				sum += corelncoeff.get(i).get(j);
			}
			conflict.add(sum);
		}
		//System.out.println("conflict" + conflict);
		//calculate the quantity of info in rel to each criteria;
		//this is pdt of std and conflict
		ArrayList<Double> resWght = new ArrayList<Double>();
		for(int i =0; i< no_OfCriteria; i++)
		{
			double v = std.get(i) * conflict.get(i);
			resWght.add(v);
		}
		//System.out.println("pdt" + resWght);
		//Acutal wght calculation for which we need sum of the above all
		double sumres = 0.0;
		for(int i =0; i< no_OfCriteria; i++)
		{
			sumres += resWght.get(i);
		}
		for(int i =0 ;i < no_OfCriteria; i++)
		{
			double v = resWght.get(i)/sumres;
			resWght.set(i, v);	
		}
		//System.out.println(resWght);
		weight = resWght;
		
		
		//TOPSIS
		ArrayList<ArrayList<Double>> norm = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> norm1 = deepCopy(metrics);
		norm = calcNorm(norm1);
		
		//System.out.println("norm" + norm);
		//System.out.println("metrics" + metrics);
		
		ArrayList<Integer> rank= calcPriority(norm);
		ArrayList<Map.Entry<Integer, Double>> tempArr = new ArrayList<Map.Entry<Integer, Double>>();
		for(int i =0; i < no_OfTask; i++)
		{
			Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(0,0.0);
			tempArr.add(tempMap);
		}
	//	System.out.println(rank);
		if(isHelper == 1)
		{
			for(int i = 0; i < no_OfTask; i++)
			{
				int pos = rank.get(i);
				Map.Entry<Integer, Double> newEntry = new AbstractMap.SimpleEntry<>(i, pe_n_m.get(i).get(number));

	            // Set the new entry at the specified position
	            tempArr.set(pos-1, newEntry);
			}
		}
		else
		{
			for(int i = 0; i < no_OfTask; i++)
			{
				int pos = rank.get(i);
				Map.Entry<Integer, Double> newEntry = new AbstractMap.SimpleEntry<>(i, pe_n_k.get(i).get(number));

	            // Set the new entry at the specified position
	            tempArr.set(pos-1, newEntry);
			}
		}
		
		//System.out.println(rank);
	//	System.out.println("--------------------------------------------");
		
	return tempArr;
	}
	
	private static ArrayList<ArrayList<Double>> deepCopy(ArrayList<ArrayList<Double>> original) {
        ArrayList<ArrayList<Double>> copy = new ArrayList<>();

        for (ArrayList<Double> innerList : original) {
            ArrayList<Double> innerCopy = new ArrayList<>(innerList);
            copy.add(innerCopy);
        }

        return copy;
    }
	static ArrayList<Double> Stdeva(ArrayList<ArrayList<Double>> mat)
	{
		//System.out.println("hi");
		int no_OfCol = mat.get(0).size();
		
		int no_OfRow = mat.size();
		
		ArrayList<Double> res = new ArrayList<Double>();
		
		for(int i = 0; i < no_OfCol; i++)
		{
			ArrayList<Double> curr = new ArrayList<Double>();
			for(int j = 0; j < no_OfRow; j++)
			{
				curr.add(mat.get(j).get(i));
			}
			double sum = 0.0;
			for(double num : curr)
			{
				sum += num;
			}
			double mean = sum/curr.size();
			double sqDiffSum = 0.0;
	        for (double num : curr)
	        {
	            double diff = num - mean;
	            sqDiffSum += Math.pow(diff, 2);
	        }
	        double meanSqDiff = sqDiffSum / curr.size();
			double std = Math.sqrt(meanSqDiff);
			res.add(std);			
			
		}
		return res;
	}
	 public static double calCorelnCoeff(ArrayList<Double> x, ArrayList<Double> y) {
	        // Check if the lists are of the same length
	        if (x.size() != y.size()) {
	            throw new IllegalArgumentException("Input lists must have the same length");
	        }

	        int n = x.size();

	        // Calculate means
	        double meanX = calculateMean(x);
	        double meanY = calculateMean(y);

	        // Calculate numerator
	        double numerator = 0;
	        for (int i = 0; i < n; i++) {
	            numerator += (x.get(i) - meanX) * (y.get(i) - meanY);
	        }

	        // Calculate denominators
	        double denominatorX = calculateSumOfSquares(x, meanX);
	        double denominatorY = calculateSumOfSquares(y, meanY);

	        // Calculate correlation coefficient
	        double correlationCoefficient = numerator / Math.sqrt(denominatorX * denominatorY);
	        
	        return correlationCoefficient;
	    }

	    private static double calculateMean(ArrayList<Double> list) {
	        if (list.isEmpty()) {
	            return Double.NaN;
	        }

	        double sum = 0;
	        for (Double value : list) {
	            sum += value;
	        }
	        //System.out.println("size" + list.size());
	        return sum / list.size();
	    }

	    private static double calculateSumOfSquares(ArrayList<Double> list, double mean) {
	        double sum = 0;
	        for (Double value : list) {
	            sum += Math.pow(value - mean, 2);
	        }
	        return sum;
	    }
	    
	    //TOPSIS............
	    private static ArrayList<ArrayList<Double>> calcNorm(ArrayList<ArrayList<Double>> mat)
	    {
	    	ArrayList<Double> sumOfSquare = new  ArrayList<Double>();
			for(int i = 0; i < no_OfCriteria; i++)
			{
				ArrayList<Double> currCol = new ArrayList<Double>(); 
				for(int j = 0;j < no_OfTask; j++)
				{
					currCol.add(mat.get(j).get(i));
				}
				sumOfSquare.add(calcSumOfSq(currCol));
			}
			for(int i = 0; i < no_OfTask; i++)
			{
				for(int j = 0; j < no_OfCriteria; j++)
				{
					double v = mat.get(i).get(j)/sumOfSquare.get(j);
					mat.get(i).set(j, v);
				}
			}
			for(int i =0 ;i < no_OfTask; i++)
			{
				for(int j = 0; j < no_OfCriteria; j++)
				{
					double v = mat.get(i).get(j)/weight.get(j);
					mat.get(i).set(j, v);
				}
			}
			return mat;
	    }
	    private static Double calcSumOfSq(ArrayList<Double> list) {
	        // Calculate the square root of the sum of squared values
	        double sumOfSquares = 0.0;
	        for (double value : list) {
	            sumOfSquares += (value * value);
	        }
	        double sqrtSumOfSquares = Math.sqrt(sumOfSquares);
	       // System.out.println("squares"+ sqrtSumOfSquares);
	        return sqrtSumOfSquares;
	        /*// Normalize each value in the ArrayList
	        ArrayList<Double> normalizedList = new ArrayList<>();
	        for (double value : list) {
	            double normalizedValue = value / sqrtSumOfSquares;
	            normalizedList.add(normalizedValue);
	        }

	        return normalizedList;*/
	    }
	   private static ArrayList<Integer> calcPriority(ArrayList<ArrayList<Double>> mat)
	   {
		   ArrayList<Integer> result = new ArrayList<Integer>();
		   ArrayList<Double> SiPlus = new ArrayList<Double>();//Euclidien distance from ideal best
		   ArrayList<Double> SiMinus = new ArrayList<Double>();//Euclidien distance from ideal worst
		   //performance score
		   ArrayList<Double> perfScore = new ArrayList<Double>();
		   //final ranking
		   Map<Double, Integer> rank = new TreeMap<>();
		  
		   for(int i = 0; i < no_OfTask; i++)
		   {
			   double sum = 0.0;
			   for(int j = 0; j < no_OfCriteria; j++)
			   {
				   double bst = mat.get(best.get(j)).get(j);
				   //System.out.println("check");
				  // double wrst = mat.get(i).get(worst.get(j));
				   double curr = mat.get(i).get(j);
				   sum += ((curr-bst) * (curr-bst));
				   
			   }
			   sum = Math.pow(sum, 0.5);
			   SiPlus.add(sum);
		   }
		  
		   for(int i = 0; i < no_OfTask; i++)
		   {
			   
			   double sum = 0.0;
			   for(int j = 0; j < no_OfCriteria; j++)
			   {
				  // double bst = mat.get(i).get(best.get(j));
				  double wrst = mat.get(worst.get(j)).get(j);
				   double curr = mat.get(i).get(j);
				   sum += ((curr-wrst) * (curr-wrst));
				   
			   }
			   sum = Math.pow(sum, 0.5);
			   SiMinus.add(sum);
		   }
		   for(int i = 0;i < no_OfTask; i++)
		   {
			   double v = SiMinus.get(i)/(SiPlus.get(i) + SiMinus.get(i));
			   perfScore.add(v);
		   }
		   for(int i = 0; i< no_OfTask; i++)
		   {
			   rank.put(perfScore.get(i), i);
			   result.add(i);
		   }
		 //  System.out.println("perfScore" + perfScore);

		   int cnt= result.size();
	        for (Map.Entry<Double, Integer> entry : rank.entrySet()) {
	            int v = entry.getValue();
	           // System.out.println("h:"+v+" "+entry.getKey());
	            result.set(v,cnt);
	            cnt--;
	        }
		   return result;
	   }

	
	// ====================>>>PMCS
	

	public static ArrayList<Map.Entry<Integer, Double>> PMCS(ArrayList<Map.Entry<Integer, Double>> A_H,
			ArrayList<Map.Entry<Integer, Double>> A_V, int task_no) {

		// The key value pair A_H represents the helper node id and its processing
		// efficiency and the same goes for A_V for VMs
		// System.out.println("=====>>>>INSIDE PMCS");

		int A_HSize = A_H.size();
		int A_VSize = A_V.size();

		int mp1;// index to last VM in A_V
		int kp2;// index to last Helper node in A_H
		int quota_n = quota.get(task_no);
		// Sorting A_H according to increasing order of Processing efficiency
		Collections.sort(A_H, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) {
				return e1.getValue().compareTo(e2.getValue());
			}
		});

		// Sorting A_K according to increasing order of Processing efficiency
		Collections.sort(A_V, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) {
				return e1.getValue().compareTo(e2.getValue());
			}
		});

		// System.out.println("Initially A_H"+A_H);
		// System.out.println("A_V"+A_V);

		if (A_HSize + A_VSize <= quota_n) {
			int x = A_H.size();
			double y = (double) x;
			Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(x, y);
			A_H.add(0, tempMap);
			A_H.addAll(A_V);
			return A_H;
		}

		if (A_HSize > quota_n) {
			mp1 = quota_n - 1;// since index starts with 0;
			while (A_H.size() > mp1 + 1) {
				int t = A_H.size() - 1;
				A_H.remove(t);
			}
		} else {
			mp1 = A_HSize - 1;
		}

		if (A_VSize > quota_n) {
			kp2 = quota_n - 1;// since index starts with 0;
			while (A_V.size() > kp2 + 1) {
				int t = A_V.size() - 1;
				A_V.remove(t);
			}
		} else {
			kp2 = A_VSize - 1;
		}

		while ((mp1 + 1) + (kp2 + 1) > quota_n) {
			double pe_mp1 = (double) (A_H.get(mp1).getValue());
			double pe_kp2 = (double) (A_V.get(kp2).getValue());
			double pe_A_V, pe_A_V1;
			
				if (pe_mp1 < pe_kp2) {
					A_V.remove(kp2);
					kp2--;
				} else {
					// Remove mp1(vm with highest processing efficiency from A_H) from A_H
					A_H.remove(mp1);
					mp1--;
				}
	
		}

		int x = A_H.size();
		double y = (double) x;
		Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(x, y);
		A_H.add(0, tempMap);
		A_H.addAll(A_V);
		return A_H;
	}

	// PCRC Algorithm...
	public static ArrayList<ArrayList<Integer>> PCRC() {
		ArrayList<ArrayList<Map.Entry<Integer, Double>>> cand_list_m = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
		ArrayList<ArrayList<Map.Entry<Integer, Double>>> cand_list_k = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
		ArrayList<Boolean> freeHelper = new ArrayList<Boolean>();// If the helper is in the waiting list of any other
																	// task node then it will become false
		ArrayList<Boolean> freeVM = new ArrayList<Boolean>();// If the vm is in the waiting list of any other task node
																// then it will become false
		for (int i = 0; i < no_OfHelper; i++) {
			freeHelper.add(true);
		}
		for (int i = 0; i < no_OfVM; i++) {
			freeVM.add(true);
		}
	
		for(int i = 0; i < no_OfHelper; i++)
		{
			// fogOrdering(int isHelper, int number)
			ArrayList<Map.Entry<Integer, Double>> tempArr = fogOrdering(1,i);
			cand_list_m.add(tempArr);
		}
		for(int i = 0; i < no_OfVM; i++)
		{
			// fogOrdering(int isHelper, int number)
			ArrayList<Map.Entry<Integer, Double>> tempArr = fogOrdering(0,i);
			cand_list_k.add(tempArr);
		}
		
		// System.out.println("Preference profile that is candidate list for helper
		// nodes" + cand_list_m);
		// System.out.println("Preference profile that is candidate list for vm nodes" +
		// cand_list_k);
		boolean bothEmpty = false;// if bothEmpty = true it means both candidate list of vms and helper nodes both
									// are empty
		ArrayList<ArrayList<Integer>> waiting_list_n = new ArrayList<ArrayList<Integer>>();// list of waiting list for
																							// every task node and the
																							// first column of every row
																							// has a
																							// number which represents
																							// upto which is helper node
																							// and the remaining are vm
		ArrayList<ArrayList<Integer>> proporser_list_n = new ArrayList<ArrayList<Integer>>();// list of waiting list for
																								// every task node and
																								// the first column of
																								// every row has a
																								// number which
																								// represents upto which
																								// is helper node and
																								// the remaining are vm
		for (int i = 0; i < no_OfTask; i++) {
			ArrayList<Integer> row = new ArrayList<Integer>();
			row.add(0);
			ArrayList<Integer> row1 = new ArrayList<Integer>();
			row1.add(0);
			proporser_list_n.add(row);
			waiting_list_n.add(row1);
		}

		while (!bothEmpty) {
			int c = 0;
			bothEmpty = true;
			c++;
			// System.out.println("********************Before proporser list " +
			// proporser_list_n);
			for (int i = 0; i < no_OfHelper; i++)// loops through the candidate list of all helper node and all vm
			{
				ArrayList<Map.Entry<Integer, Double>> row = cand_list_m.get(i);
				if ((!row.isEmpty()) && (freeHelper.get(i) == true)) {
					c++;

					bothEmpty = false;
					int pref_task;// This is the most prefered task by the VM or Helper node
					pref_task = cand_list_m.get(i).get(0).getKey();
					int helplist_no = proporser_list_n.get(pref_task).get(0);// this represents the number of helper
																				// nodes in the proporser list
					// System.out.println("@@@@@@@@@@@@@@@" + c + " i " + i + "help list no" +
					// helplist_no);
					proporser_list_n.get(pref_task).add(helplist_no + 1, i);
					proporser_list_n.get(pref_task).set(0, helplist_no + 1);// increase the number of helper nodes in
																			// the current proporser list
					row.remove(0);
				}
			}

			for (int i = 0; i < no_OfVM; i++)// loops through the candidate list of all helper node and all vm
			{
				ArrayList<Map.Entry<Integer, Double>> row = cand_list_k.get(i);
				if (!row.isEmpty() && (freeVM.get(i) == true)) {
					bothEmpty = false;
					int pref_task;// This is the most prefered task by the VM or Helper node
					pref_task = cand_list_k.get(i).get(0).getKey();
					proporser_list_n.get(pref_task).add(i);
					row.remove(0);
				}
			}
			// System.out.println("********************After proporser list " +
			// proporser_list_n);

			for (int i = 0; i < no_OfTask; i++) {
				ArrayList<Integer> curr_prop_list = proporser_list_n.get(i);
				if (curr_prop_list.size() != 1)// if size is 1 it means only one element and that represents the number
												// of helper nodes. Hence it is just empty
				{
					// separate A_H and A_V
					ArrayList<Map.Entry<Integer, Double>> A_H = new ArrayList<Map.Entry<Integer, Double>>();
					ArrayList<Map.Entry<Integer, Double>> A_V = new ArrayList<Map.Entry<Integer, Double>>();
					int no_help_currProp;// number of helper nodes in the current proposer list
					no_help_currProp = curr_prop_list.get(0);

					for (int j = 1; j <= no_help_currProp; j++)// 0 not included since it stores number of hleper nodes
																// in the list
					{
						int helper_node = curr_prop_list.get(j);
						Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(helper_node,
								pe_n_m.get(i).get(helper_node));

						A_H.add(tempMap);
					}
					for (int k = no_help_currProp + 1; k < curr_prop_list.size(); k++)// 0 not included since it stores
																						// number of hleper nodes in the
																						// list
					{
						int vm_node = curr_prop_list.get(k);
						Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(vm_node,
								pe_n_k.get(i).get(vm_node));
						A_V.add(tempMap);
					}

					// copy the waiting list also to a_h and a_v
					ArrayList<Integer> curr_wait_list = waiting_list_n.get(i);
					// System.out.println("current waiting list"+curr_wait_list);
					if (curr_wait_list.size() != 1) {
						int no_help_currWait;// number of helper nodes in the current proposer list
						no_help_currWait = curr_wait_list.get(0);

						for (int j = 1; j <= no_help_currWait; j++)// 0 not included since it stores number of hleper
																	// nodes in the list
						{
							int helper_node = curr_wait_list.get(j);

							Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(
									helper_node, pe_n_m.get(i).get(helper_node));
							A_H.add(tempMap);
						}
						for (int k = no_help_currWait + 1; k < curr_wait_list.size(); k++)// 0 not included since it
																							// stores number of hleper
																							// nodes in the list
						{
							int vm_node = curr_wait_list.get(k);
							Map.Entry<Integer, Double> tempMap = new AbstractMap.SimpleEntry<Integer, Double>(vm_node,
									pe_n_k.get(i).get(vm_node));
							A_V.add(tempMap);
						}
					}
					// we have updated a_h and a_v properly, now lets call the function PMCS
					// System.out.println("Before calling pmcs A_H"+A_H);
					// System.out.println("A_V"+A_V);
					//public static ArrayList<Map.Entry<Integer, Double>> PMCS(ArrayList<Map.Entry<Integer, Double>> A_H, ArrayList<Map.Entry<Integer, Double>> A_V, int quota_n, int task_no) {

					ArrayList<Map.Entry<Integer, Double>> C_n = PMCS(A_H, A_V, i);
					// System.out.println("The C_n value is "+ C_n);
					int no_help = C_n.get(0).getKey();
					curr_wait_list.clear();
					curr_wait_list.add(no_help);
					for (int j = 1; j <= no_help; j++) {
						int help_node = C_n.get(j).getKey();
						curr_wait_list.add(help_node);
						freeHelper.set(help_node, false);
					}
					for (int k = no_help + 1; k < C_n.size(); k++) {
						int vm_node = C_n.get(k).getKey();
						curr_wait_list.add(vm_node);
						freeVM.set(vm_node, false);
					}

					curr_prop_list.clear();
					curr_prop_list.add(0);
					C_n.clear();
				}

			}

			// System.out.println("current wait list at end of loop" + waiting_list_n);
			// System.out.println("free helpers" + freeHelper);
			// System.out.println("free vm"+ freeVM);

		}
		// System.out.println("finally" + waiting_list_n);
		return waiting_list_n;

	}

	// calculate total processing density for given set of VM
	public static double pe_n_k_totalcal(ArrayList<Map.Entry<Integer, Double>> A_V,
			int A_VSize,int task_no) {
		double t1, t2, t3;
		double sum;
		sum = 0;
		t1 = 1 / r_t_n.get(task_no);
		t3 = io_r_n.get(task_no) / r_r_n.get(task_no);
		for (int i = 0; i < A_VSize; i++) {
			int ind;
			ind = A_V.get(i).getKey();
			sum += f_k.get(ind);

		}
		t2 = pd_n.get(task_no) / sum;

		return (t1 + t2 + t3);

	}

	// pe_n_md for the given n and m;

	// TAV of helper nodes
	public static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_m_calc() {
		ArrayList<ArrayList<Map.Entry<Integer, Double>>> res = new ArrayList<ArrayList<Map.Entry<Integer, Double>>>();
		ArrayList<Double> pe_n_cnv = pe_n_cnv_calculation();
		for (int i = 0; i < no_OfTask; i++) {
			int no_H, no_VM;
			ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
			ArrayList<Integer> curr_list = allocated_list.get(i);
			no_H = curr_list.get(0);
			no_VM = curr_list.size() - no_H - 1;
			for (int j = 1; j <= no_H; j++) {
				int curr_H = curr_list.get(j);
				
				double t = 0.0;
				t = (tav_l.get(i).getValue() * r_n_m.get(i).get(curr_H) * pd_n.get(i) * f_m.get(curr_H) ) / (f_n.get(i) *( f_m.get(curr_H) + (r_n_m.get(i).get(curr_H) *pd_n.get(i)))) ;//quota.get(i);
				
				temp.add(createEntry(curr_H, t));
			}
			res.add(temp);
		}
		return res;

	}

	// TAV of local nodes
	public static ArrayList<Map.Entry<Integer, Double>> tav_l_calc() {
		ArrayList<Map.Entry<Integer, Double>> tav_n_l = new ArrayList<Map.Entry<Integer, Double>>();
		for (int i = 0; i < no_OfTask; i++) {
			int no_H, no_VM;
			ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
			ArrayList<Integer> curr_list = allocated_list.get(i);
			no_H = curr_list.get(0);
			no_VM = curr_list.size() - no_H - 1;
			double res = 1.0;
			double t = 0.0;
			for(int j = 1; j <= no_H; j++)
			{
				int curr_H = curr_list.get(j);
				t += (r_n_m.get(i).get(curr_H) * f_m.get(curr_H)) / (f_m.get(curr_H) +(r_n_m.get(i).get(curr_H) *pd_n.get(i)));
			}
			for(int j = no_H+1; j < curr_list.size(); j++) {
				int curr_VM = curr_list.get(j);
				//f_k.get(curr_VM);
				t += (r_t_n.get(i)  * f_k.get(curr_VM)) / ( f_k.get(curr_VM) + (r_t_n.get(i) *pd_n.get(i)));
			}
			t = t * pd_n.get(i) / f_n.get(i);
			res = 1/(1+t);
			tav_n_l.add(createEntry(i, res));
		}
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
		ArrayList<Double> pe_n_cnv = pe_n_cnv_calculation();
	
		for (int i = 0; i < no_OfTask; i++) {
			int no_H, no_VM;
			ArrayList<Map.Entry<Integer, Double>> temp = new ArrayList<Map.Entry<Integer, Double>>();
			ArrayList<Integer> curr_list = allocated_list.get(i);
			no_H = curr_list.get(0);
			no_VM = curr_list.size() - no_H - 1;
			for (int j = no_H+1; j < curr_list.size(); j++) {
				int curr_VM = curr_list.get(j);
				
				double t = 0.0;
				t = (tav_l.get(i).getValue() * r_t_n.get(i) * pd_n.get(i) * f_k.get(curr_VM) ) / (f_n.get(i) *( f_k.get(curr_VM) + (r_t_n.get(i) *pd_n.get(i)))) ;
				
				temp.add(createEntry(curr_VM, t));
			}
			res.add(temp);
		}
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
	//	System.out.println("comm_delay_m : " + comm_delay);
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
			//System.out.println("comm_delay_C : " + comm_delay);
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
		//	System.out.println("comp_delay_m : " + comp_delay);
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
		//	System.out.println("comp_delay_k : " + comp_delay);
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
			//System.out.println("comp_delay_l : "+ comp_delay);
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
			    total.add(res);
				totalEnergy.add(res*p_t_n.get(i));
			}
			
			//System.out.println("total delay : " + total);
			//System.out.println("total energy: "+ totalEnergy);
			for(int i = 0;i < no_OfTask; i++)
			{
				sumEnergy = sumEnergy + totalEnergy.get(i);
			}
			//writeOut(sumEnergy);
			return total;
		}
		
		public static double calc_complete_delay()
		{
			double res = 0.0;
			
			int count=0;
			//System.out.println("deadline: "+deadline);
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
				System.out.println(count);
			System.out.println(res);
			//writeOutInt(count);
			outages=no_OfTask-count;
				//System.out.println("discaraded :"+outages);
				writeOutInt(outages);
				//System.out.println(res);
				return res;
		}
		public static double local_only()
            {
				double res = 0.0;
				Double total_local=0.0;
                for(int i = 0; i< no_OfTask;i++)
                {
                        double t1 = pd_n.get(i)*ip_size.get(i);
						res = t1/f_n.get(i); 
						total_local += res;     
                }
				System.out.println("Local execution:"+total_local);
				writeOut(total_local);
                return total_local;
            }
		public static void writeCSV(String a, String b, String c,String d)
            {
                  String csvFile = "output_proposed.csv";

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

			