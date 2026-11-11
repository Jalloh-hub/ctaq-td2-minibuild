package org.acme.minibuild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class InMemoryStorageTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Artifact ARTEFACT_A = new Artifact(LIB_A, Set.of());

    private IStorage storage;   // typé par l'interface

    @BeforeEach
    void init() {
        storage = new InMemoryStorage();   // fixture reconstruite avant chaque test
    }

    @Test
    void getRenvoieLArtefactRangeSousSaCoordonnee() {
        storage.put(LIB_A, ARTEFACT_A);
        assertEquals(Optional.of(ARTEFACT_A), storage.get(LIB_A));
    }

    @Test
    void getRenvoieVidePourUneCoordonneeAbsente() {
        assertTrue(storage.get(LIB_A).isEmpty());
    }
}
