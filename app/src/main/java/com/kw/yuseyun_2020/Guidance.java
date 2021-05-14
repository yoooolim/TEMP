package com.kw.yuseyun_2020;


public class Guidance {
    private String sentence;
    private int nodeID;
    private int direction;
    private String index; // 출발, 도착, 1, 2, 3, 4 .. 저장
    private boolean passed;

    public String getSpeechSentence() {
        return speechSentence;
    }

    private String speechSentence;

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Guidance (int nodeID, int direction, String sentence) {
        this.sentence = sentence;
        this.nodeID = nodeID;
        this.direction = direction;
        if (direction == -2) {
            this.speechSentence = "경로 안내를 시작합니다.";
        } else if (direction == -3) {
            this.speechSentence = "목적지 도착입니다. 경로 안내를 종료합니다.";
        } else {
            this.speechSentence = "잠시 후 " + sentence + "입니다.";
        }
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

    /*public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }*/

    @Override
    public String toString () {
        return "\"" + sentence + "\", nodeID: " + nodeID + ", direction: " + direction + " |";
    }
}
