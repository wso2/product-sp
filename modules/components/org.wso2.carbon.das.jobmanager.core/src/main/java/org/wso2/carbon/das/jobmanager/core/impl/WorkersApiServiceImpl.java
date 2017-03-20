/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.jobmanager.core.impl;

import org.wso2.carbon.das.jobmanager.core.NotFoundException;
import org.wso2.carbon.das.jobmanager.core.WorkersApiService;
import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanDTO;
import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanListDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerListDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Worker Service Implementation.
 */
public class WorkersApiServiceImpl extends WorkersApiService {

    private static final String RESOURCE_PATH_WORKERS = "/workers";
    private static final String RESOURCE_PATH_EXECUTION_PLANS = "/executionplans";
    private List<WorkerDTO> workerDTOList = new CopyOnWriteArrayList<>();
    private Map<String, List<String>> executionPlanNameToWorkerId = new ConcurrentHashMap<>();

    @Override
    public Response workersGet(String accept) throws NotFoundException {
        WorkerListDTO workerListDTO = new WorkerListDTO();
        workerDTOList.forEach(workerListDTO::addListItem);
        return Response.ok().entity(workerListDTO).build();
    }

    @Override
    public Response workersIdDelete(String id) throws NotFoundException {
        WorkerDTO workerDTO = getWorkerDTO(id);
        workerDTOList.remove(workerDTO);
        return workerDTO == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok().entity(workerDTO)
                .build();
    }

    @Override
    public Response workersIdExecutionplansGet(String id, String accept) throws NotFoundException {
        WorkerDTO workerDTO = getWorkerDTO(id);
        if (workerDTO == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Invalid Worker Id: " + id).build();
        }
        ExecutionPlanListDTO executionPlanListDTO = new ExecutionPlanListDTO();
        executionPlanNameToWorkerId.forEach((k, v) -> {
            if (v.contains(id)) {
                executionPlanListDTO.addListItem(new ExecutionPlanDTO().name(k));
            }
        });
        return Response.ok().entity(executionPlanListDTO).build();
    }

    @Override
    public Response workersIdExecutionplansPost(String id, ExecutionPlanDTO body, String contentType) throws
            NotFoundException {
        WorkerDTO workerDTO = getWorkerDTO(id);
        if (workerDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Worker Id: " + id).build();
        }
        String executionPlanName = body.getName();
        List<String> workerIds = executionPlanNameToWorkerId.get(executionPlanName);
        if (workerIds == null) {
            workerIds = new ArrayList<>();
        }
        if (workerIds.contains(id)) {
            return Response.status(Response.Status.CONFLICT).entity("Execution plan is already added.").build();
        }
        workerIds.add(id);
        executionPlanNameToWorkerId.put(executionPlanName, workerIds);
        try {
            URI location = new URI(RESOURCE_PATH_WORKERS + "/" + id + RESOURCE_PATH_EXECUTION_PLANS +
                    "/" + executionPlanName);
            return Response.created(location).header(HttpHeaders.LOCATION, location).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.CREATED).build();
        }
    }

    @Override
    public Response workersIdGet(String id, String accept) throws NotFoundException {
        WorkerDTO workerDTO = getWorkerDTO(id);
        if (workerDTO == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Invalid Worker Id: " + id).build();
        }
        return Response.ok().entity(workerDTO).build();
    }

    @Override
    public Response workersPost(WorkerDTO worker, String contentType) throws NotFoundException {
        WorkerDTO workerDTO = getWorkerDTO(worker);
        if (workerDTO == null) {
            // if it does not exist
            worker.setId(UUID.randomUUID().toString());
            worker.setStatus("Active");
            workerDTOList.add(worker);
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
        try {
            URI location = new URI(RESOURCE_PATH_WORKERS + "/" + worker.getId());
            return Response.created(location).header(HttpHeaders.LOCATION, location).entity(worker).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.CREATED).build();
        }
    }

    private WorkerDTO getWorkerDTO(String id) {
        Optional<WorkerDTO> workerDTO = workerDTOList.stream().filter(p -> p.getId().equals(id)).findFirst();
        return workerDTO.isPresent() ? workerDTO.get() : null;
    }

    private WorkerDTO getWorkerDTO(WorkerDTO newWorker) {
        Optional<WorkerDTO> workerDTO = workerDTOList.stream().filter(p -> p.equals(newWorker)).findFirst();
        return workerDTO.isPresent() ? workerDTO.get() : null;
    }
}
