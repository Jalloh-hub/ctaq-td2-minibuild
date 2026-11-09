package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Intégration descendante (démarche du cours, C2 diapositive 40) : à chaque étape on ajoute
 * des composants réels à ceux déjà intégrés ; l'étape se caractérise par l'interface testée.
 * Les composants appelés non encore intégrés sont des bouchons. Aucun pilote : le test appelle
 * directement BuildTool, qui n'a pas d'appelant. Les tests des étapes précédentes sont conservés.
 */
class TopDownIntegrationTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Gav LIB_B = Gav.parse("org.acme:lib-b:2.1.0");
    private static final Gav LIB_C = Gav.parse("org.other:lib-c:3.0.0");

    // Étape 1 — ajoutés : BuildTool + LineBasedPomParser ; interface testée : BuildTool–IPomParser.
    // Bouchons : ILineReader, IResolver. Pilote : aucun.
    // [ILineReader | readLine ↦ ⟨"project mon-app", "dependency a", "dependency b", ⊥⟩]s,
    // [IResolver | resolve({a, b}) ↦ {a, b}, resolve({a, b}) ×1]sp
    //     ⊢  build(reader)  ⇒  {a, b}
    @Test
    void etape1BuildToolTransmetAuResolveurLeProjetLuParLeParseur() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine()).thenReturn("project mon-app",
            "dependency org.acme:lib-a:1.0.0", "dependency org.acme:lib-b:2.1.0", null);
        IResolver resolver = mock(IResolver.class);
        when(resolver.resolve(Set.of(LIB_A, LIB_B))).thenReturn(Set.of(LIB_A, LIB_B));
        BuildTool buildTool = new BuildTool(new LineBasedPomParser(), resolver);

        assertThat(buildTool.build(reader), is(Set.of(LIB_A, LIB_B)));
        verify(resolver).resolve(Set.of(LIB_A, LIB_B));
    }

    // Même étape : l'hypothèse du parseur sur un fichier vide (exception, question 14.1)
    // remonte jusqu'à BuildTool, qui ne sollicite pas le résolveur.
    // [ILineReader | readLine ↦ ⟨⊥⟩]s, [IResolver | resolve ×0]sp
    //     ⊢  build(reader)  ⇒  ↯ InvalidBuildFileException
    @Test
    void etape1UnFichierVideNeSollicitePasLeResolveur() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine()).thenReturn(null);
        IResolver resolver = mock(IResolver.class);
        BuildTool buildTool = new BuildTool(new LineBasedPomParser(), resolver);

        assertThrows(InvalidBuildFileException.class, () -> buildTool.build(reader));
        verify(resolver, never()).resolve(any());
    }

    // Étape 2 — ajouté : AllVersionsResolver ; interface testée : BuildTool–IResolver.
    // Bouchons : ILineReader, IRegistry. Pilote : aucun.
    // [ILineReader | readLine ↦ ⟨"project mon-app", "dependency a", ⊥⟩]s,
    // [IRegistry | lookup(a) ↦ Some(A), lookup(c) ↦ Some(C)]s  ⊢  build(reader)  ⇒  {a, c}
    //     où A = Artifact(a, {c}), C = Artifact(c, ∅)
    @Test
    void etape2LesDependancesLuesSontResoluesTransitivement() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine()).thenReturn("project mon-app", "dependency org.acme:lib-a:1.0.0", null);
        IRegistry registry = mock(IRegistry.class);
        when(registry.lookup(LIB_A)).thenReturn(Optional.of(new Artifact(LIB_A, Set.of(LIB_C))));
        when(registry.lookup(LIB_C)).thenReturn(Optional.of(new Artifact(LIB_C, Set.of())));
        BuildTool buildTool =
            new BuildTool(new LineBasedPomParser(), new AllVersionsResolver(registry));

        assertThat(buildTool.build(reader), is(Set.of(LIB_A, LIB_C)));
    }

    // Étape 3 — ajouté : BufferedLineReader ; interface testée : LineBasedPomParser–ILineReader.
    // Bouchon : IRegistry. Pilote : aucun. Le texte du fichier vient d'un StringReader.
    // [IRegistry | lookup(a) ↦ Some(A), lookup(c) ↦ Some(C)]s
    //     ⊢  build(BufferedLineReader("project mon-app\n\ndependency a\n"))  ⇒  {a, c}
    //     où A = Artifact(a, {c}), C = Artifact(c, ∅)
    @Test
    void etape3LeFichierEstLuParUnVraiLecteur() throws IOException {
        ILineReader reader = new BufferedLineReader(new StringReader(
            "project mon-app\n\ndependency org.acme:lib-a:1.0.0\n"));
        IRegistry registry = mock(IRegistry.class);
        when(registry.lookup(LIB_A)).thenReturn(Optional.of(new Artifact(LIB_A, Set.of(LIB_C))));
        when(registry.lookup(LIB_C)).thenReturn(Optional.of(new Artifact(LIB_C, Set.of())));
        BuildTool buildTool =
            new BuildTool(new LineBasedPomParser(), new AllVersionsResolver(registry));

        assertThat(buildTool.build(reader), is(Set.of(LIB_A, LIB_C)));
    }
}
