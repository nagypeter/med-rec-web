/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.medrec.patient;

import com.oracle.medrec.model.Patient;
import com.oracle.medrec.service.DuplicateSsnException;
import com.oracle.medrec.service.DuplicateUsernameException;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;

import javax.json.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * The patients are created, upated, deleted, and queired.
 *
 */
@Path("/patients")
@RequestScoped
public class PatientResource {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The patient provider.
     */
    private final PatientProvider patientProvider;

    public PatientResource() {
        this.patientProvider = new PatientProvider();
    }

    /**
     * Return selected patients.
     *
     * @return {@link JsonObject}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Patient> getPatients(@Context UriInfo uriInfo) {

        String lastName = uriInfo.getQueryParameters().getFirst("lastname");
        String ssn = uriInfo.getQueryParameters().getFirst("ssn");
        String id = uriInfo.getQueryParameters().getFirst("id");
        logger.finest("lastName: " + lastName);
        logger.finest("ssn: " + ssn);
        logger.finest("id: " + id);
        return patientProvider.fuzzyFindApprovedPatientsByLastNameAndSsn(lastName, ssn);
    }

    /**
     * Return a patient.
     *
     * @return {@link JsonObject}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Patient getPatientById(@PathParam("id") String patientId) {
        logger.finest("id: " + patientId);
        return patientProvider.getPatient(Long.valueOf(patientId));
    }

    /**
     * Return a patient.
     *
     * @return {@link JsonObject}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @GET
    @Path("/approve/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approvePatient(@PathParam("id") String patientId) {
        logger.finest("id: " + patientId);
        try {
            patientProvider.approvePatient(Long.valueOf(patientId));
        } catch (Exception e) {
            return Response.serverError()
                           .type(MediaType.TEXT_PLAIN)
                           .entity(e.getMessage())
                           .build();
        }
        return Response.accepted().build();
    }

    /**
     * Create the return Response.accepted().build();patient.
     *
     * @param patient the patient to create
     * @return {@link Response}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPatient(Patient patient) {
        try {
            patientProvider.createPatient(patient);
        } catch (DuplicateSsnException | DuplicateUsernameException e) {
            return Response.serverError()
                           .type(MediaType.TEXT_PLAIN)
                           .entity(e.getMessage())
                           .build();
        }
        return Response.accepted().build();
    }

    @SuppressWarnings("checkstyle:designforextension")
    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticatePatient(UserCredentials credentials) {

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        logger.finest("username: " + username);
        logger.finest("password: " + password);
        if (patientProvider.authenticatePatient(username, password)) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }


}