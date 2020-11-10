package expert.system.lab;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static guru.nidi.graphviz.model.Factory.*;

public class Graph {

    private final Fact start;

    public Graph(Fact start) {
        this.start = start;
    }

    public Fact getStart() {
        return start;
    }

    public void outputGraph(String name) {
        List<LinkSource> linkSources = new ArrayList<>();
        Deque<Fact> facts = new LinkedList<>();

        facts.push(start);

        Set<Fact> visited = new HashSet<>();
        while (!facts.isEmpty()) {
            Fact peekFact = facts.pop();
            if (visited.contains(peekFact)) {
                continue;
            } else {
                visited.add(peekFact);
            }

            List<Attributes<? extends ForNode>> attributesFact = new ArrayList<>();

            attributesFact.add(Shape.CIRCLE);
            if (peekFact.isResolved()) {
                attributesFact.add(Color.GREEN);
                attributesFact.add(Color.GREEN.font());
            }
            attributesFact.add(Label.html(peekFact.getName()));
            Node nodeFact = node(peekFact.getId().toString()).with(attributesFact);

            Set<Rule> peekRules = peekFact.getOutRules();

            for (Rule peekRule : peekRules) {
                if (peekRule != null) {
                    List<Attributes<? extends ForNode>> attributesRule = new ArrayList<>();
                    if (peekRule.getOperation() == Rule.Operation.AND) {
                        attributesRule.add(Shape.RECTANGLE);
                    }
                    if (peekRule.isResolved()) {
                        attributesRule.add(Color.GREEN);
                        attributesRule.add(Color.GREEN.font());
                    }
                    attributesRule.add(Label.html(peekRule.getName()));
                    Node nodeRule = node(peekRule.getId().toString()).with(attributesRule);

                    linkSources.add(nodeFact.link(to(nodeRule)));

                    Set<Fact> outFacts = peekRule.getOutFacts();

                    for (Fact outFact : outFacts) {
                        List<Attributes<? extends ForNode>> attrs = new ArrayList<>();
                        attrs.add(Label.html(outFact.getName()));
                        attrs.add(Shape.CIRCLE);
                        if (outFact.isResolved()) {
                            attrs.add(Color.GREEN);
                            attrs.add(Color.GREEN.font());
                        }
                        facts.push(outFact);
                        linkSources.add(nodeRule.link(to(node(outFact.getId().toString()).with(attrs))));
                    }

                }
            }
        }

        guru.nidi.graphviz.model.Graph g = graph().directed()
                .graphAttr().with(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM))
                .linkAttr().with("class", "link-class")
                .with(linkSources.toArray(LinkSource[]::new));

        try {
            LocalDateTime now = LocalDateTime.now();
            Graphviz.fromGraph(g).height(1000).render(Format.PNG)
                    .toFile(new File("graph/" + name + ".png"));
        } catch (IOException exc) {
            System.err.println(exc.getMessage());
        }
    }

    public Graph bruteForceMethod() {
        List<Fact> openList = new LinkedList<>();
        List<Fact> closeList = new LinkedList<>();

        // ШАГ 1: Помещаем начальную вершину s в список вершин с названием ОТКРЫТ
        openList.add(start);

        while (!openList.isEmpty()) {
            // ШАГ 2: Взять первую вершину из списка ОТКРЫТ и поместить
            // ее в список вершин с названием ЗАКРЫТ; обозначить эту вершину
            // через n
            Fact n = openList.remove(0);
            closeList.add(n);
            Set<Rule> outRules = n.getOutRules();

            // ШАГ 3: Раскрыть вершину n, построив все ее дочерние вершины.
            // поместить эти дочерние вершины в конец списка ОТКРЫТ и провести
            // от них указатели к вершине n. Если дочерних вершин не оказалось,
            // то пометить вершину n как неразрешимую и продолжать;
            // в противном случае перейти к ШАГУ 8
            boolean haveChildren = outRules.stream().map(rule -> rule.getOutFacts().size()).reduce(0, Integer::sum) != 0;
            if (!haveChildren) {

                n.setResolved(false);

                // ШАГ 4: применить к дереву поиска процедуру разметки неразрешимых вершин
                applyResolving(n);

                // ШАГ 5: Если начальная вершина помечена как неразрешимая, то на выходе
                // подается сигнал о неудаче. В противном случае продолжать далее.
                if (!start.isResolved()) {
                    System.out.println("Неудача");
                    break;
                } else {
                    // ШАГ 6,7: Изъять из списка ОТКРЫТ все вершины, имеющие неразрешимые предшествующие
                    // им вершины; перейти к ШАГУ 2
                    Set<Fact> removeFact = new HashSet<>();
                    for (Fact next : openList) {
                        if (!next.isResolved()) {
                            removeFact.add(next);
                        }
                        Set<Rule> rules = next.getInRules();
                        for (Rule r : rules) {
                            for (Fact f : r.getInFacts()) {
                                if (!f.isResolved()) {
                                    removeFact.add(f);
                                }
                            }
                        }
                    }
                    openList.removeAll(removeFact);
                }

            } else {
                // ШАГ 8: Если все дочерние вершины являются заключительными, то пометить их как
                // разрешимые и продолжать. В противном случае перейти к ШАГ 2
                Set<Fact> facts = new LinkedHashSet<>();
                for (Rule r : outRules) {
                    facts.addAll(r.getOutFacts());
                }
                for (Fact f : facts) {
                    if (!closeList.contains(f)) {
                        openList.add(f);
                    }
                }

                Set<Fact> terminalFacts = new HashSet<>();

                for (Rule r : n.getOutRules()) {
                    terminalFacts.addAll(r.terminalOutFacts());
                }

                Set<Fact> resolvedFacts;
                if (terminalFacts.size() == 0) {
                    continue;
                } else {
                    resolvedFacts = new HashSet<>(terminalFacts);
                }

                // ШАГ 9: Применить к дереву перебора процедуру разметки разрешимых вершин.
                applyResolving(n);

                // ШАГ 10: Если начальная вершина помечена как разрешимая, то на выход
                // выдается дерево решения, которое доказывает, что начальная вершина разрешима.
                // В противном случае продолжать.
                if (start.isResolved()) {
                    System.out.println("Успех");
                    break;
                } else {
                    // ШАГ 11, 12: Изъять из списка ОТКРЫТ все вершины, являющиеся разрешимыми
                    // или имеющие разрешимые предшествующие им вершины. Перейти к ШАГУ 2.
                    Set<Fact> removeFacts = new HashSet<>();
                    for (Fact next : openList) {
                        if (next.isResolved() || resolvedFacts.contains(next)) {
                            removeFacts.add(next);
                        }
                        for (Rule r : next.getInRules()) {
                            for (Fact f : r.getInFacts()) {
                                if (f.isResolved()) {
                                    removeFacts.add(next);
                                }
                            }
                        }
                    }

                    openList.removeAll(removeFacts);
                }
            }
        }

        return

                buildDecision();

    }

    private Graph buildDecision() {
        return buildWithoutNotResolved().buildWithoutPointer();
    }

    private Graph buildWithoutPointer() {
        Deque<Fact> stackFacts = new LinkedList<>();
        Deque<Fact> stackNewFacts = new LinkedList<>();
        stackFacts.push(start);
        Fact newStart = start.from();
        stackNewFacts.push(newStart);
        Set<Fact> visited = new HashSet<>();

        Set<Fact> newFacts = new HashSet<>();
        newFacts.add(newStart);
        while (!stackFacts.isEmpty()) {
            Fact peekFact = stackFacts.pop();
            if (visited.contains(peekFact)) {
                break;
            } else {
                visited.add(peekFact);
            }

            Fact fact = stackNewFacts.pop();

            Set<Rule> peekRules = peekFact.getOutRules();

            for (Rule peekRule : peekRules) {
                if (peekRule != null) {
                    Rule rule = peekRule.from();

                    fact.addOutRules(rule);

                    Set<Fact> outFacts = peekRule.getOutFacts();

                    for (Fact outFact : outFacts) {
                        stackFacts.push(outFact);
                        Fact newFact = outFact.from();
                        for (Fact f : newFacts) {
                            if (f.equals(newFact)) {
                                newFact = f;
                            }
                        }
                        newFacts.add(newFact);

                        stackNewFacts.push(newFact);

                        if (rule.getOperation() == Rule.Operation.AND) {
                            newFact.addInRules(rule);
                        } else if (newFact.getInRules().size() == 0) {
                            newFact.addInRules(rule);
                        }
                    }

                }
            }
        }

        return new Graph(newStart);
    }

    private Graph buildWithoutNotResolved() {
        Deque<Fact> facts = new LinkedList<>();
        Deque<Fact> newFacts = new LinkedList<>();
        facts.push(start);
        Fact newStart = start.from();
        newFacts.push(newStart);
        Set<Fact> visited = new HashSet<>();
        while (!facts.isEmpty()) {
            Fact peekFact = facts.pop();
            if (visited.contains(peekFact)) {
                break;
            } else {
                visited.add(peekFact);
            }

            Fact fact = newFacts.pop();

            Set<Rule> peekRules = peekFact.getOutRules();

            for (Rule peekRule : peekRules) {
                if (peekRule != null) {
                    Rule rule = peekRule.from();

                    if (!rule.isResolved()) {
                        continue;
                    }

                    fact.addOutRules(rule);

                    Set<Fact> outFacts = peekRule.getOutFacts();

                    for (Fact outFact : outFacts) {
                        facts.push(outFact);
                        Fact newFact = outFact.from();
                        newFacts.push(newFact);
                        if (!outFact.isResolved()) {
                            continue;
                        }

                        rule.addOutFacts(newFact);
                    }

                }
            }
        }

        return new Graph(newStart);
    }

    private void applyResolving(Fact start) {
        Set<Fact> visited = new HashSet<>();

        Deque<Fact> stack = new LinkedList<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            Fact temp = stack.pop();
            if (visited.contains(temp)) {
                continue;
            }

            visited.add(temp);

            Set<Rule> inRules = temp.getInRules();

            resolveFact(temp);

            for (Rule rule : inRules) {
                for (Fact f : rule.getInFacts()) {
                    stack.push(f);
                }
            }
        }
    }

    private void resolveFact(Fact fact) {
        Set<Rule> rules = fact.getOutRules();
        for (Rule rule : rules) {
            if (rule.getOperation() == Rule.Operation.OR) {
                boolean isResolving = false;
                for (Fact f : rule.getOutFacts()) {
                    if (f.isResolved()) {
                        isResolving = true;
                        break;
                    }
                }
                rule.setResolved(isResolving);
            } else if (rule.getOperation() == Rule.Operation.AND) {
                int countIsResolving = 0;
                for (Fact f : rule.getOutFacts()) {
                    if (f.isResolved()) {
                        countIsResolving++;
                    }
                }
                rule.setResolved(countIsResolving == rule.getOutFacts().size());
            }
        }

        for (Rule rule : rules) {
            if (rule.isResolved()) {
                fact.setResolved(rule.isResolved());
                break;
            }
        }
    }

    private void calcLevels() {
        Set<Fact> visited = new HashSet<>();
        start.setLevel(0);

        Deque<Fact> stack = new LinkedList<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            Fact temp = stack.pop();

            if (visited.contains(temp)) {
                continue;
            }

            visited.add(temp);

            Set<Rule> outRules = temp.getOutRules();

            for (Rule rule : outRules) {
                rule.setLevel(temp.getLevel() + 1);
                for (Fact f : rule.getOutFacts()) {
                    f.setLevel(rule.getLevel() + 1);
                    stack.push(f);
                }
            }
        }
    }

}
