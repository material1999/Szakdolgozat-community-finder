package Runnable;

import Graph.Edge;
import Graph.Network;
import Graph.Node;
import Graph.States;
import Reader.Read;
import Writer.Write;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class Main {

    static Network network;
    static CSVWriter writer;
    static String networkFilePath, resultPath;
    static double rand, mean;
    static int nodeNumber, s, edges, lastIteration;
    static long startTime, elapsedTime, elapsedSeconds, seconds, minutes;
    static TreeSet<Integer> L, deleteTemp;
    static HashSet<TreeSet<Integer>> K = new HashSet() {
        @Override
        public String toString() {
            String ret = "";
            for (Object o: this) {
                ret += o + " ";
            }
            ret = ret.strip();
            return ret;
        }
    };
    static HashSet<TreeSet<Integer>> J = new HashSet() {
        @Override
        public String toString() {
            String ret = "";
            for (Object o: this) {
                ret += o + " ";
            }
            ret = ret.strip();
            return ret;
        }
    };
    static HashSet<TreeSet<Integer>> helper = new HashSet() {
        @Override
        public String toString() {
            String ret = "";
            for (Object o: this) {
                ret += o + " ";
            }
            ret = ret.strip();
            return ret;
        }
    };

    public static void addVertexIteration() {
        helper.clear();
        int counter = 0;
        Set<Node> neighbourSet = new HashSet<>();
        for (TreeSet<Integer> A : K) {
            counter++;
            //System.out.println("add: " + counter + "/" + K.size());
            if (A.size() != s - 1) continue;
            neighbourSet.clear();
            for (Integer a : A) {
                neighbourSet.addAll(network.getNode(Integer.toString(a)).getNeighbour());
            }
            for (Node n : neighbourSet) {
                L = new TreeSet<>();
                L.addAll(A);
                if (isConnected(L, Integer.parseInt(n.getId()))) {
                    L.add(Integer.parseInt(n.getId()));
                    if (meanEdgeWeight(L) > Parameters.timesMean * mean && !K.contains(L)) {
                        helper.add(L);
                        lastIteration++;
                    }
                }
            }
        }
    }

    public static void deleteSubset() {
        J.clear();
        J.addAll(helper);
        helper.clear();
        int counter = 0;
        for (TreeSet<Integer> A : J) {
            int match;
            counter++;
            //System.out.println("delete: " + counter + "/" + J.size());
            for (TreeSet<Integer> a : K) {
                deleteTemp = new TreeSet<>();
                match = 0;
                for (Integer integer : a) {
                    if (A.contains(integer)) {
                        match++;
                    } else break;
                }
                if (match == a.size()) {
                    deleteTemp.addAll(a);
                    helper.add(deleteTemp);
                }
            }
        }
    }

    public static double meanEdgeWeight(TreeSet<Integer> vertices) {
        double meanWeight = 0;
        edges = 0;
        for (Integer v1 : vertices) {
            for (Integer v2 : vertices) {
                if (network.getEdge(v1, v2) != null) {
                    meanWeight += network.getEdge(v1, v2).getWeight();
                    edges++;
                }
            }
        }
        if (edges < vertices.size()) return 0;
        meanWeight /= edges;
        return meanWeight;
    }

    public static double networkMeanEdgeWeight() {
        double meanWeight = 0;
        for (Node n1 : network.getNodes()) {
            for (Node n2 : network.getNodes()) {
                if (Integer.parseInt(n1.getId()) > Integer.parseInt(n2.getId())
                        && network.getEdge(Integer.parseInt(n1.getId()), Integer.parseInt(n2.getId())) != null) {
                    meanWeight += network.getEdge(Integer.parseInt(n1.getId()), Integer.parseInt(n2.getId())).getWeight();
                }
            }
        }
        meanWeight /= network.getEdges().size();
        return meanWeight;
    }

    public static boolean isConnected(TreeSet<Integer> vertices, Integer newVertex) {
        int counter = 0;
        for (Integer v : vertices) {
            if (network.getEdge(newVertex, v) != null) {
                counter++;
            }
        }
        double threshold = vertices.size() * Parameters.connectedPercent / 100;
        if (vertices.size() == 1 && counter == 0) return false; else return counter >= threshold;
    }

    public static void infectionSimulation(CSVWriter writer) {
        HashMap<String, Double> toWrite = new HashMap<>();
        String key;
        for (int j = 1; j <= nodeNumber; j++) {
            network.deleteFv();
            for (int k = 1; k <= Parameters.sampleSize; k++) {
                network.notVisited();
                for (Node node1: network.getNode(Integer.toString(j)).getNeighbour()) {
                    // System.out.print(j + " --> " + node1.getId() + "\n");
                    rand = Math.random();
                    if (network.getEdge(j, Integer.parseInt(node1.getId())).getWeight() > rand) {
                        node1.addFv();
                        node1.setState(States.VISITED);
                        for (Node node2: node1.getNeighbour()) {
                            rand = Math.random();
                            if (node2.getState() == States.NOTVISITED &&
                                    network.getEdge(Integer.parseInt(node1.getId()), Integer.parseInt(node2.getId())).getWeight() > rand) {
                                node2.addFv();
                                node2.setState(States.VISITED);
                            }
                        }
                    }
                }
            }
            for (Node node: network.getNodes()) {
                if (node.getFv() > 0 && Integer.parseInt(node.getId()) > j) {
                    node.finalizefv(Parameters.sampleSize);
                    key = j + ";" + node.getId();
                    toWrite.put(key, node.getFv());
                }
            }
        }
        Write.WriteProbabilities(writer, toWrite);
    }

    public static void newInfectionSimulation(CSVWriter writer) {
        HashMap<String, Double> toWrite = new HashMap<>();
        Network simNetwork;
        HashMap<String, Integer> infectionSpread = new HashMap<>();
        HashSet<Node> reachedNodes = new HashSet<>();
        String key;
        for (int j = 1; j <= Parameters.sampleSize; j++) {
        //for (int j = 1; j <= 1; j++) {
            simNetwork = new Network();
            for (Edge edge: network.getEdges()) {
                rand = Math.random();
                if (edge.getWeight() > rand) {
                    simNetwork.addEdge(edge);
                }
            }
            for (Node node0: simNetwork.getNodes()) {
                reachedNodes.clear();
                for (Node node1: node0.getNeighbour()) {
                    if (simNetwork.getEdge(Integer.parseInt(node0.getId()), Integer.parseInt(node1.getId())) != null &&
                            Integer.parseInt(node1.getId()) > Integer.parseInt(node0.getId())) {
                        reachedNodes.add(node1);
                        for (Node node2: node1.getNeighbour()) {
                            if (simNetwork.getEdge(Integer.parseInt(node1.getId()), Integer.parseInt(node2.getId())) != null &&
                                    Integer.parseInt(node2.getId()) > Integer.parseInt(node0.getId())) {
                                reachedNodes.add(node2);
                            }
                        }
                    }
                }
                for (Node node: reachedNodes) {
                    key = node0.getId() + ";" + node.getId();
                    if (infectionSpread.containsKey(key)) {
                        infectionSpread.put(key, infectionSpread.get(key) + 1);
                    } else {
                        infectionSpread.put(key, 1);
                    }
                }
            }
        }
        infectionSpread.entrySet().forEach(e ->
                toWrite.put(e.getKey(), (double) e.getValue() / Parameters.sampleSize)
        );
        Write.WriteProbabilities(writer, toWrite);
    }

    public static void communityFinder() {
        K.clear();
        for (Node n : network.getNodes()) {
            TreeSet<Integer> tsi = new TreeSet<>();
            tsi.add(Integer.parseInt(n.getId()));
            K.add(tsi);
        }
        for (s = 2; s <= Parameters.maxCommunitySize; s++) {
            System.out.println("community size: " + s + " ...");
            //startTime = System.currentTimeMillis();
            lastIteration = 0;
            addVertexIteration();
            deleteSubset();
            K.addAll(J);
            K.removeAll(helper);
            if (lastIteration == 0) {
                System.out.println("break at community size: " + s);
                break;
            }
            /*
            elapsedTime = System.currentTimeMillis() - startTime;
            elapsedSeconds = elapsedTime / 1000;
            seconds = elapsedSeconds % 60;
            minutes = elapsedSeconds / 60;
            System.out.println("elapsed time: " + minutes + " min " + seconds + " sec");
            */
        }
        System.out.println("biggest community found: " + (s - 1));
        /*
        System.out.println("writing started");
        startTime = System.currentTimeMillis();
        */
        Write.WriteCommunities(writer, K);
        /*
        System.out.println("writing ended");
        elapsedTime = System.currentTimeMillis() - startTime;
        elapsedSeconds = elapsedTime / 1000;
        seconds = elapsedSeconds % 60;
        minutes = elapsedSeconds / 60;
        System.out.println("elapsed time: " + minutes + " min " + seconds + " sec\n");
        */
    }

    public static void main(String[] args) throws IOException {

        System.out.println();
        TreeSet<Integer> on = new TreeSet<>();

        //on.add(0);
        on.add(1);
        //on.add(2000);
        //on.add(750);
        //for (int i = 1; i <= Parameters.fileCount; i++) on.add(i);

        for (int i : on) {
            networkFilePath = Parameters.networksFolder + i + "/edgeweighted_edit.csv";
            network = Read.ReadCsv(networkFilePath);
            nodeNumber = network.getNodes().size();
            resultPath = "results/sim_inf/sim_inf_" + i + "_" + Parameters.sampleSize + ".csv";

            ////////// Infection Simulation //////////
            /*
            System.out.println("file: " + i);
            System.out.println("siminf started");
            startTime = System.currentTimeMillis();
            writer = new CSVWriter(new FileWriter(resultPath), ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.NO_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            writer.writeNext(new String[]{"V1;V2;edgeweight"}, true);
            //infectionSimulation(writer);
            newInfectionSimulation(writer);
            writer.close();
            System.out.println("siminf done");
            elapsedTime = System.currentTimeMillis() - startTime;
            elapsedSeconds = elapsedTime / 1000;
            seconds = elapsedSeconds % 60;
            minutes = elapsedSeconds / 60;
            System.out.println("elapsed time: " + minutes + " min " + seconds + " sec\n");
            */

            ////////// Community Detection //////////

            System.out.println("file: " + i);
            System.out.println("simcom started");
            startTime = System.currentTimeMillis();
            network = Read.ReadCsv(resultPath);
            resultPath = "results/sim_com/sim_com_" + i + "_max" + Parameters.maxCommunitySize + "_" +
                    (int)Parameters.connectedPercent + "szazalek_" + Parameters.timesMean + "x.csv";
            writer = new CSVWriter(new FileWriter(resultPath), ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.NO_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            mean = networkMeanEdgeWeight();
            communityFinder();
            writer.close();
            System.out.println("simcom done");
            elapsedTime = System.currentTimeMillis() - startTime;
            elapsedSeconds = elapsedTime / 1000;
            seconds = elapsedSeconds % 60;
            minutes = elapsedSeconds / 60;
            System.out.println("elapsed time: " + minutes + " min " + seconds + " sec\n");

        }

        System.out.println("\nProgram finished successfully, press ENTER to exit ...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}