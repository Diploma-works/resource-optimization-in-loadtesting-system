import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from tensorflow.keras import layers
from sklearn.utils import shuffle
from sklearn.preprocessing import StandardScaler
from tensorflow.keras.optimizers import SGD

def load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col):

    train_df = pd.read_csv(train_data_path)
    train_df = shuffle(train_df)
    
    
    # Выделение признаков и целевой переменной
    X_train = train_df[feature_cols]
    y_train = train_df[target_col]
    
    
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    
    
    test_df = pd.read_csv(test_data_path)
    
    X_test = test_df[feature_cols]
    y_test = test_df[target_col]
    
    
    X_test_scaled = scaler.transform(X_test)
    
    return X_train_scaled, y_train, X_test_scaled, y_test, scaler

# Создание и компиляция модели
def create_model(input_shape):
    # model = tf.keras.Sequential([
    #     layers.Dense(1, input_shape=(len(feature_cols),))
    # ])
    
    # model.compile(optimizer='adam',
    #               loss='mean_squared_error')
    
    model = tf.keras.Sequential([
        layers.Dense(1, input_shape=(input_shape,))
    ])
    
    optimizer = SGD(learning_rate=0.01)
    model.compile(optimizer=optimizer, loss='mean_squared_error')
    return model


def test_model(model, X_test_scaled, y_test):
    test_loss = model.evaluate(X_test_scaled, y_test, verbose=0)
    predictions = model.predict(X_test_scaled).flatten()
    
    mse = mean_squared_error(y_test, predictions)
    mae = mean_absolute_error(y_test, predictions)
    r2 = r2_score(y_test, predictions)
    
    return test_loss, mse, mae, r2, predictions


def analyze_results(X_test_scaled, y_test, predictions, feature_cols):
    results_df = pd.DataFrame(X_test_scaled, columns=feature_cols)
    results_df['Actual meanCPU'] = y_test
    results_df['Predicted meanCPU'] = predictions
    
    print(results_df.head(100))
    
    return results_df


def main():
    train_data_path = 'learningAllDoneDataset.csv'
    test_data_path = 'testAllDoneDataset.csv'
    feature_cols = ['vuser', 'pacing', 'requestSize', 'steps']
    target_col = 'meanCPU'

    X_train_scaled, y_train, X_test_scaled, y_test, scaler = load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col)
    
    model = create_model(len(feature_cols))
    

    model.fit(X_train_scaled, y_train, epochs=10, batch_size=32, verbose=1)  
    
    test_loss, mse, mae, r2, predictions = test_model(model, X_test_scaled, y_test)
    
    print(f'Test Loss: {test_loss}')
    print(f'Mean Squared Error (MSE): {mse}')
    print(f'Mean Absolute Error (MAE): {mae}')
    print(f'R^2 Score: {r2}')
    
    analyze_results(X_test_scaled, y_test, predictions, feature_cols)

if __name__ == "__main__":
    main()
