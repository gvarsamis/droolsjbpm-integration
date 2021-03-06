package org.drools.benchmark.benchmarks;

import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.A;
import org.drools.benchmark.model.B;
import org.kie.KnowledgeBase;
import org.kie.runtime.StatefulKnowledgeSession;

import java.util.Random;

public class CompareIndexRightModifyNoMatchOneLeft extends AbstractBenchmark {

    private final int aNr;
    private final int modifications;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private B[] bs;

    public CompareIndexRightModifyNoMatchOneLeft(int aNr, int modifications, String drlFile) {
        this.aNr = aNr;
        this.modifications = modifications;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();

        bs = new B[aNr];
        for (int i = 0; i < aNr; i++) {
            int randomInt = random.nextInt();
            bs[i] = new B(randomInt, randomInt+modifications);
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < aNr; i++) {
            ksession.insert(bs[i]);
        }
        ksession.insert(new A(Integer.MIN_VALUE));
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
