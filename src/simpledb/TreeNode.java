package src.simpledb;

import java.util.ArrayList;

public class TreeNode {
    private TreeNode parent = null; // For the root parent it is null, all other types it is equal to it's parent.
    private ArrayList<TreeNode> children; // Containing the list of all children's.
    private int key; // Containing the note's key value.
    private int childNumber = 0; // Containing how much children were born before him(with the same parent).
    private int depth; // The current depth we are at.

    TreeNode(int key, int depth, int childNumber) {
        setKey(key);
        setDepth(depth);
        setChildNumber(childNumber);
        children = new ArrayList<>();
    }

    TreeNode addChild(int childKey, int childDepth, int childNumber) {

        TreeNode child = new TreeNode(childKey, childDepth, childNumber);
        child.setParent(this);
        children.add(child);
        return child;
    }

    /**
     * @return parent of this node.
     */
    TreeNode up() {
        return parent;
    }

    /**
     * @return the first child of this node.
     */
    TreeNode down() {
        return children.get(0);
    }

    boolean hasNext() { // Also atEnd()
        return parent.children.size() - 1 != childNumber;
    }

    TreeNode next() {
        return parent.getChild(childNumber + 1);
    }

    int getKey() {
        return key;
    }

    int getDepth() {
        return depth;
    }

    int getAmountOfChildren() {
        return children.size();
    }

    int getBornIndexOfThisChild() {
        return childNumber;
    }

    TreeNode getChild(int numberOfChild) {
        return children.get(numberOfChild);
    }

    TreeNode getParent() {
        return parent;
    }

    private void setChildNumber(int childNumber) {
        this.childNumber = childNumber;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }

    private void setKey(int key) {
        this.key = key;
    }

    private void setParent(TreeNode parent) {
        this.parent = parent;
    }
}
