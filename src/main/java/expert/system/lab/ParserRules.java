package expert.system.lab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ParserRules {

    public static Graph parse(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file));
        Set<Fact> facts = new LinkedHashSet<>();
        Iterator<String> iterator = lines.iterator();

        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.equalsIgnoreCase("facts:")) {
                continue;
            }
            if (str.equalsIgnoreCase("target:")) {
                break;
            }
            if (str.equalsIgnoreCase("rules:")) {
                System.err.println("undifined target");
                System.exit(0);
            }

            if (!str.isBlank()) {
                String[] split = str.split(",");
                for (String nameFact : split) {
                    nameFact = nameFact.trim();
                    Fact fact = new Fact(nameFact);
                    facts.add(fact);
                }
            }
        }

        Fact start = new Fact("undefined");
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.equalsIgnoreCase("true:")) {
                break;
            }
            if (str.equalsIgnoreCase("rules:")) {
                System.err.println("undifined target");
                System.exit(0);
            }
            if (!str.isBlank()) {
                start = new Fact(str.trim());
                if (!facts.contains(start)) {
                    System.err.printf("Fact %s dont define\n", start.getName());
                    System.exit(0);
                } else {
                    for (Fact f : facts) {
                        if (f.equals(start)) {
                            start = f;
                        }
                    }
                }
            }
        }

        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.equalsIgnoreCase("true:")) {
                continue;
            }
            if (str.equalsIgnoreCase("rules:")) {
                break;
            }

            if (!str.isBlank()) {
                String[] split = str.split(",");
                for (String nameFact : split) {
                    nameFact = nameFact.trim();
                    Fact fact = new Fact(nameFact);
                    facts.add(fact);
                    if (!facts.contains(fact)) {
                        System.err.printf("Fact %s dont define\n", fact.getName());
                        System.exit(0);
                    } else {
                        for (Fact f : facts) {
                            if (f.equals(fact)) {
                                fact = f;
                                fact.setResolved(true);
                            }
                        }
                    }
                }
            }
        }

        while (iterator.hasNext()) {
            String str = iterator.next();
            if (!str.isBlank()) {
                String[] split = str.split("->");
                if (split.length > 2 || split.length == 0) {
                    System.err.println("ERROR parsing rules");
                    System.exit(-1);
                }

                String rule = split[0].trim();
                Fact resultFact = new Fact(split[1].trim());
                if (!facts.contains(resultFact)) {
                    System.err.printf("Fact %s dont define\n", resultFact.getName());
                    System.exit(0);
                } else {
                    for (Fact f : facts) {
                        if (f.equals(resultFact)) {
                            resultFact = f;
                        }
                    }
                }

                if (start.equals(resultFact)) {
                    start = resultFact;
                }

                split = rule.split("or");
                if (split.length == 2) {
                    Fact firstFact = new Fact(split[0].trim());
                    if (!facts.contains(firstFact)) {
                        System.err.printf("Fact %s dont define\n", firstFact.getName());
                        System.exit(0);
                    } else {
                        for (Fact f : facts) {
                            if (f.equals(firstFact)) {
                                firstFact = f;
                            }
                        }
                    }

                    Fact secondFact = new Fact(split[1].trim());
                    if (!facts.contains(secondFact)) {
                        System.err.printf("Fact %s dont define\n", secondFact.getName());
                        System.exit(0);
                    } else {
                        for (Fact f : facts) {
                            if (f.equals(secondFact)) {
                                secondFact = f;
                            }
                        }
                    }

                    Rule r = new Rule("or", Rule.Operation.OR);
                    r.addInFacts(resultFact);
                    r.addOutFacts(firstFact, secondFact);
                } else {
                    split = rule.split("and");
                    if (split.length == 2) {
                        Fact firstFact = new Fact(split[0].trim());
                        if (!facts.contains(firstFact)) {
                            System.err.printf("Fact %s dont define\n", firstFact.getName());
                            System.exit(0);
                        } else {
                            for (Fact f : facts) {
                                if (f.equals(firstFact)) {
                                    firstFact = f;
                                }
                            }
                        }

                        Fact secondFact = new Fact(split[1].trim());
                        if (!facts.contains(secondFact)) {
                            System.err.printf("Fact %s dont define\n", secondFact.getName());
                            System.exit(0);
                        } else {
                            for (Fact f : facts) {
                                if (f.equals(secondFact)) {
                                    secondFact = f;
                                }
                            }
                        }

                        Rule r = new Rule("and", Rule.Operation.AND);
                        r.addInFacts(resultFact);
                        r.addOutFacts(firstFact, secondFact);
                    } else {
                        Fact fact = new Fact(split[0].trim());
                        if (!facts.contains(fact)) {
                            System.err.printf("Fact %s dont define\n", fact.getName());
                            System.exit(0);
                        } else {
                            for (Fact f : facts) {
                                if (f.equals(fact)) {
                                    fact = f;
                                }
                            }
                        }

                        Rule r = new Rule("and", Rule.Operation.AND);
                        r.addInFacts(resultFact);
                        r.addOutFacts(fact);
                    }
                }


            }
        }

        return new Graph(start);
    }

}
