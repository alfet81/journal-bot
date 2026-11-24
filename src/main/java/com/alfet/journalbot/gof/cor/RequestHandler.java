package com.alfet.journalbot.gof.cor;

/**
 * Interface for request handler. Follows design pattern 'Chain of Responsibility'.
 *
 * @param <R> Type of the request.
 */
public interface RequestHandler<R> {

  void setNext(RequestHandler<R> handler);

  void handle(R request);
}
