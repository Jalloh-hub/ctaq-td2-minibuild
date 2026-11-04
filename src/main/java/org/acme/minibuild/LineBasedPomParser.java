package org.acme.minibuild;

import java.io.IOException;

/** Lit un fichier de build orienté lignes. */
public class LineBasedPomParser implements IPomParser {

    @Override
    public Project parse(ILineReader reader) throws IOException {
        String ligne = reader.readLine();
        if (ligne == null) {
            throw new InvalidBuildFileException("fichier vide : ligne « project » attendue");
        }
        return null;   // cas suivants : task list
    }
}
