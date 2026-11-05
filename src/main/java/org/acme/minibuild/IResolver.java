package org.acme.minibuild;

import java.util.Set;

/** Résolution des dépendances (fournie). */
public interface IResolver {
    Set<Gav> resolve(Set<Gav> directDependencies);
}
