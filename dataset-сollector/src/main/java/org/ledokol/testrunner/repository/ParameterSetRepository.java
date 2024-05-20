package org.ledokol.testrunner.repository;

import org.bson.types.ObjectId;
import org.ledokol.testrunner.model.ParameterSet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterSetRepository extends MongoRepository<ParameterSet, ObjectId> {
}
