package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class LineBasedPomParserTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");

    private final IPomParser parser = new LineBasedPomParser();

    // 14.1
    // [ILineReader | readLine ↦ ⟨⊥⟩]s  ⊢  parse(reader)  ⇒  ↯ InvalidBuildFileException
    @Test
    void unFichierVideEstInvalide() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine()).thenReturn(null);

        assertThrows(InvalidBuildFileException.class, () -> parser.parse(reader));
    }

    // 14.2
    // [ILineReader | readLine ↦ ⟨"project mon-app", ⊥⟩]s
    //     ⊢  parse(reader)  ⇒  Project("mon-app", ∅)
    @Test
    void unFichierAvecSeulementLaLigneProject() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine()).thenReturn("project mon-app", (String) null);

        Project project = parser.parse(reader);

        assertThat(project.name(), is("mon-app"));
        assertThat(project.dependencies(), is(empty()));
    }

    // 14.3
    // [ILineReader | readLine ↦ ⟨"project mon-app", "dependency org.acme:lib-a:1.0.0", ⊥⟩]s
    //     ⊢  parse(reader)  ⇒  Project("mon-app", {org.acme:lib-a:1.0.0})
    @Test
    void unFichierAvecUneDependance() throws IOException {
        ILineReader reader = mock(ILineReader.class);
        when(reader.readLine())
            .thenReturn("project mon-app", "dependency org.acme:lib-a:1.0.0", null);

        Project project = parser.parse(reader);

        assertThat(project.name(), is("mon-app"));
        assertThat(project.dependencies(), contains(LIB_A));
    }
}
