package org.sbgrid.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

public enum FileType {
	CBF, ADSC , RAXIS, TIFF, MARC, MAR,DESMOND,DESMOND_CHECKPOINT;
	
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
    	case "{": // Fooled by desmond files
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
	    	} else if ("mar2300".equals(FilenameUtils.getExtension(path))
	    			  || "mar1200".equals(FilenameUtils.getExtension(path))) {
	    		filetype = Optional.of(MAR);
	    	} else if ("cpt".equals(FilenameUtils.getExtension(path))) {
	    		filetype = Optional.of(DESMOND_CHECKPOINT);
	    	}else {
		        byte[] desm = new byte[4];
	    		in.seek(0);
	    		in.read(desm);
		        if("DESM".equals(new String(desm))){
		        	filetype = Optional.of(DESMOND);
		        } else {
		        	filetype = Optional.empty();
		        }
	    	}
	    }
	    return filetype;	
		
	}
	
	public static void main(String arg[]) throws Exception {
		Map<FileType,Integer> count = new HashMap<>();
		for(FileType filetype : FileType.values()){
			count.put(filetype,0);
		}
		Files.walk(Paths.get("/home/mwm1/Work/biogrid/sbgrid-tools/SBGRID"))
		.forEach(filePath -> {
			try {
		    if (Files.isRegularFile(filePath)) {
		    	if (filePath.toFile().length() > 100000) {
		    		Optional<FileType> type = fileType(filePath.toString());
		    		if (type.isPresent())  {
		    			FileType filetype = type.get();
		    			count.put(filetype, count.get(filetype) + 1);
		    		} else {
		    			System.out.println(filePath);
		    		}
		    	}
		    }
			}catch(Exception e){ e.printStackTrace();}
		});
		for(Map.Entry<FileType,Integer> entry : count.entrySet()){
			System.out.println(String.format(" %s %d", entry.getKey(), entry.getValue()));
		}
	}
}
