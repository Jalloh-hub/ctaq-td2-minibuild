package org.acme.minibuild;

/**
 * Une dépendance n'a pas été trouvée dans le registre. Exception non vérifiée : l'interface
 * fournie IResolver ne déclare aucune exception.
 */
public class UnresolvedDependencyException extends RuntimeException {

    public UnresolvedDependencyException(Gav gav) {
        super("dépendance absente du registre : " + gav);
    }
}
