package org.sbgrid.data.cbf;

import java.util.Optional;

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

	enum ElementType {
		UNSIGNED_1_BIT, UNSIGNED_2_BIT, UNSIGNED_8_BIT, SIGNED_8_BIT, UNSIGNED_16_BIT, SIGNED_16_BIT, UNSIGNED_32_BIT, SIGNED_32_BIT, SIGNED_32_BIT_REAL_IEEE, SIGNED_32_BITREAL_COMPLEX_IEEE, SIGNED_64_BIT_REAL_IEEE
	}
	static public final String ELEMENT_TYPE_KEY = "X-Binary-Element-Type";
	static public final String COMPRESSION_SCHEME_KEY = "COMPRESSION_SCHEME";

	static public Optional<ElementType> determineElement(String value) {
		final Optional<ElementType> result;
		switch (value) {
		case "unsigned 1-bit integer":
			result = Optional.of(ElementType.UNSIGNED_1_BIT);
			break;
		case "unsigned 8-bit integer":
			result = Optional.of(ElementType.UNSIGNED_8_BIT);
			break;
		case "signed 8-bit integer":
			result = Optional.of(ElementType.SIGNED_8_BIT);
			break;
		case "unsigned 16-bit integer":
			result = Optional.of(ElementType.UNSIGNED_16_BIT);
			break;
		case "signed 16-bit integer":
			result = Optional.of(ElementType.SIGNED_16_BIT);
			break;
		case "unsigned 32-bit integer":
			result = Optional.of(ElementType.UNSIGNED_32_BIT);
			break;
		case "signed 32-bit integer":
			result = Optional.of(ElementType.SIGNED_32_BIT);
			break;
		case "signed 32-bit real IEEE":
			result = Optional.of(ElementType.SIGNED_32_BIT_REAL_IEEE);
			break;
		case "signed 64-bit real IEEE":
			result = Optional.of(ElementType.SIGNED_64_BIT_REAL_IEEE);
			break;
		case "signed 32-bit complex IEEE":
			result = Optional.of(ElementType.SIGNED_32_BITREAL_COMPLEX_IEEE);
			break;
		default:
			result = Optional.empty();
			break;
		}
		return result;
	}

}
