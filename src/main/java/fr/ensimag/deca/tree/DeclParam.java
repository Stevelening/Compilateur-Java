package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl20
 * @date 01/01/2024
 */
public class DeclParam extends AbstractDeclParam {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier paramName;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier paramName) {
        Validate.notNull(type);
        Validate.notNull(paramName);
        this.type = type;
        this.paramName = paramName;
    }
    
    //Passe 2 :
    @Override
    protected Type verifyDeclParamSig(DecacCompiler compiler)
        throws ContextualError {
            Type synthType = type.verifyType(compiler);
            if (synthType.isVoid() || synthType == null){
                throw new ContextualError("Declaration de " + paramName.getName().getName() + " invalide (type void)", getLocation());
        }
        //Décoration
        paramName.setType(synthType);
        return synthType;
    }
    
    //Passe 3 :
    @Override
    protected void verifyDeclParam(DecacCompiler compiler,
        EnvironmentExp paramEnv) throws ContextualError {
        
        Type synthType = type.verifyType(compiler);
        ExpDefinition def = new ParamDefinition(synthType, getLocation());
        try {
            paramEnv.declare(paramName.getName(), def); // Mutation de l'environnement.    
        } catch (DoubleDefException e) {
            throw new ContextualError("" + paramName.getName().getName() + " déjà déclaré dans le contexte courant.", getLocation());
        }
        //Décoration
        paramName.setDefinition(def);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(type.decompile() + " " + paramName.decompile());

    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        paramName.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        paramName.prettyPrint(s, prefix, false);
    }

    @Override
    protected void codeGenDeclParam(DecacCompiler compiler, int index) {
        paramName.getExpDefinition().setOperand(new RegisterOffset(-2-index, Register.LB));
    }
}
