package com.gomyck.fastdfs.starter.util.velocity;

import com.gomyck.util.ObjectJudge;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author gomyck
 * @version 1.0.0
 * @since 2020/9/19
 */
public class CkStringResourceLoader extends ResourceLoader {

    Logger log = LoggerFactory.getLogger(CkStringResourceLoader.class);

    @Override
    public void init(ExtendedProperties configuration) {
        log.info("CkStringResourceLoader initialized ...");
    }

    @Override
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        InputStream result;
        if (ObjectJudge.isNull(source)) {
            throw new ResourceNotFoundException("template source can not be null !");
        }
        result = new ByteArrayInputStream(source.getBytes());
        return result;
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }
}
