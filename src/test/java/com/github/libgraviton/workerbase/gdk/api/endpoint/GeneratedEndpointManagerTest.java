package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.exception.NoCorrespondingEndpointException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class GeneratedEndpointManagerTest {

    @Test
    public void testLoadAndPersist() throws Exception {
        File serializationFile = File.createTempFile("endpoint-associations-", ".tmp");
        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);

        String className = "some.ClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://item/collection/");

        Assertions.assertFalse(generatedServiceManager.hasEndpoint(className));
        generatedServiceManager.addEndpoint(className, endpoint);
        Assertions.assertTrue(generatedServiceManager.hasEndpoint(className));

        Assertions.assertEquals(1, generatedServiceManager.persist());

        generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);
        Assertions.assertFalse(generatedServiceManager.hasEndpoint(className));
        Assertions.assertEquals(1, generatedServiceManager.load());
        Assertions.assertTrue(generatedServiceManager.hasEndpoint(className));
        Assertions.assertEquals(generatedServiceManager.getEndpoint(className), endpoint);
    }

    @Test
    public void testGetEndpointWhichIsMissing() {
        Assertions.assertThrows(NoCorrespondingEndpointException.class, () -> {
            File serializationFile = File.createTempFile("endpoint-associations-", ".tmp");
            GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);

            String className = "some.ClassName";
            Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://item/collection/");
            Assertions.assertEquals(generatedServiceManager.getEndpoint(className), endpoint);
        });
    }

    @Test
    public void testLoadFromInexistentFile() {
        Assertions.assertThrows(UnableToLoadEndpointAssociationsException.class, () -> {
            File serializationFile = File.createTempFile("endpoint-associations-deleted", ".tmp");
            // make sure the file does not exist
            Assertions.assertTrue(serializationFile.delete());
            Assertions.assertFalse(serializationFile.exists());

            GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.LOAD);
            generatedServiceManager.load();
        });
    }

    @Test
    public void testLoadFromIncompatibleFile() {
        Assertions.assertThrows(UnableToLoadEndpointAssociationsException.class, () -> {
            File serializationFile = File.createTempFile("incompatible", ".serialized");
            String content = "this is no compatible serialization";
            FileOutputStream fout = new FileOutputStream(serializationFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(content);

            Assertions.assertTrue(serializationFile.exists());

            GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);
            generatedServiceManager.load();
        });
    }

    @Test
    public void testDefaultAssocPath() throws Exception {
        String filePath = "target/generated-sources/gdk-java/assoc";
        File serializationFile = new File(filePath);
        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);

        String className = "some.ClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://item/collection/");
        generatedServiceManager.addEndpoint(className, endpoint);
        generatedServiceManager.persist();

        // new service initialized with default assoc path
        generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.LOAD);
        Assertions.assertEquals(filePath, generatedServiceManager.getSerializationFile().getPath());

        serializationFile.delete();
    }

}
