package moe.seaform.cs263.analysis;

import moe.seaform.cs263.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pascal.taie.World;
import pascal.taie.analysis.deadcode.DeadCodeDetection;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;
import pascal.taie.util.collection.Pair;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Test {

    private static final Logger logger = LogManager.getLogger(Test.class);

    private static final String DIRECTORY = "src/test/resources/";

    private Test() {}

    private static final Comparator<JMethod> methodComp = (m1, m2) -> {
        if (m1.getDeclaringClass().equals(m2.getDeclaringClass())) {
            return m1.getIR().getStmt(0).getLineNumber() -
                    m2.getIR().getStmt(0).getLineNumber();
        } else {
            return m1.getDeclaringClass().toString()
                    .compareTo(m2.getDeclaringClass().toString());
        }
    };

    public static Pair<Integer, Integer> test(String classPath, String inputClass) {
        List<String> argList = new ArrayList<>();
        Collections.addAll(argList, "-java", "8");
        Collections.addAll(argList, "-a", CustomDeadCodeAnalysis.ID);
        Collections.addAll(argList, "-cp", classPath);
        Collections.addAll(argList, "--input-classes", inputClass);
        Application.run(argList.toArray(new String[0]));
        List<JClass> classes = World.get().getClassHierarchy().allClasses().filter(c -> c.isApplication()).sorted(Comparator.comparing(JClass::toString)).toList();
        List<JMethod> methods = classes.stream().map(JClass::getDeclaredMethods).flatMap(Collection::stream).filter(m -> !m.isAbstract()).sorted(methodComp).toList();
        int sumCode = 0, deadCode = 0;
        for (JMethod method : methods) {
            Set<Stmt> result = method.getIR().getResult(CustomDeadCodeAnalysis.ID);
            deadCode += result.size();
            sumCode += method.getIR().getStmts().size();
        }
        logger.info("sumCode: {}, deadCode: {}, rate: {}", sumCode, deadCode, (float)deadCode/(float)sumCode);
        return new Pair(sumCode, deadCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DeadLoop",
            "Fibonacci"
    })
    public void test(String inputClass) {
        test(DIRECTORY, inputClass);
    }

    @org.junit.jupiter.api.Test
    public void testAll() {
        Path directory = Paths.get(DIRECTORY);
        List<String> classes = new ArrayList<>();
        List<Integer> sumCodes = new ArrayList<>(), deadCodes = new ArrayList<>();
        int sumCode = 0, deadCode = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".java")) {
                    String inputClass = fileName.substring(0, fileName.length()-5);
                    Pair<Integer, Integer> result = test(DIRECTORY, inputClass);
                    sumCode += result.first();
                    deadCode += result.second();
                    classes.add(inputClass);
                    sumCodes.add(result.first());
                    deadCodes.add(result.second());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("sumCode: {}, deadCode: {}, rate: {}", sumCode, deadCode, (float)deadCode/(float)sumCode);
        for (int i = 0; i < classes.size(); ++i) {
            logger.info("class: {}, sumCode: {}, deadCode: {}", classes.get(i), sumCodes.get(i), deadCodes.get(i));
        }
    }

}
