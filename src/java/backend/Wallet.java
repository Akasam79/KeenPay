package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Keyword Retrieve
//phone : Phone;
//SMS: balance +pin
public class Wallet {
    DbConnection dbconn = new DbConnection();
    public String walletBalance(String phone, String[] tokens) throws ClassNotFoundException, SQLException{
        int tl = tokens.length;
        if(tl == 1){
            
            
            try(Connection con = dbconn.dbConnect()){
                String sql = "SELECT * FROM users where phone = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, phone);
                ResultSet res = stmt.executeQuery();
                while(res.next()){
                    String userType = res.getString("userType");
                    String fullName = res.getString("firstName") + " "+ res.getString("lastName");
                    if(userType.equalsIgnoreCase("merchant")){
                        try(Connection mConn = dbconn.dbConnect()){
                            String mSQL = "SELECT * FROM merchant where phone = ?";
                            PreparedStatement mStmt = mConn.prepareStatement(mSQL);
                            mStmt.setString(1, phone);
                            ResultSet response = mStmt.executeQuery();
                            while(response.next()){
                                String newMshortcode = response.getString("mShortCode");
                                String mshortcode = tokens[0];
                                if(newMshortcode.equalsIgnoreCase(mshortcode)  ) {
                                    try (Connection conn = dbconn.dbConnect()){
                                        String query = "SELECT * FROM mwallet WHERE phone = ?";
                                        PreparedStatement statement = conn.prepareStatement(query);
                                        statement.setString(1, phone);
                                        ResultSet rs = statement.executeQuery();
                                        while(rs.next()){
                                            int mBalance = rs.getInt("balance");

                                            return "Hello " + fullName + " Your wallet Balance is " + "#"+mBalance;
                                        }
                                    }
                                }else{
                                    return "retrieval failed!, incorrect Merchant Code";
                                }
                            }
                        }
                    }
                
                
                    else if(userType.equalsIgnoreCase("customer")){
                        try(Connection cConnect = dbconn.dbConnect()){
                            String cSQL = "SELECT * FROM customer WHERE phone = ?";
                            PreparedStatement cStmt = cConnect.prepareStatement(cSQL);
                            cStmt.setString(1, phone);
                            ResultSet cRes = cStmt.executeQuery();
                            while(cRes.next()){
                                int dbPin = cRes.getInt("pin");
                                int pin = Integer.parseInt(tokens[0]);
                                if (dbPin == pin){
                                    try(Connection connect = dbconn.dbConnect()){
                                        String SQL = "SELECT * FROM cwallet WHERE phone = ?";
                                        PreparedStatement stmt2 = connect.prepareStatement(SQL);
                                        stmt2.setString(1, phone);
                                        ResultSet resp = stmt2.executeQuery();
                                        while(resp.next()){
                                            int cBalance = resp.getInt("balance");
                                            return "Hello " + fullName + " Your wallet Balance is " + "#"+cBalance;
                                        }
                                        

                                       
                                    }
                                }else {
                                    return "retrieval failed! Incorrect Pin";
                                }
                            }
                        }

                    }else {
                        return "Record not found!, Pls make sure you have registered in our platform";
                    }
                }
            }
            catch(ClassNotFoundException | SQLException e){
                Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, e);
                return "failed";
            }
        }
        return "Suucessful!, your wallet Balance is: + Balance ";
    }
    
}
