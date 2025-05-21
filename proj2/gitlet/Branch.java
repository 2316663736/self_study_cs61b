package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable; // Import Serializable

public class Branch implements Dumpable, Serializable { // Implement Serializable
    /**
     * 存储当前branch的一个历史提交，从0到最后，代表由旧到新
     */
    private List<String> allID; // Ensure it's private, initialized in constructor
    private String headCommit = null;
    private static final File GITLET_BRANCHES_DIR = Repository.GITLET_BRANCHES_DIR;
    /**
     * 创建一个新的，空的branch
     */
    public Branch() {
        allID = new ArrayList<>(); // Initialize here
    }
    public Branch(String init) {
        this(); // Calls the default constructor which initializes allID
        this.add(init);
    }

    public Branch(Branch other) {
        // Ensure a deep copy for `allID` if `other.allID` could be null
        // or if `getCommitHistory` is available.
        // For now, assuming `other.allID` is valid if `other` is a valid Branch object.
        // The subtask asks to replace direct access in Repository,
        // so this internal copy is okay for now.
        // However, if `getCommitHistory` was already implemented, using it would be cleaner:
        // this.allID = new ArrayList<>(other.getCommitHistory());
        // Defensive copy
        this.allID = new ArrayList<>(other.allID);
        // headCommit is a String, direct copy is fine
        this.headCommit = other.headCommit;
    }

    public boolean containsCommitID(String id) {
        return allID.contains(id);
    }

    /**
     * Returns a defensive copy of the commit history.
     * @return A new list containing all commit IDs in this branch's history.
     */
    public List<String> getCommitHistory() {
        return new ArrayList<>(this.allID);
    }

    public void add(String id) {
        allID.add(id);
        headCommit = id;
    }
    public String getNewest() {
        return headCommit;
    }
    public String getGlobalNewest() {
        return allID.get(allID.size() - 1);
    }

    public void reset(String ini) {
        headCommit = ini;
    }
    @Override
    public void dump() {
        for (int i = allID.size() - 1; i >= 0; i--) {
            System.out.println(allID.get(i));
        }
    }
    /**
     * 从文件读取Branch对象
     * @param file 包含Branch对象的文件
     * @return 读取的Branch对象
     */
    public static Branch readBranch(File file) {
        return Utils.readObject(file, Branch.class);
    }

    /**
     * 将当前Branch对象写入文件
     * @param file 要写入的文件
     */
    public void writeBranch(File file) {
        Tools.createFile(file);
        Utils.writeObject(file, this);
    }

    public static boolean branchExists(String branchPath) {
        // branchPath could be "master" or "origin/master"
        File branchFile = Utils.join(GITLET_BRANCHES_DIR, branchPath.replace("/", File.separator));
        return branchFile.exists() && branchFile.isFile();
    }
}
