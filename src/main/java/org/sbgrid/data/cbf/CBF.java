package org.sbgrid.data.cbf;

import java.io.RandomAccessFile;

import java.rmi.UnexpectedException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbgrid.data.Format;
import org.sbgrid.data.ImageData;
import org.sbgrid.data.Util;
import org.sbgrid.data.cbf.CBFTypes.ElementType;

import loci.formats.FormatTools;
public class CBF {}
/*
public class CBF extends Format {
	Pattern COMPRESSION_SCHEME_PATTERN = Pattern.compile("conversions=\"([^\"]+)");
	static private final String HEADER_PREFIX = "###CBF:";
	static private final String BINARY_SECTION_PREFIX = "--CIF-BINARY-FORMAT-SECTION--";

	public CBF(String inputfile) throws Exception {
		RandomAccessFile in = new RandomAccessFile(inputfile, "r");
		setMetadata(fileMetadata(in));
		setImageData(fileImageData(in, getMetadata()));
		setPixelType(FormatTools.UINT32);
	}
	@Override
	public Integer getPixelType() {
		final Integer result;
		switch(getElementType()) {
		case UNSIGNED_1_BIT:
			result = FormatTools.BIT;
			break;
		case UNSIGNED_8_BIT:
			result = FormatTools.UINT8;
			break;
		case SIGNED_8_BIT:
			result = FormatTools.INT8;
			break;
		case UNSIGNED_16_BIT:
			result = FormatTools.UINT16;
			break;
		case SIGNED_16_BIT:
			result = FormatTools.INT16;
			break;
		case UNSIGNED_32_BIT:
			result = FormatTools.UINT32;
			break;
		case SIGNED_32_BIT:
			result = FormatTools.INT32;
			break;
		case SIGNED_32_BIT_REAL_IEEE:
			result = FormatTools.FLOAT;
			break;
		case SIGNED_64_BIT_REAL_IEEE:
			result = FormatTools.DOUBLE;
			break;
		default:
			throw new RuntimeException(String.format("Unsupported element type %a",getElementType()));
		}
		return result;
	}
	

	ElementType getElementType() {
		String element_type = getMetadata().get(CBFTypes.ELEMENT_TYPE_KEY);
		System.out.println(element_type);
		Optional<ElementType> optional = CBFTypes.determineElement(element_type.replace('"',' ').trim());
		return optional.get();
	}

	private Map<String, String> fileMetadata(RandomAccessFile in) throws Exception {
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
					metadata.put(keyvalue[0], keyvalue[1].substring(0, keyvalue[1].length()).trim());
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
		imageData.data = ByteOffset.decompress(imageData.data,getElementType());
		System.out.println(imageSize);
		return imageData;
	}
	

	public static void main(String[] args) throws Exception {
		String inputfile = "/home/mwm1/Work/biogrid/175/hse85_3_15_1_0796.cbf";
		String outputfile = "/home/mwm1/Work/biogrid/175/hse85_3_15_1_0796.ome";
		new CBF(inputfile).write(outputfile);
		;
	}
}
*/