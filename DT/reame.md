# Instructions

## Compile and build from source
Run in terminal in /DT directory

`javac *.java`

`jar cvfe a3_DT.jar Main *.class`

**Result:** jar file "a3_DT.jar"

## How to run the program

### Add data files 
Add the data files to the /DT folder. Ensure to follow the strict naming convention:
- "BTC_test.csv" for tests
- "BTC_train.csv" for training

### Run the program
Run the following command to run the program

`java -jar a3_DT.jar`

**Result:** 
- "result.csv" file in same directory
- Output in the terminal will look as follows:
  - Duration of training: XX ms
    - (how long the program ran)
  - Correctly classified: XX/YY
    - (XX = number of correctly classified stocks, YY = total number of stocks)
  - Accuracy: XX.XX%
    - (accuracy of the model)
