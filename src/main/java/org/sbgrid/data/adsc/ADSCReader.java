package org.sbgrid.data.adsc;

import java.io.IOException;

import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;

public class ADSCReader extends FormatReader {
	ADSC adsc;
	public ADSCReader() {
		super("ADSC",new String[]{"img"});
	}
	
	
	@Override
	public boolean isThisType(byte[] block) {
		return "{".equals(new String(new byte[]{block[0]}));
	}

	@Override
	public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h) throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getBitsPerPixel() {
		return 16;
	}
	
	@Override
	public int getImageCount() {
		return 1;
	}
	
	@Override
	public int getPixelType() {
		return FormatTools.UINT16;
	}
	@Override
	public boolean isLittleEndian() {
		return false;
	};
	
	@Override
	public MetadataStore getMetadataStore() {
		return adsc.getMetadata();
	}
	
	@Override
	protected void initFile(String id) throws FormatException, IOException {
		super.initFile(id);
		try {
			adsc = new ADSC (id);
			MetadataStore store = makeFilterMetadata();
			setMetadataStore(store);
		} catch(Exception e){
			throw new IOException(e);
		}
	}
	@Override
	public int getSizeC() {
		// TODO Auto-generated method stub
		return super.getSizeC();
	}
	static public void main(String [] args) throws Exception {
		String id = "/home/mwm1/Work/biogrid/sbgrid-tools/SBGRID/84/JZ12DS.0004.img";
	    IFormatReader reader = new ADSCReader();
	    ServiceFactory factory = new ServiceFactory();
	    OMEXMLService service = factory.getInstance(OMEXMLService.class);
	    IMetadata meta = service.createOMEXMLMetadata();
	    reader.setMetadataStore(meta);
	    System.out.println("Initializing file: " + id);
	    reader.setId(id); // parse metadata
		
	 }
}
