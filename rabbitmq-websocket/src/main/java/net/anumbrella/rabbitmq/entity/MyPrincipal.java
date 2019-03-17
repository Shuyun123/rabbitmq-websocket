package net.anumbrella.rabbitmq.entity;

import java.security.Principal;

/**
 * @author Anumbrella
 */
public class MyPrincipal implements Principal {

    private String loginName;

    public MyPrincipal(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getName() {
        return loginName;
    }
}
