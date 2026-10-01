package org.acme.minibuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class GavTest {

    // Jeux de valeurs dans un fichier des ressources de test
    // (src/test/resources/coordonnees-valides.csv), première ligne = en-têtes.
    @ParameterizedTest
    @CsvFileSource(resources = "/coordonnees-valides.csv", numLinesToSkip = 1)
    void parseDecoupeLesTroisChamps(String coordonnee, String groupe, String artefact,
                                    String version) {
        Gav gav = Gav.parse(coordonnee);
        assertEquals(groupe, gav.group());
        assertEquals(artefact, gav.artifact());
        assertEquals(version, gav.version());
    }
}
