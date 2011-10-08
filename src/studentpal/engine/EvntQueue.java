package studentpal.engine;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.message.Message;

public class EvntQueue {
  private final static Logger logger = LoggerFactory.getLogger(EvntQueue.class);

  //LinkedBlockingQueue 可设置为无边界的，存取操作也可以设置为非阻塞式的
  //ConcurrentLinkedQueue size方法行为不定
  final static AbstractQueue<Message> _inner_queue = new LinkedBlockingQueue<Message>();

  public static void put(Message evnt) {
    boolean succ = _inner_queue.offer(evnt);
    if (!succ) {
      logger.warn("Message Queue is FULL, skipping...");
    }
  }

  public static Message get() {
    return _inner_queue.poll();
  }

  public static boolean isEmpty() {
    return _inner_queue.isEmpty();
  }
}
