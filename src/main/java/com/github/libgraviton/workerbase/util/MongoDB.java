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

    private static String connectionString = WorkerProperties.getProperty("mongodb.url");
    private static String databaseName = WorkerProperties.getProperty("mongodb.db");
    private static List<String> pojoCodecs = Collections.synchronizedList(new ArrayList<>(){{add("com.github.libgraviton.jdk.gravitondyn");}});
    private static MongoClientSettings.Builder cientSettings = getMongoClientSettings();


    public static void addPojoCodecs(ArrayList<String> _pojoCodecs) {
        pojoCodecs.addAll(_pojoCodecs);
        cientSettings = getMongoClientSettings();
    }

    public static void addPojoCodec(String pojoCodec) {
        pojoCodecs.add(pojoCodec);
        cientSettings = getMongoClientSettings();
    }

    private static MongoClientSettings.Builder getMongoClientSettings() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(String.join(",", pojoCodecs.toArray(new String[0]))).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        return MongoClientSettings.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .codecRegistry(pojoCodecRegistry);
    }

    public static void setConnectionString(String connectionString) {
        MongoDB.connectionString = connectionString;
    }

    public static void setDatabaseName(String databaseName) {
        MongoDB.databaseName = databaseName;
    }

    public static String getDatabaseName() {
        return databaseName;
    }

    public synchronized static MongoClient getMongoClient() {
        return MongoClients.create(
                cientSettings.applyConnectionString(
                        new ConnectionString(connectionString)
                ).build()
        );
    }

    public static <T> T getDocument(String collectionName, Bson filter, Class<T> type) {
        return getDocument(collectionName,filter,type, new ArrayList<>());
    }
    public static <T> List<T> getDocuments(String collectionName, Bson filter, Class<T> type) {
        return getDocuments(collectionName,filter,type, new ArrayList<>());
    }

    public static <T> T getDocument(String collectionName, String id, Class<T> type) {
        return getDocument(collectionName,new Document("_id",id),type, new ArrayList<>());
    }

    public static <T> T getDocument(String collectionName, String id, Class<T> type, List<String> fields) {
        return getDocument(collectionName,new Document("_id",id),type, fields);
    }

    public static <T> T getDocument(String collectionName, Bson filter, Class<T> type, List<String> fields) {
        Bson projection = fields != null ? Projections.fields(Projections.include(fields)) : new Document();

        try (MongoClient mongoClient = MongoDB.getMongoClient();
             MongoCursor<T> iterator = mongoClient.getDatabase(MongoDB.getDatabaseName()).getCollection(collectionName,type)
                     .find(filter)
                     .projection(projection)
                     .iterator()) {

            return iterator.hasNext() ? Converter.getInstance(iterator.next(),type) : null;
        }
    }

    public static <T> List<T> getDocuments(String collectionName, Bson filter, Class<T> type, List<String> fields) {
        Bson projection = fields != null ? Projections.fields(Projections.include(fields)) : new Document();

        try (MongoClient mongoClient = MongoDB.getMongoClient();
             MongoCursor<T> iterator = mongoClient.getDatabase(MongoDB.getDatabaseName()).getCollection(collectionName,type)
                     .find(filter)
                     .projection(projection)
                     .iterator()) {

            return Lists.newArrayList(Converter.getInstance(iterator.next(),type));
        }
    }

    public static UpdateResult upsertDocument(String collectionName, String id, Object document, boolean upsert) {
        MongoClient mongoClient = MongoDB.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(MongoDB.getDatabaseName()).getCollection(collectionName, document.getClass());

        UpdateResult updateResult = collection.updateOne(
                new Document().append("_id", id),
                new Document().append("$set", document),
                new UpdateOptions().upsert(upsert)
        );

        mongoClient.close();

        return updateResult;
    }

    public static UpdateResult updateMany(String collectionName, Bson filter, Object document, boolean upsert) {
        MongoClient mongoClient = MongoDB.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(MongoDB.getDatabaseName()).getCollection(collectionName, document.getClass());

        UpdateResult updateResult = collection.updateMany(
                filter,
                new Document().append("$set", document),
                new UpdateOptions().upsert(upsert)
        );

        mongoClient.close();

        return updateResult;
    }

    public static DeleteResult deleteDocument(String collectionName, String id) {
        return deleteDocument(collectionName, new Document("_id", id));
    }

    public static DeleteResult deleteDocument(String collectionName, Bson filter) {
        MongoClient mongoClient = MongoDB.getMongoClient();
        MongoCollection<?> collection = mongoClient.getDatabase(MongoDB.getDatabaseName()).getCollection(collectionName);

        DeleteResult deleteResult = collection.deleteMany(filter);
        mongoClient.close();

        return deleteResult;
    }
}