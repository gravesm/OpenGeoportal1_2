package org.OpenGeoPortal.Download.Methods;

import java.io.File;
import java.util.concurrent.Future;
import org.OpenGeoPortal.Download.Types.LayerRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;


public class ProxiedKmlMITDownloadMethod extends
        KmlMITDownloadMethod implements PerLayerDownloadMethod {
    
    private String proxyTo;
    
    @Override
    public String getUrl(){
            return this.proxyTo + "/kml";
    }
    
    public String getProxyTo() {
            return proxyTo;
    }
    
    public void setProxyTo(String proxyTo) {
            this.proxyTo = proxyTo;
    };

    @Async
    @Override
    @PreAuthorize("isAuthenticated()")
    public Future<File> download(LayerRequest currentLayer) throws Exception {
        Future<File> outputFile = super.download(currentLayer);
        return outputFile;
    }

}
