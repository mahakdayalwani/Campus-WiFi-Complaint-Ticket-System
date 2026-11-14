
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CampusWifiComplaintSystem extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField nameField, idField, locationField;
    private JComboBox<String> typeCombo;
    private JTextArea descArea;

    private DefaultTableModel tableModel;
    private JTable complaintTable;

    private JComboBox<String> ticketIdCombo;
    private JComboBox<String> statusCombo;

    public CampusWifiComplaintSystem() {

        setTitle("Campus Wi-Fi Complaint & Ticket System");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel headerLabel = new JLabel(
                "Campus Wi-Fi Complaint & Ticket System",
                JLabel.CENTER
        );

        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setPreferredSize(new Dimension(750, 50));

        add(headerLabel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        navPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        JButton registerBtn = new JButton("Register Complaint");
        JButton viewBtn = new JButton("View Complaints");
        JButton updateBtn = new JButton("Update Ticket Status");
        JButton exitBtn = new JButton("Exit");

        navPanel.add(registerBtn);
        navPanel.add(viewBtn);
        navPanel.add(updateBtn);
        navPanel.add(exitBtn);

        add(navPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createRegisterPanel(), "REGISTER");
        mainPanel.add(createViewPanel(), "VIEW");
        mainPanel.add(createUpdatePanel(), "UPDATE");

        add(mainPanel, BorderLayout.CENTER);

        registerBtn.addActionListener(e ->
                cardLayout.show(mainPanel, "REGISTER")
        );

        viewBtn.addActionListener(e -> {
            refreshTable();
            cardLayout.show(mainPanel, "VIEW");
        });

        updateBtn.addActionListener(e -> {
            refreshTicketIdCombo();
            cardLayout.show(mainPanel, "UPDATE");
        });

        exitBtn.addActionListener(e -> System.exit(0));

        cardLayout.show(mainPanel, "REGISTER");
    }

    // REGISTER COMPLAINT SCREEN

    private JPanel createRegisterPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Student Name:"), gbc);

        nameField = new JTextField(20);

        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(new JLabel("Student ID:"), gbc);

        idField = new JTextField(20);

        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(new JLabel("Room/Location:"), gbc);

        locationField = new JTextField(20);

        gbc.gridx = 1;
        panel.add(locationField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;

        panel.add(new JLabel("Complaint Type:"), gbc);

        String[] types = {
                "No Internet",
                "Slow Speed",
                "Cannot Connect",
                "Other"
        };

        typeCombo = new JComboBox<>(types);

        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;

        panel.add(new JLabel("Description:"), gbc);

        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);

        JScrollPane descScroll = new JScrollPane(descArea);

        gbc.gridx = 1;
        panel.add(descScroll, gbc);

        JButton submitBtn = new JButton("Submit Complaint");

        gbc.gridx = 1;
        gbc.gridy = 5;

        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> submitComplaint());

        return panel;
    }

    // GENERATE TICKET ID

    private String generateTicketId(Connection con) throws SQLException {

        String sql =
                "SELECT MAX(CAST(SUBSTRING(ticketId, 2) AS UNSIGNED)) " +
                        "FROM Complaint";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            rs.next();

            int nextId = rs.getInt(1) + 1;

            return String.format("T%03d", nextId);
        }
    }

    // SAVE COMPLAINT IN DATABASE

    private void submitComplaint() {

        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String location = locationField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String desc = descArea.getText().trim();

        if (name.isEmpty() || id.isEmpty() || desc.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Student Name, Student ID, and Description cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String sql = "INSERT INTO Complaint VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(this,
                        "Database connection failed!");
                return;
            }

            String ticketId = generateTicketId(con);

            try (PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, ticketId);
                pst.setString(2, name);
                pst.setString(3, id);
                pst.setString(4, location);
                pst.setString(5, type);
                pst.setString(6, desc);
                pst.setString(7, "Pending");

                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Complaint submitted successfully! Ticket ID: " + ticketId,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearRegisterFields();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // CLEAR REGISTER FIELDS

    private void clearRegisterFields() {

        nameField.setText("");
        idField.setText("");
        locationField.setText("");

        typeCombo.setSelectedIndex(0);

        descArea.setText("");
    }

    // VIEW COMPLAINTS SCREEN

    private JPanel createViewPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                "Ticket ID",
                "Student Name",
                "Student ID",
                "Location",
                "Complaint Type",
                "Description",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        complaintTable = new JTable(tableModel);

        complaintTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(complaintTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(
                "All Submitted Complaints",
                JLabel.CENTER
        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    // FETCH COMPLAINTS FROM DATABASE

    private void refreshTable() {

        tableModel.setRowCount(0);

        String sql = "SELECT * FROM Complaint";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {

                    tableModel.addRow(new Object[]{

                            rs.getString("ticketId"),
                            rs.getString("studentName"),
                            rs.getString("studentId"),
                            rs.getString("location"),
                            rs.getString("complaintType"),
                            rs.getString("description"),
                            rs.getString("status")

                    });
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading complaints: " + e.getMessage()
            );
        }
    }

    // UPDATE STATUS SCREEN

    private JPanel createUpdatePanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Select Ticket ID:"), gbc);

        ticketIdCombo = new JComboBox<>();

        gbc.gridx = 1;
        panel.add(ticketIdCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(new JLabel("New Status:"), gbc);

        String[] statuses = {
                "Pending",
                "In Progress",
                "Resolved"
        };

        statusCombo = new JComboBox<>(statuses);

        gbc.gridx = 1;
        panel.add(statusCombo, gbc);

        JButton updateBtn = new JButton("Update Status");

        gbc.gridx = 1;
        gbc.gridy = 2;

        panel.add(updateBtn, gbc);

        updateBtn.addActionListener(e -> updateTicketStatus());

        return panel;
    }

    // LOAD TICKET IDS FROM DATABASE

    private void refreshTicketIdCombo() {

        ticketIdCombo.removeAllItems();

        String sql = "SELECT ticketId FROM Complaint";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {

                    ticketIdCombo.addItem(
                            rs.getString("ticketId")
                    );
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading ticket IDs: " + e.getMessage()
            );
        }
    }

    // UPDATE TICKET STATUS IN DATABASE

    private void updateTicketStatus() {

        String selectedId =
                (String) ticketIdCombo.getSelectedItem();

        String newStatus =
                (String) statusCombo.getSelectedItem();

        if (selectedId == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No tickets available to update.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String sql =
                "UPDATE Complaint SET status = ? WHERE ticketId = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return;
            }

            try (PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, newStatus);
                pst.setString(2, selectedId);

                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Ticket " + selectedId +
                            " status updated to " + newStatus,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error updating status: " + e.getMessage()
            );
        }
    }

    // MAIN METHOD

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            CampusWifiComplaintSystem app =
                    new CampusWifiComplaintSystem();

            app.setVisible(true);
        });
    }
}