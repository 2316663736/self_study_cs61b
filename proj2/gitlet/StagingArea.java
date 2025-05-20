package gitlet;

import java.io.File;
import java.util.List;

/**
 * 对暂存区进行操作
 */
public class StagingArea {
    private static final File STAGING_AREA = Repository.GITLET_TEM_DIR;
    private static final File STAGING_AREA_DELETE = Repository.GITLET_TEM_DIR_DELETE;

    public static void init() {
        Tools.createDir(STAGING_AREA);
        Tools.createDir(STAGING_AREA_DELETE);
    }

    public static boolean haveChangeInStagingArea() {
        List<String> filesAdd = Utils.plainFilenamesIn(STAGING_AREA);
        List<String> filesRemove = Utils.plainFilenamesIn(STAGING_AREA_DELETE);
        if ((filesAdd == null || filesAdd.isEmpty()) &&
                (filesRemove == null || filesRemove.isEmpty())) {
            return false;
        }
        return true;
    }
    public static void addStagingArea(File file) {
        // 从删除暂存区移除（如果存在）
        deleteFrom(file, STAGING_AREA_DELETE);

        // 获取当前文件内容
        byte[] content = Utils.readContents(file);
        String contentHash = Utils.sha1((Object) content);

        // 获取当前HEAD提交
        String head = Tools.readHeadCommitId();
        Commit currentCommit = Commit.readCommit(Tools.getObjectFile(head,
                Repository.GITLET_FILE_DIR));

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
        File toWrite = Utils.join(STAGING_AREA, file.getName());
        Tools.writeContent(toWrite, content);
    }

    public static boolean removeStagingArea(File file) {
        return deleteFrom(file, STAGING_AREA);
    }

    private static boolean deleteFrom(File file, File stageArea) {
        List<String> fileNames = Utils.plainFilenamesIn(stageArea);
        if (fileNames != null) {
            String filename = file.getName();
            for (String name : fileNames) {
                if (filename.equals(name)) {
                    Utils.join(stageArea, name).delete();
                    return true;
                }
            }
        }
        return false;
    }

    public static void addStagingAreaDelete(File file) {
        String filename = file.getName();
        File toWrite = Utils.join(STAGING_AREA_DELETE, filename);
        byte[] con = Utils.readContents(file);
        Utils.writeContents(toWrite, con);
    }

    public static Commit updateCommit(Commit commit) {
        List<String> fileNames = Utils.plainFilenamesIn(STAGING_AREA);
        List<String> fileNamesDelete = Utils.plainFilenamesIn(STAGING_AREA_DELETE);
        if (fileNames != null) {
            for (String fileName : fileNames) {
                File file = Utils.join(STAGING_AREA, fileName);
                byte[] cont = Utils.readContents(file);
                Tools.writeContent(Tools.getObjectFile(Utils.sha1((Object) cont),
                        Repository.GITLET_FILE_DIR), cont);
                commit.put(fileName, Utils.sha1((Object) cont));
                file.delete();
            }
        }
        if (fileNamesDelete != null && !fileNamesDelete.isEmpty()) {
            for (String fileName : fileNamesDelete) {
                commit.remove(fileName);
                Utils.join(STAGING_AREA_DELETE, fileName).delete();
            }
        }
        return commit;
    }

    public static void deleteStagingArea() {
        List<String> fileNames = Utils.plainFilenamesIn(STAGING_AREA);
        List<String> fileNamesDelete = Utils.plainFilenamesIn(STAGING_AREA_DELETE);
        if (fileNames != null) {
            for (String fileName : fileNames) {
                File file = Utils.join(STAGING_AREA, fileName);
                file.delete();
            }
        }
        if (fileNamesDelete != null) {
            for (String fileName : fileNamesDelete) {
                File file = Utils.join(STAGING_AREA_DELETE, fileName);
                file.delete();
            }
        }
    }
}
