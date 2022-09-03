package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Blob implements Serializable {

    /** name of working file. */
    private String filename;

    /** contents of working file. */
    private byte[] content;

    Blob(File file) {
        filename = file.getName();
        content = Utils.readContents(file);
    }

    Blob(String path) {
        this(new File(path));
    }

    public byte[] getContents() {
        return content;
    }

    public String getContentsAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }

}
