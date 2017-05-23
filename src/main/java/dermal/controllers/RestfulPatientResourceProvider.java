package dermal.controllers;


import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;

/**
 * Created by blake on 23/05/2017.
 */
public class RestfulPatientResourceProvider implements IResourceProvider {
    @Override
    public Class<Patient> getResourceType(){
        return Patient.class;
    }

    @Read()
    public Patient getResourceById(@IdParam IdType patientId){
        Patient patient = new Patient();
        patient.addIdentifier();

        patient.getName().get(0).addGiven("PatientOne");
        patient.setGender();
        return patient;
    }


    @Search()
    public List<Patient> getPatient(@RequiredParam(name= Patient.SP_FAMILY) StringParam familyName)
}
