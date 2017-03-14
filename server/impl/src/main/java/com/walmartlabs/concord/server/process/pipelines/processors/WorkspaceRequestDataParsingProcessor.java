package com.walmartlabs.concord.server.process.pipelines.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmartlabs.concord.common.Constants;
import com.walmartlabs.concord.server.process.Payload;
import com.walmartlabs.concord.server.process.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Parses workspace's request JSON file and stores it as header values.
 */
@Named
public class WorkspaceRequestDataParsingProcessor implements PayloadProcessor {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceRequestDataParsingProcessor.class);

    @SuppressWarnings("unchecked")
    @Override
    public Payload process(Payload payload) {
        Path workspace = payload.getHeader(Payload.WORKSPACE_DIR);

        Path src = workspace.resolve(Constants.REQUEST_DATA_FILE_NAME);
        if (!Files.exists(src)) {
            return payload;
        }

        Map<String, Object> data;
        try (InputStream in = Files.newInputStream(src)) {
            ObjectMapper om = new ObjectMapper();
            data = om.readValue(in, Map.class);
        } catch (IOException e) {
            log.error("process ['{}'] -> error while parsing a request data file", payload);
            throw new ProcessException("Invalid request data format", e, Status.BAD_REQUEST);
        }

        return payload.putHeader(Payload.REQUEST_DATA_MAP, data);
    }
}
