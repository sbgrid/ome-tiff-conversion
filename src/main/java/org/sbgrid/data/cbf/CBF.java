package org.sbgrid.data.cbf;

import java.io.RandomAccessFile;
import java.rmi.UnexpectedException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import org.sbgrid.data.Format;
import org.sbgrid.data.ImageData;
import org.sbgrid.data.Util;

import loci.common.services.ServiceFactory;
import loci.formats.meta.IMetadata;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.primitives.PositiveInteger;
public class CBF extends Format {
	Pattern COMPRESSION_SCHEME_PATTERN = Pattern.compile("conversions=\"([^\"]+)");
	static private final String HEADER_PREFIX = "###CBF:";
	static private final String BINARY_SECTION_PREFIX = "--CIF-BINARY-FORMAT-SECTION--";

	public CBF(String inputfile) throws Exception {
		RandomAccessFile in = new RandomAccessFile(inputfile, "r");
		Map<String,String> attributes = getAttributes(in);
		setMetadata(getMetadata(attributes));
		setImageData(fileImageData(in,attributes));
	}
	public IMetadata getMetadata(Map<String, String> attributes){
		Exception exception;
		try{
		  // http://strucbio.biologie.uni-konstanz.de/ccp4wiki/index.php/SMV_file_format
	      // create the OME-XML metadata storage object
	      ServiceFactory factory = new ServiceFactory();
	      OMEXMLService service = factory.getInstance(OMEXMLService.class);
	      OMEXMLMetadata metadata = service.createOMEXMLMetadata();
	      service.populateOriginalMetadata(metadata, new Hashtable<>(attributes));
	      metadata.createRoot();
	      metadata.setImageID("Image:0", 0);
	      metadata.setPixelsID("Pixels:0", 0);
	      Optional<Boolean> isLittleEndian = CBFTypes.isLittleEndian(attributes.get(CBFTypes.ELEMENT_BYTE_ORDER_KEY));
	      set(CBFTypes.ELEMENT_BYTE_ORDER_KEY,
	    	  attributes,
	    	  option -> CBFTypes.isLittleEndian(option),
	    	  endian -> metadata.setPixelsBinDataBigEndian(endian, 0, 0),
	    	  "missing ending");

	      metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
	      set(CBFTypes.ELEMENT_TYPE_KEY,
		    	  attributes,
		    	  option -> CBFTypes.determineElement(option),
		    	  pixelType -> metadata.setPixelsType(pixelType, 0),
		    	  "missing pixel type");
	      
	      metadata.setPixelsSizeY(new PositiveInteger(Integer.parseInt(attributes.get("X-Binary-Size-Second-Dimension"))), 0);
	      metadata.setPixelsSizeX(new PositiveInteger(Integer.parseInt(attributes.get("X-Binary-Size-Fastest-Dimension"))), 0);
	      metadata.setPixelsSizeZ(new PositiveInteger(1), 0);
	      metadata.setPixelsSizeC(new PositiveInteger(1), 0);
	      metadata.setPixelsSizeT(new PositiveInteger(1), 0);
	      metadata.setChannelID("Channel:0:0", 0, 0);
	      metadata.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
	      return metadata;
		}catch (Exception e) { 
			exception = e; 
		}

	    System.err.println("Failed to populate OME-XML metadata object.");
	    throw new RuntimeErrorException(new Error(exception));
	}
	private Map<String, String> getAttributes(RandomAccessFile in) throws Exception {
		int linenumber = 1;
		try {
			Map<String, String> metadata = new HashMap<>();
			String line = in.readLine();

			if (!line.startsWith(HEADER_PREFIX))
				throw new Exception("This is not a valid CBF image");

			while (!line.startsWith(BINARY_SECTION_PREFIX)) {
				line = in.readLine();
				linenumber++;
			}
			while (true) {
				line = in.readLine();
				Matcher matcher = COMPRESSION_SCHEME_PATTERN.matcher(line);
				if (line.isEmpty()) {
					return metadata;
				} else if (matcher.find()) {
					metadata.put(CBFTypes.COMPRESSION_SCHEME_KEY, matcher.group(1));
				} else if (line.contains(":")) {
					String[] keyvalue = line.split(":");
					String key = keyvalue[0];
					String value = keyvalue[1].substring(0, keyvalue[1].length()).trim();
					metadata.put(key,value);
				}
			}
		} catch (Exception e) {
			throw new Exception(String.format("There was a problem parsing the CBF header information %d", linenumber),
					e);
		}
	}

	static final byte[] SENTINEL = new byte[] { (byte) 0x0C, (byte) 0x1A, (byte) 0x04, (byte) 0xD5 };
	static final String MD5_KEY = "Content-MD5";
	private boolean checkSentinel = true;
	private boolean checkMd5 = true;
	private boolean checkReadSize = true;

	private ImageData fileImageData(RandomAccessFile raf, Map<String, String> metadata) throws Exception {

		ImageData imageData = new ImageData();
		imageData.height = Integer.parseInt(metadata.get("X-Binary-Size-Second-Dimension"));
		imageData.width = Integer.parseInt(metadata.get("X-Binary-Size-Fastest-Dimension"));
		int binarySize = Integer.parseInt(metadata.get("X-Binary-Size"));
		int imageSize = Integer.parseInt(metadata.get("X-Binary-Number-of-Elements"));
		System.out.println(imageSize);
		imageData.data = new byte[binarySize];
		byte[] sentinel = new byte[4];
		raf.read(sentinel);
		// check sentinel
		if (checkSentinel && !Arrays.equals(sentinel, SENTINEL)) {
			String message = String.format("Failed Sentinel Check found %s expected %s", Util.bytesToHex(sentinel),
					Util.bytesToHex(SENTINEL));
			throw new UnexpectedException(message);
		}
		int readSize = raf.read(imageData.data);
		if (checkReadSize && readSize != binarySize) {
			String message = String.format("Failed Read Size Check found %d expected %d", readSize, binarySize);
			throw new UnexpectedException(message);

		}
		if (checkMd5 && metadata.containsKey(MD5_KEY)) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(imageData.data);
			Encoder encoder = Base64.getEncoder();
			String actual = new String(encoder.encode(digest), "US-ASCII");
			String expected = metadata.get(MD5_KEY);
			if (!actual.equals(expected)) {
				String message = String.format("Failed md5 Check found '%s' expected '%s'", actual.getBytes(),
						expected.getBytes());
				throw new UnexpectedException(message);

			}
		}
		imageData.data = ByteOffset.decompress(imageData.data,getMetadata().getPixelsType(0));
		System.out.println(imageSize);
		return imageData;
	}
	public static void main(String[] args) throws Exception {
		String inputfile = "/home/mwm1/Work/biogrid/175/hse85_3_15_1_0796.cbf";
		String outputfile = "/home/mwm1/Work/biogrid/175/hse85_3_15_1_0796.tiff";
		new CBF(inputfile).write(outputfile);
		;
	}
}
