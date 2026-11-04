package org.acme.minibuild;

import java.util.Objects;
import java.util.Set;

/**
 * Ce que décrit le fichier de build : un nom de projet et l'ensemble de ses dépendances
 * directes. Simple agrégat de données : pas de test dédié.
 */
public record Project(String name, Set<Gav> dependencies) {

    public Project {
        Objects.requireNonNull(name, "nom de projet absent");
        dependencies = Set.copyOf(dependencies);
    }
}
