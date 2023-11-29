package src.simpledb;

public class TreeRelation {
    private TreeNode currentNode; // Current node we are looking at.
    private int uid; // Unique identifier for each relation

    void setUid(int uid) {
        this.uid = uid;
    }

    TreeRelation(int[][] relArray, boolean debug) {
        if (debug) {
            System.out.println("------------------------ LOADING IN DATA ------------------------");
        }
        if (debug) {
            System.out.println("Add root =[-1]");
        }

        TreeNode rootNode = new TreeNode(-1, -1, 0);
        int j = relArray[0][0];

        if (debug) {
            System.out.println("Add parent =[" + relArray[0][0] + "]");
        }
        currentNode = rootNode.addChild(relArray[0][0], 0, 0);

        int parentCounter = 1;
        int childCounter = 0;

        for (int[] aRelArray : relArray) {
            if (j == aRelArray[0]) {
                if (debug) {
                    System.out.println("Add child [" + currentNode.getKey() + "][" + aRelArray[1] + "]");
                }
                this.currentNode.addChild(aRelArray[1], 1, childCounter);
                childCounter++;

            } else {
                if (debug) {
                    System.out.println("Add parent =[" + aRelArray[0] + "]");
                }

                currentNode = rootNode.addChild(aRelArray[0], 0, parentCounter);
                parentCounter++;
                j = aRelArray[0];

                if (debug) {
                    System.out.println("Add child [" + currentNode.getKey() + "][" + aRelArray[1] + "]");
                }

                childCounter = 0;
                this.currentNode.addChild(aRelArray[1], 1, childCounter);
                childCounter++;
            }
        }
        if (debug) {
            System.out.println("------------------------ FINISHED LOADING DATA ------------------------");
        }

        currentNode = rootNode;
    }

    public RelationIterator<Integer> iterator() {
        RelationIterator<Integer> relIt = new RelationIterator<Integer>() {

            private boolean atEnd = false; // Keeping track whether our iterator is at it's end.

            public int key() {
                return currentNode.getKey();
            }

            public boolean hasNext() {
                return currentNode.hasNext();
            }

            public Integer next() {
                if (currentNode.getBornIndexOfThisChild() + 1 >= currentNode.getParent().getAmountOfChildren()) {
                    atEnd = true;
                } else {
                    currentNode = currentNode.next();
                }
                return currentNode.getKey();
            }

            public void seek(int seekKey) {
                binarySearch(currentNode.getParent(), currentNode.getBornIndexOfThisChild(), seekKey);
            }

            public boolean atEnd() {
                return atEnd;
            }

            public int compareTo(RelationIterator<Integer> o) {
                return Integer.compare(this.key(), o.key());
            }

            public void open() {
                currentNode = currentNode.down();
            }

            public void up() {
                atEnd = false;
                TreeNode nextNode = currentNode.up();
                if (nextNode != null) {
                    currentNode = nextNode;
                } else {
                    System.out.println("We called up at a treeNode.. that shouldn't happen....");
                }
            }

            private void binarySearch(TreeNode parentNode, int minIndex, int searchValue) {
                int min = minIndex;
                int max = parentNode.getAmountOfChildren();
                while (max > min) {
                    int middle = (min + max) / 2;
                    if (parentNode.getChild(middle).getKey() == searchValue) {
                        max = middle;
                    }
                    if (parentNode.getChild(middle).getKey() < searchValue) {
                        min = middle + 1;
                    }
                    if (parentNode.getChild(middle).getKey() > searchValue) {
                        max = middle - 1;
                    }
                }
                if (min >= parentNode.getAmountOfChildren()) {
                    atEnd = true;
                    currentNode = parentNode.getChild(parentNode.getAmountOfChildren() - 1);
                } else {
                    if (parentNode.getChild(min).getKey() == searchValue) {
                        currentNode = parentNode.getChild(min);
                    }
                    if (parentNode.getChild(min).getKey() < searchValue) {
                        if (min + 1 == parentNode.getAmountOfChildren()) {
                            atEnd = true;
                            currentNode = parentNode.getChild(parentNode.getAmountOfChildren() - 1);
                        } else {
                            currentNode = parentNode.getChild(min + 1);
                        }
                    }
                    if (parentNode.getChild(min).getKey() > searchValue) {
                        currentNode = parentNode.getChild(min);
                    }
                }
            }

            public String getCurrentNodeInfo(TreeNode currentNode) {
                return "CurrentNode = [" + currentNode.getParent().getKey() + "][" + currentNode.getKey() + "] " +
                        " Depth = " + currentNode.getDepth() + " Parent depth = " + currentNode.getParent().getDepth();
            }

            public String debugString() {
                if (currentNode.getParent() != null) {
                    return "uid: " + uid + ", key: " + key() + ", depth: " + currentNode.getDepth() +
                            ", parentKey: " + currentNode.getParent().getKey() +
                            ", amount of children: " + currentNode.getAmountOfChildren() +
                            ", get amount of brothers: " + currentNode.getParent().getAmountOfChildren() +
                            ", compared to brothers at index: " + currentNode.getBornIndexOfThisChild();
                } else {
                    return "uid: " + uid + ", key: " + key() + ", depth: " + currentNode.getDepth() +
                            ", amount of children: " + currentNode.getAmountOfChildren() +
                            ", This is the root note...";
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        return relIt;
    }
}
