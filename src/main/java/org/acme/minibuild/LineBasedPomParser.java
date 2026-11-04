package org.acme.minibuild;

import java.io.IOException;
import java.util.Set;

/** Lit un fichier de build orienté lignes. */
public class LineBasedPomParser implements IPomParser {

    private static final String PROJECT = "project ";

    @Override
    public Project parse(ILineReader reader) throws IOException {
        String ligne = reader.readLine();
        if (ligne == null) {
            throw new InvalidBuildFileException("fichier vide : ligne « project » attendue");
        }
        String nom = ligne.substring(PROJECT.length());
        return new Project(nom, Set.of());
    }
}
