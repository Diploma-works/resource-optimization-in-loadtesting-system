package org.ledokol.testrunner.utils;

import org.bson.types.ObjectId;
import org.ledokol.testrunner.service.MongoDBService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

public class ParameterCombinationsGenerator {
    public static void main(String[] args) {
        // Инициализация параметров
        List<Integer> vusers = generateRange(1, 91, 10);
        List<Integer> pacing = generateRange(10, 100, 10);
        List<Integer> requestSizes = generateRange(1, 1027, 114);
        List<Integer> steps = generateRange(1, 20, 2);

        // Генерация комбинаций
        List<ParameterSet> combinations = generateCombinations(vusers, pacing, requestSizes, steps);

        IntStream.range(0, combinations.size()).forEach(i ->{
            System.out.println("==================\n" + i + ":  " + combinations.get(i).toString());
        });
        MongoDBService mongoDBService = new MongoDBService();
        mongoDBService.insertParameterSets(combinations);
        System.out.println("done");
    }

    private static List<Integer> generateRange(int start, int end, int step) {
        List<Integer> range = new ArrayList<>();
        for (int i = start; i <= end; i += step) {
            range.add(i);
        }
        return range;
    }

    private static List<ParameterSet> generateCombinations(List<Integer> vusers, List<Integer> pacing, List<Integer> requestSizes, List<Integer> steps) {
        List<ParameterSet> combinations = new ArrayList<>();
        for (int user : vusers) {
            for (int pace : pacing) {
                for (int size : requestSizes) {
                    for (int step : steps) {
                        combinations.add(new ParameterSet(user, pace, size, step, false));
                    }
                }
            }
        }
        return combinations;
    }

    // Внутренний класс для представления набора параметров
    public static class ParameterSet {
        int vuser;
        int pacing;
        int requestSize;
        int steps;
        ObjectId id;
        Date startDate;
        Date endDate;

        String generatorId;

        boolean isDone;

        public ParameterSet(ObjectId id, int vuser, int pacing, int requestSize, int steps,
                            boolean isDone, Date startDate, Date endDate, String generatorId) {
            this.id = id;
            this.vuser = vuser;
            this.pacing = pacing;
            this.requestSize = requestSize;
            this.steps = steps;
            this.isDone = isDone;
            this.startDate = startDate;
            this.endDate = endDate;
            this.generatorId = generatorId;
        }
        public ParameterSet(int vuser, int pacing, int requestSize, int steps, boolean isDone) {
            this.vuser = vuser;
            this.pacing = pacing;
            this.requestSize = requestSize;
            this.steps = steps;
            this.isDone = isDone;
        }

        public ParameterSet(int vuser, int pacing, int requestSize, int steps, boolean isDone, Date startDate, Date endDate) {
            this.vuser = vuser;
            this.pacing = pacing;
            this.requestSize = requestSize;
            this.steps = steps;
            this.isDone = isDone;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public String toString() {
            return  "pacing: " + this.pacing +
                    "\nsteps: " + this.steps +
                    "\nbodySize: " + this.requestSize +
                    "\nvusers: " + this.vuser;
        }

        public int getVuser() {
            return vuser;
        }

        public void setVuser(int vuser) {
            this.vuser = vuser;
        }

        public int getPacing() {
            return pacing;
        }

        public void setPacing(int pacing) {
            this.pacing = pacing;
        }

        public int getRequestSize() {
            return requestSize;
        }

        public void setRequestSize(int requestSize) {
            this.requestSize = requestSize;
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public boolean getIsDone() {
            return isDone;
        }

        public void setDone(boolean done) {
            isDone = done;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public boolean isDone() {
            return isDone;
        }

        public String getGeneratorId() {
            return generatorId;
        }

        public void setGeneratorId(String generatorId) {
            this.generatorId = generatorId;
        }
    }
}
