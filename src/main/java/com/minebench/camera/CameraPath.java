package com.minebench.camera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Versioned, wall-clock camera route. It never depends on client ticks or rendered FPS. */
public final class CameraPath {
    private static final Gson GSON = new GsonBuilder().create();

    private final String id;
    private final boolean loop;
    private final List<CameraKeyframe> keyframes;
    private final String routeHash;

    public CameraPath(String id, boolean loop, List<CameraKeyframe> keyframes) {
        if (keyframes == null || keyframes.size() < 2) {
            throw new IllegalArgumentException("A camera path needs at least two keyframes");
        }
        this.id = id == null || id.isBlank() ? "unnamed" : id;
        this.loop = loop;
        this.keyframes = keyframes.stream()
                .sorted(Comparator.comparingDouble(CameraKeyframe::timeSeconds))
                .toList();
        if (this.keyframes.getLast().timeSeconds() <= this.keyframes.getFirst().timeSeconds()) {
            throw new IllegalArgumentException("Camera path duration must be positive");
        }
        this.routeHash = calculateHash();
    }

    public String id() {
        return id;
    }

    public boolean loop() {
        return loop;
    }

    public List<CameraKeyframe> keyframes() {
        return keyframes;
    }

    public double durationSeconds() {
        return keyframes.getLast().timeSeconds() - keyframes.getFirst().timeSeconds();
    }

    public String routeHash() {
        return routeHash;
    }

    public CameraSample sampleAtSeconds(double seconds) {
        double startTime = keyframes.getFirst().timeSeconds();
        double endTime = keyframes.getLast().timeSeconds();
        double duration = endTime - startTime;
        double time = seconds + startTime;
        if (loop) {
            time = startTime + positiveModulo(time - startTime, duration);
        } else {
            time = Math.max(startTime, Math.min(endTime, time));
        }

        int segment = 0;
        while (segment < keyframes.size() - 2 && time > keyframes.get(segment + 1).timeSeconds()) {
            segment++;
        }
        CameraKeyframe start = keyframes.get(segment);
        CameraKeyframe end = keyframes.get(segment + 1);
        double span = end.timeSeconds() - start.timeSeconds();
        double amount = span == 0.0 ? 0.0 : (time - start.timeSeconds()) / span;

        CameraKeyframe before = keyframes.get(Math.max(0, segment - 1));
        CameraKeyframe after = keyframes.get(Math.min(keyframes.size() - 1, segment + 2));
        Vec3 position = SplineInterpolator.catmullRom(
                before.position(), start.position(), end.position(), after.position(), amount);
        Quaternion orientation = SplineInterpolator.orientation(start.orientation(), end.orientation(), amount);
        return new CameraSample(position, orientation);
    }

    public CameraSample sampleNormalized(double progress) {
        double clamped = loop ? progress : Math.max(0.0, Math.min(1.0, progress));
        return sampleAtSeconds(clamped * durationSeconds());
    }

    public String toJson() {
        SerializedPath path = new SerializedPath();
        path.id = id;
        path.loop = loop;
        path.keyframes = new ArrayList<>();
        for (CameraKeyframe keyframe : keyframes) {
            SerializedKeyframe serialized = new SerializedKeyframe();
            serialized.timeSeconds = keyframe.timeSeconds();
            serialized.position = keyframe.position();
            serialized.orientation = keyframe.orientation();
            path.keyframes.add(serialized);
        }
        return GSON.toJson(path);
    }

    public static CameraPath fromJson(String json) {
        SerializedPath serialized = GSON.fromJson(json, SerializedPath.class);
        if (serialized == null || serialized.keyframes == null) {
            throw new IllegalArgumentException("Camera path JSON is empty or missing keyframes");
        }
        List<CameraKeyframe> frames = serialized.keyframes.stream()
                .map(frame -> new CameraKeyframe(frame.timeSeconds, frame.position, frame.orientation))
                .toList();
        return new CameraPath(serialized.id, serialized.loop, frames);
    }

    private String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

    private String canonical() {
        StringBuilder builder = new StringBuilder(id).append('|').append(loop);
        for (CameraKeyframe keyframe : keyframes) {
            builder.append('|').append(Double.toString(keyframe.timeSeconds()))
                    .append('|').append(Double.toString(keyframe.position().x()))
                    .append(',').append(Double.toString(keyframe.position().y()))
                    .append(',').append(Double.toString(keyframe.position().z()))
                    .append('|').append(Double.toString(keyframe.orientation().x()))
                    .append(',').append(Double.toString(keyframe.orientation().y()))
                    .append(',').append(Double.toString(keyframe.orientation().z()))
                    .append(',').append(Double.toString(keyframe.orientation().w()));
        }
        return builder.toString();
    }

    private static double positiveModulo(double value, double modulus) {
        double result = value % modulus;
        return result < 0.0 ? result + modulus : result;
    }

    private static final class SerializedPath {
        String id;
        boolean loop;
        List<SerializedKeyframe> keyframes;
    }

    private static final class SerializedKeyframe {
        double timeSeconds;
        Vec3 position;
        Quaternion orientation;
    }
}
