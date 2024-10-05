package fr.ensimag.deca;

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl20
 * @date 01/01/2024
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        
        if (options.getPrintBanner()) {
            System.out.println("\n" + //
                    "==========================================================\n" + //
                    "              ,--,            ,----,       ,----..\n" + //
                    "            ,--.'|          .'   .' \\     /   /   \\\n" + //
                    "            |  | :        ,----,'    |   /   .     :\n" + //
                    "  ,----._,. :  : '        |    :  .  ;  .   /   ;.  \\\n" + //
                    " /   /  ' / |  ' |        ;    |.'  /  .   ;   /  ` ;\n" + //
                    "|   :     | '  | |        `----'/  ;   ;   |  ; \\ ; |\n" + //
                    "|   | .\\  . |  | :          /  ;  /    |   :  | ; | '\n" + //
                    ".   ; ';  | '  : |__       ;  /  /-,   .   |  ' ' ' :\n" + //
                    "'   .   . | |  | '.'|     /  /  /.`|   '   ;  \\; /  |\n" + //
                    " `---`-'| | ;  :    ;   ./__;      :    \\   \\  ',  /\n" + //
                    " .'__/\\_: | |  ,   /    |   :    .'      ;   :    /\n" + //
                    " |   :    :  ---`-'     ;   | .'          \\   \\ .'\n" + //
                    "  \\   \\  /              `---'              `---`\n" + //
                    "   `--`-'\n" + //
                    "==========================================================\n\n" + //
                    "gl20");
        }
        
        if (options.getSourceFiles().isEmpty() && !options.getPrintBanner()) {
            System.err.println("Error: Veuillez spécifier au moins un fichier source.");
            System.exit(1);
        }
        
        if (options.getParallel() && options.getSourceFiles().size()>1) {
            // A FAIRE : instancier DecacCompiler pour chaque fichier à
            // compiler, et lancer l'exécution des méthodes compile() de chaque
            // instance en parallèle. Il est conseillé d'utiliser
            // java.util.concurrent de la bibliothèque standard Java. 
            
            ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 10, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

            for (File source : options.getSourceFiles()) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("debut tache " + Thread.currentThread().getName());
                       // try {
                       //     Thread.sleep(1000);
                        //} catch (InterruptedException e) {
                        //    e.printStackTrace();
                        //}
                        DecacCompiler compiler = new DecacCompiler(options, source);
                        if (compiler.compile()) {
                            //error = true;
                        }

                        LOG.info("fin tache");
                    }
                });
            }

            executorService.shutdown();
            try {
              executorService.awaitTermination(300, TimeUnit.SECONDS);
            } catch (InterruptedException e){
              e.printStackTrace();
            }

        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}
