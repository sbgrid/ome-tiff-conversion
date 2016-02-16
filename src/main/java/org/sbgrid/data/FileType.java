package org.sbgrid.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public enum FileType {
	CBF, ADSC , RAXIS, TIFF, MARC;
	
	static public Optional<FileType> fileType(String path) throws IOException{
    	RandomAccessFile in = new RandomAccessFile(new File(path), "r");
    	byte[] buffer = new byte[1];
    	in.read(buffer);
    	final Optional<FileType> filetype;
    	switch(new String(buffer)){
    	case "#":
    		filetype = Optional.of(CBF);
    		break;
    	case "R":
    		filetype = Optional.of(RAXIS);
    		break;
    	case "{":
    		filetype = Optional.of(ADSC);
    		break;
    	case "I":
    		filetype = Optional.of(TIFF);
    		break;
    	default:
	        // http://marxperts.com/man/html/mar300_formats.html
	        in.seek(124);
	        byte[] marc = new byte[10];
	    	in.read(marc);
	    	if ("MARCONTROL".equals(new String(marc))) {
	    		filetype = Optional.of(MARC);
	    	} else {
	    		filetype = Optional.empty();
	    	}
	    }
	    return filetype;	
		
	}
	
	public static void main(String arg[]) throws Exception {
		Files.walk(Paths.get("/home/mwm1/Work/biogrid/sbgrid-tools/SBGRID"))
		.forEach(filePath -> {
			try {
		    if (Files.isRegularFile(filePath)) {
		        //System.out.println(filePath);
		    	
		    }
			}catch(Exception e){ e.printStackTrace();}
		});
	}
}
