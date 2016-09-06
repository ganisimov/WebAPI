package org.ohdsi.webapi.service;

import com.gs.collections.impl.block.factory.Comparators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */

@Path("/")
@Component
public class UserService {

  @Autowired
  private PermissionManager authorizer;

  public static class User implements Comparable<User> {
    public Long id;
    public String login;

    public User() {}

    public User(UserEntity userEntity) {
      this.id = userEntity.getId();
      this.login = userEntity.getLogin();
    }

    @Override
    public int compareTo(User o) {
      Comparator c = Comparators.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.login, o.login);
      else
        return c.compare(this.id, o.id);
    }
  }

  public static class Permission implements Comparable<Permission> {
    public Long id;
    public String permission;
    public String description;

    public Permission() {}

    public Permission(PermissionEntity permissionEntity) {
      this.id = permissionEntity.getId();
      this.permission = permissionEntity.getValue();
      this.description = permissionEntity.getDescription();
    }

    @Override
    public int compareTo(Permission o) {
      Comparator c = Comparators.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.permission, o.permission);
      else
        return c.compare(this.id, o.id);
    }
  }

  public static class Role implements Comparable<Role> {
    public Long id;
    public String role;

    public Role() {}

    public Role (RoleEntity roleEntity) {
      this.id = roleEntity.getId();
      this.role = roleEntity.getName();
    }

    @Override
    public int compareTo(Role o) {
      Comparator c = Comparators.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.role, o.role);
      else
        return c.compare(this.id, o.id);
    }

  }

  @GET
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<User> getUsers() {
    Iterable<UserEntity> userEntities = this.authorizer.getUsers();
    ArrayList<User> users = convertUsers(userEntities);
    return users;
  }

  @GET
  @Path("user/loggedIn")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean isLoggedIn() {
    // since this method is protected with authc filter, if we're here, user is logged in
    return true;
  }

  @POST
  @Path("user/permitted")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public boolean isPermitted (String permission) {
    boolean isPermitted = this.authorizer.isPermitted(permission);
    return isPermitted;
  }

  @GET
  @Path("user/{userId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Permission> getUsersPermissions(@PathParam("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @GET
  @Path("user/{userId}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Role> getUserRoles(@PathParam("userId") Long userId) throws Exception {
    Set<RoleEntity> roleEntities = this.authorizer.getUserRoles(userId);
    ArrayList<Role> roles = convertRoles(roleEntities);
    Collections.sort(roles);
    return roles;
  }

  @PUT
  @Path("role")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Role createRole(Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.addRole(role.role);
    Role newRole = new Role(roleEntity);
    return newRole;
  }

  @GET
  @Path("role")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Role> getRoles() {
    Iterable<RoleEntity> roleEntities = this.authorizer.getRoles();
    ArrayList<Role> roles = convertRoles(roleEntities);
    return roles;
  }

  @DELETE
  @Path("role/{roleId}")
  public void removeRole(@PathParam("roleId") Long roleId) {
    this.authorizer.removeRole(roleId);
  }

  @GET
  @Path("role/{roleId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Permission> getRolePermissions(@PathParam("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @PUT
  @Path("role/{roleId}/permissions/{permissionId}")
  public void addPermissionToRole(@PathParam("roleId") Long roleId, @PathParam("permissionId") Long permissionId) throws Exception {
    this.authorizer.addPermission(roleId, permissionId);
  }

  @DELETE
  @Path("role/{roleId}/permissions/{permissionId}")
  public void removePermissionFromRole(@PathParam("roleId") Long roleId, @PathParam("permissionId") Long permissionId) {
    this.authorizer.removePermission(permissionId, roleId);
  }

  @GET
  @Path("role/{roleId}/users")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<User> getRoleUsers(@PathParam("roleId") Long roleId) throws Exception {
    Set<UserEntity> userEntities = this.authorizer.getRoleUsers(roleId);
    ArrayList<User> users = this.convertUsers(userEntities);
    Collections.sort(users);
    return users;
  }

  @PUT
  @Path("role/{roleId}/users/{userId}")
  public void addUserToRole(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) throws Exception {
    this.authorizer.addUser(userId, roleId);
  }

  @DELETE
  @Path("role/{roleId}/users/{userId}")
  public void removeUserFromRole(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
    this.authorizer.removeUser(userId, roleId);
  }

  @GET
  @Path("permission")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Permission> getPermissions() {
    Iterable<PermissionEntity> permissionEntities = this.authorizer.getPermissions();
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @PUT
  @Path("permission")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Permission addPermissions(Permission permission) throws Exception {
    PermissionEntity permissionEntity = this.authorizer.addPermission(permission.permission, permission.description);
    Permission newPermission = new Permission(permissionEntity);
    return newPermission;
  }

  @DELETE
  @Path("permission/{permissionId}")
  public void deletePermission(@PathParam("permissionId") Long permissionId) {
    this.authorizer.removePermission(permissionId);
  }



  private ArrayList<Permission> convertPermissions(final Iterable<PermissionEntity> permissionEntities) {
    ArrayList<Permission> permissions = new ArrayList<Permission>();
    for (PermissionEntity permissionEntity : permissionEntities) {
      Permission permission = new Permission(permissionEntity);
      permissions.add(permission);
    }

    return permissions;
  }

  private ArrayList<Role> convertRoles(final Iterable<RoleEntity> roleEntities) {
    ArrayList<Role> roles = new ArrayList<Role>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = new Role(roleEntity);
      roles.add(role);
    }

    return roles;
  }

  private ArrayList<User> convertUsers(final Iterable<UserEntity> userEntities) {
    ArrayList<User> users = new ArrayList<User>();
    for (UserEntity userEntity : userEntities) {
      User user = new User(userEntity);
      users.add(user);
    }

    return users;
  }
}
