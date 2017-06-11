package unwrapp;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Shubham Mittal on 6/6/17.
 * Computes the Information Gain on the input feature matrix
 */
public class InfoGain {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(Constants.PACKAGE_PREFIX + Constants.FEATURE_FOLDER + Constants.FEATURE_MATRIX + Constants.ARFF_FORMAT));
        Instances data = new Instances(br);
        br.close();
        data.setClassIndex(data.numAttributes()-1);
        InfoGain infoGain = new InfoGain();
        AttributeSelection selector = infoGain.getAttributeSelector(data);
    }

    private AttributeSelection getAttributeSelector(Instances trainingData) throws Exception {
        AttributeSelection selector = new AttributeSelection();
        InfoGainAttributeEval evaluator = new InfoGainAttributeEval();
        Ranker ranker = new Ranker();
        ranker.setNumToSelect(-1);  // Set to default
        ranker.setGenerateRanking(true);
        ranker.setThreshold(0.00);  // Removing all attributes which do not contribute to IG
        selector.setEvaluator(evaluator);
        selector.setSearch(ranker);
        selector.SelectAttributes(trainingData);
        int[] indices = selector.selectedAttributes();
        /*for (int i : indices) {
            System.out.println(i + " : " + trainingData.attribute(i));
        }*/
        Instances trainingInstance = selector.reduceDimensionality(trainingData);
        System.out.println("TrainingInstances count: " + trainingInstance.numAttributes());
        int i=0;
        /*for (Instance instance : trainingInstance) {
            System.out.println(trainingInstance.attribute(i++));
        }*/

        //Write training instance arff to file
        Utils.writeInstanceToFile(trainingInstance, Constants.PACKAGE_PREFIX + Constants.FEATURE_FOLDER + Constants.TRIMMED_FEATURE_MATRIX + Constants.ARFF_FORMAT);

        Map<Attribute, Double> infogainscores = new HashMap<Attribute, Double>();
        for (i = 0; i < trainingData.numAttributes(); i++) {
            Attribute t_attr = trainingData.attribute(i);
            double infogain  = evaluator.evaluateAttribute(i);
            infogainscores.put(t_attr, infogain);
        }
        // Sort the hashmap based on values
        Stream<Entry<Attribute, Double>> sortedMap = infogainscores.entrySet().stream().sorted(Entry.<Attribute, Double>comparingByValue().reversed());

        // Information Gain score in descending order !
        LinkedHashMap<Attribute, Double> sortedHashMap = sortedMap.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Write Map to CSV
        Utils.writeMap2CSV(sortedHashMap, Constants.PACKAGE_PREFIX + Constants.FEATURE_FOLDER + Constants.TOP_FEATURES + Constants.CSV);
        return selector;
    }
}