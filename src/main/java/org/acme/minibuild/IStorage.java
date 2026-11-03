package org.acme.minibuild;

import java.util.Optional;

/** Stockage clé-valeur : associe à chaque coordonnée l'artefact correspondant (fourni). */
public interface IStorage {
    void put(Gav gav, Artifact artifact);
    Optional<Artifact> get(Gav gav);
}
