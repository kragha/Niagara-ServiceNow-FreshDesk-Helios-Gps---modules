package webapi;

public class AlarmConsumer implements Runnable
{
  private int childId;
  private ThreadDataShare threadDataShare;
  
  public AlarmConsumer(int childId, ThreadDataShare threadDataShare, AlarmObject alarmObject)
  {
    this.childId = childId;
    this.threadDataShare = threadDataShare;
    this.threadDataShare.alarmObject = alarmObject;
  }
 
  public void run()
  {
    try
    {
            System.out.println("Child with ID: " + this.childId + " starts work now...");
            Thread.sleep(5000);
            System.out.println("Child with ID: " + this.childId + " completed work now...");
    }
    catch (InterruptedException e)
    {
      System.out.println("InterruptedException" + " child id: " + this.childId);
      threadDataShare.threadFinished = true;
    }
    threadDataShare.threadFinished = true;
  }
}
