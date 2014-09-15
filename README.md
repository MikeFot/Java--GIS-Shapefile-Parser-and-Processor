Java-GIS-Shapefile-Parser-And-Processor
=======================================

Java Desktop application for Windows which allows for the parsing and processing (view, coordinate system transformation, export to CSV or KML) of Shapefiles.

##Description
- Supports parsing of shapefiles, coordinate system transformation, and exporting to KML / SQLite / CSV. 

##Dependencies
- GeoTools 9.4 (http://www.geotools.org/)
- Eclipse Standard Widget Toolkit (http://www.eclipse.org/swt/)
- JAXB (https://jaxb.java.net/)
- Micromata Java API for KML (http://labs.micromata.de/projects/jak.html)
- JOOQ SQL library (http://www.jooq.org/)


##Target Platforms
Project is currently using the SWT library for Windows by default.

##Disclaimer
Please be aware that transformations are done through the GeoTools library, so there is a chance that they may not meet the accuracy requirements of certain projects. If you are going for high accuracy, please consult an expert in Geodesy (http://en.wikipedia.org/wiki/Geodesy) or Cartography (http://en.wikipedia.org/wiki/Cartography) first.
