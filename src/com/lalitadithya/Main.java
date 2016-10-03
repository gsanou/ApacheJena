package com.lalitadithya;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.*;


public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    /***
     * Method to query the RDF model
     * @param query the query
     * @param inFileName the name of the input file
     */
    private static void query(String query, String inFileName) {
        Model model = ModelFactory.createDefaultModel();

        logger.debug("Attempting to open res/ISWC2010.rdf");
        InputStream in = FileManager.get().open("res/" + inFileName);
        if (in == null) {
            Throwable t = new IllegalArgumentException("File not found");
            logger.error("File not found!", t);
            t.printStackTrace();
            System.exit(1);
        }

        logger.debug("Reading from file");
        model.read(in, null);
        logger.debug("Reading success");

        logger.debug("getting resource iterator");
        ResIterator stmtIterator = model.listSubjectsWithProperty(model.getProperty(query));
        while (stmtIterator.hasNext()) {
            System.out.println(stmtIterator.nextResource()
                    .getProperty(model.getProperty(query))
                    .getString());
        }
        logger.debug("query success");
    }

    /***
     * Method to serialize an RDF model
     * @param model the RDF model
     * @param fileName the output file name
     * @throws FileNotFoundException if output file could not be created
     */
    private static void writeToFiles(Model model, String fileName) throws FileNotFoundException {
        long startTime, endTime;

        logger.debug("Writing to file in Turtle Format");
        startTime = System.nanoTime();
        model.write(new OutputStreamWriter(new FileOutputStream("result/" + fileName + ".ttl")), "TURTLE");
        endTime = System.nanoTime();
        logger.debug("Write success. Time taken = " + (endTime - startTime) * Math.pow(10, -9) + " seconds");

        logger.debug("Writing to file in N-triples Format");
        startTime = System.nanoTime();
        model.write(new OutputStreamWriter(new FileOutputStream("result/" + fileName + ".ntri")), "NTRIPLES");
        endTime = System.nanoTime();
        logger.debug("Write success. Time taken = " + (endTime - startTime) * Math.pow(10, -9) + " seconds");

        logger.debug("Writing to file in N-quads Format");
        startTime = System.nanoTime();
        model.write(new OutputStreamWriter(new FileOutputStream("result/" + fileName + ".nquad")), "NQUADS");
        endTime = System.nanoTime();
        logger.debug("Write success. Time taken = " + (endTime - startTime) * Math.pow(10, -9) + " seconds");

        logger.debug("Writing to file in RDF/XML Format");
        startTime = System.nanoTime();
        model.write(new OutputStreamWriter(new FileOutputStream("result/" + fileName + ".xml")), "RDFXML");
        endTime = System.nanoTime();
        logger.debug("Write success. Time taken = " + (endTime - startTime) * Math.pow(10, -9) + " seconds");

        logger.debug("Writing to file in JSON-LD Format");
        startTime = System.nanoTime();
        model.write(new OutputStreamWriter(new FileOutputStream("result/" + fileName + ".json")), "JSONLD");
        endTime = System.nanoTime();
        logger.debug("Write success. Time taken = " + (endTime - startTime) * Math.pow(10, -9) + " seconds");
    }

    /***
     * Method to convert RDF model from one format to another
     * @param inFileName the input file name
     * @param outFileName the output file name
     * @throws FileNotFoundException if the input file can not be read or ouput file can not be created
     */
    private static void convert(String inFileName, String outFileName) throws FileNotFoundException {
        Model myModel = ModelFactory.createDefaultModel();

        logger.debug("Attempting to open res/ISWC2010.rdf");
        InputStream in = FileManager.get().open("res/" + inFileName);
        if (in == null) {
            Throwable t = new IllegalArgumentException("File not found");
            logger.error("File not found!", t);
            t.printStackTrace();
            System.exit(1);
        }

        logger.debug("Reading from file");
        myModel.read(in, null);
        logger.debug("Reading success");

        writeToFiles(myModel, outFileName);
    }

    /***
     * Method to create a simple RDF model
     * @throws FileNotFoundException if the output file can not be created
     */
    private static void createRDF() throws FileNotFoundException {
        String personURI = "http://somewhere/JohnSmith";
        String givenName = "John";
        String familyName = "Smith";
        String fullName = givenName + " " + familyName;

        logger.debug("Creating defualt model");
        Model myModel = ModelFactory.createDefaultModel();
        logger.debug("Model created");

        logger.debug("Adding data to model");
        myModel.createResource(personURI)
                .addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N,
                        myModel.createResource()
                                .addProperty(VCARD.Given, givenName)
                                .addProperty(VCARD.Family, familyName)
                );
        logger.debug("Add success");


        logger.debug("Attempting to print model");
        StmtIterator stmtIterator = myModel.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();

            System.out.println("Subject = " + subject.toString());
            System.out.println("Predicate = " + predicate.toString());
            if (object instanceof Resource) {
                System.out.println("Object = " + object.toString());
            } else {
                System.out.println("Object = \"" + object.toString() + "\"");
            }

            System.out.println("...");
        }
        logger.debug("Printing success");

        writeToFiles(myModel, "basicModel");
    }

    /***
     * Main function
     * @param args command line args
     * @throws IOException if input or ouput file issues
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("log/myLog.txt");
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.ALL);
        fa.setAppend(true);
        fa.activateOptions();
        Logger.getRootLogger().addAppender(fa);

        System.out.println("1-> Create a new model");
        System.out.println("2-> Convert existing file");
        System.out.println("3-> Query the database");

        switch (br.read()) {
            case '1':
                createRDF();
                System.out.println("Complete!");
                break;
            case '2':
                convert("ISWC2010.rdf", "ISWC2010");
                System.out.println("Complete!");
                break;
            case '3':
                query("http://xmlns.com/foaf/0.1/name", "ISWC2010.rdf");
                System.out.println("Complete!");
                break;
        }
    }
}
