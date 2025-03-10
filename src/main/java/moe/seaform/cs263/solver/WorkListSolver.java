package moe.seaform.cs263.solver;

import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.graph.cfg.CFG;

import java.util.ArrayDeque;
import java.util.Queue;

class WorkListSolver<Node, Fact> extends Solver<Node, Fact> {

    WorkListSolver(DataflowAnalysis<Node, Fact> analysis) {
        super(analysis);
    }

    @Override
    protected void doSolveForward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        Queue<Node> workList = new ArrayDeque<>();
        for (Node node : cfg) {
            workList.add(node);
        }
        while (!workList.isEmpty()) {
            Node cur = workList.remove();
            Fact in = analysis.newInitialFact();
            cfg.getPredsOf(cur).forEach(pred -> analysis.meetInto(result.getOutFact(pred), in));
            result.setInFact(cur, in);
            if (analysis.transferNode(cur, in, result.getOutFact(cur))) {
                cfg.getSuccsOf(cur).forEach(workList::add);
            }
        }
    }

    @Override
    protected void doSolveBackward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        Queue<Node> workList = new ArrayDeque<>();
        for (Node node : cfg) {
            workList.add(node);
        }
        while (!workList.isEmpty()) {
            Node cur = workList.remove();
            Fact out = analysis.newInitialFact();
            cfg.getSuccsOf(cur).forEach(succ -> analysis.meetInto(result.getInFact(succ), out));
            result.setOutFact(cur, out);
            if (analysis.transferNode(cur, result.getInFact(cur), out)) {
                cfg.getPredsOf(cur).forEach(workList::add);
            }
        }
    }

}

