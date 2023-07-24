package com.alibou.security.token;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Integer> {

  @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByUser(Integer id); //该方法的作用是根据传入的用户 id 参数，从数据库中查询该用户的有效令牌列表，即未过期且未被撤销的令牌。

  Optional<Token> findByToken(String token);
}
