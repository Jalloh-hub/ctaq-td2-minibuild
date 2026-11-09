package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Intégration ascendante (C2, diapositive 36) : on intègre d'abord les composants de plus bas
 * niveau ; les appelants non encore intégrés sont simulés par des pilotes.
 *
 * <p>Étape 1 — ajoutés : StorageBasedRegistry + InMemoryStorage ; interface testée :
 * StorageBasedRegistry–IStorage. Bouchon : aucun (InMemoryStorage n'appelle rien).
 * Pilote : cette classe de test, qui joue le rôle de l'appelant non encore intégré
 * (le résolveur) : elle fournit les jeux d'essai au registre et en recueille les résultats.
 */
class BottomUpIntegrationTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Gav LIB_C = Gav.parse("org.other:lib-c:3.0.0");
    private static final Artifact ARTEFACT_A = new Artifact(LIB_A, Set.of(LIB_C));

    private IRegistry registry;

    @BeforeEach
    void init() {
        registry = new StorageBasedRegistry(new InMemoryStorage());
    }

    // Jeu d'essai 1 : ce qui est publié se retrouve, avec ses dépendances.
    // ⊢  publish(A) ; lookup(a)  ⇒  Some(A)     où A = Artifact(a, {c})
    @Test
    void unArtefactPublieSeRetrouve() throws AlreadyPublishedException {
        registry.publish(ARTEFACT_A);

        assertThat(registry.lookup(LIB_A), is(Optional.of(ARTEFACT_A)));
    }

    // Jeu d'essai 2 : ce qui n'a jamais été publié est absent.
    // ⊢  lookup(c)  ⇒  None
    @Test
    void unArtefactJamaisPublieEstAbsent() {
        assertThat(registry.lookup(LIB_C), is(Optional.empty()));
    }

    // Jeu d'essai 3 : la non-republication fonctionne avec le vrai stockage, et l'artefact
    // d'origine n'est pas écrasé.
    // ⊢  publish(A) ; publish(A')  ⇒  ↯ AlreadyPublishedException ; lookup(a) ⇒ Some(A)
    //     où A' = Artifact(a, ∅)
    @Test
    void uneRepublicationEstRefuseeEtNEcrasePasLOriginal() throws AlreadyPublishedException {
        registry.publish(ARTEFACT_A);
        Artifact modifie = new Artifact(LIB_A, Set.of());

        assertThrows(AlreadyPublishedException.class, () -> registry.publish(modifie));
        assertThat(registry.lookup(LIB_A), is(Optional.of(ARTEFACT_A)));
    }
}
