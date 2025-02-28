package moe.seaform.cs263.analysis;

import pascal.taie.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import pascal.taie.analysis.dataflow.analysis.AnalysisDriver;
import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.LValue;
import pascal.taie.ir.exp.RValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;

import java.util.Optional;
import java.util.Set;

public class CustomLiveVariableAnalysis extends AnalysisDriver<Stmt, SetFact<Var>> {

    public static final String ID = "custom-live-var";

    public CustomLiveVariableAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected DataflowAnalysis<Stmt, SetFact<Var>> makeAnalysis(CFG<Stmt> cfg) {
        return new Analysis(cfg);
    }

    private static class Analysis extends AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

        protected Analysis(CFG<Stmt> cfg) {
            super(cfg);
        }

        @Override
        public boolean isForward() {
            return false;
        }

        @Override
        public SetFact<Var> newBoundaryFact() {
            return new SetFact<Var>();
        }

        @Override
        public SetFact<Var> newInitialFact() {
            return new SetFact<Var>();
        }

        @Override
        public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
            target.union(fact);
        }

        @Override
        public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
            Optional<LValue> def = stmt.getDef();
            Set<RValue> use = stmt.getUses();
            SetFact<Var> newIn = newInitialFact();
            for (RValue value : use) {
                if (value instanceof Var var) {
                    newIn.add(var);
                }
            }
            SetFact<Var> temp = out.copy();
            if (def.isPresent()) {
                LValue value = def.get();
                if (value instanceof Var var) {
                    temp.remove(var);
                }
            }
            newIn.union(temp);
            boolean result = !in.equals(newIn);
            in.set(newIn);
            return result;
        }
    }

}
