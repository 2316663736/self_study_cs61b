package gitlet;

import java.io.File;
import java.io.FileReader;
import java.util.ResourceBundle;

/**
 * 对暂存区进行操作
 */
public class StagingArea {
    private static File stagingArea = Repository.GITLET_TEM_DIR;
    private static File stagingAreaDelete = Repository.GITLET_TEM_DIR_DELETE;

    public static void init() {
        Tools.createDir(stagingArea);
        Tools.createDir(stagingAreaDelete);
    }

    public static void addStagingArea(File file) {
        deleteFrom(file, stagingAreaDelete);
        File toWrite = Utils.join(stagingArea, file.getName());
        byte[] content = Utils.readContents(file);
        Tools.writeContent(toWrite, content);
    }

    public static boolean removeStagingArea(File file) {
        return deleteFrom(file, stagingArea);
    }

    private static boolean deleteFrom(File file, File stagingArea) {
        File[] files = stagingArea.listFiles(File::isFile);
        if (files != null) {
            String filename = file.getName();
            for (File f : files) {
                if (filename.equals(f.getName())) {
                    f.delete();
                    return true;
                }
            }
        }
        return false;
    }

    public static void addStagingAreaDelete(File file) {
        String filename = file.getName();
        File toWrite = Utils.join(stagingAreaDelete, filename);
        String empty = "";
        Utils.writeContents(toWrite, empty);
    }

    public static Commit updateCommit(Commit commit) {
        File[] files = stagingArea.listFiles(File::isFile);
        File[] filesDelete = stagingAreaDelete.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                byte[] cont = Utils.readContents(file);
                Tools.writeContent(Tools.getObjectFile(Utils.sha1((Object) cont), Repository.GITLET_FILE_DIR), cont);
                commit.put(file.getName(), Utils.sha1((Object) cont));
                file.delete();
            }
        }
        if (filesDelete != null) {
            for (File file : filesDelete) {
                commit.remove(file.getName());
                file.delete();
            }
        }
        return commit;
    }
}
