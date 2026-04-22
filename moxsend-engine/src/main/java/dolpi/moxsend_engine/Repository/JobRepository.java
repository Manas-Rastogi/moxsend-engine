package dolpi.moxsend_engine.Repository;

import dolpi.moxsend_engine.Model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
}
