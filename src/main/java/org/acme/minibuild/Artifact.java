package org.acme.minibuild;

import java.util.Objects;
import java.util.Set;

/**
 * Artefact publié dans le registre : sa coordonnée et l'ensemble des coordonnées dont il
 * dépend directement. Simple agrégat de données : pas de test dédié (equals, hashCode et
 * accesseurs sont engendrés par le langage).
 */
public record Artifact(Gav gav, Set<Gav> dependencies) {

    public Artifact {
        Objects.requireNonNull(gav, "coordonnée absente");
        dependencies = Set.copyOf(dependencies);   // copie défensive : artefact immuable
    }
}
