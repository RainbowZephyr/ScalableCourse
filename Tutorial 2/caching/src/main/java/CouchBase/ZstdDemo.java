package CouchBase;

import com.github.luben.zstd.Zstd;

import java.io.*;

public class ZstdDemo {
    public static void main(String[] args) {
        try {
            File file = new File("random.txt");
            InputStream i = new FileInputStream(file);

            byte fileContent[] = new byte[(int)file.length()];
            i.read(fileContent);

            byte[] compressed = Zstd.compress(fileContent);

            File compressedFile = new File("random.zst");
            FileOutputStream o = new FileOutputStream(compressedFile);
            o.write(compressed);

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
