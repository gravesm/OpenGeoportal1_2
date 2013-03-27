package org.OpenGeoPortal.Download.Methods;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.OpenGeoPortal.Download.Types.BoundingBox;
import org.OpenGeoPortal.Download.Types.LayerRequest;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WfsDownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {	
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "POST";
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getMethod(){
		return METHOD;
	}
	
	@Override
	public Set<String> getExpectedContentType(){
		Set<String> expectedContentType = new HashSet<String>();
		expectedContentType.add("application/zip");
		return expectedContentType;
	}
	
	public String createDownloadRequest() throws Exception {
		//--generate POST message
		//info needed: geometry column, bbox coords, epsg code, workspace & layername
	 	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer
	 	//in order to return the file in original projection to the user (will also need to transform the bbox)
		String layerName = this.currentLayer.getLayerNameNS();
		SolrRecord layerInfo = this.currentLayer.getLayerInfo();
		BoundingBox nativeBounds = new BoundingBox(layerInfo.getMinX(), layerInfo.getMinY(), layerInfo.getMaxX(), layerInfo.getMaxY());
		BoundingBox bounds = nativeBounds.getIntersection(this.currentLayer.getRequestedBounds());

		String workSpace = layerInfo.getWorkspaceName();
		Map<String, String> describeLayerInfo = getWfsDescribeLayerInfo();
		String geometryColumn = describeLayerInfo.get("geometryColumn");
		String nameSpace = describeLayerInfo.get("nameSpace");
		int epsgCode = 4326;//we are filtering the bounds based on WGS84
		String bboxFilter = "";
		if (!nativeBounds.isEquivalent(bounds)){

  			bboxFilter += "<ogc:Filter>"
      		+		"<ogc:BBOX>"
        	+			"<ogc:PropertyName>" + geometryColumn + "</ogc:PropertyName>"
        	+			bounds.generateGMLBox(epsgCode)
        	+		"</ogc:BBOX>"
      		+	"</ogc:Filter>";
		}

		String getFeatureRequest = "<wfs:GetFeature service=\"WFS\" version=\"1.0.0\""
			+ " outputFormat=\"shape-zip\""
			+ " xmlns:" + workSpace + "=\"" + nameSpace + "\""
  			+ " xmlns:wfs=\"http://www.opengis.net/wfs\""
  			+ " xmlns:ogc=\"http://www.opengis.net/ogc\""
  			+ " xmlns:gml=\"http://www.opengis.net/gml\""
  			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
  			+ " xsi:schemaLocation=\"http://www.opengis.net/wfs"
            + " http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd\">"
  			+ "<wfs:Query typeName=\"" + layerName + "\">"
  			+ bboxFilter
  			+ "</wfs:Query>"
			+ "</wfs:GetFeature>";

    	return getFeatureRequest;
	}
	
	@Override
	public String getUrl(){
		return this.currentLayer.getWfsUrl();
	};
	
	 Map<String, String> getWfsDescribeLayerInfo() throws Exception {
		// TODO should be xml doc fragment?
		String layerName = this.currentLayer.getLayerNameNS();
	 	String describeFeatureRequest = "<DescribeFeatureType"
	            + " version=\"1.0.0\""
	            + " service=\"WFS\""
	            + " xmlns=\"http://www.opengis.net/wfs\""
	            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
	            + " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/wfs.xsd\">"
	            + 	"<TypeName>" + layerName + "</TypeName>"
	            + "</DescribeFeatureType>";

		InputStream inputStream = this.httpRequester.sendRequest(this.getUrl(), describeFeatureRequest, "POST");
		String contentType = this.httpRequester.getContentType();

		if (!contentType.contains("xml")){
			throw new Exception("Expecting an XML response; instead, got content type '" + contentType + "'");
		}
		//parse the returned XML and return needed info as a map
		// Create a factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Use document builder factory
		DocumentBuilder builder = factory.newDocumentBuilder();
		//Parse the document
		Document document = builder.parse(inputStream);
		//initialize return variable
		Map<String, String> describeLayerInfo = new HashMap<String, String>();

		//get the namespace info
		Node schemaNode = document.getFirstChild();
		if (schemaNode.getNodeName().equals("ServiceExceptionReport")){
			this.handleServiceException(schemaNode);
		}
		try {
			NamedNodeMap schemaAttributes = schemaNode.getAttributes();
			describeLayerInfo.put("nameSpace", schemaAttributes.getNamedItem("targetNamespace").getNodeValue());

			//we can get the geometry column name from here
			NodeList elementNodes = document.getElementsByTagName("xsd:element");
			for (int i = 0; i < elementNodes.getLength(); i++){
				Node currentNode = elementNodes.item(i);
				NamedNodeMap currentAttributeMap = currentNode.getAttributes();
				String attributeValue = null;
				for (int j = 0; j < currentAttributeMap.getLength(); j++){
					Node currentAttribute = currentAttributeMap.item(j);
					String currentAttributeName = currentAttribute.getNodeName();
					if (currentAttributeName.equals("name")){
						attributeValue = currentAttribute.getNodeValue();
					} else if (currentAttributeName.equals("type")){
						if (currentAttribute.getNodeValue().startsWith("gml:")){
							describeLayerInfo.put("geometryColumn", attributeValue);
							break;
						}
					}
				}
			}
			
		} catch (Exception e){
			throw new Exception("Error getting layer info from DescribeFeatureType: "+ e.getMessage());
		}
		
		return describeLayerInfo;
	 }

	 void handleServiceException(Node schemaNode) throws Exception{
			String errorMessage = "";
			for (int i = 0; i < schemaNode.getChildNodes().getLength(); i++){
				String nodeName = schemaNode.getChildNodes().item(i).getNodeName();
				if (nodeName.equals("ServiceException")){
					errorMessage += schemaNode.getChildNodes().item(i).getTextContent().trim();
				}
			}
			throw new Exception(errorMessage);
	 }

	@Override
	public Boolean includesMetadata() {
		return INCLUDES_METADATA;
	}

    @Override
    @Async
    public Future<File> download(LayerRequest currentLayer) throws Exception {
        Future<File> outputFile = super.download(currentLayer);
        return outputFile;
    }

}
