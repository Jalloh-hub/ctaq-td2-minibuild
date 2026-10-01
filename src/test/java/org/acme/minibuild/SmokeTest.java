package org.acme.minibuild;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmokeTest {
    @Test
    void junitFonctionne() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void hamcrestFonctionne() {
        assertThat(2 + 2, is(4));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mockitoFonctionne() {
        List<String> liste = mock(List.class);
        when(liste.size()).thenReturn(3);
        assertEquals(3, liste.size());
    }
}
