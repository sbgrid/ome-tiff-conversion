package org.sbgrid.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.sbgrid.data.Configuration;
import org.sbgrid.data.Configuration.Field;
import org.sbgrid.data.adsc.ADSC;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import loci.common.services.ServiceFactory;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;

public class ConfigurationTest {
	static public void test1() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setElements(Arrays.asList(new Field("property", "property1", "property2")));
		configuration.setAttributes(Arrays.asList(new Field("metadata", "m1", "m2"), new Field("PixelsSizeX", "SIZE1"),
				new Field("PixelsSizeY", "SIZE2")));
		JAXBContext jc = JAXBContext.newInstance(Configuration.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(configuration, out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Configuration obj = (Configuration) unmarshaller.unmarshal(in);
		System.out.print(obj.getElements().size());
		System.out.print(obj.getAttributes().size());
		marshaller.marshal(obj, System.out);
	}

	static public void test2() throws Exception {
		InputStream is = Configuration.class.getResourceAsStream("/default.xml");
		Configuration configuration = Configuration.read(is);
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		OMEXMLMetadata metadata = service.createOMEXMLMetadata();
		metadata.createRoot();
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("SIZE1", "10");
		attributes.put("SIZE2", "10");
		configuration.populateMetadata(metadata, attributes);
	}

    static public void test3() throws Exception {
    	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    	context.reset();
		//new ADSC("/home/mwm1/Work/sbgrid/17").write("/tmp/1.tiff");
        //new ADSC("/home/mwm1/Work/sbgrid/1/p3_6_1_015.img").write("/tmp/1.tiff");
        new ADSC(ADSC.configuration(),"/home/mwm1/Work/sbgrid/17/ucsd6_16_1_094.img").write("/tmp/1.ome.tiff");

    }

	static public void main(String args[]) throws Exception {
		test3();
	}
}
