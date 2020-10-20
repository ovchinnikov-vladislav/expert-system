/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package expert.system.lab;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

// Посмотреть 98 страницу, 101, 129, 132, 135, 137
// Цель процесса поиска - показать, что начальная вершина разрешима
// Заключительная вершина разрешима
public class App {
    public static void main(String[] args) {
        example2();
    }

    public static void example1() {
        Fact fact1 = new Fact();
        fact1.setName("Task 1");
        fact1.setType(Fact.Type.INITIAL);
        fact1.setOperation(Fact.Operation.OR);

        Fact fact11 = new Fact();
        fact11.setName("Task 1.1");
        fact11.setType(Fact.Type.CENTRAL);
        fact11.setOperation(Fact.Operation.AND);
        fact11.addInFacts(fact1);

        Fact fact12 = new Fact();
        fact12.setName("Task 1.2");
        fact12.setType(Fact.Type.CENTRAL);
        fact12.setOperation(Fact.Operation.AND);
        fact12.addInFacts(fact1);

        Fact fact13 = new Fact();
        fact13.setName("Task 1.3");
        fact13.setType(Fact.Type.TERMINAL);
        fact13.addInFacts(fact1);
       // fact13.setResolved(true);

        Fact fact111 = new Fact();
        fact111.setName("Task 1.1.1");
        fact111.setType(Fact.Type.TERMINAL);
        fact111.addInFacts(fact11);

        Fact fact112 = new Fact();
        fact112.setName("Task 1.1.2");
        fact112.setType(Fact.Type.TERMINAL);
        fact112.addInFacts(fact11);


        Fact fact121 = new Fact();
        fact121.setName("Task 1.2.1");
        fact121.setType(Fact.Type.TERMINAL);
        fact121.addInFacts(fact12);
        fact121.setResolved(true);

        Fact fact122 = new Fact();
        fact122.setName("Task 1.2.2");
        fact122.setType(Fact.Type.TERMINAL);
        fact122.addInFacts(fact12);
        fact122.setResolved(true);

        Graph graph = new Graph(fact1);
        graph.bruteForceMethod();
        graph.outputGraph("after_bfm_");
        graph.outputFacts("", fact1);
    }

    public static void example2() {
        Fact fact1 = new Fact("1", Fact.Type.INITIAL, Fact.Operation.AND);
        Fact fact2 = new Fact("2", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact2.addInFacts(fact1);
        Fact fact3 = new Fact("3", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact3.addInFacts(fact1);
        Fact fact4 = new Fact("4", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact4.addInFacts(fact2);
        Fact fact5 = new Fact("5", Fact.Type.CENTRAL, Fact.Operation.AND);
        fact5.addInFacts(fact2);
        Fact fact6 = new Fact("6", Fact.Type.CENTRAL, Fact.Operation.AND);
        fact6.addInFacts(fact3);
        Fact fact7 = new Fact("7", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact7.addInFacts(fact3);
        Fact fact8 = new Fact("8", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact8.addInFacts(fact4);
        Fact fact9 = new Fact("9", Fact.Type.CENTRAL, Fact.Operation.AND);
        fact9.addInFacts(fact4);
        Fact fact10 = new Fact("10", Fact.Type.CENTRAL, Fact.Operation.OR);
        fact10.addInFacts(fact6);
        Fact factA = new Fact("A", Fact.Type.TERMINAL, null);
        factA.addInFacts(fact8);
        Fact factB = new Fact("B", Fact.Type.TERMINAL, null);
        factB.addInFacts(fact5);
        Fact factC = new Fact("C", Fact.Type.TERMINAL, null);
        factC.addInFacts(fact5);
        Fact factD = new Fact("D", Fact.Type.TERMINAL, null);
        factD.addInFacts(fact6);
        Fact factE = new Fact("E", Fact.Type.TERMINAL, null);
        factE.addInFacts(fact7);
        Fact factF = new Fact("F", Fact.Type.TERMINAL, null);
        factF.addInFacts(fact7);
        Fact factG = new Fact("G", Fact.Type.TERMINAL, null);
        factG.addInFacts(fact9);
        Fact factH = new Fact("H", Fact.Type.TERMINAL, null);
        factH.addInFacts(fact9);
        Fact factI = new Fact("I", Fact.Type.TERMINAL, null);
        factI.addInFacts(fact10);
        Fact factJ = new Fact("J", Fact.Type.TERMINAL, null);
        factJ.addInFacts(fact10);

        factG.setResolved(true);
        factH.setResolved(true);
        factD.setResolved(true);
        factI.setResolved(true);


        Graph graph = new Graph(fact1);
        graph.outputGraph("before_graph_example2_");
        Graph result = graph.bruteForceMethod();
       // Graph result = graph.from();
        result.outputGraph("after_graph_example2_");
        //result.outputFacts("", );
    }

//    public void bruteForceMethod(Fact top) {
//        Queue<Rule> openList = new LinkedList<>();
//        Queue<Rule> closeList = new LinkedList<>();
//
//        openList.add(top);
//
//        while (!openList.isEmpty()) {
//            Rule n = openList.remove();
//
//            closeList.add(n);
//            Set<Rule> andRules = n.getAndRules();
//            Set<Rule> orRules = n.getOrRules();
//            openList.addAll(andRules);
//            openList.addAll(orRules);
//
//            if (andRules.isEmpty() && orRules.isEmpty()) {
//                n.setResolved(false);
//                unsolvableVerticesMarking(n);
//
//                if (!top.isResolved()) {
//                    System.out.println("Неудача\n");
//                }
//            } else {
//                boolean isAllTerminal = true;
//                for (Rule r : andRules) {
//                    if (r.getType() != Rule.Type.TERMINAL) {
//                        isAllTerminal = false;
//                        break;
//                    }
//                }
//
//                for (Rule r : orRules) {
//                    if (r.getType() != Rule.Type.TERMINAL) {
//                        isAllTerminal = false;
//                        break;
//                    }
//                }
//
//                if (isAllTerminal) {
//                    for (Rule r : andRules) {
//                        r.setResolved(true);
//                    }
//                    for (Rule r : orRules) {
//                        r.setResolved(true);
//                    }
//                    n.setResolved(true);
//                }
//
//                solvableVerticesMarking(n);
//
//                if (top.isResolved()) {
//                    System.out.println("Успех");
//                }
//            }
//        }
//    }
//
//    public void solvableVerticesMarking(Rule rule) {
//        Rule temp = rule;
//        while (temp != null) {
//            if (temp.getParentRule() != null && temp.getParentRule().getAndRules().contains(temp)) {
//                temp.getParentRule().setResolved(temp.isResolved());
//            } else if (temp.getParentRule() != null && temp.getParentRule().getOrRules().contains(temp)) {
//                boolean isResolved = false;
//                for (Rule r : temp.getParentRule().getOrRules()) {
//                    if (r.isResolved()) {
//                        isResolved = true;
//                        break;
//                    }
//                }
//                temp.getParentRule().setResolved(isResolved);
//            }
//            temp = temp.getParentRule();
//        }
//    }
//
//    public void unsolvableVerticesMarking(Rule rule) {
//        Rule temp = rule;
//        while (temp != null) {
//            if (temp.getParentRule() != null && temp.getParentRule().getAndRules().contains(temp)) {
//                temp.getParentRule().setResolved(temp.isResolved());
//            } else if (temp.getParentRule() != null && temp.getParentRule().getOrRules().contains(temp)) {
//                boolean isResolved = false;
//                for (Rule r : temp.getParentRule().getOrRules()) {
//                    if (r.isResolved()) {
//                        isResolved = true;
//                        break;
//                    }
//                }
//                temp.getParentRule().setResolved(isResolved);
//            }
//            temp = temp.getParentRule();
//        }
//
//    }
}
