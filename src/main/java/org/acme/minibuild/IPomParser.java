package org.acme.minibuild;

import java.io.IOException;

/** Lecture d'un fichier de build (fournie). */
public interface IPomParser {
    Project parse(ILineReader reader) throws IOException;
}
