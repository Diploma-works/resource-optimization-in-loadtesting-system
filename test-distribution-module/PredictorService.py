import numpy as np
import tensorflow as tf

class PredictorService:
    def __init__(self, cpu_model_path, ram_model_path, vusers_model_path):
        self.cpu_model = tf.keras.models.load_model(cpu_model_path)
        self.ram_model = tf.keras.models.load_model(ram_model_path)
        self.vusers_model = tf.keras.models.load_model(vusers_model_path)

    def predict_cpu_utilization(self, pacing, steps, body_size, vusers_amount):
        test_params = np.array([[pacing, steps, body_size, vusers_amount]])
        return self.cpu_model.predict(test_params).flatten()[0]

    def predict_ram_utilization(self, pacing, steps, body_size, vusers_amount):
        test_params = np.array([[pacing, steps, body_size, vusers_amount]])
        return self.ram_model.predict(test_params).flatten()[0]