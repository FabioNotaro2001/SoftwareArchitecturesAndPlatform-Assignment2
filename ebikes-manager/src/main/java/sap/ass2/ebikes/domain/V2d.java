package sap.ass01.layered.businessLogic;

/**
 * Represents a 2-dimensional vector.
 * Instances of this class are immutable, making them stateless.
 * It supports various operations like vector summation, normalization, and rotation.
 */
public class V2d implements java.io.Serializable {
    private double x, y;  // X and Y components of the vector.

    /**
     * Constructor to initialize the vector with given x and y coordinates.
     * @param x The x component of the vector.
     * @param y The y component of the vector.
     */
    public V2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x component of the vector.
     * @return The x value.
     */
    public double x() {
        return x;
    }

    /**
     * Returns the y component of the vector.
     * @return The y value.
     */
    public double y() {
        return y;
    }

    /**
     * Returns a new vector that is the sum of the current vector and another vector.
     * @param v The vector to be added.
     * @return A new vector representing the sum of this vector and the provided vector.
     */
    public V2d sum(V2d v) {
        return new V2d(x + v.x, y + v.y);
    }

    /**
     * Rotates the vector by a specified number of degrees around the origin.
     * @param degree The degree by which the vector should be rotated.
     * @return A new vector representing the rotated version of the current vector.
     */
    public V2d rotate(double degree) {
        var rad = degree * Math.PI / 180;  // Converts degrees to radians.
        var cs = Math.cos(rad);            // Calculates the cosine of the angle.
        var sn = Math.sin(rad);            // Calculates the sine of the angle.
        var x1 = x * cs - y * sn;          // New x component after rotation.
        var y1 = x * sn + y * cs;          // New y component after rotation.
        var v = new V2d(x1, y1).getNormalized();  // Returns the rotated and normalized vector.
        return v;
    }

    /**
     * Returns the magnitude (length) of the vector.
     * @return The magnitude of the vector.
     */
    public double abs() {
        return (double) Math.sqrt(x * x + y * y);
    }

    /**
     * Returns a normalized version of the vector.
     * A normalized vector has the same direction but a magnitude of 1.
     * @return A new vector representing the normalized version of this vector.
     */
    public V2d getNormalized() {
        double module = (double) Math.sqrt(x * x + y * y);  // Computes the vector's length.
        return new V2d(x / module, y / module);  // Returns the normalized vector.
    }

    /**
     * Multiplies the vector by a scalar value.
     * @param fact The scalar factor by which to multiply the vector.
     * @return A new vector that is the result of scaling the current vector by the given factor.
     */
    public V2d mul(double fact) {
        return new V2d(x * fact, y * fact);
    }

    /**
     * Returns a string representation of the vector in the format "V2d(x, y)".
     * @return A string representing the vector.
     */
    public String toString() {
        return "V2d(" + x + "," + y + ")";
    }

}
