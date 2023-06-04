package clients.soap;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.soap.FeedsService;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import zookeeper.FeedsOperation;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

public class FeedsSoapClient extends SoapClient implements Feeds {

    private FeedsService stub;

    public FeedsSoapClient(URI serverURI) {
        super(serverURI);
    }

    synchronized private FeedsService stub() {
        if (stub == null) {
            QName qName = new QName(FeedsService.NAMESPACE, FeedsService.NAME);
            Service service = Service.create(toURL(serverURI + WSDL), qName);
            this.stub = service.getPort(FeedsService.class);
            super.setTimeouts((BindingProvider) stub);
        }
        return stub;
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return reTry(() -> responseToResult(() -> stub().postMessage(user, pwd, msg)));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return reTry(() -> responseToResult(() -> stub().removeFromPersonalFeed(user, mid, pwd)));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return reTry(() -> responseToResult(() -> stub().getMessage(user, mid)));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return reTry(() -> responseToResult(() -> stub().getMessages(user, time)));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return reTry(() -> responseToResult(() -> stub().subUser(user, userSub, pwd)));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return reTry(() -> responseToResult(() -> stub().unsubscribeUser(user, userSub, pwd)));
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return reTry(() -> responseToResult(() -> stub().listSubs(user)));
    }

    @Override
    public Result<Void> propagateMessage(Message message, String secret) {
        return reTry(() -> responseToResult(() -> stub().propagateMessage(message, secret)));
    }

    @Override
    public Result<Void> deleteUserData(String user, String secret) {
        return reTry(() -> responseToResult(() -> stub().deleteUserData(user, secret)));
    }

    @Override
    public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

}
