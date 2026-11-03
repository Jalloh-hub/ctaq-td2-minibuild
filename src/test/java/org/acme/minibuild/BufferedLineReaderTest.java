package org.acme.minibuild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class BufferedLineReaderTest {

    // Un StringReader est un vrai Reader (pas une doublure) : aucun fichier nécessaire.
    @Test
    void litLesLignesPuisRenvoieNullEnFinDeTexte() throws IOException {
        ILineReader reader = new BufferedLineReader(new StringReader("ligne 1\nligne 2\n"));
        assertEquals("ligne 1", reader.readLine());
        assertEquals("ligne 2", reader.readLine());
        assertNull(reader.readLine());
    }
}
