/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.security.usermanagement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.internal.UserDataInitializerBean;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.openengsb.core.security.model.OpenEngSBGrantedAuthority;
import org.openengsb.core.security.model.Permission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.Role;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.ServicePermission;
import org.openengsb.core.security.model.SimpleUser;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Sets;

public class UserManagerImplIT extends AbstractOpenEngSBTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private EntityManager entityManager;

    private UserManager userManager;

    private SimpleUser testUser2;
    private SimpleUser testUser3;

    @Before
    public void setUp() throws Exception {
        setupPersistence();
        setupUserManager();
        testUser2 = new SimpleUser("testUser2", "testPass");
        entityManager.persist(testUser2);
        testUser3 = new SimpleUser("testUser3", "testPass");
        entityManager.persist(testUser3);
    }

    private void setupPersistence() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.ConnectionURL", "jdbc:h2:" + tmpFolder.getRoot().getAbsolutePath() + "/TEST");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("security-test", props);
        final EntityManager entityManager = emf.createEntityManager();
        this.entityManager = entityManager;
    }

    private void setupUserManager() {
        final UserManagerImpl userManager = new UserManagerImpl();
        userManager.setEntityManager(entityManager);
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                entityManager.getTransaction().begin();
                Object result;
                try {
                    result = method.invoke(userManager, args);
                } catch (InvocationTargetException e) {
                    entityManager.getTransaction().rollback();
                    throw e.getCause();
                }
                entityManager.getTransaction().commit();
                return result;
            }
        };
        this.userManager =
            (UserManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ UserManager.class },
                invocationHandler);
    }

    @Test
    public void testToCreateUser_ShouldWork() throws Exception {
        UserDetails user = Users.create("testUser1", "testPass");
        userManager.createUser(user);
        UserDetails loadUserByUsername = userManager.loadUserByUsername("testUser1");
        assertThat(loadUserByUsername, is(user));
    }

    @Test(expected = UserExistsException.class)
    public void testToCreateUserWhichAlreadyExists_shouldNotWork() throws Exception {
        User user = Users.create("testUser2", "testPass");
        userManager.createUser(user);
    }

    @Test
    public void testToLoadAnExistingUser_ShouldWork() {
        UserDetails user = userManager.loadUserByUsername("testUser2");
        assertThat(user.getUsername(), is("testUser2"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testToLoadAnNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.loadUserByUsername("testUser1");
    }

    @Test
    public void updateUser_ShouldWork() throws Exception {
        User userNew = Users.create("testUser2", "testPassNew");
        userManager.updateUser(userNew);
        assertThat(userManager.loadUserByUsername("testUser2").getPassword(), is("testPassNew"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void updateNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        User userNew = Users.create("testUser1", "testPassNew");
        userManager.updateUser(userNew);
    }

    @Test
    public void deleteUser_ShouldWork() throws Exception {
        userManager.deleteUser("testUser3");
        assertNull(entityManager.find(SimpleUser.class, "testUser3"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void deleteNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.deleteUser("testUser1");
    }

    @Test
    public void testInitMethodCreateNewUserIfNoUserIsPresent() throws Exception {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM SimpleUser").executeUpdate();
        entityManager.getTransaction().commit();
        UserDataInitializerBean userDataInitializerBean = new UserDataInitializerBean();
        userDataInitializerBean.setUserManager(userManager);
        userDataInitializerBean.doInit();
        UserDetails loadUserByUsername = userManager.loadUserByUsername("admin");
        assertThat(loadUserByUsername.getPassword(), is("password"));
    }

    @Test
    public void testGetAllUser_ShouldContain2Users() {
        List<String> allUser = userManager.getUsernameList();
        assertThat(allUser, hasItems(testUser2.getUsername(), testUser3.getUsername()));
    }

    @Test
    public void createUserWithRoles_shouldContainRoles() throws Exception {
        Role role = new Role("ROLE_USER");
        GrantedAuthority roleAuthority = new RoleAuthority(role);
        Collection<GrantedAuthority> authorities = Sets.newHashSet(roleAuthority);
        UserDetails user = Users.create("testuser", "password", authorities);
        userManager.createUser(user);

        UserDetails loadedUser = userManager.loadUserByUsername("testuser");
        assertThat(loadedUser.getAuthorities(), hasItem(roleAuthority));
    }

    @Test
    public void createUserWithPermissions_shouldContainPermissions() throws Exception {
        Permission p = new ServicePermission("test");
        GrantedAuthority authority = new PermissionAuthority(p);
        Collection<GrantedAuthority> authorities = Sets.newHashSet(authority);
        UserDetails user = Users.create("testuser", "password", authorities);
        userManager.createUser(user);

        UserDetails loadedUser = userManager.loadUserByUsername("testuser");
        assertThat(loadedUser.getAuthorities(), hasItem(authority));
    }

    @Test
    public void setRolePermissions_shouldReflectOnUser() throws Exception {
        Permission p = new ServicePermission("test");
        Role role = new Role("test", Sets.newHashSet(p));
        GrantedAuthority authority = new RoleAuthority(role);
        Collection<GrantedAuthority> authorities = Sets.newHashSet(authority);
        UserDetails user = Users.create("testuser", "password", authorities);
        userManager.createUser(user);

        UserDetails loadedUser = userManager.loadUserByUsername("testuser");
        GrantedAuthority authority2 = loadedUser.getAuthorities().iterator().next();
        assertThat(authority2, instanceOf(OpenEngSBGrantedAuthority.class));
        OpenEngSBGrantedAuthority openengsbAuthority = (OpenEngSBGrantedAuthority) authority2;
        assertThat(openengsbAuthority.getPermissions(), hasItem(p));
    }

    @Test
    public void changeRolePermissions_shouldReflectOnUser() throws Exception {
        Permission p = new ServicePermission("test");
        Role role = new Role("test", Sets.newHashSet(p));
        GrantedAuthority authority = new RoleAuthority(role);
        Collection<GrantedAuthority> authorities = Sets.newHashSet(authority);
        UserDetails user = Users.create("testuser", "password", authorities);
        userManager.createUser(user);

        role.getPermissions().clear();
        entityManager.getTransaction().begin();
        entityManager.merge(role);
        entityManager.getTransaction().commit();

        UserDetails loadedUser = userManager.loadUserByUsername("testuser");
        GrantedAuthority authority2 = loadedUser.getAuthorities().iterator().next();
        assertThat(authority2, instanceOf(OpenEngSBGrantedAuthority.class));
        OpenEngSBGrantedAuthority openengsbAuthority = (OpenEngSBGrantedAuthority) authority2;
        assertThat(openengsbAuthority.getPermissions(), not(hasItem(p)));
    }

    @Test
    public void createUsersWithTwoRoles_shouldWork() throws Exception {
        GrantedAuthority roleAuthority = new RoleAuthority(new Role("test"));
        Role role2 = new Role("test2");
        GrantedAuthority roleAuthority2 = new RoleAuthority(role2);
        Role role3 = new Role("test3");
        RoleAuthority roleAuthority3 = new RoleAuthority(role3);

        UserDetails user = Users.create("xx", "password", Sets.newHashSet(roleAuthority, roleAuthority3));
        userManager.createUser(user);
        UserDetails user2 = Users.create("xx2", "password", Sets.newHashSet(roleAuthority2, roleAuthority3));
        userManager.createUser(user2);

        TypedQuery<SimpleUser> query =
            entityManager.createQuery("SELECT r FROM SimpleUser r WHERE (SELECT COUNT(p) FROM r.roles p) > 0 ",
                SimpleUser.class);
        assertThat(query.getResultList().size(), is(2));

        TypedQuery<Role> query2 = entityManager.createQuery("SELECT r FROM Role R", Role.class);
        assertThat(query2.getResultList().size(), is(3));
    }

    @Test
    public void createUsersWithRoles_shouldMaintainRelationBothWays() throws Exception {
        GrantedAuthority roleAuthority = new RoleAuthority(new Role("test"));
        UserDetails user = Users.create("xx", "password", Sets.newHashSet(roleAuthority));
        userManager.createUser(user);

        Role role = entityManager.find(Role.class, "test");

        // because this does not work:
        // assertThat(role.getMembers(), hasItem(hasProperty("username", is("xx"))));
        @SuppressWarnings("unchecked")
        Collection<Object> members2 = CollectionUtils.unmodifiableCollection(role.getMembers());
        assertThat(members2, hasItem(hasProperty("username", is("xx"))));
    }
}