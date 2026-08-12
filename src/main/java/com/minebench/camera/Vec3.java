package com.minebench.camera;

public record Vec3(double x, double y, double z) {
    public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);

    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    public Vec3 multiply(double scalar) {
        return new Vec3(x * scalar, y * scalar, z * scalar);
    }

    public Vec3 lerp(Vec3 other, double amount) {
        return new Vec3(
                x + (other.x - x) * amount,
                y + (other.y - y) * amount,
                z + (other.z - z) * amount
        );
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
