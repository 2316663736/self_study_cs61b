package gitlet;

import java.io.File;
import java.util.Date;
import java.util.List;

import static gitlet.Utils.*;

// : any imports you need here

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author qianye
 */
public class Repository {
    /*
     *  add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * 存储所有文件数据，包括需要的文件，commit信息(除了branch信息单独存储)。
     * 使用sha-1作为所有查找，前两位作为目录，后面作为文件名（参考实际git）
     */
    public static final File GITLET_FILE_DIR = Utils.join(GITLET_DIR, "file");

    /**
     * 存储分支，每个子文件夹都是一个分支（文件夹名字是分支名）
     */
    public static final File GITLET_BRANCHES_DIR = Utils.join(GITLET_DIR, "branches");
    /**
     * 默认的branch
     */
    public static final String GITLET_BRANCH_DEFAULT = "master";

    /**
     * 指向当前所在位置，前40位存储一个sha-1值，后面存储branch的名字
     */
    public static final File GITLET_HEAD = Utils.join(GITLET_DIR, "head");

    /**
     * 暂存文件，在这个目录下，名字就是文件名，内容是文件内容
     */
    public static final File GITLET_TEM_DIR = Utils.join(GITLET_DIR, "tem");

    /**
     * 暂存目录下的即将被取消跟踪的文件
     */
    public static final File GITLET_TEM_DIR_DELETE = Utils.join(GITLET_TEM_DIR,"delete");

    /*  fill in the rest of this class. */
    public static void init() {
        if (gitletExist()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        Commit init = new Commit("initial commit", new Date(0), null);
        String commitId = init.toString();
        Branch master = new Branch(commitId);
        String headIn = commitId + GITLET_BRANCH_DEFAULT;

        Tools.createDir(GITLET_DIR);
        Tools.createDir(GITLET_BRANCHES_DIR);
        Tools.createDir(GITLET_FILE_DIR);
        StagingArea.init();
        master.writeBranch(Utils.join(GITLET_BRANCHES_DIR, GITLET_BRANCH_DEFAULT));
        init.writeCommit(Tools.getObjectFile(commitId, GITLET_FILE_DIR));
        Utils.writeContents(GITLET_HEAD, headIn);
    }

    public static void add(String fileName) {
        checkGitlet();
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        StagingArea.addStagingArea(file);
    }
    public static void commit(String msg) {
        checkGitlet();
        List<String> filesAdd = Utils.plainFilenamesIn(GITLET_TEM_DIR);
        List<String> filesRemove = Utils.plainFilenamesIn(GITLET_TEM_DIR_DELETE);
        if ((filesAdd == null || filesAdd.isEmpty()) && (filesRemove == null || filesRemove.isEmpty())) {
            throw new GitletException("No changes added to the commit." );
        }

        String HeadCommit = Tools.readHeadCommitId();
        Commit lastCommit = Commit.readCommit(Tools.getObjectFile(HeadCommit, GITLET_FILE_DIR));
        Commit commit = new Commit(msg, HeadCommit, lastCommit);
        File branchName = Utils.join(GITLET_BRANCHES_DIR, Tools.readHeadBranch());
        Branch branch = Branch.readBranch(branchName);
        //写入文件,并清空tem
        commit = StagingArea.updateCommit(commit);
        //写入commit
        commit.writeCommit(Tools.getObjectFile(commit.toString(), GITLET_FILE_DIR));
        //更新head
        Utils.writeContents(GITLET_HEAD, commit.toString() + HeadCommit);
        //更新branch,
        branch.add(commit.toString());
        branch.writeBranch(branchName);
    }

    public static void rm(String fileName) {
        checkGitlet();

        boolean find = false;
        File file = Utils.join(CWD, fileName);
        String Head = Tools.readHeadCommitId();
        Commit commit = Commit.readCommit(Tools.getObjectFile(Head, GITLET_FILE_DIR));
        if (commit.fileExists(fileName)) {
            StagingArea.addStagingAreaDelete(file);
            file.delete();
            find = true;
        }
        find = StagingArea.removeStagingArea(file) || find;
        if (!find) {
            throw new GitletException("No reason to remove the file.");
        }
    }
    public static void log() {
        checkGitlet();
        String headCommitId = Tools.readHeadCommitId();
        Commit current = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        Commit.printLog(current);
    }

    public static void globalLog() {
        checkGitlet();
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String fileName : branchNames) {
                Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, fileName));
                Commit currentCommit = Commit.readCommit(Tools.getObjectFile(nowBranch.getNewest(), GITLET_FILE_DIR));
                Commit.printLog(currentCommit);
            }
        }
    }

    public static void find(String commitMessage) {
        checkGitlet();
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String fileName : branchNames) {
                Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, fileName));
                Commit currentCommit = Commit.readCommit(Tools.getObjectFile(nowBranch.getNewest(), GITLET_FILE_DIR));
                String res = Commit.find(commitMessage, currentCommit);
                if (res != null) {
                    System.out.println(res);
                    return;
                }
            }
        }
        throw new GitletException("Found no commit with that message.");
    }
    public static void status() {
        checkGitlet();

        //=== Branches ===
        System.out.println("=== Branches ===");
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        String nowBranch = Tools.readHeadBranch();
        if (branchNames != null) {
            for (String branchName : branchNames) {
                if (branchName.equals(nowBranch)) {
                    System.out.print("*");
                }
                System.out.println(branchName);
            }
        }
        System.out.println();
        //=== Staged Files ===
        System.out.println("=== Staged Files ===");
        List<String> stagedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR);
        if (stagedFileNames != null) {
            for (String stagedFileName : stagedFileNames) {
                System.out.println(stagedFileName);
            }
        }
        System.out.println();
        //=== Removed Files ===
        System.out.println("=== Removed Files ===");
        List<String> removedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR_DELETE);
        if (removedFileNames != null) {
            for (String removedFileName : removedFileNames) {
                System.out.println(removedFileName);
            }
        }
        System.out.println();
        //=== Modifications Not Staged For Commit ===
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        //=== Untracked Files ===
        System.out.println("=== Untracked Files ===");

        System.out.println();
    }

    public static void checkout(String ... msg) {
        checkGitlet();
    }

    public static void branch(String branchName) {
        checkGitlet();
    }

    public static void rmBranch(String branchName) {
        checkGitlet();
    }

    public static void reset(String commitID) {
        checkGitlet();
    }

    public static void merge(String branchName) {
        checkGitlet();
    }

    /**
     * 返回.gitlet目录是否存在
     * @return 如果.gitlet存在则返回true，反之false
     */
    private static boolean gitletExist() {
        return GITLET_DIR.exists();
    }
    /**
     * 检查.gitlet目录是否存在，与上面的区别在于，如果不存在，会直接报错
     */
    private static void checkGitlet() {
        if (!gitletExist()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }
    }


}
