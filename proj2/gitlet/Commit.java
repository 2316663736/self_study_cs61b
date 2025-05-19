package gitlet;

// : any imports you need here



import java.io.File;
import java.util.Date; // : You'll likely use this in this class
import java.util.HashMap;
import java.util.Map;
import java.util.Formatter;
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
public class Commit implements Dumpable {
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
    private Map<String, String> files = null;
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
    public Commit(String message, String father, String merge) {
        this.message = message;
        this.date = new Date();
        this.father = father;
        this.merge = merge;
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
        Formatter formatter = new Formatter();
        formatter.format("Date: %ta %tb %td %tT %tY %tz",date, date, date, date, date, date);
        String formattedDate = formatter.toString();
        formatter.close();

        System.out.println("===");
        System.out.println("commit " + Utils.sha1((Object) Utils.serialize(this)));
        if (merge != null) {
            System.out.println("Merge: " + father.substring(0,7) + " " + merge.substring(0,7));
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

    public static Commit readCommit(File file) {
        return Utils.readObject(file, Commit.class);
    }

    public static void printLog(Commit now) {
        now.dump();
        if (now.father == null) {
            return;
        }
        printLog(readCommit(Tools.getObjectFile(now.father, Repository.GITLET_FILE_DIR)));
    }

    public static String find(String commitMessage, Commit nowCommit) {
        if (nowCommit.message.equals(commitMessage)) {
            return nowCommit.toString();
        }
        if (nowCommit.father == null) {
            return null;
        }
        return find(commitMessage, readCommit(Tools.getObjectFile(nowCommit.father, Repository.GITLET_FILE_DIR)));
    }

}
