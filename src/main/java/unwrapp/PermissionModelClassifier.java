package unwrapp;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import java.io.*;
import java.util.Random;

/*
 * Created by Shubham Mittal on 6/7/17.
 */
public class PermissionModelClassifier {

    public static void main (String[] args) {

        ArffLoader arffLoader = new ArffLoader();

        try {
            BufferedReader br = new BufferedReader(new FileReader(Constants.PACKAGE_PREFIX + Constants.FEATURE_FOLDER + Constants.TRIMMED_FEATURE_MATRIX + Constants.ARFF_FORMAT));
            Instances data = new Instances(br);
            data.setClassIndex(data.numAttributes() - 1);

            int numFolds = 10; // k for k-fold validation

            // Randomize input data
            Random rand = new Random(1);    //considering the seed to be 1
            Instances randData = new Instances(data);
            randData.randomize(rand);
            randData.stratify(numFolds);    // Stratified Cross Validation

            Evaluation eval = new Evaluation(randData);
            for (int n = 0; n < numFolds; n++) {
                Instances train = randData.trainCV(numFolds, n);
                Instances test = randData.testCV(numFolds, n);

                // build and evaluate classifier

                NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
                nb.buildClassifier(train);
                eval.evaluateModel(nb, test);
            }

            // Write resuts to file
            StringBuilder results = new StringBuilder();
            results.append(eval.toSummaryString("Results\n======\n", true));
            results.append(Constants.NEWLINE_CHARACTER + eval.toClassDetailsString());
            results.append(Constants.NEWLINE_CHARACTER + eval.toMatrixString());
            System.out.println(results.toString());
            FileWriter writer = new FileWriter(Constants.PACKAGE_PREFIX + Constants.RESULTS + Constants.PERMISSION_RESULT_FILE);
            writer.append(results.toString());
            writer.flush();
            writer.close();

        } catch (Exception e) {
            System.out.println("Caught Exception ! Continuing !!");
            //e.printStackTrace();
        }
    }

}
