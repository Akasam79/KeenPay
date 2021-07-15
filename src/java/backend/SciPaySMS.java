package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 @Path("enroll")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String enroll(@FormParam("phone") String phone, @FormParam("sms") String sms) throws JSONException, ClassNotFoundException, SQLException {
        json = new JSONObject();
        String[] tokens = sms.split(" ");
        System.out.println("Step 1");
        if(enrollUser(phone,tokens)){
            json.put("type", "success");
            System.out.println("Step 2");
        }else{
            json.put("type", "failed");
            System.out.println("Step 3");
        }
        return json.toString();
    }
    
    @Path("pay")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String pay(@FormParam("phone") String phone,@FormParam("sms") String sms) throws JSONException, ClassNotFoundException, SQLException, InterruptedException {
        json = new JSONObject();
        String[] pTokens = sms.split(" ");
//        json.put("type", confirmPayment(pTokens)).wait();
        json.put("type", payment(phone, pTokens));
        return json.toString();
    }
    
    @Path("pay/confirm")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String confirm(@FormParam("phone") String phone,@FormParam("sms") String sms) throws JSONException, ClassNotFoundException, SQLException {
        json = new JSONObject();
        String[] cTokens = sms.split(" ");
        json.put("type", confirmPayment(phone, cTokens));
        return json.toString();
    }
    
    
    @Path("test")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String text() {
        return "Halleluyah";
    }
    
    public Connection dbConnect() throws ClassNotFoundException, SQLException{
        String url = "jdbc:mysql://localhost:3306/scipay?useSSL=false&serverTimezone=Africa/Lagos";
        String user = "root";
        String password = "samuel";
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Step 4");
        return DriverManager.getConnection(url, user, password);
    }
    //Keyweord : enroll
    //phone;
    //enroll userType, firstName, lastName, bvn
    private boolean enrollUser(String phone, String[] tokens) throws ClassNotFoundException, SQLException {
        System.out.println("Step 5");            
        int tl = tokens.length;
        String userType = tokens[1];
        if(tl>5||tl<4)return false;
        if(tl==4 && userType.equalsIgnoreCase("merchant")){
            System.out.println("Step 6");
            return false;
        }else if (tl ==5 && userType.equalsIgnoreCase("merchant")) {
            System.out.println("Step 7");
        String bvn = "";
        if(tokens.length==5)bvn = tokens[4];
        int min = 100;  
        int max = 1000000000;
        String staticCode = "100";
        String mshortcode = staticCode+(int)(Math.random()*(max-min+1)+min);
        
        try (Connection connect = dbConnect()){
                    String qry = "INSERT INTO users (phone, firstName, lastName, bvn) VALUES(?,?,?,?)";
                    PreparedStatement stmt3 = connect.prepareStatement(qry);

                    stmt3.setString(1,phone);
                    stmt3.setString(2,tokens[2]);
                    stmt3.setString(3,tokens[3]);
                    stmt3.setString(4,bvn);
                    System.out.println("Step 8");
                    while(!stmt3.execute()){
                        try(Connection conn = dbConnect()){
                            String sql = "INSERT INTO merchant (phone,mShortCode,firstName,lastName,bvn) VALUES (?,?,?,?,?)";
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setString(1,phone);
                            stmt.setString(2, mshortcode);
                            stmt.setString(3,tokens[2]);
                            stmt.setString(4,tokens[3]);
                            stmt.setString(5,bvn);
                            stmt.execute();
                            System.out.println("Step 9");
                            return true;
                        }
                        catch (ClassNotFoundException | SQLException ex) {
                            System.out.println("Step 9a");
                           Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                           return false;
                       }
            
                    }
                    System.out.println("9b");
                    return true;
            }
        catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Step 10");
            return false;
        }
        }
        
        
        
        else if (tl ==4 || tl ==5 && userType.equalsIgnoreCase("customer")) {
        String bvn = "";
        if(tokens.length==5)bvn = tokens[4];
         try (Connection connect = dbConnect()){
             
             int min = 100;  
        int max = 1000000000;
        String staticNum = "100";
        String pin = staticNum+(int)(Math.random()*(max-min+1)+min);
        if (pin.length()>6){
                 String newPin = pin.substring(0, 6);
        
                    String qry = "INSERT INTO users (phone, firstName, lastName, bvn) VALUES(?,?,?,?)";
                    PreparedStatement stmt3 = connect.prepareStatement(qry);

                    stmt3.setString(1,phone);
                    stmt3.setString(2,tokens[2]);
                    stmt3.setString(3,tokens[3]);
                    stmt3.setString(4,bvn);
                    while(!stmt3.execute()){
                        try(Connection con = dbConnect()){
                            String sql = "INSERT INTO customer (phone,firstName,lastName, bvn, pin) VALUES (?,?,?,?,?)";
                            PreparedStatement stmt = con.prepareStatement(sql);
                            stmt.setString(1,phone);
                            stmt.setString(2,tokens[2]);
                            stmt.setString(3,tokens[3]);
                            stmt.setString(4,bvn);
                            stmt.setString(5, newPin);
                            stmt.execute();
                                return true;
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                            return false;
                        }
                    }
        
        }}
         catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
        return true;
    }
    //keyword : PAY
    // pay merchantName lastName, mShortCode
    private String payment(String phone, String[] pTokens) throws ClassNotFoundException, SQLException {
        int tokL = pTokens.length;
        if(tokL>2||tokL<2)return "";
        
        String mShortCode = "";
        int amount = 0;
        String firstName = "";
        String lastName = "";
        String transID = "";
        
        int min = 100;  
        int max = 1000000000;
        String staticNum = "11";
        String pin = staticNum+(int)(Math.random()*(max-min+1)+min);
        if (pin.length()>7) transID = pin.substring(0, 7);
        
       if(tokL ==2) mShortCode = pTokens[0];
       if(tokL ==2) amount = Integer.parseInt(pTokens[1]); 
       
       try(Connection newconn = dbConnect()){
           String seql = "SELECT * FROM customer";
           PreparedStatement stmtx = newconn.prepareStatement(seql);
           ResultSet newRes = stmtx.executeQuery();
           
           while (newRes.next()){
               if (newRes.getString("phone").equals(phone)){
                  int Balance = newRes.getInt("custBalance");
                   String payerFN = newRes.getString("firstName");
                   String payerLN = newRes.getString("lastName");
                  if(Balance >= amount){
       
            try(Connection dbconn = dbConnect()){
                        String query = "SELECT * FROM merchant";
                        PreparedStatement statement = dbconn.prepareStatement(query);
                        ResultSet res = statement.executeQuery();
                        while(res.next()){
                            if(res.getString("mshortcode").equals(mShortCode)){
                                
                                String payer = payerFN +" "+ payerLN;

                                try(Connection conn = dbConnect()){
                                    String SQL = "INSERT INTO activepayments(phone, transID, payer, payee, amount) VALUES (?,?,?,?,?)";
                                    PreparedStatement stmt = conn.prepareStatement(SQL);

                                    stmt.setString(1,phone);
                                    stmt.setString(2,transID);
                                    stmt.setString(3, payer);
                                    stmt.setString(4, mShortCode);
                                    stmt.setInt(5, amount);
                                    
                                    firstName = res.getString("firstName");
                                    lastName = res.getString ("lastName");
                                    stmt.execute();

//                                    if(confirmPayment(pTokens)){
//                                      
//                                    }
                        } 
                           
                    }     
                        }
                }
            
                   } else {
                      return "Insufficient balance";
                  }
       }
           }
                    
        return "Are you sure you want to pay " + firstName +" "+ lastName +" "+ "the sum of #" + amount+ "?"
                + " Reply with your pin followed by " + transID + " or 0 to cancel";
        
        }
           catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
           return "failed";
        }
       
    }
    
    
    // keyword: confirm
    // confirm payment PIN
     private String confirmPayment(String phone, String[] cTokens) throws ClassNotFoundException, SQLException {
        int LoT = cTokens.length;
        if(LoT>2||LoT<2)return "";
        int pin = 0;
        int tID = 0;
        
        if(LoT == 2) pin = Integer.parseInt(cTokens[0]);
        if(LoT == 2) tID = Integer.parseInt(cTokens[1]);
//        String firstName = "";
//        String lastName = "";
       
        try(Connection conn = dbConnect()){
            String sql = "SELECT * FROM activepayments GROUP BY phone";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                String rsPhone = rs.getString("phone");
                if (rsPhone.equals(phone)){
                
                int transID = rs.getInt("transID");
                        
                 if(transID == tID){
                     
                 try(Connection cun = dbConnect()){
                    String sql2 = "SELECT * from customer";
                    PreparedStatement stmt = cun.prepareStatement(sql2);
                    ResultSet result = stmt.executeQuery();
                    while(result.next()){
//                        
//                        String firstName = result.getString("firstName");
//                        String lastName = result.getString("lastName");
//                        
//                        String name = firstName +" "+ lastName;
                        String resultPhone = result.getString("phone");
                        
                        if (resultPhone.equals(phone)){
                        int PIN = result.getInt("pin");
                            
                        if(PIN == pin){
                            String firstName = result.getString("firstName");
                           String lastName = result.getString("lastName");
                           String name = firstName +" "+ lastName;
//                            
//                            if(rs.getString("payer").equalsIgnoreCase(name)){
                            
//                            try(Connection dbConnx = dbConnect()){
//                                String query = "SELECT * FROM activepayments";
//                                PreparedStatement statemt = dbConnx.prepareStatement(query);
//                                ResultSet res = statemt.executeQuery();
//                                
//                                while (res.next()){
                                    
                                        String mShortCode = rs.getString("payee");
                                        int amount = rs.getInt("amount");
                                        
                                        try(Connection newConn = dbConnect()){
                                            String qry = "INSERT into payments(mShortCode, amount, madeBy) VALUES (?,?,?)";
                                            PreparedStatement statmnt = newConn.prepareStatement(qry);
                                            
                                            statmnt.setString(1, mShortCode);
                                            statmnt.setInt(2, amount);
                                            statmnt.setString(3, name);
                                            statmnt.execute();
//                                            while(statmnt.execute()){
//                                                try (Connection connecting = dbConnect()){
//                                                    String qury = "UPDATE customer set custBalance = ?  WHERE phone = ?";
//                                                    PreparedStatement stment = connecting.prepareStatement(qury);
//                                                    
//                                                    stment.setString(1, currBal);
//                                                }
//                                            }
                                            
                                        }
                                    } else{
                                            return "pins do not match";
                                        }
//                                    }
//                                     else{
//                                        return "Pins do not match";
//                         }
                               
                        
                    }
                    }
                 }
            }
                                else{
                                        return "invalid Transaction ID";
                                }
                }
            }
                return "payment successful";
            }catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            return "failed";
        }
        
    }
     
    
     
     //keyword: reset
     // reset pin, firstName, lastName, defaultPIN
      private boolean pinReset(String phone, String[] tokens) {
       
        try(Connection conn = dbConnect()){
            String sql = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet res = stmt.executeQuery();
            while(res.next()){
                
            }
            
            return true;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
}

