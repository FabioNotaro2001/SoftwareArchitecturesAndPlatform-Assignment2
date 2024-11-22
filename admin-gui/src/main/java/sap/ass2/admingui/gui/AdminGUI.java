package sap.ass2.admingui.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import io.vertx.core.json.JsonObject;
import sap.ass2.admingui.library.*;
import sap.ass2.admingui.domain.*;


/**
 * Admin GUI for managing E-Bikes, users, and rides.
 */
public class AdminGUI extends JFrame implements ActionListener, UserEventObserver, RideEventObserver, EbikeEventObserver {

	private VisualiserPanel centralPanel; // Panel for visualizing bike locations.
    private JButton addEBikeButton; // Button to add a new E-Bike.
	
	private JList<String> usersList; // List component for displaying users.
	private JList<String> bikesList; // List component for displaying bikes.
	private JList<String> ridesList; // List component for displaying rides.

	private DefaultListModel<String> usersModel; // Model for user list.
	private DefaultListModel<String> bikesModel; // Model for bike list.
	private DefaultListModel<String> ridesModel; // Model for ride list.

	private Map<String, User> users = new HashMap<>(); // Map of users.
	private Map<String, Ebike> bikes = new HashMap<>(); // Map of E-Bikes.
	private Map<String, Ride> rides = new HashMap<>(); // Map of rides.

	private ApplicationAPI app;

    public AdminGUI(ApplicationAPI app) {
		this.app = app;

		this.usersModel = new DefaultListModel<>(); // Create model for user list.
		this.bikesModel = new DefaultListModel<>(); // Create model for bike list.
		this.ridesModel = new DefaultListModel<>(); // Create model for ride list.

		this.setupModel(); // Set up the model.
		this.setupView(); // Set up the view.
    }

	private static User jsonObjToUser(JsonObject obj){
        return new User(obj.getString("userId"), obj.getInteger("credit"));
    }

	private static Ride jsonObjToRide(JsonObject obj){
        return new Ride(obj.getString("rideId"), obj.getString("userId"), obj.getString("bikeId"));
    }

	private static Ebike jsonObjToEbike(JsonObject obj){
        return new Ebike(obj.getString("ebikeId"), EbikeState.valueOf(obj.getString("state")), obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("dirX"), obj.getDouble("dirY"), obj.getDouble("speed"), obj.getInteger("batteryLevel"));
    }
    
    protected void setupModel() {
        var ebikesFut = this.app.ebikes().subscribeToEbikeEvents(this);
		ebikesFut.onSuccess( ebikesArray -> {
			SwingUtilities.invokeLater(() -> {
				this.bikes.putAll(ebikesArray.stream().map(e -> jsonObjToEbike((JsonObject)e)).collect(Collectors.toMap(Ebike::id, ebike -> ebike)));
				this.bikesModel.addAll(bikes.values().stream().map(Ebike::toString).toList());
				this.refreshView();
			});
		}); // Add all ebikes to the model.

        var ridesFut = this.app.rides().subscribeToRideEvents(this);
		ridesFut.onSuccess( ridesArray -> {
			SwingUtilities.invokeLater(() -> {
				this.rides.putAll(ridesArray.stream().map(r -> jsonObjToRide((JsonObject)r)).collect(Collectors.toMap(Ride::rideId, ride -> ride)));
				this.ridesModel.addAll(rides.values().stream().map(Ride::toString).toList());
			});
		}); // Add all rides to the model.

		var usersFut = this.app.users().subscribeToUsersEvents(this);
		usersFut.onSuccess( usersArray -> {
			SwingUtilities.invokeLater(() -> {
				this.users.putAll(usersArray.stream().map(u -> jsonObjToUser((JsonObject)u)).collect(Collectors.toMap(User::id, user -> user))); 
				this.usersModel.addAll(users.values().stream().map(User::toString).toList());
			});
		}); // Add all users to the model.
    }

    protected void setupView() {
        setTitle("ADMIN GUI");        
        setSize(1000,600); // Set the size of the window.
        setResizable(false); // Prevent resizing of the window.
        
        setLayout(new BorderLayout()); // Set layout for the frame.

		addEBikeButton = new JButton("Add EBike"); // Create add bike button.
		addEBikeButton.addActionListener(this); // Add action listener.
		
		JPanel topPanel = new JPanel(); // Panel for top components.
		topPanel.add(addEBikeButton);	
	    add(topPanel, BorderLayout.NORTH); // Add top panel to the north.

        centralPanel = new VisualiserPanel(800, 500, this); // Create visualizer panel.
	    add(centralPanel, BorderLayout.CENTER); // Add visualizer to the center.
	    	    		
		addWindowListener(new WindowAdapter() { // Handle window close events.
			public void windowClosing(WindowEvent ev) {
				System.exit(-1); // Exit the application.
			}
		});

		JPanel eastPanel = new JPanel(); // Panel for side components.
		eastPanel.setLayout(new GridLayout(3, 1)); // Use grid layout for the side panel.
		eastPanel.setPreferredSize(new Dimension(300, 500)); // Set preferred size for the side panel.
		
		this.usersModel = getUsersModel(); // Get user list model.
		this.bikesModel = getBikesModel(); // Get bike list model.
		this.ridesModel = getRidesModel(); // Get ride list model.
		this.usersList = new JList<>(usersModel); // Create user list.
		this.bikesList = new JList<>(bikesModel); // Create bike list.
		this.ridesList = new JList<>(ridesModel); // Create ride list.
		
		// Add lists to the east panel within scroll panes.
		eastPanel.add(new JScrollPane(usersList));
		eastPanel.add(new JScrollPane(bikesList));
		eastPanel.add(new JScrollPane(ridesList));
		add(eastPanel, BorderLayout.EAST); // Add east panel to the right.
    }

	private void addOrReplaceRide(Ride info) {

		// Add or replace a ride in the rides map and update the model.
		var old = rides.put(info.rideId(), info);
		if (old == null) {
			ridesModel.addElement(info.toString()); // Add new ride to model.
		} else {
			ridesModel.clear(); // Clear model and refresh.
			ridesModel.addAll(rides.values().stream().map(Ride::toString).toList());	
		}
	}

	private void addOrReplaceUser(User info) {
		// Add or replace a user in the users map and update the model.
		var old = users.put(info.id(), info);
		if (old == null) {
			usersModel.addElement(info.toString()); // Add new user to model.
		} else {
			usersModel.clear(); // Clear model and refresh.
			usersModel.addAll(users.values().stream().map(User::toString).toList());
		}
	}

	private void addOrReplaceEBike(Ebike info) {
		// Add or replace an E-Bike in the bikes map and update the model.
		var old = bikes.put(info.id(), info);
		if (old == null) {
			bikesModel.addElement(info.toString()); // Add new bike to model.
		} else {
			bikesModel.clear(); // Clear model and refresh.
			bikesModel.addAll(bikes.values().stream().map(Ebike::toString).toList());	
		}
	}

	private void removeRide(String rideId) {
		// Remove a ride by its ID and refresh the model.
		rides.remove(rideId);
		ridesModel.clear();
		ridesModel.addAll(rides.values().stream().map(Ride::toString).toList());
	}

	private void removeEBike(String bikeId) {
		// Remove an E-Bike by its ID and refresh the model.
		bikes.remove(bikeId);
		bikesModel.clear();
		bikesModel.addAll(bikes.values().stream().map(Ebike::toString).toList());
	}

	private DefaultListModel<String> getRidesModel() {
		// Create a model for the rides list.
		DefaultListModel<String> ridesModel = new DefaultListModel<>();
		ridesModel.addAll(rides.values().stream().map(Ride::toString).toList());
		return ridesModel;
	}

	private DefaultListModel<String> getBikesModel() {
		// Create a model for the bikes list.
		DefaultListModel<String> bikesModel = new DefaultListModel<>();
		bikesModel.addAll(bikes.values().stream().map(Ebike::toString).toList());
		return bikesModel;
	}

	private DefaultListModel<String> getUsersModel() {
		// Create a model for the users list.
		DefaultListModel<String> usersModel = new DefaultListModel<>();
		usersModel.addAll(users.values().stream().map(User::toString).toList());
		return usersModel;
	}

    public void display() {
    	// Show the admin GUI on the Event Dispatch Thread.
    	SwingUtilities.invokeLater(() -> {
    		this.setVisible(true);
			this.revalidate();
			this.repaint();
    	});
    }
    
    public void refreshView() {
    	// Refresh the central visualizer panel.
    	centralPanel.refresh();
    }  

    @Override
	public void actionPerformed(ActionEvent e) {
        // Handle action events.
        if (e.getSource() == this.addEBikeButton) {
	        JDialog d = new AddEBikeDialog(this, app); // Open dialog to add new E-Bike.
	        d.setVisible(true);
        }
	}
    
    public static class VisualiserPanel extends JPanel {
        private long dx; // X offset for rendering.
        private long dy; // Y offset for rendering.
        private AdminGUI app; // Reference to the main AdminGUI.
        
        public VisualiserPanel(int w, int h, AdminGUI app) {
            setSize(w, h); // Set the size of the panel.
            dx = w / 2; // Center x offset.
            dy = h / 2; // Center y offset.
            this.app = app; // Reference to the main application.
        }

        public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
    		
    		// Enable anti-aliasing and high-quality rendering.
    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    		g2.clearRect(0, 0, this.getWidth(), this.getHeight()); // Clear the panel.

			// Render each E-Bike on the visualizer.
			app.bikes.values().forEach(b -> {
    			int x0 = (int) (dx + b.locX()); // Calculate screen x coordinate.
		        int y0 = (int) (dy - b.locY()); // Calculate screen y coordinate.
		        g2.drawOval(x0, y0, 10, 10); // Draw bike as a circle.
		        g2.drawString(b.id(), x0, y0 + 35); // Display bike ID.
			});
        }
        
        public void refresh() {
            // Refresh the visualizer panel.
            repaint();
        }
    }

	@Override
	public void bikeUpdated(String bikeID, EbikeState state, double locationX, double locationY, double directionX, double directionY, double speed, int batteryLevel) {
		Ebike eBikeInfo;

		if (state == EbikeState.DISMISSED) {
			SwingUtilities.invokeLater(() -> removeEBike(bikeID)); // Remove bike if dismissed.
		} else {	
			// Create or update E-Bike info.
			eBikeInfo = new Ebike(bikeID, state, locationX, locationY, directionX, directionY, speed, batteryLevel);
			SwingUtilities.invokeLater(() -> addOrReplaceEBike(eBikeInfo)); // Update the bike list model.
		}

		// Remove ongoing ride if bike is not in use.
		if (state != EbikeState.IN_USE) {
			var ride = rides.values().stream().filter(r -> r.ebikeId().equals(bikeID)).findFirst();
			if (ride.isPresent()) {
				SwingUtilities.invokeLater(() -> removeRide(ride.get().rideId())); // Remove the ride from the model.
			}
		}
		// centralPanel.refresh(); // Refresh the visualizer panel.
	}

	@Override
	public void bikeRemoved(String bikeID) {
		SwingUtilities.invokeLater(() -> this.removeEBike(bikeID));
	}

	@Override
	public void rideStarted(String rideID, String userID, String bikeID) {
		SwingUtilities.invokeLater(() -> this.addOrReplaceRide(new Ride(rideID, userID, bikeID)));
	}

	@Override
	public void rideStep(String rideID, double x, double y, double directionX, double directionY, double speed, int batteryLevel){
		var ride = rides.get(rideID); 
		var bike = bikes.get(ride.ebikeId());
		var newBike = new Ebike(bike.id(), bike.state(), x, y, directionX, directionY, speed, batteryLevel);

		SwingUtilities.invokeLater(() -> {
			addOrReplaceEBike(newBike); // Update bike list model.
			centralPanel.refresh(); // Refresh the visualizer panel.
		});
	}

	@Override
	public void rideEnded(String rideID, String reason) {
		SwingUtilities.invokeLater(() -> this.removeRide(rideID));
	}

	@Override
	public void userUpdated(String userID, int credit) {
		SwingUtilities.invokeLater(() -> this.addOrReplaceUser(new User(userID, credit)));
	}
}
