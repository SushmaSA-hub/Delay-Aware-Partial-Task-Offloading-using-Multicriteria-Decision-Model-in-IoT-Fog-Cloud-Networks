
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class generator_new {
    static int no_OfTask;
    static int no_OfHelper;
    static int no_OfVM;
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
    static ArrayList<Integer> quota;
    static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_k;
    static ArrayList<ArrayList<Map.Entry<Integer, Double>>> tav_m;
    static ArrayList<Map.Entry<Integer, Double>> tav_l;
    static ArrayList<ArrayList<Map.Entry<Integer, Double>>> comm_delay_n_m;
    static ArrayList<Double> comm_delay_n_c;
    static ArrayList<ArrayList<Map.Entry<Integer, Double>>> comp_delay_n_m;
    static ArrayList<ArrayList<Map.Entry<Integer, Double>>> comp_delay_n_k;
    static ArrayList<Map.Entry<Integer, Double>> comp_delay_n_l;
    static ArrayList<Double> total_delay_n;
    static ArrayList<Integer> deadline;

    public static void main(String[] args) {

        no_OfVM =50;
        no_OfHelper = 200;// Helper nodes
        no_OfTask = 250;// Number of task nodes

        p_t_n = new ArrayList<Double>();// transmitting power of task nodes
        p_t_m = new ArrayList<Double>();// transmitting power of helper nodes
        p_t_c = 20.0;

        pd_n = new ArrayList<Double>(); // The processing density of task n(=====>eetta)
        io_r_n = new ArrayList<Double>(); // The output-input ration of task n(====>muuu)
        f_m = new ArrayList<Double>();// The CPU frequency of helper nodes;
        f_k = new ArrayList<Double>();// The CPU frequency of VM;
        f_n = new ArrayList<Double>();// The CPU frequency of task nodes;
        distance_n_m = new ArrayList<ArrayList<Double>>();// distance in meters
        distance_n_c = new ArrayList<Double>();
        r_n_m = new ArrayList<ArrayList<Double>>();
        r_m_n = new ArrayList<ArrayList<Double>>();
        r_t_n = new ArrayList<Double>();
        r_r_n = new ArrayList<Double>();
        quota = new ArrayList<Integer>();
        ip_size = new ArrayList<Integer>();
        deadline = new ArrayList<Integer>();
        // = new ArrayList<ArrayList<Double> >();//communication delay caused by
        // offloading computation tasks to the helper nodes followed by cloud

     // double[] f_n_choice = new double[] { 0.5e6, 0.6e6, 0.9e6, 0.7e6, 0.8e6};
        double[] f_n_choice = new double[] { 0.7e6, 0.7e6, 0.7e6, 0.7e6, 0.7e6 };  //heterogeneous parameters
   //double[] f_m_choice = new double[] { 2.5e9, 1.0e9, 1.8e9, 2.0e9, 1.5e9  };
       double[] f_m_choice = new double[]{ 1.8e9, 1.8e9, 1.8e9, 1.8e9, 1.8e9 }; //heterogeneous parameters
        //double[] f_k_choice = new double[] { 30e9, 15e9, 35e9, 25e9, 10e9, 20e9 };
      //double [] f_k_choice = new double[] {5e9, 10e9, 15e9, 20e9, 25e9, 30e9}; 
      double[] f_k_choice = new double[] {15e9, 15e9, 15e9, 15e9, 15e9, 15e9}; //heterogeneous parameters
     //double[] pd_n_choice = new double[]  { 500,800,1000 };
      double[] pd_n_choice = new double[] {800, 800, 800}; //heterogeneous parameters
       // double[] bw_choice = new double[] { 15e3, 30e3, 45e3, 60e3, 75e3, 90e3 };
    //  double[] bw_choice = new double[] { 15e3, 30e3, 45e3, 60e3, 75e3, 90e3 };
        double[] bw_choice = new double[]  { 45e3, 45e3, 45e3, 45e3, 45e3, 45e3 }; //heterogeneous parameters
        // generate distance between task node and helper node
        for (int i = 0; i < no_OfTask; i++) {
            // generate input size
            ip_size.add(generateRandomValueInRange((int) 500e3 * 8 ,(int) 5000e3 * 8));// (500, 5000));
            deadline.add(generateRandomValueInRange(6000,140000));
            ArrayList<Double> temp = new ArrayList<Double>();
            for (int j = 0; j < no_OfHelper; j++) {
                temp.add((double) generateRandomValueInRange(20, 30));
            }
            distance_n_m.add(temp);
        }

        // generate distance between task node and cloud;
        for (int i = 0; i < no_OfTask; i++) {
            distance_n_c.add((double) generateRandomValueInRange(50, 60));
            double p_t = generateRandomValueInRange(200, 200);
            p_t = p_t / 1000;// milli watt;
            p_t_n.add(p_t);
        }

        // generate tansmission power for fog nodes
        for (int i = 0; i < no_OfHelper; i++) {
            double p_t = generateRandomValueInRange(200, 200);
           // double p_t=200;//200;
            p_t = p_t / 1000;// milli watt;
            p_t_m.add(p_t);
        }
        for (int i = 0; i < no_OfTask; i++) {
            quota.add(generateRandomValueInRange(0, 6));
            ArrayList<Double> temp = new ArrayList<Double>();
            for (int j = 0; j < no_OfHelper; j++) {
                // public static double calculate_r(double bw, int d, double p_t, double N_o)
                double bw = bw_choice[generateRandomValueInRange(0, 5)];
                double distance = distance_n_m.get(i).get(j);
                distance = distance / 1000;// distance in KM
                temp.add(calculate_r(bw, distance, p_t_n.get(i), 3.98e-21));
            }
            r_n_m.add(temp);

            double distance = distance_n_c.get(i);// distance from task node to entire cloud
            distance = distance / 1000;// distance in KM
            // public static double calculate_r(double bw, int d, double p_t, double N_o)
            double bw = bw_choice[generateRandomValueInRange(0, 5)];
            r_t_n.add(calculate_r(bw, distance, p_t_c, 3.98e-21));
            r_r_n.add(calculate_r(bw, distance, p_t_c, 3.98e-21));
            io_r_n.add(generateRandomValueInDouble(0.01, 0.5));
            pd_n.add(pd_n_choice[generateRandomValueInRange(0, 2)]);
            f_n.add(f_n_choice[generateRandomValueInRange(0, 4)]);
        }
        System.out.println("quota of task nodes" + quota);
        System.out.println("inp size of task nodes :" + ip_size);
        System.out.println("Processing density is" + pd_n);
        System.out.println("-------------------------");
        System.out.println("frequency of task node is" + f_n);

        // =>add frequency of all nodes to the variable
        for (int i = 0; i < no_OfHelper; i++) {
            ArrayList<Double> temp = new ArrayList<Double>();
            for (int j = 0; j < no_OfTask; j++) {
                // public static double calculate_r(double bw, int d, double p_t, double N_o)
                double bw = bw_choice[generateRandomValueInRange(0, 5)];
                bw = bw / 1000;
                double distance = distance_n_m.get(j).get(i);
                distance = distance / 1000;
                temp.add(calculate_r(bw, distance, p_t_m.get(i), 3.98e-21));
            }
            r_m_n.add(temp);

            f_m.add(f_m_choice[generateRandomValueInRange(0, 4)]);
        }
        System.out.println("frequency of helper node is" + f_m);

        for (int i = 0; i < no_OfVM; i++) {
            f_k.add(f_k_choice[generateRandomValueInRange(0, 4)]);
        }

        System.out.println("frequency of vm node is" + f_k);
        System.out.println("-------------------------");
        // System.out.println("r_m_n" + r_m_n);
        // System.out.println("r_n_m" + r_n_m);
        // System.out.println("r_t_n" + r_t_n);
        // System.out.println("r_r_n" + r_r_n);

        String basePath = "/input/inputSTS/";
        String filePath;
        filePath = basePath + "no_OfVM.txt";
        writeInteger(filePath, no_OfVM);
        filePath = basePath + "no_OfHelper.txt";
        writeInteger(filePath, no_OfHelper);
        filePath = basePath + "no_OfTask.txt";
        writeInteger(filePath, no_OfTask);

        filePath = basePath + "r_n_m.txt";
        writeArraylist2(filePath, r_n_m);
        filePath = basePath + "r_m_n.txt";
        writeArraylist2(filePath, r_m_n);
        filePath = basePath + "r_t_n.txt";
        writeArraylist1(filePath, r_t_n);
        filePath = basePath + "r_r_n.txt";
        writeArraylist1(filePath, r_r_n);
        filePath = basePath + "io_r_n.txt";
        writeArraylist1(filePath, io_r_n);
        filePath = basePath + "ip_size.txt";
        writeArraylist1Int(filePath, ip_size);
        filePath = basePath + "deadline.txt";
        writeArraylist1Int(filePath, deadline);
        filePath = basePath + "pd_n.txt";
        writeArraylist1(filePath, pd_n);
        filePath = basePath + "f_m.txt";
        writeArraylist1(filePath, f_m);
        filePath = basePath + "f_k.txt";
        writeArraylist1(filePath, f_k);
        filePath = basePath + "f_n.txt";
        writeArraylist1(filePath, f_n);
        filePath = basePath + "p_t_n.txt";
        writeArraylist1(filePath, p_t_n);
        filePath = basePath + "p_t_m.txt";
        writeArraylist1(filePath, p_t_m);
        filePath = basePath + "p_t_c.txt";
        writeArraylist0(filePath, p_t_c);
        filePath = basePath + "distance_n_m.txt";
        writeArraylist2(filePath, distance_n_m);
        filePath = basePath + "distance_n_c.txt";
        writeArraylist1(filePath, distance_n_c);
        filePath = basePath + "quota.txt";
        writeArraylist1Int(filePath, quota);

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

    // Log base n calculation
    public static double logn(double value, double base) {
        return Math.log(value) / Math.log(base);
    }

    // channel gain i.e. path loss calculation
    /*
     * @param d ->distance in km
     * 
     * @param bw ->Bandwidth in KHz
     */
    public static double calculate_channel_gain(double d, double bw) {
        // formula for path loss to chanel gain conversion is taken from meto paper
        double path_loss = (20 * logn(d, 10)) + (20 * logn(bw, 10)) + 32.45;
        return Math.pow(10, -(path_loss / 10));
    }

    // Data Rate calculation
    /*
     * @param bw -> bandwidth in hertz
     * 
     * @param g -> channel gain -> calculated using function
     * 
     * @param d -> distance needed to calculate path loss
     * 
     * @param p_t ->transmitting power
     * 
     * @param N_o ->noise power spectral density
     * 
     */
    public static double calculate_r(double bw, double d, double p_t, double N_o) {
        double g = calculate_channel_gain(d, bw / 1000);
        double t = logn(1 + ((g * p_t) / (bw * N_o)), 2);
        return bw * t;
    }

    private static void writeArraylist2(String filePath, ArrayList<ArrayList<Double>> values) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write numbers to the file
            for (ArrayList<Double> row : values) {
                for (int i = 0; i < row.size(); i++) {
                    writer.print(row.get(i));
                    if (i < row.size() - 1) {
                        writer.print(" "); // Use space as a delimiter
                    }
                }
                writer.println(); // Move to the next line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeArraylist1(String filePath, ArrayList<Double> row) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write numbers to the file

            for (int i = 0; i < row.size(); i++) {
                writer.print(row.get(i));
                if (i < row.size() - 1) {
                    writer.print(" "); // Use space as a delimiter
                }
                // writer.println(); // Move to the next line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeInteger(String fileName, int value) {
        try {
            // Create a FileWriter and PrintWriter
            FileWriter fileWriter = new FileWriter(new File(fileName));
            PrintWriter printWriter = new PrintWriter(fileWriter);

            // Write the integer value to the file
            printWriter.println(value);

            // Close the PrintWriter to flush the data to the file
            printWriter.close();

            System.out.println("Integer value has been written to the file.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeArraylist1Int(String filePath, ArrayList<Integer> row) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write numbers to the file

            for (int i = 0; i < row.size(); i++) {
                writer.print(row.get(i));
                if (i < row.size() - 1) {
                    writer.print(" "); // Use space as a delimiter
                }
                // writer.println(); // Move to the next line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeArraylist0(String filePath, Double row) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write numbers to the fil
            writer.print(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}