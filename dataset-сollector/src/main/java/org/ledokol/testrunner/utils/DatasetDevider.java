package org.ledokol.testrunner.utils;

import org.ledokol.testrunner.model.LearningSet;
import org.ledokol.testrunner.model.ParameterSet;
import org.ledokol.testrunner.model.TestSet;
import org.ledokol.testrunner.repository.LearningSetRepository;
import org.ledokol.testrunner.repository.ParameterSetRepository;
import org.ledokol.testrunner.repository.TestSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetDevider {
    @Autowired
    private ParameterSetRepository parameterSetRepository;

    @Autowired
    private TestSetRepository testSetRepository;

    @Autowired
    private LearningSetRepository learningSetRepository;


    public void printMeanCPUValues(){
        List<ParameterSet> allSets = parameterSetRepository.findAll();
        allSets.forEach(set -> System.out.println("id " + set.getId() + ", meanCPU: "+set.getMeanCPU()));
    }

    public void devide(){
        List<ParameterSet> allSets = parameterSetRepository.findAll();
        for (int i = 0; i < allSets.size(); i++) {
            ParameterSet parameterSet = allSets.get(i);
            if (parameterSet.getIs_done()) {
                if (i % 5 == 0) {
                    TestSet testSet = new TestSet(parameterSet);
                    testSetRepository.save(testSet);
                } else {
                    learningSetRepository.save(new LearningSet(parameterSet));
                }
            }
        }
        System.out.println("done");
    }
}
