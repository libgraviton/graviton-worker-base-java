package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.exception.NoCorrespondingEndpointException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;


public class GeneratedEndpointManagerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLoadAndPersist() throws Exception {
        File serializationFile = File.createTempFile("endpoint-associations-", ".tmp");
        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);

        String className = "some.ClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://item/collection/");

        assertFalse(generatedServiceManager.hasEndpoint(className));
        generatedServiceManager.addEndpoint(className, endpoint);
        assertTrue(generatedServiceManager.hasEndpoint(className));

        assertEquals(1, generatedServiceManager.persist());

        generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);
        assertFalse(generatedServiceManager.hasEndpoint(className));
        assertEquals(1, generatedServiceManager.load());
        assertTrue(generatedServiceManager.hasEndpoint(className));
        assertEquals(generatedServiceManager.getEndpoint(className), endpoint);
    }

    @Test(expected = NoCorrespondingEndpointException.class)
    public void testGetEndpointWhichIsMissing() throws Exception {
        File serializationFile = File.createTempFile("endpoint-associations-", ".tmp");
        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);

        String className = "some.ClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://item/collection/");
        assertEquals(generatedServiceManager.getEndpoint(className), endpoint);
    }

    @Test
    public void testLoadFromInexistentFile() throws Exception {
        thrown.expect(UnableToLoadEndpointAssociationsException.class);
        thrown.expectMessage("not exist");

        File serializationFile = File.createTempFile("endpoint-associations-deleted", ".tmp");
        // make sure the file does not exist
        assertTrue(serializationFile.delete());
        assertFalse(serializationFile.exists());

        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.LOAD);
        generatedServiceManager.load();
    }

    @Test
    public void testLoadFromIncompatibleFile() throws Exception {
        thrown.expect(UnableToLoadEndpointAssociationsException.class);
        thrown.expectMessage("incompatible");

        File serializationFile = File.createTempFile("incompatible", ".serialized");
        String content = "this is no compatible serialization";
        FileOutputStream fout = new FileOutputStream(serializationFile);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(content);

        assertTrue(serializationFile.exists());

        GeneratedEndpointManager generatedServiceManager = new GeneratedEndpointManager(serializationFile, GeneratedEndpointManager.Mode.CREATE);
        generatedServiceManager.load();
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
        assertEquals(filePath, generatedServiceManager.serializationFile.getPath());

        serializationFile.delete();
    }

}
