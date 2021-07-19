package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PinReset {
    //keyword: reset
    //phone : Phone;
    //SMS: reset DefaultPin NewPin
     DbConnection dbconn = new DbConnection();
      public String pinReset(String phone, String[] tokens) throws SQLException, ClassNotFoundException {
          
       int tokenLength = tokens.length;
       if (tokenLength == 2){
           int oldPin2 = Integer.parseInt(tokens[0]);
           try{
               int pin = Integer.parseInt(tokens[1]);
               if(tokens[1].length() != 6){
                   return "your new pin must be a 6 digit number";
               }
               
           
       try(Connection cun = dbconn.dbConnect()){
           String query = "SELECT * FROM customer WHERE phone = ?";
           PreparedStatement statement = cun.prepareStatement(query);
           statement.setString(1, phone);
           ResultSet result = statement.executeQuery();
           while(result.next()){
               int oldPin1 = result.getInt("pin");
                if(oldPin1 == oldPin2) {
                    try(Connection conn = dbconn.dbConnect()){
                        String sql = "UPDATE customer SET `pin` = ? WHERE `phone` = ? ";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, pin);
                        stmt.setString(2, phone);
                            stmt.execute();

                        }
                    }
                else{
                    return "please enter your correct old pin followed by the new one";
                }
                 
                }
           return "successful Operation your new pin is " + pin;
           }
       catch (ClassNotFoundException | SQLException ex  ) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            return "failed";
        }
       
           } catch(NumberFormatException e ){   
               return "invalid entry for new pin. Pin must be digits";
       }
    }
       else {
           return "invalid operation";
       }
      }
}
