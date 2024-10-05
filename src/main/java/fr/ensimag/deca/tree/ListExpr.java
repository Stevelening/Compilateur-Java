package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl20
 * @date 01/01/2024
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        int j = 0;
        for (AbstractExpr expr : getList()){
            if(j < getList().size()-1) {
                expr.decompile(s);
                s.println();
            }
            else{
                expr.decompile(s);
            }
            j++;
        }
    }


    // Pas de Verify ??
}
