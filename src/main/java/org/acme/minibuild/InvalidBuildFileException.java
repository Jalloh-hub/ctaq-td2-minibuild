package org.acme.minibuild;

/**
 * Fichier de build syntaxiquement invalide. Exception non vérifiée : l'interface fournie
 * IPomParser ne déclare que IOException, et un contenu invalide n'est pas une erreur
 * d'entrée-sortie.
 */
public class InvalidBuildFileException extends RuntimeException {

    public InvalidBuildFileException(String message) {
        super(message);
    }

    public InvalidBuildFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
