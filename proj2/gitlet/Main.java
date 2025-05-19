package gitlet;


import static gitlet.Tools.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        try {
            // 检查是否有输入命令
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }

            String firstArg = args[0];
            switch(firstArg) {
                case "init":
                    if (args.length > 1) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.init();
                    break;
                case "add":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.add(args[1]);
                    break;
                case "commit":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    if (args[1].trim().isEmpty()) {
                        throw new GitletException("Please enter a commit message.");
                    }
                    Repository.commit(args[1]);
                    break;
                case "rm":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.rm(args[1]);
                    break;
                case "log":
                    if (args.length != 1) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.log();
                    break;
                case "global-log":
                    if (args.length != 1) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.globalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.find(args[1]);
                    break;
                case "status":
                    if (args.length != 1) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.status();
                    break;
                case "checkout":
                    if (args.length <= 1 || args.length > 4) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.checkout(args);
                    break;
                case "branch":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.branch(args[1]);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.rmBranch(args[1]);
                    break;
                case "reset":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    if (args.length != 2) {
                        throw new GitletException("Incorrect operands.");
                    }
                    Repository.merge(args[1]);
                    break;
                default:
                    throw new GitletException("No command with that name exists.");
            }
        } catch (GitletException e) {
            // 统一输出异常消息并退出
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

}
