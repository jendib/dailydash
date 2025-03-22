package org.bgamard.dailydash.model;

import java.util.List;

public class ChangeResponse {
    public List<Node> nodes;
    
    public static class Node {
        public String type;
        public String text;
        public String parentId;
        public boolean checked;
    }
}
