package fr.ensimag.deca.tree;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Selection extends AbstractLValue{
    private AbstractExpr obj;
    private AbstractIdentifier field;

    public Selection(AbstractExpr obj, AbstractIdentifier field){
        Validate.notNull(obj);
        Validate.notNull(field);
        
        this.obj = obj;
        this.field = field;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        
        Type t = obj.verifyExpr(compiler, localEnv, currentClass);
        if (!t.isClass()) {
            throw new ContextualError(obj.toString() + " n'est pas une classe. On ne peut récupérer un champ que sur une classe.", getLocation());
        }

        EnvironmentExp objEnv = ((ClassDefinition)compiler.environmentType.defOfType(t.getName())).getMembers();
        String fieldDefCastErrorMsg = "L'identifiant " + field.getName().getName() + " ne peut pas être interprété comme un champ.";
        FieldDefinition fieldDef = objEnv.get(field.getName()).asFieldDefinition(fieldDefCastErrorMsg, getLocation());
        
        if (fieldDef.getVisibility() == Visibility.PROTECTED){ //Si le champ est protégé (règle 3.66)
            if (currentClass == null) {
                throw new ContextualError("On ne peut pas accéder à ce champ dans le main (champ protégé).", getLocation());
            }
            boolean cond1;
            try {
                cond1 = t.asClassType(null, getLocation()).isSubClassOf(currentClass.getType());    
            } catch (ContextualError e) {
                throw new DecacInternalError("Something went really really wrong, should be a classType here");
            }
            boolean cond2 = currentClass.getType().isSubClassOf(fieldDef.getContainingClass().getType());
            
            if (!cond1) {
                throw new ContextualError("Le type de l'objet concerné par le champ doit être une sous-classe de la classe courante", getLocation());
            }
            if (!cond2) {
                throw new ContextualError("Impossible d'accéder à ce champ : la classe courante n'est pas une sous-classe de la classe propriétaire du champ protégé.", getLocation());
            }
        }

        Type fieldType = field.verifyExpr(compiler, fieldDef.getContainingClass().getMembers(), currentClass);
        setType(fieldType);
        return fieldType;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(obj.decompile() + "." + field.decompile());
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        obj.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        obj.iter(f);
        field.iter(f);
    }

    @Override
    public ExpDefinition getExpDefinition(){
        return field.getExpDefinition();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        obj.codeGenInst(compiler);
        compiler.addInstruction(new CMP(new NullOperand(), (GPRegister)Rn));
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BEQ(GenerationCode.dereferencement_null));
        }
        // a tester
        if(!GenerationCode.isLeftAssign){
            compiler.addInstruction(new LOAD(new RegisterOffset(field.getFieldDefinition().getIndex(), Rn), (GPRegister)Rn));
        }
    }
}
