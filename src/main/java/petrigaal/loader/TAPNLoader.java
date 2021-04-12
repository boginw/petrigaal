package petrigaal.loader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TAPNLoader implements Loader {
    private PetriGame game;
    private Map<String, Transition> transitions;

    @Override
    public PetriGame load(InputStream file) {
        Document doc = loadDocument(file);

        return parse(doc);
    }

    private Document loadDocument(InputStream file) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }

    private PetriGame parse(Document doc) {
        game = new PetriGame();
        transitions = new HashMap<>();

        Node node = getNetNode(doc);

        Node first = node;
        node = first;
        //We parse the places and transitions first
        while (node != null) {
            String tag = node.getNodeName();
            if (tag.equals("place")) {
                parsePlace(node);
            } else if (tag.equals("transition")) {
                parseTransition(node);
            }
            node = node.getNextSibling();
        }

        //We parse the transitions last, as we need the places and transitions it refers to
        node = first;
        while (node != null) {
            String tag = node.getNodeName();
            if (tag.equals("arc")) {
                parseArc(node);
            }
            node = node.getNextSibling();
        }

        return game;
    }

    private void parseArc(Node node) {
        if (!(node instanceof Element)) {
            return;
        }

        Element element = (Element) node;
        String sourceId = purify(element.getAttribute("source"));
        String targetId = purify(element.getAttribute("target"));

        if (transitions.containsKey(sourceId)) {
            transitions.get(sourceId).addOutput(targetId);
        } else {
            transitions.get(targetId).addInput(sourceId);
        }
    }

    private void parseTransition(Node node) {
        if (!(node instanceof Element)) {
            return;
        }

        String id = purify(((Element) node).getAttribute("id"));
        Player player = getPlayer((Element) node);
        Transition transition = new Transition(id);
        transitions.put(id, transition);

        game.addTransition(player, transition);
    }

    private Player getPlayer(Element node) {
        return node.getAttribute("player").equals("0") ?
                Player.Controller :
                Player.Environment;
    }

    private void parsePlace(Node node) {
        if (!(node instanceof Element)) {
            return;
        }

        String id = purify(((Element) node).getAttribute("id"));
        int marking = Integer.parseInt(((Element) node).getAttribute("initialMarking"));
        game.setMarking(id, marking);
    }

    private Node getNetNode(Document doc) {
        //We assume there is only one net per file (this is what we call a TAPN Network)
        Node pnmlElement = doc.getElementsByTagName("pnml").item(0);
        return getFirstDirectChild(pnmlElement, "net").getFirstChild();
    }

    private Node getFirstDirectChild(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals(tagName)) {
                return children.item(i);
            }
        }
        return null;
    }

    public static String purify(String name) {
        return name.trim().
                replace(".", "_dot_").
                replace(" ", "_space_").
                replace("-", "_dash_").
                replace("/", "_slash_").
                replace("=", "_equals_");
    }
}
