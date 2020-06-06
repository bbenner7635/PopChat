package com.pc;

/** Listens to the status of a user. */
public interface UserStatusListener {

    public void online(String login);

    public void offline(String login);
}
