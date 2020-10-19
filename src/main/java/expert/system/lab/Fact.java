package expert.system.lab;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Fact {

    private UUID id = UUID.randomUUID();
    private String name;
    private Type type;
    private Operation operation;
    private Fact inFact;
    private Set<Fact> outFacts = new LinkedHashSet<>();
    private boolean isFiction;
    private boolean isResolved;

    public Fact() {}

    public Fact(String name, Type type, Operation operation) {
        this.name = name;
        this.type = type;
        this.operation = operation;
    }

    public void setInFact(Fact inFact) {
        this.inFact = inFact;
        inFact.outFacts.add(this);
    }

    public void addOutFacts(Fact... facts) {
        outFacts.addAll(List.of(facts));
    }

    public boolean isAllTerminalOutFacts() {
        int countTerminalOutFacts = 0;
        for (Fact f : outFacts) {
            if (f.getType() == Type.TERMINAL) {
                countTerminalOutFacts++;
            }
        }
        return countTerminalOutFacts == outFacts.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fact fact = (Fact) o;
        return isFiction == fact.isFiction &&
                isResolved == fact.isResolved &&
                Objects.equals(name, fact.name) &&
                type == fact.type &&
                operation == fact.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, operation, isFiction, isResolved);
    }

    @Override
    public String toString() {
        return "Fact{" +
                "name='" + name + '\'' +
                '}';
    }

    public enum Type {
        INITIAL, CENTRAL, TERMINAL
    }

    public enum Operation {
        AND, OR
    }
}
