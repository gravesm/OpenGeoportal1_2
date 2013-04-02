package org.OpenGeoPortal.Download.Methods;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import org.OpenGeoPortal.Download.Types.BoundingBox;
import org.OpenGeoPortal.Download.Types.LayerRequest;
import org.OpenGeoPortal.Layer.GeometryType;
import org.springframework.scheduling.annotation.Async;

public class KmlMITDownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "GET";
	
	@Override
	public Boolean includesMetadata() {
		return INCLUDES_METADATA;
	}

	@Override
	public String getMethod(){
		return METHOD;
	}

	@Override
	public Set<String> getExpectedContentType(){
		Set<String> expectedContentType = new HashSet<String>();
		expectedContentType.add("application/vnd.google-earth.kmz+xml");
		expectedContentType.add("application/vnd.google-earth.kmz");
		return expectedContentType;
	}

	@Override
	public String createDownloadRequest() throws Exception {
		//--generate POST message
		//info needed: geometry column, bbox coords, epsg code, workspace & layername
	 	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer
	 	//in order to return the file in original projection to the user (will also need to transform the bbox)
		BoundingBox bounds = this.getClipBounds();
		String layerName = this.currentLayer.getLayerNameNS();
		/* we're going to use the kml reflector
		 * http://localhost:8080/geoserver/wms/kml?layers=topp:states

		kmscore=<value> 0 = force raster, 100 = force vector
		kmattr=[true|false] : whether or not kml has clickable attributes
		The format_options is a container for parameters that are format specific. The options in it are expressed as:
		 */
		String getFeatureRequest = "BBOX=" + bounds.toString() + "&LAYERS=" + layerName + "&format_options=kmattr:true;";
		if (GeometryType.isVector(GeometryType.parseGeometryType(currentLayer.getLayerInfo().getDataType()))){
			getFeatureRequest += "kmscore:100;";
		} else {
			getFeatureRequest += "kmscore:0;";
		}
		getFeatureRequest += "&format=application/vnd.google-earth.kmz+xml";
        
    	return getFeatureRequest;
	}

	@Override
	public String getUrl() {
		String url = this.currentLayer.getWmsUrl();
		url += "/kml";
		return url;
	}
        
        @Override
        @Async
        public Future<File> download(LayerRequest currentLayer) throws Exception {
            Future<File> outputFile = super.download(currentLayer);
            return outputFile;
        }
}
