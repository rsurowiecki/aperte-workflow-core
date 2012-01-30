package org.aperteworkflow.bpm.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TransitionArc implements GraphElement{
    private String name;
    private List<TransitionArcPoint> path = new ArrayList<TransitionArcPoint>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPoint(int x, int y) {
        path.add(new TransitionArcPoint(x, y));
    }
    public List<TransitionArcPoint> getPath() {
        return path;
    }

    public void setPath(List<TransitionArcPoint> path) {
        this.path = path;
    }

    public GraphElement cloneNode() {
        TransitionArc clone = new TransitionArc();
        clone.name = name;
        for (TransitionArcPoint tap : path) {
            clone.addPoint(tap.getX(), tap.getY());
        }
        return clone;
    }

    @Override
    public String toString() {
        String s = "TransitionArc{" +
                "name='" + name + '\'' +
                ",\n path=";
        for (TransitionArcPoint p : path) {
            s+= p + " ";
        }
        return s + '}';
    }
}
