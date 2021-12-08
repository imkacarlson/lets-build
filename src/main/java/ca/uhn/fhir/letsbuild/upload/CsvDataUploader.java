package ca.uhn.fhir.letsbuild.upload;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.common.base.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;

public class CsvDataUploader {

    private static final Logger ourLog = LoggerFactory.getLogger(CsvDataUploader.class);

    public static void main(String[] theArgs) throws Exception {

        // Open the CSV file for reading
        try (InputStream inputStream = new FileInputStream("src/main/resources/sample-data.csv")) {
            Reader reader = new InputStreamReader(inputStream, Charsets.UTF_8);

            CSVFormat format = CSVFormat.EXCEL
                    .withFirstRecordAsHeader()
                    .withDelimiter(',');
            CSVParser csvParser = format.parse(reader);

            // Loop throw each row in the CSV file
            for (CSVRecord nextRecord : csvParser.getRecords()) {

                // Sequence number - This could be used as an ID for generated resources
                String seqN = nextRecord.get("SEQN");

                // Add a log line - you can copy this to add more helpful logging
                ourLog.info("Processing row: {}", seqN);

                // Timestamp - This will be formatted in ISO8601 format
                String timestamp = nextRecord.get("TIMESTAMP");

                // Patient ID
                String patientId = nextRecord.get("PATIENT_ID");

                // Patient Family Name
                String patientFamilyName = nextRecord.get("PATIENT_FAMILYNAME");

                // Patient Given Name
                String patientGivenName = nextRecord.get("PATIENT_GIVENNAME");

                // Patient Gender - Values will be "M" or "F"
                String patientGender = nextRecord.get("PATIENT_GENDER");

                // White blood cell count - This corresponds to LOINC code:
                // Code:        6690-2
                // Display:     Leukocytes [#/volume] in Blood by Automated count
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   10*3/uL
                String rbc = nextRecord.get("RBC");

                // White blood cell count - This corresponds to LOINC code:
                // Code:        789-8
                // Display:     Erythrocytes [#/volume] in Blood by Automated count
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   10*6/uL
                String wbc = nextRecord.get("WBC");

                // Hemoglobin
                // Code:        718-7
                // Display:     Hemoglobin [Mass/volume] in Blood
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   g/dL
                String hb = nextRecord.get("HB");

                // Day 1 Exercise:
                // Create a Patient resource, and 3 Observation resources, and
                // log them to the console.

                // Creating Patient resource
                Patient patient = new Patient();
                patient.addIdentifier().setValue(patientId);

                HumanName name = patient.addName();
                name.setFamily(patientFamilyName);
                StringType given = name.addGivenElement();
                given.setValue(patientGivenName);

                if (patientGender == "M") {
                    patient.setGender(Enumerations.AdministrativeGender.MALE);
                } else {
                    patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                }

                FhirContext ctx = FhirContext.forR4();
                IParser jsonParser = ctx.newJsonParser();
                ourLog.info("Created Patient resource: " + jsonParser.encodeResourceToString(patient));

                // Creating WBC Observation
                Observation observationWbc = new Observation();
                observationWbc.addIdentifier().setValue("wbc-" + seqN);
                observationWbc.setStatus(Observation.ObservationStatus.FINAL);
                Coding wbcCode = new Coding()
                        .setSystem("http://loinc.org")
                        .setCode("789-8")
                        .setDisplay("Erythrocytes [#/volume] in Blood by Automated count");
                observationWbc.getCode().addCoding(wbcCode);

                Quantity wbcValue = new SimpleQuantity()
                        .setSystem("http://unitsofmeasure.org")
                        .setUnit("10*6/uL")
                        .setCode("10*6/uL")
                        .setValue(new BigDecimal(wbc));
                observationWbc.setValue(wbcValue);

                ourLog.info("Created WBC Observation resource: " + jsonParser.encodeResourceToString(observationWbc));
            }
        }
    }

}
