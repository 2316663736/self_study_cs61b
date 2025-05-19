package gitlet;

import java.io.File;
import java.util.List;

/**
 * 对暂存区进行操作
 */
public class StagingArea {
    private static final File stagingArea = Repository.GITLET_TEM_DIR;
    private static final File stagingAreaDelete = Repository.GITLET_TEM_DIR_DELETE;

    public static void init() {
        Tools.createDir(stagingArea);
        Tools.createDir(stagingAreaDelete);
    }

    public static boolean haveChangeInStagingArea() {
        List<String> filesAdd = Utils.plainFilenamesIn(stagingArea);
        List<String> filesRemove = Utils.plainFilenamesIn(stagingAreaDelete);
        if ((filesAdd == null || filesAdd.isEmpty()) && (filesRemove == null || filesRemove.isEmpty())) {
            return false;
        }
        return true;
    }
    public static void addStagingArea(File file) {
        // 从删除暂存区移除（如果存在）
        deleteFrom(file, stagingAreaDelete);

        // 获取当前文件内容
        byte[] content = Utils.readContents(file);
        String contentHash = Utils.sha1((Object) content);

        // 获取当前HEAD提交
        String head = Tools.readHeadCommitId();
        Commit currentCommit = Commit.readCommit(Tools.getObjectFile(head, Repository.GITLET_FILE_DIR));

        // 检查文件是否在当前提交中且内容相同
        if (currentCommit.fileExists(file.getName())) {
            String commitFileSHA = currentCommit.getFileSHA(file);
            if (contentHash.equals(commitFileSHA)) {
                // 如果文件内容与当前提交相同，从暂存区移除
                removeStagingArea(file);
                return; // 不再继续往下暂存
            }
        }

        // 内容不同或未在当前提交中，将文件暂存
        File toWrite = Utils.join(stagingArea, file.getName());
        Tools.writeContent(toWrite, content);
    }

    public static boolean removeStagingArea(File file) {
        return deleteFrom(file, stagingArea);
    }

    private static boolean deleteFrom(File file, File stagingArea) {
        List<String> fileNames = Utils.plainFilenamesIn(stagingArea);
        if (fileNames != null) {
            String filename = file.getName();
            for (String name : fileNames) {
                if (filename.equals(name)) {
                    Utils.join(stagingArea, name).delete();
                    return true;
                }
            }
        }
        return false;
    }

    public static void addStagingAreaDelete(File file) {
        String filename = file.getName();
        File toWrite = Utils.join(stagingAreaDelete, filename);
        byte[] con = Utils.readContents(file);
        Utils.writeContents(toWrite, con);
    }

    public static Commit updateCommit(Commit commit) {
        List<String> fileNames = Utils.plainFilenamesIn(stagingArea);
        List<String> fileNamesDelete = Utils.plainFilenamesIn(stagingAreaDelete);
        if (fileNames != null) {
            for (String fileName : fileNames) {
                File file = Utils.join(stagingArea, fileName);
                byte[] cont = Utils.readContents(file);
                Tools.writeContent(Tools.getObjectFile(Utils.sha1((Object) cont), Repository.GITLET_FILE_DIR), cont);
                commit.put(fileName, Utils.sha1((Object) cont));
                file.delete();
            }
        }
        if (fileNamesDelete != null && !fileNamesDelete.isEmpty()) {
            for (String fileName : fileNamesDelete) {
                commit.remove(fileName);
                Utils.join(stagingAreaDelete, fileName).delete();
            }
        }
        return commit;
    }
}
