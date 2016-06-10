package org.sbgrid.data;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import loci.formats.meta.IMetadata;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;

/**
 * A representation of an XML configuration object.
 * 
 * This allows the user to customize the metadata fields and how they are stored
 * in an ome tiff file.
 * 
 * @author mwm1
 *
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class Configuration {
	/**
	 * A meta data field.
	 * 
	 * @author mwm1
	 *
	 */
	static public class Replacement {
		@XmlAttribute(name = "pattern")
		String pattern;
		@XmlAttribute(name = "replacement")
		String replacement;
		
		public Replacement(){}
		public Replacement(String pattern,String replacement){
			this.pattern = pattern;
			this.replacement = replacement;
		}
	}
	static public class Source {
		@XmlAttribute(name = "field")
		String field;
		@XmlElement(name = "regexp")
		List<Replacement> regexp;
		
		public Source(){}
		public Source(String field,Replacement ... regexp){
			this.field = field;
			this.regexp = Arrays.asList(regexp); 
		}
	}
	
	static public class Field {
		/**
		 * Where to get the data to place in the field. The first value that is
		 * found is placed in the field. If none is found an exception is thrown
		 * if there is no default.
		 */
		@XmlElement(name = "source")
		List<Source> fields = null;
		/**
		 * The name used to refer to the field.
		 */
		@XmlAttribute(name = "name")
		String name = null;

		@XmlAttribute(name = "value")
		String value = null;

		
		@XmlAttribute(name = "required")
		Boolean required = false;
		/**
		 * Constructor
		 * 
		 * @param name
		 *            of the field
		 * @param fields
		 *            providing the value.
		 */
		public Field(String name, String... fields) {
			super();
			this.name = name;
			this.fields = new ArrayList<Configuration.Source>();
			for(String field : fields){
				this.fields.add(new Source(field));
			}
		}

		public Field() {
		}

		@Override
		public String toString() {
			return String.format("{ Field : { fields : %s , name : %s , value : %s } }"
					            , fields, name, value);
		}

	}

	/**
	 * These are properties used in the creation of the TIFF file. Properties
	 * such as height width of the TIFF file are used to determine how to
	 * convert the tiff file.
	 * 
	 */

	@XmlElement(name = "attribute")
	List<Field> attributes = new ArrayList<Field>();

	List<Field> getAttributes() {
		return attributes;
	}

	void setAttributes(List<Field> p) {
		this.attributes = p;
	}

	static Map<String, Field> index(List<Field> fields) {
		Map<String, Field> result = new HashMap<String, Field>();
		for (Field f : fields) {
			result.put(f.name, f);
		}
		return result;

	}

	/**
	 * Metadata property. This properties will be mapped to the OME meta data
	 * property.
	 */
	@XmlElement(name = "element")
	List<Field> elements = new ArrayList<Field>();

	List<Field> getElements() {
		return elements;
	}

	void setElements(List<Field> e) {
		elements = e;
	}

	/**
	 * Given a set of attributes and name try to resolve the name. First check
	 * if
	 * 
	 * @lookup where to lookup
	 * @param name
	 *            name to resolve
	 * @param attributes
	 * @return
	 * @throws Exception
	 */
	public List<String> resolve(List<Field> fields, String name, Map<String, List<String>> attributes) throws Exception {
		List<String> value = null;
		Map<String, Field> supportedFields = index(fields);
		if (supportedFields.containsKey(name)) {
			Field field = supportedFields.get(name);
			value = Arrays.asList(new String[]{field.value}) ;
			for (Source source : field.fields) {
				if (attributes.containsKey(source.field)) {
					value = attributes.get(source.field);
					break;
				}
			}
			if(field.required && value == null){
				throw new Exception(String.format("Missing required value '%s'", name));
			}
		} else {
			throw new Exception(String.format("Unsupported property '%s'", name));
		}
		return value;
	}

	public List<String> resolveElement(Field field, Map<String, List<String>> values) throws Exception {
		List<String> value = Arrays.asList(new String[]{field.value});
		if(field.fields != null)
		for (Source source : field.fields) {
			if (values.containsKey(source.field)) {
				value = values.get(source.field);
			}
		}
		if (value == null) {
			throw new Exception(String.format("Unsupported property '%s'", field.name));
		}
		return value;
	}

	public List<String> resolveProperties(String name, Map<String, List<String>> values) throws Exception {
		return resolve(attributes, name, values);
	}
	public String resolveProperty(String name, Map<String, List<String>> values) throws Exception {
		List<String> properties = resolveProperties(name,values);
		if(properties.size() == 1){
			return properties.get(0);
		}
		if(properties.size() > 1){
			throw new Exception(String.format("The field '%s' contains multiple values %s ", name , String.join(" , ",properties)));
		} else {
			throw new Exception("missing property");
		}
	}

	private String capitalize(final String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	public Object fromString(String value, Class<?> clazz) {
		Object result = null;
		if (Boolean.class.equals(clazz)) {
			result = Boolean.parseBoolean(value);
		} else if (Double.class.equals(clazz)) {
			result = Double.parseDouble(value);
		} else if (PositiveInteger.class.equals(clazz)) {
			result = new PositiveInteger(Integer.parseInt(value));
		} else if (PercentFraction.class.equals(clazz)) {
			result = new PercentFraction(Float.parseFloat(value));
		} else if (NonNegativeInteger.class.equals(clazz)) {
			result = new NonNegativeInteger(Integer.parseInt(value));
		} else if (String.class.equals(clazz)) {
			result = value;
		} else if (Timestamp.class.equals(clazz)) {
			result = Timestamp.valueOf(value);
		}
		return result;
	}

	public Boolean updateMetadata(IMetadata metadata, String methodName, String value) throws Exception {
		for (Method method : Arrays.asList(IMetadata.class.getMethods())) {
			if (methodName.equals(method.getName())) {
				Class<?>[] parameters = method.getParameterTypes();
				if (parameters.length > 0) {
					Object[] arguments = new Object[parameters.length];
					arguments[0] = fromString(value, parameters[0]);
					if (arguments[0] != null) {
						for (int index = 1; index < parameters.length && arguments != null; index++) {
							if (Integer.class.isAssignableFrom(parameters[index])) {
								arguments = null;
							} else {
								arguments[index] = 0;
							}
						}
						if (arguments != null) {
							method.invoke(metadata, arguments);
							return true;
						}
					}
				}
			}

		}
		return false;
	}

	/**
	 * 
	 * @param metadata
	 * @param attributes
	 * @throws Exception
	 */
	public void populateMetadata(IMetadata metadata, Map<String, List<String>> attributes) throws Exception {
		for (Field f : getElements()) {
			String methodName = String.format("set%1$s", capitalize(f.name));
			List<String> values = resolveElement(f, attributes);
			if (values != null) {
				if(values.size() > 1){
					throw new Exception(String.format("The field '%s' contains multiple values %s ", f.name , String.join(" , ",values)));
				}
				String value = values.get(0);
				Boolean updated = updateMetadata(metadata, methodName, value);
				if (!updated) {
					throw new Exception(String.format("Could not update property '%s'", f.name));
				}

			}
		}
	}

	/**
	 * Covert a XML string into a configuration object.
	 * 
	 * @param xmlConfiguration
	 *            string containing the configuration.
	 * @return
	 */
	static public Configuration read(InputStream is) {
		try {
			JAXBContext jc = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Configuration configuration = (Configuration) unmarshaller.unmarshal(is);
			return configuration;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
