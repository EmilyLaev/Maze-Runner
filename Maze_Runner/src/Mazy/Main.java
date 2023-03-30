package Mazy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

public class Main {

    //This is the main running of the program.  It prints a menu for the user to pick amoung
    //the various options, to generate, load, save, display, find path, or exit
    //A maze must already be displayed for options 3, 4 or 5 to be available
    public static void main(String[] args) {
        boolean currentMaze = false;
        boolean on = true;
        Maze newMaze = new Maze(0, 0);
        while (on) {
            System.out.println("=== Menu ===");
            System.out.println("1. Generate a new maze");
            System.out.println("2. Load a maze");
            if (currentMaze) {
                System.out.println("3. Save the maze");
                System.out.println("4. Display the maze");
                System.out.println("5. Find the escape");
            }
            System.out.println("0. Exit");
            Scanner scan = new Scanner(System.in);
            if (scan.hasNextInt()) {
                int k = scan.nextInt();
                switch (k) {
                    case 0:
                        on = false;
                        break;
                    case 1:
                        newMaze = generateMaze();
                        currentMaze = true;
                        break;
                    case 2:
                        newMaze = loadMaze();
                        currentMaze = true;
                        break;
                    case 3:
                        if (currentMaze) {
                            saveMaze(newMaze);
                            break;
                        }
                    case 4:
                        if (currentMaze) {
                            drawMaze(newMaze, false);
                            break;
                        }
                    case 5:
                        if (currentMaze) {
                            drawMaze(newMaze, true);
                            break;
                        }
                    default:
                        System.out.println("Incorrect option. Please try again");
                        break;
                }
            }
        }
    }

    //This is a method to save a maze by writing it to a file
    //the maze is saved under the path name entered by the user
    //maze is converted to a character ArrayList through invoking method mazeToText
    //there is an error if the path name is unavailable
    public static void saveMaze(Maze maze) {
        System.out.println("Enter path of where to save maze:");
        Scanner scan = new Scanner(System.in);
        String path = scan.next();
        File save = new File(path);
        try (FileWriter writer = new FileWriter(save)) {
            writer.write(maze.H + " " + maze.W + " ");
            ArrayList<Character> text = mazeToText(maze);
            for (int i = 0; i < text.size(); i++) {
                writer.write(text.get(i));
            }
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    //Previously saved mazes are loaded once user enters correct path name
    //The file is opened and the maze object is displayed, it is now considered the new current maze 
    public static Maze loadMaze(){
        System.out.println("Enter path of maze to be loaded");
        Scanner scan = new Scanner(System.in);
        String path = scan.next();
        File file = new File(path);
        String fileText = "";
        try (Scanner fileScan = new Scanner(file)) {
            int H = fileScan.nextInt();
            int W = fileScan.nextInt();
            Maze maze = new Maze(H, W);
            while (fileScan.hasNext()) {
                fileText = fileText + fileScan.next();
            }
            maze = textToMaze(fileText, maze);
            return maze;
        } catch (IOException e) {
            System.out.println("Error Cannot read file " + e.getMessage());
            return null;
        }
    }


    //This method turns the integer values of a maze into a string of characters
    public static ArrayList<Character> mazeToText(Maze maze) {
        ArrayList<Character> textForm = new ArrayList<>();
        for (int i = 0; i < maze.H; i++) {
            for (int j = 0; j < maze.W; j++) {
                if (maze.field[i][j] == 1) {
                    textForm.add('W');
                } else if (maze.field[i][j] == 0) {
                    textForm.add('S');
                } else if (maze.field[i][j] == 2) {
                    textForm.add('P');
                }
            }
        }
        return textForm;
    }

    //This method turns the text of a string of characters into integer values for the maze
    public static Maze textToMaze(String text, Maze maze) {
        for (int i = 0; i < maze.H; i++) {
            for (int j = 0; j < maze.W; j++) {
                char c = text.charAt((i * maze.W) + j);
                if (c == 'W') {
                    maze.field[i][j] = 1;
                } else if (c == 'S') {
                    maze.field[i][j] = 0;
                } else if (c == 'P') {
                    maze.field[i][j] = 2;
                }
            }
        }
        return maze;
    }

    //This is the method that creates a new maze object and fills it with values
    //First the user is asked for dimensions, then the maze object is created
    //The algorithm creates a randomized maze
    //The exit points of the maze are determined then the pathways of the maze
    public static Maze generateMaze() {
        int H;
        //int W = 0;
        while (true) {
            System.out.println("Please enter size of a maze");
            Scanner scan = new Scanner(System.in);
            H = scan.nextInt();
            //W = scan.nextInt();
            if (H > 2) {
                break;
            } else {
                System.out.println("Please enter bigger number");
            }
        }
        Maze myMaze = new Maze(H, H);
        boolean addHeight = false;
        boolean addWidth = false;
        int h;
        if (H % 2 == 0) {
            h = H / 2 - 1;
            addHeight = true;
            addWidth = true;
        } else {
            h = (H - 1) / 2;
        }
        int w = h;
        //if (W % 2 == 0) {
        //    w = W / 2 - 1;
        //    addWidth = true;
        //} else {
        //    w = (W - 1) / 2;
        //}
        ArrayList<Edge> feList = new ArrayList<>();
        ArrayList<Node> fnList = new ArrayList<>();
        ArrayList<Node> NodeList = createNodes(h, w);
        ArrayList<Edge> EdgeList = createEdges(NodeList, h, w);
        setConEdges(NodeList, EdgeList);
        Alg(NodeList, EdgeList, feList, fnList);
        setDownRight(fnList, feList);
        pickExits(NodeList, h, w, myMaze, addHeight, addWidth);
        ArrayList<Node> path = findPath(myMaze, feList);
        addPath(path, w);
        createField(myMaze, fnList, h, w, path);
        drawMaze(myMaze, false);
        return myMaze;
    }

    //This method takes the field value of the maze and displays it
    //if drawPath is true the path is highlight, if false it is ignored
    public static void drawMaze(Maze maze, boolean drawPath) {
        System.out.println();
        for (int i = 0; i < maze.H; i++) {
            for (int j = 0; j < maze.W; j++) {
                if (maze.field[i][j] == 0) {
                    System.out.print("  ");
                } else if (maze.field[i][j] == 1) {
                    System.out.print("\u2588\u2588");
                } else if (maze.field[i][j] == 2) {
                    if (drawPath) {
                        System.out.print("//");
                    } else {
                        System.out.print("  ");
                    }
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    //method to create the nodes an put them in an arraylist
    public static ArrayList<Node> createNodes(int H, int W) {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                Node thisNode = new Node((i * W) + j);
                nodeList.add(thisNode);
                thisNode.setxCor((j * 2) + 1);
                thisNode.setyCor((i * 2) + 1);
                thisNode.setCord();
                System.out.print(thisNode.val + " [" + thisNode.yCor + " " + thisNode.xCor + "],  ");
            }
            System.out.println();
        }
        return nodeList;
    }

    //A method to create edges between nodes and put them in an arraylist
    public static ArrayList<Edge> createEdges(ArrayList<Node> nodeList, int h, int w) {
        ArrayList<Edge> Edges = new ArrayList<>();
        int count = h * w;
        for (int i = 0; i < count; i++) {
            if (!((i + 1) % w == 0)) {
                Edge thisEdge = new Edge(nodeList.get(i), nodeList.get(i + 1));
                Edges.add(thisEdge);
                //System.out.print(" [" + thisEdge.A.val + ", " + thisEdge.B.val + "], " + "weight = " + thisEdge.weight);
            }
        }
        for (int i = 0; i < count - w; i++) {
            Edge thisEdge = new Edge(nodeList.get(i), nodeList.get(i + w));
            Edges.add(thisEdge);
            //System.out.print("[" + thisEdge.A.val + ", " + thisEdge.B.val + "],  ");
        }
        //System.out.println();
        return Edges;
    }

    //A method to sort out which nodes are connected to which edges
    public static void setConEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        for (int i = 0; i < nodes.size(); i++) {
            Node thisNode = nodes.get(i);
            for (int j = 0; j < edges.size(); j++) {
                Edge thisEdge = edges.get(j);
                if (thisEdge.A == thisNode || thisEdge.B == thisNode) {
                    thisNode.editConEdges(thisEdge);
                }
            }
        }
    }

    //method Algorithm
    //This is the implenmation of the Prim's Spanning tree algorithm, it takes the nodelist, edgelist as well as empty lists
    //As inputs.  The empty lists are updated to have only edges that are connected in the maze
    public static void Alg(ArrayList<Node> nodes, ArrayList<Edge> edges, ArrayList<Edge> feList, ArrayList<Node> fnList) {
        int k = nodes.size();
        Node startNode = nodes.get(0);
        fnList.add(startNode);
        ArrayList<Edge> connected = startNode.ConEdges;
        while (fnList.size() < k) {
            Edge addingEdge = minEdge(connected);
            Node addingNode = newNode(fnList, addingEdge);
            if (addingNode != null) {
                fnList.add(addingNode);
                feList.add(addingEdge);
                System.out.println("[" + addingEdge.A.val + ", " + addingEdge.B.val + "] ");
                for (int i = 0; i < addingNode.ConEdges.size(); i++) {
                    connected.add(addingNode.ConEdges.get(i));
                }
                delDoubles(connected);
                delCopies(feList, connected);
            } else {
                connected.remove(addingEdge);
            }
        }
    }

    //This is a method to find the minimum weighted edge from a list
    public static Edge minEdge(ArrayList<Edge> edges) {
        Edge addingEdge = edges.get(0);
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).weight < addingEdge.weight) {
                addingEdge = edges.get(i);
            }
        }
        return addingEdge;
    }

    //method to delete edges listed more than once in a list
    public static void delDoubles(ArrayList<Edge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            for (int j = i + 1; j < edges.size(); j++) {
                if (edges.get(i) == edges.get(j)) {
                    edges.remove(j);
                }
            }
        }
    }

    //Method to delete edges from one list if they are in another
    public static void delCopies(ArrayList<Edge> same, ArrayList<Edge> reduce) {
        for (int i = 0; i < same.size(); i++) {
            for (int j = 0; j < reduce.size(); j++) {
                if (same.get(i) == reduce.get(j)) {
                    reduce.remove(j);
                }
            }
        }
    }

    //This method finds the next node connected to an edge
    public static Node newNode(ArrayList<Node> nodes, Edge addingEdge) {
        boolean checkA = false;
        boolean checkB = false;
        for (int i = 0; i < nodes.size(); i++) {
            if (addingEdge.A == nodes.get(i)) {
                checkA = true;
            } else if (addingEdge.B == nodes.get(i)) {
                checkB = true;
            }
        }
        if (checkA && checkB) {
            return null;
        } else if (checkA) {
            return addingEdge.B;
        } else {
            return addingEdge.A;
        }
    }

    //This method iterates through the node and edges arraylist and finds how many edges are connected
    public static void setDownRight(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        //System.out.println();
        for (Node thisNode : nodes) {
            for (Edge thisEdge : edges) {
                if (thisEdge.A == thisNode) {
                    if (thisNode.val + 1 == thisEdge.B.val) {
                        thisNode.rEdges[0] = 0;
                    } else {
                        thisNode.rEdges[1] = 0;
                    }
                }
                if (thisEdge.B == thisNode) {
                    if (thisNode.val - 1 == thisEdge.A.val) {
                        thisNode.rEdges[2] = 0;
                    } else {
                        thisNode.rEdges[3] = 0;
                    }
                }
            }
            //System.out.println("rEdge list:" + thisNode.val + " " + Arrays.toString(thisNode.rEdges));
        }
    }

    //A method to find two nodes that only have three edges (on the sides) and label them as exits
    //The exits must be on different sides of the maze, the outer values of the maze field are changed as well
    public static void pickExits(ArrayList<Node> nodes, int h, int w, Maze maze, boolean AddH, boolean AddW) {
        Random random = new Random();
        int spot = 0;
        int side1 = random.nextInt(4);
        switch (side1) {
            case 0:
                spot = random.nextInt(h);
                nodes.get(spot * w).rEdges[2] = 2;
                maze.Exits.add(nodes.get(spot * w));
                break;
            case 1:
                spot = random.nextInt(w);
                nodes.get(spot).rEdges[3] = 2;
                maze.Exits.add(nodes.get(spot));
                break;
            case 2:
                spot = random.nextInt(h);
                nodes.get((spot * w) + w - 1).rEdges[0] = 2;
                maze.Exits.add(nodes.get((spot * w) + w - 1));
                if (AddW) {
                    maze.setVal((spot * 2) + 1, maze.W - 1, 2);
                }
                break;
            case 3:
                spot = random.nextInt(w);
                nodes.get(spot + (h * w) - w).rEdges[1] = 2;
                maze.Exits.add(nodes.get(spot + (h * w) - w));
                if (AddH) {
                    maze.setVal(maze.H - 1, (spot * 2) + 1, 2);
                }
                break;
        }
        System.out.println("Side1 " + side1 + ", spot " + spot);
        for (Node thisNode : nodes) {
            //System.out.print("new rEdge list:" + thisNode.val + " " + Arrays.toString(thisNode.rEdges));
        }
        int side2;
        while (true) {
            side2 = random.nextInt(4);
            if (side2 != side1) {
                break;
            }
        }

        switch (side2) {
            case 0:
                spot = random.nextInt(h);
                nodes.get(spot * w).rEdges[2] = 2;
                maze.Exits.add(nodes.get(spot * w));
                break;
            case 1:
                spot = random.nextInt(w);
                nodes.get(spot).rEdges[3] = 2;
                maze.Exits.add(nodes.get(spot));
                break;
            case 2:
                spot = random.nextInt(h);
                nodes.get((spot * w) + w - 1).rEdges[0] = 2;
                maze.Exits.add(nodes.get((spot * w) + w - 1));
                if (AddW) {
                    maze.setVal((spot * 2) + 1 , maze.W - 1, 2);
                }
                break;
            case 3:
                spot = random.nextInt(w);
                nodes.get(spot + (h * w) - w).rEdges[1] = 2;
                maze.Exits.add(nodes.get(spot + (h * w) - w));
                if (AddH) {
                    maze.setVal(maze.H - 1, (spot * 2) + 1, 2);
                }
                break;
        }
        System.out.println("Side2 " + side2 + ", spot " + spot);
    }

    //This method finds the path by starting with one exit
    //The connected edges are examined and temporary paths are created until exit is reached
    public static ArrayList<Node> findPath(Maze maze, ArrayList<Edge> edges) {
        Node Ent = maze.Exits.get(0);
        Node Ext = maze.Exits.get(1);
        ArrayList<Edge> eList = copyList(edges);
        Node tempN = Ent;
        Edge tempE;
        ArrayList<Node> Path = new ArrayList<>();
        Path.add(Ent);
        int count = -1;
        while (tempN != Ext) {
            tempE = null;
            System.out.print(tempN.val + ", ");
            for (int i = 0; i < eList.size(); i++) {
                if (tempN == eList.get(i).A || tempN == eList.get(i).B) {
                    if (count == -1 || eList.get(i) != eList.get(count)) {
                        tempN = getOtherNode(eList.get(i), tempN);
                        tempE = eList.get(i);
                        count = i;
                        Path.add(tempN);
                        break;
                    } } }
            System.out.println(tempN.val); 
            if (tempN == Ext) {
                Path.add(Ext);
                break;
            }
            if (tempE == null) {
                eList.remove(count);
                tempN = Ent;
                Path.clear();
                Path.add(Ent);
                count = 0;
                //System.out.println("Happened here");
            }
        }
        System.out.println("Finished here");
        return Path;
    }

    //A method to copy an arraylist of edges
    public static ArrayList<Edge> copyList(ArrayList<Edge> List) {
        return new ArrayList<>(List);
    }

    //Method to find the second node connected to an edge
    public static Node getOtherNode(Edge edge, Node node) {
        if (edge.A == node) {
            return edge.B;
        } else {
            return edge.A;
        }
    }

    //This method takes the path (already determined) and adds value to the
    //maze field so the path will be displayed
    public static void addPath(ArrayList<Node> path, int w) {
        for (int i = 0; i < path.size() - 1; i++) {
            if (path.get(i).val + 1 == path.get(i + 1).val) {
                path.get(i).rEdges[0] = 2;
                path.get(i + 1).rEdges[2] = 2;
            } else if (path.get(i).val - 1 == path.get(i + 1).val) {
                path.get(i).rEdges[2] = 2;
                path.get(i + 1).rEdges[0] = 2;
            } else if (path.get(i).val - w == path.get(i + 1).val) {
                path.get(i).rEdges[3] = 2;
                path.get(i + 1).rEdges[1] = 2;
            } else if (path.get(i).val + w == path.get(i + 1).val) {
                path.get(i).rEdges[1] = 2;
                path.get(i + 1).rEdges[3] = 2;
            }
        }
    }

    //This method creates the field by iterating through the nodes of the maze
    public static void createField(Maze maze, ArrayList<Node> nodes, int h, int w, ArrayList<Node> path) {
        int[] oCords;
        for (Node thisNode : nodes) {
            oCords = thisNode.cord;
            maze.setVal(oCords, 0);
            maze.setVal(oCords[0], oCords[1] + 1, thisNode.rEdges[0]);
            maze.setVal(oCords[0] + 1, oCords[1], thisNode.rEdges[1]);
            maze.setVal(oCords[0], oCords[1] - 1, thisNode.rEdges[2]);
            maze.setVal(oCords[0] - 1, oCords[1], thisNode.rEdges[3]);
            if (maze.Exits.get(0) == thisNode || maze.Exits.get(1) == thisNode) {
                System.out.println(Arrays.toString(thisNode.rEdges));
            }
        }
        for (Node thisNode : path) {
            oCords = thisNode.cord;
            maze.setVal(oCords, 2);
        }
    }


    //A class to make nodes as objects, the points within the maze that can be reached
    //each node has a value, unique coordinates, and four possible edges
    static class Node {
        int val;
        int xCor;
        int yCor;
        int[] cord;
        int[] rEdges;
        //right, down, left, up
        private final ArrayList<Edge> ConEdges;

        public Node(int val) {
            this.val = val;
            this.rEdges = new int[]{1, 1, 1, 1};
            this.ConEdges = new ArrayList<>();
        }

        public int[] getrEdges() {
            return rEdges;
        }

        public void setxCor(int xCor) {
            this.xCor = xCor; }

        public void setyCor(int yCor) {
            this.yCor = yCor; }

        public void setCord() {
            cord = new int[]{yCor, xCor};
        }

        public int getVal() { return val; }

        //
        public ArrayList<Edge> getConEdges() { return ConEdges; }

        public void editConEdges(Edge edge) {
            ConEdges.add(edge);
        }
    }

    //A class for the edges between nodes
    //Each edge is made of two nodes and a random weight between 0 and 10
    static class Edge {
        int weight;
        ArrayList<Node> nodes;
        Node A;
        Node B;

        public Edge(Node A, Node B) {
            ArrayList<Node> nodes = new ArrayList<>();
            nodes.add(A);
            nodes.add(B);
            Random random = new Random();
            this.nodes = nodes;
            this.A = A;
            this.B = B;
            this.weight = random.nextInt(10);
        }

        public ArrayList<Node> getNodes() { return nodes; }

        public int getWeight() { return weight; }

        public Node getANode() { return A; }

        public Node getBNode() { return B; }
    }

    //The object class for the maze itself
    //Each maze is made up of the integer field, height, width, and exit locations
    //The field values say what should be displayed.
    //The path (sequence of edges
    static class Maze {

        private String path;
        private final int H;
        private final int W;
        private int[][] field;
        private final ArrayList<Node> Exits;

        public Maze(int H, int W) {
            this.W = W;
            this.H = H;
            this.path = null;
            int[][] field = new int[H][W];
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    field[i][j] = 1;
                }
            }
            this.field = field;
            this.Exits = new ArrayList<>();
        }

        public void setPath(String path) { this.path = path; }

        public String getPath() { return path; }

        public void setVal(int i, int j, int val) {
            field[i][j] = val;
        }

        public void setVal(int[] cord, int val) { field[cord[0]][cord[1]] = val; }

        public int[][] getField() {
            return field;
        }

        public void setField(int[][] field) {
            this.field = field;
        }
    }
}
