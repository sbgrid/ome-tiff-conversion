package org.sbgrid.data;

import org.sbgrid.data.adsc.ADSC;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import loci.formats.tiff.TiffSaver;

public class Converter {
    public enum Formats implements IStringConverter<Formats> {
	ADSC, CBF;

	@Override
	public Formats convert(String f) {
	    return valueOf(f);
	}
    }
    @Parameter(names = { "-d", "-debug" }, description = "Debug mode")
    private boolean debug = false;

    @Parameter(names = { "-h" , "--help" }, help = true)
    private boolean help = false;

    @Parameter(names = { "-l", "-logile" }, description = "log file",required = false)
    private String logfile = null;

    @Parameter(names = { "-i", "-infile" }, description = "in file",required = true)
    private String infile;

    @Parameter(names = { "-o", "-outfile" }, description = "out file",required = true)
    private String outfile;

    @Parameter(names = { "-f", "-format" }, description = "file format",required = true)
    private Formats format;

    public void configureLogfile(){
	// http://www.programcreek.com/java-api-examples/index.php?api=ch.qos.logback.core.FileAppender
	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	context.reset();
	PatternLayoutEncoder encoder = new PatternLayoutEncoder();
	encoder.setContext(context);
	encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
	encoder.start();
	if(logfile == null){
	} else if ("-".equals(logfile)) {
	    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
	    consoleAppender.setEncoder(encoder);
	    consoleAppender.setContext(context);
	    consoleAppender.start();
	    root.addAppender(consoleAppender);
	} else {
	    FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
	    fileAppender.setFile(logfile);
	    fileAppender.setContext(context);
	    fileAppender.setEncoder(encoder);
	    fileAppender.start();
	    root.addAppender(fileAppender);
	}
    }
    public void run() throws Exception {
	configureLogfile();
	Format f = null;
	switch(format){
	case ADSC:
	    f = new ADSC(infile);
	    break;
	case CBF:
	    //f = new CBF(infile);
	    break;
	default:
	    throw new Exception("unsupported format");
	}
	f.write(outfile);
    }
    public static void main(String[] args) throws Exception {
	TiffSaver save = new TiffSaver("filename");
	Converter cli = new Converter();
	JCommander jCommander = null;
	try {
	jCommander = new JCommander(cli, args);
	if(cli.help)
	    { jCommander.usage(); }
	else { cli.run(); }
	} catch(com.beust.jcommander.ParameterException e){
	    System.out.println(e.getMessage());
	    new JCommander(cli).usage();
	}
    }
}
