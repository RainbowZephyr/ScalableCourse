package CouchBase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

public class CouchBase {
    public static void main(String[] args) {
        Cluster cluster = CouchbaseCluster.create();
        cluster.authenticate("ahmed", "123456");
        Bucket bucket = cluster.openBucket("images");

        bucket.counter("id", 0, Long.MAX_VALUE);
        long id = bucket.counter("id", 1).content();
        System.out.println("ID "+ id);
        JsonObject user = JsonObject.empty()
                .put("firstname", "Walter")
                .put("lastname", "White")
                .put("job", "chemistry teacher")
                .put("age", 50);


        JsonDocument doc = JsonDocument.create(id +"" , user);
        JsonDocument response = bucket.upsert(doc);



        cluster.disconnect();
    }

}
