package main.java.com.todo;
import java.sql.SQLException;
import com.todo.util.DatabaseConnection;
import java.sql.Connection;
public class Main{
    public static void main(String args[]){
        DatabaseConnection db_Connection = new DatabaseConnection();
        try{
            Connection cn= db_Connection.getDBConnection();
            System.out.println("Connected to the database");
        }
        catch(SQLException e){
            System.out.println("Failed to connect to the database" + e.getMessage());
        }
    }
}