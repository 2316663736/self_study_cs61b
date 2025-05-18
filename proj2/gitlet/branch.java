package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class branch implements Dumpable {
    /**
     * 存储当前branch的一个历史提交，从0到最后，代表由旧到新
     */
    private List<String> allID = null;

    /**
     * 创建一个新的，空的branch
     */
    public branch() {
        allID = new ArrayList<String>();
    }


    public branch(branch other) {
        this.allID = other.allID;
    }
    public void add(String id) {
        allID.add(id);
    }
    public String getNewest() {
        return allID.get(allID.size() - 1);
    }
    @Override
    public void dump() {
        for (int i = allID.size() - 1; i >= 0; i--) {
            System.out.println(allID.get(i));
        }
    }
    public static branch readBranch(String branchName) {
        return Utils.readObject(Utils.join(Repository.GITLET_BRANCHES_DIR,branchName), branch.class);
    }
    public void writeBranch(String branchName) {
        Utils.writeObject(Utils.join(Repository.GITLET_BRANCHES_DIR, branchName), this);
    }
}
