package gitlet;

import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.util.Date;

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
    public static final File GITLET_FILE_DIR = new File(GITLET_DIR, "file");

    /**
     * 存储分支，每个子文件夹都是一个分支（文件夹名字是分支名）
     */
    public static final File GITLET_BRANCHES_DIR = new File(GITLET_DIR, "branches");
    /**
     * 默认的branch
     */
    public static final String GITLET_BRANCH_DEFAULT = "master";

    /**
     * 指向当前所在位置，前40位存储一个sha-1值，后面存储branch的名字
     */
    public static final File GITLET_HEAD = new File(GITLET_DIR, "head");

    /**
     * 暂存文件，在这个目录下，名字就是文件名，内容是文件内容
     */
    public static final File GITLET_TEM_DIR = new File(GITLET_DIR, "tem");

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
        Tools.createDir(GITLET_TEM_DIR);
        master.writeBranch(Utils.join(GITLET_BRANCHES_DIR, GITLET_BRANCH_DEFAULT));
        init.writeCommit(Tools.getObjectFile(commitId, GITLET_FILE_DIR));
        Utils.writeContents(GITLET_HEAD, headIn);
    }

    public static void add(String fileName) {
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        byte[] cont = Utils.readContents(file);
        File target = new File(GITLET_TEM_DIR, fileName);
        Tools.writeContent(target, cont);
    }
    public static void commit(String msg) {
        File[] files = GITLET_TEM_DIR.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            throw new GitletException("No changes added to the commit." );
        }

        String Head = readHead();
        Commit lastCommit = Commit.readCommit(Tools.getObjectFile(Head.substring(0, UID_LENGTH), GITLET_FILE_DIR));
        Commit commit = new Commit(msg, Head.substring(0,Utils.UID_LENGTH), lastCommit);
        File branchName = Utils.join(GITLET_BRANCHES_DIR,Head.substring(Utils.UID_LENGTH));
        Branch branch = Branch.readBranch(branchName);
        //写入文件,并清空tem
        for (File file : files) {
            byte[] cont = Utils.readContents(file);
            Tools.writeContent(Tools.getObjectFile(Utils.sha1((Object) cont), GITLET_FILE_DIR), cont);
            commit.put(file.getName(), Utils.sha1((Object) cont));
            file.delete();
        }
        //写入commit
        commit.writeCommit(Tools.getObjectFile(commit.toString(), GITLET_FILE_DIR));
        //更新head
        Utils.writeContents(GITLET_HEAD, commit.toString()+Head.substring(UID_LENGTH));
        //更新branch,
        branch.add(commit.toString());
        branch.writeBranch(branchName);
    }
    public static void rm(String fileName) {

    }
    public static void log() {

    }

    public static void globalLog() {

    }

    public static void find(String commitMessage) {
        System.err.println("Found no commit with that message.");
    }
    public static void status() {

    }

    public static void checkout(String ... msg) {

    }

    public static void branch(String branchName) {

    }

    public static void rmBranch(String branchName) {

    }

    public static void reset(String commitID) {

    }

    public static void merge(String branchName) {

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
            throw new GitletException("Gitlet does not exist.");
        }
    }

    /**
     * 读取head中的值，并返回（由于比较常用，所以单独提出来）
     * 此函数会顺便检查是否存在.gitlet目录
     * @return head中的内容
     */
    private static String readHead() {
        checkGitlet();
        return readContentsAsString(GITLET_HEAD);
    }
}
