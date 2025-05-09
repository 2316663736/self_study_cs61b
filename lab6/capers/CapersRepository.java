package capers;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/** A repository for Capers 
 * @author qianye
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 *  : change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD,".capers","story"); //  Hint: look at the `join`
                                            //      function in Utils

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {
        createFile(CAPERS_FOLDER);
        createDir(Dog.DOG_FOLDER);
    }

    /**
     * @param file
     */
    private static void  createFile(File file) {
        createDir(file.getParentFile());

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Utils.exitWithError("Could not create file " + file.getName());
                }
            } catch (IOException e) {
                Utils.exitWithError(e.getMessage());
            }
        }
    }
    private static void createDir(File file) {
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
    };

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        String content = readContentsAsString(CAPERS_FOLDER) + text + '\n';
        System.out.print(content);
        writeContents(CAPERS_FOLDER, content);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        //
        createFile(Utils.join(Dog.DOG_FOLDER, name));
        Dog dog = new Dog(name, breed, age);
        dog.saveDog();
        System.out.println(dog);
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        //
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        dog.saveDog();
    }


}
