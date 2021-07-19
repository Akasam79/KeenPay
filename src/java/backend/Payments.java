package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Payments {
     //keyword : PAY
    //phone : Phone;
    //SMS: pay mShortCode amount
    DbConnection dbConn = new DbConnection();
    public String payment(String phone, String[] pTokens) throws ClassNotFoundException, SQLException {
        
        int tokL = pTokens.length;
        if(tokL>2||tokL<2)return "Enter the Proper keywords. i.e Pay `Merchant short code` amount (No double spaces)";
        
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
       
        try(Connection newconn = dbConn.dbConnect()){
           String seql = "SELECT * FROM customer WHERE phone = ?";
           PreparedStatement stmtx = newconn.prepareStatement(seql);
           stmtx.setString(1, phone);
           ResultSet newRes = stmtx.executeQuery();
           while (newRes.next()){
            try(Connection walletCon = dbConn.dbConnect()){
                String walSQL = "SELECT * FROM cwallet WHERE phone = ?";
                PreparedStatement walStmt = walletCon.prepareStatement(walSQL);
                walStmt.setString(1, phone);
                ResultSet walRes = walStmt.executeQuery();
                
                    if (newRes.getString("phone").equals(phone)){
                        while(walRes.next()){
                            int Balance = walRes.getInt("balance");
                            String payerFN = newRes.getString("firstName");
                            String payerLN = newRes.getString("lastName");
                                if(Balance >= amount){

                                    try(Connection dbconn = dbConn.dbConnect()){
                                                String query = "SELECT * FROM merchant";
                                                PreparedStatement statement = dbconn.prepareStatement(query);
                                                ResultSet res = statement.executeQuery();
                                                while(res.next()){
                                                    if(res.getString("mshortcode").equals(mShortCode)){

                                                        String payer = payerFN +" "+ payerLN;

                                                        try(Connection conn = dbConn.dbConnect()){
                                                            String SQL = "INSERT INTO activepayments(phone, transID, payer, payee, amount)"
                                                                    + " VALUES (?,?,?,?,?)";
                                                            PreparedStatement stmt = conn.prepareStatement(SQL);

                                                            stmt.setString(1,phone);
                                                            stmt.setString(2,transID);
                                                            stmt.setString(3, payer);
                                                            stmt.setString(4, mShortCode);
                                                            stmt.setInt(5, amount);

                                                            firstName = res.getString("firstName");
                                                            lastName = res.getString ("lastName");
                                                            stmt.execute();

                                                        } 

                                                    }     
                                                }
                                    }

                            } else {
                              return "Insufficient balance";
                            }
                        }
                    }else {
                        return "phone number not registered in database";
                    }
                }
            }
                    
        return "Are you sure you want to pay " + firstName +" "+ lastName +" "+ "the sum of #" + amount+ "?"
                + " Reply with confirm +your pin followed by " + transID + " or 0 to cancel";
        
        }
           catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
           return "failed";
        }
       
    }
    // keyword: confirm
    // confirm payment PIN
     public String confirmPayment(String phone, String[] cTokens) throws ClassNotFoundException, SQLException {
        int LoT = cTokens.length;
        if(LoT>2||LoT<2)return "Pls enter the proper keyword as described prior to this message";
        int pin = 0;
        int tID = 0;
        
        if(LoT == 2) pin = Integer.parseInt(cTokens[0]);
        if(LoT == 2) tID = Integer.parseInt(cTokens[1]);
//        String firstName = "";
//        String lastName = "";
       
        try(Connection conn = dbConn.dbConnect()){
            String sql = "SELECT * FROM activepayments WHERE phone = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, phone);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                String rsPhone = rs.getString("phone");
                if (rsPhone.equals(phone)){
                
                int transID = rs.getInt("transID");
                        
                    if(transID == tID){
                     
                        try(Connection cun = dbConn.dbConnect()){
                            String sql2 = "SELECT * from customer WHERE phone = ?";
                            PreparedStatement stmt = cun.prepareStatement(sql2);
                            stmt.setString(1, phone);
                            ResultSet result = stmt.executeQuery();
                            while(result.next()){

                                String resultPhone = result.getString("phone");

                                if (resultPhone.equals(phone)){
                                int PIN = result.getInt("pin");

                                    if(PIN == pin){
                                        String firstName = result.getString("firstName");
                                        String lastName = result.getString("lastName");
                                        String name = firstName +" "+ lastName;
                                            String mShortCode = rs.getString("payee");
                                            int amount = rs.getInt("amount");
                                            
                                            try(Connection dbConect = dbConn.dbConnect()){
                                                String queryMerch = "SELECT * FROM merchant WHERE mshortcode = ?";
                                                PreparedStatement merchStmt = dbConect.prepareStatement(queryMerch);
                                                merchStmt.setString(1, mShortCode);
                                                ResultSet resp = merchStmt.executeQuery();
                                                
                                                
                                                while(resp.next()){
                                                    String merchPhone = resp.getString("phone");
                                                    try(Connection newConn = dbConn.dbConnect()){
                                                        String qry = "INSERT into payments(mShortCode, amount, madeBy) VALUES (?,?,?)";
                                                        PreparedStatement statmnt = newConn.prepareStatement(qry);
                                                        statmnt.setString(1, mShortCode);
                                                        statmnt.setInt(2, amount);
                                                        statmnt.setString(3, name);
                                                        while(!statmnt.execute()){
                                                            try(Connection connect = dbConn.dbConnect()){
                                                                String sequel = "SELECT * FROM cwallet WHERE phone =  ?";
                                                                PreparedStatement state = connect.prepareStatement(sequel);
                                                                state.setString(1, phone);
                                                                ResultSet res = state.executeQuery();
                                                                
                                                                while(res.next()){
                                                                int balance = res.getInt("balance");
                                                                int newBalance = balance - amount;
                                                                    try(Connection updateCust = dbConn.dbConnect()){
                                                                        String seql = "UPDATE cwallet SET balance = ? WHERE phone = ?";
                                                                        PreparedStatement updateStmt = updateCust.prepareStatement(seql);
                                                                        updateStmt.setInt(1, newBalance);
                                                                        updateStmt.setString(2, phone);
                                                                        while(!updateStmt.execute()){
                                                                            try(Connection Msequel = dbConn.dbConnect()){
                                                                            String qury = "SELECT * FROM mwallet where phone = ?";
                                                                            PreparedStatement init = Msequel.prepareStatement(qury);
                                                                            init.setString(1, merchPhone);
                                                                            ResultSet mResult = init.executeQuery();
                                                                            
                                                                                while(mResult.next()){
                                                                                    int PreviousBalance = mResult.getInt("balance");
                                                                                    int merchBalance = PreviousBalance + amount;
                                                                                    try(Connection updateMerch = dbConn.dbConnect()){
                                                                                        String Mseql = "UPDATE mwallet SET balance = ? where phone = ?";
                                                                                        PreparedStatement mUpdateStmt = updateMerch.prepareStatement(Mseql);
                                                                                        mUpdateStmt.setInt(1, merchBalance);
                                                                                        mUpdateStmt.setString(2, merchPhone);
                                                                                        while(!mUpdateStmt.execute()){
                                                                                            try (Connection deleteCon = dbConn.dbConnect()){
                                                                                                String delQry = "DELETE FROM activepayments WHERE transID = ?";
                                                                                                PreparedStatement delStmt = deleteCon.prepareStatement(delQry);
                                                                                                delStmt.setInt(1, transID);
                                                                                                delStmt.execute();
                                                                                                return "All operations successful";
                                                                                            }
                                                                                        }
                                                                                        
                                                                                        

                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    }else{
                                        return "pins do not match";
                                    }

                                }
                            }
                        }
                    }
                    else{
                    return "invalid Transaction ID";
                    }
                }
                else{
                    return "phone numbers do not match";
                }
                
                
            }
                return "payment successful";
        }catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
            return "failed";
        }
        
    }
}

