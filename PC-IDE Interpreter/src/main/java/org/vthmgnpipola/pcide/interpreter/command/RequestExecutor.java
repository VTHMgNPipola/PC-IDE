package org.vthmgnpipola.pcide.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.interpreter.command.impl.HasTasksCommand;
import org.vthmgnpipola.pcide.interpreter.command.impl.ListLanguagesCommand;

public class RequestExecutor implements Flow.Subscriber<ClientRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);

    private Flow.Subscription subscription;
    private final List<Command> commands;

    public RequestExecutor() {
        commands = new ArrayList<>();

        // TODO: Make org.reflections work so that I can remove this and dynamically add commands
        commands.add(new ListLanguagesCommand());
        commands.add(new HasTasksCommand());
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE); // I didn't want to this because when the requests end the server stops
        // for no reason, but Long.MAX_VALUE will last for quite some time before running out. And the infinite
        // request solution was causing problems.
    }

    @Override
    public void onNext(ClientRequest request) {
        for (Command command : commands) {
            if (command.command.equals(request.getRequest())) {
                logger.info(String.format("Running command %s", request.getRequest()));
                Object response = command.run(request);
                request.sendResponse(response);
                return;
            }
        }

        request.sendResponse(null); // If the requested command was not found send null
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
        subscription.cancel();
    }
}
