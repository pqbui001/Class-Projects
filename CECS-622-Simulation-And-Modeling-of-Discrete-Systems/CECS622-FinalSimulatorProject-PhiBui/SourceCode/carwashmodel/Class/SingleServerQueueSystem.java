/*
 * University of Louisville
 * Spring 2014 - CECS 622
 * Project Title: Car Wash Queuing Model Simulation
 * Author: Phi Bui
 */
package carwashmodel.Class;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author Phi Bui
 */
public class SingleServerQueueSystem {

    //Declare attributes 
    Status status;
    Clock clock;
    Statistic statistic;
    FutureEventList futureEventList;
    EventProcessor eventProcessor;
    public Runner runner;

    public SingleServerQueueSystem() {
        status = new Status();
        clock = new Clock();
        statistic = new Statistic();
        futureEventList = new FutureEventList();
        eventProcessor = new EventProcessor(this);
        runner = new Runner(this);
    }

    class Status {

        int lq = 0; //Number of customer in queue
        int ls = 0; //Number of customer in server

        void Reset() {
            lq = 0;
            ls = 0;
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

        Event() {
            type = EventType.NULL;
            time = 0;
        }

        Event(char tp, int tm) {
            type = tp;
            time = tm;
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

        PriorityQueue<Event> q; //Data of this list, use heap

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
    class Statistic {
        void Reset() {}
    } //END Statistic class
    

    class EventProcessor {

        SingleServerQueueSystem sys;

        EventProcessor(SingleServerQueueSystem mySystem) {
            sys = mySystem;
        }

        void ProcessEvent(Event event) {
        }
    } 
    public class Runner {

        SingleServerQueueSystem sys;

        public Runner(SingleServerQueueSystem mySystem) {
            sys = mySystem;
        }

        //The initial procedure before each running
        void PrepareRun() {
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

        //This routine is call after finish each run
        void PostRun() {
        }

        public void Run() {
            PrepareRun();
            MainLoop();
            PostRun();
        }
    } 
}
