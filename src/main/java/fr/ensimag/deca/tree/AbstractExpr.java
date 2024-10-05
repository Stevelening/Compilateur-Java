package fr.ensimag.deca.tree;

import fr.ensimag.deca.bytecodegen.TypeMap;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.context.NullType;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WSTR;


import static org.mockito.Mockito.timeout;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.apache.log4j.Logger;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractExpr extends AbstractInst {
    private static final Logger LOG = Logger.getLogger(Program.class);
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {

        Type t = verifyExpr(compiler, localEnv, currentClass);
        
        LOG.debug("debut verifyExpr");
        if (t.isClass()) {
            ClassType asClassT;
            try {
                asClassT = t.asClassType(null, getLocation());
            } catch (ContextualError e) {
                throw new DecacInternalError("Something went really wrong, should be a class here", e);
            }

            String errorMsg = "Types incompatibles pour l'assignation, on ne peut pas assigner une classe à un int, float ou boolean";
            if (!asClassT.isSubClassOf(expectedType.asClassType(errorMsg, getLocation()))) {           
                throw new ContextualError("Types non compatibles pour l'assignation, l'opérande droit n'est pas un sous-type de l'opérande gauche.", getLocation());    
            }

            return this;

        }else if(t.isNull()){
            if(!expectedType.isClass()){
                throw new ContextualError("Types incompatibles pour l'assignation, on ne peut pas assigner null à un int, float ou boolean", getLocation());
            }
            
            return this; //null sous-type de toute classe.
        }else{
            if (t.sameType(expectedType)){
                return this;
            }else if (expectedType.isFloat()){
                AbstractExpr newExpr = new ConvFloat(this);
                newExpr.setLocation(getLocation());
                newExpr.verifyExpr(compiler, localEnv, currentClass);
                return newExpr;
            }else{
                throw new ContextualError("Types non compatibles pour l'assignation.", getLocation());
            }
        }
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        verifyExpr(compiler, localEnv, currentClass); //le cas d'un Assign appelle directement verifyInst il faut juste transmettre la vérification
        // L'attribut synthétisé Type n'est pas utilisé.
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = verifyExpr(compiler, localEnv, currentClass);
        if (!t.isBoolean()){
            throw new ContextualError("Une condition doit être de type booléen. ", getLocation());
        }
        //throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, Boolean printHex) {
        Register Rn = gencode.getCurrentRegister();
        int recup = gencode.getCurrent();
        codeGenInst(compiler); // result in Rn or R0 if it is a MethodCall
        compiler.addInstruction(new LOAD(Rn, Register.R1)); // R1 = Rn
        gencode.setCurrent(recup);
        
        if(getType().isFloat()){
            if(printHex){
                compiler.addInstruction(new WFLOATX()); // print R1 en hexa
            }
            else{
                compiler.addInstruction(new WFLOAT()); // print R1
            }
        }
        else if(getType().isInt()){
            compiler.addInstruction(new WINT());
        }
    }
    protected void byteCodeGenPrint(DecacCompiler compiler, MethodVisitor mv){
        mv.visitFieldInsn(Opcodes.GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
        //Ajouter les méthodes de print
        this.byteCodeGenInst(compiler, mv);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/io/PrintStream","print", "(" + TypeMap.getTypeDescriptor(this.getType()) + ")V", false);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        throw new UnsupportedOperationException("not implemented");
    }

    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv){
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }
}
