package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Tag("unit")
class AllVersionsResolverTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Gav LIB_B = Gav.parse("org.acme:lib-b:2.1.0");
    private static final Gav LIB_C = Gav.parse("org.other:lib-c:3.0.0");

    private IRegistry registry;
    private IResolver resolver;

    // Fixture : registre bouchonné où lib-a dépend de lib-b, qui dépend de lib-c.
    @BeforeEach
    void init() {
        registry = mock(IRegistry.class);
        when(registry.lookup(LIB_A)).thenReturn(Optional.of(new Artifact(LIB_A, Set.of(LIB_B))));
        when(registry.lookup(LIB_B)).thenReturn(Optional.of(new Artifact(LIB_B, Set.of(LIB_C))));
        when(registry.lookup(LIB_C)).thenReturn(Optional.of(new Artifact(LIB_C, Set.of())));
        resolver = new AllVersionsResolver(registry);
    }

    // Q17 — test orienté ÉTAT
    // [IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C)]s
    //     ⊢  resolve({a})  ⇒  {a, b, c}
    //     où A = Artifact(a, {b}), B = Artifact(b, {c}), C = Artifact(c, ∅)
    @Test
    void resolveRenvoieLaFermetureTransitive() {
        Set<Gav> resolved = resolver.resolve(Set.of(LIB_A));

        assertEquals(Set.of(LIB_A, LIB_B, LIB_C), resolved);          // JUnit seul
        assertThat(resolved, containsInAnyOrder(LIB_A, LIB_B, LIB_C)); // Hamcrest
    }

    // Q18 — test orienté INTERACTION (même scénario)
    // [IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C)]sp
    //     ⊢  resolve({a})
    //     ⇒  ∀ g ∈ {a, b, c} · lookup(g) ×1, et aucun autre appel au registre
    //     où A = Artifact(a, {b}), B = Artifact(b, {c}), C = Artifact(c, ∅)
    @Test
    void resolveInterrogeLeRegistreUneSeuleFoisParCoordonnee() {
        resolver.resolve(Set.of(LIB_A));

        verify(registry, times(1)).lookup(LIB_A);
        verify(registry, times(1)).lookup(LIB_B);
        verify(registry, times(1)).lookup(LIB_C);
        verifyNoMoreInteractions(registry);
    }

    // Q19 — cycle dans les données du registre bouchonné : lib-c dépend de lib-a.
    // [IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C')]s
    //     ⊢  resolve({a})  ⇒  {a, b, c}
    //     où A = Artifact(a, {b}), B = Artifact(b, {c}), C' = Artifact(c, {a})
    // @Timeout : un résolveur qui boucle ferait échouer le test au lieu de le bloquer.
    @Test
    @Timeout(2)
    void resolveTermineEnPresenceDUnCycle() {
        when(registry.lookup(LIB_C)).thenReturn(Optional.of(new Artifact(LIB_C, Set.of(LIB_A))));

        assertThat(resolver.resolve(Set.of(LIB_A)), containsInAnyOrder(LIB_A, LIB_B, LIB_C));
    }

    // Après correction : en losange (a → b, a → c, b → d, c → d), d n'est demandé qu'une
    // fois. La version récursive naïve le demandait deux fois : seul un test d'interaction
    // voit cette différence, le résultat (l'état) étant le même.
    // [IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C),
    //              lookup(d) ↦ Some(D)]sp
    //     ⊢  resolve({a})  ⇒  lookup(d) ×1
    //     où A = Artifact(a, {b, c}), B = Artifact(b, {d}), C = Artifact(c, {d}),
    //        D = Artifact(d, ∅)
    @Test
    void resolveNeRedemandePasUneCoordonneeDejaVue() {
        Gav libD = Gav.parse("org.other:lib-d:4.0.0");
        when(registry.lookup(LIB_A)).thenReturn(Optional.of(new Artifact(LIB_A, Set.of(LIB_B, LIB_C))));
        when(registry.lookup(LIB_B)).thenReturn(Optional.of(new Artifact(LIB_B, Set.of(libD))));
        when(registry.lookup(LIB_C)).thenReturn(Optional.of(new Artifact(LIB_C, Set.of(libD))));
        when(registry.lookup(libD)).thenReturn(Optional.of(new Artifact(libD, Set.of())));

        resolver.resolve(Set.of(LIB_A));

        verify(registry, times(1)).lookup(libD);
    }

    // Tâche 20 (découverte en question 34) : lib-b introuvable dans le registre.
    // [IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ None]s  ⊢  resolve({a})
    //     ⇒  ↯ UnresolvedDependencyException
    //     où A = Artifact(a, {b})
    @Test
    void resolveEchoueSurUneDependanceAbsenteDuRegistre() {
        when(registry.lookup(LIB_B)).thenReturn(Optional.empty());

        assertThrows(UnresolvedDependencyException.class, () -> resolver.resolve(Set.of(LIB_A)));
    }
}
