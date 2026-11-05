package org.acme.minibuild;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Calcule la fermeture transitive des dépendances en interrogeant le registre. Garde toutes
 * les versions rencontrées (aucune résolution de conflit).
 */
public class AllVersionsResolver implements IResolver {

    private final IRegistry registry;

    public AllVersionsResolver(IRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public Set<Gav> resolve(Set<Gav> directDependencies) {
        Set<Gav> resolues = new HashSet<>();                  // déjà vues : garantit la
        Deque<Gav> aTraiter = new ArrayDeque<>(directDependencies); // terminaison (cycles)
        while (!aTraiter.isEmpty()) {
            Gav gav = aTraiter.pop();
            if (resolues.add(gav)) {                          // chaque coordonnée une fois
                registry.lookup(gav)
                        .ifPresent(artefact -> aTraiter.addAll(artefact.dependencies()));
            }
        }
        return resolues;
    }
}
