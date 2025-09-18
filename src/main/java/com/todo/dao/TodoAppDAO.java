package com.todo.dao;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.todo.model.Todo;
import com.todo.util.DatabaseConnection;

public class TodoAppDAO {
    private static final String SELECT_ALL_TODOS = "SELECT * FROM todos order by created_at DESC";
    private static final String INSERT_TODO="Insert INTO todos(title,description,completed,created_at,updated_at) VALUES(?,?,?,?,?)";
    //create a new todo
    // public int createtodo(Todo todo)
    // {
    //     try(
    //         Connection conn=DatabaseConnection.getDBConnection();
    //     )
    // }

    private Todo getTodoRow(ResultSet rs) throws SQLException
    {
        int id=rs.getInt("id");
        String title=rs.getString("title");
        String description=rs.getString("description");
        boolean completed=rs.getBoolean("completed");
        LocalDateTime createdAt=rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt=rs.getTimestamp("updated_at").toLocalDateTime();
        return new Todo(id, title, description, completed, createdAt, updatedAt);   
    }
    public List<Todo> getAllTodos() throws SQLException
    {
        List<Todo> todos=new ArrayList<>();
        try(Connection conn=DatabaseConnection.getDBConnection();
            PreparedStatement stmt=conn.prepareStatement("SELECT * from todos ORDER BY created_at DESC");
            ResultSet res=stmt.executeQuery();
        )
        {
            while(res.next())
            {
                todos.add(getTodoRow(res));
            }
        }
        catch(SQLException e) {
            System.err.println("Error fetching todos: "+e.getMessage());
            throw e; // rethrow the exception after logging
        }
        return todos;
    }
}