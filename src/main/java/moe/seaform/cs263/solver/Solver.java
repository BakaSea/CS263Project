package moe.seaform.cs263.solver;

import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.graph.cfg.CFG;

public abstract class Solver<Node, Fact> {

    protected final DataflowAnalysis<Node, Fact> analysis;

    protected Solver(DataflowAnalysis<Node, Fact> analysis) {
        this.analysis = analysis;
    }

    public static <Node, Fact> Solver<Node, Fact> makeSolver(DataflowAnalysis<Node, Fact> analysis) {
        return new WorkListSolver<>(analysis);
    }

    public DataflowResult<Node, Fact> solve(CFG<Node> cfg) {
        DataflowResult<Node, Fact> result = initialize(cfg);
        doSolve(cfg, result);
        return result;
    }

    private DataflowResult<Node, Fact> initialize(CFG<Node> cfg) {
        DataflowResult<Node, Fact> result = new DataflowResult<>();
        if (analysis.isForward()) {
            initializeForward(cfg, result);
        } else {
            initializeBackward(cfg, result);
        }
        return result;
    }

    protected void initializeForward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        result.setOutFact(cfg.getEntry(), analysis.newBoundaryFact());
        for (Node node : cfg) {
            if (node != cfg.getEntry()) {
                result.setOutFact(node, analysis.newInitialFact());
            }
        }
    }

    protected void initializeBackward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        result.setInFact(cfg.getExit(), analysis.newBoundaryFact());
        for (Node node : cfg) {
            if (node != cfg.getExit()) {
                result.setInFact(node, analysis.newInitialFact());
            }
        }
    }

    private void doSolve(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        if (analysis.isForward()) {
            doSolveForward(cfg, result);
        } else {
            doSolveBackward(cfg, result);
        }
    }

    protected abstract void doSolveForward(CFG<Node> cfg, DataflowResult<Node, Fact> result);

    protected abstract void doSolveBackward(CFG<Node> cfg, DataflowResult<Node, Fact> result);

}
