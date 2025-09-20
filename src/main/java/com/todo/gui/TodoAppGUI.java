package com.todo.gui;
import com.todo.dao.TodoAppDAO;
import com.todo.model.Todo;
import com.todo.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TodoAppGUI extends JFrame{
    private TodoAppDAO todoDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String>filterComboBox;

    public TodoAppGUI(){
        this.todoDAO=new TodoAppDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();  
        loadTodos();  
    }
    private void initializeComponents(){
        setTitle("Todo Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setLocationRelativeTo(null);//to make the position of the application in center
        String [] columnNames={"ID","Title","Description","Completed","Created At","Updated At"};        tableModel=new DefaultTableModel(columnNames,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        todoTable=new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(
            (e)->{ // e contains the selcted row
                if(!e.getValueIsAdjusting()){
                    loadSelectedtodo();

                }
            }
        );
        titleField=new JTextField(20);

        descriptionArea=new JTextArea(3,20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        completedCheckBox=new JCheckBox("Completed");

        addButton=new JButton("Add Todo");
        updateButton=new JButton("Update Todo");
        deleteButton=new JButton("Delete Todo");
        refreshButton=new JButton("Refresh Todo");

        String []filterOptions={"All","Completed","Pending"};
        filterComboBox=new JComboBox<>(filterOptions);
        filterComboBox.addActionListener((e)->{
            filterTodos();
        });

    }
    
    private void setupLayout(){
        setLayout(new BorderLayout());  
        JPanel inputPanel=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets =  new Insets(5,5,5,5);//padding(attribute of gbc-inset expects a obj of class Insets)
        gbc.gridx=0;  //0 th row 
        gbc.gridy=0; //0 th column
        gbc.anchor=GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Title"),gbc);
        gbc.gridx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        inputPanel.add(titleField,gbc);
        gbc.gridx=0;
        gbc.gridy=1;
        inputPanel.add(new JLabel("Description"),gbc);
        gbc.gridx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JScrollPane(descriptionArea),gbc);
        gbc.gridx=1;
        gbc.gridy=2;
        inputPanel.add(completedCheckBox,gbc);
        JPanel buttonPanel =new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputPanel,BorderLayout.CENTER);
        northPanel.add(buttonPanel,BorderLayout.SOUTH);
        northPanel.add(filterPanel,BorderLayout.NORTH);

        add(northPanel,BorderLayout.NORTH);
        add(new JScrollPane(todoTable),BorderLayout.CENTER);
        JPanel statusPanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Select a todo to Edit or Delete"));
        add(statusPanel,BorderLayout.SOUTH);

    }

    private void setupEventListeners(){
        addButton.addActionListener((e)->{addTodo();});
        updateButton.addActionListener((e)->{updateTodo();});
        deleteButton.addActionListener((e)->{deleteTodo();});
        refreshButton.addActionListener((e)->{refreshTodo();});

    }
    private void addTodo() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();
        Todo todo = new Todo(title, description, completed);

        if(title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Title for the Todo", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = todoDAO.createTodo(todo);
            todo.setCompleted(completed);
            todo.setId(id);
            JOptionPane.showMessageDialog(this, "Todo created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating todo: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        loadTodos();
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckBox.setSelected(false);
    }

    private void deleteTodo(){
        int row = todoTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a todo to delete",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this todo?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) todoTable.getValueAt(row, 0);
            try {
                boolean deleted = todoDAO.deleteTodo(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "Todo deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTodos(); // refresh table
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete todo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting todo: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void updateTodo(){
        int row=todoTable.getSelectedRow();
        if(row==-1){
            JOptionPane.showMessageDialog(this, "Please select a todo to update", "Validation Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title=titleField.getText().trim();
        if(title.isEmpty()){
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id =(int) todoTable.getValueAt(row,0);
        try{
            Todo todo = todoDAO.getTodoById(id);

            if(todo!=null){
                todo.setTitle(title);
                todo.setDescription(descriptionArea.getText().trim());
                todo.setCompleted(completedCheckBox.isSelected());

                if (todoDAO.updateTodo(todo)) {
                    JOptionPane.showMessageDialog(this, "Todo updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTodos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update todo", "Error", JOptionPane.ERROR_MESSAGE);
                    
                }
            }
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error updating todo: "+e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


    }

    private void refreshTodo(){
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckBox.setSelected(false);
        todoTable.clearSelection();
        loadTodos();
        
    }

    private void filterTodos(){
        String selectedFilter=(String) filterComboBox.getSelectedItem();
        try{
            List<Todo> todos=todoDAO.getAllTodos();
            if(selectedFilter.equals("Completed")){
                todos=todoDAO.filterTodos(true);
            }
            else if(selectedFilter.equals("Pending")){
                todos=todoDAO.filterTodos(false);
            }
            updateTable(todos);
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error filtering todos: "+e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTodos(){
        try{
        List<Todo> todos=todoDAO.getAllTodos();
        updateTable(todos);
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error fetching todos", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    private void loadSelectedtodo(){
        int row = todoTable.getSelectedRow();
        if(row!=-1){
            String title=(String)tableModel.getValueAt(row,1);
            String description=(String)tableModel.getValueAt(row,2);
            boolean completed=(boolean)tableModel.getValueAt(row,3);

            titleField.setText(title);
            descriptionArea.setText(description);
            completedCheckBox.setSelected(completed);

        }
    }

    private void updateTable(List<Todo> todos){
        tableModel.setRowCount(0);
        for(Todo t:todos){
            Object[] row={t.getId(),t.getTitle(),t.getDescription(),t.isCompleted(),t.getCreated_at(),t.getUpdated_at()};
             tableModel.addRow(row);
        }
    }


}