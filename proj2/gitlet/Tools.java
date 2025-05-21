package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import static gitlet.Utils.readContentsAsString;

public class Tools {

    public  static List<String> mergeAndSort(List<String>... lists) {
        // 创建TreeSet，它会自动去重和排序
        TreeSet<String> treeSet = new TreeSet<>();

        // 将所有列表的元素添加到TreeSet
        for (List<String> list : lists) {
            treeSet.addAll(list);
        }

        // 将TreeSet转换回List并返回
        return new ArrayList<>(treeSet);
    }

    public static String getSHA1ofFile(File file) {
        if (!file.exists()) {
            return null;
        }
        return Utils.sha1((Object) Utils.readContents(file));
    }
    public static boolean compareSHA1ofFile(File file1, File file2) {
        if (!file1.exists()) {
            return false;
        }
        if (!file2.exists()) {
            return false;
        }
        return Objects.equals(getSHA1ofFile(file1), getSHA1ofFile(file2));
    }
    public static boolean compareSHA1ofFile(File file1, String sha2) {
        if (!file1.exists()) {
            return false;
        }
        return Objects.equals(getSHA1ofFile(file1), sha2);
    }

    /**
     * 判断一个字符串是否是有效的SHA-1哈希值
     * @param s 要检查的字符串
     * @return 如果是有效的SHA-1哈希值则返回true
     */
    public static boolean isValidSha1(String s, boolean strict) {
        if (s == null) {
            return false;
        }

        // 检查长度是否为40（完整SHA-1）
        if (strict && s.length() != Utils.UID_LENGTH) {
            return false;
        }

        // 检查是否只包含有效的十六进制字符
        return s.matches("[a-f0-9]+");
    }

    /**
     * 根据SHA-1简写查找对应的完整SHA-1值
     * @param shaPrefix SHA-1前缀
     * @return 完整的SHA-1哈希值，如果没找到或找到多个匹配则返回null
     */
    public static String getFullSha1(String shaPrefix, File fileDirectory) {
        if (shaPrefix == null || shaPrefix.isEmpty()) {
            throw new GitletException("No commit with that id exists."); // Or invalid input
        }

        shaPrefix = shaPrefix.toLowerCase();

        // Check for invalid characters early.
        // isValidSha1(shaPrefix, false) allows any length, just checks chars.
        if (!isValidSha1(shaPrefix, false)) {
            // While "Invalid sha1" is descriptive, the requirement is to unify error messages.
            throw new GitletException("No commit with that id exists.");
        }

        // If already a full SHA-1
        if (shaPrefix.length() == Utils.UID_LENGTH) {
            File objFile = getObjectFile(shaPrefix, fileDirectory); // getObjectFile itself validates format
            if (objFile.exists()) {
                return shaPrefix;
            } else {
                throw new GitletException("No commit with that id exists.");
            }
        }

        // Handle short IDs
        // A short ID must be long enough to form a directory prefix if that's how objects are stored.
        // Typically, at least 2 characters for the directory.
        // The problem description implies any short ID should be processed.
        // Let's assume short IDs less than 2 chars are invalid or won't match.
        if (shaPrefix.length() < 2) { // Git usually requires a minimum length for short SHAs (e.g., 4-7)
                                      // For this structure, minimum 2 for dir.
            throw new GitletException("No commit with that id exists."); // Too short to be practically unique or find dir
        }

        String dirPrefix = shaPrefix.substring(0, 2);
        File objDir = Utils.join(fileDirectory, dirPrefix);

        if (!objDir.exists() || !objDir.isDirectory()) {
            throw new GitletException("No commit with that id exists.");
        }

        String remainingPrefix = shaPrefix.substring(2);
        List<String> matchedShas = new ArrayList<>();
        List<String> fileNames = Utils.plainFilenamesIn(objDir);

        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (fileName.startsWith(remainingPrefix)) {
                    // Before adding, ensure the full SHA corresponds to an existing file
                    String potentialFullSha = dirPrefix + fileName;
                    if (Tools.getObjectFile(potentialFullSha, fileDirectory).exists()) {
                        matchedShas.add(potentialFullSha);
                    }
                }
            }
        }

        if (matchedShas.size() == 1) {
            return matchedShas.get(0);
        } else if (matchedShas.size() > 1) {
            // Ambiguous prefix, but spec wants "No commit with that id exists."
            // This might hide useful info from user, but adhering to spec.
            throw new GitletException("No commit with that id exists.");
        } else {
            // No matches found
            throw new GitletException("No commit with that id exists.");
        }
    }

    /**
     * 根据SHA-1值获取对应的对象文件
     * @param sha1 完整的SHA-1哈希值
     * @return 对象文件
     */
    public static File getObjectFile(String sha1, File fileDirectory) {

        if (!isValidSha1(sha1, true)) {
            throw new GitletException("Invalid SHA-1: " + sha1 + ".");
        }

        String dirName = sha1.substring(0, 2);
        String fileName = sha1.substring(2);

        return Utils.join(fileDirectory, dirName, fileName);
    }

    /**
     * 创建dir这个目录，如果父目录不存在，则创建
     * @param dir 创建的目录
     */
    public static void createDir(File dir) {
        if (dir.exists()) {
            return;
        }

        // mkdirs() 会创建整个路径（包括所有必需但不存在的父目录）
        if (!dir.mkdirs()) {
            throw new GitletException("无法创建目录: " + dir.getPath() + ".");
        }
    }

    /**
     * 创建f这个文件，如果它的父目录不存在，则会进行创建
     * @param f 需要创建的文件
     */
    public static void createFile(File f) {
        if (f.exists()) {
            return;
        }

        // 确保父目录存在
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            createDir(parent);
        }

        // 创建文件
        try {
            if (!f.createNewFile()) {
                throw new GitletException("无法创建文件: " + f.getPath() + ".");
            }
        } catch (IOException e) {
            throw new GitletException("创建文件时发生IO错误: " + e.getMessage() + ".");
        }
    }
    public static void writeContent(File f, byte[] content) {
        createFile(f);
        Utils.writeContents(f, (Object) content);
    }

    /**
     * 读取head中的值，并返回（由于比较常用，所以单独提出来）
     * 此函数会顺便检查是否存在.gitlet目录
     * @return head中的内容
     */
    public static String readHead() {
        return readContentsAsString(Repository.GITLET_HEAD);
    }
    public static String readHeadCommitId() {
        return readHead().substring(0, Utils.UID_LENGTH);
    }
    public static String readHeadBranch() {
        return readHead().substring(Utils.UID_LENGTH);
    }
}
