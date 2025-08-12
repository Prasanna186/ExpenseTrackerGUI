import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class ExpenseTrackerGUI extends JFrame {

    private JTextField nameField, categoryField, amountField, searchCategoryField, searchDateField;
    private DefaultTableModel tableModel;
    private JTable expenseTable;
    private Connection conn;

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker - SQLite");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        connectDatabase();
        createTableIfNotExists();

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Expense Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);

        inputPanel.add(new JLabel("Amount ($):"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        JButton addButton = new JButton("Add Expense");
        JButton deleteButton = new JButton("Delete Selected");
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        add(inputPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Date", "Name", "Category", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent table editing
            }
        };

        expenseTable = new JTable(tableModel);
        expenseTable.removeColumn(expenseTable.getColumnModel().getColumn(0)); // hide ID column
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        JButton totalButton = new JButton("Show Total");
        JButton categoryTotalButton = new JButton("Category Totals");
        JButton pieChartButton = new JButton("Show Pie Chart");

        searchCategoryField = new JTextField(8);
        searchDateField = new JTextField(8);
        JButton searchButton = new JButton("Search");
        JButton resetButton = new JButton("Reset");

        bottomPanel.add(totalButton);
        bottomPanel.add(categoryTotalButton);
        bottomPanel.add(pieChartButton);
        bottomPanel.add(new JLabel("Category:"));
        bottomPanel.add(searchCategoryField);
        bottomPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        bottomPanel.add(searchDateField);
        bottomPanel.add(searchButton);
        bottomPanel.add(resetButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadExpenses();

        // Add Expense
        addButton.addActionListener(e -> addExpense());

        // Delete Expense
        deleteButton.addActionListener(e -> deleteSelectedExpense());

        // Show Total
        totalButton.addActionListener(e -> showTotal());

        // Category Totals
        categoryTotalButton.addActionListener(e -> showCategoryTotals());

        // Pie Chart
        pieChartButton.addActionListener(e -> showPieChart());

        // Search
        searchButton.addActionListener(e -> searchExpenses());

        // Reset
        resetButton.addActionListener(e -> {
            searchCategoryField.setText("");
            searchDateField.setText("");
            loadExpenses();
        });
    }

    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:expenses.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        try {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "name TEXT," +
                "category TEXT," +
                "amount REAL)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadExpenses() {
        loadExpenses("", "");
    }

    private void loadExpenses(String categoryFilter, String dateFilter) {
        tableModel.setRowCount(0);
        try {
            StringBuilder query = new StringBuilder("SELECT * FROM expenses WHERE 1=1");
            if (!categoryFilter.isEmpty()) {
                query.append(" AND LOWER(category) LIKE LOWER('%").append(categoryFilter).append("%')");
            }
            if (!dateFilter.isEmpty()) {
                query.append(" AND date LIKE '").append(dateFilter).append("%'");
            }
            query.append(" ORDER BY date DESC");

            ResultSet rs = conn.createStatement().executeQuery(query.toString());
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("date"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("amount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addExpense() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String amountText = amountField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
            PreparedStatement ps = conn.prepareStatement("INSERT INTO expenses(date, name, category, amount) VALUES (?, ?, ?, ?)");
            ps.setString(1, date);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setDouble(4, amount);
            ps.executeUpdate();
            loadExpenses();
            nameField.setText("");
            categoryField.setText("");
            amountField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = expenseTable.convertRowIndexToModel(selectedRow);
            int id = (int) tableModel.getValueAt(modelRow, 0);
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadExpenses();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to delete");
        }
    }

    private void showTotal() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(amount) FROM expenses");
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Total Expenses: $" + rs.getDouble(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showCategoryTotals() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT category, SUM(amount) FROM expenses GROUP BY category");
            StringBuilder result = new StringBuilder("Category Totals:\n");
            while (rs.next()) {
                result.append(rs.getString(1)).append(": $").append(rs.getDouble(2)).append("\n");
            }
            JOptionPane.showMessageDialog(this, result.toString(), "Category Totals", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showPieChart() {
        try {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT category, SUM(amount) FROM expenses GROUP BY category");
            while (rs.next()) {
                dataset.setValue(rs.getString(1), rs.getDouble(2));
            }
            JFreeChart chart = ChartFactory.createPieChart(
                "Expenses by Category",
                dataset,
                true, true, false
            );
            ChartFrame frame = new ChartFrame("Expense Chart", chart);
            frame.pack();
            frame.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchExpenses() {
        String category = searchCategoryField.getText().trim();
        String date = searchDateField.getText().trim();
        loadExpenses(category, date);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTrackerGUI().setVisible(true));
    }
}
