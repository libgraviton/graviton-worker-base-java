package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToPersistEndpointAssociationsException;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * Endpoint manager for generated POJOs. This service manager is capable of serializing it's service to POJO class
 * association to a file and deserialize it afterwards.
 */
public class GeneratedEndpointManager extends EndpointManager {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratedEndpointManager.class);

    public enum Mode {
        LOAD, CREATE
    }

    /**
     * The file holding the serialized service to POJO class association.
     */
    private final File serializationFile;

    private final String assocFilePath;

    /**
     * Constructor. Defines the serialization file.
     *
     * @param serializationFile The serialization file.
     * @param mode Whether to load the serialization file or not.
     *
     * @throws UnableToLoadEndpointAssociationsException When the serialization file cannot be loaded.
     */
    public GeneratedEndpointManager(File serializationFile, Mode mode) throws UnableToLoadEndpointAssociationsException {
        this.serializationFile = serializationFile;
        assocFilePath = WorkerProperties.getProperty("graviton.assoc.file.location");
        if (Mode.LOAD.equals(mode)) {
            load();
        } else {
            if (serializationFile != null) {
                serializationFile.getParentFile().mkdirs();
                try {
                    serializationFile.createNewFile();
                } catch (IOException e) {
                    throw new UnableToLoadEndpointAssociationsException("Unable to create new file at '" + assocFilePath + "'.");
                }
            }
        }

        this.strategy = EndpointInclusionStrategy.create(EndpointInclusionStrategy.Strategy.DEFAULT, null);
    }

    public File getSerializationFile() {
        return serializationFile;
    }

    /**
     * Constructor. Defines the serialization file.
     *
     * @param mode Whether to load the serialization file or not.
     *
     * @throws UnableToLoadEndpointAssociationsException When the serialization file cannot be loaded.
     */
    public GeneratedEndpointManager(Mode mode) throws UnableToLoadEndpointAssociationsException {
        this(null, mode);
    }

    /**
     * Constructor. Defines the serialization file and tries to load it if it exists.
     *
     * @throws UnableToLoadEndpointAssociationsException When the serialization file cannot be loaded.
     */
    public GeneratedEndpointManager() throws UnableToLoadEndpointAssociationsException {
        this(Mode.LOAD);
    }

    /**
     * Loads the service endpoints to POJO class association from the serialization file.
     *
     * @return The number of currently loaded service endpoints to POJO class associations.
     *
     * @throws UnableToLoadEndpointAssociationsException When service endpoints loading is not possible / failed.
     */
    public int load() throws UnableToLoadEndpointAssociationsException {
        try {
            try (InputStream inputStream = loadInputStream()) {
                if (inputStream != null) {
                    try (ObjectInputStream objectinputstream = new ObjectInputStream(inputStream)) {
                        endpoints.putAll(
                                (Map<String, Endpoint>) objectinputstream.readObject()
                        );
                    }
                }
            }

            LOG.debug("{} endpoints loaded", endpoints.size());
        } catch (IOException e) {
            throw new UnableToLoadEndpointAssociationsException(
                    "Unable to deserialize '" + assocFilePath + "'.",
                    e
            );
        } catch (ClassNotFoundException e) {
            throw new UnableToLoadEndpointAssociationsException(
                    "Cannot deserialize from '" + assocFilePath +
                            "' because one ore multiple destination classes do not exist.",
                    e
            );
        } catch (ClassCastException e) {
            throw new UnableToLoadEndpointAssociationsException(
                    "Failed to load from '" + assocFilePath +
                            "'. File content is incompatible.",
                    e
            );
        }
        return endpoints.size();
    }

    protected InputStream loadInputStream() throws UnableToLoadEndpointAssociationsException {
        // if serializationFile is set, this overrides the assocFilePath
        if (serializationFile != null) {
            try {
                // resource not found? Try to load as file
                return new FileInputStream(serializationFile);
            } catch (FileNotFoundException e) {
                throw new UnableToLoadEndpointAssociationsException(
                        "Resource / file '" + assocFilePath + "' does not exist."
                );
            }
        }

        // try to load as resource first
        LOG.debug("Load resource as stream from '{}'.", assocFilePath);

        return GeneratedEndpointManager.class.getClassLoader().getResourceAsStream(assocFilePath);
    }

    /**
     * Writes the service to POJO class associations to the serialization file.
     *
     * @return The number of service to POJO class associations written.
     * @throws UnableToPersistEndpointAssociationsException on persistence problems
     */
    public int persist() throws UnableToPersistEndpointAssociationsException {
        try {
            FileOutputStream fout = new FileOutputStream(serializationFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(endpoints);
            fout.close();
            oos.close();
        } catch (IOException e) {
            throw new UnableToPersistEndpointAssociationsException(
                    "Cannot persist to file '" + serializationFile.getAbsolutePath() + "'. An IO error occurred.",
                    e
            );
        }
        return endpoints.size();
    }
}
