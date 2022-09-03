package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class GLet {

    /**
     * reads commit from file's contents (if it's a reference).
     * refToCommit converts branchFile to its commit
     * @param file
     * @return commit referenced by contents of file
     */
    public static Commit refToCommit(File file) {
        return Utils.readObject(new File(Utils.readContentsAsString(file)),
                Commit.class);
    }

    public static Blob findBlobInstance(Commit target,
                                        File dir, String filename) {
        return Utils.readObject(new File(dir, target.blobs(filename)),
                Blob.class);
    }

    /**
     * returns sha1 after serializing object obj.
     * @param obj
     */
    public static String sha1Obj(Serializable obj) {
        return Utils.sha1(Utils.serialize(obj));
    }

    public static void error(String msg, Object... args) {
        System.out.printf(msg + "\n", args);
        System.exit(0);
    }

    /**
     * returns ref ending with a hash as the num char abbreviated hash
     * without regex. assumed ref length >= 40.
     * @param ref
     * @param num
     */
    public static String abbvHash(String ref, int num) {
        int len = ref.length();
        return ref.substring(len - HASH_LEN, len - HASH_LEN + num);
    }

    /** checks if .
     * @param args length is in range.
     * @param lower inclusive,
     * @param upper exclusive.
     * @param msg
     * outputs formatted message with
     * @param args1 if not
     * @param repo*/
    public static void checkArgs(Object[] args, int lower, int upper,
                                 String msg, File repo, Object... args1) {
        checkRepo(repo);
        if (args.length < lower || args.length >= upper) {
            error(msg, args);
        }
    }

    /** errors is repo does not exist.
     * @param repo */
    public static void checkRepo(File repo) {
        if (!repo.exists()) {
            error("Not in an initialized Gitlet directory.");
        }
    }

    /** returns full hash id from.
     * @param abbreviation in
     * @param hashDir . */
    public static String expandHashID(String abbreviation, File hashDir) {
        int len = abbreviation.length();
        String match = null;
        for (String id : Objects.requireNonNull(
                Utils.plainFilenamesIn(hashDir))) {
            if (id.substring(0, len).equals(abbreviation)) {
                if (match != null) {
                    throw Utils.error("invalid abbreviation/hashID");
                }
                match = id;
            }
        }
        if (match == null) {
            throw Utils.error("invalid abbreviation/hashID");
        }
        return match;
    }

    /** length of a sha 1 hash in hexadecimal. */
    public static final int HASH_LEN = 40;
}
