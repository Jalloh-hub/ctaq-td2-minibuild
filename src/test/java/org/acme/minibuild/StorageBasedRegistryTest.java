package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // --- Mêmes tests avec Mockito : la doublure (rôle de Stub) est décrite dans le test ---

    // [IStorage | get(a) ↦ Some(A)]s  ⊢  lookup(a)  ⇒  Some(A)
    @Test
    void lookupRenvoieLArtefactDuStockageAvecMockito() {
        IStorage storage = mock(IStorage.class);
        when(storage.get(LIB_A)).thenReturn(Optional.of(ARTEFACT_A));
        IRegistry registry = new StorageBasedRegistry(storage);
        assertThat(registry.lookup(LIB_A), is(Optional.of(ARTEFACT_A)));
    }

    // [IStorage | get(a) ↦ Some(A)]s  ⊢  publish(A)  ⇒  ↯ AlreadyPublishedException
    @Test
    void publishRefuseUneCoordonneeDejaPublieeAvecMockito() {
        IStorage storage = mock(IStorage.class);
        when(storage.get(LIB_A)).thenReturn(Optional.of(ARTEFACT_A));
        IRegistry registry = new StorageBasedRegistry(storage);
        assertThrows(AlreadyPublishedException.class, () -> registry.publish(ARTEFACT_A));
    }

    // --- Ce que Mockito apporte : vérifier aussi les interactions (rôle de Spy) ---
    // Avec le bouchon écrit à la main, impossible de savoir si put a été appelé :
    // il faudrait écrire, à la main aussi, un Spy qui enregistre ses appels.

    // [IStorage | get(a) ↦ Some(A), put ×0]sp  ⊢  publish(A)  ⇒  ↯ AlreadyPublishedException
    // État (l'exception) ET interaction (put n'est jamais appelé) : les deux.
    @Test
    void publishRefuseUneCoordonneeDejaPublieeSansToucherAuStockage() {
        IStorage storage = mock(IStorage.class);
        when(storage.get(LIB_A)).thenReturn(Optional.of(ARTEFACT_A));
        IRegistry registry = new StorageBasedRegistry(storage);

        assertThrows(AlreadyPublishedException.class, () -> registry.publish(ARTEFACT_A));
        verify(storage, never()).put(any(), any());
    }

    // [IStorage | get(a) ↦ None, put(a, A) ×1]sp  ⊢  publish(A)  ⇒  (rien)
    @Test
    void publishRangeUnNouvelArtefactDansLeStockage() throws AlreadyPublishedException {
        IStorage storage = mock(IStorage.class);
        when(storage.get(LIB_A)).thenReturn(Optional.empty());
        IRegistry registry = new StorageBasedRegistry(storage);

        registry.publish(ARTEFACT_A);

        verify(storage).put(LIB_A, ARTEFACT_A);
    }
}
