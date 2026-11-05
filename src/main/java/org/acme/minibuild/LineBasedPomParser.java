package org.acme.minibuild;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Lit un fichier de build orienté lignes : une ligne {@code project <nom>} (la première ligne
 * utile), puis des lignes {@code dependency <group:artifact:version>} ; les lignes vides sont
 * ignorées.
 */
public class LineBasedPomParser implements IPomParser {

    private static final String PROJECT = "project";
    private static final String DEPENDENCY = "dependency";

    @Override
    public Project parse(ILineReader reader) throws IOException {
        String ligne = prochaineLigneUtile(reader);
        if (ligne == null) {
            throw new InvalidBuildFileException("fichier vide : ligne « project » attendue");
        }
        String nom = valeurApres(PROJECT, ligne);
        Set<Gav> dependances = new HashSet<>();
        while ((ligne = prochaineLigneUtile(reader)) != null) {
            dependances.add(coordonnee(valeurApres(DEPENDENCY, ligne)));
        }
        return new Project(nom, dependances);
    }

    /** Ligne suivante non vide, ou null en fin de fichier : les lignes vides sont ignorées. */
    private static String prochaineLigneUtile(ILineReader reader) throws IOException {
        String ligne;
        do {
            ligne = reader.readLine();
        } while (ligne != null && ligne.isBlank());
        return ligne;
    }

    /** Valeur qui suit le mot-clé attendu, ou exception si la ligne ne commence pas par lui. */
    private static String valeurApres(String motCle, String ligne) {
        String[] morceaux = ligne.strip().split("\\s+", 2);
        if (!morceaux[0].equals(motCle) || morceaux.length < 2) {
            throw new InvalidBuildFileException(
                "ligne « " + motCle + " <valeur> » attendue : " + ligne);
        }
        return morceaux[1];
    }

    private static Gav coordonnee(String texte) {
        try {
            return Gav.parse(texte);
        } catch (IllegalArgumentException e) {
            throw new InvalidBuildFileException("coordonnée invalide : " + texte, e);
        }
    }
}
