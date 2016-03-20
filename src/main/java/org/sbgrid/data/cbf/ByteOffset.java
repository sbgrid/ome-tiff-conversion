package org.sbgrid.data.cbf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;

public class ByteOffset {
	// signed 32-bit integer - int
	public static byte[] decompress(byte[] data,PixelType pixelType,Boolean pixelsBigEndian) throws Exception {
		final int element_size;
		switch (pixelType) {  
		case UINT8:
			element_size = 1;
			break;
		case INT8:
			element_size = 1;
			break;
		case UINT16:
			element_size = 2;
			break;
		case INT16:
			element_size = 2;
			break;
		case UINT32:
			element_size = 4;
			break;
		case INT32:
			element_size = 4;
			break;
		default:
			throw new Exception("unexected element type");
		}
		ByteBuffer buffer = ByteBuffer.allocate(element_size);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteArrayInputStream input = new ByteArrayInputStream(data);
		long value = 0;
		
		ByteOrder order = pixelsBigEndian?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN;
		byte[] buffer_8 = new byte[1];
		while (input.read(buffer_8) == 1) {
			byte delta_8 = buffer_8[0];
			if(delta_8 != Byte.MIN_VALUE) {
				delta_8 += (byte) value;
				value = delta_8;
				buffer.clear();
				buffer.put(delta_8);
				output.write(buffer.array());
			} else {
				byte[] buffer_16 = new byte[2];
				if(input.read(buffer_16) == 2) {
					ByteBuffer little_endian = ByteBuffer.wrap(buffer_16);
					little_endian.order(order);
					short delta_16 = little_endian.getShort();
					if(delta_16 != Short.MIN_VALUE) {
						delta_16 += (byte) value;
						value = delta_16;
						buffer.clear();
						buffer.putShort(delta_16);
						output.write(buffer.array());
					} else {
						byte[] buffer_32 = new byte[4];
						if(input.read(buffer_32) == 4) {
							ByteBuffer little_endian_32 = ByteBuffer.wrap(buffer_32);
							little_endian_32.order(order);
							int delta_32 = little_endian_32.getInt();
							if(delta_32 != Integer.MIN_VALUE) {
								delta_32 += (byte) value;
								value = delta_32;
								buffer.clear();
								buffer.putInt(delta_32);
								output.write(buffer.array());
							} else { 
								byte[] buffer_64 = new byte[8];
								ByteBuffer little_endian_64 = ByteBuffer.wrap(buffer_64);
								little_endian_64.order(order);
								int delta_64 = little_endian_64.getInt();
								delta_64 += (byte) value;
								value = delta_64;
								buffer.clear();
								buffer.putInt(delta_64);
								output.write(buffer.array());
							}
						} else { throw new Exception("32 under flow"); }
					}
				} else { throw new Exception("16 under flow"); }
			}	
		}
		System.out.println("Decompressed -"+(output.size()/4));
		return output.toByteArray();
	}
	
	Byte[] compress(Byte[] data) {
		return data;
	}
	
	byte[] cbfPacked(byte[] input, int img_size) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		byte[] buffer_8 = new byte[1];
		int size = 0;
		long base = 0;
		while (inputStream.read(buffer_8) == 1 && size < img_size) {
			byte delta = buffer_8[0];
			if (delta != Byte.MIN_VALUE) {
				delta += (byte) base;
				output.write(new byte[] { (byte) delta });
				size++;
				base = delta;
			} else {
				byte[] buffer_16 = new byte[2];
				if (inputStream.read(buffer_16) == 1) {
					ByteBuffer little_endian = ByteBuffer.wrap(buffer_16);
					little_endian.order(ByteOrder.LITTLE_ENDIAN);
					short delta_16 = little_endian.getShort();
					if (delta_16 != Short.MIN_VALUE) {
						delta_16 += (short) base;
						ByteBuffer outputShort = ByteBuffer.allocate(2);
						outputShort.putShort(delta_16);
						output.write(outputShort.array());
						size++;
						base = delta;
					} else {
						byte[] buffer_32 = new byte[4];
						if (inputStream.read(buffer_32) == 1) {
							ByteBuffer wrap_32 = ByteBuffer.wrap(buffer_32);
							wrap_32.order(ByteOrder.LITTLE_ENDIAN);
							int delta_32 = wrap_32.getInt();
							if (delta_32 != Integer.MIN_VALUE) {
								delta_32 += (int) base;
								ByteBuffer outputInt = ByteBuffer.allocate(4);
								outputInt.putInt(delta_32);
								output.write(outputInt.array());
								size++;
								base = delta;
							} else {
								byte[] buffer_64 = new byte[8];
								if (inputStream.read(buffer_64) == 1) {
									ByteBuffer wrap_64 = ByteBuffer.wrap(buffer_64);
									wrap_64.order(ByteOrder.LITTLE_ENDIAN);
									long delta_64 = wrap_64.getLong();
									delta_64 += (long) base;
									ByteBuffer outputLong = ByteBuffer.allocate(8);
									outputLong.putLong(delta_64);
									output.write(outputLong.array());
									size++;
									base = delta;
								}
							}
						} else {
							throw new Exception("Int buffer under read");
						}

					}
				} else {
					throw new Exception("Short buffer under read");
				}
			}
		}
		while (output.size() < img_size) {
			output.write(0);
		}
		return output.toByteArray();
	}


}
