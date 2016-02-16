package org.sbgrid.data;

import org.sbgrid.data.adsc.ADSC;
import org.sbgrid.data.cbf.CBF;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Converter {
	public enum Formats implements IStringConverter<Formats> {
	    ADSC, CBF;

		@Override
		public Formats convert(String f) {
			return valueOf(f);
		}
	}
	  @Parameter(names = { "-i", "-infile" }, description = "in file",required = true)
	  private String infile;
	 
	  @Parameter(names = { "-o", "-outfile" }, description = "out file",required = true)
	  private String outfile;
	  
	  @Parameter(names = { "-f", "-format" }, description = "file format",required = true)
	  private Formats format;

	  public void run() throws Exception {
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
		Converter cli = new Converter();
		JCommander jcommander = new JCommander(cli, args);
		cli.run();

	}
}
