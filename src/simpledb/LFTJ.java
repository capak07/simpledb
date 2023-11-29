package src.simpledb;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;

public class LFTJ {
    int debug = 0; // Represents the amount of debugging. 0 = None, 3 = Extreme

    ArrayList<ArrayList<Integer>> result; // Result set: array with tuples
    ArrayList<ArrayList<RelationIterator<Integer>>> iteratorPerDepth; // Contains the iterator for each dept
    ArrayList<Integer> currentTuple = new ArrayList<>(); // Contains the key values of all parents
    int p = 0; // Current iterator pointer
    int numIters; // Number of iterators at this depth
    int depth = -1; // Current depth
    int maxDepth = 0; // How deep is our relation? R(x,y), T(y,z) yields 2.
    int key = 0; // Contains the key value when we find a matching search in leapfrogSearch.
    boolean atEnd; // Mirrors whether there is an iterator at his end.

    public enum CycleOrPathsEnum {
        CYCLE, PATH
    }

    long startTime, midTime, endTime; // Information about when the round started and finished.
    String resultCycleOrPath, resultAmountOfCycleorPath;

    public LFTJ(String dataSetPath, Enum CycleOrPaths, int amountOfPathOrCycle) throws IOException {
        startTime = System.nanoTime(); // Getting the time at the start
        // initDataSets("./data/test.txt", CycleOrPathsEnum.CYCLE, 7);
        if (CycleOrPaths == CycleOrPathsEnum.CYCLE) { // Used for printing results.
            resultCycleOrPath = "Cycle";
        } else {
            resultCycleOrPath = "Path";
        }
        resultAmountOfCycleorPath = String.valueOf(amountOfPathOrCycle);

        initDataSets(dataSetPath, CycleOrPaths, amountOfPathOrCycle);

        result = new ArrayList<>(); // create an array that will hold the results

        midTime = System.nanoTime(); // Getting the time it took to initialize
    }

    public void initDataSets(String fileName, Enum CycleOrRounds, int amountOfPathOrCycle) throws IOException {
        // for a cycle we have as many relations as amountOfPathOrCycle, i.e. a 4-cycle
        // query gives 4 relations
        int amountOfRelations = amountOfPathOrCycle;
        // for a path we have one less, i.e. a 4-path query gives 3 relations
        if (CycleOrRounds == CycleOrPathsEnum.PATH) {
            amountOfRelations--;
        }
        ArrayList<RelationIterator<Integer>> relIts = new ArrayList<>();
        int i;
        for (i = 1; i <= amountOfRelations; i++) {
            DataImporter di;
            if (CycleOrRounds == CycleOrPathsEnum.CYCLE && i == amountOfRelations) {
                di = new DataImporter(fileName, true, debug > 1);
            } else {
                di = new DataImporter(fileName, false, debug > 1);
            }
            TreeRelation rel = di.getRelArray();
            rel.setUid(i);
            RelationIterator<Integer> relIterator = rel.iterator();
            relIts.add(relIterator);
        }

        maxDepth = amountOfPathOrCycle - 1;

        iteratorPerDepth = new ArrayList<>();
        for (int j = 0; j <= maxDepth; j++) {
            ArrayList<RelationIterator<Integer>> intermedAListForIterators = new ArrayList<>();

            int a = Math.max(0, j - 1);
            int b = Math.min(j, maxDepth - 1);
            for (int k = a; k <= b; k++) {
                intermedAListForIterators.add(relIts.get(k));
            }

            if ((CycleOrRounds == CycleOrPathsEnum.CYCLE) && (j == 0 || j == maxDepth)) {
                intermedAListForIterators.add(relIts.get(maxDepth));
            }

            iteratorPerDepth.add(intermedAListForIterators);

        }

    }

    public void multiJoin() {
        leapfrogOpen();

        while (true) { // This is our search function
            if (debug >= 2) {
                printDebugInfo("A - Continue with the true loop");
            }

            if (atEnd) { // If we did not find the specific value we were looking for and an iterator is
                // at an end.
                if (debug >= 2) {
                    printDebugInfo("B2 - We got one iterator that is at an end");
                }

                if (depth == 0) { // We stop because we are all the way at the end
                    break;
                }
                // We continue the search. At this depth we were at the end, so we go one up and
                // go to the next value.
                leapfrogUp();
                leapfrogNext();
                if (debug >= 2) {
                    printDebugInfo("B3 - Executed leapfrogUp and leapfrogNext");
                }

            } else { // No iterator is at it's end.
                if (depth == maxDepth) {// We got a winner
                    if (debug >= 2) {
                        printDebugInfo("C1 - We got a winner");
                    }
                    ArrayList<Integer> tuple = new ArrayList<>();
                    currentTuple.add(key);
                    tuple.addAll(currentTuple);
                    result.add(tuple);
                    tuple = null;
                    currentTuple.remove(currentTuple.size() - 1);

                    key = -1;

                    if (debug >= 1) {
                        System.out.println(result);
                    }
                    leapfrogNext();

                } else {// We can still go level deeper.
                    if (debug >= 2) {
                        System.out.println("C2 - Depth -> Level down");
                    }
                    leapfrogOpen();
                }
            }
        }

        System.out.println("Number of results: " + result.size());
        System.out.println(result);

        endTime = System.nanoTime();
        printResults();

    }

    public void leapfrogInit() {
        // Checking if any iterator is empty return (empty) result array
        for (RelationIterator<Integer> relIt : iteratorPerDepth.get(depth)) {
            if (relIt.atEnd()) {
                atEnd = true;
                return;
            }
        }

        if (!atEnd) {
            atEnd = false;
            Collections.sort(iteratorPerDepth.get(depth));
            p = 0;
            leapfrogSearch();
        }
    }

    public void leapfrogSearch() {
        int maxKeyIndex = ((p - 1) % numIters) + ((p - 1) < 0 ? numIters : 0);
        // int maxKeyIndex = numIters-1;
        maxKeyIndex = numIters == 1 ? 0 : maxKeyIndex; // Special case where maxKeyIndex = 1 while numIters = 1
        int maxKey = iteratorPerDepth.get(depth).get(maxKeyIndex).key();

        while (true) {
            int minKey = iteratorPerDepth.get(depth).get(p).key();

            if (debug >= 2) {
                System.out
                        .println("--- Searching --- Depth: " + depth + ", MaxKeyIndex: " + maxKeyIndex + ", NumIters: "
                                + numIters + ", maxKey: " + maxKey + ", minKey: " + minKey);
            }
            if (debug >= 2) {
                System.out.println("curIt values: " + iteratorPerDepth.get(depth).get(p).debugString());
            }

            if (maxKey == minKey) {
                if (debug >= 2) {
                    System.out.println("Found key = " + minKey);
                }

                key = minKey;
                return;
            } else { // If no common key is found, update pointer of iterator
                if (debug >= 2) {
                    System.out.println("Key not equal, Searching for " + maxKey + " with minkey " + minKey);
                }

                if (debug >= 2) {
                    System.out.println("Seek with: " + iteratorPerDepth.get(depth).get(p).debugString());
                }

                iteratorPerDepth.get(depth).get(p).seek(maxKey);
                if (iteratorPerDepth.get(depth).get(p).atEnd()) { // The maxKey is not found
                    if (debug >= 2) {
                        System.out.println("key = -1");
                    }
                    atEnd = true;
                    return;
                } else { // The maxKey is found and thus we check if the next iterator can also find it
                    maxKey = iteratorPerDepth.get(depth).get(p).key();
                    p = (p + 1) % numIters;
                }
            }
        }
    }

    public void leapfrogNext() {
        atEnd = false;
        iteratorPerDepth.get(depth).get(p).next();
        if (iteratorPerDepth.get(depth).get(p).atEnd()) {
            atEnd = true;
        } else {
            p = (p + 1) % numIters;
            leapfrogInit();
        }
    }

    public void leapfrogSeek(int seekKey) {
        iteratorPerDepth.get(depth).get(p).seek(seekKey);
        if (iteratorPerDepth.get(depth).get(p).atEnd()) {
            atEnd = true;
        } else {
            p = (p + 1) % numIters;
            leapfrogSearch();
        }
    }

    public void leapfrogOpen() {
        if (depth > -1) { // Used to be able to report the currentTuple easily.
            currentTuple.add(iteratorPerDepth.get(depth).get(0).key());
        }
        depth = depth + 1;
        updateIterPandNumIters();
        for (RelationIterator relIt : iteratorPerDepth.get(depth)) {
            relIt.open();
        }
        leapfrogInit();
    }

    public void leapfrogUp() {
        if (depth > 0) {
            currentTuple.remove(currentTuple.size() - 1);
        }
        for (RelationIterator relIt : iteratorPerDepth.get(depth)) {
            relIt.up();
        }
        depth = depth - 1;
        updateIterPandNumIters();
    }

    void updateIterPandNumIters() {
        numIters = iteratorPerDepth.get(depth).size();
        if (numIters <= p) {
            p = 0;
        }
    }

    private void printResults() {
        System.out.println("No caching" + "\t" +
                resultCycleOrPath + "\t" +
                resultAmountOfCycleorPath + "\t" +
                (midTime - startTime) / 1000000 + "\t" +
                (endTime - midTime) / 1000000 + "\t" +
                (endTime - startTime) / 1000000 + "\t" +
                result.size() + "\t");
    }

    void printDebugInfo(String message) {
        if (message.length() >= 1) {
            System.out.println("Message: " + message);
        }
        if (debug >= 3) {
            if (depth > maxDepth) {
                System.out.println(
                        "Our depth is " + depth + " while our maxDepth is " + maxDepth + " hence no debuginfo");
            } else {
                for (int i = 0; i < iteratorPerDepth.get(depth).size(); i++) {
                    System.out.println("Info of iterator " + Integer.toString(i) + ": " +
                            iteratorPerDepth.get(depth).get(i).debugString());
                }
            }
        }
    }

    static void printRunningTimes(long startTime, long midTime, long endTime) {
        long interTime = (midTime - startTime) / 1000000;
        System.out.println("Time to load the data: " + interTime + " ms");
        interTime = (endTime - midTime) / 1000000;
        System.out.println("Time to execute the algorithm: " + interTime + " ms");
        interTime = (endTime - startTime) / 1000000;
        System.out.println("Time to execute both: " + interTime + " ms");
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        LFTJ lftj = null;
        try {
            lftj = new LFTJ("simpledb\\src\\simpledb\\test.txt",
                    CycleOrPathsEnum.PATH, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create a LFTJ with cache, load the datasets and ready to rumble
        long midTime = System.nanoTime();
        lftj.multiJoin(); // We start the joins and count the cache
        long endTime = System.nanoTime();
        printRunningTimes(startTime, midTime, endTime);
    }
}
