package com.leanovate.helios;

import javax.baja.alarm.*;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import org.json.JSONException;
import org.json.JSONObject;

import javax.baja.serial.BSerialHelper;
import javax.baja.serial.BISerialPort;
import javax.baja.sys.Property;
import javax.baja.sys.Context;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by e283233 on 10/4/2017.
 */
@NiagaraType
public class BMyRecipient extends BAlarmRecipient {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.abc.test1.BMyRecipient(2979906276)1.0$ @*/
/* Generated Wed Oct 04 15:12:14 IST 2017 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }

  public static final Type TYPE = Sys.loadType(BMyRecipient.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

  // constants
  private static final int MAX_WORKER_THREADS = 50;
  private static final int ERROR = -1;


  // public attributes
  public ThreadDataShare[] threadDataShare;
  public  boolean monitorThreadStarted;
  public  int runningThreads;
  private int alarmEventsHandled;
  private int getThreadErrors;
  
  public BMyRecipient()
  {
    System.out.println("LeanovateHeliosRx: Creating LeanovateAlarmRecipient Datastore...");
    threadDataShare = new ThreadDataShare[MAX_WORKER_THREADS];
    
    for (int i=0; i < MAX_WORKER_THREADS; i++)
    {
      threadDataShare[i] = new ThreadDataShare();
    }

    System.out.print("LeanovateHeliosRx: Starting Monitor thread...");
    
    Runnable r = new MonitorThread(MAX_WORKER_THREADS, threadDataShare);
    //Runnable r = new GpsComPortAccess();
    Thread child = new Thread(r);
    child.setDaemon(true);
    child.start();
    System.out.println("OK");
    
    monitorThreadStarted = true;
    
    runningThreads = 0;
    alarmEventsHandled = 0;
    getThreadErrors = 0;
    
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
  
  
  @Override
  public void handleAlarm(BAlarmRecord bAlarmRecord) {
    
    alarmEventsHandled++;
    System.out.println("\n***LeanovateHandleAlarm: " 
        + " EVNTS: " + alarmEventsHandled + " THREAD-ERRS: " + getThreadErrors 
        + " AlmRec:" + bAlarmRecord); 
    
    int childId = getFreeWorkerThreadId();
    if (childId != ERROR)
    {             
      //System.out.print("LeanovateHeliosRx: Starting child thread with ID: " + childId + "...");
      Runnable r = new BLeanovateAlarmConsumer(childId, this.threadDataShare[childId], bAlarmRecord);
      Thread child = new Thread(r);
      child.setDaemon(true);
      child.start();    
      this.threadDataShare[childId].threadInUse = true; // TODO: add set method
      //System.out.println("OK");
    }
    else
    {
      System.out.println("LeanovateHeliosRx: HndlAlm: No more free worker threads. All busy!. Unexpected error!! Check system performance design");
      getThreadErrors++;
    }

  }
  
}

//wakes up once X secs. runs through threaddatashare for abnormal stuff like errors. 
//tries or initiates retries or logging where required. TODO: if some thread stuck, see what to do?
class MonitorThread  implements Runnable
{
  private int maxThreads;
  private ThreadDataShare[] threadDataShare;
  private int maxThreadsUsedSoFar;
  
  public MonitorThread(int maxThreads, ThreadDataShare[] threadDataShare)
  {
   this.maxThreads = maxThreads;
   this.threadDataShare = threadDataShare;
   maxThreadsUsedSoFar = 0;
  }
  
  public void run()
  {
   try
   {
       while(true)
       {
         int activeThreads = 0;
         // check threadDataShare array for abnormal stuff and act
         //System.out.println("LeanovateHeliosRx: Monitor: Wokeup: Checking Threads...");
         
         for (int i=0; i < maxThreads; i++)
         {
           if (threadDataShare[i].threadInUse == true)
           {
             activeThreads++;
/*             System.out.println("LeanovateHeliosRx: Monitor: threadID: " + i + " In Use" 
               + " Finished: " + threadDataShare[i].threadFinished
             + " Alarm Object" + threadDataShare[i].alarmObject
             + " NetworkError" + threadDataShare[i].networkError
             + " ServerError" + threadDataShare[i].serverError);
*/              
/*             if (threadDataShare[i].networkError == true)
                 System.out.println("LeanovateHeliosRx: Monitor: threadID: " + i + " Network Error Detected!"); 
  
             if (threadDataShare[i].serverError == true)
               System.out.println("LeanovateHeliosRx: Monitor: threadID: " + i + " Server Error Detected!");
*/
           }
           
           // child finished work. cleanup datastore
           if (threadDataShare[i].threadFinished == true)
           {
             //System.out.println("LeanovateHeliosRx: Monitor: threadID: " + i + " Child finished job. Freeing up worker thread and data share object");
             threadDataShare[i].threadInUse = false;
             threadDataShare[i].alarmObject = null; 
             threadDataShare[i].networkError = threadDataShare[i].serverError = false;
             threadDataShare[i].threadFinished = false; 
             threadDataShare[i].threadId = -1;
             activeThreads--;
           }
         }
         if (activeThreads > maxThreadsUsedSoFar)
           maxThreadsUsedSoFar = activeThreads;

         SimpleDateFormat dateTimeNow = new SimpleDateFormat("HH:mm:ss dd-MMM-yy");
         String timeStamp = dateTimeNow.format(new Date());
         
         System.out.println("[ "+ timeStamp + " ]" + "LeanovateHeliosRx: Monitor: Total Active Threads: " 
                 + activeThreads + " Total Free Threads: " 
                 + (maxThreads - activeThreads)
                 + " max threads used till now: " + maxThreadsUsedSoFar);
         
         Thread.sleep(60000); // wake up and check every X secs
       }
   }
   catch (InterruptedException e)
   {
     System.out.println("LeanovateHeliosRx: InterruptedException");
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
  public BAlarmRecord alarmObject;
 
  public ThreadDataShare(int threadId , BAlarmRecord alarmObject)
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

class GpsComPortAccess implements Runnable
{
   public void run()
   {
     try
     {
       BSerialHelper serHelper = new BSerialHelper();
       serHelper.setPortName("COM3");
       BISerialPort sp = serHelper.open("admin");
     }
     catch (Exception e)
     {
       System.out.println("Exception!");
     }
   }
}
