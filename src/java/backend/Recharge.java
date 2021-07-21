
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Recharge
// SMS : recharge phone + amount
// returns : are you sure you want to top-up "fullName" with "amount"? reply with PIN + transID


public class Recharge {
    DbConnection dbconn = new DbConnection();
    String firstName = "";
    String lastName = "";
    String adminName = "";
    int adminPin;
    public String recharge(String phone, String[] tokens) throws ClassNotFoundException, SQLException{
        
        int tl = tokens.length;
        String transID = "";
        
        int min = 100;  
        int max = 1000000000;
        String staticNum = "11";
        String pin = staticNum+(int)(Math.random()*(max-min+1)+min);
        if (pin.length()>7) transID = pin.substring(0, 7);
        
        if (tl == 2){
            String custPhone = tokens[0];
            int amount = Integer.parseInt(tokens[1]);
            
            
            try(Connection connect = dbconn.dbConnect()){
                String query = "SELECT * FROM outletadmins WHERE phone = ?";
                PreparedStatement state = connect.prepareStatement(query);
                state.setString(1, phone);
                ResultSet res = state.executeQuery();
                while(res.next()){
                    String adminPhone = res.getString("phone");
                    adminName = res.getString("fullName");
                    adminPin = res.getInt("Pin");
                    if (adminPhone.equalsIgnoreCase(phone)){
                    try(Connection con = dbconn.dbConnect()){
                        String qry = "SELECT * FROM users WHERE phone = ?";
                        PreparedStatement stmt2 = con.prepareStatement(qry);
                        stmt2.setString(1, custPhone);
                        ResultSet rs = stmt2.executeQuery();
                        while (rs.next()){
                            firstName = rs.getString("firstName");
                            lastName = rs.getString("lastName");
                            String custName = firstName +" " + lastName;
                                    
                                        try(Connection conn = dbconn.dbConnect()){
                                            String SQL = "INSERT INTO activepayments(phone, transID, payer, payeePhone, payeeName, amount)"
                                                           + " VALUES (?,?,?,?,?,?)";
                                            PreparedStatement stmt = conn.prepareStatement(SQL);

                                            stmt.setString(1,phone);
                                            stmt.setString(2,transID);
                                            stmt.setString(3, adminName);
                                            stmt.setString(4, custPhone);
                                            stmt.setString(5, custName);
                                            stmt.setInt(6, amount);
                                            stmt.execute();
                                        }
                                    }
                            }
                        }else{
                            return "To perform this transaction, you need to be registered as an admin pls contact us at 08139749489";
                        }
                        return "Are you sure you want to top-up " + firstName + " " + lastName + "'s Account with #" + amount+
                        "? Reply with confirmTopUP +yourPin +" +transID +" to complete this process";
                    }
                
                }catch(SQLException ex){
                    Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                    return "failed"+ ex.getMessage();
            }
             
        }
        else{
            return "Invalid/Improper Keywords";
        }
        return "Failed! To perform this transaction, you need to be registered as an admin pls contact us at 08139749489";
    }
    
    //Keyword: ConfirmTOpUP
    // sms: confirmTopUp + pin + transID
    public String topUp (String phone, String [] tokens) throws ClassNotFoundException, SQLException{
        
        int tokL = tokens.length;
        if (tokL == 2){
            int pin = Integer.parseInt(tokens[0]);
            int transID = Integer.parseInt(tokens[1]);
            try(Connection con = dbconn.dbConnect()){
                String SQL = "SELECT * FROM activepayments WHERE transID = ?";
                PreparedStatement stmt = con.prepareStatement(SQL);
                stmt.setInt(1, transID);
                ResultSet res = stmt.executeQuery();
                
                
                while(res.next()){
                    int transID2 = res.getInt("transID");
                    String custName = res.getString("payeeName");
                    
                        try(Connection pinCon = dbconn.dbConnect()){
                            String pQry = "SELECT * FROM outletadmins WHERE phone =?";
                            PreparedStatement pstmt = pinCon.prepareStatement(pQry);
                            pstmt.setString(1, phone);
                            ResultSet pRes = pstmt.executeQuery();
                            while(pRes.next()){

                                String payeePhone = res.getString("payeePhone");
                                int amount = res.getInt("amount");
                                adminPin = pRes.getInt("Pin");
                                adminName = pRes.getString("fullName");
                                if(pin == adminPin && transID == transID2){
                                    try(Connection conn = dbconn.dbConnect()){
                                        String query = "INSERT INTO topups (madeBy, adminPhone, recPhone, recName, amount) VALUES (?,?,?,?,?)";
                                        PreparedStatement statement = conn.prepareStatement(query);
                                        statement.setString(1, adminName);
                                        statement.setString(2, phone);
                                        statement.setString(3, payeePhone);
                                        statement.setString (4, custName);
                                        statement.setInt(5, amount);
                                        while(!statement.execute()){
                                            try(Connection connect = dbconn.dbConnect()){
                                                String qry = "SELECT * FROM users WHERE phone =? ";
                                                PreparedStatement stmt2 = connect.prepareStatement(qry);
                                                stmt2.setString(1, payeePhone);
                                                ResultSet result = stmt2.executeQuery();
                                                while(result.next()){
                                                    String userType = result.getString("userType");
                                                    if (userType.equalsIgnoreCase("merchant")){
                                                        try(Connection mcon = dbconn.dbConnect()){
                                                            String sequel = "SELECT * FROM mwallet WHERE phone =  ?";
                                                            PreparedStatement state = mcon.prepareStatement(sequel);
                                                            state.setString(1, payeePhone);
                                                            ResultSet mres = state.executeQuery();

                                                            while(mres.next()){
                                                                int balance = mres.getInt("balance");
                                                                int newBalance = balance + amount;
                                                                    try(Connection updateMerch = dbconn.dbConnect()){
                                                                        String seql = "UPDATE mwallet SET balance = ? WHERE phone = ?";
                                                                        PreparedStatement updateStmt = updateMerch.prepareStatement(seql);
                                                                        updateStmt.setInt(1, newBalance);
                                                                        updateStmt.setString(2, payeePhone);
                                                                            while(!updateStmt.execute()){
                                                                                try (Connection deleteCon = dbconn.dbConnect()){
                                                                                    String delQry = "DELETE FROM activepayments WHERE transID = ?";
                                                                                    PreparedStatement delStmt = deleteCon.prepareStatement(delQry);
                                                                                    delStmt.setInt(1, transID);
                                                                                    delStmt.execute();
                                                                                    return custName + "'s account topped up with " + amount +" successfully";
                                                                                }
                                                                            }                                                        

                                                                    }
                                                            }
                                                        }
                                                    }else if(userType.equalsIgnoreCase("customer")){
                                                        try (Connection custCon = dbconn.dbConnect()){
                                                            String cQry = "SELECT * FROM cwallet WHERE phone = ?";
                                                            PreparedStatement cStmt = custCon.prepareStatement(cQry);
                                                            cStmt.setString(1, payeePhone);
                                                            ResultSet cRes = cStmt.executeQuery();
                                                            while(cRes.next()){
                                                                int balance = cRes.getInt("balance");
                                                                int newBalance = balance + amount;
                                                                    try(Connection updateCust = dbconn.dbConnect()){
                                                                        String seql2 = "UPDATE cwallet SET balance = ? WHERE phone = ?";
                                                                        PreparedStatement updateStmt2 = updateCust.prepareStatement(seql2);
                                                                        updateStmt2.setInt(1, newBalance);
                                                                        updateStmt2.setString(2, payeePhone);
                                                                            while(!updateStmt2.execute()){
                                                                                try (Connection deleteCon2 = dbconn.dbConnect()){
                                                                                    String delQry2 = "DELETE FROM activepayments WHERE transID = ?";
                                                                                    PreparedStatement delStmt2 = deleteCon2.prepareStatement(delQry2);
                                                                                    delStmt2.setInt(1, transID);
                                                                                    delStmt2.execute();
                                                                                    return  custName +"'s account topped up with " + amount +" successfully";
                                                                                }
                                                                            }                                                        

                                                                    }
                                                            }
                                                        }
                                                    }
                                        return "TopUp Successful";

                                                }
                                            }
                                        }
                                    }
                            }else {
                                    return "invalid pin or transaction ID: pls check and try again";
                                }
                        }
                    }
                }
            }catch (SQLException ex){
                Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                return "API failed"+ ex.getMessage();
            }
        }else{
            return "Invalid/Improper Keywords";
        }
        return "Failed! Something went wrong!";
    }
}
