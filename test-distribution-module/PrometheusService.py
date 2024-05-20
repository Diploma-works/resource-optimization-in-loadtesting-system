import requests
import json
from flask import Flask, jsonify
from datetime import datetime, timedelta

app = Flask(__name__)

PROMETHEUS_URL = "http://localhost:9090"

class PrometheusService:
    def __init__(self, prometheus_url):
        self.prometheus_url = prometheus_url

    def query_prometheus(self, query, start, end, step):
        url = f"{self.prometheus_url}/api/v1/query_range"
        params = {
            "query": query,
            "start": start,
            "end": end,
            "step": step
        }
        response = requests.get(url, params=params)
        if response.status_code == 200:
            return response.json()
        else:
            raise Exception(f"Failed to query Prometheus: {response.text}")

    def get_mean_value_from_query(self, query, start, end, generator_id):
        prometheus_response = self.query_prometheus(query, start, end, 20)
        for result in prometheus_response['data']['result']:
            if generator_id == result['metric'].get('pod'):
                values = result['values']
                total = sum(float(value[1]) for value in values)
                return total / len(values)
        return -1.0

    def get_max_value_from_query(self, query, start, end, generator_id):
        prometheus_response = self.query_prometheus(query, start, end, 20)
        max_value = -1.0
        for result in prometheus_response['data']['result']:
            if generator_id == result['metric'].get('pod'):
                values = result['values']
                max_value = max(max_value, max(float(value[1]) for value in values))
        return max_value

    def get_current_value_from_query(self, query, generator_id):
        current_time = datetime.now()
        start = int(current_time.timestamp())
        end = int((current_time - timedelta(minutes=5)).timestamp())
        return self.get_max_value_from_query(query, start, end, generator_id)



prometheus_service = PrometheusService(PROMETHEUS_URL)

@app.route('/api/current_cpu_utilization/<generator_id>', methods=['GET'])
def get_current_cpu_utilization(generator_id):
    current_cpu_query = "your_cpu_query_here"  # Ваш запрос к Prometheus
    value = prometheus_service.get_current_value_from_query(current_cpu_query, generator_id)
    return jsonify({"current_cpu_utilization": value})

@app.route('/api/current_ram_utilization/<generator_id>', methods=['GET'])
def get_current_ram_utilization(generator_id):
    current_ram_query = "your_ram_query_here"  # Ваш запрос к Prometheus
    value = prometheus_service.get_current_value_from_query(current_ram_query, generator_id)
    return jsonify({"current_ram_utilization": value})

if __name__ == "__main__":
    app.run(debug=True)
