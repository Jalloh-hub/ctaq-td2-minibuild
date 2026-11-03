package org.acme.minibuild;

import java.io.IOException;

/** Lecture ligne à ligne (fournie) : readLine renvoie null en fin de texte. */
public interface ILineReader {
    String readLine() throws IOException;
}
