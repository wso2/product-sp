/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.stream.processor.tooling.service.workspace.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.stream.processor.tooling.service.workspace.Constants;
import org.wso2.stream.processor.tooling.service.workspace.rest.FileServer;
import org.wso2.stream.processor.tooling.service.workspace.rest.SourceEditorService;

import java.nio.file.Paths;

/**
 * Workspace Service Entry point.
 *
 */
public class WorkspaceServiceRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceServiceRunner.class);

    public static void main(String[] args) {

        String streamProcessorHome = System.getProperty(Constants.SYS_STREAM_PROCESSOR_HOME);
        if (streamProcessorHome == null) {
            streamProcessorHome = System.getenv(Constants.SYS_STREAM_PROCESSOR_HOME);
        }
        if (streamProcessorHome == null) {
            logger.error("SP_HOME is not set. Please set sp.home system variable.");
            return;
        }

        int port = Integer.getInteger(Constants.SYS_FILE_WEB_PORT, Constants.DEFAULT_FILE_WEB_PORT);
        String contextRoot = Paths.get(streamProcessorHome, Constants.FILE_CONTEXT_RESOURCE, Constants
                .FILE_CONTEXT_RESOURCE_EDITOR, Constants.FILE_CONTEXT_RESOURCE_EDITOR_WEB)
                .toString();


        FileServer fileServer = new FileServer();

        fileServer.setContextRoot(contextRoot);
        SourceEditorService sourceEditorService = new SourceEditorService();
        new MicroservicesRunner(port)
                .deploy(fileServer, sourceEditorService)
                .start();

        logger.info("Stream Processor Editor URL: http://localhost:" + port);
    }
}
