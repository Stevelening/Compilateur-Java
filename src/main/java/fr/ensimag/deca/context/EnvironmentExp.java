package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 * 
 * Insertion (through method declare) is always done in the "current" dictionary.
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class EnvironmentExp {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).

    EnvironmentExp parentEnvironment;

    private Map<Symbol, ExpDefinition> environment;
    
    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
        environment = new HashMap<>();
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        if (environment.containsKey(key)) {
            return environment.get(key);
        }

        if (parentEnvironment != null){
            return parentEnvironment.get(key);
        }
        return null;

        //throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (environment.containsKey(name)) {
            throw new DoubleDefException();
        }

        environment.put(name, def);

        //throw new UnsupportedOperationException("not yet implemented");
    }

    public ArrayList<ExpDefinition> getEnvironmentFields(){
        ArrayList<ExpDefinition> liste = new ArrayList<>();
        for(ExpDefinition exp : environment.values()){
            if(exp.isField()){
                liste.add(exp);
            }
        }
        return liste;
    }
}
