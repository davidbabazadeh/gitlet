package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;

/** Commit Nodes along tree branches representing different versions
 * of a directory.
 *  @author David Babazadeh
 */
public class Commit implements Serializable {

    /** metadata - date created. */
    private final Date _timestamp;

    /** user message at creation.. */
    private final String _message;

    /** metadata - creator. */
    private final String _author;

    /** represents depth from initial commit. **/
    private final int _version;

    /** node parent. */
    private final String _parent1;

    /** for merge. */
    private final String _parent2;

    /** map for all blobs. */
    private TreeMap<String, String> _blobs = new TreeMap<String, String>();

    /** returns committed filenames. */
    public Set<String> filenames() {
        return _blobs.keySet();
    }

    /** returns true if commit tracks given file.
     * @param name */
    public boolean containsKey(String name) {
        return _blobs.containsKey(name);
    }

    /** returns commit message. */
    public String getMessage() {
        return _message;
    }

    /**
     * returns Sha-1 hash of blob stored under filename.
     * @param filename
     */
    public String blobs(String filename) {
        return _blobs.get(filename);
    }

    public Commit(String message, String parentRef1, String parentRef2) {
        _message = message;
        _author = "ur mom";
        _parent1 = parentRef1;
        _parent2 = parentRef2;
        if (parentRef1.equals("")) {
            _timestamp = new Date(0);
            _version = 0;
            return;
        }
        _timestamp = new Date();
        Commit parent1 = Utils.readObject(new File(parentRef1), Commit.class);
        _blobs = parent1._blobs;
        _version = parent1._version + 1;

    }

    public Commit(String message, String parentRef1) {
        this(message, parentRef1, "");
    }

    public Commit(String message) {
        this(message, "", "");
    }

    /**
     * updates blob treenode.
     * @param file -> File file represents staged file:
     *             (cwd-filename :: serialized blob)
     * @param repo
     */
    public void updateBlob(File file, File repo) {
        if (!_blobs.containsKey(file.getName()) || !blobs(file.getName())
                .equals(Utils.sha1(Utils.readContents(file)))) {

            byte[] contents = Utils.readContents(file);
            String blobID = Utils.sha1(contents);
            _blobs.put(file.getName(), blobID);
            try {
                File blob = new File(repo, "blobs/" + blobID);
                blob.createNewFile();
                Utils.writeContents(blob, contents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * removes blob treenode.
     * @param filename -> name of blob to stop tracking
     */
    public void removeblob(String filename) {
        _blobs.remove(filename);
    }

    /**
     * saves commit to given location.
     * @param location
     */
    public void saveCommit(File location) {
        Utils.writeObject(location, this);
    }

    /**
     * onk.
     * @param dir
     * @return location of file.
     */
    public File getLocation(File dir) {
        return new File(dir, GLet.sha1Obj(this));
    }

    /**
     * @return true if is initial commit.
     */
    public boolean isInitialCommit() {
        return _parent1.equals("");
    }

    /** returns true if this has merged parent. */
    public boolean hasParent2() {
        return !_parent2.equals("");
    }

    /** returns positive if a > b.
     * @param b */
    public int compareVersions(Commit b) {
        return _version - b._version;
    }

    /** compares file contents with b.
     * returns true if same.
     * @param b
     * @param filename */
    public boolean compareBlobs(Commit b, String filename) {
        String hash1 = blobs(filename);
        String hash2 = b.blobs(filename);
        if (hash1 == null || hash2 == null) {
            return hash1 == null && hash2 == null;
        }
        return hash1.equals(hash2);
    }

    /** compares file contents with b.
     * returns true if same.
     * @param b */
    public boolean equals(Commit b) {
        return GLet.sha1Obj(this).equals(GLet.sha1Obj(b));
    }

    /** searches backwards from nodes to find ancestors.
     * returns those ancestors. */
    public ArrayList<String> ancestors() {
        ArrayList<String> all = new ArrayList<String>();
        Commit a = this;
        all.add(a.id());
        while (a != null) {
            Commit p1 = a.parent1();
            if (p1 != null) {
                all.add(p1.id());
            }
            if (a.hasParent2()) {
                Commit p2 = a.parent2();
                all.addAll(p2.ancestors());
            }
            a = p1;
        }
        return all;
    }

    /** worst case Theta(N).
     * returns true if filename is stored in
     * commit's history somewhere.
     * @param filename */
    public boolean tracks(String filename) {
        if (_parent1.equals("")) {
            return false;
        } else if (containsKey(filename)) {
            return true;
        } else {
            Commit p = parent1();
            return p.tracks(filename);
        }
    }

    /** returns parent1 commit. */
    public Commit parent1() {
        if (isInitialCommit()) {
            return null;
        }
        return Utils.readObject(new File(_parent1), Commit.class);
    }

    /** returns parent2 commit. */
    public Commit parent2() {
        if (!hasParent2()) {
            return null;
        }
        return Utils.readObject(new File(_parent2), Commit.class);
    }

    /** recursively displays commit logs backwards until initial commit. */
    public void logHistory() {
        log();
        if (!_parent1.equals("")) {
            Commit p = Utils.readObject(new File(_parent1), Commit.class);
            p.logHistory();
        }
    }

    /** displays commit log in standard format. */
    public void log() {
        System.out.println("===");
        System.out.println("commit " + id());
        if (!_parent2.equals("")) {
            System.out.println("Merge: " + GLet.abbvHash(_parent1, 7)
                    + " " + GLet.abbvHash(_parent2, 7));
        }

        System.out.format("Date: %ta %tb %td %tT %tY %tz\n", _timestamp,
                _timestamp, _timestamp, _timestamp, _timestamp,
                _timestamp, _timestamp);
        System.out.println(_message);
        System.out.println();
    }

    /** returns sha1 hash id of itself.
     */
    public String id() {
        return GLet.sha1Obj(this);
    }

}
