package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StorageBasedRegistryTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Artifact ARTEFACT_A = new Artifact(LIB_A, Set.of());

    /** Bouchon écrit à la main : get renvoie la réponse programmée, put ne fait rien. */
    private static final class StubStorage implements IStorage {
        private final Optional<Artifact> reponse;

        StubStorage(Optional<Artifact> reponse) {
            this.reponse = reponse;
        }

        @Override
        public void put(Gav gav, Artifact artifact) {
            // rien : un bouchon ne fait que répondre
        }

        @Override
        public Optional<Artifact> get(Gav gav) {
            return reponse;
        }
    }

    // [IStorage | get(a) ↦ Some(A)]s  ⊢  lookup(a)  ⇒  Some(A)
    @Test
    void lookupRenvoieLArtefactDuStockage() {
        IRegistry registry = new StorageBasedRegistry(new StubStorage(Optional.of(ARTEFACT_A)));
        assertThat(registry.lookup(LIB_A), is(Optional.of(ARTEFACT_A)));
    }

    // [IStorage | get(a) ↦ Some(A)]s  ⊢  publish(A)  ⇒  ↯ AlreadyPublishedException
    @Test
    void publishRefuseUneCoordonneeDejaPubliee() {
        IRegistry registry = new StorageBasedRegistry(new StubStorage(Optional.of(ARTEFACT_A)));
        assertThrows(AlreadyPublishedException.class, () -> registry.publish(ARTEFACT_A));
    }
}
