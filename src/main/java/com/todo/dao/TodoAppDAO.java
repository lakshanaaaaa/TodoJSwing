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
                Todo todo=new Todo();
                todo.setId(res.getInt("id"));
                todo.setTitle(res.getString("title"));
                todo.setDescription(res.getString("description"));
                todo.setCompleted(res.getBoolean("completed"));
                
                LocalDateTime createdAt=res.getTimestamp("created_at").toLocalDateTime();
                todo.setCreated_at(createdAt);
                LocalDateTime updatedAt=res.getTimestamp("updated_at").toLocalDateTime();
                todo.setUpdated_at(updatedAt);

                todos.add(todo); 
            }
        }
        return todos;
    }
}