package org.acme.minibuild;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Lit un fichier de build orienté lignes. */
public class LineBasedPomParser implements IPomParser {

    private static final String PROJECT = "project ";
    private static final String DEPENDENCY = "dependency ";

    @Override
    public Project parse(ILineReader reader) throws IOException {
        String ligne = prochaineLigneUtile(reader);
        if (ligne == null) {
            throw new InvalidBuildFileException("fichier vide : ligne « project » attendue");
        }
        String nom = ligne.substring(PROJECT.length());
        Set<Gav> dependances = new HashSet<>();
        while ((ligne = prochaineLigneUtile(reader)) != null) {
            dependances.add(Gav.parse(ligne.substring(DEPENDENCY.length())));
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
}
