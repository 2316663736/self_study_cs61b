package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// : any imports you need here

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author qianye
 */
public class Repository {
    /*
     *  add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * 存储所有文件数据，包括需要的文件，commit信息(除了branch信息单独存储)。
     * 使用sha-1作为所有查找，前两位作为目录，后面作为文件名（参考实际git）
     */
    public static final File GITLET_FILE_DIR = Utils.join(GITLET_DIR, "file");

    /**
     * 存储分支，每个子文件夹都是一个分支（文件夹名字是分支名）
     */
    public static final File GITLET_BRANCHES_DIR = Utils.join(GITLET_DIR, "branches");
    /**
     * 默认的branch
     */
    public static final String GITLET_BRANCH_DEFAULT = "master";

    /**
     * 指向当前所在位置，前40位存储一个sha-1值，后面存储branch的名字
     */
    public static final File GITLET_HEAD = Utils.join(GITLET_DIR, "head");

    /**
     * 暂存文件，在这个目录下，名字就是文件名，内容是文件内容
     */
    public static final File GITLET_TEM_DIR = Utils.join(GITLET_DIR, "tem");

    /**
     * 暂存目录下的即将被取消跟踪的文件
     */
    public static final File GITLET_TEM_DIR_DELETE = Utils.join(GITLET_TEM_DIR, "delete");


    public static final File GITLET_REMOTE_FILES_DIR = Utils.join(GITLET_DIR, "remote_files");
    /*  fill in the rest of this class. */
    public static void init() {
        if (gitletExist()) {
            throw new GitletException("A Gitlet version-control system already exists "
                    + "in the current directory.");
        }
        Commit init = new Commit("initial commit", new Date(0), null);
        String commitId = init.toString();
        Branch master = new Branch(commitId);
        String headIn = commitId + GITLET_BRANCH_DEFAULT;

        Tools.createDir(GITLET_DIR);
        Tools.createDir(GITLET_BRANCHES_DIR);
        Tools.createDir(GITLET_FILE_DIR);
        Tools.createDir(GITLET_REMOTE_FILES_DIR);
        StagingArea.init();
        master.writeBranch(Utils.join(GITLET_BRANCHES_DIR, GITLET_BRANCH_DEFAULT));
        init.writeCommit(Tools.getObjectFile(commitId, GITLET_FILE_DIR));
        Utils.writeContents(GITLET_HEAD, headIn);
    }

    public static void add(String fileName) {
        checkGitlet();
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        StagingArea.addStagingArea(file);
    }
    public static void commit(String msg) {
        checkGitlet();
        if (!StagingArea.haveChangeInStagingArea()) {
            throw new GitletException("No changes added to the commit.");
        }

        String headCommit = Tools.readHeadCommitId();
        String headBranch = Tools.readHeadBranch();
        Commit lastCommit = Commit.readCommit(Tools.getObjectFile(headCommit, GITLET_FILE_DIR));
        Commit commit = new Commit(msg, headCommit, lastCommit);
        File branchName = Utils.join(GITLET_BRANCHES_DIR, Tools.readHeadBranch());
        Branch branch = Branch.readBranch(branchName);
        //写入文件,并清空tem
        commit = StagingArea.updateCommit(commit);
        //写入commit
        commit.writeCommit(Tools.getObjectFile(commit.toString(), GITLET_FILE_DIR));
        //更新head
        Utils.writeContents(GITLET_HEAD, commit.toString() + headBranch);
        //更新branch,
        branch.add(commit.toString());
        branch.writeBranch(branchName);
    }

    public static void rm(String fileName) {
        checkGitlet();

        boolean find = false;
        File file = Utils.join(CWD, fileName);
        String headCommitId = Tools.readHeadCommitId();
        Commit commit = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        if (commit.fileExists(fileName)) {
            StagingArea.addStagingAreaDelete(file);
            find = true;
            file.delete();
        }
        find = StagingArea.removeStagingArea(file) || find;
        if (!find) {
            throw new GitletException("No reason to remove the file.");
        }

    }
    public static void log() {
        checkGitlet();
        String headCommitId = Tools.readHeadCommitId();
        Commit current = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        Commit.printLog(current);
    }

    public static void globalLog() {
        checkGitlet();
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String fileName : branchNames) {
                Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, fileName));
                Commit currentCommit = Commit.readCommit(
                        Tools.getObjectFile(nowBranch.getGlobalNewest(), GITLET_FILE_DIR));
                Commit.printLog(currentCommit);
            }
        }
    }

    public static void find(String commitMessage) {
        checkGitlet();
        boolean find = false;
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String fileName : branchNames) {
                Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, fileName));
                Commit currentCommit = Commit.readCommit(Tools.getObjectFile(
                        nowBranch.getGlobalNewest(), GITLET_FILE_DIR));
                List<String> res = Commit.find(commitMessage, currentCommit);
                for (String re : res) {
                    System.out.println(re);
                    find = true;
                }
            }
        }
        if (find) {
            return;
        }
        throw new GitletException("Found no commit with that message.");
    }
    public static void status() {
        checkGitlet();
        //=== Branches ===
        System.out.println("=== Branches ===");
        String nowBranch = Tools.readHeadBranch();
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String branchName : branchNames) {
                if (branchName.equals(nowBranch)) {
                    System.out.print("*");
                }
                System.out.println(branchName);
            }
        }
        System.out.println();
        //=== Staged Files ===
        System.out.println("=== Staged Files ===");
        List<String> stagedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR);
        if (stagedFileNames != null) {
            for (String stagedFileName : stagedFileNames) {
                System.out.println(stagedFileName);
            }
        }
        System.out.println();
        //=== Removed Files ===
        System.out.println("=== Removed Files ===");
        List<String> removedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR_DELETE);
        if (removedFileNames != null) {
            for (String removedFileName : removedFileNames) {
                System.out.println(removedFileName);
            }
        }
        System.out.println();
        //=== Modifications Not Staged For Commit ===
        System.out.println("=== Modifications Not Staged For Commit ===");

        TreeMap<String, String> allFileStatusMap = getAllFileStatus();
        for (String fileName : allFileStatusMap.keySet()) {
            String status = allFileStatusMap.get(fileName);
            if (status.equals("modified") || status.equals("deleted")) {
                System.out.println(fileName + " (" + status + ")");
            }
        }

        System.out.println();
        //=== Untracked Files ===
        System.out.println("=== Untracked Files ===");
        for (String fileName : allFileStatusMap.keySet()) {
            String status = allFileStatusMap.get(fileName);
            if (status.equals("untracked")) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }


    public static void checkout(String ... msg) {
        checkGitlet();
        if (msg.length == 2) {

            if (!Branch.branchExists(msg[1])) {
                throw new GitletException("No such branch exists.");
            } else if (Tools.readHeadBranch().equals(msg[1])) {
                throw new GitletException("No need to checkout the current branch.");
            } else if (anyFileUntracked()) {
                throw new GitletException("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
            Branch outBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, msg[1]));
            StagingArea.deleteStagingArea();  //清除暂存区
            changeToCommit(outBranch.getNewest());
            String newHead = outBranch.getNewest() + msg[1];
            Utils.writeContents(GITLET_HEAD, newHead);
        } else if (msg.length == 3 || msg.length == 4) {
            String fileName = msg[msg.length - 1];
            String commitID = null;
            if (msg.length == 3) {
                if (!msg[1].equals("--")) {
                    throw new GitletException("Incorrect operands.");
                }
                commitID = Tools.readHeadCommitId();
            } else {
                if (!msg[2].equals("--")) {
                    throw new GitletException("Incorrect operands.");
                }
                try {
                    commitID = Tools.getFullSha1(msg[1], GITLET_FILE_DIR);
                    if (findBranchOfCommit(commitID) == null) {
                        throw new GitletException("No such branch.");
                    }
                } catch (GitletException e) {
                    throw new GitletException("No commit with that id exists.");
                }
            }
            Commit nowCommit = Commit.readCommit(Tools.getObjectFile(commitID, GITLET_FILE_DIR));
            if (!nowCommit.fileExists(fileName)) {
                throw new GitletException("File does not exist in that commit.");
            }
            changeOneFileCWD(fileName, nowCommit.getFileSHA(fileName));
        }
    }

    public static void branch(String branchName) {
        checkGitlet();
        if (Branch.branchExists(branchName)) {
            throw new GitletException("A branch with that name already exists.");
        }
        Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR,
                Tools.readHeadBranch()));
        nowBranch.writeBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));
    }

    public static void rmBranch(String branchName) {
        checkGitlet();
        if (!Branch.branchExists(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        String headBranch = Tools.readHeadBranch();
        if (headBranch.equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        File deleteBranch = Utils.join(GITLET_BRANCHES_DIR, branchName);
        deleteBranch.delete();
    }

    public static void reset(String commitID) {
        checkGitlet();
        String branch = null;
        try {
            commitID = Tools.getFullSha1(commitID, GITLET_FILE_DIR);
            branch = findBranchOfCommit(commitID);
            if (branch == null) {
                throw new GitletException("No such branch.");
            }
        } catch (GitletException e) {
            throw new GitletException("No commit with that id exists.");
        }
        if (anyFileUntracked()) {
            throw new GitletException("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        changeToCommit(commitID);
        //更新branch相关
        Branch nowBranch = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, branch));
        nowBranch.reset(commitID);
        nowBranch.writeBranch(Utils.join(GITLET_BRANCHES_DIR, branch));
        //更新head以及清除暂存区
        String newHead = commitID + branch;
        Utils.writeContents(GITLET_HEAD, newHead);
        StagingArea.deleteStagingArea();
    }

    public static void merge(String branchName) {
        // 初始检查
        checkMergePrerequisites(branchName);

        String currentBranch = Tools.readHeadBranch();
        Branch current = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, currentBranch));
        Branch target = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));

        String currentCommitId = current.getNewest();
        String targetCommitId = target.getNewest();

        // 检查分裂点和特殊情况
        handleSplitPointCases(currentCommitId, targetCommitId, current, currentBranch);

        // 执行文件合并
        boolean conflict = mergeFiles(currentCommitId, targetCommitId);

        // 创建合并提交并更新引用
        createMergeCommit(currentCommitId, targetCommitId, currentBranch, branchName, current);

        // 只有遇到冲突时才输出消息
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * 检查合并前的先决条件
     */
    private static void checkMergePrerequisites(String branchName) {
        checkGitlet();
        if (StagingArea.haveChangeInStagingArea()) {
            throw new GitletException("You have uncommitted changes.");
        }
        // 检查分支是否存在
        if (!Branch.branchExists(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        // 检查是否与当前分支相同
        String currentBranch = Tools.readHeadBranch();
        if (currentBranch.equals(branchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        // 检查是否有未跟踪文件会被覆盖
        if (anyFileUntracked()) {
            throw new GitletException("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
    }

    /**
     * 处理分裂点相关的特殊情况
     */
    private static void handleSplitPointCases(String currentCommitId, String targetCommitId,
                                              Branch currentBranch, String branchName) {
        // 找到分支的分裂点
        String splitPoint = Commit.findSplitPoint(currentCommitId, targetCommitId);
        if (splitPoint == null) {
            throw new GitletException("No commit with that id exists.");
        }

        // 处理特殊情况
        if (splitPoint.equals(targetCommitId)) {
            // 指定分支是当前分支的祖先
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.equals(currentCommitId)) {
            // 当前分支是指定分支的祖先，执行快进
            currentBranch.add(targetCommitId);
            changeToCommit(targetCommitId);
            currentBranch.writeBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));
            String newHead = targetCommitId + branchName;
            Utils.writeContents(GITLET_HEAD, newHead);
            throw new GitletException("Current branch fast-forwarded.");
        }
    }

    /**
     * 执行文件合并，返回是否有冲突
     */
    private static boolean mergeFiles(String currentCommitId, String targetCommitId) {
        boolean conflict = false;

        // 获取三个提交
        Commit currentCommit = Commit.readCommit(Tools.getObjectFile(currentCommitId,
                GITLET_FILE_DIR));
        Commit targetCommit = Commit.readCommit(Tools.getObjectFile(targetCommitId,
                GITLET_FILE_DIR));
        String splitPoint = Commit.findSplitPoint(currentCommitId, targetCommitId);
        Commit splitCommit = Commit.readCommit(Tools.getObjectFile(splitPoint, GITLET_FILE_DIR));

        // 获取所有文件名
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(currentCommit.getAllFiles());
        allFiles.addAll(targetCommit.getAllFiles());
        allFiles.addAll(splitCommit.getAllFiles());

        for (String fileName : allFiles) {
            conflict = processFileMerge(fileName, currentCommit, targetCommit, splitCommit)
                    || conflict;
        }

        return conflict;
    }

    /**
     * 处理单个文件的合并逻辑
     */
    private static boolean processFileMerge(String fileName, Commit currentCommit,
                                            Commit targetCommit, Commit splitCommit) {
        String currentFileSHA = currentCommit.getFileSHA(fileName);
        String targetFileSHA = targetCommit.getFileSHA(fileName);
        String splitFileSHA = splitCommit.getFileSHA(fileName);

        if (Objects.equals(currentFileSHA, splitFileSHA)
                && !Objects.equals(targetFileSHA, splitFileSHA)) {
            // 1. 目标分支修改，当前分支未修改
            if (targetFileSHA != null) {
                // 目标分支更新了文件
                changeOneFileCWD(fileName, targetFileSHA);
                add(fileName);
            } else {
                // 目标分支删除了文件
                rm(fileName);
            }
            return false;
        } else if (!Objects.equals(currentFileSHA, splitFileSHA)
                && Objects.equals(targetFileSHA, splitFileSHA)) {
            // 2. 当前分支修改，目标分支未修改
            // 保持当前分支状态，不需要操作
            return false;
        }  else if (Objects.equals(currentFileSHA, targetFileSHA)) {
            // 3. 两个分支没有变化，或以相同方式变化
            // 保持当前状态，不需要操作
            return false;
        } else {
            // 4. 冲突情况：两个分支都修改且不同，或一个修改一个删除
            return handleFileConflict(fileName, currentFileSHA, targetFileSHA);
        }
    }

    /**
     * 处理文件冲突
     */
    private static boolean handleFileConflict(String fileName, String currentFileSHA,
                                              String targetFileSHA) {
        // 创建冲突内容
        String currentContent = "";
        if (currentFileSHA != null) {
            currentContent = new String(Utils.readContents(
                    Tools.getObjectFile(currentFileSHA, GITLET_FILE_DIR)
            ));
        }

        String targetContent = "";
        if (targetFileSHA != null) {
            targetContent = new String(Utils.readContents(
                    Tools.getObjectFile(targetFileSHA, GITLET_FILE_DIR)
            ));
        }

        // 创建冲突标记内容
        String conflictContent = String.join("\n",
                "<<<<<<< HEAD",
                currentContent,
                "=======",
                targetContent,
                ">>>>>>>",
                ""); // Adds a trailing newline like the original format

        // 写入工作目录并暂存
        File file = Utils.join(CWD, fileName);
        Utils.writeContents(file, conflictContent);
        add(fileName);

        return true; // 返回有冲突
    }

    /**
     * 创建合并提交并更新引用
     */
    private static void createMergeCommit(String currentCommitId, String targetCommitId,
                                          String currentBranch,
                                          String targetBranch, Branch current) {
        Commit currentCommit = Commit.readCommit(Tools.getObjectFile(currentCommitId,
                GITLET_FILE_DIR));

        // 创建合并提交
        String message = "Merged " + targetBranch + " into " + currentBranch + ".";
        Commit mergeCommit = new Commit(message, currentCommitId, targetCommitId, currentCommit);
        mergeCommit = StagingArea.updateCommit(mergeCommit);
        mergeCommit.writeCommit(Tools.getObjectFile(mergeCommit.toString(), GITLET_FILE_DIR));

        // 更新分支和HEAD指针
        current.add(mergeCommit.toString());
        current.writeBranch(Utils.join(GITLET_BRANCHES_DIR, currentBranch));
        Utils.writeContents(GITLET_HEAD, mergeCommit.toString() + currentBranch);
    }

    public static void addRemote(String remoteName, String remotePath) {
        checkGitlet();
        File remoteFile = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        if (remoteFile.exists()) {
            throw new GitletException("A remote with that name already exists.");
        }
        // Convert remotePath to an absolute path to the .gitlet directory
        String platformPath = remotePath.replace("/", File.separator);
        File remoteDirAsFile = new File(platformPath);
        String absoluteRemotePath = remoteDirAsFile.getAbsolutePath();
        
        Utils.writeContents(remoteFile, absoluteRemotePath);
    }

    public static void rmRemote(String remoteName) {
        checkGitlet();
        List<String> allRemoteFiles = Utils.plainFilenamesIn(GITLET_REMOTE_FILES_DIR);
        if (allRemoteFiles == null || !allRemoteFiles.contains(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        }
        File file = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        file.delete();
    }

    // Helper class for push method to return multiple File objects
    private static class RemotePaths {
        final File remoteGitletDir;
        final File remoteObjectsDir;
        final File remoteBranchesDir;
        final File remoteBranchFile;

        RemotePaths(File rgDir, File roDir, File rbDir, File rbrFile) {
            this.remoteGitletDir = rgDir;
            this.remoteObjectsDir = roDir;
            this.remoteBranchesDir = rbDir;
            this.remoteBranchFile = rbrFile;
        }
    }

    private static RemotePaths validateAndGetRemotePaths(String remoteName, String remoteBranchName) {
        File remoteNameFile = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        String remoteRepoPathString = Utils.readContentsAsString(remoteNameFile);
        if (remoteRepoPathString == null || remoteRepoPathString.isEmpty()) {
            throw new GitletException("Remote directory not found.");
        }

        File remoteGitletDir = new File(remoteRepoPathString); // Path stored is already to .gitlet dir
        if (!remoteGitletDir.exists() || !remoteGitletDir.isDirectory()) {
            // This check also implicitly verifies if the path is a valid .gitlet directory
            throw new GitletException("Remote directory not found.");
        }

        File remoteObjectsDir = Utils.join(remoteGitletDir, "file");
        File remoteBranchesDir = Utils.join(remoteGitletDir, "branches");
        File remoteBranchFile = Utils.join(remoteBranchesDir, remoteBranchName);
        return new RemotePaths(remoteGitletDir, remoteObjectsDir, remoteBranchesDir, remoteBranchFile);
    }

    private static String performPushPreChecks(Branch localBranchObject, File remoteBranchFile) {
        String remoteHeadCommitId = null;
        if (remoteBranchFile.exists()) {
            Branch remoteBranchObject = Branch.readBranch(remoteBranchFile);
            remoteHeadCommitId = remoteBranchObject.getNewest();
            // Line 569: Ensure this condition is not on an overly long line.
            boolean requiresPull = !localBranchObject.containsCommitID(remoteHeadCommitId);
            if (requiresPull) {
                throw new GitletException("Please pull down remote changes before pushing.");
            }
        }
        return remoteHeadCommitId;
    }

    private static void transferObjectsToRemote(List<String> commitsToPushIds,
                                                File localObjectsDir, File remoteObjectsDir) {
        if (!remoteObjectsDir.exists()) {
            remoteObjectsDir.mkdirs();
        }

        for (String commitId : commitsToPushIds) {
            File localCommitFile = Tools.getObjectFile(commitId, localObjectsDir);
            Commit commitToPush = Commit.readCommit(localCommitFile);

            File remoteCommitFile = Tools.getObjectFile(commitId, remoteObjectsDir);
            if (!remoteCommitFile.getParentFile().exists()) {
                remoteCommitFile.getParentFile().mkdirs();
            }
            commitToPush.writeCommit(remoteCommitFile);

            Map<String, String> trackedFiles = commitToPush.getTrackedFiles();
            if (!trackedFiles.isEmpty()) {
                for (String blobSha1 : trackedFiles.values()) {
                    File localBlobFile = Tools.getObjectFile(blobSha1, localObjectsDir);
                    File remoteBlobFile = Tools.getObjectFile(blobSha1, remoteObjectsDir);
                    if (!remoteBlobFile.getParentFile().exists()) {
                        remoteBlobFile.getParentFile().mkdirs();
                    }
                    Utils.writeContents(remoteBlobFile, Utils.readContents(localBlobFile));
                }
            }
        }
    }

    public static void push(String remoteName, String remoteBranchName) {
        checkGitlet();

        RemotePaths paths = validateAndGetRemotePaths(remoteName, remoteBranchName);

        String localHeadCommitId = Tools.readHeadCommitId();
        String localBranchNameString = Tools.readHeadBranch();
        Branch localBranchObject = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, localBranchNameString));
        
        // Line 570: localCommitHistoryIds initialization
        // Moved comment to its own line.
        List<String> localCommitHistoryIds = localBranchObject.getCommitHistory();

        String remoteHeadCommitId = performPushPreChecks(localBranchObject, paths.remoteBranchFile);

        if (Objects.equals(localHeadCommitId, remoteHeadCommitId)) {
            return; // Nothing to do
        }

        List<String> commitsToPushIds = new ArrayList<>();
        int indexOfRemoteHead = (remoteHeadCommitId == null) ? -1 : localCommitHistoryIds.indexOf(remoteHeadCommitId);
        for (int i = indexOfRemoteHead + 1; i < localCommitHistoryIds.size(); i++) {
            commitsToPushIds.add(localCommitHistoryIds.get(i));
        }
        
        transferObjectsToRemote(commitsToPushIds, GITLET_FILE_DIR, paths.remoteObjectsDir);

        if (!paths.remoteBranchesDir.exists()) {
            paths.remoteBranchesDir.mkdirs();
        }
        Branch newOrUpdatedRemoteBranch = new Branch(localBranchObject);
        newOrUpdatedRemoteBranch.writeBranch(paths.remoteBranchFile);
    }

    private static RemotePaths validateAndGetRemotePathsForFetch(String remoteName, String remoteBranchName) {
        File remoteNameInfoFile = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        String remoteRepoPathString = Utils.readContentsAsString(remoteNameInfoFile);

        // Handles original L642 context: if remote config is bad, can't find .gitlet
        if (remoteRepoPathString == null || remoteRepoPathString.isEmpty()) {
            throw new GitletException("Remote directory not found.");
        }

        File remoteGitletDir = new File(remoteRepoPathString); // Path stored is already to .gitlet dir
        if (!remoteGitletDir.exists() || !remoteGitletDir.isDirectory()) {
            // This check also implicitly verifies if the path is a valid .gitlet directory
            throw new GitletException("Remote directory not found.");
        }

        // Handles original L647 context for remoteBranchFile path
        File remoteBranchesDir = Utils.join(remoteGitletDir, "branches");
        File remoteBranchFile = Utils.join(remoteBranchesDir, remoteBranchName);

        if (!remoteBranchFile.exists()) {
            throw new GitletException("That remote does not have that branch.");
        }
        File remoteObjectsDir = Utils.join(remoteGitletDir, "file");
        // Pass null for fields not strictly needed by fetch logic after this point
        return new RemotePaths(null, remoteObjectsDir, null, remoteBranchFile);
    }

    private static void copyMissingObjectsFromRemote(List<String> commitHistoryIds,
                                               File remoteObjectsDir, File localObjectsDir) {
        for (String commitId : commitHistoryIds) {
            File localCommitObjFile = Tools.getObjectFile(commitId, localObjectsDir);
            if (!localCommitObjFile.exists()) {
                File remoteCommitObjFile = Tools.getObjectFile(commitId, remoteObjectsDir);
                if (!remoteCommitObjFile.exists()) {
                    throw new GitletException("Remote repository is missing object: " + commitId);
                }
                Commit commitToFetch = Commit.readCommit(remoteCommitObjFile);

                File localCommitParentDir = localCommitObjFile.getParentFile();
                if (localCommitParentDir != null && !localCommitParentDir.exists()) {
                    localCommitParentDir.mkdirs();
                }
                commitToFetch.writeCommit(localCommitObjFile);

                Map<String, String> trackedFiles = commitToFetch.getTrackedFiles();
                if (!trackedFiles.isEmpty()) {
                    for (String blobSha1 : trackedFiles.values()) {
                        File localBlobFile = Tools.getObjectFile(blobSha1, localObjectsDir);
                        // Handles original L692 indentation and L693 line length
                        if (!localBlobFile.exists()) {
                            File remoteBlobFile = Tools.getObjectFile(blobSha1, remoteObjectsDir);
                            if (!remoteBlobFile.exists()) {
                                throw new GitletException("Remote repository is missing object: " + blobSha1);
                            }
                            byte[] blobContents = Utils.readContents(remoteBlobFile);
                            File localBlobParentDir = localBlobFile.getParentFile();
                            if (localBlobParentDir != null && !localBlobParentDir.exists()) {
                                localBlobParentDir.mkdirs();
                            }
                            Utils.writeContents(localBlobFile, blobContents);
                        }
                    }
                }
            }
        }
    }

    private static void createOrUpdateLocalFetchedBranch(String remoteName, String remoteBranchName,
                                                       Branch remoteBranchObject) {
        String localFetchBranchDirName = remoteName;
        File localFetchBranchParentDir = Utils.join(GITLET_BRANCHES_DIR, localFetchBranchDirName);

        if (!localFetchBranchParentDir.exists()) {
            localFetchBranchParentDir.mkdirs();
        }

        File localFetchBranchFile = Utils.join(localFetchBranchParentDir, remoteBranchName);
        Branch newLocalFetchedBranch = new Branch(remoteBranchObject); // Uses copy constructor
        newLocalFetchedBranch.writeBranch(localFetchBranchFile);
    }

    public static void fetch(String remoteName, String remoteBranchName) {
        checkGitlet();

        RemotePaths fetchPaths = validateAndGetRemotePathsForFetch(remoteName, remoteBranchName);

        // Handles original L666 line length
        Branch remoteBranchObject = Branch.readBranch(fetchPaths.remoteBranchFile);
        
        // Handles original L667 line length (comment moved)
        // Use accessor
        List<String> remoteCommitHistoryIds = remoteBranchObject.getCommitHistory(); 

        copyMissingObjectsFromRemote(remoteCommitHistoryIds, fetchPaths.remoteObjectsDir, GITLET_FILE_DIR);

        createOrUpdateLocalFetchedBranch(remoteName, remoteBranchName, remoteBranchObject);
    }

    public static void pull(String remoteName, String remoteBranchName) {
        checkGitlet();
        Repository.fetch(remoteName, remoteBranchName);
        // Construct the branch name as it's stored by fetch: remoteName/remoteBranchName
        // The modified Branch.branchExists and merge logic should handle this path.
        String fetchedBranchIdentifier = remoteName + "/" + remoteBranchName;
        Repository.merge(fetchedBranchIdentifier);
    }

    private static void changeOneFileCWD(String fileName, String sha1OfFile) {
        File pathToWrite = Utils.join(CWD, fileName);
        //sha1为null，代表需要删除
        if (sha1OfFile == null) {
            if (pathToWrite.exists()) {
                pathToWrite.delete();
            }
            return;
        }
        //sha1不合法
        if (!Tools.isValidSha1(sha1OfFile, true)) {
            throw new GitletException("Sha1 " + sha1OfFile + " is not valid.");
        }
        byte[] con = Utils.readContents(Tools.getObjectFile(sha1OfFile, GITLET_FILE_DIR));
        Utils.writeContents(pathToWrite, (Object) con);
    }

    private static void changeToCommit(String commitID) {
        if (!Tools.isValidSha1(commitID, true)) {
            throw new GitletException("Sha1 " + commitID + " is not valid.");
        }
        Commit tarCommit = Commit.readCommit(Tools.getObjectFile(commitID, GITLET_FILE_DIR));
        List<String> tarFiles = tarCommit.getAllFiles();
        List<String> nowFileNames = Utils.plainFilenamesIn(CWD);
        List<String> allFileNames = Tools.mergeAndSort(tarFiles, nowFileNames);
        for (String fileName : allFileNames) {
            if (tarFiles == null || !tarFiles.contains(fileName)) {
                changeOneFileCWD(fileName, null);
            } else if (nowFileNames != null && nowFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        Utils.join(GITLET_FILE_DIR, fileName))) {
                    changeOneFileCWD(fileName, tarCommit.getFileSHA(fileName));
                }
            } else {
                changeOneFileCWD(fileName, tarCommit.getFileSHA(fileName));
            }
        }
    }
    /**
     * 返回.gitlet目录是否存在
     * @return 如果.gitlet存在则返回true，反之false
     */
    private static boolean gitletExist() {
        return GITLET_DIR.exists();
    }
    /**
     * 检查.gitlet目录是否存在，与上面的区别在于，如果不存在，会直接报错
     */
    private static void checkGitlet() {
        if (!gitletExist()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }
    }

    private static TreeMap<String, String> getAllFileStatus() {
        //存储各个文件状态的Map，前者是文件名，后者是状态
        // none表示无事
        // delete表示被删除了（不是使用rm删除）
        //modified表示修改了
        //untracked，未跟踪
        TreeMap<String, String> result = new TreeMap<>();

        String headCommitId = Tools.readHeadCommitId();
        //当前目录下的文件
        List<String> nowFileNames = Utils.plainFilenamesIn(CWD);
        Commit nowCommit = Commit.readCommit(Tools.getObjectFile(headCommitId, GITLET_FILE_DIR));
        //上一次commit中的文件
        List<String> commitFileNames = nowCommit.getAllFiles();
        //暂存文件
        List<String> stagedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR);
        //上述三者文件
        List<String> allFileNames = Tools.mergeAndSort(nowFileNames,
                stagedFileNames, commitFileNames);
        //rm指令删除的文件，为了判断delete类型
        List<String> deletedFileNames = Utils.plainFilenamesIn(GITLET_TEM_DIR_DELETE);
        for (String fileName : allFileNames) {
            String temp = "none";
            if (nowFileNames != null && !nowFileNames.contains(fileName)) {
                if (deletedFileNames != null && !deletedFileNames.contains(fileName)) {
                    temp = "deleted";
                }
            } else if (stagedFileNames != null && stagedFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        Utils.join(GITLET_TEM_DIR, fileName))) {
                    temp = "modified";
                }
            } else if (commitFileNames != null && commitFileNames.contains(fileName)) {
                if (!Tools.compareSHA1ofFile(Utils.join(CWD, fileName),
                        nowCommit.getFileSHA(fileName))) {
                    temp = "modified";
                }
            } else {
                temp = "untracked";
            }
            result.put(fileName, temp);
        }
        return result;
    }

    private static boolean anyFileUntracked() {
        TreeMap<String, String> allFileStatus = getAllFileStatus();
        for (String fileName : allFileStatus.keySet()) {
            if (allFileStatus.get(fileName).equals("untracked")) {
                return true;
            }
        }
        return false;
    }

    private  static String findBranchOfCommit(String commitID) {
        List<String> branchNames = Utils.plainFilenamesIn(GITLET_BRANCHES_DIR);
        if (branchNames != null) {
            for (String branchName : branchNames) {
                Branch now = Branch.readBranch(Utils.join(GITLET_BRANCHES_DIR, branchName));
                if (now.containsCommitID(commitID)) {
                    return branchName;
                }
            }
        }

        return null;
    }
}
