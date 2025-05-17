import numpy as np
import pandas as pd
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix

class StockMLP:
    def __init__(self, input_size, hidden_layers=[64, 32], seed=None):
        """
        Initialize MLP with configurable architecture
        Args:
            input_size: Number of input features
            hidden_layers: List specifying number of neurons in each hidden layer
            seed: Random seed for reproducibility
        """
        if seed is not None:
            np.random.seed(seed)
            
        self.layers = []
        layer_sizes = [input_size] + hidden_layers + [1]  # Input + Hidden + Output
        
        # Initialize weights and biases
        for i in range(len(layer_sizes)-1):
            # He initialization for ReLU
            w = np.random.randn(layer_sizes[i], layer_sizes[i+1]) * np.sqrt(2./layer_sizes[i])
            b = np.zeros((1, layer_sizes[i+1]))
            self.layers.append({'weights': w, 'biases': b})
    
    def relu(self, x):
        return np.maximum(0, x)
    
    def relu_derivative(self, x):
        return (x > 0).astype(float)
    
    def sigmoid(self, x):
        return 1 / (1 + np.exp(-x))
    
    def forward(self, X):
        """Forward pass through all layers"""
        self.activations = [X]
        self.layer_outputs = []
        
        # Hidden layers (ReLU activation)
        for i, layer in enumerate(self.layers[:-1]):
            z = np.dot(self.activations[-1], layer['weights']) + layer['biases']
            self.layer_outputs.append(z)
            a = self.relu(z)
            self.activations.append(a)
        
        # Output layer (Sigmoid activation)
        z = np.dot(self.activations[-1], self.layers[-1]['weights']) + self.layers[-1]['biases']
        a = self.sigmoid(z)
        self.activations.append(a)
        
        return a
    
    def backward(self, X, y, learning_rate):
        """Backpropagation algorithm"""
        m = X.shape[0]
        gradients = []
        
        # Output layer gradient
        error = self.activations[-1] - y
        delta = error * (self.activations[-1] * (1 - self.activations[-1]))  # Sigmoid derivative
        gradients.insert(0, delta)
        
        # Hidden layers gradients
        for i in reversed(range(len(self.layers)-1)):
            error = np.dot(gradients[0], self.layers[i+1]['weights'].T)
            delta = error * self.relu_derivative(self.layer_outputs[i])
            gradients.insert(0, delta)
        
        # Update weights and biases
        for i in range(len(self.layers)):
            dw = np.dot(self.activations[i].T, gradients[i]) / m
            db = np.sum(gradients[i], axis=0, keepdims=True) / m
            self.layers[i]['weights'] -= learning_rate * dw
            self.layers[i]['biases'] -= learning_rate * db
    
    def train(self, X_train, y_train, X_val=None, y_val=None, 
              epochs=1000, learning_rate=0.001, batch_size=32, verbose=True):
        """Train the model with optional validation"""
        for epoch in range(epochs):
            # Mini-batch training
            indices = np.random.permutation(X_train.shape[0])
            for i in range(0, X_train.shape[0], batch_size):
                batch_idx = indices[i:i+batch_size]
                X_batch = X_train[batch_idx]
                y_batch = y_train[batch_idx]
                
                self.forward(X_batch)
                self.backward(X_batch, y_batch, learning_rate)
            
            # Print training progress
            if verbose and (epoch % 100 == 0 or epoch == epochs-1):
                train_pred = self.predict(X_train)
                train_acc = accuracy_score(y_train, train_pred)
                msg = f"Epoch {epoch:4d}, Train Acc: {train_acc:.4f}"
                
                if X_val is not None:
                    val_pred = self.predict(X_val)
                    val_acc = accuracy_score(y_val, val_pred)
                    msg += f", Val Acc: {val_acc:.4f}"
                
                print(msg)
    
    def predict(self, X, threshold=0.5):
        """Make binary predictions"""
        return (self.forward(X) > threshold).astype(int)
    
    def evaluate(self, X, y):
        """Evaluate model performance"""
        y_pred = self.predict(X)
        
        print("\nClassification Report:")
        print(classification_report(y, y_pred))
        
        print("Confusion Matrix:")
        print(confusion_matrix(y, y_pred))
        
        accuracy = accuracy_score(y, y_pred)
        print(f"\nAccuracy: {accuracy:.4f}")
        return accuracy

def preprocess_data(filepath):
    """Load and preprocess stock data"""
    df = pd.read_csv(filepath)
    
    # Feature engineering could be added here
    X = df[['Open', 'High', 'Low', 'Close', 'Adj Close']].values
    y = df['Output'].values.reshape(-1, 1)
    
    # Normalize features
    mean = np.mean(X, axis=0)
    std = np.std(X, axis=0)
    X = (X - mean) / std
    
    return X, y, mean, std

def main():
    print("Stock Purchase Classifier using MLP")
    
    try:
        # Get user inputs
        seed = int(input("Enter random seed for reproducibility: "))
        train_path = input("Enter path to training data CSV: ")
        test_path = input("Enter path to test data CSV: ")
        
        # Load and preprocess data
        X_train, y_train, train_mean, train_std = preprocess_data(train_path)
        X_test, y_test, _, _ = preprocess_data(test_path)
        X_test = (X_test - train_mean) / train_std  # Use training stats for normalization
        
        # Initialize MLP
        mlp = StockMLP(
            input_size=X_train.shape[1],
            hidden_layers=[64, 32],  # Two hidden layers with 64 and 32 neurons
            seed=seed
        )
        
        # Train the model
        print("\nTraining MLP model...")
        mlp.train(
            X_train, y_train,
            X_val=X_test, y_val=y_test,
            epochs=1000,
            learning_rate=0.001,
            batch_size=64
        )
        
        # Evaluate on test set
        print("\nFinal Test Set Evaluation:")
        mlp.evaluate(X_test, y_test)
        
    except Exception as e:
        print(f"\nError: {str(e)}")
        print("Please check your input files and try again.")

if __name__ == "__main__":
    main()