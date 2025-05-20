package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Branch implements Dumpable {
    /**
     * 存储当前branch的一个历史提交，从0到最后，代表由旧到新
     */
    private List<String> allID = null;
    private String headCommit = null;
    private static final File GITLET_BRANCHES_DIR = Repository.GITLET_BRANCHES_DIR;
    /**
     * 创建一个新的，空的branch
     */
    public Branch() {
        allID = new ArrayList<String>();
    }
    public Branch(String init) {
        this();
        this.add(init);
    }

    public Branch(Branch other) {
        this.allID = other.allID;
    }

    public boolean containsCommitID(String id) {
        return allID.contains(id);
    }
    public void add(String id) {
        allID.add(id);
        headCommit = id;
    }
    public String getNewest() {
        return headCommit;
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

    public static boolean branchExists(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        return allBranchNames != null && allBranchNames.contains(branchName);
    }
}
