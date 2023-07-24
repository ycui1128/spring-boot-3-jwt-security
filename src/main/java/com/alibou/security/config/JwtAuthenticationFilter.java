package com.alibou.security.config;

import com.alibou.security.token.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 这是一个 Spring Boot 中的自定义过滤器（Filter），称为 JwtAuthenticationFilter。
// 该过滤器用于拦截 HTTP 请求，并在请求中检查是否包含有效的 JWT (JSON Web Token) 令牌，以实现基于 JWT 的身份认证功能。
// 过滤器的作用是在请求到达控制器（Controller）之前或之后执行某些逻辑。在这个特定的过滤器中，它会在请求到达控制器之前进行以下操作：

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  // 构造方法，注入所需的依赖对象
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  // 重写 doFilterInternal 方法，该方法会在每个 HTTP 请求被处理时执行
  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    // 检查请求的路径是否包含 "/api/v1/auth"。如果是，表示这是用于用户身份认证的请求，因此直接放行，不进行后续操作，让 Spring Security 接管认证逻辑。
    if (request.getServletPath().contains("/api/v1/auth")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 检查请求头中是否包含 "Authorization" 信息或者 "Authorization" 信息不是以 "Bearer " 开头，也直接放行。
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 从请求头的 "Authorization" 中提取 JWT 令牌，并解析出用户的邮箱（或用户名，取决于 JWT 中存储的信息）。
    jwt = authHeader.substring(7);
    userEmail = jwtService.extractUsername(jwt);

    // 如果 JWT 令牌中包含有效的用户邮箱，并且当前的安全上下文（SecurityContextHolder）中没有认证过的用户，继续执行下面的逻辑：
    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      // 使用 Spring Security 提供的 UserDetailsService 加载用户的详细信息（如角色、权限等）。
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

      // 从数据库或存储中查找该 JWT 令牌是否存在并且没有过期或被撤销。
      var isTokenValid = tokenRepository.findByToken(jwt)
              .map(t -> !t.isExpired() && !t.isRevoked())  //使用map方法，对数据库里的token数据做判断
              .orElse(false); //如果找不到就判断无效

      // 如果 JWT 令牌是有效的，并且在数据库中也是有效的（没有过期或被撤销），则创建一个 UsernamePasswordAuthenticationToken 对象，并将用户详细信息设置为认证对象。
      if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities() // 此处设置用户权限
        );
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // 将认证对象设置到安全上下文中，表示该用户已通过认证。
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    // 继续将请求传递给下一个过滤器或最终的控制器。
    filterChain.doFilter(request, response);
  }
}

