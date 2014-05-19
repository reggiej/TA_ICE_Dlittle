// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remotecommand;

import oracle.toplink.remotecommand.Command;

/**
 * <p>
 * <b>Purpose</b>: Define a pluggable conversion interface that can be supplied
 * by the application
 * <p>
 * <b>Description</b>: The implementation class of this interface should be set
 * on the remote command manager through the setCommandConverter() method. The
 * implementation class will get invoked, through its convertToTopLinkCommand(),
 * to convert the application command format into a TopLink Command object that
 * can be propagated throughout the cluster. Similarly, convertToUserCommand()
 * will be invoked on the implementation class to give the application an
 * opportunity to convert the TopLink Command object into an object that is suitable
 * for being processed by the application.
 *
 * @see Command
 * @see CommandManager
 * @author Steven Vo
 * @since OracleAS TopLink 10<i>g</i> (9.0.4)
 */
public interface CommandConverter {

    /**
     * PUBLIC:
     * Convert a command from its application-specific format to
     * a TopLink Command object.
     *
     * @param command An application-formatted command
     * @return The converted Command object that will be sent to remote services
     */
    public Command convertToTopLinkCommand(Object command);

    /**
     * PUBLIC:
     * Convert a TopLink Command object into its application-specific
     * format to a
     *
     * @param command An application-formatted command
     * @return The converted Command object that will be sent to be remotely executed
     */
    public Object convertToUserCommand(Command command);
}