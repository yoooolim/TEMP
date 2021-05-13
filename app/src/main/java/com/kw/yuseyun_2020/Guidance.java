package com.kw.yuseyun_2020;


public class Guidance {
    private String sentence;
    private int nodeID;
    private int direction;

    public Guidance (int nodeID, int direction, String sentence) {
        this.sentence = sentence;
        this.nodeID = nodeID;
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }


    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    @Override
    public String toString () {
        return "\"" + sentence + "\", nodeID: " + nodeID + ", direction: " + direction + " |";
    }
}
