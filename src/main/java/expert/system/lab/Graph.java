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

    private final Fact root;

    public Graph(Fact root) {
        this.root = root;
    }

    public Fact getRoot() {
        return root;
    }

    public void outputGraph(String prefix) {
        Set<Fact> visitedFacts = new HashSet<>();

        List<LinkSource> linkSources = new ArrayList<>();
        Deque<Fact> facts = new LinkedList<>();

        visitedFacts.add(root);
        facts.push(root);

        while (!facts.isEmpty()) {
            Fact peekFact = facts.peek();

            Node n;
            List<Attributes<? extends ForNode>> attributes = new ArrayList<>();
            if (peekFact.getOperation() == Fact.Operation.AND) {
                attributes.add(Shape.RECTANGLE);
            }
            if (peekFact.isResolved()) {
                attributes.add(Color.GREEN);
                attributes.add(Color.GREEN.font());
            }

            attributes.add(Label.html(peekFact.getName()));

            n = node(peekFact.getId().toString()).with(attributes);

            Set<Fact> outFacts = peekFact.getOutFacts();

            for (Fact outFact : outFacts) {
                List<Attributes<? extends ForNode>> attrs = new ArrayList<>();
                attrs.add(Label.html(outFact.getName()));
                if (outFact.isResolved()) {
                    attrs.add(Color.GREEN);
                    attrs.add(Color.GREEN.font());
                }
                linkSources.add(n.link(to(node(outFact.getId().toString()).with(attrs))));
            }

            Fact f = getAdjUnvisited(peekFact, visitedFacts);
            if (f == null) {
                facts.pop();
            } else {
                visitedFacts.add(f);
                facts.push(f);
            }
        }

        guru.nidi.graphviz.model.Graph g = graph().directed()
                .graphAttr().with(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM))
                .linkAttr().with("class", "link-class")
                .with(linkSources.toArray(LinkSource[]::new));

        try {
            LocalDateTime now = LocalDateTime.now();
            Graphviz.fromGraph(g).height(1000).render(Format.PNG)
                    .toFile(new File("graph/"+prefix+"graph_and_or_" +
                            now.getDayOfMonth() + "-" +
                            now.getMonthValue() + "-" +
                            now.getYear() + "-" +
                            now.getHour() + "-" +
                            now.getMinute() + "-" +
                            now.getSecond() + ".png"));
        } catch (IOException exc) {
            System.err.println(exc.getMessage());
        }
    }

    public void outputFacts(String space, Fact start) {
        Fact temp = start;
        if (root.isResolved()) {
            if (temp.getOperation() == Fact.Operation.AND) {
                System.out.printf("%sДля %s требуется\n", space, temp.getName());
                space += "\t";
                for (Fact f : temp.getOutFacts()) {
                    if (f.isResolved()) {
                        System.out.printf("%s%s\n", space, f.getName());
                        outputFacts(space, f);
                    }
                }
            } else if (temp.getOperation() == Fact.Operation.OR) {
                System.out.printf("%sДля %s один из возможных путей решения\n", space, temp.getName());
                space += "\t";
                for (Fact f : temp.getOutFacts()) {
                    if (f.isResolved()) {
                        System.out.printf("%s%s\n", space, f.getName());
                        outputFacts(space, f);
                    }
                }
            }
        } else {
            System.out.printf("Задача %s - не разрешима", root.getName());
        }
    }

    private Fact getAdjUnvisited(Fact fact, Set<Fact> visitedFacts) {
        Set<Fact> facts = fact.getOutFacts();
        for (Fact f : facts) {
            if (!visitedFacts.contains(f)) {
                return f;
            }
        }
        return null;
    }

    public Graph bruteForceMethod() {
        List<Fact> openList = new LinkedList<>();
        List<Fact> closeList = new LinkedList<>();

        // ШАГ 1: Помещаем начальную вершину s в список вершин с названием ОТКРЫТ
        openList.add(root);

        while (!openList.isEmpty()) {

            // ШАГ 2: Взять первую вершину из списка ОТКРЫТ и поместить
            // ее в список вершин с названием ЗАКРЫТ; обозначить эту вершину
            // через n
            Fact n = openList.remove(0);
            closeList.add(n);

            // ШАГ 3: Раскрыть вершину n, построив все ее дочерние вершины.
            // поместить эти дочерние вершины в конец списка ОТКРЫТ и провести
            // от них указатели к вершине n. Если дочерних вершин не оказалось,
            // то пометить вершину n как неразрешимую и продолжать;
            // в противном случае перейти к ШАГУ 8
            boolean haveChildren = !n.getOutFacts().isEmpty();
            if (!haveChildren) {

                n.setResolved(false);

                // ШАГ 4: применить к дереву поиска процедуру разметки неразрешимых вершин
                applyNotResolving(n);

                // ШАГ 5: Если начальная вершина помечена как неразрешимая, то на выходе
                // подается сигнал о неудаче. В противном случае продолжать далее.
                if (!root.isResolved()) {
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
                        for (Fact f : next.getInFacts()) {
                            if (!f.isResolved()) {
                                removeFact.add(f);
                            }
                        }
                    }

                    openList.removeAll(removeFact);
                }

            } else {
                // ШАГ 8: Если все дочерние вершины являются заключительными, то пометить их как
                // разрешимые и продолжать. В противном случае перейти к ШАГ 2
                openList.addAll(n.getOutFacts());

                Set<Fact> resolvedFacts = new HashSet<>();
                if (!n.isAllTerminalOutFacts()) {
                    continue;
                } else {
                    resolvedFacts.addAll(n.getOutFacts());
                }

                // ШАГ 9: Применить к дереву перебора процедуру разметки разрешимых вершин.
                applyResolving(n);

                // ШАГ 10: Если начальная вершина помечена как разрешимая, то на выход
                // выдается дерево решения, которое доказывает, что начальная вершина разрешима.
                // В противном случае продолжать.
                if (root.isResolved()) {
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
                        for (Fact f : next.getInFacts()) {
                            if (f.isResolved()) {
                                removeFacts.add(next);
                            }
                        }
                    }

                    openList.removeAll(removeFacts);
                }
            }
        }

        return buildDecision(this);
    }

    private Graph buildDecision(Graph graph) {
        return graph.from(false);
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

            if (temp.getType() == Fact.Type.CENTRAL) {
                resolveFact(temp);
                for (Fact f : temp.getInFacts()) {
                    stack.push(f);
                }
            }
            if (temp.getType() == Fact.Type.INITIAL) {
                resolveFact(temp);
            }
        }
    }

    private void resolveFact(Fact fact) {
        if (fact.getOperation() == Fact.Operation.OR) {
            boolean isResolving = false;
            for (Fact f : fact.getOutFacts()) {
                if (f.isResolved()) {
                    isResolving = true;
                    break;
                }
            }
            fact.setResolved(isResolving);
        } else if (fact.getOperation() == Fact.Operation.AND) {
            int countIsResolving = 0;
            for (Fact f : fact.getOutFacts()) {
                if (f.isResolved()) {
                    countIsResolving++;
                }
            }
            fact.setResolved(countIsResolving == fact.getOutFacts().size());
        }
    }

    private void applyNotResolving(Fact start) {
        Set<Fact> visited = new HashSet<>();

        Deque<Fact> stack = new LinkedList<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            Fact temp = stack.pop();
            if (visited.contains(temp)) {
                continue;
            }

            visited.add(temp);

            if (temp.getType() == Fact.Type.CENTRAL) {
                notResolveFact(temp);
                for (Fact f : temp.getInFacts()) {
                    stack.push(f);
                }
            }
            if (temp.getType() == Fact.Type.INITIAL) {
                notResolveFact(temp);
            }

        }
    }

    private void notResolveFact(Fact fact) {
        if (fact.getOperation() == Fact.Operation.OR) {
            boolean isResolving = true;
            for (Fact f : fact.getOutFacts()) {
                if (!f.isResolved()) {
                    isResolving = false;
                    break;
                }
            }
            fact.setResolved(isResolving);
        } else if (fact.getOperation() == Fact.Operation.AND) {
            int countNotResolving = 0;
            for (Fact f : fact.getOutFacts()) {
                if (!f.isResolved()) {
                    countNotResolving++;
                }
            }
            fact.setResolved(countNotResolving == fact.getOutFacts().size());
        }
    }


    public void resolvingWithRecursion() {
        resolvingWithRecursion(root);
    }

    private void resolvingWithRecursion(Fact start) {
        if (!start.getOutFacts().isEmpty()) {
            for (Fact outFact : start.getOutFacts()) {
                resolvingWithRecursion(outFact);
            }
        }

        applyResolving(start);
    }

    private Graph from(boolean resolved) {
        Set<Fact> visitedFacts = new HashSet<>();

        visitedFacts.add(this.root);
        Queue<Fact> queue = new LinkedList<>();
        Queue<Fact> newFactsQueue = new LinkedList<>();

        queue.add(this.root);
        Fact newRoot = this.root.from();
        newFactsQueue.add(newRoot);

        Fact v2;

        if (resolved && !newRoot.isResolved()) {
            return new Graph(new Fact());
        }

        while (!queue.isEmpty()) {
            Fact v1 = queue.remove();
            Fact vNew = newFactsQueue.remove();

            while ((v2 = getAdjUnvisited(v1, visitedFacts)) != null) {
                visitedFacts.add(v2);
                Fact newFact = v2.from();

                if (resolved) {
                    if (vNew.isResolved() && newFact.isResolved()) {
                        newFact.addInFacts(vNew);
                    }
                } else {
                    newFact.addInFacts(vNew);
                }

                queue.add(v2);
                newFactsQueue.add(newFact);
            }
        }

        return new Graph(newRoot);
    }

}
