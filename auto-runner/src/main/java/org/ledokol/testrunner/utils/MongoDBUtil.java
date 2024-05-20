package org.ledokol.testrunner.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;

public class MongoDBUtil {

    private static MongoDBUtil mongoDBUtil;
    private static MongoClient mongoClient;

    @Value("${mongodb.connection-string}")
    private String CONNECTION_STRING;

    @Value("${mongodb.database-name}")
    private String DATABASE_NAME = "ledokol";

    public MongoDatabase getDatabase() {
        if (mongoClient == null)
            mongoClient = MongoClients.create(CONNECTION_STRING);
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoDBUtil getInstance(){
        if (mongoDBUtil == null)
            mongoDBUtil = new MongoDBUtil();
        return mongoDBUtil;
    }
}
