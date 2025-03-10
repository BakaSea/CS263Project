package moe.seaform.cs263.analysis;

import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.dataflow.analysis.constprop.CPFact;
import pascal.taie.analysis.dataflow.analysis.constprop.Value;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.analysis.graph.cfg.CFGEdge;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.stmt.AssignStmt;
import pascal.taie.ir.stmt.If;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.ir.stmt.SwitchStmt;
import pascal.taie.util.collection.Pair;

import java.util.*;

public class CustomDeadCodeAnalysis extends MethodAnalysis<Set<Stmt>> {

    public static final String ID = "custom-dead-code";

    public CustomDeadCodeAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Set<Stmt> analyze(IR ir) {
        CFG<Stmt> cfg = ir.getResult(CFGBuilder.ID);
        DataflowResult<Stmt, CPFact> constants = ir.getResult(CustomConstantPropagationAnalysis.ID);
        DataflowResult<Stmt, SetFact<Var>> liveVars = ir.getResult(CustomLiveVariableAnalysis.ID);
        Set<Stmt> deadCode = new TreeSet<>(Comparator.comparing(Stmt::getIndex));
        Map<Stmt, Boolean> visited = new HashMap<>();
        for (Stmt node : cfg) {
            visited.put(node, false);
        }
        Queue<Stmt> q = new ArrayDeque<>();
        q.add(cfg.getEntry());
        visited.put(cfg.getEntry(), true);
        while (!q.isEmpty()) {
            Stmt cur = q.remove();
            if (cur instanceof If) {
                If stmt = (If) cur;
                Value condition = CustomConstantPropagationAnalysis.Analysis.evaluate(stmt.getCondition(), constants.getInFact(cur));
                if (condition.isConstant()) {
                    if (condition.getConstant() == 1) {
                        if (!visited.get(stmt.getTarget())) {
                            q.add(stmt.getTarget());
                            visited.put(stmt.getTarget(), true);
                        }
                    } else {
                        cfg.getOutEdgesOf(cur).forEach(edge -> {
                            if (edge.getKind() == CFGEdge.Kind.IF_FALSE) {
                                if (!visited.get(edge.target())) {
                                    q.add(edge.target());
                                    visited.put(edge.target(), true);
                                }
                            }
                        });
                    }
                } else {
                    cfg.getOutEdgesOf(cur).forEach(edge -> {
                        if (!visited.get(edge.target())) {
                            q.add(edge.target());
                            visited.put(edge.target(), true);
                        }
                    });
                }
            } else if (cur instanceof SwitchStmt) {
                SwitchStmt stmt = (SwitchStmt) cur;
                Value condition = CustomConstantPropagationAnalysis.Analysis.evaluate(stmt.getVar(), constants.getInFact(cur));
                if (condition.isConstant()) {
                    List<Pair<Integer, Stmt>> cases = stmt.getCaseTargets();
                    boolean flag = false;
                    for (Pair<Integer, Stmt> c : cases) {
                        if (c.first() == condition.getConstant()) {
                            if (!visited.get(c.second())) {
                                q.add(c.second());
                                visited.put(c.second(), true);
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        if (!visited.get(stmt.getDefaultTarget())) {
                            q.add(stmt.getDefaultTarget());
                            visited.put(stmt.getDefaultTarget(), true);
                        }
                    }
                } else {
                    cfg.getOutEdgesOf(cur).forEach(edge -> {
                        if (!visited.get(edge.target())) {
                            q.add(edge.target());
                            visited.put(edge.target(), true);
                        }
                    });
                }
            } else {
                if (cur instanceof AssignStmt) {
                    AssignStmt<?, ?> stmt = (AssignStmt<?, ?>) cur;
                    LValue lv = stmt.getLValue();
                    if (lv instanceof Var v) {
                        if (!liveVars.getOutFact(cur).contains(v)) {
                            if (hasNoSideEffect(stmt.getRValue())) {
                                deadCode.add(cur);
                            }
                        }
                    }
                }
                cfg.getOutEdgesOf(cur).forEach(edge -> {
                    if (!visited.get(edge.target())) {
                        q.add(edge.target());
                        visited.put(edge.target(), true);
                    }
                });
            }
        }
        for (Stmt node : cfg) {
            if (node != cfg.getExit()) {
                if (!visited.get(node)) {
                    deadCode.add(node);
                }
            }
        }
        return deadCode;
    }

    private static boolean hasNoSideEffect(RValue rvalue) {
        if (rvalue instanceof NewExp ||
                rvalue instanceof CastExp ||
                rvalue instanceof FieldAccess ||
                rvalue instanceof ArrayAccess) {
            return false;
        }
        if (rvalue instanceof ArithmeticExp) {
            ArithmeticExp.Op op = ((ArithmeticExp) rvalue).getOperator();
            return op != ArithmeticExp.Op.DIV && op != ArithmeticExp.Op.REM;
        }
        return true;
    }
}
