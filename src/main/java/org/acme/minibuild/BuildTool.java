package org.acme.minibuild;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Orchestre : lit le fichier de build, puis résout ses dépendances. Pas d'interface : aucun
 * composant n'en dépend.
 */
public class BuildTool {

    private final IPomParser parser;
    private final IResolver resolver;

    public BuildTool(IPomParser parser, IResolver resolver) {
        this.parser = Objects.requireNonNull(parser);
        this.resolver = Objects.requireNonNull(resolver);
    }

    /** Dépendances (directes et transitives) du projet décrit par le fichier de build. */
    public Set<Gav> build(ILineReader buildFile) throws IOException {
        Project project = parser.parse(buildFile);
        return resolver.resolve(project.dependencies());
    }
}
