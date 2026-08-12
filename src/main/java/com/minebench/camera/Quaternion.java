package com.minebench.camera;

public record Quaternion(double x, double y, double z, double w) {
    public static final Quaternion IDENTITY = new Quaternion(0.0, 0.0, 0.0, 1.0);

    public Quaternion normalized() {
        double length = Math.sqrt(x * x + y * y + z * z + w * w);
        if (length == 0.0 || !Double.isFinite(length)) {
            return IDENTITY;
        }
        return new Quaternion(x / length, y / length, z / length, w / length);
    }

    public double dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public Quaternion slerp(Quaternion other, double amount) {
        Quaternion a = normalized();
        Quaternion b = other.normalized();
        double dot = a.dot(b);
        if (dot < 0.0) {
            b = new Quaternion(-b.x, -b.y, -b.z, -b.w);
            dot = -dot;
        }
        if (dot > 0.9995) {
            return new Quaternion(
                    a.x + amount * (b.x - a.x),
                    a.y + amount * (b.y - a.y),
                    a.z + amount * (b.z - a.z),
                    a.w + amount * (b.w - a.w)
            ).normalized();
        }
        double theta = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
        double sinTheta = Math.sin(theta);
        if (sinTheta == 0.0) {
            return a;
        }
        double first = Math.sin((1.0 - amount) * theta) / sinTheta;
        double second = Math.sin(amount * theta) / sinTheta;
        return new Quaternion(
                first * a.x + second * b.x,
                first * a.y + second * b.y,
                first * a.z + second * b.z,
                first * a.w + second * b.w
        ).normalized();
    }

    public double yawDegrees() {
        Quaternion normalized = normalized();
        double radians = Math.atan2(
                2.0 * (normalized.w * normalized.y + normalized.x * normalized.z),
                1.0 - 2.0 * (normalized.y * normalized.y + normalized.z * normalized.z));
        return Math.toDegrees(radians);
    }

    public double pitchDegrees() {
        Quaternion normalized = normalized();
        double sine = 2.0 * (normalized.w * normalized.x - normalized.z * normalized.y);
        return Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, sine))));
    }
}
