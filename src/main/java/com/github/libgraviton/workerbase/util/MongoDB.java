package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.google.common.collect.Lists;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoDB {

    private String connectionString = WorkerProperties.getProperty("mongodb.url");
    private String databaseName = WorkerProperties.getProperty("mongodb.db");
    private static List<String> pojoCodecs = Collections.synchronizedList(new ArrayList<>(){{add("com.github.libgraviton.jdk.gravitondyn");}});
    private static MongoClientSettings.Builder clientSettings = getMongoClientSettings();


    public static void addPojoCodecs(ArrayList<String> _pojoCodecs) {
        pojoCodecs.addAll(_pojoCodecs);
        clientSettings = getMongoClientSettings();
    }

    public static void addPojoCodec(String pojoCodec) {
        pojoCodecs.add(pojoCodec);
        clientSettings = getMongoClientSettings();
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private static MongoClientSettings.Builder getMongoClientSettings() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(String.join(",", pojoCodecs.toArray(new String[0]))).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        return MongoClientSettings.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .codecRegistry(pojoCodecRegistry);
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public synchronized MongoClient getMongoClient() {
        return MongoClients.create(
                clientSettings.applyConnectionString(
                        new ConnectionString(this.connectionString)
                ).build()
        );
    }

    public <T> T getDocument(String collectionName, Bson filter, Class<T> type) {
        return getDocument(collectionName,filter,type, new ArrayList<>());
    }
    public <T> List<T> getDocuments(String collectionName, Bson filter, Class<T> type) {
        return getDocuments(collectionName,filter,type, new ArrayList<>());
    }

    public <T> T getDocument(String collectionName, String id, Class<T> type) {
        return getDocument(collectionName,new Document("_id",id),type, new ArrayList<>());
    }

    public <T> T getDocument(String collectionName, String id, Class<T> type, List<String> fields) {
        return getDocument(collectionName,new Document("_id",id),type, fields);
    }

    public <T> T getDocument(String collectionName, Bson filter, Class<T> type, List<String> fields) {
        Bson projection = fields != null ? Projections.fields(Projections.include(fields)) : new Document();

        return getDocument(collectionName, filter, type, projection);
    }

    public <T> T getDocument(String collectionName, Bson filter, Class<T> type, Bson projection) {
        projection = projection != null ? projection : new Document();

        try (MongoClient mongoClient = this.getMongoClient();
             MongoCursor<T> iterator = mongoClient.getDatabase(this.getDatabaseName()).getCollection(collectionName,type)
                     .find(filter)
                     .projection(projection)
                     .iterator()) {

            return iterator.hasNext() ? Converter.getInstance(iterator.next(),type) : null;
        }
    }

    public <T> List<T> getDocuments(String collectionName, Bson filter, Class<T> type, List<String> fields) {
        Bson projection = fields != null ? Projections.fields(Projections.include(fields)) : new Document();

        return getDocuments(collectionName, filter, type, projection);
    }

    public <T> List<T> getDocuments(String collectionName, Bson filter, Class<T> type, Bson projection) {
        projection = projection != null ? projection : new Document();

        try (MongoClient mongoClient = this.getMongoClient();
             MongoCursor<T> iterator = mongoClient.getDatabase(this.getDatabaseName()).getCollection(collectionName,type)
                     .find(filter)
                     .projection(projection)
                     .iterator()) {

            return iterator.hasNext() ? Lists.newArrayList(Converter.getInstance(iterator.next(),type)) : null;
        }
    }

    public UpdateResult upsertDocument(String collectionName, String id, Object document, boolean upsert) {
        MongoClient mongoClient = this.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(this.getDatabaseName()).getCollection(collectionName, document.getClass());

        UpdateResult updateResult = collection.updateOne(
                new Document().append("_id", id),
                new Document().append("$set", document),
                new UpdateOptions().upsert(upsert)
        );

        mongoClient.close();

        return updateResult;
    }

    public UpdateResult updateMany(String collectionName, Bson filter, Object document, boolean upsert) {
        MongoClient mongoClient = this.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(this.getDatabaseName()).getCollection(collectionName, document.getClass());

        UpdateResult updateResult = collection.updateMany(
                filter,
                new Document().append("$set", document),
                new UpdateOptions().upsert(upsert)
        );

        mongoClient.close();

        return updateResult;
    }

    public DeleteResult deleteDocument(String collectionName, String id) {
        return deleteDocument(collectionName, new Document("_id", id));
    }

    public DeleteResult deleteDocument(String collectionName, Bson filter) {
        MongoClient mongoClient = this.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(this.getDatabaseName()).getCollection(collectionName);

        DeleteResult deleteResult = collection.deleteMany(filter);
        mongoClient.close();

        return deleteResult;
    }
}