package webapi;

public class AlarmConsumer implements Runnable
{
  private int childId;
  
  public AlarmConsumer(int childId)
  {
    this.childId = childId;
  }
 
  public void run()
  {
    try
    {
        while(true)
        {
            System.out.println("Child with ID: " + this.childId + " starts work now...");
            Thread.sleep(5000);
            System.out.println("Child with ID: " + this.childId + " completed work now...");
        }
    }
    catch (InterruptedException e)
    {
      System.out.println("InterruptedException" + " child id: " + this.childId);
    }
  }
}
