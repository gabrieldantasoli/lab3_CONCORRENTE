import java.io.*;
import java.util.*;

public class FileSimilarity {

    public static void main(String[] args) throws Exception {
        System.out.println("CONMCORRENTE");
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();
        ArrayList<SumThread> sumThreads = new ArrayList<>();
        ArrayList<CompareThread> compareThreads = new ArrayList<>();

        // Calculate the fingerprint for each file
        for (String path : args) {
            SumThread sumThread  = new SumThread(path);
            sumThreads.add(sumThread);
            sumThreads.get(sumThreads.size()-1).start();
        }

        for (int i = 0; i < sumThreads.size(); i++) {
            sumThreads.get(i).join();
            fileFingerprints.put(args[i], sumThreads.get(i).getChunks());
        }


        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {

            // criar n trheads
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                CompareThread compareThread = new CompareThread(fingerprint1, fingerprint2, file1, file2);
                compareThread.start();

            }

            compareThreads = new ArrayList<>();
        }
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    static class SumThread extends Thread {
        private String filePath;
        private List<Long> chunks;

        public SumThread(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            File file = new File(filePath);
            this.chunks = new ArrayList<>();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[100];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    long sum = sum(buffer, bytesRead);
                    this.chunks.add(sum);
                }
            } catch (IOException e) {
                
            }
        }

        public List<Long> getChunks() {
            return this.chunks;
        }
    }

    static class CompareThread extends Thread {
        private List<Long> file1;
        private List<Long> file2;
        private String path1;
        private String path2;

        public CompareThread(List<Long> file1, List<Long> file2, String path1, String path2) {
            this.file1 = file1;
            this.file2 = file2;
            this.path1 = path1;
            this.path2 = path2;
        }

        @Override
        public void run() {
            int counter = 0;
            List<Long> targetCopy = new ArrayList<>(file2);

            for (Long value : file1) {
                if (targetCopy.contains(value)) {
                    counter++;
                    targetCopy.remove(value);
                }
            }

            System.out.println("Similarity between " + path1 + " and " + path2 + ": " + ((float) counter / file1.size() * 100) + "%");
        }
    }
}
