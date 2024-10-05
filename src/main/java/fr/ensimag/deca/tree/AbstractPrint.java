package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

import static org.mockito.Mockito.never;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;

/**
 * Print statement (print, println, ...).
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        for (AbstractExpr arg : arguments.getList()) {
            Type t = arg.verifyExpr(compiler, localEnv, currentClass);
            if (!t.isInt() && !t.isFloat() && !t.isString()){
                throw new ContextualError("Les arguments de print doivent être de type 'int', 'float' ou 'string'.", getLocation());
            }
        }
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler, getPrintHex());
        }
    }
    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        for (AbstractExpr a : getArguments().getList()) {
            a.byteCodeGenPrint(compiler, mv);
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        String res = "print" + this.getSuffix() + (getPrintHex() ? "x" : "")+ "(";
        
        for (AbstractExpr arg : this.arguments.getList()) {
            res += arg.decompile();
            res += ",";
        }
        if (arguments.size() > 0) {
            res = res.substring(0, res.lastIndexOf(","));    
        }
        
        res += ");";
        s.print(res);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
