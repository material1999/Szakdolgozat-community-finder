package Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Node {

	private String id;
	private States state;
	private ArrayList<Edge> inlist;
	private ArrayList<Edge> outlist;
	private Set<Node> outneighbour;
	private Set<Node> neighbour;
	private double fv;
	
	public Node(String id) {
		this.id = id;
		this.state = States.NOTVISITED;
		this.outlist = new ArrayList<Edge>();
		this.inlist = new ArrayList<Edge>();
		this.outneighbour = new HashSet<Node>();
		this.neighbour = new HashSet<Node>();
		this.fv = 0;
	}

	public ArrayList<Edge> getInlist() {
		return inlist;
	}

	public void setInlist(ArrayList<Edge> inlist) {
		this.inlist = inlist;
	}
	
	public void addinEdge(Edge e) {
		this.inlist.add(e);
	}
	
	public double getFv() {
		return fv;
	}

	public void addFv() {
		this.fv++;
	}
	
	public void setFv(double fv) {
		this.fv=fv;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public States getState() {
		return state;
	}

	public void setState(States state) {
		this.state = state;
	}

	public ArrayList<Edge> getOutlist() {
		return outlist;
	}

	public void setOutlist(ArrayList<Edge> outlist) {
		this.outlist = outlist;
	}
	
	public void addOutEdge(Edge e) {
		this.outlist.add(e);
	}

	public Set<Node> getOutneighbour() {
		return outneighbour;
	}

	public void setOutneighbour(Set<Node> outneighbour) {
		this.outneighbour = outneighbour;
	}
	
	public void addOutNeighbour(Node a) {
		this.outneighbour.add(a);
	}

	public Set<Node> getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(Set<Node> neighbour) {
		this.neighbour = neighbour;
	}
	
	public void addNeighbour(Node a) {
		this.neighbour.add(a);
	}

	public void finalizefv(int samplesize) {
		this.fv=this.fv/(double)samplesize;
		if(this.fv>1.0) {
			System.out.println("fv bigger than 1: " + this.fv + ", setting it to 1...");
			this.fv = 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", state=" + state + ", fv=" + fv + "]";
	}
	

	
	

}
