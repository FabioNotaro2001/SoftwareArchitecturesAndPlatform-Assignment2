package sap.ass2.usergui.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

/**
 * Dialog for starting a ride on an E-Bike.
 */
public class RideDialog extends JDialog {
    private JComboBox<String> bikesComboBox; // Dropdown for selecting available bikes.
    private JButton startButton; // Button to start the ride.
    private JButton cancelButton; // Button to cancel the dialog.
    private UserGUI fatherUserGUI; // Reference to the parent UserGUI.
    private String userRiding; // User ID of the rider.
    private List<String> availableBikes; // List of available bike IDs.
    private String bikeSelectedID; // Selected bike ID.
    private UserService userService; // Service for user operations.

    public RideDialog(UserGUI fatherUserGUI, String user, UserService userService) throws RemoteException {
        super(fatherUserGUI, "Start Riding an EBike", true);
        this.userService = userService;
        // Get available bikes and map to bike IDs.
        this.availableBikes = this.userService.getAvailableBikes().stream().map(b -> b.bikeID()).toList();
        initializeComponents(); // Initialize UI components.
        setupLayout(); // Setup layout for the dialog.
        pack(); // Resize dialog to fit components.
        addEventHandlers(); // Add action listeners for buttons.
        setLocationRelativeTo(fatherUserGUI); // Center dialog relative to parent.
        this.fatherUserGUI = fatherUserGUI; // Set parent GUI.
        this.userRiding = user; // Set user ID of the rider.
    }

    private void initializeComponents() {
        // Initialize the bike selection dropdown and buttons.
        bikesComboBox = new JComboBox<String>(new Vector<>(this.availableBikes));
        startButton = new JButton("Start Riding");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        // Setup the layout for input and button panels.
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("E-Bike to ride:"));
        inputPanel.add(bikesComboBox);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout(10, 10)); // Set main layout.
        add(inputPanel, BorderLayout.CENTER); // Add input panel to center.
        add(buttonPanel, BorderLayout.SOUTH); // Add button panel to bottom.
    }

    private void addEventHandlers() {
        // Action listener for the start button.
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bikeSelectedID = bikesComboBox.getSelectedItem().toString(); // Get selected bike ID.
                cancelButton.setEnabled(false); // Disable cancel button.
                try {
                    // Start the ride and set launched ride in parent GUI.
                    fatherUserGUI.setLaunchedRide(userService.beginRide(userRiding, bikeSelectedID));
                } catch (RemoteException | IllegalArgumentException | IllegalStateException | RepositoryException e1) {
                    e1.printStackTrace(); // Handle exceptions.
                }
                dispose(); // Close the dialog.
            }
        });
        
        // Action listener for the cancel button.
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the dialog.
            }
        });
    }
}
