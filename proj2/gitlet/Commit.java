package gitlet;

// : any imports you need here



import java.io.File;
import java.io.Serializable; // Import Serializable
import java.util.*;

import static gitlet.Repository.GITLET_FILE_DIR;

/** Represents a gitlet commit object.
 *   存储commit信息，
 *   包括提交时的message，
 *   父提交（如果是merge得到的，则有两个），
 *   提交时间，
 *   提交文件与SHA-1对应map。
 *
 *
 *  @author qianye
 */
public class Commit implements Dumpable, Serializable { // Implement Serializable
    /*
       add instance variables here.
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /**
     * 提交时间
     */
    private Date date;
    /**
     * 父提交
     */
    private String father;
    /**
     * 父提交（被合并的分支）
     */
    private String merge = null;
    /**
     * 文件名到sha1的映射
     */
    private Map<String, String> files; // Initialized to null by default or in constructor
    /* : fill in the rest of this class. */

    /**
     * 普通初始化Commit类
     * @param message 提交信息
     * @param father 父提交
     */
    public Commit(String message, String father, Commit lastCommit) {
        this.message = message;
        this.date = new Date();
        this.father = father;
        if (lastCommit.files == null) {
            return;
        }
        this.files = lastCommit.files;
    }

    /**
     * 支持自定义时间的Commit初始化（应该只有init使用）
     * @param message 提交信息
     * @param date    提交时间
     * @param father  父提交
     */
    public Commit(String message, Date date, String father) {
        this.message = message;
        this.date = date;
        this.father = father;
    }
    /**
     * 初始化Commit类（merge时使用）
     * @param message 提交信息
     * @param father 父提交（当前所在的分支）
     * @param merge 父提交（被合并进来的分支）
     */
    public Commit(String message, String father, String merge, Commit lastCommit) {
        this.message = message;
        this.date = new Date();
        this.father = father;
        this.merge = merge;
        if (lastCommit.files == null) {
            return;
        }
        this.files = lastCommit.files;
    }
    /**
     * 加入文件名到文件sha-1值的映射
     * @param key 文件名
     * @param value 文件sha-1值
     */
    public void put(String key, String value) {
        if (files == null) {
            files = new HashMap<>();
        }
        files.put(key, value);
    }


    public String remove(String key) {
        if (files == null) {
            return null;
        }
        return files.remove(key);
    }

    public boolean fileExists(String key) {
        if (files == null) {
            return false;
        }
        return files.containsKey(key);
    }
    /**
     * 输出当前commit的信息，可以用于输出log或者debug
     */
    @Override
    public void dump() {
        Formatter formatter = new Formatter(Locale.US);
        formatter.format("Date: %ta %tb %td %tT %tY %tz", date, date, date, date, date, date);
        String formattedDate = formatter.toString();
        formatter.close();

        System.out.println("===");
        System.out.println("commit " + Utils.sha1((Object) Utils.serialize(this)));
        if (merge != null) {
            System.out.println("Merge: " + father.substring(0, 7) + " " + merge.substring(0, 7));
        }
        System.out.println(formattedDate);
        System.out.println(message);
        System.out.println();
    }

    /**
     * @return 返回这个commit的sha-1值
     */
    @Override
    public String toString() {
        return Utils.sha1((Object) Utils.serialize(this));
    }
    public String getFileSHA(String fileName) {
        if (files == null) {
            return null;
        }
        return files.get(fileName);
    }
    public String getFileSHA(File file) {
        if (file == null) {
            return null;
        }
        return files.get(file.getName());
    }

    public  void writeCommit(File file) {
        Tools.createFile(file);
        Utils.writeObject(file, this);
    }

    public List<String> getAllFiles() {
        List<String> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (String fileName : files.keySet()) {
            result.add(fileName);
        }
        return result;
    }

    /**
     * Returns a defensive copy of the tracked files map.
     * @return A new map containing file names and their SHA-1s, or an empty map if no files are tracked.
     */
    public Map<String, String> getTrackedFiles() {
        if (this.files == null) {
            return new HashMap<>(); // Or Collections.emptyMap();
        }
        return new HashMap<>(this.files);
    }

    public static Commit readCommit(File file) {
        return Utils.readObject(file, Commit.class);
    }

    public static void printLog(Commit now) {
        now.dump();
        if (now.father == null) {
            return;
        }
        printLog(readCommit(Tools.getObjectFile(now.father, GITLET_FILE_DIR)));
    }

    public static List<String> find(String commitMessage, Commit nowCommit) {
        List<String> res = new ArrayList<>();

        while (nowCommit != null) {
            if (nowCommit.message.equals(commitMessage)) {
                res.add(nowCommit.toString());
            }
            if (nowCommit.father == null) {
                break;
            }
            nowCommit = readCommit(Tools.getObjectFile(nowCommit.father,
                    GITLET_FILE_DIR));
        }
        return res;
    }

    /**
     * 查找两个提交的最近公共祖先（分裂点）
     * @param commit1ID 第一个提交的ID
     * @param commit2ID 第二个提交的ID
     * @return 分裂点的提交ID
     */
    public static String findSplitPoint(String commit1ID, String commit2ID) {
        // 获取第一个提交的所有祖先（包括自己）
        Set<String> ancestors1 = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(commit1ID);

        // 使用BFS遍历第一个提交的所有祖先
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (ancestors1.contains(current)) {
                continue;  // 避免环形引用或重复处理
            }

            ancestors1.add(current);
            Commit commit = Commit.readCommit(Tools.getObjectFile(current, GITLET_FILE_DIR));

            // 添加父提交到队列
            if (commit.father != null) {
                queue.offer(commit.father);
            }
            // 如果是合并提交，还需要添加第二个父提交
            if (commit.merge != null) {
                queue.offer(commit.merge);
            }
        }

        // 从第二个提交开始向上查找，第一个在ancestors1中出现的提交就是分裂点
        queue.clear();
        queue.offer(commit2ID);
        Set<String> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();

            // 如果当前提交已经在第一个提交的祖先中，则它就是分裂点
            if (ancestors1.contains(current)) {
                return current;
            }

            if (visited.contains(current)) {
                continue;  // 避免环形引用或重复处理
            }

            visited.add(current);
            Commit commit = Commit.readCommit(Tools.getObjectFile(current, GITLET_FILE_DIR));

            // 添加父提交到队列
            if (commit.father != null) {
                queue.offer(commit.father);
            }
            // 如果是合并提交，还需要添加第二个父提交
            if (commit.merge != null) {
                queue.offer(commit.merge);
            }
        }

        // 如果没有找到公共祖先，返回初始提交ID（理论上一定会有公共祖先）
        return null;
    }
    
    /**
     * Checks if 'ancestorCommitId' is an ancestor of or the same as 'descendantCommitId'.
     * Traverses backwards from descendantCommitId using parent pointers.
     * @param descendantCommitId The SHA1 of the commit to start traversal from.
     * @param ancestorCommitId The SHA1 of the potential ancestor commit.
     * @param objectsDir The directory where commit objects are stored (e.g., GITLET_FILE_DIR).
     * @return true if ancestorCommitId is an ancestor of or same as descendantCommitId.
     */
    public static boolean isAncestor(String descendantCommitId, String ancestorCommitId, File objectsDir) {
        if (descendantCommitId == null || ancestorCommitId == null) {
            return false;
        }
        // If they are the same commit, it's considered an ancestor for fast-forward purposes.
        if (descendantCommitId.equals(ancestorCommitId)) {
            return true;
        }

        Queue<String> toVisit = new LinkedList<>();
        toVisit.offer(descendantCommitId);
        // Using a Set to keep track of visited commits can prevent re-processing in complex histories,
        // though for simple parent traversal it might be overkill if commit graph is strictly a DAG.
        // However, it's good practice for graph traversals.
        Set<String> visitedDuringTraversal = new HashSet<>(); 

        while (!toVisit.isEmpty()) {
            String currentCommitId = toVisit.poll();

            if (visitedDuringTraversal.contains(currentCommitId)) {
                continue;
            }
            visitedDuringTraversal.add(currentCommitId);

            File commitFile = Tools.getObjectFile(currentCommitId, objectsDir);
            // If a commit object is missing from the objectsDir, something is wrong with the repo integrity.
            // For this check, we can treat it as the ancestor not being found down this path.
            if (!commitFile.exists()) { 
                continue; 
            }
            Commit currentCommit = Commit.readCommit(commitFile);

            // Check first parent
            if (currentCommit.father != null) {
                if (currentCommit.father.equals(ancestorCommitId)) {
                    return true;
                }
                toVisit.offer(currentCommit.father);
            }
            // Check second parent (if it's a merge commit)
            if (currentCommit.merge != null) { 
                if (currentCommit.merge.equals(ancestorCommitId)) {
                    return true;
                }
                toVisit.offer(currentCommit.merge);
            }
        }
        return false; // Ancestor not found in the history of the descendant
    }

    /**
     * Gets the ancestry path of a commit, tracing back via first parent.
     * @param commitId The SHA1 of the commit to start from.
     * @param objectsDir The directory where commit objects are stored.
     * @return A list of commit IDs from the initial commit to the given commitId, in chronological order.
     */
    public static List<String> getAncestryPath(String commitId, File objectsDir) {
        LinkedList<String> path = new LinkedList<>();
        String currentCommitId = commitId;
        while (currentCommitId != null) {
            path.addFirst(currentCommitId); // Add to the beginning to reverse order later
            File commitFile = Tools.getObjectFile(currentCommitId, objectsDir);
            if (!commitFile.exists()) {
                // Should not happen if commitId is valid and from a consistent repository state
                break; 
            }
            Commit currentCommit = Commit.readCommit(commitFile);
            currentCommitId = currentCommit.father; // Trace first parent
        }
        return path; // path is now in chronological order (root to commitId)
    }
}
