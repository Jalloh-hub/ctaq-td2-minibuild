package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests de validation : système complet, sans aucune doublure, à travers ses interfaces
 * externes (un vrai fichier de build sur disque, un registre réel pré-alimenté).
 */
@Tag("validation")
class ValidationTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Gav LIB_B = Gav.parse("org.acme:lib-b:2.1.0");
    private static final Gav LIB_C = Gav.parse("org.other:lib-c:3.0.0");

    private BuildTool buildTool;

    // Registre de l'exemple de la partie 1 : lib-a dépend de lib-c ; lib-b et lib-c n'ont
    // pas de dépendance.
    @BeforeEach
    void init() throws AlreadyPublishedException {
        IRegistry registry = new StorageBasedRegistry(new InMemoryStorage());
        registry.publish(new Artifact(LIB_A, Set.of(LIB_C)));
        registry.publish(new Artifact(LIB_B, Set.of()));
        registry.publish(new Artifact(LIB_C, Set.of()));
        buildTool = new BuildTool(new LineBasedPomParser(), new AllVersionsResolver(registry));
    }

    private static Path ressource(String nom) throws URISyntaxException {
        return Path.of(ValidationTest.class.getResource("/validation/" + nom).toURI());
    }

    // Q32 — le fichier de la partie 1 (mon-app dépend de lib-a et lib-b) :
    // fermeture transitive {lib-a, lib-b, lib-c}.
    @Test
    void resoutLesDependancesDuFichierDeLaPartie1() throws Exception {
        try (Reader fichier = Files.newBufferedReader(ressource("mon-app.build"))) {
            Set<Gav> resolues = buildTool.build(new BufferedLineReader(fichier));

            assertThat(resolues, containsInAnyOrder(LIB_A, LIB_B, LIB_C));
        }
    }

    // Q34 — une dépendance absente du registre (lib-x n'a jamais été publiée). Le sujet ne dit
    // pas quoi faire ; choix fait ici : échouer explicitement plutôt que rendre une fermeture
    // silencieusement incomplète (le build échouerait plus tard, loin de la cause).
    @Test
    void uneDependanceAbsenteDuRegistreFaitEchouerLaResolution() throws Exception {
        try (Reader fichier = Files.newBufferedReader(ressource("dependance-absente.build"))) {
            ILineReader reader = new BufferedLineReader(fichier);

            assertThrows(UnresolvedDependencyException.class, () -> buildTool.build(reader));
        }
    }

    // Q34 — un fichier syntaxiquement invalide (coordonnée à deux parties seulement).
    @Test
    void unFichierInvalideEstRejete() throws Exception {
        try (Reader fichier = Files.newBufferedReader(ressource("invalide.build"))) {
            ILineReader reader = new BufferedLineReader(fichier);

            assertThrows(InvalidBuildFileException.class, () -> buildTool.build(reader));
        }
    }
}
