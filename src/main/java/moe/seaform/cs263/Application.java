package moe.seaform.cs263;

import pascal.taie.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Application {

    public static void main(String args[]) {
        if (args.length > 0) {
            List<String> argList = new ArrayList<>();
            Collections.addAll(argList, "-java", "8", "-p", "plan.yml");
            Collections.addAll(argList, args);
            Main.main(argList.toArray(new String[0]));
        } else {
            System.out.println("Commands error!");
        }
    }

}
