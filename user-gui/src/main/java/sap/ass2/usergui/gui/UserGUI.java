package sap.ass2.usergui.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import io.vertx.core.json.JsonObject;
import sap.ass2.usergui.domain.*;
import sap.ass2.usergui.library.ApplicationAPI;
import sap.ass2.usergui.library.RideEventObserver;
import sap.ass2.usergui.library.UserEventObserver;


public class UserGUI extends JFrame implements ActionListener, UserEventObserver, RideEventObserver {
    private JButton startRideButton;                // Button to start a ride.
    private JButton endRideButton;                  // Button to end a ride.
    private JLabel userCreditLabel;                 // Label to display user credits.
    private JTextField creditRechargeTextField;     // Input field for credit recharge.
    private JButton creditRechargeButton;           // Button to recharge credits.
    private JButton loginButton;                    // Button for user login.
    private JButton registerUserButton;             // Button to register a new user.
    private JComboBox<String> userDropdown;         // Dropdown for selecting users.
    private JPanel mainPanel;                       // Main panel to hold different views.
    private CardLayout cardLayout;                  // Layout manager for switching views.
    private User selectedUser;                      // Currently selected user.
    private Ride launchedRide;                      // Information about the current ride.
    private ApplicationAPI app;
    private List<User> availableUsers;

    public UserGUI(ApplicationAPI app) {
        this.app = app;
        setupView(); // Set up the view.
    }

    private static User jsonObjToUser(JsonObject obj){
        return new User(obj.getString("userId"), obj.getInteger("credit"));
    }

    protected void setupView() {
        setTitle("USER GUI");        
        setSize(800, 300); // Set the size of the GUI.
        setResizable(false); // Disable resizing of the window.
        setLayout(new BorderLayout()); // Use BorderLayout for the main layout.
        cardLayout = new CardLayout(); // Initialize CardLayout for switching panels.
        mainPanel = new JPanel(cardLayout); // Create the main panel with CardLayout.

        // Panel for user selection (login/registration).
        JPanel userSelectionPanel = new JPanel();

        userDropdown = new JComboBox<>(); // Dropdown for user selection.
        this.app.users().getAllUsers()
            .onSuccess(users -> {
                availableUsers = users.stream().map(obj -> jsonObjToUser((JsonObject)obj)).collect(Collectors.toList()); 
                userDropdown.setModel(new DefaultComboBoxModel<String>(availableUsers.stream().map(User::id).collect(Vector<String>::new, Vector::add, Vector::addAll)));
            })
            .onFailure(ex -> {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            });
        
        loginButton = new JButton("LOGIN");             // Login button.
        registerUserButton = new JButton("NEW USER");   // Register button.
        loginButton.addActionListener(this);            // Add action listener for login.
        registerUserButton.addActionListener(this);     // Add action listener for registration.
        userSelectionPanel.add(userDropdown);           // Add dropdown to panel.
        userSelectionPanel.add(loginButton);            // Add login button to panel.
        userSelectionPanel.add(registerUserButton);     // Add register button to panel.

        // Panel for ride operations.
        JPanel ridePanel = new JPanel();
        startRideButton = new JButton("Start Ride");    // Button to start ride.
        startRideButton.addActionListener(this);        // Add action listener for starting ride.
        endRideButton = new JButton("End Ride");        // Button to end ride.
        endRideButton.addActionListener(this);          // Add action listener for ending ride.
        endRideButton.setEnabled(false);                // Initially disable end ride button.
        creditRechargeButton = new JButton("RECHARGE"); // Button for credit recharge.
        creditRechargeButton.addActionListener(this);   // Add action listener for recharge.
        creditRechargeTextField = new JTextField();     // Text field for credit input.
        creditRechargeTextField.setColumns(2);          // Set width of text field.
        userCreditLabel = new JLabel("Credit: ");       // Label to show user credits.
        ridePanel.add(startRideButton);                 // Add start ride button to panel.
        ridePanel.add(endRideButton);                   // Add end ride button to panel.
        ridePanel.add(userCreditLabel);                 // Add credit label to panel.
        ridePanel.add(creditRechargeTextField);         // Add recharge text field to panel.
        ridePanel.add(creditRechargeButton);            // Add recharge button to panel.

        // Add panels to the main panel.
        mainPanel.add(userSelectionPanel, "UserSelection"); // Add user selection panel.
        mainPanel.add(ridePanel, "RidePanel");              // Add ride panel.

        cardLayout.show(mainPanel, "UserSelection");         // Show user selection panel initially.

        add(mainPanel, BorderLayout.CENTER);                // Add main panel to the frame.

        // Window listener to handle window closing event.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(-1);                            // Exit the application on window close.
            }
        });

        pack(); // Adjust frame size to fit contents.
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true); // Set the GUI visible on the Event Dispatch Thread.
        });
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle button actions.
        if (e.getSource() == registerUserButton) {
            // Open dialog for user registration.
            JDialog registerDialog = new JDialog(this, "CREATE NEW USER", true);
            registerDialog.setSize(300, 150);
            registerDialog.setLayout(new GridLayout(3, 1));

            JTextField newUserField = new JTextField(); // Input field for new user ID.
            JButton confirmButton = new JButton("REGISTER"); // Confirm button for registration.

            confirmButton.addActionListener(ev -> {
                String newUserId = newUserField.getText(); // Get user input.
                if (!newUserId.isEmpty()) {
                    // Attempt to create a new user.
                    this.app.users().createUser(newUserId)
                        .onSuccess(user -> {
                            var newUser = jsonObjToUser(user);
                            this.availableUsers.add(newUser);
                            userDropdown.addItem(newUser.id());     // Add the new user to the dropdown.
                            registerDialog.dispose();
                        })
                        .onFailure(ex -> {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                        });
                }
            });

            // Add components to the registration dialog.
            registerDialog.add(new JLabel("INSERT USER-ID:"));
            registerDialog.add(newUserField);
            registerDialog.add(confirmButton);

            registerDialog.setVisible(true); // Show the registration dialog.
        } else if (e.getSource() == loginButton) {
            // Log in the selected user and update credits display.
            this.selectedUser = this.availableUsers.get(userDropdown.getSelectedIndex());
            userCreditLabel.setText("Credit: " + this.selectedUser.credit());

            this.app.users().subscribeToUserEvents(this.selectedUser.id(), this);

            cardLayout.show(mainPanel, "RidePanel"); // Switch to ride panel.
            this.pack(); // Adjust size after login.
        } else if (e.getSource() == creditRechargeButton) {
            this.app.users().rechargeCredit(selectedUser.id(), Integer.parseInt(creditRechargeTextField.getText()))
                .onFailure(ex -> {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                });
        } else if (e.getSource() == startRideButton) {
            JDialog d;
            // Opens the ride dialog to start a ride.
            d = new RideDialog(this, this.selectedUser.id(), this.app);
            d.setVisible(true);
        } else if (e.getSource() == endRideButton) {
            // Ends the current ride.
            this.app.rides().stopRide(launchedRide.rideId(), launchedRide.userId())
                .onFailure(ex -> {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                });
        }
    }

    public void setLaunchedRide(Ride newRide) {
        // Set the current ride and update button states.
        this.launchedRide = newRide;
        this.startRideButton.setEnabled(false); // Disable start button.
        this.endRideButton.setEnabled(true); // Enable end button.

        this.app.rides().subscribeToRideEvents(this.launchedRide.rideId(), this);
        System.out.println("Ride started.");
    }

    @Override
    public void userUpdated(String userID, int credit) {
        this.selectedUser = this.selectedUser.updateCredit(credit);
        // Technically the list should also be updated but we have no logout so it doesn't matter.
        this.userCreditLabel.setText("Credits: " + credit);
        this.pack();
    }

    @Override
    public void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel) {
        System.out.println("Bike movement: [x -> " + x + "], [y ->" + y + "], [battery -> " + batteryLevel + "]");
    }

    @Override
    public void rideEnded(String rideID, String reason) {
        System.out.println("Ride ended.");
        JOptionPane.showMessageDialog(this, reason, "Info", JOptionPane.INFORMATION_MESSAGE);
        this.launchedRide = null; // Reset launched ride.
                
        // Enable start ride button and disable end ride button.
        this.startRideButton.setEnabled(true);
        this.endRideButton.setEnabled(false);

        this.app.rides().unsubscribeFromRideEvents();
    }
}
