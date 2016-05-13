package org.sbgrid.data.adsc;

public class ADSCTest {
	public void single() throws Exception {
		String inputfile = "/home/mwm1/Work/biogrid/1/p3_6_1_009.img";
		String outputfile = "/home/mwm1/Work/biogrid/1/p3_6_1_009.ome";
		new ADSC(inputfile).write(outputfile);
	}
    /*
	public void multiple() throws Exception {
		Files.walk(Paths.get("/home/mwm1/Work/biogrid/sbgrid-tools/SBGRID"))
		.forEach(filePath -> {
			try {
		    if (Files.isRegularFile(filePath)) {
		    	String path = filePath.toFile().getAbsolutePath();
		    	Optional<FileType> filetype = FileType.fileType(path);
		    	if (filetype.isPresent())
		    	switch(filetype.get()){
		    	case ADSC:
		    		System.out.println(filePath);
		    		new ADSC(path).write(path+".tiff");

		 		}
		    }
			}catch(Exception e){ e.printStackTrace();}
		});

	}
    */
	public static void main(String[] args) throws Exception {

	}

}
