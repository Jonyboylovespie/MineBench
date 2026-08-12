package com.minebench.camera;

/** Pure deterministic interpolation utilities used by the scripted camera and unit tests. */
public final class SplineInterpolator {
    private SplineInterpolator() {
    }

    public static Vec3 catmullRom(Vec3 before, Vec3 start, Vec3 end, Vec3 after, double amount) {
        double t = amount;
        double t2 = t * t;
        double t3 = t2 * t;
        return new Vec3(
                0.5 * ((2.0 * start.x())
                        + (-before.x() + end.x()) * t
                        + (2.0 * before.x() - 5.0 * start.x() + 4.0 * end.x() - after.x()) * t2
                        + (-before.x() + 3.0 * start.x() - 3.0 * end.x() + after.x()) * t3),
                0.5 * ((2.0 * start.y())
                        + (-before.y() + end.y()) * t
                        + (2.0 * before.y() - 5.0 * start.y() + 4.0 * end.y() - after.y()) * t2
                        + (-before.y() + 3.0 * start.y() - 3.0 * end.y() + after.y()) * t3),
                0.5 * ((2.0 * start.z())
                        + (-before.z() + end.z()) * t
                        + (2.0 * before.z() - 5.0 * start.z() + 4.0 * end.z() - after.z()) * t2
                        + (-before.z() + 3.0 * start.z() - 3.0 * end.z() + after.z()) * t3)
        );
    }

    public static Quaternion orientation(Quaternion start, Quaternion end, double amount) {
        return start.slerp(end, amount);
    }
}
