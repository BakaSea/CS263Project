package moe.seaform.cs263.analysis;

import moe.seaform.cs263.solver.Solver;
import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;

public abstract class AbstractCustomDataflowAnalysis<Node, Fact> extends MethodAnalysis<DataflowResult<Node, Fact>> {

    public AbstractCustomDataflowAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public DataflowResult<Node, Fact> analyze(IR ir) {
        CFG<Node> cfg = (CFG)ir.getResult("cfg");
        DataflowAnalysis<Node, Fact> analysis = this.makeAnalysis(cfg);
        Solver<Node, Fact> solver = Solver.makeSolver(analysis);
        return solver.solve(cfg);
    }

    protected abstract DataflowAnalysis<Node, Fact> makeAnalysis(CFG<Node> cfg);

}
