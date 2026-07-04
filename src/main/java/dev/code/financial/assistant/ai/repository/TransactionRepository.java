package dev.code.financial.assistant.ai.repository;

import dev.code.financial.assistant.ai.dto.TotalAmount;
import dev.code.financial.assistant.ai.dto.TransactionFrequent;
import dev.code.financial.assistant.ai.entity.Transaction;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByCategoryAndDatetimeBetween(String category, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByDatetimeBetween(LocalDateTime from, LocalDateTime to);

    @Aggregation(pipeline = {
            "{ $match: { category: ?0 } }",
            "{ $group: { _id: '$description', times: { $sum: 1 } } }",
            "{ $project: { _id: 0, description: '$_id', times: 1 } }",
            "{ $sort: { count: -1 } }"
    })
    List<TransactionFrequent> getMostFrequentDescriptionsByCategory(String category);

    @Aggregation(pipeline = {
            "{ $match: { description: { $regex: ?0, $options: 'i' }, datetime: { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: null, total: { $sum: '$amount' } } }",
            "{ $project: { _id: 0, total: 1 } }"
    })
    Optional<TotalAmount> getTotalByDescriptionAndDateRange(String keyword, LocalDateTime from, LocalDateTime to);

}
