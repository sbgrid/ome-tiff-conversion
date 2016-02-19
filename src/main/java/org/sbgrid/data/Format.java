package org.sbgrid.data;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
	
	public <E> void set(String key,Map<String,String> attribute,Function<String,Optional<E>> map,Consumer<E> c,String message) throws Exception {
		if(attribute.containsKey(key)){
			String value = attribute.get(key);
			value=value.replace('"',' ').trim();
			Optional<E> option = map.apply(value);
			if(option.isPresent()) {
				c.accept(option.get());
			} else {
				throw new Exception(message);
			}
		} else {
			throw new Exception(message);
		}
			
	}
}
