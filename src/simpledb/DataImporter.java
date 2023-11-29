package src.simpledb;

import java.io.*;
import java.util.*;

public class DataImporter {
    private BufferedReader br;
    private boolean reverseOrder;
    private boolean debug;

    DataImporter(String fileName, boolean reverseOrder, boolean debug) throws IOException {
        this.reverseOrder = reverseOrder;
        this.debug = debug;
        try {
            File file = new File(fileName);
            br = new BufferedReader((new FileReader(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    TreeRelation getRelArray() throws IOException {
        List<List<Integer>> relArrayList = new ArrayList<>();

        while (br.ready()) {
            String line = br.readLine();
            if (line.charAt(0) != '#') {
                String[] split = line.split("\\s+");
                int fromEdge;
                int toEdge;
                if (!reverseOrder) {
                    fromEdge = Integer.parseInt(split[0]);
                    toEdge = Integer.parseInt(split[1]);
                } else {
                    fromEdge = Integer.parseInt(split[1]);
                    toEdge = Integer.parseInt(split[0]);
                }
                relArrayList.add(Arrays.asList(fromEdge, toEdge));
            }
        }

        relArrayList = sortRelArray(relArrayList);

        int[][] relArray = new int[relArrayList.size()][];
        for (int i = 0; i < relArrayList.size(); i++) {
            relArray[i] = new int[relArrayList.get(i).size()];
            for (int j = 0; j < relArrayList.get(i).size(); j++) {
                relArray[i][j] = relArrayList.get(i).get(j);
            }
        }

        return new TreeRelation(relArray, false);
    }

    private List<List<Integer>> sortRelArray(List<List<Integer>> relArrayList) {
        Collections.sort(relArrayList, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> l1, List<Integer> l2) {
                for (int i = 0; i < l1.size(); i++) {
                    // If the a1 element compared at index i is smaller, return -1
                    if (l1.get(i) < l2.get(i)) {
                        return -1;
                    }
                    // If the a1 element compared at index 1 is larger, return 1
                    else if (l1.get(i) > l2.get(i)) {
                        return 1;
                    }
                }
                return 0;
            }
        });
        return relArrayList;
    }
}
