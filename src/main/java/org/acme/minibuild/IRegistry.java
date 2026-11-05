package org.acme.minibuild;

import java.util.Optional;

/** Registre d'artefacts (fourni). */
public interface IRegistry {
    // lève AlreadyPublishedException si la coordonnée de l'artefact est déjà publiée
    void publish(Artifact artifact) throws AlreadyPublishedException;
    Optional<Artifact> lookup(Gav gav);
}
