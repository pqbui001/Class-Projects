/*
 * University of Louisville
 * Spring 2014 - CECS 622
 * Project Title: Car Wash Queuing Model Simulation
 * Author: Phi Bui
 */

package carwashmodel.Class;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author Phi Bui
 */
public class SingleJobServerSystem extends SingleServerQueueSystem {

    //Declare variables
    private int TotalRunningTime;
    private int InterArrivalMin;
    private int InterArrivalMax;
    private int ServeMu;
    private int ServeSigma;

    //Declare atributes
    public StringBuilder strPrintResults;
    CustomerWaitingLine QueueLine;
    BuildResults resultsInfo;

    public SingleJobServerSystem() {
        QueueLine = new CustomerWaitingLine();
        eventProcessor = new JobEventProcessor(this);
        statistic = new JobStatistic();
        runner = new JobRunner(this);
        resultsInfo = new BuildResults(this);
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

    class Customer {

        int num; //num & i stands for the i'th customer
        int arriveTime;
        int serviceStartTime;

        Customer(int i, int arvTime) {
            num = i;
            arriveTime = arvTime;
        }

        void Arrive(int time) {
            arriveTime = time;
        }

        void BeginService(int time) {
            serviceStartTime = time;
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
        
        PriorityQueue<Customer> q; //Data of this list, heap is being used.

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

    class JobStatistic extends Statistic {

        int totalResponseTime = 0;
        int totalServeTime = 0;
        int totalWaitTime = 0;
        int nDepartureCustomer = 0;

        void Reset() {
            totalResponseTime = 0;
            totalServeTime = 0;
            totalWaitTime = 0;
            nDepartureCustomer = 0;
        }
    }

    class JobEvent extends Event {

        int customerNum;

        JobEvent() {
            super();
            customerNum = 0;
        }

        JobEvent(char tp, int tm, int cusNum) {
            super(tp, tm);
            customerNum = cusNum;
        }
    }

    class JobEventProcessor extends EventProcessor {

        JobEventProcessor(SingleJobServerSystem mysys) {
            super(mysys);
        }

        private int genA() {
            return (int) RandomGeneratorHelper.UniformDistribution(InterArrivalMin, InterArrivalMax);
        }

        private int genS() {
            //Generate service time s
            return (int) RandomGeneratorHelper.GaussDistribution(ServeMu, ServeSigma);
        }

        private void procArrival(Event event) {
            SingleJobServerSystem mySys = (SingleJobServerSystem) sys;
            Status status = sys.status;
            JobEvent evt = (JobEvent) event; //(A,t,i)
            int t = evt.time;
            int i = evt.customerNum;

            //put this customer into CustomerWaitingList
            Customer c = new Customer(i, t);
            mySys.QueueLine.Enqueue(c);

            if (status.ls > 0) {
                //If server is busy, place the customer in queue
                status.lq++;
            } else {
                //Serve this customer
                c.BeginService(t);
                status.ls = 1;
                //Generate service time s
                int s = genS();
                //Generate new departure event
                JobEvent depEvt = new JobEvent(EventType.DEPARTURE, t + s, i);
                sys.futureEventList.Enqueue(depEvt);
            }

            //Generate interarrival time a
            int a = genA();
            JobEvent arvEvt = new JobEvent(EventType.ARRIVAL, t + a, i + 1);
            sys.futureEventList.Enqueue(arvEvt);
        }

        private void processDeparture(Event event) {
            SingleJobServerSystem mySys = (SingleJobServerSystem) sys;
            Status status = sys.status;
            JobStatistic statistic = (JobStatistic) sys.statistic;
            JobEvent evt = (JobEvent) event; //(D,t,i)
            int t = evt.time;

            //Quit this customer from QueueList
            Customer depC = mySys.QueueLine.Dequeue(); //The departured customer

            if (status.lq > 0) {
                
                //A customer from queue go out queue, to server
                status.lq--;
                
                //Begin service the customer in queue
                Customer c = mySys.QueueLine.GetFirst();
                c.BeginService(t);
                
                //Generate service time s
                int s = genS();
                
                //Generate new departure event
                int i2 = c.num;
                JobEvent depEvt = new JobEvent(EventType.DEPARTURE, t + s, i2);
                sys.futureEventList.Enqueue(depEvt);
            } else {
                status.ls = 0;
            }
            
            //Collect statistic information
            statistic.totalResponseTime += t - depC.arriveTime;
            statistic.totalServeTime += t - depC.serviceStartTime;
            statistic.totalWaitTime += depC.serviceStartTime - depC.arriveTime;
            statistic.nDepartureCustomer++;
        }

        void ProcessEvent(Event event) {
            SingleJobServerSystem mysys = (SingleJobServerSystem) sys;
            if (event == null) {
                return;
            }
            switch (event.type) {
                case EventType.ARRIVAL:
                    procArrival(event);
                    mysys.resultsInfo.PrintStatus();
                    break;
                case EventType.DEPARTURE:
                    processDeparture(event);
                    mysys.resultsInfo.PrintStatus();
                    break;
            }
        }
    } 
    
    public class JobRunner extends Runner {

        JobRunner(SingleJobServerSystem mySystem) {
            super(mySystem);
        }

        void PrepareRun() {
            //Clock  LQ LS   QueueList    FEL         TResponsTm  TServeTm  Nd
            //  0    0  0              (A,C1,0)(E,50)   0          0         0
            sys.clock.set(0);
            sys.status.Reset();
            SingleJobServerSystem mysys = (SingleJobServerSystem) sys;
            mysys.QueueLine.Clear();
            sys.futureEventList.Clear();
            sys.futureEventList.Enqueue(new JobEvent(EventType.ARRIVAL, 0, 1));
            sys.futureEventList.Enqueue(new JobEvent(EventType.END, TotalRunningTime, 0));
            sys.statistic.Reset();
            
            //Print title
            strPrintResults = mysys.resultsInfo.PrintTitle();
            mysys.resultsInfo.PrintStatus();//print initial status
        }

        void PostRun() {
            //Show the final statistic result
            SingleJobServerSystem mysys = (SingleJobServerSystem) sys;
            strPrintResults.append("\n====================================================================================================");
            strPrintResults.append(mysys.resultsInfo.PrintFinalStatistic());
        }
    }

    //Show system status
    public class BuildResults {

        SingleJobServerSystem sys;

        BuildResults(SingleJobServerSystem mysys) {
            sys = mysys;
        }

        public StringBuilder PrintTitle() {
            StringBuilder str = new StringBuilder("Case 1: Single-server queuing model simulation results");
            str.append("\n====================================================================================================");
            str.append("\nClock     LQ     LS     | \t Queue List    \t       || \t   Future Event List    \t         ||| \t R    S    W    Nd");
            str.append("\n____________________________________________________________________________________________________");
            return str;
        }

        // Print system current status
        void PrintStatus() {

            strPrintResults.append(String.format("\n   %2d        %2d         %d     ", sys.clock.t, sys.status.lq, sys.status.ls));
            strPrintResults.append(" | ");

            PrintQueueList();

            strPrintResults.append("       ||      ");

            PrintFutureEventList();

            strPrintResults.append("    ||| ");

            PrintStatistic();

        }

        void PrintQueueList() {

            int n = sys.QueueLine.Size();
            Customer[] arr = sys.QueueLine.ToArray();
            Arrays.sort(arr, new CustomerComperator());
            n = arr.length;

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) {
                Customer c = arr[i];
                String tmp = String.format("(C%d,%d,%d)",
                        c.num, c.arriveTime, c.serviceStartTime);
                s.append(tmp);
            }

            strPrintResults.append(String.format(" %24s \t", s));
        }

        //Print future event list
        void PrintFutureEventList() {
            Event[] arr = sys.futureEventList.ToArray();
            Arrays.sort(arr, new EventComperator());
            int n = arr.length;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; i++) {
                JobEvent e = (JobEvent) arr[i];
                String tmp = String.format("(%c,C%d,%d)",
                        e.type, e.customerNum, e.time);
                s.append(tmp);
            }

            strPrintResults.append(String.format("%30s \t     ", s));
        }

        void PrintStatistic() {
            JobStatistic statistic = (JobStatistic) sys.statistic;
            strPrintResults.append(String.format("\t %2d   %2d   %2d   %2d",
                    statistic.totalResponseTime,
                    statistic.totalServeTime,
                    statistic.totalWaitTime,
                    statistic.nDepartureCustomer));
        }

        public StringBuilder PrintFinalStatistic() {
            JobStatistic statistic = (JobStatistic) sys.statistic;
            int nd = statistic.nDepartureCustomer;
            double throughput = nd / (double) sys.clock.t;
            double avgResponseTime = statistic.totalResponseTime / (double)nd;
            double avgWaitTime = statistic.totalWaitTime / (double) nd;
            double utilization = statistic.totalServeTime / (double) sys.clock.t;

            StringBuilder s = new StringBuilder();
            s.append(String.format("\n\nThroughput: %f\n", throughput));
            s.append(String.format("Average Response Time: %f\n", avgResponseTime));
            s.append(String.format("Average Wait Time: %f\n", avgWaitTime));
            s.append(String.format("Server Utilization: %f\n", utilization));

            return s;
        }

    }

}
