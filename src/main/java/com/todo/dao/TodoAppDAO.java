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
import java.sql.Statement;

public class TodoAppDAO {
    private static final String SELECT_ALL_TODOS = "SELECT * FROM todos order by id ASC";
    private static final String INSERT_TODO="Insert INTO todos(title,description,completed,created_at,updated_at) VALUES(?,?,?,?,?)";
    private static final String SELECT_TODO_BY_ID = "SELECT * FROM todos WHERE id=?";
    private static final String UPDATE_TODO = "UPDATE todos SET title=?, description=?, completed=?, updated_at=? WHERE id=?";
    private static final  String DELETE_TODO = "DELETE FROM todos WHERE id=?";
    private static final  String FILTER_TODOS = "SELECT * FROM todos WHERE completed=? ORDER BY created_at ASC";

    public int createTodo(Todo todo) throws SQLException {
        DatabaseConnection dbConn = new DatabaseConnection();
        try (
            Connection conn = dbConn.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(INSERT_TODO, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setBoolean(3, todo.isCompleted());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(todo.getCreated_at()));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(todo.getUpdated_at()));
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create todo");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated key");
                }
            }
        }
    }

    public List<Todo> filterTodos(boolean isCompleted) throws SQLException {
        List<Todo> todos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(FILTER_TODOS)) {
            stmt.setBoolean(1, isCompleted);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(getTodoRow(rs));
                }
            }
        }
        return todos;
    } 

    public Todo getTodoById(int todoId) throws SQLException {
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_TODO_BY_ID)) {
            stmt.setInt(1, todoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getTodoRow(rs);
                } else {
                    return null; // No todo found with the given ID
                } 
            }
        }
    }

    public boolean updateTodo(Todo todo) throws SQLException {
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_TODO)) {
            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setBoolean(3, todo.isCompleted());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5,todo.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected> 0;
        }
    } 
    public boolean deleteTodo(int todoId) throws SQLException {
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_TODO)) {
            stmt.setInt(1, todoId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
        catch(SQLException e){
            System.err.println("Error deleting todo: "+e.getMessage());
            throw e;
        }
    }

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
            PreparedStatement stmt=conn.prepareStatement(SELECT_ALL_TODOS);
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