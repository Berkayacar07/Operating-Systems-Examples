import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Berkay Acar
 * @StudentNumber 20190808036
 * 22.12.2022
 */

// Run with makefile --> make ARGS=jobs.txt
// Delete .class files with makefile --> make clean
public class c20190808036 {

    public static String[] split;
    public static String[] split2;
    public static ArrayList<Integer> processId = new ArrayList<>();
    public static ArrayList<Integer> processTimes = new ArrayList<>();
    public static ArrayList<Integer> processTimesList = new ArrayList<>();
    public static LinkedHashMap<Integer, List<Integer>> map = new LinkedHashMap<>();
    public static LinkedHashMap<Integer, List<Map.Entry<Integer, Integer>>> processMap = new LinkedHashMap<>();
    public static ArrayList<Process> processQueue = new ArrayList<>();

    public static HashMap<Integer,Integer> returnTimeMap = new HashMap<>();
    public static HashMap<Integer,Integer> currentTimeMap = new HashMap<>();

    public static HashMap<Integer,Integer> tempReturnTimeMap = new HashMap<>();

    public static int currentTime = 0;
    public static int idleProcess = 0;
    public static Process process = null;
    public static void main(String[] args) {
        readIdAndBurstTimes(args[0]);
        processingMethod();
    }
    // Adds the process with the given id to the queue.
    public static void addProcessQueue(int id){

        // If there is no tuple to run, it says the process with that id is finished.
        if(map.get(processId.get(id)).size() ==0) {
            //System.out.println("Process " + processId.get(id) + " is finished");
            return;
        }
        // If the last tuple is reached, it sets the IO burst time to 0. Thus, there is no error in the return time calculation.
        if(map.get(processId.get(id)).get(1)==-1){
            process.cpuBurst = map.get(processId.get(id)).get(0);
            process.processId = processId.get(id);
            process.ioBurst = 0;
            processQueue.add(process);
        }else{
            // Adds ordinary.
            process.cpuBurst = map.get(processId.get(id)).get(0);
            process.ioBurst = map.get(processId.get(id)).get(1);
            process.processId = processId.get(id);
            processQueue.add(process);
        }
    }

    // It sorts how many processes there are in the txt according to their id number and adds them to the queue
    // It is only called the first time the application runs.
    public static void addProcessFirst(){
        // Sort here as ids should be in ascending order at first. As stated in the PDF
        processId.sort(Comparator.naturalOrder());

        // first add all process ids in order because there is no IO wait time
        for (Integer integer : processId) {
            process = new Process();
            process.cpuBurst = map.get(integer).get(0);
            process.ioBurst = map.get(integer).get(1);
            process.processId = integer;
            processQueue.add(process);
        }
    }

    // Waiting time hesaplaması için kullanılır.
    public static void calculateWaitingTime(){
        // hashMap içerisindeki valueleri toplayıp return time dan çıkaracağım için mapin içerisini burada doldurdum.
        addHashMap();
        for (int i = 0; i < map.size(); i++) {
            int sum = 0;
            for (int j = 0; j < map.get(processId.get(i)).size(); j+=2) {
                sum += map.get(processId.get(i)).get(j);
            }
            currentTimeMap.put(processId.get(i),sum);
            }
        // Avarage waiting time yazdırıyorum
        double sum = 0;
        for (int i = 0; i < currentTimeMap.size(); i++) {
            int a = (returnTimeMap.get(processId.get(i)) - currentTimeMap.get(processId.get(i)));
            sum += a;
        }
        System.out.println("Average waiting time: " + (sum / currentTimeMap.size()));
        }

    // It is used for Idle process operations.
    public static void idleProcess(){
        // Increase the number of idle processes each time the idle process method runs
        idleProcess++;

        // Check the processes in the processQueue to find the lowest return time correctly.
        // Add it to the tempReturnTimeMap if it is in the queue
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        for (Process value : processQueue) {
            integerArrayList.add(value.processId);
        }
        for (Integer integer : integerArrayList) {
            if (returnTimeMap.containsKey(integer)) {
                tempReturnTimeMap.put(integerArrayList.get(0), returnTimeMap.get(integer));
            }
        }
        //  Find the lowest return time
            int min = tempReturnTimeMap.values().stream().min(Integer::compare).get();


            // Find the id of the next process with the smallest return time
            // and equate it to the current process, then it leaves the queue.
            for (int i = 0; i < processQueue.size(); i++) {
                if(processQueue.get(i).returnTime == min){
                    process = processQueue.get(i);
                    processQueue.remove(i);
                    break;
                }
            }
        //System.out.println("Process idle is running at " + currentTime + " and returns at " + min);
            currentTime = min;
    }

    // It is used for turnaround time calculation.
    public static void calculateTurnAroundTime(){
        System.out.println("Average turnaround time: "+(returnTimeMap.values().stream().mapToDouble(Integer::intValue).sum()/processId.size()));
    }


    // The method by which all process operations take place
    public static void processingMethod(){
        processMap();
        addProcessFirst();
        for (Integer integer : processId) {
            returnTimeMap.put(integer, 0);
        }
        while (true) {
            for (int i = 0; i < processQueue.size(); i++) {
                boolean idle = true;
                process = processQueue.get(0);
                // If the return time of the current process is greater than the current time, it cannot run.
                if(currentTime < returnTimeMap.get(process.processId)){
                    for (int j = 0; j < processQueue.size(); j++) {
                        // If there is a process that can run in the queue,
                        // whose return time is less than or equal to the present time, give the queue to it.
                        if(processQueue.get(j).returnTime <= currentTime){
                            process = processQueue.get(j);
                            processQueue.remove(process);
                            idle = false;
                            break;
                        }
                    }
                    // Run an idle process if there is no process available
                    if(idle){
                        idleProcess();
                    }
                }

                // Make the necessary assignments to the process.
                process.currentTime = currentTime;
                currentTime = process.currentTime + process.cpuBurst;
                process.returnTime = process.currentTime + process.ioBurst + process.cpuBurst;
                returnTimeMap.put(process.processId, process.returnTime);

                //System.out.println("Process " + process.processId + " is running at " + process.currentTime + " and returns at " + process.returnTime);

                    // Remove the operand process from the map and queue and add its new tuple.
                    int index = processId.indexOf(process.processId);
                    List<Integer> list = map.get(processId.get(index));
                    processQueue.remove(process);

                    // Continue until tuples finish
                    if(list.size() >= 2) {
                        list.remove(0);
                        list.remove(0);
                        map.put(processId.get(index), list);
                        addProcessQueue(index);

                    }

                // If there are no processes left to process, exit the loop.
                if(processQueue.size()==0){
                    calculateTurnAroundTime();
                    calculateWaitingTime();
                    System.out.println("Idle process count: " + idleProcess);
                    System.out.println("HALT");
                    return;
                }
            }

        }
    }

    // I find the integer numbers in the data I read from the file and put them in the integer array list.
    public static ArrayList<Integer> convertInteger(String testdata) {
        ArrayList<Integer> arrayInt = new ArrayList<>();
        try (Scanner readFile = new Scanner(testdata)) {
            Pattern digitsPattern = Pattern.compile("(\\d+)");
            while (readFile.hasNextLine()) {
                Matcher m = digitsPattern.matcher(readFile.nextLine());
                while (m.find())
                    arrayInt.add(Integer.valueOf(m.group(1)));
            }
        }
        return arrayInt;
    }

    // (I map the data I put into the map as tuples.) Map içerisine attığım verileri tuple olarak birbirine eşliyorum
    public static void processMap(){
         addHashMap();
         processMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            List<Map.Entry<Integer, Integer>> pairs = new ArrayList<>();
            for (int i = 0; i < value.size() - 1; i += 2) {
                pairs.add(new AbstractMap.SimpleEntry<>(value.get(i), value.get(i + 1)));
            }
            processMap.put(key, pairs);
        }
    }

    // Reading from the file is done according to the given path.
    public static void readIdAndBurstTimes(String path) {
        try (BufferedReader okuyucu = new BufferedReader
                (new FileReader(path))) {
            String text;
            while ((text = okuyucu.readLine()) != null) {
                split = text.split(":");
                split2 = split[1].split(" (.*)");
                processId.add(Integer.valueOf(split[0]));
                processTimes.addAll(convertInteger(split2[0]));
                int IoLastBurst = processTimes.get(processTimes.size()-1) * -1;
                processTimes.set(processTimes.size()-1, IoLastBurst);
            }
        } catch (Exception e) {
            System.out.println("File is not found");
        }
    }

    // I'm putting the data I read from the file into the map according to the id value
    public static void addHashMap(){
        int i = 0;
        for (Integer processTime : processTimes) {
            if (i >= processId.size()) {
                return;
            }
            // (With this I understand if it comes to an end for the current key value)
            // Bu if ile o anki key değeri için sona geldiğini anlıyorum
            if (processTime != -1) {
                processTimesList.add(processTime);
            } else {
                i++;
                processTimesList.add(-1);
                map.put(processId.get(i - 1), processTimesList);
                processTimesList = new ArrayList<>();
            }
        }
    }


    // The process class I defined to make operations easier
    static class Process {
        private int cpuBurst;

        private int ioBurst;
        private int processId;

        private int currentTime;
        private int returnTime;


        public int getIoBurst() {
            return ioBurst;
        }

        public void setIoBurst(int ioBurst) {
            this.ioBurst = ioBurst;
        }

        public int getCpuBurst() {
            return cpuBurst;
        }

        public void setCpuBurst(int cpuBurst) {
            this.cpuBurst = cpuBurst;
        }

        public int getProcessId() {
            return processId;
        }

        public void setProcessId(int processId) {
            this.processId = processId;
        }

        public int getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(int currentTime) {
            this.currentTime = currentTime;
        }

        public int getReturnTime() {
            return returnTime;
        }

        public void setReturnTime(int returnTime) {
            this.returnTime = returnTime;
        }

        @Override
        public String toString() {
            return "Burst{" +
                    "cpuBurst=" + cpuBurst +
                    ", ioBurst=" + ioBurst +
                    ", processId=" + processId +
                    ", currentTime=" + currentTime +
                    ", returnTime=" + returnTime +
                    '}';
        }

    }








}
