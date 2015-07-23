package com.jorgen;

import com.jorgen.store.KvStore;
import com.jorgen.store.KvStoreMapDB;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    private static final String DEFAULT_PORT = "3434";
    private static final String DEFAULT_THREADS = "10";
    private static final String DEFAULT_FILE = "my.db";


    public static void main( String[] args ) throws ParseException, InterruptedException {
        CommandLine cmdLine = parseArguments(args);
        final int port = Integer.parseInt(cmdLine.getOptionValue(PORT_OPTION.getOpt(), DEFAULT_PORT));
        final int threads = Integer.parseInt(cmdLine.getOptionValue(THREADS_OPTION.getOpt(), DEFAULT_THREADS));
        final String file = cmdLine.getOptionValue(FILE_OPTION.getOpt(), DEFAULT_FILE);

        final KvStore kvStore = new KvStoreMapDB(file);
        new Server(kvStore).
                run(port, threads);
    }


    private static CommandLine parseArguments(String[] args) throws ParseException {
        LOG.info("Command line arguments:" + args.toString());
        Options options = createOptions();
        CommandLine cmdLine = new PosixParser().parse(options, args);
        // Print help and exit if "-h" flag specified.
        if (cmdLine.hasOption(HELP_OPTION.getOpt())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Main", options);
            System.exit(0);
        }
        return cmdLine;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(PORT_OPTION);
        options.addOption(THREADS_OPTION);
        options.addOption(FILE_OPTION);
        options.addOption(HELP_OPTION);
        return options;
    }

    @SuppressWarnings("static-access")
    private static Option PORT_OPTION = OptionBuilder.
            hasArg(true).
            withArgName("port").
            withDescription("The port the KvServer is listening on").
            create("p");

    @SuppressWarnings("static-access")
    private static Option THREADS_OPTION = OptionBuilder.
            hasArg(true).
            withArgName("threads").
            withDescription("The number of server threads").
            create("t");

    @SuppressWarnings("static-access")
    private static Option FILE_OPTION = OptionBuilder.
            hasArg(true).
            withArgName("file").
            withDescription("File name (full path) to the kv storage").
            create("f");

    @SuppressWarnings("static-access")
    private static Option HELP_OPTION = OptionBuilder
            .withDescription("print this help")
            .create("h");


}
