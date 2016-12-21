import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is used for giving the user based prediction on the trained data. 
 * This class uses User-Based Collaborative Filtering Algorithm to give prediction for the user with 0 ratings.
 * Its uses Pearson's Correlation Coefficient algorithm to calculate user similarities and Weigted Sum to give predition 
 * for user.
 * @author  Ashish Bhumkar
 */
public class RecommenderSystem {
	
	private final int iUser = 943;
	private final int iItem = 1682;
	private final String sSplitValue = " +";
	private int[][] arrMatrix = new int[iUser+1][iItem+1];
	private int[][] arrOutputMatrix = new int[iUser+1][iItem+1];
	private int[] arrSimilarUser = new int[iUser+1];
	Map<Integer, HashMap<Integer, Double>> mapCoeffient = new HashMap<Integer, HashMap<Integer,Double>>();
	
	/**
	 * This function initialize the Similarity Matrix with 0. 
	 */
	public void initializeSimilarUserArray() {
		for(int i = 1; i <= iUser; i++) {
			arrSimilarUser[i] = 0;
		}
	}
	
	/**
	 * This function gives prediction to user's with rating 0 based on algorithm.
	 * It computes weighted sum algorithm on similar user's tastes to compute the predicted value.
	 * @param iUserId The user who's to give recommendation
	 * @param lst List of other similar users
	 */
	public void recommend(int iUserId, List<Integer> lst) {
		for(int i = 1; i <= iItem; i++) {
			if(arrMatrix[iUserId][i] == 0) {
				double dblValue = 0.0;
				double dblDemo = 0.0;
				for(int j : lst) {
					if(arrMatrix[j][i] == 0)
						continue;					
					dblValue += arrMatrix[j][i] * mapCoeffient.get(iUserId).get(j);
					dblDemo += Math.abs(mapCoeffient.get(iUserId).get(j));
				}
				double dblSimilar = dblValue/dblDemo;
				dblSimilar = Math.round(dblSimilar);
				if(dblSimilar < 1)
					dblSimilar = 1;
				else if(dblSimilar > 5)
					dblSimilar = 5;
				arrOutputMatrix[iUserId][i] = (int) dblSimilar;
			} else {
				arrOutputMatrix[iUserId][i] = arrMatrix[iUserId][i];
			}
		}
	}
	
	/**
	 * This function writes the matrix to output file.
	 * @param fDataSet File to write
	 */
	public void writeData(File fDataSet) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fDataSet));		
			for(int i = 1; i <= iUser; i++) {
				for(int j = 1; j <= iItem; j++) {			    
					    writer.write(i + " " + j + " " + arrOutputMatrix[i][j] + "\n");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}				
	
	/**
	 * This function is used to read the input file and stores the values in memory.
	 * @param fDataSet File to read
	 */
	public void setMatrixData(File fDataSet) {
		try (BufferedReader br = new BufferedReader(new FileReader(fDataSet))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] arrSplit = new String[3];
		    	arrSplit = line.split(sSplitValue);
		    	arrMatrix[Integer.parseInt(arrSplit[0])][Integer.parseInt(arrSplit[1])] = Integer.parseInt(arrSplit[2]);
		    }
		} catch(FileNotFoundException e) {} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * This function implements user similarity algorithm on each pair of users and compute Pearson's Correlation Coeffient.
	 * @param objNearestNNeighbor Object that stores Users with similar tastes
	 */
	public void implementUserSimilarities(NearestNNeighbor objNearestNNeighbor) {
		for(int i = 1; i <= iUser; i++) {
			List<Integer> lstSimilarNearestNeighbor = new ArrayList<Integer>();
			HashMap<Integer, Double> mapUser = new HashMap<Integer, Double>();
			for(int j = 1; j <= iUser; j++) {
				if(i == j)
					continue;
				double dUserSimilarCoefficient = 0.0;
				Pearson objPearson = new Pearson();
				dUserSimilarCoefficient = objPearson.GetCorrelation(arrMatrix[i], arrMatrix[j]);
				//if(dUserSimilarCoefficient > 0.2) {
					lstSimilarNearestNeighbor.add(j);
				//}
				mapUser.put(j, dUserSimilarCoefficient);
			}
			mapCoeffient.put(i, mapUser);
			objNearestNNeighbor.arrNearestUser.put(i, lstSimilarNearestNeighbor);
		}
	}
	
	/**
	 * This function sorted the HashMap on values 
	 * @param passedMap HashMap of the similar user's list
	 * @return sortedMap the sorted LinkedHashMap on values with descending order.
	 */
	public LinkedHashMap<Integer, Double> sortHashMapByValuesD(HashMap passedMap) {
		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys, Collections.reverseOrder());
		LinkedHashMap sortedMap = new LinkedHashMap();
		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();

				if (comp1.equals(comp2)) {
	               passedMap.remove(key);
	               mapKeys.remove(key);
	               sortedMap.put((Integer)key, (Double)val);
	               break;
	           }
			}
		}
		return sortedMap;
	}
	
	/**
	 * Main function to compute
	 * @param args arguments
	 */
	public static void main(String args[]) {
		final long startTime = System.currentTimeMillis();
		File fInputDataSet = new File("train_all_txt.txt");
		File fOutputDataSet = new File("Output.txt");
		RecommenderSystem objRecommenderSystem = new RecommenderSystem();
		objRecommenderSystem.setMatrixData(fInputDataSet);		
		NearestNNeighbor objNearestNNeighbor = new NearestNNeighbor();
		objRecommenderSystem.implementUserSimilarities(objNearestNNeighbor);
		for(int i = 1; i <= objRecommenderSystem.iUser; i++) {
			objRecommenderSystem.recommend(i, objNearestNNeighbor.arrNearestUser.get(i));
		}
		objRecommenderSystem.writeData(fOutputDataSet);
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime)/1000 + "seconds" );
	}
}
