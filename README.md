# Материалы для Выпускной Квалификационной Работы (ВКР)

В данном репозитории собраны проекты, работа над которыми велась в рамках выполнения ВКР. Ниже представлено описание каждого:

## 1. auto-runner

Модуль, реализующий автоматизированный запуск 10 000 тестов на основе сгенерированных комбинаций параметров. Модуль разворачивается в кластере Kubernetes вместе с остальными компонентами инструмента «Ледокол». Интегрирован с управляющим тестом, MongoDB.

### Настройка перед деплоем

В файле `application.properties` необходимо установить:
- `main-service.baseurl` - базовый URL управляющего сервиса
- `mongodb.connection-string` - строка для подключения к MongoDB
- `mongodb.database-name` - имя базы данных 
- `target-system.baseurl` - базовый URL объекта тестирования

### Деплой

1. Подготовить Dockerfile:
    ```Dockerfile
    FROM openjdk:11-jre-slim

    WORKDIR /app

    COPY target/auto-runner-0.0.1-SNAPSHOT.jar ./app.jar

    ENTRYPOINT ["java", "-jar", "app.jar"]
    ```

2. Собрать образ:
    ```sh
    docker build -t tests-auto-running:v1.4 .
    ```

3. Пометить тегом:
    ```sh
    docker tag runner alexxxtrsv/runner:v2.3
    ```

4. Запушить в Docker Hub:
    ```sh
    docker push alexxxtrsv/runner:v2.3
    ```

5. Добавить конфигурационные файлы Kubernetes (см. `ledokol-config/auto-running-deployment.yaml`, `ledokol-config/auto-running-service.yaml`)

## 2. dataset-collector

Модуль, реализующий сбор данных мониторинга после запуска тестов и формирование датасета.

### Настройка

В файле `application.yml` необходимо указать:
```yaml
data:
  mongodb:
    host: # хост базы данных
    port: # порт
    database: # имя базы данных
    username: ***
    password: ***
    auto-index-creation: true
prometheus:
  url: # URL Прометеуса
```

## 3. ledokol-config

Описывает конфигурацию инструмента тестирования «Ледокол». Содержит необходимые Helm-чарты и шаблоны для деплоя всех компонентов проекта в кластер Kubernetes.

### Шаги деплоя

1. Создать секрет Kubernetes с реквизитами базы данных:
    ```sh
    kubectl create secret generic db-user --from-literal=username=*** --from-literal=password=***
    ```

2. В main-deployment установить актуальные хост и логин пользователя БД:
    ```sh
    SPRING.DATA.MONGODB.HOST и SPRING.DATA.MONGODB.USERNAME
    ```

3. Установить «Ледокол» в кластер Kubernetes с помощью Helm:
    ```sh
    helm install ledokol-config .
    ```

4. При внесении изменений в конфигурацию обновить командой:
    ```sh
    helm upgrade ledokol-config .
    ```

### Доступы

- Доступ к мониторингу:
    ```sh
    kubectl -n prometheus-monitoring port-forward service/kube-prometheus-stack-grafana 8001:80
    ```
    [http://127.0.0.1:8001](http://127.0.0.1:8001)

- Доступ к дашборду:
    ```sh
    kubectl proxy -n kubernetes-dashboard https://kubernetes-dashboard.svc
    ```

- Проброс портов для фронтенда:
    ```sh
    kubectl -n default port-forward service/frontend-service 8003:80
    ```

- Проброс портов для управляющего сервиса:
    ```sh
    kubectl -n default port-forward service/main-service 8004:8080
    ```

## 4. model-research

Содержит код для создания, обучения и тестирования моделей машинного обучения. Используется для исследования методов МО, а также для создания финальных обученных моделей для использования в модуле распределения тестов.

## 5. test-distribution-module

Сборник выдержек из кода управляющего сервиса и сервиса распределения тестов по генераторам.
