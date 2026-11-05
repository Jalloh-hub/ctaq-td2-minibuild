package org.acme.minibuild;

import java.util.Objects;
import java.util.Optional;

/**
 * Registre qui s'appuie sur un IStorage. Responsabilité propre : la règle de
 * non-republication (une coordonnée publiée ne peut plus être modifiée).
 */
public class StorageBasedRegistry implements IRegistry {

    private final IStorage storage;

    public StorageBasedRegistry(IStorage storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public void publish(Artifact artifact) throws AlreadyPublishedException {
        if (storage.get(artifact.gav()).isPresent()) {
            throw new AlreadyPublishedException(artifact.gav());
        }
        storage.put(artifact.gav(), artifact);
    }

    @Override
    public Optional<Artifact> lookup(Gav gav) {
        return storage.get(gav);
    }
}
