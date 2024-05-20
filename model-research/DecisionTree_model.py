import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.tree import DecisionTreeRegressor
from sklearn.utils import shuffle


def load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col):
  
    train_df = pd.read_csv(train_data_path)
    train_df = shuffle(train_df)
    
   
    test_df = pd.read_csv(test_data_path)
    
   
    X_train = train_df[feature_cols]
    y_train = train_df[target_col]
    
    X_test = test_df[feature_cols]
    y_test = test_df[target_col]
    
    return X_train, y_train, X_test, y_test


def train_decision_tree(X_train, y_train):
    model = DecisionTreeRegressor(
        max_depth=7,             
        min_samples_split=10,    
        min_samples_leaf=6,      
        random_state=42
    )
    model.fit(X_train, y_train)
    return model

def test_model(model, X_test, y_test):
    y_pred = model.predict(X_test)
    
    mse = mean_squared_error(y_test, y_pred)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    
    return mse, mae, r2, y_pred


def analyze_results(X_test, y_test, y_pred, feature_cols):
    results_df = pd.DataFrame(X_test, columns=feature_cols)
    results_df['Actual meanCPU'] = y_test
    results_df['Predicted meanCPU'] = y_pred
    
    pd.set_option('display.max_rows', None)  
    pd.set_option('display.max_columns', None)  
    
    print("\nПервые 10 строк таблицы сравнения актуальных и предсказанных значений:")
    print(results_df.head(10))
    
    return results_df


def plot_feature_importances(model, feature_cols):
    feature_importances = model.feature_importances_
    plt.barh(feature_cols, feature_importances)
    plt.xlabel("Важность признака")
    plt.ylabel("Признак")
    plt.title("Важность признаков для модели дерева решений")
    plt.show()

def main():
    train_data_path = 'ledokol.learningSet2.csv'
    test_data_path = 'ledokol.testSet2.csv'
    feature_cols = ['vuser', 'pacing', 'requestSize', 'steps']
    target_col = 'meanCPU'

    X_train, y_train, X_test, y_test = load_and_prepare_data(train_data_path, test_data_path, feature_cols, target_col)

    model = train_decision_tree(X_train, y_train)

    mse, mae, r2, y_pred = test_model(model, X_test, y_test)

    print("Результаты тестирования модели дерева решений:")
    print(f"Среднеквадратичная ошибка (MSE): {mse}")
    print(f"Средняя абсолютная ошибка (MAE): {mae}")
    print(f"Коэффициент детерминации (R^2 Score): {r2}")

    results_df = analyze_results(X_test, y_test, y_pred, feature_cols)

    plot_feature_importances(model, feature_cols)

if __name__ == "__main__":
    main()
