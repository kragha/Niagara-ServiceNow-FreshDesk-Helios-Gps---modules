package webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class LeanovateHttpUrlConnection 
{

  private static final String USER_AGENT = "niagara-leanovate/1.0";
  private static final String USERNAME_PASSWD = "kragha2000@gmail.com:123surya";
  private static final String GET_URL = "https://ragkrish.freshdesk.com/api/v2/tickets";
  private static final String GET_URL2 = "https://ragkrish.freshdesk.com/api/v2/ticket_fields";
  private static final String GET_SEARCH_TKT_URL = "https://ragkrish.freshdesk.com/api/v2/search/tickets?query=";
  private static final String POST_URL = "https://ragkrish.freshdesk.com/api/v2/tickets";
  //private static final int INVALID_TICKET_ID = -1;
  private static final int OK = 0;
  private static final int ERROR = -1;
  private static final int RESOLVED = 4;
 
  //private static final String POST_PARAMS = "-d {"description":"Details about the issue...","email":"tom@outerspace.com"}";

  public static void main(String[] args) throws IOException 
  {
    while(true)
    {
      System.out.println("\nPress 1 for GET, 2 for POST, 3 for SEARCH-TKT, 4 for updating status to RESOLVED, 5 for Exit \n");
      int option = System.in.read();
  
      if (option == '1')
      {
        sendGET();
        System.out.println("GET DONE");
      }
      else if (option == '2')
      {
        sendPOST("point1", 1);
        System.out.println("POST DONE");
      }
      else if (option == '3')
      {
        int id = searchTicket("point1");
        System.out.println("Search GET DONE" + "tktId:" + id);
      }
      else if (option == '4')
      {
        int id = updateTicket("point1",RESOLVED);
        System.out.println("UPDATE DONE" + "tktId:" + id);
      }
      else if (option == '5')
      {
        System.out.println("Exiting...");
        return;
      }
    }    
  }

  /* START sendGET */
  private static void sendGET() throws IOException
  
  {
    URL obj = new URL(GET_URL);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    con.setRequestMethod("GET");
    con.setRequestProperty("User-Agent", USER_AGENT);    
    con.setRequestProperty("content-type", "application/json; charset=utf-8");
    
    String userpass = USERNAME_PASSWD;
    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    con.setRequestProperty ("Authorization", basicAuth);    
        
    int responseCode = con.getResponseCode();
    System.out.println("GET Response Code :: " + responseCode);

    BufferedReader in;
    if (responseCode == HttpURLConnection.HTTP_OK) 
    {   
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      System.out.println("GET OK");
    } 
    else 
    {
      in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
      System.out.println("GET request ERROR!");
    }
    
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      System.out.println(response.toString());
  }
/* END sendGET */
  
  /* START searchTicket */  
  private static int searchTicket(String pointName) throws IOException
  
  {
    String queryField = "pointname";
    String FilterGetUrl = GET_SEARCH_TKT_URL + "\"" + queryField + ":" + pointName + "\"";
    System.out.println("searchTkt: url: " + FilterGetUrl);    
    
    URL obj = new URL(FilterGetUrl);
    int retCode = OK;
    
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    con.setRequestMethod("GET");
    con.setRequestProperty("User-Agent", USER_AGENT);    
    con.setRequestProperty("content-type", "application/json; charset=utf-8");
    
    String userpass = USERNAME_PASSWD;
    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    con.setRequestProperty ("Authorization", basicAuth);    
        
    int responseCode = con.getResponseCode();
    System.out.println("SEARCHTKT: GET Response Code :: " + responseCode);

    BufferedReader in;
    
    if (responseCode == HttpURLConnection.HTTP_OK) 
    {   
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      System.out.println("SEARCH TKT GET OK");
      retCode = OK;
    } 
    else 
    {
      in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
      System.out.println("SEARCH TKT GET Error!!");
      retCode = ERROR;
    }

      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      String rspString = response.toString();
      
      System.out.println("RESPONSE STRING: " + rspString);
      
      if (retCode == OK)
      {
        // we got results. 
        // check total > 0 if found. then look for result[] and find id field
        
        JSONObject rspJsonObj = new JSONObject(rspString);
        int id;
        
        int total = rspJsonObj.getInt("total");
        if (total > 0)
        {
          if (total > 1)
          {
            // v expect 1 tkt per point. this is unexpected when more than 1 tkt matches a point name. log it
            System.out.println("More than one tkt for same point found. unexpected error!");
          }
          // atleast 1 entry found. get 1st entry. assuming 1 unique match as we already searched on pointname. we are assuming
          // for each point, only 1 tkt will be created. ASSUME
          id = rspJsonObj.getJSONArray("results").getJSONObject(0).getInt("id");
        
          System.out.println("ID: " + id);    
          return id;
        }
        else
        {
          System.out.println("total 0. unexpected when search passed!");    
          return ERROR;
        }
      }
      else
      {
        return ERROR;
      }
  }
/* END searchTicket */
  
/* START sendPOST */
  private static int sendPOST(String pointName, int priority) throws IOException 
  {   
    int retCode = OK;
    URL obj = new URL(POST_URL);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", USER_AGENT);

    con.setRequestProperty("content-type", "application/json; charset=utf-8");
    
    String userpass = USERNAME_PASSWD;
    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    con.setRequestProperty ("Authorization", basicAuth);               
    
    String params;
    try
    {
      JSONObject json = new JSONObject();
      json.put("email", "test@test.com");
      json.put("description", "Temperature Alarm detected by Niagara!");
      json.put("subject","Niagara Alarm Ticket: Fix AC/Heater via command centre");
 
      
      String[] tmpStrArray = new String[2];
      tmpStrArray[0] = pointName;
      
      json.put("tags",  tmpStrArray);
      json.put("priority", priority);
      json.put("status", 2);

      Map<String, String> map = new HashMap<String, String>();
      map.put("pointname", pointName);      
      
      json.put("custom_fields", map);
 
      
      params = json.toString();
      System.out.println("params:"+ params);
    }
    catch (JSONException e)
    {
      System.out.println("sendPOST: json exception!!");
      System.out.println("Aborting sendPOST");
      return ERROR;
    }

    // For POST only - START
    con.setDoOutput(true);
    OutputStream os = con.getOutputStream();
    os.write(params.getBytes());
    os.flush();
    os.close();
    // For POST only - END

    int responseCode = con.getResponseCode();
    System.out.println("POST Response Code :: " + responseCode);

    BufferedReader in;
    if (responseCode == HttpURLConnection.HTTP_CREATED) 
    {   
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      System.out.println("POST OK. Ticket Created!!");
      retCode = OK;
    } 
    else 
    {
      in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
      System.out.println("POST request ERROR!");
      retCode = ERROR;
    }    
    
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      System.out.println(response.toString());

      return retCode;
  }  
/* END sendPOST */  
  
}
