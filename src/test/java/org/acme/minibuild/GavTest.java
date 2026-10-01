package org.acme.minibuild;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GavTest {
    @Test
    void parseExtraitLeGroupe() {
        Gav gav = Gav.parse("org.acme:lib-a:1.0.0");
        assertEquals("org.acme", gav.group());
    }
}
