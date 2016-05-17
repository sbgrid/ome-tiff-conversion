**Getting started**

OEM metadata is configured via an xml file.  An example of using the
metadata mapping is given by using the default included with the
source code.

```src/main/resources/ADSC.xml```

The following is an example which illustrating the usage of a
configuration file supplied by the _-m_ option.

```
java -jar target/ome-tiff-conversion-1.0.jar \
     -o /tmp/001.ome.tiff \
     -i /home/mwm1/Work/sbgrid/1/p3_6_1_001.img \
     -m src/main/resources/ADSC.xml \
     -f ADSC -l /tmp/p3_6_1_001.log
```

**Authoring Mappings**

The element tags set OME metadata properties.  The list of metadata
properties can be found in the OME documentation
[MetadataStore](https://downloads.openmicroscopy.org/bio-formats/5.0.5/api/ome/xml/meta/MetadataStore.html).


A valid property X will be referred to by setX.  For instance the property
PixelsId corresponds to :

```
setPixelsID(String id, int imageIndex)
```

The first argument is the value that is mapped.  The second
values is provided by the framework.  The following tag sets
the _PixelsID_ to zero :

```
<element name="PixelsID" value="0"/>
```

The following types are supported :

    Boolean
    Double
    PositiveInteger
    PercentFraction
    NonNegativeInteger
    String
    Timestamp

For more complicated mappings code has to be written to convert the values.
Attributes then supply the appropriate value the converter.  For instance _TYPE_
is an attribute which is used to determine the number of bits used in the image.

It is configured by the following tag:

```
<attribute name="TYPE" required="true">
    <source>Data_type</source>
    <source>TYPE</source>
</attribute>
```

In this attribute tag the value of type is first gotten from the Data_type
field then the Type field.

Additional attributes are

- _value_ : default value if non is supplied
- _required_ : will throw an exception if a value is not supplied
