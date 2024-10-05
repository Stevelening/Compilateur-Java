package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;

import java.util.ArrayList;

import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl20
 * @date 01/01/2024
 */
public class ClassDefinition extends TypeDefinition {


    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass; 

    public EnvironmentExp getMembers() {
        return members;
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }
    
    // tableau d'etiquettes des methodes de la classe
    private ArrayList<Label> methodLabels = new ArrayList<>();
    private ArrayList<String> methodsNames = new ArrayList<>();
    private int vTableAddress = 1;

    public void addMethodLabel(Label label){
        methodLabels.add(label);
    }

    public ArrayList<Label> getMethodLabels(){
        return methodLabels;
    }

    public ArrayList<String> getMethodsNames(){
        return methodsNames;
    }

    public void addMethodName(String name){
        methodsNames.add(name);
    }

    public int getVTableAddress(){
        return vTableAddress;
    }

    public void setVTableAddress(int value){
        vTableAddress = value;
    }
}
