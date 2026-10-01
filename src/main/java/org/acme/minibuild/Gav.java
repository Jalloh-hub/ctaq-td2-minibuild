package org.acme.minibuild;

public record Gav(String group, String artifact, String version) {
    public static Gav parse(String coordinate) {
        String[] parts = coordinate.split(":");
        return new Gav(parts[0], parts[1], parts[2]);
    }
}
