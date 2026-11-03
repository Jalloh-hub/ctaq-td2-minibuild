package org.acme.minibuild;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/** Adaptateur : réalise ILineReader en déléguant à un BufferedReader. */
public class BufferedLineReader implements ILineReader {

    private final BufferedReader delegue;

    public BufferedLineReader(Reader source) {
        this.delegue = new BufferedReader(source);
    }

    @Override
    public String readLine() throws IOException {
        return delegue.readLine();
    }
}
