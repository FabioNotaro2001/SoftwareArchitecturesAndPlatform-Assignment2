package sap.ass2.rides.domain;

import java.util.Date;
import java.util.Optional;

/**
 * Represents a ride in the system.
 * This class encapsulates the details of a ride, including the user, ebike, start date, and end date.
 */
public class Ride {
    private Date startedDate;          // The date and time when the ride started.
    private Optional<Date> endDate;    // The optional date and time when the ride ended.
    private User user;                  // The user associated with the ride.
    private Ebike ebike;                // The ebike associated with the ride.
    private String id;                  // The unique identifier for the ride.

    /**
     * Constructor to initialize a ride with an ID, user, and Ebike.
     * The ride's start date is set to the current date and time.
     * The end date is initialized as empty since the ride is ongoing at creation.
     * @param id The unique identifier for the ride.
     * @param user The user participating in the ride.
     * @param ebike The ebike being used for the ride.
     */
    public Ride(String id, User user, Ebike ebike) {
        this.id = id;                    
        this.startedDate = new Date();   
        this.endDate = Optional.empty();  
        this.user = user;                
        this.ebike = ebike;              
    }

    /**
     * Returns the unique identifier of the ride.
     * @return The ride ID.
     */
    public String getId() {
        return id;                        
    }

    /**
     * Returns the date and time when the ride started.
     * @return The start date of the ride.
     */
    public Date getStartedDate() {
        return startedDate;               
    }
    
    /**
     * Returns an optional date representing when the ride ended.
     * @return An Optional containing the end date if the ride has ended; otherwise, empty.
     */
    public Optional<Date> getEndDate() {
        return endDate;                   
    }

    /**
     * Returns the user associated with the ride.
     * @return The user participating in the ride.
     */
    public User getUser() {
        return user;                      
    }

    /**
     * Returns the ebike associated with the ride.
     * @return The ebike being used for the ride.
     */
    public Ebike getEbike() {
        return ebike;                    
    }

    /**
     * Returns a string representation of the ride in JSON-like format.
     * @return A string representing the ride's ID, user ID, and ebike ID.
     */
    public String toString() {
        return "{ id: " + this.id + ", user: " + user.id() + ", bike: " + ebike.id() + " }"; 
    }
}
