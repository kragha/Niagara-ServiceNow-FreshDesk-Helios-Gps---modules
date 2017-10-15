package webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LeanovateHttpUrlConnection 
{

  private static final String USER_AGENT = "ragkrish/1.0";

  private static final String USERNAME_PASSWD = "kragha2000@gmail.com:123surya";
  
  private static final String GET_URL = "https://ragkrish.freshdesk.com/api/v2/tickets";

  private static final String POST_URL = "https://ragkrish.freshdesk.com/api/v2/tickets";

  //private static final String POST_PARAMS = "-d {"description":"Details about the issue...","email":"tom@outerspace.com"}";

  public static void main(String[] args) throws IOException 
  {
    System.out.println("Press 1 for GET, 2 for POST\n");
    int option = System.in.read();

    if (option == '1')
    {
      sendGET();
      System.out.println("GET DONE");
    }
    else
    {
      sendPOST();
      System.out.println("POST DONE");
    }
  }

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

  private static void sendPOST() throws IOException 
  {   
    URL obj = new URL(POST_URL);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", USER_AGENT);

    con.setRequestProperty("content-type", "application/json; charset=utf-8");
    
    String userpass = USERNAME_PASSWD;
    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    con.setRequestProperty ("Authorization", basicAuth);               
    
    JSONObject json = new JSONObject();
    json.put("email", "test@test.com");
    json.put("description", "Details about the issue...");
    json.put("subject","Support Needed...");
    json.put("priority",1);
    json.put("status", 2);

    String params = json.toString();
    
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
    } 
    else 
    {
      in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
      System.out.println("POST request ERROR!");
    }    
    
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      System.out.println(response.toString());
  }

}
