package moe.seaform.cs263;

import pascal.taie.World;
import pascal.taie.WorldBuilder;
import pascal.taie.analysis.AnalysisManager;
import pascal.taie.config.*;
import pascal.taie.frontend.cache.CachedWorldBuilder;
import pascal.taie.util.RuntimeInfoLogger;
import pascal.taie.util.Timer;
import pascal.taie.util.collection.Lists;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String args[]) {
        if (args.length > 0) {
            List<String> argList = new ArrayList<>();
            Collections.addAll(argList, "-java", "8", "-p", "plan.yml");
            Collections.addAll(argList, args);
            run(argList.toArray(new String[0]));
        } else {
            System.out.println("Commands error!");
        }
    }

    private static void run(String... args) {
        Timer.runAndCount(() -> {
            Options options = processArgs(args);
            LoggerConfigs.setOutput(options.getOutputDir());
            RuntimeInfoLogger.logRuntimeInfo();
            Plan plan = processConfigs(options);
            if (plan.analyses().isEmpty()) {
                logger.info("No analyses are specified");
                System.exit(0);
            }

            buildWorld(options, plan.analyses());
            executePlan(plan);
        }, "Tai-e");
        LoggerConfigs.reconfigure();
    }

    private static Options processArgs(String... args) {
        Options options = Options.parse(args);
        if (options.isPrintHelp() || args.length == 0) {
            options.printHelp();
            System.exit(0);
        }

        return options;
    }

    private static InputStream getCustomAnalysisConfig() {
        return Configs.class.getClassLoader().getResourceAsStream("custom-analyses.yml");
    }

    private static Plan processConfigs(Options options) {
        InputStream originalContent = Configs.getAnalysisConfig();
        InputStream customContent = getCustomAnalysisConfig();
        InputStream content = new SequenceInputStream(originalContent, customContent);
        List<AnalysisConfig> analysisConfigs = AnalysisConfig.parseConfigs(content);
        ConfigManager manager = new ConfigManager(analysisConfigs);
        AnalysisPlanner planner = new AnalysisPlanner(manager, options.getKeepResult());
        boolean reachableScope = options.getScope().equals(Scope.REACHABLE);
        List planConfigs;
        if (!options.getAnalyses().isEmpty()) {
            planConfigs = PlanConfig.readConfigs(options);
            manager.overwriteOptions(planConfigs);
            Plan plan = planner.expandPlan(planConfigs, reachableScope);
            planConfigs = Lists.map(plan.analyses(), (ac) -> {
                return new PlanConfig(ac.getId(), ac.getOptions());
            });
            PlanConfig.writeConfigs(planConfigs, options.getOutputDir());
            if (!options.isOnlyGenPlan()) {
                return plan;
            }
        } else if (options.getPlanFile() != null) {
            planConfigs = PlanConfig.readConfigs(options.getPlanFile());
            manager.overwriteOptions(planConfigs);
            return planner.makePlan(planConfigs, reachableScope);
        }

        return Plan.emptyPlan();
    }

    public static void buildWorld(String... args) {
        Options options = Options.parse(args);
        LoggerConfigs.setOutput(options.getOutputDir());
        Plan plan = processConfigs(options);
        buildWorld(options, plan.analyses());
        LoggerConfigs.reconfigure();
    }

    private static void buildWorld(Options options, List<AnalysisConfig> analyses) {
        Timer.runAndCount(() -> {
            try {
                Class<? extends WorldBuilder> builderClass = options.getWorldBuilderClass();
                Constructor<? extends WorldBuilder> builderCtor = builderClass.getConstructor();
                WorldBuilder builder = (WorldBuilder)builderCtor.newInstance();
                if (options.isWorldCacheMode()) {
                    builder = new CachedWorldBuilder((WorldBuilder)builder);
                }

                ((WorldBuilder)builder).build(options, analyses);
                logger.info("{} classes with {} methods in the world", World.get().getClassHierarchy().allClasses().count(), World.get().getClassHierarchy().allClasses().mapToInt((c) -> {
                    return c.getDeclaredMethods().size();
                }).sum());
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException var5) {
                System.err.println("Failed to build world due to " + var5);
                System.exit(1);
            }

        }, "WorldBuilder");
    }

    private static void executePlan(Plan plan) {
        (new AnalysisManager(plan)).execute();
    }

}
