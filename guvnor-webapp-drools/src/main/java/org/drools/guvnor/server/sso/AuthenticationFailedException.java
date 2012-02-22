package org.drools.guvnor.server.sso;

/**
 *
 * @author esteban
 */
public class AuthenticationFailedException extends Exception {

    /**
     * Creates a new instance of
     * <code>AuthenticationFailedException</code> without detail message.
     */
    public AuthenticationFailedException() {
    }

    /**
     * Constructs an instance of <code>AuthenticationFailedException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AuthenticationFailedException(String msg) {
        super(msg);
    }
}
