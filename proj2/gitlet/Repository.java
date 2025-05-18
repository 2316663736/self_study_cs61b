package gitlet;

import java.io.File;
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
     * 指向当前所在位置，前40位存储一个sha-1值，后面存储branch的名字
     */
    public static final File GITLET_HEAD = new File(GITLET_DIR, "head");

    /**
     * 暂存文件，在这个目录下，名字就是文件名，内容是文件内容
     */
    public static final File GITLET_TEM = new File(GITLET_DIR, "tem");

    /*  fill in the rest of this class. */
    public static void init() {

    }

    public static void add(String fileName) {

    }
    public static void commit(Commit msg) {

    }
    public static void commit(String msg) {

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
}
