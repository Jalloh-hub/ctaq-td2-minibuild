package org.acme.minibuild;

/**
 * Tentative de republier une coordonnée déjà publiée. Comme sur Maven Central, un artefact
 * publié ne peut plus être modifié. Exception vérifiée : l'appelant peut s'en relever (en
 * publiant une nouvelle version), et la règle métier apparaît ainsi dans le contrat.
 */
public class AlreadyPublishedException extends Exception {

    public AlreadyPublishedException(Gav gav) {
        super("coordonnée déjà publiée : " + gav);
    }
}
