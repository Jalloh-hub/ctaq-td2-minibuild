package org.acme.minibuild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    // Classes d'équivalence invalides de l'entrée (comme au TD1), un représentant chacune :
    // chaîne vide ; trop peu de parties ; trop de parties ; une partie vide (au début,
    // au milieu, à la fin) ; une partie faite d'espaces.
    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "org.acme:lib-a",
        "org.acme:lib-a:1.0.0:extra",
        ":lib-a:1.0.0",
        "org.acme::1.0.0",
        "org.acme:lib-a:",
        "org.acme:   :1.0.0"
    })
    void parseRejetteUneCoordonneeMalFormee(String coordonnee) {
        assertThrows(IllegalArgumentException.class, () -> Gav.parse(coordonnee));
    }

    // null n'est pas une chaîne mal formée mais une absence de valeur : convention Java,
    // NullPointerException (Objects.requireNonNull).
    @Test
    void parseRejetteNull() {
        assertThrows(NullPointerException.class, () -> Gav.parse(null));
    }
}
