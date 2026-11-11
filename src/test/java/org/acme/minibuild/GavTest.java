package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
class GavTest {

    // Jeux de valeurs dans un fichier des ressources de test
    // (src/test/resources/coordonnees-valides.csv), première ligne = en-têtes.
    @ParameterizedTest
    @CsvFileSource(resources = "/coordonnees-valides.csv", numLinesToSkip = 1)
    void parseDecoupeLesTroisChamps(String coordonnee, String groupe, String artefact,
                                    String version) {
        Gav gav = Gav.parse(coordonnee);
        assertThat(gav.group(), is(groupe));
        assertThat(gav.artifact(), is(artefact));
        assertThat(gav.version(), is(version));
        assertThat(gav.toString(), startsWith(groupe + ":"));
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
