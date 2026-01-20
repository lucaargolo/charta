package dev.lucaargolo.charta.utils;

public class Vec3d {
    private double x, y, z;

    // Constructor
    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Add another Vec3d to this one
    public Vec3d add(Vec3d other) {
        return new Vec3d(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Multiply this Vec3d by a scalar
    public Vec3d multiply(double scalar) {
        return new Vec3d(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    // Static helper method to create a Vec3d from a 24-bit RGB integer
    public static Vec3d fromRGB24(int rgb) {
        double r = ((rgb >> 16) & 0xFF) / 255.0; // Extract red and normalize to [0, 1]
        double g = ((rgb >> 8) & 0xFF) / 255.0;  // Extract green and normalize to [0, 1]
        double b = (rgb & 0xFF) / 255.0;         // Extract blue and normalize to [0, 1]
        return new Vec3d(r, g, b);
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    // String representation for debugging
    @Override
    public String toString() {
        return String.format("Vec3d(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }

}