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

    class remoteRepository {
        private final File REMOTE_GITLET_DIR;
        private final File REMOTE_GITLET_FILE_DIR;
        private final File REMOTE_BRANCHES_DIR;
        private final File REMOTE_GITLET_HEAD;
        remoteRepository(String remoteDir) {
            REMOTE_GITLET_DIR = Utils.join(remoteDir);
            REMOTE_GITLET_FILE_DIR = Utils.join(REMOTE_GITLET_DIR, "file");
            REMOTE_BRANCHES_DIR = Utils.join(REMOTE_GITLET_DIR, "branches");
            REMOTE_GITLET_HEAD = Utils.join(REMOTE_GITLET_DIR, "head");
        }
        public File getRemoteGitletDir() {
            return REMOTE_GITLET_DIR;
        }
        public File getRemoteGitletFileDir() {
            return REMOTE_GITLET_FILE_DIR;
        }
        public File getRemoteBranchesDir() {
            return REMOTE_BRANCHES_DIR;
        }
        public List<String> getRemoteGitletBranches() {
            return Utils.plainFilenamesIn(REMOTE_BRANCHES_DIR);
        }
        public File getRemoteGitletHead() {
            return REMOTE_GITLET_HEAD;
        }
    }
    /*  fill in the rest of this class. */
    public static void init() {
        if (gitletExist()) {
            throw new GitletException("A Gitlet version-control system"
                    + " already exists in the current directory.");
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
                throw new GitletException("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
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
            throw new GitletException("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
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
        String conflictContent = "<<<<<<< HEAD\n"
                + currentContent + "=======\n"
                + targetContent + ">>>>>>>\n";

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

    public static void addRemote(String remoteName, String fileName) {
        checkGitlet();
        List<String> allRemoteFiles = Utils.plainFilenamesIn(GITLET_REMOTE_FILES_DIR);
        if (allRemoteFiles != null && allRemoteFiles.contains(remoteName)) {
            throw new GitletException("A remote with that name already exists.");
        }

        // 标准化路径分隔符为正斜杠（测试期望的格式）
        String normalizedPath = fileName.replace('\\', '/');
        Utils.writeContents(Utils.join(GITLET_REMOTE_FILES_DIR, remoteName), normalizedPath);
    }

    public static void rmRemote(String remoteName) {
        checkGitlet();
        if (!remoteExist(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        }
        File file = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        file.delete();
    }

    public static void push(String remoteName, String remoteBranchName) {
        checkGitlet();

        // 获取远程仓库路径
        File remoteFile = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        if (!remoteFile.exists()) {
            throw new GitletException("Remote directory not found.");
        }

        String remotePath = Utils.readContentsAsString(remoteFile);
        // 处理路径分隔符
        remotePath = remotePath.replace('/', File.separatorChar);

        // 构建远程路径 - 注意路径已经包含.gitlet
        File remoteGitletDir = new File(CWD, remotePath);

        // 检查远程.gitlet目录是否存在
        if (!remoteGitletDir.exists()) {
            throw new GitletException("Remote directory not found.");
        }

        // 获取远程仓库的文件目录和分支目录
        File remoteFileDir = Utils.join(remoteGitletDir, "file");
        File remoteBranchesDir = Utils.join(remoteGitletDir, "branches");
        File remoteBranchFile = Utils.join(remoteBranchesDir, remoteBranchName);

        // 获取当前分支
        String currentBranch = Tools.readHeadBranch();
        String localHeadCommitId = Tools.readHeadCommitId();

        // 如果远程分支存在，检查是否可以fast-forward
        if (remoteBranchFile.exists()) {
            Branch remoteBranch = Branch.readBranch(remoteBranchFile);
            String remoteHeadCommitId = remoteBranch.getNewest();

            // 检查远程分支的头是否在本地分支的历史中
            if (!isAncestor(remoteHeadCommitId, localHeadCommitId)) {
                throw new GitletException("Please pull down remote changes before pushing.");
            }

            // 更新远程分支的所有提交历史
            copyAllCommitsHistory(localHeadCommitId, remoteBranch);
            remoteBranch.writeBranch(remoteBranchFile);
        } else {
            // 创建新分支，需要复制完整的提交历史
            Branch newRemoteBranch = createBranchWithHistory(localHeadCommitId);
            newRemoteBranch.writeBranch(remoteBranchFile);
        }

        // 复制所有需要的提交和文件到远程仓库
        copyCommitsToRemote(localHeadCommitId, remoteFileDir);

        // 如果推送的分支是远程的当前分支，更新远程的HEAD文件
        File remoteHead = Utils.join(remoteGitletDir, "head");
        if (remoteHead.exists()) {
            String remoteHeadContent = Utils.readContentsAsString(remoteHead);
            String remoteCurrentBranch = remoteHeadContent.substring(Utils.UID_LENGTH);

            if (remoteCurrentBranch.equals(remoteBranchName)) {
                Utils.writeContents(remoteHead, localHeadCommitId + remoteBranchName);
            }
        }
    }
    public static void fetch(String remoteName, String remoteBranchName) {
        checkGitlet();

        // 获取远程仓库路径
        File remoteFile = Utils.join(GITLET_REMOTE_FILES_DIR, remoteName);
        if (!remoteFile.exists()) {
            throw new GitletException("Remote directory not found.");
        }

        String remotePath = Utils.readContentsAsString(remoteFile);
        // 处理路径分隔符
        remotePath = remotePath.replace('/', File.separatorChar);

        // 构建远程路径
        File remoteGitletDir = new File(CWD, remotePath);

        // 检查远程.gitlet目录是否存在
        if (!remoteGitletDir.exists()) {
            throw new GitletException("Remote directory not found.");
        }

        // 获取远程分支
        File remoteBranchesDir = Utils.join(remoteGitletDir, "branches");
        File remoteBranchFile = Utils.join(remoteBranchesDir, remoteBranchName);

        if (!remoteBranchFile.exists()) {
            throw new GitletException("That remote does not have that branch.");
        }

        // 读取远程分支
        Branch remoteBranch = Branch.readBranch(remoteBranchFile);

        // 获取远程仓库的文件目录
        File remoteFileDir = Utils.join(remoteGitletDir, "file");

        // 复制远程提交和文件到本地
        String remoteHeadCommitId = remoteBranch.getNewest();
        copyCommitsFromRemote(remoteHeadCommitId, remoteFileDir);

        // 创建或更新本地的远程分支 [remote name]/[remote branch name]
        String localRemoteBranchName = remoteName + "/" + remoteBranchName;
        File localRemoteBranchFile = Utils.join(GITLET_BRANCHES_DIR, localRemoteBranchName);

        // 复制完整的分支历史
        Branch localRemoteBranch = new Branch(remoteBranch);
        localRemoteBranch.writeBranch(localRemoteBranchFile);
    }
    /**
     * 创建带有完整历史的分支
     */
    private static Branch createBranchWithHistory(String headCommitId) {
        Branch newBranch = new Branch();

        // 构建完整的提交历史
        List<String> history = new ArrayList<>();
        String currentId = headCommitId;

        while (currentId != null) {
            history.add(0, currentId);  // 添加到开头
            Commit commit = Commit.readCommit(Tools.getObjectFile(currentId, GITLET_FILE_DIR));
            currentId = commit.getFather();  // 只跟随第一个父提交
        }

        // 添加所有历史到分支
        for (String commitId : history) {
            newBranch.add(commitId);
        }

        return newBranch;
    }

    /**
     * 复制所有提交历史到远程分支
     */
    private static void copyAllCommitsHistory(String localHeadId, Branch remoteBranch) {
        // 获取本地的完整历史
        List<String> localHistory = new ArrayList<>();
        String currentId = localHeadId;

        while (currentId != null) {
            localHistory.add(0, currentId);
            Commit commit = Commit.readCommit(Tools.getObjectFile(currentId, GITLET_FILE_DIR));
            currentId = commit.getFather();
        }

        // 更新远程分支的历史
        for (String commitId : localHistory) {
            if (!remoteBranch.containsCommitID(commitId)) {
                remoteBranch.add(commitId);
            }
        }

        remoteBranch.reset(localHeadId);
    }
    public static void pull(String remoteName, String remoteBranchName) {
        checkGitlet();

        // 先执行fetch
        fetch(remoteName, remoteBranchName);

        // 然后merge远程分支
        String localRemoteBranchName = remoteName + "/" + remoteBranchName;
        merge(localRemoteBranchName);
    }
    private static boolean remoteExist(String remoteName) {
        List<String> allRemoteFiles = Utils.plainFilenamesIn(GITLET_REMOTE_FILES_DIR);
        return allRemoteFiles != null && allRemoteFiles.contains(remoteName);
    }
    private static File remoteBranchCheck(String remoteName, String remoteBranchName) {
        checkGitlet();
        if(!remoteExist(remoteName)) {
            throw new GitletException("A remote with that name does not exist.");
        };
        File otherGitlet = Utils.join(CWD,
                readContentsAsString(join(GITLET_REMOTE_FILES_DIR, remoteName)));
        if (!otherGitlet.exists()) {
            throw new GitletException("Remote directory not found.");
        }
        File otherBranch = join(otherGitlet, GITLET_BRANCHES_DIR.getName(), remoteBranchName);
        if (!otherBranch.exists()) {
            throw new GitletException("That remote does not have that branch.");
        }
        return otherGitlet;
    }
    /**
     * 检查ancestorId是否是descendantId的祖先
     */
    private static boolean isAncestor(String ancestorId, String descendantId) {
        if (ancestorId.equals(descendantId)) {
            return true;
        }

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(descendantId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(ancestorId)) {
                return true;
            }

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            Commit commit = Commit.readCommit(Tools.getObjectFile(current, GITLET_FILE_DIR));
            if (commit.father != null) {
                queue.offer(commit.father);
            }
            if (commit.merge != null) {
                queue.offer(commit.merge);
            }
        }

        return false;
    }

    /**
     * 复制提交和相关文件到远程仓库
     */
    private static void copyCommitsToRemote(String headCommitId, File remoteFileDir) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(headCommitId);

        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (visited.contains(commitId)) {
                continue;
            }
            visited.add(commitId);

            // 复制提交对象
            File localCommitFile = Tools.getObjectFile(commitId, GITLET_FILE_DIR);
            File remoteCommitFile = Tools.getObjectFile(commitId, remoteFileDir);

            if (!remoteCommitFile.exists()) {
                Tools.createFile(remoteCommitFile);
                byte[] content = Utils.readContents(localCommitFile);
                Utils.writeContents(remoteCommitFile, (Object) content);

                // 读取提交并复制相关文件
                Commit commit = Commit.readCommit(localCommitFile);

                // 复制提交中的所有文件
                for (String fileName : commit.getAllFiles()) {
                    String fileSHA = commit.getFileSHA(fileName);
                    File localFile = Tools.getObjectFile(fileSHA, GITLET_FILE_DIR);
                    File remoteFile = Tools.getObjectFile(fileSHA, remoteFileDir);

                    if (!remoteFile.exists()) {
                        Tools.createFile(remoteFile);
                        byte[] fileContent = Utils.readContents(localFile);
                        Utils.writeContents(remoteFile, (Object) fileContent);
                    }
                }

                // 添加父提交到队列
                if (commit.father != null) {
                    queue.offer(commit.father);
                }
                if (commit.merge != null) {
                    queue.offer(commit.merge);
                }
            }
        }
    }

    /**
     * 从远程仓库复制提交和相关文件
     */
    private static void copyCommitsFromRemote(String headCommitId, File remoteFileDir) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(headCommitId);

        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (visited.contains(commitId)) {
                continue;
            }
            visited.add(commitId);

            // 复制提交对象
            File remoteCommitFile = Tools.getObjectFile(commitId, remoteFileDir);
            File localCommitFile = Tools.getObjectFile(commitId, GITLET_FILE_DIR);

            if (!localCommitFile.exists()) {
                Tools.createFile(localCommitFile);
                byte[] content = Utils.readContents(remoteCommitFile);
                Utils.writeContents(localCommitFile, (Object) content);

                // 读取提交并复制相关文件
                Commit commit = Commit.readCommit(remoteCommitFile);

                // 复制提交中的所有文件
                for (String fileName : commit.getAllFiles()) {
                    String fileSHA = commit.getFileSHA(fileName);
                    File remoteFile = Tools.getObjectFile(fileSHA, remoteFileDir);
                    File localFile = Tools.getObjectFile(fileSHA, GITLET_FILE_DIR);

                    if (!localFile.exists()) {
                        Tools.createFile(localFile);
                        byte[] fileContent = Utils.readContents(remoteFile);
                        Utils.writeContents(localFile, (Object) fileContent);
                    }
                }

                // 添加父提交到队列
                if (commit.father != null) {
                    queue.offer(commit.father);
                }
                if (commit.merge != null) {
                    queue.offer(commit.merge);
                }
            }
        }
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
