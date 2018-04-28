/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.graph.build.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.line.OptDirectedXYNode;
import org.geotools.graph.structure.opt.OptDirectedNode;

/**
 * An implementation of GraphGenerator used to generate an optimized graph representing a line
 * network. Graphs are generated by supplying the generator with objects of type LineSegment via the
 * add(Object) method. <br>
 * <br>
 * For each line segment added, an edge in the graph is created. The builder records the end
 * coordinates of each line added, and maintains a map of coordinates to nodes, creating nodes when
 * neccessary.<br>
 * <br>
 * Edges created by the generator are of type OptBasicEdge. Nodes created by the generator are of
 * type OptXYNode.
 *
 * @see org.geotools.graph.structure.opt.OptEdge
 * @see org.geotools.graph.structure.line.OptXYNode
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 * @source $URL$
 */
public class OptDirectedLineGraphGenerator extends OptLineGraphGenerator {

    /** maps in coordinates to count / node * */
    HashMap m_in2count;

    /** maps out coordinates to count / node * */
    HashMap m_out2count;

    /** Constructs a new OptLineGraphGenerator. */
    public OptDirectedLineGraphGenerator() {
        super();

        m_in2count = new HashMap();
        m_out2count = new HashMap();

        setGraphBuilder(new OptDirectedLineGraphBuilder());
    }

    /**
     * Adds a line to the graph. Note that this method returns null since actual building of the
     * graph components is delayed until generate() is called.
     *
     * @param obj A LineSegment object.
     * @return null because the actual building of the graph components is delayed until generate()
     *     is called.
     */
    public Graphable add(Object obj) {
        LineSegment line = (LineSegment) obj;
        Integer count;

        // increment the count of the in coordinate
        if ((count = (Integer) m_in2count.get(line.p0)) == null) {
            m_in2count.put(line.p0, new Integer(1));
        } else m_in2count.put(line.p0, new Integer(count.intValue() + 1));

        // increment the count of the out coordinate
        if ((count = (Integer) m_out2count.get(line.p1)) == null) {
            m_out2count.put(line.p1, new Integer(1));
        } else m_out2count.put(line.p1, new Integer(count.intValue() + 1));

        getLines().add(line);

        return (null);
    }

    /**
     * Returns the coordinate to <B>in</B> node map. Note that before the call to generate the map
     * does not contain any nodes.
     *
     * @return Coordinate to in node map.
     */
    public Map getInNodeMap() {
        return (m_in2count);
    }

    /**
     * Returns the coordinate to <B>out</B> node map. Note that before the call to generate the map
     * does not contain any nodes.
     *
     * @return Coordinate to out node map.
     */
    public Map getOutNodeMap() {
        return (m_out2count);
    }

    protected void generateNodes() {
        // create the nodes, starting with in nodes
        for (Iterator itr = m_in2count.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            Coordinate coord = (Coordinate) entry.getKey();
            Integer count = (Integer) entry.getValue();

            OptDirectedXYNode node = (OptDirectedXYNode) getGraphBuilder().buildNode();
            node.setCoordinate(coord);

            // set the out degree (in count means => out degree)
            node.setOutDegree(count.intValue());

            // get the in degree (out count) from the out map, if no entry, set to 0
            count = (Integer) m_out2count.get(coord);
            if (count != null) node.setInDegree(count.intValue());
            else node.setInDegree(0);

            getGraphBuilder().addNode(node);

            // set map value to be node instead of count
            entry.setValue(node);
        }

        // create only nodes that are not in the in set
        for (Iterator itr = m_out2count.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            Coordinate coord = (Coordinate) entry.getKey();
            Integer count = (Integer) entry.getValue();

            // look for the node in the in set, if not there, it means that the
            // node is never an in node => out degree = 0
            OptDirectedXYNode node = (OptDirectedXYNode) m_in2count.get(coord);
            if (node == null) {
                node = (OptDirectedXYNode) getGraphBuilder().buildNode();
                node.setCoordinate(coord);

                node.setOutDegree(0);
                node.setInDegree(count.intValue());

                getGraphBuilder().addNode(node);
            }
            // else do nothing, the node was already set when processing in set

            // set map value to be node instead of count
            entry.setValue(node);
        }
    }

    protected Edge generateEdge(LineSegment line) {
        OptDirectedNode n1 = (OptDirectedNode) m_in2count.get(line.p0);
        OptDirectedNode n2 = (OptDirectedNode) m_out2count.get(line.p1);

        Edge edge = getGraphBuilder().buildEdge(n1, n2);
        getGraphBuilder().addEdge(edge);

        return (edge);
    }

    public Node getNode(Coordinate c) {
        Node n = (Node) m_in2count.get(c);

        if (n != null) return (n);
        return ((Node) m_out2count.get(c));
    }

    public Edge getEdge(Coordinate c1, Coordinate c2) {
        // TODO: IMPLEMENT

        return (null);
    }
}
