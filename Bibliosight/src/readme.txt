---------------------------------
- Basic Bibliosight Information -
---------------------------------

Java
====
Written with JDK 1.6

Access to Web of Science - Web Services Lite
============================================
See here: http://isiwebofknowledge.com/products_tools/products/related/webservices/
Once entitlement is granted, Thomson Reuters will give access to some documentation that provides more details
on setting up WS Lite and creating the relevant source files from WSDL.

Some useful info for running Bibliosight follows:

Download Apache CXF
===================
Found here: http://cxf.apache.org/
Version 2.2.x recommended but others are likely to also work.
Documentation on getting CXF set up can also be found at that location.

Creating the java files from the WSDL using Apache CXF
======================================================
See: http://cxf.apache.org/docs/wsdl-to-java.html

Apache CXF dependencies for Bibliosight
=======================================
Based on the list found here: http://cxf.apache.org/docs/a-simple-jax-ws-service.html
The whole list here may not actually be necessary at the current time.

commons-logging-1.1.1.jar
cxf-2.2.5.jar
geronimo-activation_1.1_spec-1.0.2.jar
geronimo-annotation_1.0_spec-1.1.1.jar
geronimo-javamail_1.4_spec-1.6.jar
geronimo-jaxws_2.1_spec-1.0.jar
geronimo-jms_1.1_spec-1.1.1.jar
geronimo-servlet_2.5_spec-1.2.jar
geronimo-stax-api_1.0_spec-1.0.1.jar
geronimo-ws-metadata_2.0_spec-1.1.2.jar
jaxb-api-2.1.jar
jaxb-impl-2.1.12.jar
jetty-6.1.21.jar
jetty-util-6.1.21.jar
neethi-2.0.4.jar
saaj-api-1.3.jar
saaj-impl-1.3.2.jar
wsdl4j-1.6.2.jar
wstx-asl-3.2.9.jar
xml-resolver-1.2.jar
XmlSchema-1.4.5.jar