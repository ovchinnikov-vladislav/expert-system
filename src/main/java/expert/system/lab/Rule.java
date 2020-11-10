package expert.system.lab;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Rule {

    private UUID id = UUID.randomUUID();
    private String name;
    private Operation operation;
    private Set<Fact> inFacts = new LinkedHashSet<>();
    private Set<Fact> outFacts = new LinkedHashSet<>();
    private boolean resolved;
    private int level;

    public Rule() {}

    public Rule(String name, Operation operation) {
        this.name = name;
        this.operation = operation;
    }

    public void addInFacts(Fact... facts) {
        inFacts.addAll(List.of(facts));
        for (Fact fact : inFacts) {
            if (!fact.getOutRules().contains(this)) {
                fact.addOutRules(this);
            }
        }
    }

    public void addOutFacts(Fact... facts) {
        outFacts.addAll(List.of(facts));
        for (Fact fact : outFacts) {
            if (!fact.getInRules().contains(this)) {
                fact.addInRules(this);
            }
        }
    }

    public Set<Fact> terminalOutFacts() {
        int countTerminalOutFacts = 0;
        Set<Fact> terminalFacts = new HashSet<>();
        for (Fact f : outFacts) {
            if (f.getOutRules().size() == 0) {
                terminalFacts.add(f);
            }
        }
        return terminalFacts;
    }

    public Rule from() {
        Rule rule = new Rule();
        rule.setId(this.id);
        rule.setOperation(this.operation);
        rule.setName(this.name);
        rule.setResolved(this.resolved);

        return rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return id.equals(rule.id) &&
                Objects.equals(name, rule.name) &&
                operation == rule.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, operation);
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", operation=" + operation +
                '}';
    }

    public enum Operation {
        AND, OR
    }
}
