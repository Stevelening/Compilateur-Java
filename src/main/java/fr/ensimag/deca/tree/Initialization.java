package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl20
 * @date 01/01/2024
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }


    //Passe 3 :
    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        expression = expression.verifyRValue(compiler, localEnv, currentClass, t);
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenInitialization(DecacCompiler compiler, AbstractIdentifier varName){
        if(varName.getType().isClass()){
            if(getExpression() instanceof New){
                Register Rn = gencode.getCurrentRegister();
                getExpression().codeGenInst(compiler);
                compiler.addInstruction(new STORE(Rn, varName.getExpDefinition().getOperand()));
            }
            else{
                compiler.addInstruction(new LOAD(((Identifier)getExpression()).getExpDefinition().getOperand(), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, varName.getExpDefinition().getOperand()));
            }
        }
        else{ // int, float et Boolean
            (new Assign(varName, getExpression())).codeGenInst(compiler);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(expression.decompile());
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}
