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

package org.openengsb.core.security.model;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Table(name = "SIMPLEUSER")
@Entity
public class SimpleUser {

    @Id
    private String username;
    private String password;
    @ManyToMany(cascade = { CascadeType.PERSIST })
    private Collection<Role> roles;
    @OneToMany(cascade = { CascadeType.PERSIST })
    private Collection<Permission> permissions;

    public SimpleUser(String username) {
        this.username = username;
    }

    public SimpleUser(String username, String password) {
        this(username);
        this.password = password;
    }

    public SimpleUser() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
        Iterable<Role> outdatedRoles = Iterables.filter(roles, new Predicate<Role>() {
            @Override
            public boolean apply(Role input) {
                return !input.getMembers().contains(this);
            }
        });
        for (Role r : outdatedRoles) {
            r.addMember(this);
        }
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return username + " [password hidden] " + roles;
    }

    public void addRole(Role role) {
        roles.add(role);
        if (!role.getMembers().contains(this)) {
            role.addMember(this);
        }
    }

    public void removeRole(Role role) {
        roles.remove(role);
        if (role.getMembers().contains(this)) {
            role.removeMember(this);
        }
    }

}