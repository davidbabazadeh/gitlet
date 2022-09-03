package gitlet;

import java.util.Objects;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author David Babazadeh
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            GLet.error("Please enter a command.");
        }
        Repo repo = new Repo();
        switch (args[0]) {
        case "init":
            repo.init();
            break;
        case "add":
            GLet.checkArgs(args, 2, Integer.MAX_VALUE,
                    "add requires more arguments", repo.gitlet());
            repo.add(java.util.Arrays.copyOfRange(args, 1, args.length));
            break;
        case "commit":
            checkCommitArgs(args, repo);
            repo.commit(args[1]);
            break;
        case "rm":
            GLet.checkArgs(args, 2, Integer.MAX_VALUE,
                    "rm requires more arguments", repo.gitlet());
            repo.rm(java.util.Arrays.copyOfRange(args, 1, args.length));
            break;
        case "log":
            repo.logHeadPath();
            break;
        case "global-log":
            repo.logAll();
            break;
        case "find":
            checkSoloArg(args, "find requires a commit message", repo);
            repo.find(args[1]);
            break;
        case "status":
            GLet.checkRepo(repo.gitlet());
            repo.status();
            break;
        case "checkout":
            checkCheckoutArgs(args, repo);
            break;
        case "branch":
            checkSoloArg(args, "requires single branch name", repo);
            repo.addBranch(args[1]);
            break;
        case "rm-branch":
            checkSoloArg(args, "requires single branch name", repo);
            repo.rmBranch(args[1]);
            break;
        case "reset":
            checkSoloArg(args, "requires single commit id", repo);
            repo.checkoutCommit(args[1]);
            break;
        case "merge":
            checkSoloArg(args, "requires single branch name",  repo);
            repo.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }

    /**
     * checks if one input is given.
     * @param args
     * @param repo
     * @param msg
     */
    public static void checkSoloArg(String[] args, String msg, Repo repo) {
        GLet.checkArgs(args, 2, 3,
                msg, repo.gitlet());
    }

    /**
     * checks to make sure args are formatted corrently
     * in checkout specifically.
     * @param args
     * @param repo
     */
    private static void checkCheckoutArgs(String[] args, Repo repo) {
        switch (args.length) {
        case 2:
            repo.checkoutBranch(args[1]);
            break;
        case 3:
            if (!Objects.equals(args[1], "--")) {
                GLet.error("Incorrect operands.");
            }
            repo.checkoutFile(args[2]);
            break;
        case 4:
            if (!Objects.equals(args[2], "--")) {
                GLet.error("Incorrect operands.");
            }
            repo.checkoutFile(args[1], args[3]);
            break;
        default:
            GLet.error("invalid checkout arguments");
        }
    }

    /**
     * checks to make sure args are formatted corrently
     * in commit specifically.
     * @param args
     * @param repo
     */
    private static void checkCommitArgs(String[] args, Repo repo) {
        if (args.length > 2) {
            GLet.error("too many arguments for commit");
        } else if (args.length == 1 || args[1].equals("")) {
            GLet.error("Please enter a commit message.");
        }
    }

}
