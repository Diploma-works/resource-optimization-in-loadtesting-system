package org.ledokol.testrunner.repository;

import org.bson.types.ObjectId;
import org.ledokol.testrunner.model.ParameterSet;
import org.ledokol.testrunner.model.TestSet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestSetRepository extends MongoRepository<TestSet, ObjectId> {
}
