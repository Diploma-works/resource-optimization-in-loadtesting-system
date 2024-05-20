package org.ledokol.testrunner.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.ledokol.testrunner.utils.MongoDBUtil;
import org.ledokol.testrunner.utils.ParameterCombinationsGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class MongoDBService {

    private static final MongoDBUtil mongoDBUtil = MongoDBUtil.getInstance();

    public static void insertParameterSets(List<ParameterCombinationsGenerator.ParameterSet> combinations) {
        MongoDatabase database = mongoDBUtil.getDatabase();
        MongoCollection<Document> collection = database.getCollection("importedSet");

        List<Document> documents = new ArrayList<>();
        for (ParameterCombinationsGenerator.ParameterSet combo : combinations) {
            Document document = new Document("vuser", combo.getVuser())
                    .append("pacing", combo.getPacing())
                    .append("requestSize", combo.getRequestSize())
                    .append("steps", combo.getSteps())
                    .append("is_done", combo.getIsDone());
            documents.add(document);
        }

        collection.insertMany(documents);
    }

    public static List<ParameterCombinationsGenerator.ParameterSet> findAllParameterSets() {
        MongoDatabase database = mongoDBUtil.getDatabase();
        MongoCollection<Document> collection = database.getCollection("importedSet");

        List<ParameterCombinationsGenerator.ParameterSet> parameterSets = new ArrayList<>();
        FindIterable<Document> documents = collection.find();

        documents.forEach((Consumer<Document>) document -> {
            parameterSets.add(documentToParameterSet(document));
        });

        return parameterSets;
    }

    public static void updateParameterSetWithTestInfo(String parameterSetId, String testId, String testRunId, String generatorInstanceId,
                                                      Date startDate, Date endDate) {
        try {
            MongoDatabase database = mongoDBUtil.getDatabase(); // Предполагаем, что этот метод уже реализован
            MongoCollection<Document> collection = database.getCollection("importedSet");

            // Создаем фильтр по _id, используя parameterSetId для поиска соответствующего документа
            // Преобразуем строковый ID в ObjectId для использования в фильтре
            ObjectId id = new ObjectId(parameterSetId);

            // Обновляем документ, добавляя testId и testRunId
            collection.updateOne(eq("_id", id), set("testId", testId));
            collection.updateOne(eq("_id", id), set("testRunId", testRunId));
            collection.updateOne(eq("_id", id), set("generatorInstanceId", generatorInstanceId));
            collection.updateOne(eq("_id", id), set("is_done", true));
            collection.updateOne(eq("_id", id), set("startDate", startDate));
            collection.updateOne(eq("_id", id), set("endDate", endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static ParameterCombinationsGenerator.ParameterSet documentToParameterSet(Document document) {
        ObjectId id = document.getObjectId("_id");
        int vuser = document.getInteger("vuser");
        int pacing = document.getInteger("pacing");
        int requestSize = document.getInteger("requestSize");
        int steps = document.getInteger("steps");
        boolean isDone = document.getBoolean("is_done");
        Date startDate  = document.getDate("startDate");
        Date endDate  = document.getDate("endDate");
        String generatorId  = document.getString("generatorInstanceId");
        return new ParameterCombinationsGenerator.ParameterSet(id, vuser, pacing, requestSize, steps, isDone, startDate, endDate, generatorId);
    }

}
