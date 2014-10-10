/*
 * University of Louisville
 * Spring 2014 - CECS 622
 * Project Title: Car Wash Queuing Model Simulation
 * Author: Phi Bui
 */

package carwashmodel.Class;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * @author Phi Bui
 */
public class MultiServerSingleQueueSystem {

    //Parameters -------------
    private int TotalRunningTime;
    private int InterArrivalMin;
    private int InterArrivalMax;
    private int ServeMu;
    private int ServeSigma;
    private int TotalServer;//2 Servers

    public StringBuilder strPrintResults;

    /* attributes */
    Status status;
    Clock clock;
    Statistic statistic;
    FutureEventList futureEventList;
    EventProcessor eventProcessor;
    public Runner runner;
    BuildResults resultsInfo;
    Server[] servers;
    CarWashSystemQueue queue;

    public void SetParameters(int totalRunningTime, int interArrivalMin, int interArrivalMax, int serveMu, int serveSigma, int totalServer) {
        this.TotalRunningTime = totalRunningTime;
        this.InterArrivalMin = interArrivalMin;
        this.InterArrivalMax = interArrivalMax;
        this.ServeMu = serveMu;
        this.ServeSigma = serveSigma;
        this.TotalServer = totalServer;
    }

    public MultiServerSingleQueueSystem(int totalRunningTime, int interArrivalMin, int interArrivalMax, int serveMu, int serveSigma, int totalServer) {
        SetParameters(totalRunningTime, interArrivalMin, interArrivalMax, serveMu, serveSigma, totalServer);
        status = new Status(this);
        clock = new Clock();
        statistic = new Statistic(this);
        futureEventList = new FutureEventList();
        eventProcessor = new EventProcessor(this);
        runner = new Runner(this);
        resultsInfo = new BuildResults(this);

        servers = new Server[TotalServer + 1];
        for (int i = 1; i <= TotalServer; i++) {
            servers[i] = new Server();
        }
        queue = new CarWashSystemQueue();
    }

    public void SetTotalRunningTime(int totalRunningTime) {
        this.TotalRunningTime = totalRunningTime;
    }

    public void SetInterArrivalMin(int interArrivalMin) {
        this.InterArrivalMin = interArrivalMin;
    }

    public void SetInterArrivalMax(int interArrivalMax) {
        this.InterArrivalMax = interArrivalMax;
    }

    public void SetServeMu(int serveMu) {
        this.ServeMu = serveMu;
    }

    public void SetServeSigma(int serveSigma) {
        this.ServeSigma = serveSigma;
    }

    public void SetTotalServer(int totalServer) {
        this.TotalServer = totalServer;
    }

    class Status {

        MultiServerSingleQueueSystem sys;
        int lq; //number of customer in queue
        int[] ls; //number of customer in server

        Status(MultiServerSingleQueueSystem msys) {
            sys = msys;
            lq = 0;
            ls = new int[sys.TotalServer + 1];
        }

        void Reset() {
            lq = 0;
            ls = new int[sys.TotalServer + 1];
            for (int i = 1; i <= sys.TotalServer; i++) {
                ls[i] = 0;
            }
        }
    }

    class Clock {

        int t = 0;

        void set(int i) {
            t = i;
        }
    }

    class EventType {
        public static final char END = 'E'; //End of simulation
        public static final char ARRIVAL = 'A'; //Customer Arrival
        public static final char DEPARTURE = 'D'; //Customer Departure
        public static final char NULL = 'N'; //Undefined event
    }

    class Event {

        char type;
        int time;
        int customerNum;
        int serverNum;

        Event() {
            type = EventType.NULL;
            time = 0;
            customerNum = 0;
            serverNum = 0;
        }

        Event(char tp, int tm) {
            type = tp;
            time = tm;
        }

        Event(char tp, int tm, int cusNum) {
            type = tp;
            time = tm;
            customerNum = cusNum;
        }

        Event(char tp, int tm, int cusNum, int serNum) {
            type = tp;
            time = tm;
            customerNum = cusNum;
            serverNum = serNum;
        }
    }

    class EventComperator implements Comparator<Event> {

        public int compare(Event o1, Event o2) {
            if (o1.time < o2.time) {
                return -1;
            }
            if (o1.time > o2.time) {
                return 1;
            }
            return 0;
        }
    }

    class FutureEventList {

        PriorityQueue<Event> q; //Data of this list, heap is being used.

        FutureEventList() {
            q = new PriorityQueue<Event>(10, new EventComperator());
        }

        void Clear() {
            q.clear();
        }

        void Enqueue(Event event) {
            q.add(event);
        }

        Event Dequeue() {
            return q.poll();
        }

        Event[] ToArray() {
            return q.toArray(new Event[0]);
        }

        int Size() {
            return q.size();
        }
    }

    class Customer {

        int num; //num i stands for the i'th customer
        int arriveTime;

        Customer(int i, int arvTime) {
            num = i;
            arriveTime = arvTime;
        }

        void Arrive(int time) {
            arriveTime = time;
        }
    }

    class CustomerComperator implements Comparator<Customer> {

        public int compare(Customer o1, Customer o2) {
            if (o1.num < o2.num) {
                return -1;
            }
            if (o1.num > o2.num) {
                return 1;
            }
            return 0;
        }
    }

    class CustomerWaitingLine {
       
        PriorityQueue<Customer> q; //Data of this list, use heap is being used.

        CustomerWaitingLine() {
            q = new PriorityQueue<Customer>(10, new CustomerComperator());
        }

        void Clear() {
            q.clear();
        }

        void Enqueue(Customer c) {
            q.add(c);
        }

        Customer Dequeue() {
            return q.poll();
        }

        Customer GetFirst() {
            return q.peek();
        }

        Customer[] ToArray() {
            return q.toArray(new Customer[0]);
        }

        int Size() {
            return q.size();
        }
    }

    class Statistic {

        MultiServerSingleQueueSystem sys;
        int totalResponseTime = 0;
        int[] totalServeTime;
        int totalWaitTime = 0;
        int nDepartureCustomer = 0;
        int[] serveStartTime;

        Statistic(MultiServerSingleQueueSystem msys) {
            sys = msys;
            totalResponseTime = 0;
            totalServeTime = new int[sys.TotalServer + 1];
            totalWaitTime = 0;
            nDepartureCustomer = 0;
            serveStartTime = new int[sys.TotalServer + 1];
        }

        void Reset() {
            totalResponseTime = 0;
            totalWaitTime = 0;
            nDepartureCustomer = 0;
            totalServeTime = new int[sys.TotalServer + 1];
            serveStartTime = new int[sys.TotalServer + 1];
            
            for (int i = 1; i <= sys.TotalServer; i++) {
                totalServeTime[i] = 0;
                serveStartTime[i] = 0;
            }
        }
    }

    class EventProcessor {

        MultiServerSingleQueueSystem sys;

        EventProcessor(MultiServerSingleQueueSystem mySystem) {
            sys = mySystem;
        }

        private int genA() {
            return (int) RandomGeneratorHelper.UniformDistribution(sys.InterArrivalMin, sys.InterArrivalMax);
        }

        private int genS() {
            //gen service time s
            return (int) RandomGeneratorHelper.GaussDistribution(sys.ServeMu, sys.ServeSigma);
        }

        private void processArrival(Event event) {
            Status status = sys.status;
            Statistic statistic = sys.statistic;
            Event evt = event; //(A,t,i)
            int t = evt.time;
            int i = evt.customerNum;

            //Place this customer into queue
            Customer c = new Customer(i, t);

            int k = sys.TotalServer;
            int j = 1;
            for (j = 1; j <= k; j++) {
                if (status.ls[j] == 0) {
                    break;
                }
            }

            if (j > k) {
                //If every server is busy, place the customer in queue
                status.lq++;
                sys.queue.Enqueue(c);
            } else {
                //Serve this customer

                sys.servers[j].Serve(c);
                status.ls[j] = 1;
                statistic.serveStartTime[j] = t;
                
                //gen service time s
                int s = genS();
                
                //gen new departure event
                Event depEvt = new Event(EventType.DEPARTURE, t + s, i, j);
                sys.futureEventList.Enqueue(depEvt);
            }

            //gen interarrival time a
            int a = genA();
            Event arvEvt = new Event(EventType.ARRIVAL, t + a, i + 1);
            sys.futureEventList.Enqueue(arvEvt);
        }

        private void processDepature(Event event) {
            Status status = sys.status;
            Statistic statistic = sys.statistic;
            Event evt = event; //(D,t,i,j)
            int j = evt.serverNum;
            int t = evt.time;

            //quit this customer from the QueueLine
            Customer depC = sys.servers[j].Departure(); //the departured customer

            if (status.lq > 0) {
                // a customer from queue go out queue, to server
                status.lq--;
                Customer c = sys.queue.Dequeue();
                //begin service the customer in queue
                sys.servers[j].Serve(c);
                statistic.serveStartTime[j] = t;
                
                //gen service time s
                int s = genS();
                
                //gen new departure event
                int i2 = c.num;
                Event depEvt = new Event(EventType.DEPARTURE, t + s, i2, j);
                sys.futureEventList.Enqueue(depEvt);
            } else {
                status.ls[j] = 0;
            }
            //collect statistic information
            statistic.totalResponseTime += t - depC.arriveTime;
            statistic.totalServeTime[j] += t - statistic.serveStartTime[j];
            statistic.totalWaitTime += statistic.serveStartTime[j] - depC.arriveTime;
            statistic.nDepartureCustomer++;
        }

        void ProcessEvent(Event event) {
            if (event == null) {
                return;
            }
            switch (event.type) {
                case EventType.ARRIVAL:
                    processArrival(event);
                    sys.resultsInfo.PrintStatus();
                    break;
                case EventType.DEPARTURE:
                    processDepature(event);
                    sys.resultsInfo.PrintStatus();
                    break;
            }
        }
    }

    public class Runner {

        public MultiServerSingleQueueSystem sys;

        public Runner(MultiServerSingleQueueSystem mySystem) {
            sys = mySystem;
        }

        //the initial procedure before each running
        void PrepareRun() {
            //Clock  LQ LS   QueueList    FEL         TResponsTm  TServeTm  Nd
            //  0    0  0             (A,C1,0)(E,50)   0          0         0
            sys.clock.set(0);
            sys.status.Reset();
            sys.queue.Reset();
            for (int i = 1; i <= sys.TotalServer; i++) {
                sys.servers[i].Reset();
            }
            sys.futureEventList.Clear();
            sys.futureEventList.Enqueue(new Event(EventType.ARRIVAL, 0, 1));
            sys.futureEventList.Enqueue(new Event(EventType.END, sys.TotalRunningTime, 0));
            sys.statistic.Reset();

            //Print title
            strPrintResults = sys.resultsInfo.PrintTitle();
            sys.resultsInfo.PrintStatus();//print initial status
        }

        void MainLoop() {
            Event evt = null;
            evt = sys.futureEventList.Dequeue();
            if (evt == null) {
                return;
            }
            while (evt.type != EventType.END) {
                sys.clock.set(evt.time);
                sys.eventProcessor.ProcessEvent(evt);
                evt = sys.futureEventList.Dequeue();
                if (evt == null) {
                    return;
                }
            }
        }

        //this routine is call after finish each run
        void PostRun() {
            strPrintResults.append("\n===============================================================================================================");
            //show the final statistic result
            strPrintResults.append(sys.resultsInfo.PrintFinalStatistic());
        }

        public void Run() {
            PrepareRun();
            MainLoop();
            PostRun();
        }
    }

    //Show Car Wash Queuing Model Status
    public class BuildResults {

        MultiServerSingleQueueSystem sys;

        BuildResults(MultiServerSingleQueueSystem mysys) {
            sys = mysys;
        }

        public StringBuilder PrintTitle() {
            StringBuilder str = new StringBuilder("Case 2: Multi-server single queuing model simulation results");
            str.append("\n===============================================================================================================");
            str.append("\nClock   LQ ");

            int k = sys.TotalServer;
            for (int i = 1; i <= k; i++) {
                str.append(String.format("   LS%d   ", i));
            }

            str.append("|");
            for (int i = 1; i <= k; i++) {
                str.append(String.format("   SL%d  \t   || ", i));
            }
            str.append("    Queue   ");
            str.append(" |||      \t    Future Event List    \t     ||||  R  ");
            str.append("W Nd");

           
            for (int i = 1; i <= k; i++) {
                str.append(String.format(" S%d", i));
            }
            
            str.append("\n______________________________________________________________________________________________________________");
            str.append("\n");
            return str;
        }

        // Print system current status
        void PrintStatus() {
            strPrintResults.append(String.format("%2d    ", sys.clock.t));
            strPrintResults.append(String.format("     %2d  ", sys.status.lq));

            int k = sys.TotalServer;
            for (int i = 1; i <= k; i++) {
                strPrintResults.append(String.format("    %3d    ", sys.status.ls[i]));
            }

            strPrintResults.append("  |   ");
            PrintServers();
            PrintQueue();

            strPrintResults.append("      |||   ");
            PrintFutureEventList();

            strPrintResults.append("\t     |||| ");
            PrintStatistic();

            strPrintResults.append("\n");
        }

        void PrintQueue() {
            int n = sys.queue.Size();
            Customer[] arr = sys.queue.ToArray();
            n = arr.length;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) {
                Customer c = arr[i];
                String tmp = String.format("(C%d,%d)",
                        c.num, c.arriveTime);
                s.append(tmp);
            }
            strPrintResults.append(String.format("%8s     ", s));
        }

        void PrintServer(int j) {
            Customer c = sys.servers[j].GetCustomer();
            String s = "";
            if (c != null) {
                s = String.format("(C%d,%d)",
                        c.num, c.arriveTime);
            }

            strPrintResults.append(String.format("  %8s \t ", s));
        }

        void PrintServers() {
            for (int j = 1; j <= sys.TotalServer; j++) {
                PrintServer(j);

                strPrintResults.append("  ||  ");
            }
        }

        //Print future event list
        void PrintFutureEventList() {
            Event[] arr = sys.futureEventList.ToArray();
            Arrays.sort(arr, new EventComperator());
            int n = arr.length;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) {
                Event e = arr[i];
                String tmp = String.format("(%c,C%d,%d,%d)",
                        e.type, e.customerNum, e.time, e.serverNum);
                s.append(tmp);
            }
            strPrintResults.append(String.format("%47s", s));
        }

        void PrintStatistic() {
            Statistic statistic = sys.statistic;

            strPrintResults.append(String.format("%2d %2d %2d",
                    statistic.totalResponseTime,
                    statistic.totalWaitTime,
                    statistic.nDepartureCustomer));

            for (int i = 1; i <= sys.TotalServer; i++) {
                strPrintResults.append(String.format("%3d", statistic.totalServeTime[i]));
            }

        }

        public StringBuilder PrintFinalStatistic() {
            Statistic statistic = sys.statistic;
            int nd = statistic.nDepartureCustomer;
            double throughput = nd / (double) sys.clock.t;
            double avgResponseTime = statistic.totalResponseTime / (double) nd;
            double avgWaitTime = statistic.totalWaitTime / (double) nd;

            int k = sys.TotalServer;
            double[] utilization = new double[k + 1];

            StringBuilder s = new StringBuilder();
            s.append(String.format("\n\nThroughput: %f\n", throughput));
            s.append(String.format("Average Response Time: %f\n", avgResponseTime));
            s.append(String.format("Average Wait Time: %f\n", avgWaitTime));

            for (int i = 1; i <= k; i++) {
                utilization[i] = statistic.totalServeTime[i] / (double) sys.clock.t;
                s.append(String.format("Server Utilization (%d): %f\n", i, utilization[i]));
            }

            return s;
        }
    }

    class Server {

        Customer customer;

        void Serve(Customer c) {
            customer = c;
        }

        Customer Departure() {
            Customer c = customer;
            customer = null;
            return c;
        }

        Customer GetCustomer() {
            return customer;
        }

        //The number of customers in the Server
        int Ls() {
            if (customer != null) {
                return 1;
            }
            return 0;
        }

        void Reset() {
            customer = null;
        }
    }

    //Queue
    class CarWashSystemQueue {

        LinkedList<Customer> q;

        CarWashSystemQueue() {
            q = new LinkedList<Customer>();
        }

        int Size() {
            return q.size();
        }

        Customer[] ToArray() {
            return q.toArray(new Customer[0]);
        }

        void Enqueue(Customer c) {
            q.add(c);
        }

        Customer Dequeue() {
            return q.poll();
        }

        //The number of customers in the Server
        int Lq() {
            return q.size();
        }

        void Reset() {
            q.clear();
        }
    }

}
