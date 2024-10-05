package fr.ensimag.deca.codegen;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;

public class GenerationCode {
    private static int currentRegister = 2; // le numero du premier registre libre
    private static int MAX = 15; // indice du dernier registre utilisable
    private static int global_vars = 0; // nombre de variables globales
    private static int local_vars = 0; // nombre de variables locales a une methode
    private static int stack_size = 0; // taille de la pile

    public void setMAX(int value){
        MAX = value;
    }

    public Register getCurrentRegister(){
        return Register.getR(currentRegister);
    }

    public int getCurrent(){
        return currentRegister;
    }

    public void setCurrent(int value){
        currentRegister = value;
    }

    public boolean nextRegister(){ // retourne true s'il le registre suivant existe
        if(currentRegister < MAX){
            currentRegister += 1;
            return true;
        }
        else{
            return false;
        }
    }

    public int getGlobalVars(){
        return global_vars;
    }

    public void increaseGlobalVars(){
        global_vars += 1;
    }

    public int getLocalVars(){
        return local_vars;
    }

    public void increaseLocalVars(){
        local_vars += 1;
    }

    public void initLocalVars(){
        local_vars = 0;
    }

    public int getStackSize(){
        return stack_size;
    }

    public void increaseStackSize(){
        stack_size += 1;
    }

    public void increaseStackSize(int value){
        stack_size += value;
    }

    public void decreaseStackSize(){
        stack_size -= 1;
    }

    public static Label overflow_error = new Label("overflow_error");
    public static Label stack_overflow_error = new Label("stack_overflow_error");
    public static Label io_error = new Label("io_error");
    public static Label dereferencement_null = new Label("dereferencement_null");
    public static Label heap_error = new Label("heap_error");

    public static void getErrors(DecacCompiler compiler){
        compiler.addLabel(overflow_error);
        compiler.addInstruction(new WSTR("Error: Overflow during arithmetic operation"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());

        compiler.addLabel(stack_overflow_error);
        compiler.addInstruction(new WSTR("Erreur : La pile est pleine"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());

        compiler.addLabel(heap_error);
        compiler.addInstruction(new WSTR("Erreur : Le tas est plein"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());

        compiler.addLabel(io_error);
        compiler.addInstruction(new WSTR("Error: Input/Output error"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());

        compiler.addLabel(dereferencement_null);
        compiler.addInstruction(new WSTR("Erreur : Dereferencement de null"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }

    // isForClass est un bolléen qui nous permet de savoir que l'appel de codeGenInst est 
    // faite dans une classe, ainsi, on n'incremente pas le nombre de cases memoires a allouer
    // dans la pile pour les variables globales + construction de la vtable lorsqu'on fait le PUSH.
    // on incremente plutot dClass qui est alors le nombre de push necessaire lorsqu'on veut initialiser 
    // les champs d'une classe
    public static Boolean isForClass = false;
    public static int dClass = 0;

    // Idem que isForClass et dClass, mais dans le cas d'une mehtode
    public static Boolean isForMethods = false;
    public static int dMethod = 0;

    public int saveRegisters(DecacCompiler compiler){
        int index = this.getCurrent(); // >= 2
        for(int i = 2; i <= index; i++){
            compiler.addInstruction(new PUSH(Register.getR(i)));
            if(GenerationCode.isForClass){
                GenerationCode.dClass += 1;
            }
            else if(GenerationCode.isForMethods){
                GenerationCode.dMethod += 1;
            }
        }
        this.setCurrent(2);
        return index;
    }

    public void restoreRegisters(DecacCompiler compiler, int index){
        for(int i = index; i >= 2;i--){
            compiler.addInstruction(new POP(Register.getR(i)));
        }
        this.setCurrent(index);
    }

    private static int index = 0;

    public int getIndex(){
        return index;
    }

    public void incrIndex(){
        index += 1;
    }

    // cette variable permettra de creer l'etiquete qui marque la fin d'une methode dans le code assembleur
    public static String finMethode = "";

    // cette variable vaut true lorsque la selection s'effectue a gauche d'un Assign
    public static Boolean isLeftAssign = false;

    // Partie C : génération du code assembleur (02 passes)

    // passe 1 : 
    // construction de la table des methodes (vtable) :
    // - construction du tableau des etiquetes des methodes
    // - generation du code permettant de construire la table des methodes


    // passe 2 : 
    // - codage des champs de chaque classe
    // - codage des methodes de chaque classe
    // - codage de la methode principale
}


// dans ce package, on met le code commun aux classes du package tree