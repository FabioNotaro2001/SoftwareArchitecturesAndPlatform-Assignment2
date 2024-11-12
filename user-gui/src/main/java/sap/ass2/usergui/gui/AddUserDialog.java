package sap.ass2.usergui.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for adding a new user.
 * This class provides a GUI for entering a user ID, 
 * with options to confirm or cancel the action.
 */
public class AddUserDialog extends JDialog {

    private JTextField idField; // Field for user ID input.
    private JTextField errorField; // Field for displaying error messages.
    private JButton okButton; // Button to confirm user addition.
    private JButton cancelButton; // Button to cancel the addition.
    private AdminGUI app; // Reference to the admin GUI.

    /**
     * Constructor for the AddUserDialog.
     * Initializes the dialog with the specified owner.
     *
     * @param owner The parent AdminGUI that owns this dialog.
     */
    public AddUserDialog(AdminGUI owner) {
        super(owner, "Adding User", true); // Set dialog title and modality.
        app = owner; // Store reference to the main application.
        initializeComponents(); // Initialize UI components.
        setupLayout(); // Set up the layout of the dialog.
        addEventHandlers(); // Register action listeners for buttons.
        pack(); // Size the dialog to fit its contents.
        setLocationRelativeTo(owner); // Center dialog relative to the owner.
    }

    /**
     * Initialize the components of the dialog.
     * Creates text fields and buttons for user input.
     */
    private void initializeComponents() {
        idField = new JTextField(15); // Text field for entering User ID.
        errorField = new JTextField(25); // Text field for error messages (if needed).
        okButton = new JButton("OK"); // Button to confirm addition.
        cancelButton = new JButton("Cancel"); // Button to cancel the operation.
    }

    /**
     * Set up the layout for the dialog.
     * Arranges components in a grid for input and a panel for buttons.
     */
    private void setupLayout() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10)); // Grid layout for input.
        inputPanel.add(new JLabel("User ID:")); // Label for User ID input.
        inputPanel.add(idField); // Add the User ID input field.

        JPanel buttonPanel = new JPanel(); // Panel for buttons.
        buttonPanel.add(okButton); // Add OK button.
        buttonPanel.add(cancelButton); // Add Cancel button.

        setLayout(new BorderLayout(10, 10)); // Set the layout for the dialog.
        add(inputPanel, BorderLayout.CENTER); // Add input panel to the center.
        add(buttonPanel, BorderLayout.SOUTH); // Add button panel to the bottom.
    }

    /**
     * Add event handlers for button actions.
     * Defines what happens when buttons are clicked.
     */
    private void addEventHandlers() {
        // Handle OK button click
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText(); // Retrieve the User ID input.
                // Here you could add code to validate the User ID and add it to the application.
                dispose(); // Close the dialog.
            }
        });

        // Handle Cancel button click
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the dialog without making any changes.
            }
        });
    }
}
