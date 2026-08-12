package com.minebench.scene;

public enum SceneId {
    OVERWORLD_PANORAMA("overworld_panorama", "Overworld static panorama", 30, true),
    OVERWORLD_FLYTHROUGH("overworld_flythrough", "Overworld cinematic flythrough", 60, true),
    NETHER_FLYTHROUGH("nether_flythrough", "Nether cinematic flythrough", 60, true),
    END_FLYTHROUGH("end_flythrough", "End cinematic flythrough", 60, true),
    CHUNK_REBUILD("chunk_rebuild", "Overworld geometry rebuild", 30, true);

    private final String id;
    private final String displayName;
    private final int durationSeconds;
    private final boolean mandatory;

    SceneId(String id, String displayName, int durationSeconds, boolean mandatory) {
        this.id = id;
        this.displayName = displayName;
        this.durationSeconds = durationSeconds;
        this.mandatory = mandatory;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public boolean mandatory() {
        return mandatory;
    }

    public static SceneId fromId(String id) {
        for (SceneId value : values()) {
            if (value.id.equalsIgnoreCase(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown MineBench scene: " + id);
    }
}
