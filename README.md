Java-GIS-Shapefile-Parser-And-Processor
=======================================

Java Desktop application for Windows which allows for the parsing and processing (displaying, coordinate system transformation, exporting to KML, CSV, SQLite) of Shapefiles.

##Description
Supports parsing of shapefiles, coordinate system transformation, and exporting to KML / SQLite / CSV. 

##Dependencies
- GeoTools 9.4 (http://www.geotools.org/)
- Eclipse Standard Widget Toolkit (http://www.eclipse.org/swt/)
- JAXB (https://jaxb.java.net/)
- Micromata Java API for KML (http://labs.micromata.de/projects/jak.html)
- JOOQ SQL library (http://www.jooq.org/)


##Target Platforms
Project is currently using the SWT library for Windows by default.

##Notes
- Exporting to KML is not currently corrected for Google Sphere offset. I will not be adding it until I can validate the external accuracy of the transformation.
- Please be aware that transformations are done through the GeoTools library, so there is a chance that they may not meet the accuracy requirements of certain projects. If you are going for high accuracy, please consult an expert in Geodesy (http://en.wikipedia.org/wiki/Geodesy) or Cartography (http://en.wikipedia.org/wiki/Cartography) first.
