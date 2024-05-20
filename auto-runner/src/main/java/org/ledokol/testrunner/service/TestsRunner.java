package org.ledokol.testrunner.service;

import org.json.JSONObject;
import org.ledokol.testrunner.exceptions.IncorrectStoppingTestException;
import org.ledokol.testrunner.model.script.Script;
import org.ledokol.testrunner.model.test.Test;
import org.ledokol.testrunner.utils.JsonUtil;
import org.ledokol.testrunner.utils.ParameterCombinationsGenerator;
import org.ledokol.testrunner.utils.TestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service("default-runner")
public class TestsRunner {

    @Autowired
    ApiService apiService;

    private int NUMBER_OF_THREADS; // Используйте настройки, соответствующие вашей системе
    private ExecutorService executorService;
    private static final TestBuilder testBuilder = new TestBuilder();


    @Async
    public void runAutoTesting() {
        List<String> generators = null; // Получаем список генераторов
        try {
            generators = apiService.getGenerators();
            NUMBER_OF_THREADS = generators.size();
            executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        } catch (Exception e) {
            e.printStackTrace();
        }
        List<ParameterCombinationsGenerator.ParameterSet> allParameterSets = MongoDBService.findAllParameterSets();
        int totalSets = allParameterSets.size();
        int groupSize = (int) Math.ceil((double) totalSets / NUMBER_OF_THREADS);

        CompletableFuture[] futures = new CompletableFuture[NUMBER_OF_THREADS];

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            String generatorId = generators.get(i); // Выбираем уникальный генератор для каждой группы
            int start = i * groupSize;
            int end = Math.min((i + 1) * groupSize, totalSets);
            futures[i] = CompletableFuture.runAsync(() -> processParameterSetsGroup(allParameterSets, start, end, generatorId), executorService);
        }

        CompletableFuture.allOf(futures).join(); // Дожидаемся выполнения всех задач


        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        System.out.print("All tests have been created");
    }


    private void processParameterSetsGroup(List<ParameterCombinationsGenerator.ParameterSet> parameterSets, int start, int end, String generatorId) {
        for (int i = start; i < end; i++) {
            try {
                ParameterCombinationsGenerator.ParameterSet parameterSet = parameterSets.get(i);

                Random random = new Random(); // Создаём экземпляр Random
                int initSleepInSeconds = random.nextInt(20) + 1; // Генерируем число от 0 до 9, затем прибавляем 1
                TimeUnit.SECONDS.sleep(initSleepInSeconds);

                // Создание объекта скрипта
                String scriptResponse = createAndSendScript(parameterSet);
                JSONObject scriptResponseJson = new JSONObject(scriptResponse);
                String scriptId = scriptResponseJson.getString("id");
                System.out.println("создан скипрт с id = " + scriptId);


                // Создание объекта теста
                String testResponse = createAndSendTest(parameterSet, scriptId);
                JSONObject testResponseJson = new JSONObject(testResponse);
                String testId = testResponseJson.getString("id");
                System.out.println("создан тест с id = " + testId);

                // Запуск теста
                String startTestResponse = startTest(testId, generatorId);
                JSONObject startTestResponseJson = new JSONObject(startTestResponse);
                String testRunId = startTestResponseJson.getString("id");
                System.out.println("++запущен тест с id = " + testId + ";  testRunId = " + testRunId);
                Date startDate = new Date();

                // Ждем 7 минут  перед остановкой теста
                TimeUnit.MINUTES.sleep(11);

                // Остановка теста
                try {
                    apiService.stopTest(testRunId);
                    System.out.println("--остановлен тест с id = " + testId + ";  testRunId = " + testRunId);
                }catch (IncorrectStoppingTestException ex){
                    System.err.println("не удалось завершить тест, продолжаю работу");
                }
                // Обновление документа в MongoDB

                Date endDate = new Date();
                MongoDBService.updateParameterSetWithTestInfo(String.valueOf(parameterSet.getId()), testId, testRunId, generatorId, startDate, endDate);
                TimeUnit.MINUTES.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
                // Логирование или другая обработка исключения
            }
        }
    }

    public String createAndSendTest(ParameterCombinationsGenerator.ParameterSet parameterSet, String scriptId) throws Exception {
        Test test = testBuilder.createTestFromParameterSet(parameterSet);
        test.getScenarios().forEach(scenario -> scenario.setScriptId(scriptId));
        String testJson = JsonUtil.toJson(test);
        return sendTestWithRetry(test.getName(), testJson, 2);
    }

    // Вспомогательный метод для отправки скрипта с возможностью повторной попытки
    private String sendTestWithRetry(String testName, String testJson, int maxAttempts) throws Exception {
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                // Попытка отправить тест
                System.out.println("\n" + attempt + " попытка создать тест " + testName);
                return apiService.createTest(testJson);
            } catch (Exception e) {
                attempt++;
                System.out.println("\nНе удалось создать тест " + testName + ", stacktrace:\n" + e.getMessage());
                if (attempt < maxAttempts) {
                    System.out.println("Попытка " + (attempt + 1) + " через минуту\n");
                    try {
                        TimeUnit.MINUTES.sleep(1); // Задержка перед следующей попыткой
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Восстановление прерванного статуса
                        throw new RuntimeException("Прервано во время ожидания", ie);
                    }
                } else {
                    throw e; // Переброшено исключение, если превышено максимальное количество попыток
                }
            }
        }
        return ""; // Возвращается пустая строка, если метод не удается выполнить до конца
    }


    public  String createAndSendScript(ParameterCombinationsGenerator.ParameterSet parameterSet) throws Exception {
        Script script = testBuilder.createScriptFromParameterSet(parameterSet);
        String scriptJson = JsonUtil.toJson(script);
        return sendScriptWithRetry(script.getName(), scriptJson, 2); // Попытка отправить скрипт, максимум 2 раза
    }

    private  String sendScriptWithRetry(String scriptName, String scriptJson, int maxAttempts) throws Exception {
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                // Попытка отправить скрипт
                System.out.println("\n" + attempt + " попытка создать скрипт " + scriptName);
                return apiService.createScript(scriptJson);
            } catch (Exception e) {
                attempt++;
                System.out.println("\nНе удалось создать скрипт " + scriptName + ", stacktrace:\n" + e.getMessage());
                if (attempt < maxAttempts) {
                    System.out.println("Попытка " + (attempt + 1) + " через минуту\n");
                    try {
                        TimeUnit.MINUTES.sleep(1); // Задержка перед следующей попыткой
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Восстановление прерванного статуса
                        throw new RuntimeException("Прервано во время ожидания", ie);
                    }
                } else {
                    throw e; // Переброшено исключение, если превышено максимальное количество попыток
                }
            }
        }
        return ""; // Возвращается пустая строка, если метод не удается выполнить до конца
    }


    public String startTest(String testId, String generatorId) throws Exception {
        return startTestWithRetry(testId, generatorId); // Попытка запустить тест, максимум 2 раза
    }

    // Вспомогательный метод для попытки запуска теста с повтором
    private String startTestWithRetry(String testId, String generatorId) throws Exception {
        int attempt = 0;
        while (attempt < 2) {
            try {
                // Попытка запустить тест
                return apiService.startTestWithGenerator(testId, generatorId);
            } catch (RuntimeException e) {
                attempt++;
                System.err.println("Не удалось запустить тест, попытка " + attempt + ": " + e.getMessage());
                if (attempt < 2) {
                    try {
                        TimeUnit.SECONDS.sleep(10); // Задержка перед следующей попыткой, 10 секунд для примера
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Восстановление прерванного статуса
                        throw new RuntimeException("Прервано во время ожидания", ie);
                    }
                } else {
                    throw e; // Переброс исключения, если превышено максимальное количество попыток
                }
            }
        }
        throw new RuntimeException("Превышено максимальное количество попыток запуска теста");
    }
}
