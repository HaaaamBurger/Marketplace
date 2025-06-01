package com.marketplace.order.config;

import com.marketplace.order.web.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexesConfig implements InitializingBean {

    private final MongoTemplate mongoTemplate;

    @Override
    public void afterPropertiesSet() {
        mongoTemplate.indexOps(Order.class)
                .ensureIndex(
                    new Index()
                            .on("ownerId", Sort.Direction.ASC)
                            .unique()
                            .partial(PartialIndexFilter.of(Criteria.where("status").is("IN_PROGRESS")))
                            .named("unique_in_progress_order_per_owner")
                    );
    }
}
