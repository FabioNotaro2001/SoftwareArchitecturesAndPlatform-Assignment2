package sap.ass01.layered.businessLogic;

/**
 * Represents a 2-dimensional point in a Cartesian coordinate system.
 */
public class P2d implements java.io.Serializable {
    private double x, y; // The x and y coordinates of the point.

    /**
     * Constructs a P2d object with specified x and y coordinates.
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     */
    public P2d(double x, double y) {
        this.x = x; 
        this.y = y; 
    }

    /**
     * Returns the x coordinate of the point.
     * @return The x coordinate.
     */
    public double x() {
        return x; 
    }

    /**
     * Returns the y coordinate of the point.
     * @return The y coordinate.
     */
    public double y() {
        return y; 
    }

    /**
     * Sums the current point with a 2-dimensional vector and returns the resulting point.
     * @param v The vector to be added to the point.
     * @return A new P2d object representing the sum of the point and the vector.
     */
    public P2d sum(V2d v) {
        return new P2d(x + v.x(), y + v.y()); 
    }

    /**
     * Subtracts a given point from the current point and returns the resulting vector.
     * @param v The point to be subtracted.
     * @return A new V2d object representing the vector from the given point to this point.
     */
    public V2d sub(P2d v) {
        return new V2d(x - v.x(), y - v.y()); 
    }

    /**
     * Returns a string representation of the P2d object.
     * @return A string in the format "P2d(x,y)".
     */
    public String toString() {
        return "P2d(" + x + "," + y + ")"; 
    }
}
