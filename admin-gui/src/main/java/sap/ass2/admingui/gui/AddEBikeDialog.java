package sap.ass2.admingui.gui;

import javax.swing.*;

import sap.ass2.admingui.library.ApplicationAPI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for adding a new E-Bike.
 * This class provides a GUI for entering the E-Bike ID and its location coordinates,
 * with options to confirm or cancel the addition.
 */
public class AddEBikeDialog extends JDialog {

    private JTextField idField;     // Field for E-Bike ID input.
    private JTextField xCoordField; // Field for X coordinate input.
    private JTextField yCoordField; // Field for Y coordinate input.
    private JButton okButton;       // Button to confirm E-Bike addition.
    private JButton cancelButton;   // Button to cancel the addition.
    private ApplicationAPI app;     // Service to handle E-Bike operations.

    /**
     * Constructor for AddEBikeDialog.
     * Initializes the dialog with the specified owner and AdminService.
     *
     * @param owner The parent AdminGUI that owns this dialog.
     * @param adminService The service to manage E-Bikes.
     */
    public AddEBikeDialog(AdminGUI owner, ApplicationAPI app) {
        super(owner, "Adding E-Bike", true);    // Set dialog title and modality.
        this.app = app; 
        initializeComponents();                 // Initialize UI components.
        setupLayout();                          // Set up the layout of the dialog.
        addEventHandlers();                     // Register action listeners for buttons.
        pack();                                 // Size the dialog to fit its contents.
        setLocationRelativeTo(owner);           // Center dialog relative to the owner.
    }

    /**
     * Initialize the components of the dialog.
     * Creates text fields and buttons for user input.
     */
    private void initializeComponents() {
        idField = new JTextField(15);           // Text field for entering E-Bike ID.
        xCoordField = new JTextField(15);       // Text field for entering X coordinate.
        yCoordField = new JTextField(15);       // Text field for entering Y coordinate.
        okButton = new JButton("OK");           // Button to confirm addition.
        cancelButton = new JButton("Cancel");   // Button to cancel the operation.
    }

    /**
     * Set up the layout for the dialog.
     * Arranges components in a grid for input and a panel for buttons.
     */
    private void setupLayout() {
        // Create a panel for input fields with a grid layout.
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("E-Bike ID:")); // Label for E-Bike ID input.
        inputPanel.add(idField); // Add the E-Bike ID input field.
        inputPanel.add(new JLabel("E-Bike location - X coord:")); // Label for X coordinate.
        inputPanel.add(xCoordField); // Add the X coordinate input field.
        inputPanel.add(new JLabel("E-Bike location - Y coord:")); // Label for Y coordinate.
        inputPanel.add(yCoordField); // Add the Y coordinate input field.

        // Create a panel for buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton); // Add OK button.
        buttonPanel.add(cancelButton); // Add Cancel button.

        // Set the main layout for the dialog and add components.
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.CENTER); // Add input panel to the center.
        add(buttonPanel, BorderLayout.SOUTH); // Add button panel to the bottom.
    }

    /**
     * Add event handlers for button actions.
     * Defines what happens when buttons are clicked.
     */
    private void addEventHandlers() {
        // Handle OK button click.
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve input values.
                String id = idField.getText();
                String xCoord = xCoordField.getText();
                String yCoord = yCoordField.getText();

                app.ebikes().createEbike(id, Integer.parseInt(xCoord), Integer.parseInt(yCoord));
                dispose(); // Close the dialog after adding the E-Bike.
            }
        });

        // Handle Cancel button click.
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the dialog without making any changes.
            }
        });
    }
}
