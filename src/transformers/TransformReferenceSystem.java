package transformers;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import containers.ergo.geometry.ErgoCoordinateSet;

public class TransformReferenceSystem {

	/**
	 * 
	 * @param coordinateSet
	 * @param sourceCRS
	 * @param targetCRS
	 * @param leniency
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	public static ErgoCoordinateSet TransformCoordinates(Coordinate coordinateSet, String sourceCRS, String targetCRS, boolean leniency) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {

		boolean mLeniency = leniency;

		CoordinateReferenceSystem mSourceCRS = CRS.decode(sourceCRS);
		CoordinateReferenceSystem mTargetCRS = CRS.decode(targetCRS);

		MathTransform customTransformation = CRS.findMathTransform(mSourceCRS, mTargetCRS, mLeniency);

		GeometryFactory geomFactory = new GeometryFactory();

		Point sourcePoint = geomFactory.createPoint(coordinateSet);

		Point targetPoint = (Point) JTS.transform(sourcePoint,
				customTransformation);

		Double[] targetCoords = new Double[3];

		targetCoords[0] = targetPoint.getCoordinate().x;
		targetCoords[1] = targetPoint.getCoordinate().y;
		targetCoords[2] = targetPoint.getCoordinate().z;

		ErgoCoordinateSet transformedSet = new ErgoCoordinateSet(targetCoords[0], targetCoords[1], targetCoords[2], mTargetCRS.toString());

		return transformedSet;
	}


}
