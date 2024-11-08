package sap.ass2.rides.domain;

/**
 * Represents a user in the system.
 * This class manages user information, including user ID and credit balance.
 */
public class User {
    private String id;      // The unique identifier for the user.
    private int credit;     // The current credit balance of the user.

    /**
     * Constructor to initialize a user with an ID and zero credits.
     * @param id The unique identifier for the user.
     */
    public User(String id) {
        this.id = id;         
        this.credit = 0;       
    }

    /**
     * Constructor to initialize a user with a specified ID and credit balance.
     * @param id The unique identifier for the user.
     * @param credit The initial credit balance of the user.
     */
    public User(String id, int credit) {
        this.id = id;           
        this.credit = credit;   
    }

    /**
     * Returns the unique identifier of the user.
     * @return The user's ID.
     */
    public String getId() {
        return id;              
    }

    /**
     * Returns the current credit balance of the user.
     * @return The user's credit balance.
     */
    public int getCredit() {
        return credit;          
    }

    /**
     * Recharges the user's credit by a specified amount.
     * @param deltaCredit The amount of credit to add.
     */
    public void rechargeCredit(int deltaCredit) {
        credit += deltaCredit;  
    }

    /**
     * Decreases the user's credit by a specified amount.
     * If the resulting credit balance is negative, it sets it to zero.
     * @param amount The amount of credit to subtract.
     */
    public void decreaseCredit(int amount) {
        credit -= amount;       
        if (credit < 0) {      
            credit = 0;         // Resets the credit balance to zero if it is negative.
        }
    }

    /**
     * Returns a string representation of the user in JSON-like format.
     * @return A string representing the user's ID and credit balance.
     */
    public String toString() {
        return "{ id: " + id + ", credit: " + credit + " }"; // Formats the user details as a string.
    }
}
