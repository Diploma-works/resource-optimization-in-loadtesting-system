import requests

class ApiService:
    BASE_URL = "http://main-service.default.svc.cluster.local:8080/api"

    def get_generators(self):
        url = f"{self.BASE_URL}/generators"
        response = requests.get(url)
        if response.status_code == 200:
            return [generator['instanceId'] for generator in response.json()]
        else:
            raise RuntimeError(f"Failed to get generators: {response.text}")


    def getGeneratorsAmount(self):
        return len(self.get_generators())
