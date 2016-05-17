package org.sbgrid.data.adsc;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ADSCTest {
	static final String ADSC_IMG = "/adsc.img";
	/* http://cci.lbl.gov/cctbx_downloads/regression/iotbx/adsc.img
	 * based on :
	 * https://sourceforge.net/p/cctbx/code/HEAD/tree/trunk/iotbx/detectors/tst_adsc.py
	 */ 
	@Test
	public void expectedRead() throws Exception {
		File ADSCFile = new File(ADSCTest.class.getResource(ADSC_IMG).toURI());
		ADSC adsc = new ADSC(ADSC.configuration(),ADSCFile.getAbsolutePath());
		Assert.assertEquals(2304, adsc.getMetadata().getPixelsSizeX(0).getNumberValue().intValue());
		Assert.assertEquals(2304, adsc.getMetadata().getPixelsSizeY(0).getNumberValue().intValue());
		Assert.assertEquals(0.0816, adsc.getMetadata().getPixelsPhysicalSizeX(0).value().floatValue(),0.01);
	}
	
	public void single() throws Exception {
		String inputfile = "/home/mwm1/Work/biogrid/1/p3_6_1_009.img";
		String outputfile = "/home/mwm1/Work/biogrid/1/p3_6_1_009.ome";
		new ADSC(ADSC.configuration(),inputfile).write(outputfile);
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
