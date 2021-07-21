package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    
      public Connection dbConnect() throws ClassNotFoundException, SQLException{
        String url = "jdbc:mysql://localhost:3306/scipay?useSSL=false&serverTimezone=Africa/Lagos";
        String user = "root";
//        String password = "samuel";
       String password = "Prescotcruzy9";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }
    
}
