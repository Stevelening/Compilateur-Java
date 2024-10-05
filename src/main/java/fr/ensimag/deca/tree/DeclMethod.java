package fr.ensimag.deca.tree;

import static org.mockito.Mockito.never;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.apache.log4j.Logger;

public class DeclMethod extends AbstractDeclMethod{
    private static final Logger LOG = Logger.getLogger(Program.class);
    private final AbstractIdentifier type, methodName;
    private final ListDeclParam params;
    private final AbstractMethodBody body;

    public DeclMethod(AbstractIdentifier returnType, AbstractIdentifier name,
     ListDeclParam params, AbstractMethodBody body){
        Validate.notNull(body);
        Validate.notNull(name);
        Validate.notNull(returnType);
        Validate.notNull(params);

        this.type = returnType;
        this.methodName = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public AbstractIdentifier getMethodName(){
        return methodName;
    }

    @Override
    public void decompile(IndentPrintStream s){
        s.println(type.decompile() + " " + methodName.decompile() + "(" + params.decompile() + " )" + body.decompile());
    }

    /**
     * Implémente la règle 2.7 de la passe 2
     * @param compiler contient env_types
     * @param superclass la classe parent
     * @throws ContextualError
     */
    @Override
    protected void verifyDeclMethodSignature(DecacCompiler compiler, 
            ClassDefinition currentclass, EnvironmentExp methodEnv) throws ContextualError{
        Type synthType = type.verifyType(compiler);
        Signature sig = params.verifyListDeclParamSig(compiler);
        ClassDefinition superclass = currentclass.getSuperClass();
        ExpDefinition superMethod = superclass.getMembers().get(methodName.getName());
        int index;
        if (superMethod == null){
            currentclass.incNumberOfMethods();
            index = currentclass.getNumberOfMethods();
        } else {
            index = superMethod.asMethodDefinition("" + methodName.getName().getName() + " n'est pas une method dans la class héritée", getLocation()).getIndex();
        }
        LOG.debug("" + methodName.getName().getName() + " : " + index);
        MethodDefinition def = new MethodDefinition(synthType, getLocation(), sig, index);
        try {
            methodEnv.declare(methodName.getName(), def); // Mutation de l'environnement.    
        } catch (DoubleDefException e) {
            throw new ContextualError("" + methodName.getName().getName() + " déjà déclaré dans le contexte courant.", getLocation());
        }
        methodName.setType(synthType);
        methodName.setDefinition(def);
    }

    /**
     * Implémente la règle 3.11 de la passe 3
     * @param compiler contient env_types
     * @param className la classe dans laquelle se trouve la méthode
     * @param classEnvExp environnement de la classe
     * @throws ContextualError
     */
    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, 
            ClassDefinition currentclass, EnvironmentExp classEnvExp) throws ContextualError{
        Type synthType = type.verifyType(compiler);
        EnvironmentExp paramEnv = new EnvironmentExp(classEnvExp);
        params.verifyListDeclParam(compiler, paramEnv);
        body.verifyMethodBody(compiler, paramEnv, currentclass, synthType);
    }

    @Override
    protected void codeGenDeclMethod(DecacCompiler compiler, String className){
        params.codeGenListDeclParam(compiler);

        compiler.addComment("Code de la méthode "+this.getMethodName().getName().toString());
        String name = className;
        name += "." + getMethodName().getName().toString();
        compiler.addLabel(new Label("code."+name));
        Label finM = new Label("fin."+name);
        Line tsto = new Line(new TSTO(0));
        Line addsp = new Line(new ADDSP(0));
        int d1 = 0;
        int d2 = 0;
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.add(tsto);
            compiler.addInstruction(new BOV(GenerationCode.stack_overflow_error));
        }
        compiler.add(addsp);
        // sauvegarde des registres
        compiler.addComment("Sauvegarde des registres");
        GenerationCode.isForMethods = true;
        GenerationCode.dMethod = 0;
        gencode.initLocalVars();
        int index = gencode.saveRegisters(compiler);
        
        GenerationCode.finMethode = "fin."+name;
        body.codeGenMethodBody(compiler);

        d1 += GenerationCode.dMethod;
        d2 += gencode.getLocalVars();
        tsto.setInstruction(new TSTO(d1));
        addsp.setInstruction(new ADDSP(d2));
        if(!type.getType().isVoid()){
            compiler.addInstruction(new WSTR("Erreur : sortie de la methode "+name+" sans return"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
        compiler.addLabel(finM);
        // restauration des registsres
        compiler.addComment("Restauration des registres");
        gencode.restoreRegisters(compiler, index);
        GenerationCode.isForMethods = false;
        
        compiler.addInstruction(new RTS());
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        body.iter(f);
    }

}
