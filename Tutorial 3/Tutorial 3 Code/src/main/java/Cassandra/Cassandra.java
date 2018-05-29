package Cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.List;
import java.util.stream.Collectors;

public class Cassandra {

    public static void main(String[] args) {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
            Session session = cluster.connect();
            ResultSet rs = session.execute("select release_version from system.local");
            Row row = rs.one();
            System.out.println(row.getString("release_version"));

            String keySpace = "sensors";
            String replicationStrategy = "SimpleStrategy";
            String replicationFactor = "1";

            StringBuilder sb =
                    new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                            .append(keySpace).append(" WITH replication = {")
                            .append("'class':'").append(replicationStrategy)
                            .append("','replication_factor':").append(replicationFactor)
                            .append("};");

            String query = sb.toString();
            session.execute(query);

            session.execute("USE " + keySpace + ";");

            String TABLE_NAME = "temperature";
            sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                    .append(keySpace + "." + TABLE_NAME).append("(")
                    .append("id int PRIMARY KEY, ")
                    .append("reading double")
                    .append(");");

            session.execute(sb.toString());

            ResultSet result = session.execute("SELECT * FROM " + keySpace + "." + TABLE_NAME + ";");

            List<String> columnNames = result.getColumnDefinitions().asList().stream().map(cl -> cl.getName()).collect(Collectors.toList());
            System.out.println(columnNames);

            sb = new StringBuilder("INSERT INTO ")
                    .append(keySpace)
                    .append(".")
                    .append(TABLE_NAME).append("(id,reading)")
                    .append(" VALUES(").append("2").append(",").append("2.8")
                    .append(");");

            session.execute(sb.toString());
        } finally {
            if (cluster != null) cluster.close();
        }
    }
}
