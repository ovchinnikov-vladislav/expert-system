package expert.system.lab;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Fact {

    private UUID id = UUID.randomUUID();
    private String name;
    private final Set<Rule> inRules = new LinkedHashSet<>();
    private final Set<Rule> outRules = new LinkedHashSet<>();
    private Type type;
    private boolean isResolved;
    private int level;

    public Fact(String name) {
        this.name = name;
    }

    public Fact(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void addInRules(Rule... rules) {
        inRules.addAll(List.of(rules));
        for (Rule rule : inRules) {
            if (!rule.getOutFacts().contains(this)) {
                rule.addOutFacts(this);
            }
        }
    }

    public void addOutRules(Rule... rules) {
        outRules.addAll(List.of(rules));
        for (Rule rule : outRules) {
            if (!rule.getInFacts().contains(this)) {
                rule.addInFacts(this);
            }
        }
    }

    public Fact from() {
        Fact fact = new Fact(this.name);
        fact.setId(this.id);
        fact.setType(this.type);
        fact.setResolved(this.isResolved);

        return fact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fact fact = (Fact) o;
        return Objects.equals(name, fact.name) &&
                type == fact.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, isResolved);
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isResolved=" + isResolved +
                '}';
    }

    public enum Type {
        INITIAL, CENTRAL, TERMINAL
    }
}
