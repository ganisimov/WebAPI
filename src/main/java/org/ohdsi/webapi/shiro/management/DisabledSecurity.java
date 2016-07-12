package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class DisabledSecurity extends Security {

    @Override
    public void checkPermission(String permission) {
        return;
    }

    @Override
    public void login(AuthenticationToken token) {
        return;
    }

    @Override
    public void registerUser(String login) {
        return;
    }

    @Override
    public String getAccessToken(String login) {
        return null;
    }

  @Override
  public Map<String, String> getFilterChain() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Filter> getFilters() {
    return new HashMap<>();
  }

  @Override
  public Set<Realm> getRealms() {
    return new HashSet<>();
  }

  @Override
  public Authenticator getAuthenticator() {
    return new ModularRealmAuthenticator();
  }

}
