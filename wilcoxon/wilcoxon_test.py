import csv
from scipy.stats import wilcoxon

def load_accuracies(file_path):
    """Load accuracy values from a CSV file (assumes 4th column is test accuracy)."""
    accuracies = []
    with open(file_path, 'r') as f:
        reader = csv.reader(f)
        next(reader)  # Skip header row
        for row in reader:
            accuracies.append(float(row[3]))  # 4th column = Test Accuracy
    return accuracies

# Load data
gp_acc = load_accuracies('GP.csv')
mlp_acc = load_accuracies('MLP.csv')

# Run Wilcoxon test
statistic, p_value = wilcoxon(gp_acc, mlp_acc)

# Print results
print(f"Wilcoxon Statistic: {statistic}")
print(f"p-value: {p_value:.4f}")

if p_value < 0.05:
    print("✅ Significant difference (reject null hypothesis)")
else:
    print("❌ No significant difference")