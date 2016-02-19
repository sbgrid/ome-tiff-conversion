package org.sbgrid.data.cbf;
//http://www.ccp4.ac.uk/dist/checkout/cctbx-phaser-dials-2015-12-22/cbflib/m4/fcb_read_xds_i2.m4

import java.util.Optional;

import ome.xml.model.enums.PixelType;

public class CBFTypes {
	enum Compression {
		CBF_PACKED, CBF_CANONICAL, CBF_BYTE_OFFSET, CBF_BACKGROUND_OFFSET_DELTA
	}

	static public Optional<Compression> determineCompression(String value) {
		final Optional<Compression> result;
		switch (value) {
		case "X-CBF_PACKED":
			result = Optional.of(Compression.CBF_PACKED);
			break;
		case "X-CBF_CANONICAL":
			result = Optional.of(Compression.CBF_CANONICAL);
			break;
		case "X-CBF_BYTE_OFFSET":
			result = Optional.of(Compression.CBF_BYTE_OFFSET);
			break;
		case "X-CBF_BACKGROUND_OFFSET_DELTA":
			result = Optional.of(Compression.CBF_BACKGROUND_OFFSET_DELTA);
			break;
		default:
			result = Optional.empty();
			break;
		}
		return result;
	}

	static public final String ELEMENT_TYPE_KEY = "X-Binary-Element-Type";
	static public final String COMPRESSION_SCHEME_KEY = "COMPRESSION_SCHEME";

	static public Optional<PixelType> determineElement(String value) {
		final Optional<PixelType> result;
		switch (value) {
		case "unsigned 1-bit integer":
			result = Optional.of(PixelType.BIT);
			break;
		case "unsigned 8-bit integer":
			result = Optional.of(PixelType.UINT8);
			break;
		case "signed 8-bit integer":
			result = Optional.of(PixelType.INT8);
			break;
		case "unsigned 16-bit integer":
			result = Optional.of(PixelType.UINT16);
			break;
		case "signed 16-bit integer":
			result = Optional.of(PixelType.INT16);
			break;
		case "unsigned 32-bit integer":
			result = Optional.of(PixelType.UINT32);
			break;
		case "signed 32-bit integer":
			result = Optional.of(PixelType.INT32);
			break;
		case "signed 32-bit real IEEE":
			result = Optional.of(PixelType.FLOAT);
			break;
		case "signed 64-bit real IEEE":
			result = Optional.of(PixelType.DOUBLE);
			break;
		case "signed 32-bit complex IEEE":
			result = Optional.of(PixelType.COMPLEX);
			break;
		default:
			result = Optional.empty();
			break;
		}
		return result;
	}
	
	static public final String ELEMENT_BYTE_ORDER_KEY = "X-Binary-Element-Byte-Order";
	static public Optional<Boolean> isLittleEndian(String value) {
		final Optional<Boolean> result;
		switch (value) {
		case "LITTLE_ENDIAN":
			result = Optional.of(true);
			break;
		case "BIG_ENDIAN":
			result = Optional.of(false);
			break;
		default:
			result = Optional.empty();
		}
		return result;
	};
}
