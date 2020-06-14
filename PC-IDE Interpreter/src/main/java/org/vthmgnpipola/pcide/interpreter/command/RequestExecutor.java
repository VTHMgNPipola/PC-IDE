package org.vthmgnpipola.pcide.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class RequestExecutor implements Flow.Subscriber<ClientRequest> {
    private Flow.Subscription subscription;
    private RequestReceiver requestReceiver;
    private List<Command> commands;

    public RequestExecutor() {
        commands = new ArrayList<>();
    }

    public void setRequestReceiver(RequestReceiver requestReceiver) {
        this.requestReceiver = requestReceiver;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(ClientRequest request) {
        subscription.request(1); // Doing this way prevents the program from stop working because 9223372036854775807
        // requests were done

        for (Command command : commands) {
            if (command.command.equals(request.getRequest())) {
                command.run(request);
                break;
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
        subscription.cancel();
    }
}
