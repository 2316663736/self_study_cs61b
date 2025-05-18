package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class tool {



    public static final int errorNoArgs = 0;
    public static final int errorNoCommand = 1;
    public static final int errorIncorrectOperand = 2;
    public static  void exitWithErrorExist(int err) {
        switch (err) {
            case errorNoArgs:
                System.out.println("Please enter a command.");
                break;
            case errorNoCommand:
                System.out.println("No command with that name exists.");
                break;
            case errorIncorrectOperand:
                System.out.println("Incorrect operand.");
                break;
            default:
                System.out.println("Unknown error in Main exitWithErrorExist.");
        }
        System.exit(0);
    }
    public static  void exitWithErrorNotExist(String err, int errCode) {
        System.out.println(err);
        System.exit(errCode);
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
        if (strict && s.length() != 40) {
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
    public static String getFullSha1(String shaPrefix, File FILE_DIR) {
        if (shaPrefix == null || shaPrefix.length() < 1) {
            return null;
        }

        // 将前缀标准化为小写
        shaPrefix = shaPrefix.toLowerCase();

        // 如果已经是完整的SHA-1，直接检查是否存在
        if (shaPrefix.length() == 40) {
            File objFile = getObjectFile(shaPrefix, FILE_DIR);
            return objFile.exists() ? shaPrefix : null;
        }
        if (!isValidSha1(shaPrefix, false)) {
            throw new GitletException("Invalid sha1: " + shaPrefix);
        }

        // 获取前两位作为目录名
        String dirPrefix = shaPrefix.length() >= 2 ? shaPrefix.substring(0, 2) : shaPrefix;
        File objDir = Utils.join(FILE_DIR, dirPrefix);

        // 如果目录不存在，则没有匹配
        if (!objDir.exists() || !objDir.isDirectory()) {
            return null;
        }

        // 查找匹配的文件
        String remainingPrefix = shaPrefix.length() >= 2 ? shaPrefix.substring(2) : "";
        List<String> matchedShas = new ArrayList<>();

        for (File file : objDir.listFiles()) {
            String fileName = file.getName();
            // 检查文件名是否以剩余前缀开头
            if (fileName.startsWith(remainingPrefix)) {
                matchedShas.add(dirPrefix + fileName);
            }
        }

        // 根据匹配结果返回
        if (matchedShas.size() == 1) {
            return matchedShas.get(0);
        } else if (matchedShas.size() > 1) {
            // 多个匹配，返回null或抛出异常
            // 如果希望直接报错，可以改为:
             throw new GitletException("Ambiguous prefix: " + shaPrefix);
//            return null;
        } else {
            return null;
        }
    }

    /**
     * 根据SHA-1值获取对应的对象文件
     * @param sha1 完整的SHA-1哈希值
     * @return 对象文件
     */
    public static File getObjectFile(String sha1, File FILE_DIR) {

        if (! isValidSha1(sha1, true)) {
            throw new GitletException("Invalid SHA-1: " + sha1);
        }

        String dirName = sha1.substring(0, 2);
        String fileName = sha1.substring(2);

        return Utils.join(FILE_DIR, dirName, fileName);
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
            throw new GitletException("无法创建目录: " + dir.getPath());
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
                throw new GitletException("无法创建文件: " + f.getPath());
            }
        } catch (IOException e) {
            throw new GitletException("创建文件时发生IO错误: " + e.getMessage());
        }
    }
    public static void writeContent(File f, byte[] content) {
        createFile(f);
        Utils.writeContents(f, (Object) content);
    }
}
