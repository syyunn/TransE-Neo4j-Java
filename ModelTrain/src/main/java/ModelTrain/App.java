/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package ModelTrain;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.util.*;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        String neo4jFolder="C:\\Users\\D-blue\\Desktop\\Graph_Database\\hw6\\db\\FB13";
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("C:\\Users\\D-blue\\Desktop\\Graph_Database\\hw6\\db\\FB13"));
        Transaction tx=db.beginTx();
        Result rs;
        rs=db.execute("match ()-[p]->() where p.split='Validation' with rand() as r, p order by r skip 25 delete p");
        rs= db.execute("match ()-[p]->() return count(p), p.split");
        while(rs.hasNext()){
            System.out.println(rs.next());
        }


        tx.close();
        db.shutdown();

    }
    static double[] normalize(double[] vector){
        double l2_distance = Math.sqrt(Arrays.stream(vector).map(item -> Math.pow(item, 2)).sum());
        vector=Arrays.stream(vector).map(item->item/l2_distance).toArray();
        return vector;
    }
    static double[] initialize(double[] embedding,Random rand,double min,double max){
        // -6/sqrt(dim)<init<6/sqrt(dim)
        embedding=Arrays.stream(embedding).map(item->min+(max-min)*rand.nextDouble()).toArray();
        return embedding;
    }
    public static String loadFromFile(String neo4jFolder, String inputFile) throws Exception {
    String norm = null;
    Scanner sc = new Scanner(new File(inputFile));
    // Read norm.
    norm = sc.nextLine();
    // Skip entities header.
    sc.nextLine();

    // Load entity embeddings.
    BatchInserter inserter = BatchInserters.inserter(new File(neo4jFolder));
    while (sc.hasNextLine()) {
        String line = sc.nextLine();
        if (line.equals("Predicates"))
            break;
        String[] idAndEmbed = line.split("\t");
        inserter.setNodeProperty(Long.valueOf(idAndEmbed[0]), "embedding",
                Arrays.stream(idAndEmbed[1].substring(1, idAndEmbed[1].length()-1).split(", ")).mapToDouble(x->Double.valueOf(x)).toArray());
        System.out.println(inserter.getNodeProperties(Long.valueOf(idAndEmbed[0])));
    }

    inserter.shutdown();

    // Load predicate embeddings.
    GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(neo4jFolder));
    Transaction tx = db.beginTx();
    while (sc.hasNextLine()) {
        String[] predAndEmbed = sc.nextLine().split("\t");
        long relid = (long) db.execute("MATCH ()-[p:`" + predAndEmbed[0] + "`]->() RETURN MIN(id(p)) AS min").next().get("min");
        db.getRelationshipById(relid).setProperty("embedding",
                Arrays.stream(predAndEmbed[1].substring(1, predAndEmbed[1].length()-1).split(", ")).mapToDouble(x->Double.valueOf(x)).toArray());
    }
//        Result rs=db.execute("match (e) return e.properties");
//        while(rs.hasNext()){
//            System.out.println(rs.next());
//        }
    tx.success();
    tx.close();
    db.shutdown();
    sc.close();

    return norm;
}}