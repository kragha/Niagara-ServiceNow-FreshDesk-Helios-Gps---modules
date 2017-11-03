package webapi;

import java.io.IOException;
import java.util.Scanner;

public class HeliosHttpUrlConnection 
{
  private static final int ERROR = -1;
  private static final int TKT_STATUS_RESOLVED = 4;
  private static final int MAX_WORKER_THREADS = 5;
  
  // public attributes
  public ThreadDataShare[] threadDataShare;
  public  boolean monitorThreadStarted;
  public  int runningThreads;
  
  public HeliosHttpUrlConnection()
  {
    System.out.println("Creating Thread Data Share...");
    threadDataShare = new ThreadDataShare[MAX_WORKER_THREADS];
    
    for (int i=0; i < MAX_WORKER_THREADS; i++)
    {
      threadDataShare[i] = new ThreadDataShare();
    }

/*    System.out.print("Starting Monitor thread...");
    
    Runnable r = new MonitorThread(MAX_WORKER_THREADS, threadDataShare);
    Thread child = new Thread(r);
    child.setDaemon(true);
    child.start();
    System.out.println("OK");
    
    monitorThreadStarted = true;
    
    runningThreads = 0;
    */
  }
  
  public int getFreeWorkerThreadId()
  {
    for (int i=0; i < MAX_WORKER_THREADS; i++)
    {
      if (threadDataShare[i].threadInUse == false)
        return i;
    }
    return ERROR; // all threads busy now
  }
  
  // public methods...
/* START main */  
  public static void main(String[] args) throws IOException
  {
    HeliosTicketHandler ticketHandler = new HeliosTicketHandler();

    Scanner input = new Scanner(System.in);
    
    HeliosHttpUrlConnection leanovateObj = new HeliosHttpUrlConnection();
    
    while(true)
    {
      System.out.println("\nPress 0 for childSpawn, 1 for GET, 2 for POST, 3 for SEARCH-TKT, " +
              "4 for updating status to RESOLVED, 5 for Exit \n");
      int option = input.nextInt();

      System.out.println("option is: " + option);
      
      if (option == 0)
      {
        int childId = leanovateObj.getFreeWorkerThreadId();
        if (childId != ERROR)
        {            
          System.out.print("Starting child thread with ID: " + childId + "...");
          
          AlarmObject alarm = new AlarmObject("point1", 0 , 2);
          
          Runnable r = new AlarmConsumer(childId, leanovateObj.threadDataShare[childId], alarm);
          Thread child = new Thread(r);
          child.setDaemon(true);
          child.start();
          leanovateObj.threadDataShare[childId].threadInUse = true; // TODO: add set method
          System.out.println("OK");
        }
        else
          System.out.println("No more free worker threads. All busy!. Unexpected error!! Check system performance design");
      }
      else if (option == 1)
      {
        ticketHandler.getAllTickets();
        System.out.println("GET DONE");
      }
      else if (option == 2)
      {
        ticketHandler.createTicket("point1", 1);
        System.out.println("POST DONE");
      }
      else if (option == 3)
      {
        int id = ticketHandler.searchOpenTicket("point1");
        System.out.println("Search GET DONE\n" + "tktId:" + id);
      }
      else if (option == 4)
      {
          int id = ticketHandler.searchOpenTicket("point1");
          if (id != ERROR)
          {
            int ret = ticketHandler.updateTicket(id, "point1", TKT_STATUS_RESOLVED);
            System.out.println("UPDATE DONE.. " + "retcode:" + ret + 
                           "  status set to: " + TKT_STATUS_RESOLVED);
          }
          else
            System.out.println("search error!"); 
             
      }
      else if (option == 5)
      {
        System.out.println("Exiting...");
        break;
      }
    }
    input.close();
  }
/* END main */   

  
} // class urlconnection


// wakes up once X secs. runs through threaddatashare for abnormal stuff like errors. 
// tries or initiates retries or logging where required. TODO: if some thread stuck, see what to do?
class MonitorThread  implements Runnable
{
  private int maxThreads;
  private ThreadDataShare[] threadDataShare;
  
  public MonitorThread(int maxThreads, ThreadDataShare[] threadDataShare)
  {
    this.maxThreads = maxThreads;
    this.threadDataShare = threadDataShare;
  }
  
  
  public void run()
  {
    try
    {
        while(true)
        {
          int activeThreads = 0;
          // check threadDataShare array for abnormal stuff and act
          System.out.println("\nMonitor: Wokeup: Checking Threads for Errors to Rectify...");
          
          for (int i=0; i < maxThreads; i++)
          {
            if (threadDataShare[i].threadInUse == true)
            {
              activeThreads++;
              System.out.println("Monitor: threadID: " + i + " In Use");
              System.out.println("Monitor: threadID: " + i + " Finished: " + threadDataShare[i].threadFinished);
              System.out.println("Monitor: threadID: " + i + " Alarm Object" + threadDataShare[i].alarmObject);
               
              if (threadDataShare[i].networkError == true)
                  System.out.println("Monitor: threadID: " + i + " Network Error Detected!"); 

              if (threadDataShare[i].serverError == true)
                System.out.println("Monitor: threadID: " + i + " Server Error Detected!");
            }
            
            // child finished work. cleanup datastore
            if (threadDataShare[i].threadFinished == true)
            {
              System.out.println("Monitor: threadID: " + i + " Child finished job. Freeing up worker thread and data share object");
              threadDataShare[i].threadInUse = false;
              threadDataShare[i].alarmObject = null; 
              threadDataShare[i].networkError = threadDataShare[i].serverError = false;
              threadDataShare[i].threadFinished = false; 
              threadDataShare[i].threadId = -1;
              activeThreads--;
            }
          }
          System.out.println("Monitor: Total Active Threads: " + activeThreads);
          System.out.println("Monitor: Total Free Threads: " + (maxThreads - activeThreads) );
          
          Thread.sleep(10000); // wake up and check every X secs - 3 secs
        }
    }
    catch (InterruptedException e)
    {
      System.out.println("InterruptedException");
    }
  }
} //class monitor


class ThreadDataShare
{
  public boolean threadInUse;  // set by parent when worked thread is initiated. RO from child and others.
  public boolean threadFinished;  // set by child when work completes. RO from parent and other childs
  public int threadId; // 0-MAX_WORKERS ID assigned when worker thread got spawned. parent:W, others:RO
  public boolean serverError; // W:child, RO: others. child writes true if it encountered server error like wrong params, auth fail (but is reachable)
  public boolean networkError; // W:child, RO: others. child writes true if it encountered network error like not reachable
  public AlarmObject alarmObject;
  
  public ThreadDataShare(int threadId , AlarmObject alarmObject)
  {
    threadInUse = false;
    this.threadId = threadId;
    this.alarmObject = alarmObject;
    serverError = networkError = threadFinished = false;
  }

  public ThreadDataShare()
  {
    threadInUse = false;
    this.threadId = -1;
    this.alarmObject = null;
    serverError = networkError = threadFinished = false;
  }
}

class AlarmObject
{
  public AlarmObject(String name, int alarmStatus, int prty)
  {
    sourceName = name;
    sourceState = alarmStatus;
    priority = prty;
  }
  String sourceName;
  int sourceState;
  int priority;
}