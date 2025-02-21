package moe.seaform.cs263.analysis;

import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.RValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.util.collection.*;

import java.util.Iterator;

public class CustomDefUseAnalysis extends MethodAnalysis<CustomDefUse> {

    public static final String ID = "custom-def-use";

    public CustomDefUseAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public CustomDefUse analyze(IR ir) {
        DataflowResult<Stmt, SetFact<Stmt>> rdResult = (DataflowResult)ir.getResult("reach-def");
        TwoKeyMultiMap<Stmt, Var, Stmt> defs = Maps.newTwoKeyMultiMap(new IndexMap(ir, ir.getStmts().size()), () -> {
            return Maps.newMultiMap(Maps.newHybridMap());
        });
        MultiMap<Stmt, Stmt> uses = Maps.newMultiMap(new IndexMap(ir, ir.getStmts().size()), Sets::newHybridSet);
        Iterator irIter = ir.iterator();

        label:
        while(irIter.hasNext()) {
            Stmt stmt = (Stmt)irIter.next();
            SetFact<Stmt> reachDefs = (SetFact)rdResult.getInFact(stmt);
            Iterator useIter = stmt.getUses().iterator();

            while(true) {
                RValue use = null;
                do {
                    if (!useIter.hasNext()) {
                        continue label;
                    }

                    use = (RValue)useIter.next();
                } while(!(use instanceof Var));

                Var useVar = (Var)use;
                Iterator rdIter = reachDefs.iterator();

                while(rdIter.hasNext()) {
                    Stmt reachDef = (Stmt)rdIter.next();
                    RValue finalUse = use;
                    reachDef.getDef().ifPresent((lhs) -> {
                        if (lhs.equals(finalUse)) {
                            defs.put(stmt, useVar, reachDef);
                            uses.put(reachDef, stmt);
                        }
                    });
                }
            }
        }

        return new CustomDefUse(defs, uses);
    }

}
