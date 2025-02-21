package moe.seaform.cs263.analysis;

import pascal.taie.analysis.StmtResult;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.util.collection.MultiMap;
import pascal.taie.util.collection.TwoKeyMultiMap;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public class CustomDefUse implements StmtResult<MultiMap<Var, Stmt>> {

    private static final String NULL_DEFS = "defs is null (not computed) as it is disabled in def-use analysis";
    private static final String NULL_USES = "uses is null (not computed) as it is disabled in def-use analysis";
    @Nullable
    private final TwoKeyMultiMap<Stmt, Var, Stmt> defs;
    @Nullable
    private final MultiMap<Stmt, Stmt> uses;

    CustomDefUse(@Nullable TwoKeyMultiMap<Stmt, Var, Stmt> defs, @Nullable MultiMap<Stmt, Stmt> uses) {
        this.defs = defs;
        this.uses = uses;
    }

    public Set<Stmt> getDefs(Stmt stmt, Var var) {
        Objects.requireNonNull(this.defs, "defs is null (not computed) as it is disabled in def-use analysis");
        return this.defs.get(stmt, var);
    }

    public Set<Stmt> getUses(Stmt stmt) {
        Objects.requireNonNull(this.uses, "uses is null (not computed) as it is disabled in def-use analysis");
        return this.uses.get(stmt);
    }

    public boolean isRelevant(Stmt stmt) {
        return true;
    }

    public MultiMap<Var, Stmt> getResult(Stmt stmt) {
        Objects.requireNonNull(this.defs, "defs is null (not computed) as it is disabled in def-use analysis");
        return this.defs.get(stmt);
    }

}
