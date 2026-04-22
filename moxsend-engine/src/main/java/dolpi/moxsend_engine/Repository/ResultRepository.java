package dolpi.moxsend_engine.Repository;

import dolpi.moxsend_engine.Model.Result;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends MongoRepository<Result, String> {
    List<Result> findByJobId(String jobId);
}
