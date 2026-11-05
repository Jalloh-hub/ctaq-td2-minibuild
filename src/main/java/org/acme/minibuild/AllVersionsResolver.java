package org.acme.minibuild;

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
        Set<Gav> resolues = new HashSet<>();
        for (Gav gav : directDependencies) {
            resolues.add(gav);
            registry.lookup(gav)
                    .ifPresent(artefact -> resolues.addAll(resolve(artefact.dependencies())));
        }
        return resolues;
    }
}
