package sap.ass2.usergui.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.*;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import sap.ass2.usergui.domain.*;
import sap.ass2.usergui.library.EbikesManagerProxy;
import sap.ass2.usergui.library.EbikesManagerRemoteAPI;
import sap.ass2.usergui.library.RegistryProxy;
import sap.ass2.usergui.library.RidesManagerProxy;
import sap.ass2.usergui.library.RidesManagerRemoteAPI;
import sap.ass2.usergui.library.UsersManagerProxy;
import sap.ass2.usergui.library.UsersManagerRemoteAPI;


public class UserGUI extends JFrame implements ActionListener {
    private JButton startRideButton; // Button to start a ride.
    private JButton endRideButton; // Button to end a ride.
    private JLabel userCreditLabel; // Label to display user credits.
    private JTextField creditRechargeTextField; // Input field for credit recharge.
    private JButton creditRechargeButton; // Button to recharge credits.
    private JButton loginButton; // Button for user login.
    private JButton registerUserButton; // Button to register a new user.
    private JComboBox<String> userDropdown; // Dropdown for selecting users.
    private JPanel mainPanel; // Main panel to hold different views.
    private CardLayout cardLayout; // Layout manager for switching views.
    private User currentUser; // Currently connected user information.
    private Ride launchedRide; // Information about the current ride.
    private UsersManagerRemoteAPI userManager;
    private RidesManagerRemoteAPI ridesManager;
    private EbikesManagerRemoteAPI ebikesManager;
    private List<User> availableUsers;

    public UserGUI(UsersManagerProxy usersManager, RidesManagerProxy ridesManager, EbikesManagerProxy ebikesManager) {
        this.userManager = usersManager;
        this.ridesManager = ridesManager;
        this.ebikesManager = ebikesManager;
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
        this.userManager.getAllUsers().onSuccess(users -> {
            availableUsers = users.stream().map(obj -> jsonObjToUser((JsonObject)obj)).collect(Collectors.toList()); 
            userDropdown.setModel(new DefaultComboBoxModel<String>(availableUsers.stream().map(User::id).collect(Vector<String>::new, Vector::add, Vector::addAll)));
        });
        loginButton = new JButton("LOGIN"); // Login button.
        registerUserButton = new JButton("NEW USER"); // Register button.
        loginButton.addActionListener(this); // Add action listener for login.
        registerUserButton.addActionListener(this); // Add action listener for registration.
        userSelectionPanel.add(userDropdown); // Add dropdown to panel.
        userSelectionPanel.add(loginButton); // Add login button to panel.
        userSelectionPanel.add(registerUserButton); // Add register button to panel.

        // Panel for ride operations.
        JPanel ridePanel = new JPanel();
        startRideButton = new JButton("Start Ride"); // Button to start ride.
        startRideButton.addActionListener(this); // Add action listener for starting ride.
        endRideButton = new JButton("End Ride"); // Button to end ride.
        endRideButton.addActionListener(this); // Add action listener for ending ride.
        endRideButton.setEnabled(false); // Initially disable end ride button.
        creditRechargeButton = new JButton("RECHARGE"); // Button for credit recharge.
        creditRechargeButton.addActionListener(this); // Add action listener for recharge.
        creditRechargeTextField = new JTextField(); // Text field for credit input.
        creditRechargeTextField.setColumns(2); // Set width of text field.
        userCreditLabel = new JLabel("Credit: "); // Label to show user credits.
        ridePanel.add(startRideButton); // Add start ride button to panel.
        ridePanel.add(endRideButton); // Add end ride button to panel.
        ridePanel.add(userCreditLabel); // Add credit label to panel.
        ridePanel.add(creditRechargeTextField); // Add recharge text field to panel.
        ridePanel.add(creditRechargeButton); // Add recharge button to panel.

        // Add panels to the main panel.
        mainPanel.add(userSelectionPanel, "UserSelection"); // Add user selection panel.
        mainPanel.add(ridePanel, "RidePanel"); // Add ride panel.

        cardLayout.show(mainPanel, "UserSelection"); // Show user selection panel initially.

        add(mainPanel, BorderLayout.CENTER); // Add main panel to the frame.

        // Window listener to handle window closing event.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(-1); // Exit the application on window close.
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
        if (e.getSource() == startRideButton) {
            JDialog d;
            try {
                // Open ride dialog to start a ride.
                d = new RideDialog(this, this.userConnected.userID(), this.userService);
                d.setVisible(true);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource() == creditRechargeButton) {
            try {
                // Recharge user credits and update the display label.
                this.userService.rechargeCredit(this.userConnected.userID(), Integer.parseInt(creditRechargeTextField.getText()));
                userCreditLabel.setText("Credit: " + this.userConnected.credits());
            } catch (NumberFormatException | RemoteException | RepositoryException e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource() == endRideButton) {
            try {
                // End the current ride.
                this.userService.endRide(launchedRide.userID(), launchedRide.bikeID());
            } catch (RemoteException | RepositoryException e1) {
                e1.printStackTrace();
            }
            // Update button states after ending ride.
            this.startRideButton.setEnabled(true);
            this.endRideButton.setEnabled(false);
        } else if (e.getSource() == loginButton) {
            try {
                // Log in the selected user and update credits display.
                this.userConnected = this.userService.logAsUser((String) userDropdown.getSelectedItem());
                userCreditLabel.setText("Credit: " + this.userConnected.credits());
                this.pack(); // Adjust size after login.
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }

            cardLayout.show(mainPanel, "RidePanel"); // Switch to ride panel.
        } else if (e.getSource() == registerUserButton) {
            // Open dialog for user registration.
            JDialog registerDialog = new JDialog(this, "CREATE NEW USER", true);
            registerDialog.setSize(300, 150);
            registerDialog.setLayout(new GridLayout(3, 1));

            JTextField newUserField = new JTextField(); // Input field for new user ID.
            JButton confirmButton = new JButton("REGISTER"); // Confirm button for registration.

            confirmButton.addActionListener(ev -> {
                UserInfo newUserToBeChecked;
                String newUser = newUserField.getText(); // Get user input.
                if (!newUser.isEmpty()) {
                    try {
                        // Attempt to create a new user.
                        newUserToBeChecked = this.userService.createUser(newUser, 100);
                        if (newUserToBeChecked == null) {
                            // Show error if user creation fails.
                            JOptionPane.showMessageDialog(this, "Error when attempting to create your account", "ERROR", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (RemoteException | RepositoryException e1) {
                        e1.printStackTrace();
                    }

                    userDropdown.addItem(newUser); // Add new user to dropdown.
                    registerDialog.dispose(); // Close the registration dialog.
                }
            });

            // Add components to the registration dialog.
            registerDialog.add(new JLabel("INSERT USER-ID:"));
            registerDialog.add(newUserField);
            registerDialog.add(confirmButton);

            registerDialog.setVisible(true); // Show the registration dialog.
        }
    }

    @Override
    public void notifyBikeStateChanged(String bikeID, EBikeState state, double x, double y, int batteryLevel) {
        // Update GUI based on bike state changes.
        if (launchedRide == null || !launchedRide.bikeID().equals(bikeID)) {
            return; // Ignore if the ride is not active for the given bike ID.
        }

        switch (state) {
            case AVAILABLE:
                // Handle bike becoming available.
                if (userConnected.credits() <= 0) {
                    System.out.println("Ride ended due to lack of credits."); // Notify lack of credits.
                }
                this.launchedRide = null; // Reset launched ride.

                // Enable start ride button and disable end ride button.
                this.startRideButton.setEnabled(true);
                this.endRideButton.setEnabled(false);
                break;
            case MAINTENANCE:
                // Handle bike maintenance state.
                System.out.println("Bike ran out of battery.");
                this.launchedRide = null; // Reset launched ride.
                
                // Enable start ride button and disable end ride button.
                this.startRideButton.setEnabled(true);
                this.endRideButton.setEnabled(false);
                break;
            default:
                break;
        }
	}

	@Override
	public void notifyUserCreditRecharged(String userID, int credits) {
		// Update user information when credits are recharged.
		this.userConnected = new UserInfo(userID, credits);
        this.userCreditLabel.setText("Credits: " + credits); // Update credits label.
        pack(); // Adjust frame size.
	}

	@Override
	public void notifyRideStepDone(String rideId, double x, double y, int batteryLevel, int userCredits) {
		// Update the GUI with ride progress information.
		System.out.println("Bike moving, pos: (" + x + ", " + y + "), battery level: " + batteryLevel + ", credits left: " + userCredits);
        this.userConnected = new UserInfo(this.userConnected.userID(), userCredits); // Update user credits.
        this.userCreditLabel.setText("Credits: " + userCredits); // Update credits label.
        pack(); // Adjust frame size.
	}

    public void setLaunchedRide(RideInfo newRide) {
        // Set the current ride and update button states.
        this.launchedRide = newRide;
        this.startRideButton.setEnabled(false); // Disable start button.
        this.endRideButton.setEnabled(true); // Enable end button.
    }
}
