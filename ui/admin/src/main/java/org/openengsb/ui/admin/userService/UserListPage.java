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

package org.openengsb.ui.admin.userService;

import org.apache.wicket.PageParameters;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

import com.google.common.collect.ImmutableMap;

@SecurityAttribute(value = "USER_ADMIN", action = "RENDER")
@PaxWicketMountPoint(mountPoint = "users")
public class UserListPage extends BasePage {

    private final class MyUserListPanel extends UserListPanel {
        private static final long serialVersionUID = 4561294480791309137L;

        private MyUserListPanel(String id) {
            super(id);
        }

        @Override
        public void gotoEditorPage(String username) {
            if (username != null) {
                setResponsePage(UserEditPage.class, new PageParameters(ImmutableMap.of("user", username)));
            } else {
                setResponsePage(UserEditPage.class);
            }
        }
    }

    public UserListPage() {
        initContent();
    }

    public UserListPage(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    private void initContent() {
        add(new MyUserListPanel("lazy"));

    }

}