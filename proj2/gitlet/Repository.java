package gitlet;

import java.io.File;
import java.util.*;

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
    public static final File GITLET_TEM_DIR_DELETE = Utils.join(GITLET_TEM_DIR, "delete");

    /*  fill in the rest of this class. */
    public static void init() {
        if (gitletExist()) {
            throw new GitletException("A Gitlet version-control system" +
                    " already exists in the current directory.");
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
        if (!StagingArea.haveChangeInStagingArea()) {
            throw new GitletException("No changes added to the commit.");
        }

        String headCommit = Tools.readHeadCommitId();
        String headBranch = Tools.readHeadBranch();
        Commit lastCommit = Commit.readCommit(Tools.getObjectFile(headCommit, GITLET_FILE_DIR));
        Commit commit = new Commit(msg, headCommit, lastCommit);
        File branchName = Utils.join(GITLET_BRANCHES_DIR, Tools.readHeadBranch());
        Branch branch = Branch.readBranch(branchName);
        //写入文件,并清空tem
        commit = StagingArea.updateCommit(commit);
        //写入commit
        commit.writeCommit(Tools.getObjectFile(commit.toString(), GITLET_FILE_DIR));
        //更新head
        Utils.writeContents(GITLET_HEAD, commit.toString() + headBranch);
        //更新branch,
        branch.add(commit.toString());
        branch.writeBranch(branchName);
    }

    public static void rm(String fileName) {
        checkGitlet();

        boolean find = false;
        File file = Utils.join(CWD, fileName);
        String headCommitId = Tools.readHeadCommitId();
        Commit commit = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        if (commit.fileExists(fileName)) {
            StagingArea.addStagingAreaDelete(file);
            find = true;
            file.delete();
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
                Commit currentCommit = Commit.readCommit(
                        Tools.getObjectFile(nowBranch.getNewest(), GITLET_FILE_DIR));
                Commit.printLog(currentCommit);
            }
        }
    }

    public static void find(String commitMessage) {
        checkGitlet();
        boolean find = false;
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String fileName : branchNames) {
                Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, fileName));
                Commit currentCommit = Commit.readCommit(Tools.getObjectFile(
                        nowBranch.getNewest(), GITLET_FILE_DIR));
                List<String> res = Commit.find(commitMessage, currentCommit);
                for (String re : res) {
                    System.out.println(re);
                    find = true;
                }
            }
        }
        if (find) {
            return;
        }
        throw new GitletException("Found no commit with that message.");
    }
    public static void status() {
        checkGitlet();
        //=== Branches ===
        System.out.println("=== Branches ===");
        String nowBranch = Tools.readHeadBranch();
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
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

        TreeMap<String, String> allFileStatusMap = getAllFileStatus();
        for (String fileName : allFileStatusMap.keySet()) {
            String status = allFileStatusMap.get(fileName);
            if (status.equals("modified") || status.equals("deleted")) {
                System.out.println(fileName + " (" + status + ")");
            }
        }

        System.out.println();
        //=== Untracked Files ===
        System.out.println("=== Untracked Files ===");
        for (String fileName : allFileStatusMap.keySet()) {
            String status = allFileStatusMap.get(fileName);
            if (status.equals("untracked")) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }


    public static void checkout(String ... msg) {
        checkGitlet();
        if (msg.length == 2) {

            if (!Branch.branchExists(msg[1])) {
                throw new GitletException("No such branch exists.");
            } else if (Tools.readHeadBranch().equals(msg[1])) {
                throw new GitletException("No need to checkout the current branch.");
            } else if (anyFileUntracked()) {
                throw new GitletException("There is an untracked file in the way; delete it, " +
                        "or add and commit it first.");
            }
            Branch outBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, msg[1]));
            StagingArea.deleteStagingArea();  //清除暂存区
            changeToCommit(outBranch.getNewest());
            String newHead = outBranch.getNewest() + msg[1];
            Utils.writeContents(GITLET_HEAD, newHead);
        } else if (msg.length == 3 || msg.length == 4) {
            String fileName = msg[msg.length - 1];
            String commitID = null;
            if (msg.length == 3) {
                if (!msg[1].equals("--")) {
                    throw new GitletException("Incorrect operands.");
                }
                commitID = Tools.readHeadCommitId();
            } else {
                if (!msg[2].equals("--")) {
                    throw new GitletException("Incorrect operands.");
                }
                try {
                    commitID = Tools.getFullSha1(msg[1], GITLET_FILE_DIR);
                    if (findBranchOfCommit(commitID) == null) {
                        throw new GitletException("No such branch.");
                    }
                } catch (GitletException e) {
                    throw new GitletException("No commit with that id exists.");
                }
            }
            Commit nowCommit = Commit.readCommit(Tools.getObjectFile(commitID, GITLET_FILE_DIR));
            if (!nowCommit.fileExists(fileName)) {
                throw new GitletException("File does not exist in that commit.");
            }
            changeOneFileCWD(fileName, nowCommit.getFileSHA(fileName));
        }
    }

    public static void branch(String branchName) {
        checkGitlet();
        if (Branch.branchExists(branchName)) {
            throw new GitletException("A branch with that name already exists.");
        }
        Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR,
                Tools.readHeadBranch()));
        nowBranch.writeBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));
    }

    public static void rmBranch(String branchName) {
        checkGitlet();
        if (!Branch.branchExists(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        String headBranch = Tools.readHeadBranch();
        if (headBranch.equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        File deleteBranch = Utils.join(GITLET_BRANCHES_DIR, branchName);
        deleteBranch.delete();
    }

    public static void reset(String commitID) {
        checkGitlet();
        String branch = null;
        try {
            commitID = Tools.getFullSha1(commitID, GITLET_FILE_DIR);
            branch = findBranchOfCommit(commitID);
            if (branch == null) {
                throw new GitletException("No such branch.");
            }
        } catch (GitletException e) {
            throw new GitletException("No commit with that id exists.");
        }
        if (anyFileUntracked()) {
            throw new GitletException("There is an untracked file in the way;" +
                    " delete it, or add and commit it first.");
        }
        changeToCommit(commitID);
        //更新branch相关
        Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR,branch));
        nowBranch.reset(commitID);
        nowBranch.writeBranch(Utils.join(GITLET_BRANCHES_DIR, branch));
        //更新head以及清除暂存区
        String newHead = commitID + branch;
        Utils.writeContents(GITLET_HEAD, newHead);
        StagingArea.deleteStagingArea();
    }

    public static void merge(String branchName) {
        checkGitlet();
    }

    private static void changeOneFileCWD(String fileName, String sha1OfFile) {
        File pathToWrite = Utils.join(CWD, fileName);
        //sha1为null，代表需要删除
        if (sha1OfFile == null) {
            if (pathToWrite.exists()) {
                pathToWrite.delete();
            }
            return;
        }
        //sha1不合法
        if (!Tools.isValidSha1(sha1OfFile, true)) {
            throw new GitletException("Sha1 " + sha1OfFile + " is not valid.");
        }
        byte[] con = Utils.readContents(Tools.getObjectFile(sha1OfFile, GITLET_FILE_DIR));
        Utils.writeContents(pathToWrite, (Object) con);
    }

    private static void changeToCommit(String commitID) {
        if (!Tools.isValidSha1(commitID, true)) {
            throw new GitletException("Sha1 " + commitID + " is not valid.");
        }
        Commit tarCommit = Commit.readCommit(Tools.getObjectFile(commitID, GITLET_FILE_DIR));
        List<String> tarFiles = tarCommit.getAllFiles();
        List<String> nowFileNames = Utils.plainFilenamesIn(CWD);
        List<String> allFileNames = Tools.mergeAndSort(tarFiles, nowFileNames);
        for (String fileName : allFileNames) {
            if (tarFiles == null || !tarFiles.contains(fileName)) {
                changeOneFileCWD(fileName, null);
            } else if (nowFileNames != null && nowFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        Utils.join(GITLET_FILE_DIR, fileName))) {
                    changeOneFileCWD(fileName, tarCommit.getFileSHA(fileName));
                }
            } else {
                changeOneFileCWD(fileName, tarCommit.getFileSHA(fileName));
            }
        }
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

    private static TreeMap<String, String> getAllFileStatus() {
        //存储各个文件状态的Map，前者是文件名，后者是状态
        // none表示无事
        // delete表示被删除了（不是使用rm删除）
        //modified表示修改了
        //untracked，未跟踪
        TreeMap<String, String> result = new TreeMap<>();

        String headCommitId = Tools.readHeadCommitId();
        //当前目录下的文件
        List<String> nowFileNames = Utils.plainFilenamesIn(CWD);
        Commit nowCommit = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        //上一次commit中的文件
        List<String> commitFileNames = nowCommit.getAllFiles();
        //暂存文件
        List<String> stagedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR);
        //上述三者文件
        List<String> allFileNames = Tools.mergeAndSort(nowFileNames,
                stagedFileNames, commitFileNames);
        //rm指令删除的文件，为了判断delete类型
        List<String> deletedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR_DELETE);
        for (String fileName : allFileNames) {
            String temp = "none";
            if (nowFileNames != null && !nowFileNames.contains(fileName)) {
                if (deletedFileNames != null && !deletedFileNames.contains(fileName)) {
                    temp = "deleted";
                }
            } else if (stagedFileNames != null && stagedFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        Utils.join(GITLET_TEM_DIR, fileName))) {
                    temp = "modified";
                }
            } else if (commitFileNames != null && commitFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        nowCommit.getFileSHA(fileName))) {
                    temp = "modified";
                }
            } else {
                temp = "untracked";
            }
            result.put(fileName, temp);
        }
        return result;
    }

    private static boolean anyFileUntracked() {
        TreeMap<String, String> allFileStatus = getAllFileStatus();
        for (String fileName : allFileStatus.keySet()) {
            if (allFileStatus.get(fileName).equals("untracked")) {
                return true;
            }
        }
        return false;
    }

    private  static String findBranchOfCommit(String commitID) {
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String branchName : branchNames) {
                Branch now = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));
                if (now.containsCommitID(commitID)) {
                    return branchName;
                }
            }
        }

        return null;
    }
}
