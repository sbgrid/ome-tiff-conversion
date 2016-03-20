package org.sbgrid.data.adsc;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.management.RuntimeErrorException;

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
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;
/**
 *
 * @author major seitan
 * References
 * https://www.openmicroscopy.org/site/support/bio-formats5.1/_downloads/FileExport.java
 * http://dawb.eclipselabs.org.codespot.com/svn-history/r1274/trunk/uk. ac.diamond.scisoft.analysis/src/uk/ac/diamond/scisoft/analysis/io/ADSCImageLoader.java
 */
public class ADSC extends Format {

        public ADSC (String inputfile) throws Exception {
                RandomAccessFile raf = new RandomAccessFile(inputfile, "r");
                Map<String, String> attributes = getAttributes(raf);
                setMetadata(attributesToMetadata(attributes));
                ImageData imageData = fileImageData(raf,attributes);
                setImageData(imageData);
        }

        private ImageData fileImageData(RandomAccessFile raf,Map<String,String> attributes) throws Exception {
                ImageData imageData = new ImageData();
                imageData.height = getMetadata().getPixelsSizeX(0).getValue();
                imageData.width = getMetadata().getPixelsSizeY(0).getValue();
                imageData.data = new byte[(imageData.height * imageData.width) * 2];
                int pointer = Integer.parseInt(attributes.get("HEADER_BYTES").trim());
                raf.seek(pointer);
                raf.read(imageData.data);
                return imageData;
        }
        private IMetadata attributesToMetadata(Map<String, String> attributes){
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
              String type = attributes.get("TYPE");
              if(!"unsigned_short".equals(type)) {
                 throw new Exception(String.format("Unexpected type '%s'",type));
              }
              String byteOrder = attributes.get("BYTE_ORDER");
              switch (byteOrder) {
              case "little_endian" :
                  metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
                      break;
              case "big_endian" :
                  metadata.setPixelsBinDataBigEndian(Boolean.FALSE, 0, 0);
                      break;
              default:
                      throw new Exception(String.format("Unexpected endian '%s'",byteOrder));
              }
              metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
              metadata.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(FormatTools.UINT16)), 0);
              metadata.setPixelsSizeX(new PositiveInteger(Integer.parseInt(attributes.get("SIZE1"))), 0);
              metadata.setPixelsSizeY(new PositiveInteger(Integer.parseInt(attributes.get("SIZE2"))), 0);
              metadata.setPixelsSizeZ(new PositiveInteger(1), 0);
              metadata.setPixelsSizeC(new PositiveInteger(1), 0);
              metadata.setPixelsSizeT(new PositiveInteger(1), 0);
              if(attributes.containsKey("PIXEL_SIZE")){
                  metadata.setPixelsPhysicalSizeX(new Length(Float.parseFloat(attributes.get("PIXEL_SIZE")), UNITS.MILLI(UNITS.METRE)),0);
                  metadata.setPixelsPhysicalSizeY(new Length(Float.parseFloat(attributes.get("PIXEL_SIZE")), UNITS.MILLI(UNITS.METRE)),0);
              }
              metadata.setChannelID("Channel:0:0", 0, 0);
              metadata.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
              System.out.println(service.getOMEXML(metadata));
              return metadata;
            }
            catch (DependencyException e) {
                exception = e;
            }
            catch (ServiceException e) {
                exception = e;
            } catch (EnumerationException e) {
                exception = e;
                } catch (Exception e) {
                        exception = e;
                }

            System.err.println("Failed to populate OME-XML metadata object.");
            throw new RuntimeErrorException(new Error(exception));
          }

        private Map<String, String> getAttributes(RandomAccessFile in) throws Exception {
                Map<String, String> metadata    = new HashMap<>();
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
                                        metadata.put(keyvalue[0], keyvalue[1].substring(0, keyvalue[1].length() - 1));
                                } // dropping the extra headers for now
                                else {
                                        throw new Exception(String.format("There was a problem parsing the ADSC header information : %d",linenumber));
                                }
                        }
                } catch (IOException e) {
                        throw new Exception(String.format("There was a problem parsing the ADSC header information %d",linenumber), e);
                }
        }
        static public void main(String arg[]) throws Exception {
                new ADSC("/home/mwm1/Work/biogrid/1/p3_6_1_015.img").write("/home/mwm1/Work/biogrid/1/p3_6_1_015.tiff");;
        }
}
