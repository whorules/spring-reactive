package com.korovko.reactive.repository;

import com.korovko.reactive.entity.UserInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserInfoRepository extends ReactiveMongoRepository<UserInfo, String> {}
