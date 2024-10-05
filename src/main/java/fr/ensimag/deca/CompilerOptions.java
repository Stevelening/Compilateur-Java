package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl20
 * @date 01/01/2024
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public int getMaxReg(){
        return maxReg;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public boolean getParseOnly(){
        return parseOnly;
    }
    
    public boolean getVerificationOnly(){
        return verificationOnly;
    }

    public boolean getCheckExecErrors(){
        return checkExecErrors;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public boolean getGenByte() {
        return genByte;
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();

    //ADDED
    private boolean parseOnly = false;
    private boolean verificationOnly = false;
    private boolean checkExecErrors = true;
    private int maxReg = 16;
    private boolean genByte = false;

    private boolean anyArgExceptBanner = false;
    
    public void parseArgs(String[] args) throws CLIException {
        // A FAIRE : parcourir args pour positionner les options correctement.


        int argsCounter = 0;
        while (argsCounter < args.length){

            if (args[argsCounter].charAt(0) == '-'){
                switch (args[argsCounter]) {

                    case "-b":
                    if (anyArgExceptBanner) { throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source)."); }
                        printBanner = true;
                        break;
                    case "-d":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        debug++;
                        break;
                    case "-P":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        parallel = true;
                        break;                    
                    case "-p":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        if (verificationOnly){ throw new CLIException("Options -p et -v incompatibles !"); }
                        parseOnly = true;
                        break;
                    case "-v":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        if (parseOnly){ throw new CLIException("Options -p et -v incompatibles !"); }
                        verificationOnly = true;
                        break;                    
                    case "-r":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        argsCounter++;
                        if (argsCounter >= args.length) { throw new CLIException("Bad option usage, use -r X, with 4 <= X <= 16"); }
                        
                        try {
                            maxReg = Integer.parseInt(args[argsCounter]);
                            if (maxReg < 4 || maxReg > 16){
                                throw new CLIException("Bad option usage, use -r X, with 4 <= X <= 16");
                            }
                        } catch (Exception e) {
                            if (e instanceof NumberFormatException){
                                throw new CLIException("Bad option usage, use -r X, with 4 <= X <= 16");
                            } else {
                                throw e;
                            }
                        }
                        break;  
                    case "-B":
                            genByte = true;
                            break;
                    case "-n":
                        if (printBanner) {
                            throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                        }else{ anyArgExceptBanner = true; }
                        checkExecErrors = false;
                        break;

                    default:
                        throw new CLIException("L'option '" + args[argsCounter] + "' n'est pas implémentée.");
                        
                }
            } else {
                if (printBanner) {
                    throw new CLIException("L'option -b ne peut être utilisée que seule, sans autre option ou argument (fichier source).");
                }else{ anyArgExceptBanner = true; }
                sourceFiles.add(new File(args[argsCounter]));
            }

            argsCounter++;
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

        //throw new UnsupportedOperationException("not yet implemented");
    }

    protected void displayUsage() {
        System.out.println("\nusage : decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]\n\n"  + //
                ". -b (banner) : affiche une bannière indiquant le nom de l'équipe\n" + //
                ". -p (parse) : arrête decac après l'étape de construction de\n" + //
                "\tl'arbre, et affiche la décompilation de ce dernier\n" + //
                "\t(i.e. s'il n'y a qu'un fichier source à\n" + //
                "\tcompiler, la sortie doit être un programme\n" + //
                "\tdeca syntaxiquement correct)\n" + //
                ". -v (verification) : arrête decac après l'étape de vérifications\n" + //
                "\t(ne produit aucune sortie en l'absence d'erreur)\n" + //
                ". -n (no check) : supprime les tests à l'exécution spécifiés dans\n" + //
                "\tles points 11.1 et 11.3 de la sémantique de Deca.\n" + //
                ". -r X (registers) : limite les registres banalisés disponibles à\n" + //
                "\tR0 ... R{X-1}, avec 4 <= X <= 16\n" + //
                ". -d (debug) : active les traces de debug. Répéter\n" + //
                "\tl'option plusieurs fois pour avoir plus de\n" + //
                "\ttraces.\n" + //
                ". -P (parallel) : s'il y a plusieurs fichiers sources,\n" + //
                "\tlance la compilation des fichiers en\n" + //
                "\tparallèle (pour accélérer la compilation)\n" + //
                "\nN.B. Les options '-p' et '-v' sont incompatibles.\n");
    }
}
