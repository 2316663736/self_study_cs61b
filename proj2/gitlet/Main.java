package gitlet;

import gitlet.tool;

import static gitlet.tool.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exitWithErrorExist(errorNoArgs);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length > 1) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.rm(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.globalLog();
                break;
            case "find":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.status();
                break;
            case "checkout":
                if (args.length <= 1 || args.length > 4) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.checkout(args);
                break;
            case "branch":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    exitWithErrorExist(errorIncorrectOperand);
                }
                Repository.merge(args[1]);
                break;
            default:
                exitWithErrorExist(errorNoCommand);
                break;
        }
    }

}
