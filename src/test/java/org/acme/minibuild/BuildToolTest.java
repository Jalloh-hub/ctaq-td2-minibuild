package org.acme.minibuild;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BuildToolTest {

    private static final Gav LIB_A = Gav.parse("org.acme:lib-a:1.0.0");
    private static final Gav LIB_B = Gav.parse("org.acme:lib-b:2.1.0");

    // Q21 — test UNITAIRE de BuildTool : ses deux collaborateurs sont doublés.
    // [ILineReader | ]d, [IPomParser | parse(r) ↦ Project("mon-app", {a})]s,
    // [IResolver | resolve({a}) ↦ {a, b}, resolve({a}) ×1]sp
    //     ⊢  build(r)  ⇒  {a, b}
    @Test
    void buildTransmetAuResolveurLesDependancesIssuesDuParseur() throws IOException {
        ILineReader reader = mock(ILineReader.class);        // jamais utilisé (dummy)
        IPomParser parser = mock(IPomParser.class);
        IResolver resolver = mock(IResolver.class);
        when(parser.parse(reader)).thenReturn(new Project("mon-app", Set.of(LIB_A)));
        when(resolver.resolve(Set.of(LIB_A))).thenReturn(Set.of(LIB_A, LIB_B));
        BuildTool buildTool = new BuildTool(parser, resolver);

        Set<Gav> resolues = buildTool.build(reader);

        verify(resolver).resolve(Set.of(LIB_A));             // interaction : bonne transmission
        assertThat(resolues, is(Set.of(LIB_A, LIB_B)));      // état : résultat du résolveur
    }
}
