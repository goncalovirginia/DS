package zookeeper;

import java.util.List;

public record FeedsOperation(long version, FeedsOperationType type, List<String> args) {

}

