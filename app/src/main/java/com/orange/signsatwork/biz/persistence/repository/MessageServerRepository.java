package com.orange.signsatwork.biz.persistence.repository;

import com.orange.signsatwork.biz.persistence.model.MessageServerDB;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageServerRepository extends CrudRepository<MessageServerDB, Long> {

  @Query("select distinct m FROM MessageServerDB m order by m.date asc")
  List<MessageServerDB> findMessagesServerAllAsc();

  @Query("select distinct m FROM MessageServerDB m order by m.date desc")
  List<MessageServerDB> findMessagesServerAllDesc();
}
