package backend;


import java.sql.SQLException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;


@Path("api")
public class SciPaySMS {
    JSONObject json;
    @Context
    private UriInfo context;

    public SciPaySMS() {
    }
    
    @Path("main")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String main(@FormParam("phone") String phone, @FormParam("sms") String sms) 
            throws JSONException, ClassNotFoundException, SQLException, InterruptedException{
        
        //check the sms to determine next line of action
        sms = sms.trim().replaceFirst("\\s", "_");
        
        String []i_a = sms.split("_");
        String keyword = i_a[0].toLowerCase();
        String res = "API failed";
        switch(keyword){
            case "enroll":
                res = enroll(phone,i_a[1]);
                break;
                
            case "pay":
                res = pay(phone, i_a[1]);
                break;
                
            case "confirm":
                res = confirm (phone, i_a[1]);
                break;
                
            case "reset":
                res = reset(phone, i_a[1]);
                break;
                
            case"balance":
                res = balance(phone, i_a[1]);
                break;
                
        }
        
        return res;
    }
    
    
    public String enroll(String phone,String sms) 
            throws JSONException, ClassNotFoundException, SQLException {
        Enrollment enroll = new Enrollment();
        json = new JSONObject();
        String[] tokens = sms.split(" ");
            json.put("type", enroll.enrollUser(phone, tokens));
        return json.toString();
    }
    
    public String pay(String phone,String sms) throws JSONException, 
            ClassNotFoundException, SQLException, InterruptedException {
        
        Payments pay = new Payments();
        json = new JSONObject();
        String[] pTokens = sms.split(" ");
        json.put("type", pay.payment(phone, pTokens));
        return json.toString();
    }
    
    public String confirm(String phone, String sms) 
            throws JSONException, ClassNotFoundException, SQLException {
        Payments pay = new Payments();
        json = new JSONObject();
        String[] cTokens = sms.split(" ");
        json.put("type", pay.confirmPayment(phone, cTokens));
        return json.toString();
    }
    
    public String reset(String phone, String sms) 
            throws JSONException, ClassNotFoundException, SQLException {
        PinReset reset = new PinReset();
        json = new JSONObject();
        String[] cTokens = sms.split(" ");
        json.put("type", reset.pinReset(phone, cTokens));
        return json.toString();
    }
    
    public String balance(String phone, String sms) 
            throws JSONException, ClassNotFoundException, SQLException {
        Wallet walletBal = new Wallet();
        json = new JSONObject();
        String[] Tokens = sms.split(" ");
        json.put("type", walletBal.walletBalance(phone, Tokens)); 
        return json.toString();
    }
    
    
    @Path("test")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String text() {
        return "Halleluyah";
    }
    
  
    
    
}

