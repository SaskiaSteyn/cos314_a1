# Instructions

## Compile and build from source
Run in terminal in /GeneticProgramming directory

`javac *.java`

`jar cvfe a3_GP.jar Main *.class`

**Result:** jar file "a3_GP.jar"

## How to run the program

### Add data files
Add the data files to the /GeneticProgramming folder. Ensure to follow the strict naming convention:
- "BTC_test.csv" for tests
- "BTC_train.csv" for training

### Run the program
Run the following command to run the program

`java -jar a3_GP.jar`

**Result:**
- "GP.csv" file in same directory
- Output in the terminal will look as follows:
  ```
  Run 1 completed - Test Accuracy: 99.62%
  Run 2 completed - Test Accuracy: 100.00%
  Run 3 completed - Test Accuracy: 99.62%
  Run 4 completed - Test Accuracy: 100.00%
  Run 5 completed - Test Accuracy: 100.00%
  Run 6 completed - Test Accuracy: 93.92%
  Run 7 completed - Test Accuracy: 99.62%
  Run 8 completed - Test Accuracy: 100.00%
  Run 9 completed - Test Accuracy: 100.00%
  Run 10 completed - Test Accuracy: 99.62%
  
  All 10 runs completed. Results saved to: ./GP.csv
```