
package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Enrollment {
    
    //Keyweord : enroll
    //phone : Phone;
    //SMS: enroll userType firstName lastName bvn
    DbConnection dbconn = new DbConnection();
    public String enrollUser(String phone, String[] tokens) throws ClassNotFoundException, SQLException {
        int tl = tokens.length;
        String userType = tokens[0];
        if(tl>4||tl<3)return "pls enter valid keywords as directed";
//        try(Connection checkPhone = dbconn.dbConnect()){
//            String uQry = "SELECT * FROM users";
//            PreparedStatement uStmt = checkPhone.prepareStatement(uQry);
//            ResultSet uRes = uStmt.executeQuery();
//            while(uRes.next()){
//            String uPhone = uRes.getString("phone");
//            if(uPhone.equalsIgnoreCase(phone) == false){
                if(tl==3 && userType.equalsIgnoreCase("merchant")){
                    return "merchant must have firstName, LastName and BVN in that order";
                }else if (tl ==4 && userType.equalsIgnoreCase("merchant")) {
                String bvn = "";
                if(tokens.length==4)bvn = tokens[3];
                int min = 100;  
                int max = 1000000000;
                String staticCode = "100";
                String mshortcode = staticCode+(int)(Math.random()*(max-min+1)+min);

                    try (Connection connect = dbconn.dbConnect()){
                                String qry = "INSERT INTO users (phone,userType, firstName, lastName, bvn) VALUES(?,?,?,?,?)";
                                PreparedStatement stmt3 = connect.prepareStatement(qry);

                                stmt3.setString(1,phone);
                                stmt3.setString(2, userType);
                                stmt3.setString(3,tokens[1]);
                                stmt3.setString(4,tokens[2]);
                                stmt3.setString(5,bvn);
                                String fullname = tokens[1] +" "+ tokens[2];
                                while(!stmt3.execute()){
                                    try(Connection conn = dbconn.dbConnect()){
                                        String sql = "INSERT INTO merchant (phone,mShortCode,firstName,lastName,bvn) VALUES (?,?,?,?,?)";
                                        PreparedStatement stmt = conn.prepareStatement(sql);
                                        stmt.setString(1,phone);
                                        stmt.setString(2, mshortcode);
                                        stmt.setString(3,tokens[1]);
                                        stmt.setString(4,tokens[2]);
                                        stmt.setString(5,bvn);
                                        while(!stmt.execute()){
                                             try(Connection init = dbconn.dbConnect()){
                                                String sequel = "INSERT INTO mwallet (phone, fullname) VALUES (?,?)";
                                                PreparedStatement pstmt = init.prepareStatement(sequel);
                                                pstmt.setString(1,phone);
                                                pstmt.setString(2, fullname);
                                                pstmt.execute();
                                                return "Registration successful!";
                                        }
                                    }

                                }
                        }
                    }
                    catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                        return "Account creation 1 failed! pls try again " + ex.getMessage();
                    }


                }

                    else if (tl ==3 || tl ==4 && userType.equalsIgnoreCase("customer")) {
                    String bvn = "";
                    if(tokens.length==4)bvn = tokens[3];
                        try (Connection connect = dbconn.dbConnect()){
                            int min = 100;  
                            int max = 1000000000;
                            String staticNum = "100";
                            String pin = staticNum+(int)(Math.random()*(max-min+1)+min);
                            if (pin.length()>6){
                                     String newPin = pin.substring(0, 6);
                                        String fullname = tokens[1] +" "+ tokens[2];
                                        String qry = "INSERT INTO users (phone, userType, firstName, lastName, bvn) VALUES(?,?,?,?,?)";
                                        PreparedStatement stmt3 = connect.prepareStatement(qry);

                                        stmt3.setString(1,phone);
                                        stmt3.setString(2, userType);
                                        stmt3.setString(3,tokens[1]);
                                        stmt3.setString(4,tokens[2]);
                                        stmt3.setString(5,bvn);
                                        while(!stmt3.execute()){
                                            try(Connection con = dbconn.dbConnect()){
                                                String sql = "INSERT INTO customer (phone,firstName,lastName, bvn, pin) VALUES (?,?,?,?,?)";
                                                PreparedStatement stmt = con.prepareStatement(sql);
                                                stmt.setString(1,phone);
                                                stmt.setString(2,tokens[1]);
                                                stmt.setString(3,tokens[2]);
                                                stmt.setString(4,bvn);
                                                stmt.setString(5, newPin);

                                                while(!stmt.execute()){
                                                    try(Connection connecting = dbconn.dbConnect()){
                                                        String sequl = "INSERT INTO cwallet (phone, fullname) VALUES (?,?)";
                                                        PreparedStatement state = connecting.prepareStatement(sequl);
                                                        state.setString(1, phone);
                                                        state.setString(2, fullname);
                                                        state.execute();
                                                        return "Registration successfully";
                                                    }
                                                }
                                            } catch (ClassNotFoundException | SQLException ex) {
                                                Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                                                return "Customer account creation failed! pls try again";
                                            }
                                        }

                                }
                            
                            }
                             catch (ClassNotFoundException | SQLException ex) {
                                Logger.getLogger(SciPaySMS.class.getName()).log(Level.SEVERE, null, ex);
                                return "Account creation 2 failed! pls try again "+ ex.getMessage();
                            }
                    

            }else{
                return "Phone number already Registered. Pls try another phone number";
            }
                
            return  "enrollment successful. To make payment, send: pay `merchant code` amount. "
                    + "To check wallet balance, send : retrieve `pin`. "
                    + "To reset pin, send: reset `old pin` `new pin`";
    } 
}
