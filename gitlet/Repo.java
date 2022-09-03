package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Queue;
import java.util.LinkedList;

/** Creates Version-System tree to track commits.
 *  @author David Babazadeh
 */
class Repo {

    /** returns repo's cwd. */
    public File cwd() {
        return _cwd;
    }

    /** returns .gitlet dir. */
    public File gitlet() {
        return _gitlet;
    }

    /** directory which repository exists in/represents. */
    private final File _cwd;

    /** directory to store hidden repo information. */
    private final File _gitlet;

    /** directory to store commit information. */
    private final File _commits;

    /** directory to store branch references. */
    private final File _branches;

    /** directory to store blobs information. */
    private final File _blobs;

    /** directory to store remote references. */
    private final File _remotes;

    /** file to store default branch reference. */
    private final File _master;

    /** file to store current branch. */
    private final File _head;

    /** directory to store staging information. */
    private final File _stage;

    /** directory to store added blobs. */
    private final File _add;

    /** directory to store removed blobs. */
    private final File _rm;

    /** Creates a new Gitlet version-control system in the current directory.
     * begins with an initial commit containing no files and the commit message
     * "initial commit" . It will have a single branch: master, initially
     * pointing to this initial commit, and master will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1
     * January 1970 in whatever format you choose for dates (this is called
     * "The (Unix) Epoch", represented internally by the time 0.) Since the
     * initial commit in all repositories created by Gitlet will have exactly
     * the same content, it follows that all repositories will share
     * this commit (they will all have the same UID) and all commits in all
     * repositories will trace back to it.
     */
    public void init() {
        if (_gitlet.exists()) {
            GLet.error("A Gitlet version-control system already exists"
                    + " in the current directory");
            return;
        }

        _gitlet.mkdir();
        Commit root = new Commit("initial commit");
        File location = new File(_commits, Utils.sha1(Utils.serialize(root))
                .toString());

        try {
            _commits.mkdir();
            _branches.mkdir();
            _blobs.mkdir();
            _remotes.mkdir();
            _stage.mkdir();
            _add.mkdir();
            _rm.mkdir();
            _master.createNewFile();
            _head.createNewFile();
            location.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeContents(_head, _master.getName());
        Utils.writeContents(_master, location.toString());
        Utils.writeObject(location, root);
    }

    /**
     * Adds a series of files. calls add(String filename) on files indicated by
     * each string given (see below).
     * @param filenames String Array of files to be staged
     */
    public void add(String[] filenames) {
        for (String filename : filenames) {
            add(filename);
        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area (see
     * the description of the commit command). For this reason, adding a file
     * is also called staging the file for addition. Staging an already-staged
     * file overwrites the previous entry in the staging area with the new
     * contents. The staging area should be somewhere in .gitlet. If the
     * current working version of the file is identical to the version in the
     * current commit, do not stage it to be added, and remove it from the
     * staging area if it is already there (as can happen when a file is
     * changed, added, and then changed back). The file will no longer be
     * staged for removal (see gitlet rm), if it was at the time of the command
     * @param filename file to be staged
     */
    public void add(String filename) {
        File actor = new File(_add, filename);
        File og = new File(_cwd, filename);
        if (!og.exists()) {
            GLet.error("File does not exist.");
            return;
        }
        Blob blob = new Blob(og);
        (new File(_rm, filename)).delete();

        try {
            Commit current = getHeadCommit();

            if (current.containsKey(filename)
                    && current.blobs(filename).equals(GLet.sha1Obj(blob))) {
                actor.delete();
                return;
            }
            actor.createNewFile();
            Utils.writeObject(actor, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * calls rm(File file) on using files indicated by each string given.
     * @param filenames String Array of files to be staged for removal
     */
    public void rm(String[] filenames) {
        for (String filename : filenames) {
            rm(filename);
        }
    }

    /**
     * unstages file if staged for addition, removes file from cwd
     * & stages removal if file is tracked by current Commit.
     * @param filename
     */
    public void rm(String filename) {
        File actor = new File(_rm, filename);
        File og = new File(_gitlet.getParent(), filename);
        File addition = (new File(_add, filename));
        Commit head = getHeadCommit();

        if (!head.tracks(filename)) {
            if (!addition.exists()) {
                GLet.error("No reason to remove the file.");
            }
            addition.delete();
            return;
        }

        addition.delete();
        og.delete();
        try {
            actor.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** commit without second (merged) parent.
     * @param msg */
    public void commit(String msg) {
        commit(msg, "");
    }

    /**
     * instantiates a commit that persists in the commits folder, tracking the
     * current commit & staged files. new commit message is defined by MSG
     * which is surrounded by quotes if multi-worded. abbreviation allowed.
     *
     * details/quirks: default commit only tracks its parent's files. next,
     * updates the content from previous commit tracks (added) staged files
     * which its parent did not track. compliment for removed staged files.
     * clears staging area. never modifies cwd. head & current branch moves
     * to new commit.
     * @param msg
     * @param parent2Ref
     */
    public void commit(String msg, String parent2Ref) {
        Commit next = new Commit(msg, Utils.readContentsAsString(
                headRef()).toString(), parent2Ref);
        boolean error = true;

        for (String filename : Utils.plainFilenamesIn(_rm)) {
            File staged = new File(_rm, filename);
            error = false;
            next.removeblob(filename);
            staged.delete();
        }
        for (String filename : Utils.plainFilenamesIn(_add)) {
            File staged = new File(_add, filename);
            next.updateBlob(staged, _gitlet);
            error = false;
            staged.delete();
        }

        if (error) {
            GLet.error("No changes added to the commit.");
        }

        File location = new File(_commits, GLet.sha1Obj(next));
        next.saveCommit(location);
        Utils.writeContents(headRef(), location.toString());
    }

    /** recursively displays commit history from head to
     * initial commit (backwards). */
    public void logHeadPath() {
        getHeadCommit().logHistory();
    }

    /** handles global-log dispays commit info for all commits in repo
     * by iterating through commits. */
    public void logAll() {
        for (String filename: Objects.requireNonNull(
                Utils.plainFilenamesIn(_commits))) {
            Utils.readObject(new File(_commits, filename), Commit.class).log();
        }
    }

    /** prints all commit id's that have the
     * exact commit message (one per line).
     * @param msg*/
    public void find(String msg) {
        boolean error = true;
        for (String filename: Objects.requireNonNull(
                Utils.plainFilenamesIn(_commits))) {
            Commit candidate = Utils.readObject(new File(_commits, filename),
                    Commit.class);
            if (candidate.getMessage().equals(msg)) {
                System.out.println(filename);
                error = false;
            }
        }
        if (error) {
            GLet.error("Found no commit with that message.");
        }
    }

    /** displays existing branches (marking head with preceding *),
     * staged files, removed files, (modded | deleted) && unstaged files,
     * and untracked files (ignoring subdirectories). lexicographic order. */
    public void status() {
        System.out.println("=== Branches ===");
        String headname = Utils.readContentsAsString(_head);
        for (String branchname : Objects.requireNonNull(
               Utils.plainFilenamesIn(_branches))) {
            if (branchname.equals(headname)) {
                System.out.print("*");
            }
            System.out.println(branchname);
        }

        System.out.println("\n=== Staged Files ===");
        for (String filename : Objects.requireNonNull(
                Utils.plainFilenamesIn(_add))) {
            System.out.println(filename);
        }

        System.out.println("\n=== Removed Files ===");
        for (String filename : Objects.requireNonNull(
                Utils.plainFilenamesIn(_rm))) {
            System.out.println(filename);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
        System.out.println();
    }

    /** handles reset. checks out commit then moves current branch head.
     * commitID can be abbreviated
     * @param commitID */
    public void checkoutCommit(String commitID) {
        try {
            commitID = GLet.expandHashID(commitID, _commits);
        } catch (GitletException | IndexOutOfBoundsException e) {
            GLet.error("No commit with that id exists.");
        }

        Commit source = Utils.readObject(new File(_commits, commitID),
                Commit.class);
        checkoutCommit(source);
        clearStagingArea();

        Utils.writeContents(headRef(),
                (new File(_commits, commitID)).toString());
    }

    /** copy/replaces the source's files, deleting
     * currently tracked files that are unpresent in th checked-out branch.
     * @param source */
    public void checkoutCommit(Commit source) {
        Commit current = getHeadCommit();
        for (String filename : source.filenames()) {
            File work = new File(_cwd, filename);
            if (work.exists()) {
                Blob working = new Blob(work);
                if (!current.containsKey(filename) || !current.blobs(filename)
                        .equals(GLet.sha1Obj(working))) {
                    GLet.error("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }
        for (String filename : source.filenames()) {
            checkoutFile(source, filename);
        }
        for (String filename : current.filenames()) {
            if (source.blobs(filename) == null) {
                (new File(_cwd, filename)).delete();
            }
        }
    }

    /**
     * finds branch and copy/replaces the commit's files, deleting
     * currently tracked files that are unpresent in th checked-out branch.
     * updates head to this branch.
     * clears staging area if checked-out branch != current branch
     * @param branchname
     */
    public void checkoutBranch(String branchname) {
        File branch = new File(_branches, branchname);
        if (!branch.exists()) {
            GLet.error("No such branch exists.");
        } else if (branch.equals(headRef())) {
            GLet.error("No need to checkout the current branch");
        }

        checkoutCommit(GLet.refToCommit(branch));
        clearStagingArea();

        Utils.writeContents(_head, branchname);

    }

    /** deletes all files in staging area deletions/additions. */
    private void clearStagingArea() {
        for (String filename : Utils.plainFilenamesIn(_rm)) {
            (new File(_rm, filename)).delete();
        }
        for (String filename : Utils.plainFilenamesIn(_add)) {
            (new File(_add, filename)).delete();
        }
    }

    /**
     * case of " -- filename".
     * @param filename
     */
    public void checkoutFile(String filename) {
        checkoutFile(getHeadCommit(), filename);
    }

    /**
     * case of " id -- filename". overwrites file in cwd with commit's
     * version of that file. commit abbreviation allowed.
     * @param commitID
     * @param filename
     */
    public void checkoutFile(String commitID, String filename) {
        if (commitID.length() < GLet.HASH_LEN) {
            commitID = GLet.expandHashID(commitID, _commits);
        }
        File commitFile = new File(_commits, commitID);
        if (!commitFile.exists()) {
            GLet.error("No commit with that id exists");
        }
        checkoutFile(Utils.readObject(commitFile, Commit.class), filename);
    }

    /**
     * overwrites file in cwd with source's version of that file.
     * @param source
     * @param filename
     */
    public void checkoutFile(Commit source, String filename) {
        File currentVersion = new File(_cwd, filename);
        if  (source.blobs(filename) == null) {
            GLet.error("File does not exist in that commit.");
        }
        File blob = new File(_blobs, source.blobs(filename));
        byte[] contents = (Utils.readObject(blob, Blob.class)).getContents();
        Utils.writeContents(currentVersion, contents);
    }

    /** creates symbolic id pointed at head by persisting a file
     * named symbol containing hash of head in _branches_ folder.
     * does not change head.
     * @param branchname*/
    public void addBranch(String branchname) {
        File branch = new File(_branches, branchname);
        if (branch.exists()) {
            GLet.error("A branch with that name already exists.");
        }

        try {
            branch.createNewFile();
            Utils.writeContents(branch,
                    Utils.readContentsAsString(headRef()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** removes branch id/pointer; deletes branch file named symbol in
     * _branches_.
     * @param branchname */
    public void rmBranch(String branchname) {
        File branch = new File(_branches, branchname);
        if (!branch.exists()) {
            GLet.error("A branch with that name does not exist.");
        } else if (Utils.readContentsAsString(_head).equals(branchname)) {
            GLet.error("Cannot remove the current branch.");
        }

        branch.delete();
    }

    /** dun dun dun.
     * @param branchname*/
    public void merge(String branchname) {
        mergeAssurance(branchname);
        Commit source = GLet.refToCommit(new File(_branches, branchname));
        Commit current = getHeadCommit();
        Commit split = splitPoint(current, source);
        if (split.equals(source)) {
            GLet.error("Given branch is an ancestor of the current branch.");
        } else if (split.equals(current)) {
            checkoutBranch(branchname);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        TreeSet<String> conflictedFiles = new TreeSet<String>();
        TreeSet<String> checkouts = new TreeSet<String>();
        TreeSet<String> removals = new TreeSet<String>();
        for (String filename : current.filenames()) {
            boolean splitPresent = split.containsKey(filename);
            boolean sourcePresent = source.containsKey(filename);
            boolean sourceSame = split.compareBlobs(source, filename);
            boolean currentSame = split.compareBlobs(current, filename);
            if (!sourceSame && currentSame) {
                assureBlobTracking(current, filename);
                if (sourcePresent) {
                    checkouts.add(filename);
                } else {
                    removals.add(filename);
                }
            } else if (sourceSame && !currentSame
                    || current.compareBlobs(source, filename)
                    || !splitPresent && !sourcePresent) {
                continue;
            } else if (splitPresent && !sourcePresent && currentSame) {
                assureBlobTracking(current, filename);
                removals.add(filename);
            } else {
                conflictedFiles.add(filename);
            }
        }
        for (String filename : source.filenames()) {
            if (!current.containsKey(filename)
                    && !split.containsKey(filename)) {
                assureBlobTracking(current, filename);
                checkouts.add(filename);
            }
        }
        addCheckouts(checkouts, source);
        rmRemovals(removals);
        addConflicts(conflictedFiles, source, current);
        String msg = "Merged " + branchname + " into "
                + Utils.readContentsAsString(_head) + ".";
        commit(msg, source.getLocation(_commits).toString());
        if (!conflictedFiles.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * errors if file is diferent in cwd vs head.
     * @param current
     * @param filename
     */
    private void assureBlobTracking(Commit current, String filename) {
        File working = new File(_cwd, filename);
        if (!current.containsKey(filename)) {
            if (working.exists()) {
                GLet.error("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        } else if (!current.blobs(filename).equals(GLet.sha1Obj(
                new Blob(working)))) {
            GLet.error("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        }
    }

    private void rmRemovals(TreeSet<String> removals) {
        for (String filename : removals) {
            rm(filename);
        }
    }

    /**
     * adds conflicted files to stage.
     * @param conflictedFiles
     * @param source
     * @param current
     */
    private void addConflicts(Set<String> conflictedFiles,
                              Commit source, Commit current) {
        for (String filename : conflictedFiles) {
            if (!current.blobs(filename).equals(GLet.sha1Obj(
                    new Blob(new File(_cwd, filename))))) {
                GLet.error("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
            File conflict = new File(_cwd, filename);
            Blob curr = GLet.findBlobInstance(current, _blobs, filename);
            String sourContent = "";
            if (source.containsKey(filename)) {
                Blob sour = GLet.findBlobInstance(source, _blobs, filename);
                sourContent = sour.getContentsAsString();
            }
            Utils.writeContents(conflict,
                    "<<<<<<< HEAD\n"
                            + curr.getContentsAsString() + "=======\n"
                            + sourContent + ">>>>>>>\n");
            add(filename);
        }
    }

    /**
     * adds checkouts files to stage.
     * @param checkouts
     * @param source
     */
    private void addCheckouts(Set<String> checkouts,
                              Commit source) {
        for (String filename : checkouts) {
            checkoutFile(source, filename);
            add(filename);
        }
    }

    /** (BFS) searches backwards from nodes to find common ancestor
     *  nearest to a. returns commit representing that ancestor.
     *  @param a (prioritizes proximity)
     *  @param b
     *  @return commit latest common ancestor */
    private Commit splitPoint(Commit a, Commit b) {

        ArrayList<String> bAncestors = b.ancestors();

        Queue<Commit> ancestors = new LinkedList<Commit>();
        ancestors.add(a);
        while (!ancestors.isEmpty()) {
            Commit node = ancestors.remove();
            if (bAncestors.contains(node.id())) {
                return node;
            }
            if (!node.isInitialCommit()) {
                ancestors.add(node.parent1());
            }
            if (node.hasParent2()) {
                ancestors.add(node.parent2());
            }
        }

        return new Commit("initial commit");
    }

    /** return Commit which head is pointing to. */
    private Commit getHeadCommit() {
        return GLet.refToCommit(headRef());
    }

    /** return branch file which head points to.
     * eg. File(.../branches/master) */
    private File headRef() {
        return new File(_branches, Utils.readContentsAsString(_head));
    }

    /** asserts that stage is clear, errors otherwise.
     * @param branchname  */
    private void mergeAssurance(String branchname) {
        if (!Utils.plainFilenamesIn(_rm).isEmpty()
            || !Utils.plainFilenamesIn(_add).isEmpty()) {
            GLet.error("You have uncommitted changes.");
        } else if (!Utils.plainFilenamesIn(_branches).contains(branchname)) {
            GLet.error("A branch with that name does not exist.");
        } else if (branchname.equals(Utils.readContentsAsString(_head))) {
            GLet.error("Cannot merge a branch with itself.");
        }
    }

    Repo() {
        _cwd = new File(System.getProperty("user.dir"));
        _gitlet = new File(cwd(), ".gitlet");
        _commits = new File(_gitlet, "commits");
        _branches = new File(_gitlet, "branches");
        _blobs = new File(_gitlet, "blobs");
        _remotes = new File(_gitlet, "remotes");
        _master = new File(_branches, "master");
        _head = new File(_gitlet, "HEAD");
        _stage = new File(_gitlet, "staging_area");
        _add = new File(_stage, "additions");
        _rm = new File(_stage, "deletions");
    }
}
