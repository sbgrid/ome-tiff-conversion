package org.sbgrid.data;

import java.io.File;

import loci.formats.FormatWriter;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;

public abstract class Format {
	IMetadata metadata;
	ImageData imageData;
	public IMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(IMetadata metadata){
		this.metadata = metadata;
	}

	public ImageData getImageData() {
		return imageData;
	}
	public void setImageData(ImageData imageData) {
		this.imageData = imageData;
	}
	
	public void write(String outputfile) throws Exception {
		File file = new File(outputfile);
		if(file.exists())
	    {   System.err.println("deleting outputfile");
			new File(outputfile).delete(); }
		IMetadata omexml = getMetadata();
		ImageWriter writer = new ImageWriter();
		writer.setMetadataRetrieve(omexml);
	    writer.setId(outputfile);
	    writer.saveBytes(0, getImageData().data);
		writer.close();
	}
}
