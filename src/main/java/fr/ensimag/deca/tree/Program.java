package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.ObjectInternals;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Line;
import static fr.ensimag.deca.tools.ObjectInternals.*;//.getIdentifier;
//import fr.ensimag.deca.tools.ObjectInternals.initializeObjEqualsDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    //Partie B :
    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");

        //generation de object
        AbstractIdentifier obj = getIdentifier(compiler);
        ClassType type = compiler.environmentType.OBJECT; //new ClassType(obj.getName(), getLocation(), null);
        ClassDefinition def = type.getDefinition();
        //compiler.environmentType.addClassToEnvType(obj.getName(), def);
        EnvironmentExp predefEnv = def.getMembers();//new EnvironmentExp(null);
        MethodDefinition equalsDef = initializeObjEqualsDefinition(compiler);
        try {
            predefEnv.declare(compiler.createSymbol("equals"), equalsDef); 
        } catch (DoubleDefException e) {
            throw new ContextualError("Identifiant déjà déclaré dans le contexte courant.", getLocation());
        }
        //ClassDefinition newDef = new ClassDefinition(type, getLocation(), null);
        //compiler.environmentType.addClassToEnvType(obj.getName(), newDef);

        obj.setType(type);
        obj.setDefinition(def);
        

        //Passe 1
        classes.verifyListClass(compiler);

        //Passe 2
        classes.verifyListClassMembers(compiler);

        //Passe 3
        classes.verifyListClassBody(compiler); // N'est pas nécessaire pour la partie sans objet.
        main.verifyMain(compiler);
        
        LOG.debug("verify program: end");
    }

    //Partie C :
    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code

        // Passe 1
        Line tsto = new Line(new TSTO(0));
        Line addsp = new Line(new ADDSP(0));
        if(compiler.getCompilerOptions().getCheckExecErrors()){ // for option -n
            compiler.add(tsto);
            compiler.add(new Line(new BOV(GenerationCode.stack_overflow_error)));
        }
        compiler.add(addsp);

        ObjectInternals.codeGenObjectVTable(compiler);
        classes.codeGenVTable(compiler);
        
        // Passe 2
        compiler.addComment("Main program");
        main.codeGenMain(compiler);

        tsto.setInstruction(new TSTO(new ImmediateInteger(gencode.getGlobalVars())));
        addsp.setInstruction(new ADDSP(new ImmediateInteger(gencode.getStackSize())));

        compiler.addInstruction(new HALT());
        compiler.addComment("end main program");

        // Code de l'initialisation des champs 
        ObjectInternals.codeGenInitObject(compiler);
        classes.codeGenInitClass(compiler);

        // Code des mehtodes
        ObjectInternals.codeGenObjectMethods(compiler);
        classes.codeGenMethods(compiler);

        // Gestion des erreurs d'execution
        compiler.addComment("Errors messages");
        GenerationCode.getErrors(compiler);
    }
    @Override
    public void byteCodeGenProgram(DecacCompiler compiler, String filename){
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        try {
            PrintWriter pw = new PrintWriter(filename);
            TraceClassVisitor cv = new TraceClassVisitor(cw, pw);
            main.byteCodeGenMain(compiler, cv);
            cv.visitEnd();
            byte[] b = cw.toByteArray();
            FileOutputStream fileOutputStream = new FileOutputStream(/*filename + ".class" à utiliser après*/"Main.class");
            //Method to print the byte array
            fileOutputStream.write(b);
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //TODO : Add class related byteCodeGen

    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
