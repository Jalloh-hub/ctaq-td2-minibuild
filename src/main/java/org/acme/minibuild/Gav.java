package org.acme.minibuild;

import java.util.Objects;

/** Coordonnée d'un artefact : groupe, nom (artifact) et version. */
public record Gav(String group, String artifact, String version) {

    private static final String SEPARATEUR = ":";

    /**
     * Construit une coordonnée à partir d'une chaîne {@code group:artifact:version}.
     *
     * @throws NullPointerException     si {@code coordinate} est {@code null}
     * @throws IllegalArgumentException si la chaîne n'a pas exactement trois parties non vides
     */
    public static Gav parse(String coordinate) {
        Objects.requireNonNull(coordinate, "coordonnée absente");
        String[] parts = coordinate.split(SEPARATEUR, -1);   // -1 : garder les parties vides
        if (parts.length != 3) {
            throw new IllegalArgumentException("coordonnée mal formée : " + coordinate);
        }
        for (String part : parts) {
            if (part.isBlank()) {
                throw new IllegalArgumentException("partie vide dans : " + coordinate);
            }
        }
        return new Gav(parts[0], parts[1], parts[2]);
    }
}
