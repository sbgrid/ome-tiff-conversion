package org.sbgrid.data.adsc;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.sbgrid.data.Configuration;
import org.sbgrid.data.Format;
import org.sbgrid.data.ImageData;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.PixelType;

/**
 *
 * @author major seitan References
 *         https://www.openmicroscopy.org/site/support/bio-formats5.1/_downloads
 *         /FileExport.java
 *         http://dawb.eclipselabs.org.codespot.com/svn-history/r1274/trunk/uk.
 *         ac.diamond.scisoft.analysis/src/uk/ac/diamond/scisoft/analysis/io/
 *         ADSCImageLoader.java
 */
public class ADSC extends Format {
	public void upsert(Map<String, List<String>> kv,String key,String value){
		if(!kv.containsKey(key)){
			kv.put(key,new ArrayList<String>());
		}
		List<String> values = kv.get(key);
		values.add(value);
	}
	public ADSC(Configuration configuration, String inputfile) throws Exception {
		RandomAccessFile raf = new RandomAccessFile(inputfile, "r");
		Map<String, List<String>> attributes = getAttributes(raf);
		upsert(attributes,"filename", inputfile);
		String header = fileHeader(configuration,raf, attributes);
		setMetadata(attributesToMetadata(configuration, attributes, header));
		ImageData imageData = fileImageData(configuration,raf, attributes);
		setImageData(imageData);
	}

	private static String SHA1(RandomAccessFile raf) throws IOException, NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		raf.seek(0);
		byte[] buffer = new byte[8192];
		int len = raf.read(buffer);

		while (len != -1) {
			sha1.update(buffer, 0, len);
			len = raf.read(buffer);
		}

		return new HexBinaryAdapter().marshal(sha1.digest());
	}

	private String fileHeader(Configuration configuration, RandomAccessFile raf, Map<String, List<String>> attributes) throws Exception {
		int pointer = Integer.parseInt(configuration.resolveProperty("HEADER_BYTES", attributes).trim());
		raf.seek(0);
		byte headerByte[] = new byte[pointer];
		raf.read(headerByte);
		String raw = new String(headerByte);
		raw = raw.substring(0, raw.indexOf('}') + 1);
		return raw;
	}

	private ImageData fileImageData(Configuration configuration, RandomAccessFile raf, Map<String, List<String>> attributes) throws Exception {
		ImageData imageData = new ImageData();
		imageData.height = getMetadata().getPixelsSizeX(0).getValue();
		imageData.width = getMetadata().getPixelsSizeY(0).getValue();
		int size = (imageData.height * imageData.width) * 2;
		imageData.data = new byte[size];
		int pointer = Integer.parseInt(configuration.resolveProperty("HEADER_BYTES", attributes).trim());
		raf.seek(pointer);
		raf.read(imageData.data);
		String sha1 = SHA1(raf);
		upsert(attributes,"SHA-1", sha1);
		return imageData;
	}

	private IMetadata attributesToMetadata(Configuration configuration, Map<String, List<String>> attributes, String header) {

		Exception exception;
		try { // http://strucbio.biologie.uni-konstanz.de/ccp4wiki/index.php/SMV_file_format
				// create the OME-XML metadata storage object
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			OMEXMLMetadata metadata = service.createOMEXMLMetadata();
			metadata.createRoot();
			configuration.populateMetadata(metadata, attributes);
			// http://lists.openmicroscopy.org.uk/pipermail/ome-users/2013-January/003508.html
			int comment_id = 0;
			for (Entry<String, List<String>> entry : attributes.entrySet()) {
				for(String value : entry.getValue()){
				metadata.setCommentAnnotationID(entry.getKey(), comment_id);
				metadata.setCommentAnnotationValue(value, comment_id);
				}
				comment_id++;
			}

			metadata.setXMLAnnotationID("Annotation:XML0", 0);
			metadata.setXMLAnnotationDescription("ADSC parsed headers", 0);
			metadata.setXMLAnnotationValue("<header><![CDATA[" + header + "]]></header>", 0);

			String type = configuration.resolveProperty("TYPE", attributes);
			if (!"unsigned_short".equals(type) && !"unsigned short int".equals(type)) {
				throw new Exception(String.format("Unexpected type '%s'", type));
			}
			String byteOrder = configuration.resolveProperty("BYTE_ORDER", attributes);
			if ("little_endian".equals(byteOrder)) {
				metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
			} else if ("big_endian".equals(byteOrder)) {
				metadata.setPixelsBinDataBigEndian(Boolean.FALSE, 0, 0);
			} else {
				throw new Exception(String.format("Unexpected endian '%s'", byteOrder));
			}

			metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
			metadata.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(FormatTools.UINT16)), 0);
			String pixelSize = configuration.resolveProperty("PIXEL_SIZE", attributes);
			if (pixelSize != null) {
				metadata.setPixelsPhysicalSizeX(new Length(Float.parseFloat(pixelSize), UNITS.MILLI(UNITS.METRE)), 0);
				metadata.setPixelsPhysicalSizeY(new Length(Float.parseFloat(pixelSize), UNITS.MILLI(UNITS.METRE)), 0);
			} else {
				throw new Exception(String.format("Missing pixel size"));
			}
			return metadata;
		} catch (DependencyException e) {
			exception = e;
		} catch (ServiceException e) {
			exception = e;
		} catch (EnumerationException e) {
			exception = e;
		} catch (Exception e) {
			exception = e;
		}

		System.err.println("Failed to populate OME-XML metadata object.");
		throw new RuntimeErrorException(new Error(exception));
	}

	private Map<String, List<String>> getAttributes(RandomAccessFile in) throws Exception {
		Map<String, List<String>> metadata = new HashMap<String, List<String>>();
		int linenumber = 1;
		// handling metadata in the file header
		try {
			byte firstChar = in.readByte();
			in.seek(0);
			if (firstChar != '{')
				throw new Exception("This is not a valid ADSC image");
			String line = in.readLine();

			// an updated header reader which ignores all of the header.
			while (!line.contains("{")) {
				line = in.readLine();
				linenumber++;
			}

			while (true) {
				line = in.readLine();
				if (line.contains("}")) {// stop at end of header
					return metadata;
				} else if (line.contains("=")) {
					String[] keyvalue = line.split("=");
					String key = keyvalue[0];
					String value = keyvalue[1].substring(0, keyvalue[1].length() - 1);
					upsert(metadata,key,value);
				} // dropping the extra headers for now
				else {
					throw new Exception(
							String.format("There was a problem parsing the ADSC header information : %d", linenumber));
				}
			}
		} catch (IOException e) {
			throw new Exception(String.format("There was a problem parsing the ADSC header information %d", linenumber),e);
		}
	}

	static public Configuration configuration() {
		Configuration configuration = Configuration.read(Configuration.class.getResourceAsStream("/ADSC.xml"));
		return configuration;
	}
}
