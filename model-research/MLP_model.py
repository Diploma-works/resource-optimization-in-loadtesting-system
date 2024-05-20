import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from tensorflow.keras import layers, callbacks
from sklearn.utils import shuffle
import numpy as np

def load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col):
    train_df = pd.read_csv(train_data_path)
    train_df = shuffle(train_df)
    X = train_df[feature_cols]
    y = train_df[target_col]

    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

    test_df = pd.read_csv(test_data_path)
    X_test = test_df[feature_cols]
    y_test = test_df[target_col]

    return X_train, X_val, y_train, y_val, X_test, y_test

def create_model(input_shape, custom_loss):
    model = tf.keras.Sequential([
        layers.Dense(64, activation='relu', input_shape=(input_shape,)),
        layers.Dense(64, activation='relu'),
        layers.Dense(64, activation='relu'),
        layers.Dense(1)
    ])
    model.compile(optimizer='adam', loss=custom_loss)
    return model


def create_custom_loss(L, max_percentage):
    mse_loss = tf.keras.losses.MeanSquaredError()
    limit_offset = max_percentage * L

    def custom_loss(y_true, y_pred):
        mse = mse_loss(y_true, y_pred)
        relative_errors = tf.abs((y_pred - y_true) / y_true)
        penalty = tf.maximum(relative_errors - (limit_offset / tf.reduce_mean(y_true)), 0.0)
        penalty = tf.reduce_mean(penalty)
        return mse + penalty

    return custom_loss


def train_model(model, X_train, y_train, X_val, y_val):
    early_stopping = callbacks.EarlyStopping(
        min_delta=0.000001,
        patience=300,
        restore_best_weights=True,
    )
    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=1200,
        batch_size=64,
        callbacks=[early_stopping],
        verbose=1
    )
    return history


def test_model(model, X_test, y_test):
    test_loss = model.evaluate(X_test, y_test, verbose=0)
    predictions = model.predict(X_test).flatten()

    mse = mean_squared_error(y_test, predictions)
    mae = mean_absolute_error(y_test, predictions)
    r2 = r2_score(y_test, predictions)
    mspe = mean_squared_percentage_error(y_test, predictions)
    rmspe = root_mean_squared_percentage_error(y_test, predictions)

    return test_loss, mse, mae, r2, mspe, rmspe, predictions


def mean_squared_percentage_error(y_true, y_pred):
    return np.mean(((y_true - y_pred) / y_true) ** 2) * 100

def root_mean_squared_percentage_error(y_true, y_pred):
    return np.sqrt(mean_squared_percentage_error(y_true, y_pred))


def analyze_results(X_test, y_test, predictions, feature_cols):
    results_df = pd.DataFrame(X_test, columns=feature_cols)
    results_df['Actual maxCPU'] = y_test
    results_df['Predicted maxCPU'] = predictions

    max_index = results_df['Actual maxCPU'].idxmax()
    min_index = results_df['Actual maxCPU'].idxmin()

    max_row = results_df.loc[max_index]
    min_row = results_df.loc[min_index]

    print("\nRow with Maximum Actual maxCPU:")
    print(max_row)

    print("\nRow with Minimum Actual maxCPU:")
    print(min_row)

    pd.set_option('display.max_rows', None)
    print(results_df.iloc[::7])

    return results_df


def main():
    train_data_path = 'ledokol.learningSet2.csv'
    test_data_path = 'ledokol.testSet2.csv'
    feature_cols = ['vuser', 'pacing', 'requestSize', 'steps']
    target_col = 'maxCPU'

    X_train, X_val, y_train, y_val, X_test, y_test = load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col)

    L = 300
    max_percentage = 0.05
    custom_loss = create_custom_loss(L, max_percentage)

    model = create_model(len(feature_cols), custom_loss)

    train_model(model, X_train, y_train, X_val, y_val)

    test_loss, mse, mae, r2, mspe, rmspe, predictions = test_model(model, X_test, y_test)

    print(f'Test Loss: {test_loss}')
    print(f'Mean Squared Error (MSE): {mse}')
    print(f'Mean Absolute Error (MAE): {mae}')
    print(f'R^2 Score: {r2}')
    print(f'Mean Squared Percentage Error (MSPE): {mspe}')
    print(f'Root Mean Squared Percentage Error (RMSPE): {rmspe}')

    analyze_results(X_test, y_test, predictions, feature_cols)

if __name__ == "__main__":
    main()
